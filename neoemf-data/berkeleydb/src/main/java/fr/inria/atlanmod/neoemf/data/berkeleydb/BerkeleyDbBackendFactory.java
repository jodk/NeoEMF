/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.berkeleydb;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import fr.inria.atlanmod.neoemf.data.AbstractBackendFactory;
import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.berkeleydb.config.BerkeleyDbConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link fr.inria.atlanmod.neoemf.data.BackendFactory} that creates {@link BerkeleyDbBackend} instances.
 */

@ParametersAreNonnullByDefault
public class BerkeleyDbBackendFactory extends AbstractBackendFactory<BerkeleyDbConfig> {

    /**
     * Constructs a new {@code BerkeleyDbBackendFactory}.
     */
    public BerkeleyDbBackendFactory() {
        super("berkeleydb");
    }

    @Nonnull
    @Override
    protected Backend createLocalBackend(Path directory, BerkeleyDbConfig config) throws Exception {
        if (!directory.toFile().exists()) {
            Files.createDirectories(directory);
        }

        final boolean isReadOnly = config.isReadOnly();

        EnvironmentConfig environmentConfig = new EnvironmentConfig()
                .setAllowCreate(!isReadOnly)
                .setReadOnly(isReadOnly);

        DatabaseConfig databaseConfig = new DatabaseConfig()
                .setAllowCreate(!isReadOnly)
                .setReadOnly(isReadOnly)
                .setSortedDuplicates(false)
                .setDeferredWrite(true);

        Environment environment = new Environment(directory.toFile(), environmentConfig);

        return createMapper(config.getMapping(), environment, databaseConfig);
    }
}
