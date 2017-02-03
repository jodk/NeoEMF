/*
 * Copyright (c) 2013-2017 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.data.blueprints;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.GraphHelper;
import com.tinkerpop.blueprints.util.wrappers.id.IdEdge;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;
import fr.inria.atlanmod.neoemf.core.StringId;
import fr.inria.atlanmod.neoemf.data.AbstractPersistenceBackend;
import fr.inria.atlanmod.neoemf.data.PersistenceBackend;
import fr.inria.atlanmod.neoemf.data.blueprints.store.DirectWriteBlueprintsCacheManyStore;
import fr.inria.atlanmod.neoemf.data.blueprints.store.DirectWriteBlueprintsStore;
import fr.inria.atlanmod.neoemf.data.structure.ClassInfo;
import fr.inria.atlanmod.neoemf.data.structure.ContainerInfo;
import fr.inria.atlanmod.neoemf.data.structure.FeatureKey;
import fr.inria.atlanmod.neoemf.data.structure.MultivaluedFeatureKey;
import fr.inria.atlanmod.neoemf.util.logging.NeoLogger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A {@link PersistenceBackend} that is responsible of low-level access to a Blueprints database.
 * <p>
 * It wraps an existing Blueprints database and provides facilities to create and retrieve elements, map {@link
 * PersistentEObject}s to {@link Vertex} elements in order to speed up attribute access, and manage a set of lightweight
 * caches to improve access time of {@link Vertex} from  their corresponding {@link PersistentEObject}.
 *
 * @note This class is used in {@link DirectWriteBlueprintsStore} and {@link DirectWriteBlueprintsCacheManyStore} to
 * access and manipulate the database.
 * @note Instances of {@link BlueprintsPersistenceBackend} are created by {@link BlueprintsPersistenceBackendFactory}
 * that provides an usable {@link KeyIndexableGraph} that can be manipulated by this wrapper.
 * @see BlueprintsPersistenceBackendFactory
 * @see DirectWriteBlueprintsStore
 * @see DirectWriteBlueprintsCacheManyStore
 */
public class BlueprintsPersistenceBackend extends AbstractPersistenceBackend {

    /**
     * The literal description of this back-end.
     */
    public static final String NAME = "blueprints";

    /**
     * The property key used to set metaclass name in metaclass {@link Vertex}s.
     */
    public static final String KEY_ECLASS_NAME = EcorePackage.eINSTANCE.getENamedElement_Name().getName();

    /**
     * The property key used to set the {@link EPackage} {@code nsURI} in metaclass {@link Vertex}s.
     */
    public static final String KEY_EPACKAGE_NSURI = EcorePackage.eINSTANCE.getEPackage_NsURI().getName();

    /**
     * The label of type conformance {@link Edge}s.
     */
    public static final String KEY_INSTANCE_OF = "kyanosInstanceOf";

    /**
     * The name of the index entry holding metaclass {@link Vertex}s.
     */
    public static final String KEY_METACLASSES = "metaclasses";

    /**
     * The index key used to retrieve metaclass {@link Vertex}s.
     */
    public static final String KEY_NAME = "name";

    /**
     * The property key used to define the index of an edge.
     */
    protected static final String KEY_POSITION = "position";

    /**
     * The label used to define container {@link Edge}s.
     */
    protected static final String KEY_CONTAINER = "eContainer";

    /**
     * The label used to link root vertex to top-level elements.
     */
    protected static final String KEY_CONTENTS = "eContents";

    /**
     * The property key used to define the opposite containing feature in container {@link Edge}s.
     */
    protected static final String KEY_CONTAINING_FEATURE = "containingFeature";

    /**
     * The property key used to define the number of edges with a specific label.
     */
    protected static final String KEY_SIZE = "size";

    /**
     * In-memory cache that holds recently loaded {@link Vertex}s, identified by the associated object {@link Id}.
     */
    private final Cache<Id, Vertex> verticesCache = Caffeine.newBuilder()
            .initialCapacity(1_000)
            .maximumSize(10_000)
            .build();

    /**
     * List that holds indexed {@link ClassInfo}.
     */
    private final List<ClassInfo> indexedMetaclasses;

    /**
     * Index containing metaclasses.
     */
    private final Index<Vertex> metaclassIndex;

    /**
     * The Blueprints graph.
     */
    private final IdGraph<KeyIndexableGraph> graph;

    /**
     * Whether the underlying database is closed.
     */
    private boolean isClosed = false;

    /**
     * Constructs a new {@code BlueprintsPersistenceBackend} wrapping the provided {@code baseGraph}.
     * <p>
     * This constructor initialize the caches and create the metaclass index.
     *
     * @param baseGraph the base {@link KeyIndexableGraph} used to access the database
     *
     * @note This constructor is protected. To create a new {@code BlueprintsPersistenceBackend} use {@link
     * BlueprintsPersistenceBackendFactory#createPersistentBackend(java.io.File, Map)}.
     * @see BlueprintsPersistenceBackendFactory
     */
    protected BlueprintsPersistenceBackend(KeyIndexableGraph baseGraph) {
        this.graph = new AutoCleanerIdGraph(baseGraph);
        this.indexedMetaclasses = new ArrayList<>();

        Index<Vertex> metaclasses = graph.getIndex(KEY_METACLASSES, Vertex.class);
        if (isNull(metaclasses)) {
            metaclassIndex = graph.createIndex(KEY_METACLASSES, Vertex.class);
        }
        else {
            metaclassIndex = metaclasses;
        }
    }

    /**
     * Builds the {@link Id} used to identify a {@link ClassInfo} {@link Vertex}.
     *
     * @param metaclass the {@link ClassInfo} to build an {@link Id} from
     *
     * @return the create {@link Id}
     */
    private static Id buildId(ClassInfo metaclass) {
        return isNull(metaclass) ? null : new StringId(metaclass.name() + '@' + metaclass.uri());
    }

    /**
     * Formats a property key as {@code prefix:suffix}.
     *
     * @param prefix the prefix of the property key
     * @param suffix the suffix of the property key
     *
     * @return the formatted property key
     */
    private static String formatProperty(String prefix, Object suffix) {
        return prefix + ':' + suffix;
    }

    @Override
    public void save() {
        if (graph.getFeatures().supportsTransactions) {
            graph.commit();
        }
        else {
            graph.shutdown();
        }
    }

    @Override
    public void close() {
        try {
            graph.shutdown();
        }
        catch (Exception e) {
            NeoLogger.warn(e);
        }
        isClosed = true;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    @Override
    public ContainerInfo containerFor(Id id) {
        Vertex containmentVertex = getVertex(id);

        Iterable<Edge> containerEdges = containmentVertex.getEdges(Direction.OUT, KEY_CONTAINER);
        Optional<Edge> containerEdge = StreamSupport.stream(containerEdges.spliterator(), false).findAny();

        if (containerEdge.isPresent()) {
            String featureName = containerEdge.get().getProperty(KEY_CONTAINING_FEATURE);
            Vertex containerVertex = containerEdge.get().getVertex(Direction.IN);
            return ContainerInfo.of(new StringId(containerVertex.getId().toString()), featureName);
        }

        return null;
    }

    @Override
    public void storeContainer(Id id, ContainerInfo container) {
        Vertex containmentVertex = getVertex(id);
        Vertex containerVertex = getVertex(container.id());

        containmentVertex.getEdges(Direction.OUT, KEY_CONTAINER).forEach(Element::remove);

        Edge edge = containmentVertex.addEdge(KEY_CONTAINER, containerVertex);
        edge.setProperty(KEY_CONTAINING_FEATURE, container.name());
    }

    @Override
    public ClassInfo metaclassFor(Id id) {
        Vertex vertex = getVertex(id);

        Iterable<Vertex> metaclassVertices = vertex.getVertices(Direction.OUT, KEY_INSTANCE_OF);
        Optional<Vertex> metaclassVertex = StreamSupport.stream(metaclassVertices.spliterator(), false).findAny();

        return metaclassVertex.map(v -> ClassInfo.of(v.getProperty(KEY_ECLASS_NAME), v.getProperty(KEY_EPACKAGE_NSURI))).orElse(null);
    }

    @Override
    public void storeMetaclass(Id id, ClassInfo metaclass) {
        Vertex metaclassVertex = Iterables.getOnlyElement(metaclassIndex.get(KEY_NAME, metaclass.name()), null);

        if (isNull(metaclassVertex)) {
            metaclassVertex = addVertex(metaclass);
            metaclassIndex.put(KEY_NAME, metaclass.name(), metaclassVertex);
            indexedMetaclasses.add(metaclass);
        }

        Vertex vertex = getVertex(id);
        vertex.addEdge(KEY_INSTANCE_OF, metaclassVertex);
    }

    @Override
    public Object getValue(FeatureKey key) {
        return getVertex(key.id()).getProperty(key.name());
    }

    @Override
    public Object setValue(FeatureKey key, Object value) {
        Object oldValue = getValue(key);

        getVertex(key.id()).setProperty(key.name(), value);

        return oldValue;
    }

    @Override
    public void unsetValue(FeatureKey key) {
        getVertex(key.id()).removeProperty(key.name());
    }

    @Override
    public boolean hasValue(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        return nonNull(vertex) && nonNull(vertex.getProperty(key.name()));
    }

    @Override
    public void addValue(MultivaluedFeatureKey key, Object value) {
        Integer size = sizeOf(key);
        int newSize = size + 1;

        Vertex vertex = getVertex(key.id());

        // TODO Replace by Stream
        for (int i = size; i > key.position(); i--) {
            vertex.setProperty(formatProperty(key.name(), i), vertex.getProperty(formatProperty(key.name(), (i - 1))));
        }

        vertex.setProperty(formatProperty(key.name(), key.position()), value);

        sizeOf(key, newSize);
    }

    @Override
    public Object removeValue(MultivaluedFeatureKey key) {
        Integer size = sizeOf(key);
        int newSize = size - 1;

        Vertex vertex = getVertex(key.id());

        Object previousValue = vertex.getProperty(formatProperty(key.name(), key.position()));

        // TODO Replace by Stream
        for (int i = newSize; i > key.position(); i--) {
            vertex.setProperty(formatProperty(key.name(), i - 1), vertex.getProperty(formatProperty(key.name(), i)));
        }

        sizeOf(key, newSize);

        return previousValue;
    }

    @Override
    public void cleanValue(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        IntStream.range(0, sizeOf(key))
                .forEach(i -> vertex.removeProperty(formatProperty(key.name(), i)));

        sizeOf(key, 0);
    }

    @Override
    public Iterable<Object> valueAsList(FeatureKey key) {
        return getVertex(key.id()).getProperty(key.name());
    }

    @Override
    public Id getReference(FeatureKey key) {
        Iterable<Vertex> referencedVertices = getVertex(key.id()).getVertices(Direction.OUT, key.name());
        Optional<Vertex> referencedVertex = StreamSupport.stream(referencedVertices.spliterator(), false).findAny();

        return referencedVertex.map(v -> new StringId(v.getId().toString())).orElse(null);
    }

    @Override
    public Id setReference(FeatureKey key, Id id) {
        Vertex vertex = getVertex(key.id());

        Iterable<Edge> referenceEdges = vertex.getEdges(Direction.OUT, key.name());
        Optional<Edge> referenceEdge = StreamSupport.stream(referenceEdges.spliterator(), false).findAny();

        Id previousId = null;
        if (referenceEdge.isPresent()) {
            Vertex previouslyReferencedVertex = referenceEdge.get().getVertex(Direction.IN);
            previousId = new StringId(previouslyReferencedVertex.getId().toString());
            referenceEdge.get().remove();
        }

        getVertex(key.id()).addEdge(key.name(), getVertex(id));

        return previousId;
    }

    @Override
    public void unsetReference(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        Iterable<Edge> referenceEdges = vertex.getEdges(Direction.OUT, key.name());
        Optional<Edge> referenceEdge = StreamSupport.stream(referenceEdges.spliterator(), false).findAny();

        referenceEdge.ifPresent(Element::remove);
    }

    @Override
    public boolean hasReference(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        return nonNull(vertex) && StreamSupport.stream(vertex.getVertices(Direction.OUT, key.name()).spliterator(), false).findAny().isPresent();
    }

    @Override
    public void addReference(MultivaluedFeatureKey key, Id id) {
        Integer size = sizeOf(key);
        int newSize = size + 1;

        Vertex vertex = getVertex(key.id());

        if (key.position() != size) {
            Iterable<Edge> edges = vertex.query()
                    .labels(key.name())
                    .direction(Direction.OUT)
                    .interval(KEY_POSITION, key.position(), newSize)
                    .edges();

            edges.forEach(e -> e.setProperty(KEY_POSITION, e.<Integer>getProperty(KEY_POSITION) + 1));
        }

        Edge edge = vertex.addEdge(key.name(), getVertex(id));
        edge.setProperty(KEY_POSITION, key.position());

        sizeOf(key, newSize);
    }

    @Override
    public Id removeReference(MultivaluedFeatureKey key) {
        Integer size = sizeOf(key);
        int newSize = size - 1;

        Vertex vertex = getVertex(key.id());

        Iterable<Edge> edges = vertex.query()
                .labels(key.name())
                .direction(Direction.OUT)
                .interval(KEY_POSITION, key.position(), size)
                .edges();

        Id previousId = null;
        for (Edge edge : edges) {
            int position = edge.getProperty(KEY_POSITION);

            if (position != key.position()) {
                edge.setProperty(KEY_POSITION, position - 1);
            }
            else {
                Vertex referencedVertex = edge.getVertex(Direction.IN);
                previousId = new StringId(referencedVertex.getId().toString());
                edge.remove();

                referencedVertex.getEdges(Direction.OUT, KEY_CONTAINER).forEach(Element::remove);
            }
        }

        sizeOf(key, newSize);

        return previousId;
    }

    @Override
    public void cleanReference(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        Iterable<Edge> edges = vertex.query()
                .labels(key.name())
                .direction(Direction.OUT)
                .edges();

        edges.forEach(Element::remove);

        sizeOf(key, 0);
    }

    @Override
    public Iterable<Id> referenceAsList(FeatureKey key) {
        Iterable<Vertex> referencedVertices = getVertex(key.id()).getVertices(Direction.OUT, key.name());

        return StreamSupport.stream(referencedVertices.spliterator(), false)
                .map(v -> new StringId(v.getId().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getValueAtIndex(MultivaluedFeatureKey key) {
        return getVertex(key.id()).getProperty(formatProperty(key.name(), key.position()));
    }

    public Object setValueAtIndex(MultivaluedFeatureKey key, Object value) {
        Object oldValue = getValueAtIndex(key);

        getVertex(key.id()).setProperty(formatProperty(key.name(), key.position()), value);

        return oldValue;
    }

    @Override
    public void unsetValueAtIndex(FeatureKey key) {
        Vertex vertex = getVertex(key.id());
        String property = formatProperty(key.name(), KEY_SIZE);

        IntStream.range(0, vertex.getProperty(property))
                .forEach(i -> vertex.removeProperty(formatProperty(key.name(), i)));

        vertex.removeProperty(property);
    }

    public boolean hasValueAtIndex(FeatureKey key) {
        Vertex vertex = getVertex(key.id());
        return nonNull(vertex) && nonNull(vertex.getProperty(formatProperty(key.name(), KEY_SIZE)));
    }

    @Override
    public Iterable<Object> valueAtIndexAsList(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        return IntStream.range(0, sizeOf(key))
                .mapToObj(i -> vertex.getProperty(formatProperty(key.name(), i)))
                .collect(Collectors.toList());
    }

    @Override
    public Id getReferenceAtIndex(MultivaluedFeatureKey key) {
        Vertex vertex = getVertex(key.id());

        Iterable<Vertex> referencedVertices = vertex.query()
                .labels(key.name())
                .direction(Direction.OUT)
                .has(KEY_POSITION, key.position())
                .vertices();

        Optional<Vertex> referencedVertex = StreamSupport.stream(referencedVertices.spliterator(), false).findAny();

        return referencedVertex.map(v -> new StringId(v.getId().toString())).orElse(null);
    }

    @Override
    public Id setReferenceAtIndex(MultivaluedFeatureKey key, Id id) {
        Vertex vertex = getVertex(key.id());

        Iterable<Edge> edges = vertex.query()
                .labels(key.name())
                .direction(Direction.OUT)
                .has(KEY_POSITION, key.position())
                .edges();

        Optional<Edge> previousEdge = StreamSupport.stream(edges.spliterator(), false).findAny();

        Id previousId = null;
        if (previousEdge.isPresent()) {
            Vertex referencedVertex = previousEdge.get().getVertex(Direction.IN);
            previousId = new StringId(referencedVertex.getId().toString());
            previousEdge.get().remove();
        }

        Edge edge = getVertex(key.id()).addEdge(key.name(), getVertex(id));
        edge.setProperty(KEY_POSITION, key.position());

        return previousId;
    }

    @Override
    public void unsetReferenceAtIndex(FeatureKey key) {
        Vertex vertex = getVertex(key.id());

        Iterable<Edge> edges = vertex.query()
                .labels(key.name())
                .direction(Direction.OUT)
                .edges();

        StreamSupport.stream(edges.spliterator(), false).forEach(Element::remove);
        vertex.removeProperty(formatProperty(key.name(), KEY_SIZE));
    }

    @Override
    public boolean hasReferenceAtIndex(FeatureKey key) {
        return hasReference(key);
    }

    @Override
    public Iterable<Id> referenceAtIndexAsList(FeatureKey key) {
        Iterable<Edge> edges = getVertex(key.id()).query()
                .labels(key.name())
                .direction(Direction.OUT)
                .edges();

        Comparator<Edge> byPosition = Comparator.comparingInt(e -> e.getProperty(KEY_POSITION));

        return StreamSupport.stream(edges.spliterator(), false)
                .sorted(byPosition)
                .map(e -> new StringId(e.getVertex(Direction.IN).getId().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public int sizeOf(FeatureKey key) {
        Vertex vertex = getVertex(key.id());
        if (isNull(vertex)) {
            return 0;
        }

        Integer size = vertex.getProperty(formatProperty(key.name(), KEY_SIZE));
        return isNull(size) ? 0 : size;
    }

    /**
     * Defines the {@code size} of the property identified by the given {@code key}.
     *
     * @param key the feature key identifying the property
     * @param size the new size
     */
    protected void sizeOf(FeatureKey key, int size) {
        getVertex(key.id()).setProperty(formatProperty(key.name(), KEY_SIZE), size);
    }

    @Override
    public Map<EClass, Iterable<Vertex>> getAllInstances(EClass eClass, boolean strict) {
        Map<EClass, Iterable<Vertex>> indexHits;

        // There is no strict instance of an abstract class
        if (eClass.isAbstract() && strict) {
            indexHits = Collections.emptyMap();
        }
        else {
            indexHits = new HashMap<>();
            Set<EClass> eClassToFind = new HashSet<>();
            eClassToFind.add(eClass);

            // Find all the concrete subclasses of the given EClass (the metaclass index only stores concretes EClass)
            if (!strict) {
                eClass.getEPackage().getEClassifiers()
                        .stream()
                        .filter(EClass.class::isInstance)
                        .map(EClass.class::cast)
                        .filter(c -> eClass.isSuperTypeOf(c) && !c.isAbstract())
                        .forEach(eClassToFind::add);
            }

            // Get all the vertices that are indexed with one of the EClass
            for (EClass ec : eClassToFind) {
                Vertex metaClassVertex = Iterables.getOnlyElement(metaclassIndex.get(KEY_NAME, ec.getName()), null);
                if (nonNull(metaClassVertex)) {
                    Iterable<Vertex> instanceVertexIterable = metaClassVertex.getVertices(Direction.IN, KEY_INSTANCE_OF);
                    indexHits.put(ec, instanceVertexIterable);
                }
                else {
                    NeoLogger.warn("Metaclass {0} not found in index", ec.getName());
                }
            }
        }
        return indexHits;
    }

    /**
     * Create a new vertex, add it to the graph, and return the newly created vertex.
     *
     * @param id the identifier of the {@link Vertex}
     *
     * @return the newly created vertex
     */
    public Vertex addVertex(Id id) {
        return graph.addVertex(id.toString());
    }

    /**
     * Create a new vertex, add it to the graph, and return the newly created vertex. The issued {@link EClass} is used
     * to calculate the {@link Vertex} {@code id}.
     *
     * @param metaclass The corresponding {@link EClass}
     *
     * @return the newly created vertex
     */
    private Vertex addVertex(ClassInfo metaclass) {
        Vertex vertex = addVertex(buildId(metaclass));
        vertex.setProperty(KEY_ECLASS_NAME, metaclass.name());
        vertex.setProperty(KEY_EPACKAGE_NSURI, metaclass.uri());
        return vertex;
    }

    /**
     * Returns the vertex corresponding to the provided {@code id}. If no vertex corresponds to that {@code id}, then
     * return {@code null}.
     *
     * @param id the {@link Id} of the element to find
     *
     * @return the vertex referenced by the provided {@link EObject} or {@code null} when no such vertex exists
     */
    public Vertex getVertex(Id id) {
        return verticesCache.get(id, key -> graph.getVertex(key.toString()));
    }

    /**
     * Returns the vertex corresponding to the provided {@link ClassInfo}. If no vertex corresponds to that
     * {@link ClassInfo}, then return {@code null}.
     *
     * @param metaclass the {@link ClassInfo} to find
     *
     * @return the vertex corresponding to the provided {@link ClassInfo} or {@code null} when no such vertex exists
     */
    private Vertex getVertex(ClassInfo metaclass) {
        return getVertex(buildId(metaclass));
    }

    /**
     * Return the vertex corresponding to the provided {@link PersistentEObject}. If no vertex corresponds to that
     * {@link EObject}, then the corresponding {@link Vertex} together with its {@link #KEY_INSTANCE_OF} relationship is
     * created.
     *
     * @param object the {@link PersistentEObject} to find
     *
     * @return the vertex referenced by the provided {@link EObject} or {@code null} when no such vertex exists
     */
    public Vertex getOrCreateVertex(PersistentEObject object) {
        Vertex vertex;
        if (object.isMapped()) {
            vertex = getVertex(object.id());
        }
        else {
            vertex = addVertex(object.id());
            object.setMapped(true);
            verticesCache.put(object.id(), vertex);
        }
        return vertex;
    }

    /**
     * Copies all the contents of this {@code PersistenceBackend} to the {@code target} one.
     *
     * @param target the {@code PersistenceBackend} to copy the database contents to
     */
    public void copyTo(BlueprintsPersistenceBackend target) {
        GraphHelper.copyGraph(graph, target.graph);

        for (ClassInfo metaclass : indexedMetaclasses) {
            checkArgument(Iterables.isEmpty(target.metaclassIndex.get(KEY_NAME, metaclass.name())), "Index is not consistent");
            target.metaclassIndex.put(KEY_NAME, metaclass.name(), getVertex(metaclass));
        }
    }

    /**
     * Provides a direct access to the underlying graph.
     * <p>
     * This method is public for tool compatibility (see the
     * <a href="https://github.com/atlanmod/Mogwai">Mogwaï</a>) framework, NeoEMF consistency is not guaranteed if
     * the graph is modified manually.
     *
     * @return the underlying Blueprints {@link IdGraph}
     */
    public IdGraph<KeyIndexableGraph> getGraph() {
        return graph;
    }

    /**
     * An {@link IdGraph} that automatically removes unused {@link Vertex}.
     */
    private static class AutoCleanerIdGraph extends IdGraph<KeyIndexableGraph> {

        /**
         * Constructs a new {@code AutoCleanerIdGraph} on the specified {@code baseGraph}.
         *
         * @param baseGraph the base graph
         */
        public AutoCleanerIdGraph(KeyIndexableGraph baseGraph) {
            super(baseGraph);
        }

        @Override
        public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
            return createFrom(super.addEdge(id, outVertex, inVertex, label));
        }

        @Override
        public Edge getEdge(Object id) {
            return createFrom(super.getEdge(id));
        }

        /**
         * Creates a new {@link AutoCleanerIdEdge} from another {@link Edge}.
         *
         * @param edge the base edge
         *
         * @return an {@link AutoCleanerIdEdge}
         */
        private Edge createFrom(Edge edge) {
            return isNull(edge) ? null : new AutoCleanerIdEdge(edge);
        }

        /**
         * An {@link IdEdge} that automatically removes {@link Vertex} that are no longer referenced.
         */
        private class AutoCleanerIdEdge extends IdEdge {

            /**
             * Constructs a new {@code AutoCleanerIdEdge} on the specified {@code edge}.
             *
             * @param edge the base edge
             */
            public AutoCleanerIdEdge(Edge edge) {
                super(edge, AutoCleanerIdGraph.this);
            }

            /**
             * {@inheritDoc}
             * <p>
             * If the {@link Edge} references a {@link Vertex} with no more incoming {@link Edge}, the referenced
             * {@link Vertex} is removed as well.
             */
            @Override
            public void remove() {
                Vertex referencedVertex = getVertex(Direction.IN);
                super.remove();
                if (Iterables.isEmpty(referencedVertex.getEdges(Direction.IN))) {
                    // If the Vertex has no more incoming edges remove it from the DB
                    referencedVertex.remove();
                }
            }
        }
    }
}
