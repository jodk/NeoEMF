package fr.inria.atlanmod.neoemf.data.janusgraph;

import fr.inria.atlanmod.neoemf.context.Context;
import fr.inria.atlanmod.neoemf.data.mapping.AbstractDataMapperTest;

import fr.inria.atlanmod.neoemf.data.janusgraph.context.JanusgraphContext;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A test-case about {@link DefaultJanusgraphBackend}.
 */
@ParametersAreNonnullByDefault
class DefaultJanusgraphBackendTest extends AbstractDataMapperTest {

    @Nonnull
    @Override
    protected Context context() {
        return JanusgraphContext.getDefault();
    }
}
