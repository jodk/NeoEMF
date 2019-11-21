package fr.inria.atlanmod.neoemf.data.blueprints;

import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.core.PersistenceFactory;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;
import fr.inria.atlanmod.neoemf.data.blueprints.config.BlueprintsTinkerConfig;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsUriFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Test;

import java.io.File;

public class ResourceTest {
    @Test
    public void uri() {
        URI localUri = new BlueprintsUriFactory().createLocalUri("graph.db");
        System.out.println(localUri);
    }

    @Test
    public void save() throws Exception {
        String path = "src/test/graph";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        URI uri = new BlueprintsUriFactory().createLocalUri(file);
        // Creates the resource
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.createResource(uri);
        // Creates a new configuration builder, used to easily configure the behavior of NeoEMF and the inner database.
        // In addition to the options common to all backends, each builder has its own options.
        ImmutableConfig config = new BlueprintsTinkerConfig()
                // .autoSave(50000)              // A common option
                .log();           // Another common option

        // Load an existing resource (optional)
        resource.load(config.toMap());

        // Do something on the resource
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("relation");
        ePackage.setNsURI("relation-ns");
        EcoreFactory ecoreFactory = new EcoreFactoryImpl();
        EClass eClass = ecoreFactory.createEClass();
        eClass.setName("table");
        ePackage.getEClassifiers().add(eClass);
        PersistentEObject persistentEObject = PersistenceFactory.newInstance(eClass);
        resource.getContents().add(persistentEObject);
        // Save your modifications
        resource.save(config.toMap());
        // resource.unload();

        System.out.println(resource.getContents().size());
    }
}
