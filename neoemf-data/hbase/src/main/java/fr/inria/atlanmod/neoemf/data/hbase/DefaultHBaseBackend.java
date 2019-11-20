/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.hbase;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.IdConverters;
import fr.inria.atlanmod.neoemf.data.mapping.ManyReferenceMergedAs;
import fr.inria.atlanmod.neoemf.data.mapping.ManyValueWithArrays;
import fr.inria.atlanmod.neoemf.data.mapping.ReferenceAs;
import org.apache.hadoop.hbase.client.Table;
import org.atlanmod.commons.function.Converter;
import org.atlanmod.commons.primitive.Strings;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link HBaseBackend} that use a {@link ManyValueWithArrays} mapping for storing attributes and {@link
 * ReferenceAs}/{@link ManyReferenceMergedAs} mappings for storing references.
 *
 * @see HBaseBackendFactory
 */
@ParametersAreNonnullByDefault
class DefaultHBaseBackend extends AbstractHBaseBackend implements ReferenceAs<String>, ManyValueWithArrays, ManyReferenceMergedAs<String> {

    /**
     * The {@link Converter} used to convert multi-valued references.
     */
    @Nonnull
    private static final Converter<List<Id>, String> MANY_AS_HEXAS = new Converter<List<Id>, String>() {

        /**
         * The {@link String} used to delimit multi-valued references.
         */
        @Nonnull
        private static final String DELIMITER = ";";

        @Nonnull
        private final Converter<Id, String> baseConverter = IdConverters.withHexString();

        @Override
        public String convert(List<Id> ids) {
            return ids.stream()
                    .map(r -> Optional.ofNullable(r).map(baseConverter::convert).orElse(null))
                    .map(Strings::nullToEmpty)
                    .collect(Collectors.joining(DELIMITER));
        }

        @Override
        public List<Id> revert(String s) {
            return Arrays.stream(s.split(DELIMITER))
                    .map(Strings::emptyToNull)
                    .map(baseConverter::revert)
                    .collect(Collectors.toList());
        }
    };

    /**
     * Constructs a new {@code HBaseBackendArrays} on the given {@code table}.
     *
     * @param table the HBase table
     */
    protected DefaultHBaseBackend(Table table) {
        super(table);
    }

    @Nonnull
    @Override
    public Converter<Id, String> referenceConverter() {
        return IdConverters.withHexString();
    }

    @Nonnull
    @Override
    public Converter<List<Id>, String> manyReferenceMerger() {
        return MANY_AS_HEXAS;
    }
}
