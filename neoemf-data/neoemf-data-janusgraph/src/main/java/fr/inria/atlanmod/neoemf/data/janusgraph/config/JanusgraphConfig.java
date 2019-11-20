package fr.inria.atlanmod.neoemf.data.janusgraph.config;

import fr.inria.atlanmod.neoemf.bind.FactoryBinding;
import fr.inria.atlanmod.neoemf.config.BaseConfig;
import fr.inria.atlanmod.neoemf.config.Config;

import fr.inria.atlanmod.neoemf.data.janusgraph.JanusgraphBackendFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link fr.inria.atlanmod.neoemf.config.Config} that creates Janusgraph specific configuration.
 * <p>
 * All features are all optional: configuration can be created using all or none of them.
 */
@Component(service = Config.class, scope = ServiceScope.PROTOTYPE)
@FactoryBinding(factory = JanusgraphBackendFactory.class)
@ParametersAreNonnullByDefault
public class JanusgraphConfig extends BaseConfig<JanusgraphConfig> {

    /**
     * Constructs a new {@code JanusgraphConfig}.
     */
    public JanusgraphConfig() {
        withDefault();

        // TODO Declare all default values
    }

    /**
     * Defines the mapping to use for the created {@link fr.inria.atlanmod.neoemf.data.janusgraph.JanusgraphBackend}.
     *
     * @return this configuration (for chaining)
     */
    @Nonnull
    protected JanusgraphConfig withDefault() {
        return setMappingWithCheck("fr.inria.atlanmod.neoemf.data.janusgraph.DefaultJanusgraphBackend", false);
    }

    // TODO Add mapping declarations

    // TODO Add methods specific to your database
}
