package fr.inria.atlanmod.neoemf.data.janusgraph;

import fr.inria.atlanmod.neoemf.context.Context;
import fr.inria.atlanmod.neoemf.data.AbstractBackendFactoryTest;

import fr.inria.atlanmod.neoemf.data.janusgraph.config.JanusgraphConfig;
import fr.inria.atlanmod.neoemf.data.janusgraph.context.JanusgraphContext;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A test-case about {@link JanusgraphBackendFactory}.
 */
@ParametersAreNonnullByDefault
class JanusgraphBackendFactoryTest extends AbstractBackendFactoryTest {

    @Nonnull
    @Override
    protected Context context() {
        return JanusgraphContext.getDefault();
    }

    @Nonnull
    @Override
    protected Stream<Arguments> allMappings() {
        return Stream.of(
                Arguments.of(new JanusgraphConfig(), DefaultJanusgraphBackend.class)

                // TODO Fill with other mappings
        );
    }
}
