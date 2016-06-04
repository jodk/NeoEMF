/*
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.graph.blueprints.datastore.estores.impl;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;
import fr.inria.atlanmod.neoemf.core.impl.NeoEObjectAdapterFactoryImpl;
import fr.inria.atlanmod.neoemf.datastore.InternalPersistentEObject;
import fr.inria.atlanmod.neoemf.datastore.estores.impl.AbstractDirectWriteResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.graph.blueprints.datastore.BlueprintsPersistenceBackend;
import fr.inria.atlanmod.neoemf.logger.NeoLogger;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.Map;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

public class DirectWriteBlueprintsResourceEStoreImpl extends AbstractDirectWriteResourceEStoreImpl<BlueprintsPersistenceBackend> {

	private static final String SEPARATOR = ":";
	private static final String POSITION = "position";
	private static final String SIZE_LITERAL = "size";

	private static final String CONTAINER = "eContainer";
	private static final String CONTAINING_FEATURE = "containingFeature";

	public DirectWriteBlueprintsResourceEStoreImpl(Resource.Internal resource, BlueprintsPersistenceBackend graph) {
		super(resource, graph);
        NeoLogger.info("DirectWrite Store Created");
	}

	@Override
	protected Object getWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, int index) {
		Vertex vertex = persistenceBackend.getVertex(object);
		String propertyName = eAttribute.getName();
		if (eAttribute.isMany()) {
			checkElementIndex(index, getSize(vertex, eAttribute));
			propertyName += SEPARATOR + index;
		}
		return parseProperty(eAttribute, vertex.getProperty(propertyName));
	}

	@Override
	protected Object getWithReference(InternalPersistentEObject object, EReference eReference, int index) {
		Object returnValue = null;
		Vertex vertex = persistenceBackend.getVertex(object);
		Vertex referencedVertex;
		if (!eReference.isMany()) {
			referencedVertex = Iterables.getOnlyElement(
					vertex.getVertices(Direction.OUT, eReference.getName()), null
			);
		} else {
			checkElementIndex(index, getSize(vertex, eReference));
			referencedVertex = Iterables.getOnlyElement(
					vertex.query()
							.labels(eReference.getName())
							.direction(Direction.OUT)
							.has(POSITION, index)
							.vertices(),
					null);
		}
		if (referencedVertex != null) {
			returnValue = reifyVertex(referencedVertex);
		}
		return returnValue;
	}

	@Override
	protected Object setWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, int index, Object value) {
		Object returnValue;
		if (value == null) {
			returnValue = get(object, eAttribute, index);
			clear(object, eAttribute);
		} else {
			Vertex vertex = persistenceBackend.getOrCreateVertex(object);
			String propertyName = eAttribute.getName();
			if (!eAttribute.isMany()) {
				Object property = vertex.getProperty(propertyName);
				returnValue = parseProperty(eAttribute, property);
			} else {
				checkElementIndex(index, getSize(vertex, eAttribute));
				propertyName += SEPARATOR + index;
				returnValue = vertex.getProperty(propertyName);
			}
			vertex.setProperty(propertyName, serializeToProperty(eAttribute, value));
		}
		return returnValue;
	}

	@Override
	protected Object setWithReference(InternalPersistentEObject object, EReference eReference, int index, PersistentEObject value) {
		Object returnValue = null;
		if (value == null) {
			returnValue = get(object, eReference, index);
			clear(object, eReference);
		} else {
			Vertex vertex = persistenceBackend.getOrCreateVertex(object);
			Vertex newReferencedVertex = persistenceBackend.getOrCreateVertex(value);

			// Update the containment reference if needed
			if (eReference.isContainment()) {
				updateContainment(eReference, vertex, newReferencedVertex);
			}

			if (!eReference.isMany()) {
				Edge edge = Iterables.getOnlyElement(
						vertex.getEdges(Direction.OUT, eReference.getName()), null);
				if (edge != null) {
					Vertex referencedVertex = edge.getVertex(Direction.IN);
					returnValue = reifyVertex(referencedVertex);
					edge.remove();
				}
				vertex.addEdge(eReference.getName(), newReferencedVertex);
			} else {
				checkElementIndex(index, getSize(vertex, eReference));
				Iterable<Edge> edges = vertex.query()
						.labels(eReference.getName())
						.direction(Direction.OUT)
						.has(POSITION, index)
						.edges();

				for (Edge edge : edges) {
					Vertex referencedVertex = edge.getVertex(Direction.IN);
					returnValue = reifyVertex(referencedVertex);
					edge.remove();
				}
				Edge edge = vertex.addEdge(eReference.getName(), newReferencedVertex);
				edge.setProperty(POSITION, index);
			}
		}
		return returnValue;
	}

	@Override
	protected boolean isSetWithAttribute(InternalPersistentEObject object, EAttribute eAttribute) {
		boolean returnValue = false;
		Vertex vertex = persistenceBackend.getVertex(object);
		if (vertex != null) {
			String propertyName = eAttribute.getName();
			if (eAttribute.isMany()) {
				propertyName += SEPARATOR + SIZE_LITERAL;
			}
			returnValue = null != vertex.getProperty(propertyName);
		}
		return returnValue;
	}

	@Override
	protected boolean isSetWithReference(InternalPersistentEObject object, EReference eReference) {
		boolean returnValue = false;
		Vertex vertex = persistenceBackend.getVertex(object);
		if (vertex != null) {
			returnValue = !Iterables.isEmpty(vertex.getVertices(Direction.OUT, eReference.getName()));
		}
		return returnValue;
	}

	@Override
	protected void unsetWithAttribute(InternalPersistentEObject object, EAttribute eAttribute) {
		Vertex vertex = persistenceBackend.getVertex(object);
		String propertyName = eAttribute.getName();
		if (eAttribute.isMany()) {
			propertyName += SEPARATOR + SIZE_LITERAL;
			Integer size = vertex.getProperty(propertyName);
			for (int i = 0; i < size; i++) {
				vertex.removeProperty(eAttribute.getName() + SEPARATOR + i);
			}
		}
		vertex.removeProperty(propertyName);
	}

	@Override
	protected void unsetWithReference(InternalPersistentEObject object, EReference eReference) {
		Vertex vertex = persistenceBackend.getVertex(object);
		if (!eReference.isMany()) {
			Edge edge = Iterables.getOnlyElement(vertex.getEdges(Direction.OUT, eReference.getName()), null);
			if (edge != null) {
				edge.remove();
			}
		} else {
			for (Edge edge : vertex.query().labels(eReference.getName()).direction(Direction.OUT).edges()) {
				edge.remove();
			}
			vertex.removeProperty(eReference.getName() + SEPARATOR + SIZE_LITERAL);
		}
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		Vertex vertex = persistenceBackend.getVertex(object);
		return vertex != null ? getSize(vertex, feature) : 0;
	}

	protected static Integer getSize(Vertex vertex, EStructuralFeature feature) {
		Integer size = vertex.getProperty(feature.getName() + SEPARATOR + SIZE_LITERAL);
		return size != null ? size : 0;
	}

	protected static void setSize(Vertex vertex, EStructuralFeature feature, int size) {
		vertex.setProperty(feature.getName() + SEPARATOR + SIZE_LITERAL, size);
	}

	@Override
	protected boolean containsWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, Object value) {
		return ArrayUtils.contains(toArray(object, eAttribute), value);
	}

	@Override
	protected boolean containsWithReference(InternalPersistentEObject object, EReference eReference, Object value) {
		InternalPersistentEObject eValue = checkNotNull(
				NeoEObjectAdapterFactoryImpl.getAdapter(value, InternalPersistentEObject.class));
		Vertex v = persistenceBackend.getOrCreateVertex(object);
		for (Vertex vOut : v.getVertices(Direction.OUT, eReference.getName())) {
			if (vOut.getId().equals(eValue.id().toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected int indexOfWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, Object value) {
		return ArrayUtils.indexOf(toArray(object, eAttribute), value);
	}

	@Override
	protected int indexOfWithReference(InternalPersistentEObject object, EReference eReference, Object value) {
	    if (value != null) {
			Vertex inVertex = persistenceBackend.getVertex(object);
			Vertex outVertex = persistenceBackend.getVertex((EObject) value);
			for (Edge e : outVertex.getEdges(Direction.IN, eReference.getName())) {
				if (e.getVertex(Direction.OUT).equals(inVertex)) {
					return e.getProperty(POSITION);
				}
			}
	    }
		return ArrayUtils.INDEX_NOT_FOUND;
	}

	@Override
	protected int lastIndexOfWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, Object value) {
		return ArrayUtils.lastIndexOf(toArray(object, eAttribute), value);
	}

	@Override
	protected int lastIndexOfWithReference(InternalPersistentEObject object, EReference eReference, Object value) {
		int resultValue;
		if (value == null) {
			resultValue = ArrayUtils.INDEX_NOT_FOUND;
		} else {
			Vertex inVertex = persistenceBackend.getVertex(object);
			Vertex outVertex = persistenceBackend.getVertex((EObject) value);
			Edge lastPositionEdge = null;
			for (Edge e : outVertex.getEdges(Direction.IN, eReference.getName())) {
				if (e.getVertex(Direction.OUT).equals(inVertex)
						&& (lastPositionEdge == null
						|| (int) e.getProperty(POSITION) > (int) lastPositionEdge.getProperty(POSITION))) {
					lastPositionEdge = e;
				}
			}
			if (lastPositionEdge == null) {
				resultValue = ArrayUtils.INDEX_NOT_FOUND;
			}
			else {
				resultValue = lastPositionEdge.getProperty(POSITION);
			}
		}
		return resultValue;
	}

	@Override
	protected void addWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, int index, Object value) {
		Vertex vertex = persistenceBackend.getOrCreateVertex(object);
		Integer size = getSize(vertex, eAttribute);
		size++;
		setSize(vertex, eAttribute, size);
		checkPositionIndex(index, size);
		for (int i = size - 1; i > index; i--) {
			Object movingProperty = vertex.getProperty(eAttribute.getName() + SEPARATOR + (i - 1));
			vertex.setProperty(eAttribute.getName() + SEPARATOR + i, movingProperty);
		}
		vertex.setProperty(eAttribute.getName() + SEPARATOR + index, serializeToProperty(eAttribute, value));
	}

	@Override
	protected void addWithReference(InternalPersistentEObject object, EReference eReference, int index, PersistentEObject value) {
		Vertex vertex = persistenceBackend.getOrCreateVertex(object);

		Vertex referencedVertex = persistenceBackend.getOrCreateVertex(value);
		// Update the containment reference if needed
		if (eReference.isContainment()) {
			updateContainment(eReference, vertex, referencedVertex);
		}

		Integer size = getSize(vertex, eReference);
		int newSize = size + 1;
		checkPositionIndex(index, newSize);
		if(index != size) {
			Iterable<Edge> edges = vertex.query()
					.labels(eReference.getName())
					.direction(Direction.OUT)
					.interval(POSITION, index, newSize)
					.edges();

			// Avoid unnecessary database access
			for (Edge edge : edges) {
				int position = edge.getProperty(POSITION);
				edge.setProperty(POSITION, position + 1);
			}
		}
		Edge edge = vertex.addEdge(eReference.getName(), referencedVertex);
		edge.setProperty(POSITION, index);

		setSize(vertex, eReference, newSize);
	}

	@Override
	protected Object removeWithAttribute(InternalPersistentEObject object, EAttribute eAttribute, int index) {
		Vertex vertex = persistenceBackend.getVertex(object);
		Integer size = getSize(vertex, eAttribute);
		Object returnValue;
		checkPositionIndex(index, size);
		returnValue = parseProperty(eAttribute, vertex.getProperty(eAttribute.getName() + SEPARATOR + index));
		int newSize = size - 1;
		for (int i = newSize; i > index; i--) {
			Object movingProperty = vertex.getProperty(eAttribute.getName() + SEPARATOR + i);
			vertex.setProperty(eAttribute.getName() + SEPARATOR + (i - 1), movingProperty);
		}
		setSize(vertex, eAttribute, newSize);
		return returnValue;
	}

	@Override
	protected Object removeWithReference(InternalPersistentEObject object, EReference eReference, int index) {
		Vertex vertex = persistenceBackend.getVertex(object);
		String referenceName = eReference.getName();
		Integer size = getSize(vertex, eReference);
		InternalEObject returnValue = null;
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		} else {
			Iterable<Edge> edges = vertex.query()
					.labels(referenceName)
					.direction(Direction.OUT)
					.interval(POSITION, index, size)
					.edges();

			for (Edge edge : edges) {
				int position = edge.getProperty(POSITION);
				if (position == index) {
					Vertex referencedVertex = edge.getVertex(Direction.IN);
					returnValue = reifyVertex(referencedVertex);
					edge.remove();
					if (eReference.isContainment()) {
						for (Edge conEdge : referencedVertex.getEdges(Direction.OUT, CONTAINER)) {
							conEdge.remove();
						}
					}
				} else {
					edge.setProperty(POSITION, position - 1);
				}
			}
		}
		setSize(vertex, eReference, size - 1); // Update size
		checkNotNull(returnValue);
		if(eReference.isContainment()) {
			returnValue.eBasicSetContainer(null, -1, null);
			((InternalPersistentEObject)returnValue).resource(null);
		}
		return returnValue;
	}

	@Override
	protected void clearWithAttribute(InternalPersistentEObject object, EAttribute eAttribute) {
		Vertex vertex = persistenceBackend.getVertex(object);
		Integer size = getSize(vertex, eAttribute);
		for (int i = 0; i < size; i++) {
			vertex.removeProperty(eAttribute.getName() + SEPARATOR + i);
		}
		setSize(vertex, eAttribute, 0);
	}

	@Override
	protected void clearWithReference(InternalPersistentEObject object, EReference eReference) {
		Vertex vertex = persistenceBackend.getOrCreateVertex(object);
		for (Edge edge : vertex.query().labels(eReference.getName()).direction(Direction.OUT).edges()) {
			edge.remove();
		}
		setSize(vertex, eReference, 0);
	}

	@Override
	public InternalEObject getContainer(InternalEObject object) {
		InternalEObject returnValue = null;
		Vertex vertex = persistenceBackend.getVertex(object);
		Vertex containerVertex = Iterables.getOnlyElement(vertex.getVertices(Direction.OUT, CONTAINER), null);
		if (containerVertex != null) {
			returnValue = reifyVertex(containerVertex);
		}
		return returnValue;
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject object) {
		EStructuralFeature resultValue = null;
		Vertex vertex = persistenceBackend.getVertex(object);
		Edge edge = Iterables.getOnlyElement(vertex.getEdges(Direction.OUT, CONTAINER), null);
		if (edge != null) {
			String featureName = edge.getProperty(CONTAINING_FEATURE);
			Vertex containerVertex = edge.getVertex(Direction.IN);
	        if (featureName != null) {
                EObject container = reifyVertex(containerVertex);
				resultValue = container.eClass().getEStructuralFeature(featureName);
			}
		}
		return resultValue;
	}

	protected static void updateContainment(EReference eReference, Vertex parentVertex, Vertex childVertex) {
		for (Edge edge : childVertex.getEdges(Direction.OUT, CONTAINER)) {
			edge.remove();
		}
		Edge edge = childVertex.addEdge(CONTAINER, parentVertex);
		edge.setProperty(CONTAINING_FEATURE, eReference.getName());
	}

	protected InternalEObject reifyVertex(Vertex vertex) {
		return reifyVertex(vertex, null);
	}

	protected InternalEObject reifyVertex(Vertex vertex, EClass eClass) {
		InternalPersistentEObject internalEObject = persistenceBackend.reifyVertex(vertex, eClass);
		if(internalEObject.resource() != resource()) {
			if(Iterables.isEmpty(vertex.getEdges(Direction.OUT, CONTAINER))) {
				if(!Iterables.isEmpty(vertex.getVertices(Direction.IN,"eContents"))) {
					internalEObject.resource(resource());
				}
				// else : not part of the resource
			}
			else {
				internalEObject.resource(resource());
			}
		}
		return internalEObject;
	}
	
	@Override
	public EObject eObject(Id uriFragment) {
		Vertex vertex = persistenceBackend.getVertex(uriFragment);
		return vertex != null ? reifyVertex(vertex) : null;
	}

	@Override
	public EList<EObject> getAllInstances(EClass eClass, boolean strict) {
		Map<EClass, Iterable<Vertex>> indexHits = persistenceBackend.getAllInstances(eClass, strict);
		EList<EObject> instances = new BasicEList<>();
		for(Map.Entry<EClass, Iterable<Vertex>> entry : indexHits.entrySet()) {
			for (Vertex instanceVertex : entry.getValue()) {
				instances.add(reifyVertex(instanceVertex, entry.getKey()));
			}
		}
		return instances;
	}
}
