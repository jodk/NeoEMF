package fr.inria.atlanmod.neoemf.data.janusgraph.util;

import fr.inria.atlanmod.neoemf.bind.FactoryBinding;
import fr.inria.atlanmod.neoemf.util.AbstractUriFactory;
import fr.inria.atlanmod.neoemf.util.UriFactory;

import fr.inria.atlanmod.neoemf.data.janusgraph.JanusgraphBackendFactory;

import org.osgi.service.component.annotations.Component;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link fr.inria.atlanmod.neoemf.util.UriFactory} that creates Janusgraph specific resource {@link org.eclipse.emf.common.util.URI}s.
 *
 * @see JanusgraphBackendFactory
 * @see fr.inria.atlanmod.neoemf.data.BackendFactoryRegistry
 * @see fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory
 */
@Component(service = UriFactory.class)
@FactoryBinding(factory = JanusgraphBackendFactory.class)
@ParametersAreNonnullByDefault
public class JanusgraphUriFactory extends AbstractUriFactory {

    /**
     * Constructs a new {@code JanusgraphUriFactory}.
     */
    public JanusgraphUriFactory() {
        super(true, false);
    }
}
