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

package fr.inria.atlanmod.neoemf.data.store;

import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.mapping.AbstractMapperDecorator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link Store} that translates model-level operations into datastore calls.
 */
@ParametersAreNonnullByDefault
public class DirectWriteStore extends AbstractMapperDecorator<Backend> implements Store {

    /**
     * Constructs a new {@code DirectWriteStore} between the given {@code resource} and the {@code backend}.
     *
     * @param backend the back-end used to store the model
     */
    public DirectWriteStore(Backend backend) {
        super(backend);
    }

    @Nonnull
    @Override
    public Backend backend() {
        return next();
    }
}