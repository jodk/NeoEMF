/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.im;

import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.data.AbstractBackendFactory;
import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.im.config.InMemoryConfig;
import org.eclipse.emf.common.util.URI;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link fr.inria.atlanmod.neoemf.data.BackendFactory} that create {@link fr.inria.atlanmod.neoemf.data.im.InMemoryBackend}
 * instances.
 */

@ParametersAreNonnullByDefault
public class InMemoryBackendFactory extends AbstractBackendFactory<InMemoryConfig> {

    /**
     * Constructs a new {@code InMemoryBackendFactory}.
     */
    public InMemoryBackendFactory() {
        super("im");
    }

    @Nonnull
    @Override
    public Backend createBackend(URI uri, ImmutableConfig baseConfig) {
        return new DefaultInMemoryBackend();
    }
}
