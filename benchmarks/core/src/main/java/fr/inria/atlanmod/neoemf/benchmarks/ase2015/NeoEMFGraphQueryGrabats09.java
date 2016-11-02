/*
 * Copyright (c) 2013-2016 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.benchmarks.ase2015;

import fr.inria.atlanmod.neoemf.benchmarks.ase2015.queries.ASE2015JavaQueries;
import fr.inria.atlanmod.neoemf.benchmarks.util.MessageUtil;
import fr.inria.atlanmod.neoemf.datastore.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.graph.blueprints.datastore.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.graph.blueprints.resources.BlueprintsResourceOptions;
import fr.inria.atlanmod.neoemf.graph.blueprints.util.NeoBlueprintsURI;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceFactory;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions.StoreOption;
import fr.inria.atlanmod.neoemf.resources.impl.PersistentResourceImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmt.modisco.java.ClassDeclaration;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeoEMFGraphQueryGrabats09 {

    private static final Logger LOG = Logger.getLogger(NeoEMFGraphQueryGrabats09.class.getName());

    private static final String IN = "input";

    private static final String EPACKAGE_CLASS = "epackage_class";

    private static final String OPTIONS_FILE = "options_file";

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder(IN)
                .argName("INPUT")
                .desc("Input NeoEMF resource directory")
                .numberOfArgs(1)
                .required()
                .build());

        options.addOption(Option.builder(EPACKAGE_CLASS)
                .argName("CLASS")
                .desc("FQN of EPackage implementation class")
                .numberOfArgs(1)
                .required()
                .build());

        options.addOption(Option.builder(OPTIONS_FILE)
                .argName("FILE")
                .desc("Properties file holding the options to be used in the NeoEMF Resource")
                .numberOfArgs(1)
                .build());

        CommandLineParser parser = new DefaultParser();

        try {
            PersistenceBackendFactoryRegistry.register(NeoBlueprintsURI.NEO_GRAPH_SCHEME, BlueprintsPersistenceBackendFactory.getInstance());

            CommandLine commandLine = parser.parse(options, args);

            URI uri = NeoBlueprintsURI.createNeoGraphURI(new File(commandLine.getOptionValue(IN)));

            Class<?> inClazz = NeoEMFGraphQueryGrabats09.class.getClassLoader().loadClass(commandLine.getOptionValue(EPACKAGE_CLASS));
            inClazz.getMethod("init").invoke(null);

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(NeoBlueprintsURI.NEO_GRAPH_SCHEME, PersistentResourceFactory.getInstance());

            Resource resource = resourceSet.createResource(uri);

            Map<String, Object> loadOpts = new HashMap<>();

            if (commandLine.hasOption(OPTIONS_FILE)) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(new File(commandLine.getOptionValue(OPTIONS_FILE))));
                for (final Entry<Object, Object> entry : properties.entrySet()) {
                    loadOpts.put((String) entry.getKey(), entry.getValue());
                }
            }

            // Add the LoadedObjectCounter store
            List<StoreOption> storeOptions = new ArrayList<>();
//			storeOptions.add(PersistentResourceOptions.EStoreOption.LOADED_OBJECT_COUNTER_LOGGING);
            storeOptions.add(BlueprintsResourceOptions.EStoreGraphOption.AUTOCOMMIT);
            loadOpts.put(PersistentResourceOptions.STORE_OPTIONS, storeOptions);
            resource.load(loadOpts);
            {
                Runtime.getRuntime().gc();
                long initialUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                LOG.log(Level.INFO, MessageFormat.format("Used memory before query: {0}", MessageUtil.byteCountToDisplaySize(initialUsedMemory)));
                LOG.log(Level.INFO, "Start query");
                long begin = System.currentTimeMillis();
                EList<ClassDeclaration> list = ASE2015JavaQueries.grabats09(resource);
                long end = System.currentTimeMillis();
                LOG.log(Level.INFO, "End query");
                LOG.log(Level.INFO, MessageFormat.format("Query result contains {0} elements", list.size()));
                LOG.log(Level.INFO, MessageFormat.format("Time spent: {0}", MessageUtil.formatMillis(end - begin)));
                Runtime.getRuntime().gc();
                long finalUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                LOG.log(Level.INFO, MessageFormat.format("Used memory after query: {0}", MessageUtil.byteCountToDisplaySize(finalUsedMemory)));
                LOG.log(Level.INFO, MessageFormat.format("Memory use increase: {0}", MessageUtil.byteCountToDisplaySize(finalUsedMemory - initialUsedMemory)));
            }

            if (resource instanceof PersistentResourceImpl) {
                PersistentResourceImpl.shutdownWithoutUnload((PersistentResourceImpl) resource);
            }
            else {
                resource.unload();
            }
        }
        catch (ParseException e) {
            MessageUtil.showError(e.toString());
            MessageUtil.showError("Current arguments: " + Arrays.toString(args));
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar <this-file.jar>", options, true);
        }
        catch (Throwable e) {
            MessageUtil.showError(e.toString());
        }
    }
}
