/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.im.config;

import fr.inria.atlanmod.neoemf.bind.FactoryBinding;
import fr.inria.atlanmod.neoemf.config.BaseConfig;
import fr.inria.atlanmod.neoemf.data.im.InMemoryBackendFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;

/**
 * A {@link fr.inria.atlanmod.neoemf.config.Config} that creates specific configuration for an {@link
 * fr.inria.atlanmod.neoemf.data.im.InMemoryBackend} instance.
 * <p>
 * All features are all optional: configuration can be created using all or none of them.
 */

@FactoryBinding(factory = InMemoryBackendFactory.class)
@ParametersAreNonnullByDefault
public class InMemoryConfig extends BaseConfig<InMemoryConfig> {

    /**
     * Constructs a new {@code InMemoryConfig} with default settings.
     */
    public InMemoryConfig() {
        // Don't set a default mapping for a multi-mapping configuration.
    }

    @Override
    public void save(Path directory) {
        throw new UnsupportedOperationException("An in-memory backend cannot be stored locally");
    }
}
