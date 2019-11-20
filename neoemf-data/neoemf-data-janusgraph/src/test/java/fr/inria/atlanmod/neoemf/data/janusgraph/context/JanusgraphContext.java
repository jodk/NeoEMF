package fr.inria.atlanmod.neoemf.data.janusgraph.context;

import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.context.AbstractLocalContext;
import fr.inria.atlanmod.neoemf.context.Context;
import fr.inria.atlanmod.neoemf.data.BackendFactory;

import fr.inria.atlanmod.neoemf.data.janusgraph.JanusgraphBackendFactory;
import fr.inria.atlanmod.neoemf.data.janusgraph.config.JanusgraphConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A specific {@link Context} for the Janusgraph implementation.
 */
@ParametersAreNonnullByDefault
public abstract class JanusgraphContext extends AbstractLocalContext {

    /**
     * Creates a new {@code BerkeleyDbContext}.
     *
     * @return a new context.
     */
    @Nonnull
    public static Context getDefault() {
        return new JanusgraphContext() {
            @Nonnull
            @Override
            public ImmutableConfig config() {
                return new JanusgraphConfig();
            }
        };
    }

    @Nonnull
    @Override
    public String name() {
        return "Janusgraph";
    }

    @Nonnull
    @Override
    public BackendFactory factory() {
        return new JanusgraphBackendFactory();
    }
}
