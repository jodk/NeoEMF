package fr.inria.atlanmod.neoemf.data.janusgraph;

import fr.inria.atlanmod.neoemf.data.AbstractBackendFactory;
import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.BackendFactory;

import fr.inria.atlanmod.neoemf.data.janusgraph.config.JanusgraphConfig;

import org.osgi.service.component.annotations.Component;

import java.net.URL;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link fr.inria.atlanmod.neoemf.data.BackendFactory} that creates {@link JanusgraphBackend} instances.
 */
@Component(service = BackendFactory.class)
@ParametersAreNonnullByDefault
public class JanusgraphBackendFactory extends AbstractBackendFactory<JanusgraphConfig> {

    /**
     * Constructs a new {@code JanusgraphBackendFactory}.
     */
    public JanusgraphBackendFactory() {
        super("janusgraph");
    }

    @Nonnull
    @Override
    protected Backend createLocalBackend(Path directory, JanusgraphConfig config) throws Exception {
        final boolean isReadOnly = config.isReadOnly();

        // TODO Start/Create the database

        return createMapper(config.getMapping());
    }

    @Nonnull
    @Override
    protected Backend createRemoteBackend(URL url, JanusgraphConfig config) throws Exception {
        final boolean isReadOnly = config.isReadOnly();

        // TODO Start/Create the database

        return createMapper(config.getMapping());
    }
}
