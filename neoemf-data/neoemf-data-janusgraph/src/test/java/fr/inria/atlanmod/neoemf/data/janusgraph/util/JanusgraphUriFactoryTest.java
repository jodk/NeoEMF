package fr.inria.atlanmod.neoemf.data.janusgraph.util;

import fr.inria.atlanmod.neoemf.context.Context;
import fr.inria.atlanmod.neoemf.util.AbstractUriFactoryTest;

import fr.inria.atlanmod.neoemf.data.janusgraph.context.JanusgraphContext;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A test-case about {@link JanusgraphUriFactory}.
 */
@ParametersAreNonnullByDefault
class JanusgraphUriFactoryTest extends AbstractUriFactoryTest {

    @Nonnull
    @Override
    protected Context context() {
        return JanusgraphContext.getDefault();
    }
}
