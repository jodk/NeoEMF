/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data;

import fr.inria.atlanmod.neoemf.config.Config;
import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import org.eclipse.emf.common.util.URI;

import javax.annotation.MatchesPattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A factory that creates {@link Backend} instances.
 * <p>
 * The creation can be configured using {@link fr.inria.atlanmod.neoemf.resource.PersistentResource#save(java.util.Map)}
 * and {@link fr.inria.atlanmod.neoemf.resource.PersistentResource#load(java.util.Map)} option maps.
 *
 * @see Config
 */
@ParametersAreNonnullByDefault
public interface BackendFactory {

    /**
     * Returns the literal description of the created {@link Backend}.
     *
     * @return the literal description of the created {@link Backend}
     */
    @MatchesPattern("^[a-z]+$")
    String name();

    /**
     * Returns whether the {@link Backend}s created by this factory support the transient state.
     *
     * @return {@code true} if the {@link Backend}s created by this factory support the transient state, {@code false}
     * otherwise
     */
    boolean supportsTransient();

    /**
     * Creates a new {@link Backend} located by the {@code uri}.
     *
     * @param uri        the {@link URI} where to store the back-end
     * @param baseConfig the base configuration that defines the behaviour of the back-end
     *
     * @return a new back-end
     *
     * @throws InvalidBackendException if an error occurs during the back-end initialization
     */
    @Nonnull
    Backend createBackend(URI uri, ImmutableConfig baseConfig);
}
