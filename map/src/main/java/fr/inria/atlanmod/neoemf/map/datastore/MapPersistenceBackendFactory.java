/*
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.map.datastore;

import fr.inria.atlanmod.neoemf.datastore.AbstractPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.datastore.InvalidDataStoreException;
import fr.inria.atlanmod.neoemf.datastore.PersistenceBackend;
import fr.inria.atlanmod.neoemf.datastore.estores.SearcheableResourceEStore;
import fr.inria.atlanmod.neoemf.logger.NeoLogger;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.AutocommitMapResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.CachedManyDirectWriteMapResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapResourceWithListsEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapWithIndexesResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.resources.MapResourceOptions;
import fr.inria.atlanmod.neoemf.map.util.NeoMapURI;
import fr.inria.atlanmod.neoemf.resources.PersistentResource;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class MapPersistenceBackendFactory extends AbstractPersistenceBackendFactory {

    public static final String MAPDB_BACKEND = "mapdb";
    
	@Override
	public PersistenceBackend createTransientBackend() {
	    Engine mapEngine = DBMaker.newMemoryDB().makeEngine();
		return new MapPersistenceBackend(mapEngine);
	}

	@Override
	public SearcheableResourceEStore createTransientEStore(
			PersistentResource resource, PersistenceBackend backend) {
		checkArgument(backend instanceof DB, "Trying to create a Map-based EStore with an invalid backend");
		return new DirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
	}

	@Override
	public PersistenceBackend createPersistentBackend(File file, Map<?, ?> options) throws InvalidDataStoreException {
	    File dbFile = FileUtils.getFile(NeoMapURI.createNeoMapURI(URI.createFileURI(file.getAbsolutePath()).appendSegment("neoemf.mapdb")).toFileString());
	    if (!dbFile.getParentFile().exists()) {
			try {
				Files.createDirectories(dbFile.getParentFile().toPath());
			} catch (IOException e) {
				NeoLogger.error(e);
			}
		}
	    PropertiesConfiguration neoConfig;
	    Path neoConfigPath = Paths.get(file.getAbsolutePath()).resolve(NEO_CONFIG_FILE);
        try {
            neoConfig= new PropertiesConfiguration(neoConfigPath.toFile());
        } catch (ConfigurationException e) {
            throw new InvalidDataStoreException(e);
        }
        if (!neoConfig.containsKey(BACKEND_PROPERTY)) {
            neoConfig.setProperty(BACKEND_PROPERTY, MAPDB_BACKEND);
        }
		try {
            neoConfig.save();
        } catch(ConfigurationException e) {
            NeoLogger.error(e);
        }
	    //Engine mapEngine = DBMaker.newFileDB(dbFile).cacheLRUEnable().mmapFileEnableIfSupported().asyncWriteEnable().makeEngine();
	    /*
         * TODO Check the difference when asyncWriteEnable() is set.
         * It has been desactived for MONDO deliverable but not well tested
         */
	    Engine mapEngine = DBMaker.newFileDB(dbFile).cacheLRUEnable().mmapFileEnableIfSupported().makeEngine();
        return new MapPersistenceBackend(mapEngine);
	}

	@Override
	protected SearcheableResourceEStore internalCreatePersistentEStore(
			PersistentResource resource, PersistenceBackend backend, Map<?,?> options) throws InvalidDataStoreException {
		checkArgument(backend instanceof DB, "Trying to create a Map-based EStore with an invalid backend");
		SearcheableResourceEStore returnValue;
		@SuppressWarnings("unchecked")
		List<PersistentResourceOptions.StoreOption> storeOptions = (List<PersistentResourceOptions.StoreOption>)options.get(PersistentResourceOptions.STORE_OPTIONS);
        if(storeOptions == null || storeOptions.isEmpty() || storeOptions.contains(MapResourceOptions.EStoreMapOption.DIRECT_WRITE)) {
			// Default store
			returnValue = new DirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
        }
        else {
            if(storeOptions.contains(MapResourceOptions.EStoreMapOption.AUTOCOMMIT)) {
				returnValue = new AutocommitMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
            }
            else if(storeOptions.contains(MapResourceOptions.EStoreMapOption.CACHED_MANY)) {
				returnValue = new CachedManyDirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
            }
            else if(storeOptions.contains(MapResourceOptions.EStoreMapOption.DIRECT_WRITE_WITH_LISTS)) {
				returnValue = new DirectWriteMapResourceWithListsEStoreImpl(resource, (MapPersistenceBackend)backend);
            }
            else if(storeOptions.contains(MapResourceOptions.EStoreMapOption.DIRECT_WRITE_WITH_INDEXES)) {
				returnValue = new DirectWriteMapWithIndexesResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
            }
            else {
                throw new InvalidDataStoreException();
            }
        }
		return returnValue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void copyBackend(PersistenceBackend from, PersistenceBackend to) {
		checkArgument(from instanceof MapPersistenceBackend, "The backend to copy is not an instance of MapPersistenceBackend");
		checkArgument(to instanceof MapPersistenceBackend, "The target copy backend is not an instance of MapPersistenceBackend");
	    MapPersistenceBackend mapFrom = (MapPersistenceBackend)from;
	    MapPersistenceBackend mapTo = (MapPersistenceBackend)to;
	    for(Map.Entry<String, Object> entry : mapFrom.getAll().entrySet()) {
	        Object collection = entry.getValue();
	        if(collection instanceof Map) {
                Map fromMap = (Map)collection;
                Map toMap = mapTo.getHashMap(entry.getKey());
	            toMap.putAll(fromMap);
	        }
	        else {
	            throw new UnsupportedOperationException("Cannot copy Map backend: store type " + collection.getClass().getSimpleName() + " is not supported");
	        }
	    }
	}
}
