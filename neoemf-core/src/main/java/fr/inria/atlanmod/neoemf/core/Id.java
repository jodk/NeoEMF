/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.core;

import fr.inria.atlanmod.neoemf.core.internal.LongIdProvider;
import fr.inria.atlanmod.neoemf.core.internal.UndefinedId;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * An identifier used to identify {@link PersistentEObject}s in datastores.
 * <p>
 * This is the primary key in datastores, so, each {@code Id} must be unique at {@link
 * fr.inria.atlanmod.neoemf.data.mapping.DataMapper} level to ensure data consistency. The unicity is not guaranteed
 * before adding a new {@link PersistentEObject} to a database.
 */
@Immutable
@ParametersAreNonnullByDefault
public interface Id extends Comparable<Id>, Serializable {

    /**
     * An immutable undefined {@link Id}.
     */
    @Nonnull
    Id UNDEFINED = new UndefinedId();

    /**
     * Returns the instance of the default {@link IdProvider}.
     *
     * @return the instance
     */
    @Nonnull
    static IdProvider getProvider() {
        return LongIdProvider.getInstance();
    }

    /**
     * Returns the {@link Long} representation of this {@code Id}.
     *
     * @return a long
     */
    long toLong();

    /**
     * Returns the {@link String} representation of this {@code Id}, as an hexadecimal value.
     *
     * @return a hexadecimal string
     */
    @Nonnull
    String toHexString();
}
