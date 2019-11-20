package fr.inria.atlanmod.neoemf.data.janusgraph;

import fr.inria.atlanmod.neoemf.data.Backend;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link fr.inria.atlanmod.neoemf.data.Backend} that is responsible of low-level access to a Janusgraph database.
 * <p>
 * It wraps an existing Janusgraph database and provides facilities to create and retrieve elements.
 * <p>
 * <b>Note:</b> Instances of {@code JanusgraphBackend} are created by {@link JanusgraphBackendFactory} that
 * provides an usable database that can be manipulated by this wrapper.
 *
 * @see JanusgraphBackendFactory
 */
@ParametersAreNonnullByDefault
public interface JanusgraphBackend extends Backend {

    @Override
    default boolean isPersistent() {
        // TODO Implement this method
        return true;
    }

    @Override
    default boolean isDistributed() {
        // TODO Implement this method
        return false;
    }
}
