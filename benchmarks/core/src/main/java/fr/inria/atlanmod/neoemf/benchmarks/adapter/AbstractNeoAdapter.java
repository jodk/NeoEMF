/*
 * Copyright (c) 2013-2017 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.benchmarks.adapter;

import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.BackendFactory;
import fr.inria.atlanmod.neoemf.data.mapping.DataMapper;
import fr.inria.atlanmod.neoemf.data.store.StoreFactory;
import fr.inria.atlanmod.neoemf.option.PersistenceOptions;
import fr.inria.atlanmod.neoemf.util.UriBuilder;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An abstract {@link Adapter} on top of a {@link fr.inria.atlanmod.neoemf.data.Backend}.
 */
@ParametersAreNonnullByDefault
abstract class AbstractNeoAdapter extends AbstractAdapter {

    /**
     * Constructs a new {@code AbstractNeoAdapter}.
     *
     * @param storeExtension the extension of the resource, used for benchmarks
     */
    protected AbstractNeoAdapter(String storeExtension) {
        super("neoemf", storeExtension + ".resource", org.eclipse.gmt.modisco.java.neoemf.impl.JavaPackageImpl.class);
    }

    /**
     * Returns the {@link BackendFactory} associated to this adapter.
     *
     * @return a factory
     */
    @Nonnull
    protected abstract BackendFactory getFactory();

    @Override
    public boolean supportsMapper() {
        return true;
    }

    @Override
    public DataMapper createMapper(File file, PersistenceOptions options) {
        Map<String, Object> mapOptions = options.withOptions(getOptions()).asMap();

        Backend backend = getFactory().createPersistentBackend(URI.createFileURI(file.getAbsolutePath()), mapOptions);
        return StoreFactory.getInstance().createStore(backend, mapOptions);
    }

    @Nonnull
    @Override
    public Resource createResource(File file, ResourceSet resourceSet) {
        return resourceSet.createResource(UriBuilder.forName(getFactory().name()).fromFile(file));
    }

    @Nonnull
    @Override
    public Map<String, Object> getOptions() {
        return PersistenceOptions.forName(getFactory().name()).asMap();
    }

    @Nonnull
    @Override
    public Resource load(File file, PersistenceOptions options) throws IOException {
        initAndGetEPackage();

        Resource resource = createResource(file, new ResourceSetImpl());
        resource.load(options.withOptions(getOptions()).asMap());

        return resource;
    }

    @Override
    public void unload(Resource resource) {
        if (resource.isLoaded()) {
            resource.unload();
        }
    }
}
