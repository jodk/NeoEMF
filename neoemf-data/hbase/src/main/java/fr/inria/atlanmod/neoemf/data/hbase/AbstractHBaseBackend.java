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

package fr.inria.atlanmod.neoemf.data.hbase;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.data.AbstractPersistenceBackend;
import fr.inria.atlanmod.neoemf.data.hbase.util.serializer.ObjectSerializer;
import fr.inria.atlanmod.neoemf.data.structure.ContainerValue;
import fr.inria.atlanmod.neoemf.data.structure.FeatureKey;
import fr.inria.atlanmod.neoemf.data.structure.MetaclassValue;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

/**
 *
 */
@ParametersAreNonnullByDefault
abstract class AbstractHBaseBackend extends AbstractPersistenceBackend implements HBaseBackend {

    /**
     * The column family holding properties.
     */
    protected static final byte[] PROPERTY_FAMILY = Bytes.toBytes("p");

    /**
     * The column family holding instances.
     */
    protected static final byte[] TYPE_FAMILY = Bytes.toBytes("t");

    /**
     * The column family holding containments.
     */
    protected static final byte[] CONTAINMENT_FAMILY = Bytes.toBytes("c");

    /**
     * The column qualifier holding the URI of metamodels.
     */
    private static final byte[] METAMODEL_QUALIFIER = Bytes.toBytes("m");

    /**
     * The column qualifier holding the name of classes.
     */
    private static final byte[] ECLASS_QUALIFIER = Bytes.toBytes("e");

    /**
     * The column qualifier holding the identifier of containers.
     */
    private static final byte[] CONTAINER_QUALIFIER = Bytes.toBytes("n");

    /**
     * The column qualifier holding the name of the feature used to retrieve the containment.
     */
    private static final byte[] CONTAINING_FEATURE_QUALIFIER = Bytes.toBytes("g");

    /**
     * The HBase table used to access the model.
     */
    protected final Table table;

    /**
     * Constructs a new {@code AbstractHBaseBackend} on th given {@code table}
     *
     * @param table the HBase table
     */
    protected AbstractHBaseBackend(Table table) {
        this.table = checkNotNull(table);
    }

    @Override
    public void save() {
        // TODO Implement this method
    }

    @Override
    public void close() {
        // TODO Implement this method
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isDistributed() {
        return true;
    }

    @Override
    public void create(Id id) {
        // Do nothing
    }

    @Override
    public boolean has(Id id) {
        return false;
    }

    @Nonnull
    @Override
    public Optional<ContainerValue> containerOf(Id id) {
        return fromDatabase(id)
                .map(result -> {
                    byte[] byteId = result.getValue(CONTAINMENT_FAMILY, CONTAINER_QUALIFIER);
                    byte[] byteName = result.getValue(CONTAINMENT_FAMILY, CONTAINING_FEATURE_QUALIFIER);
                    if (nonNull(byteId) && nonNull(byteName)) {
                        return Optional.of(ContainerValue.of(ObjectSerializer.deserialize(byteId), ObjectSerializer.deserialize(byteName)));
                    }
                    return Optional.<ContainerValue>empty();
                })
                .orElse(Optional.empty());
    }

    @Override
    public void containerFor(Id id, ContainerValue container) {
        try {
            Put put = new Put(ObjectSerializer.serialize(id));
            put.addColumn(CONTAINMENT_FAMILY, CONTAINER_QUALIFIER, ObjectSerializer.serialize(container.id()));
            put.addColumn(CONTAINMENT_FAMILY, CONTAINING_FEATURE_QUALIFIER, ObjectSerializer.serialize(container.name()));
            table.put(put);
        }
        catch (IOException ignore) {
        }
    }

    @Nonnull
    @Override
    public Optional<MetaclassValue> metaclassOf(Id id) {
        return fromDatabase(id)
                .map(result -> {
                    byte[] byteName = result.getValue(TYPE_FAMILY, ECLASS_QUALIFIER);
                    byte[] byteUri = result.getValue(TYPE_FAMILY, METAMODEL_QUALIFIER);
                    if (nonNull(byteName) && nonNull(byteUri)) {
                        return Optional.of(MetaclassValue.of(ObjectSerializer.deserialize(byteName), ObjectSerializer.deserialize(byteUri)));
                    }
                    return Optional.<MetaclassValue>empty();
                })
                .orElse(Optional.empty());
    }

    @Override
    public void metaclassFor(Id id, MetaclassValue metaclass) {
        try {
            Put put = new Put(ObjectSerializer.serialize(id));
            put.addColumn(TYPE_FAMILY, ECLASS_QUALIFIER, ObjectSerializer.serialize(metaclass.name()));
            put.addColumn(TYPE_FAMILY, METAMODEL_QUALIFIER, ObjectSerializer.serialize(metaclass.uri()));
            table.put(put);
        }
        catch (IOException ignore) {
        }
    }

    /**
     * Retrieves a value from the {@link Table} according to the given {@code key}.
     *
     * @param key the key of the element to retrieve
     *
     * @return on {@link Optional} containing the element, or an empty {@link Optional} if the element has not been
     * found
     */
    protected <V> Optional<V> fromDatabase(FeatureKey key) {
        return fromDatabase(key.id())
                .map(result -> {
                    Optional<byte[]> value = Optional.ofNullable(result.getValue(PROPERTY_FAMILY, ObjectSerializer.serialize(key.name())));
                    return value.map(bytes -> Optional.<V>of(ObjectSerializer.deserialize(bytes))).orElse(Optional.empty());
                })
                .orElse(Optional.empty());
    }

    /**
     * Saves a {@code value} identified by the {@code key} in the {@link Table}.
     *
     * @param key   the key of the element to save
     * @param value the value to save
     */
    protected <V> void toDatabase(FeatureKey key, V value) {
        try {
            Put put = new Put(ObjectSerializer.serialize(key.id()));
            put.addColumn(PROPERTY_FAMILY, ObjectSerializer.serialize(key.name()), ObjectSerializer.serialize(value));
            table.put(put);
        }
        catch (IOException ignore) {
        }
    }

    /**
     * Removes a value from the {@link Table} according to its {@code key}.
     *
     * @param key the key of the element to remove
     */
    protected void outDatabase(FeatureKey key) {
        try {
            Delete delete = new Delete(ObjectSerializer.serialize(key.id()));
            delete.addColumn(PROPERTY_FAMILY, ObjectSerializer.serialize(key.name()));
            table.delete(delete);
        }
        catch (IOException ignore) {
        }
    }

    /**
     * Retrieves a raw value from the {@link Table} according to the given {@code id}.
     *
     * @param id the identifier of the {@link Result} to retrieve
     *
     * @return on {@link Optional} containing the {@link Result}, or an empty {@link Optional} if the element has not
     * been found
     */
    private Optional<Result> fromDatabase(Id id) {
        Optional<Result> value = Optional.empty();

        try {
            Get get = new Get(ObjectSerializer.serialize(id));
            Result result = table.get(get);
            if (!result.isEmpty()) {
                value = Optional.of(result);
            }
        }
        catch (IOException ignore) {
        }

        return value;
    }
}