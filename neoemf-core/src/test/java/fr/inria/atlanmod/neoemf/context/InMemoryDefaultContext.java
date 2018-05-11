/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.context;

import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.data.im.config.InMemoryConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The default {@link AbstractInMemoryContext}.
 */
@ParametersAreNonnullByDefault
public class InMemoryDefaultContext extends AbstractInMemoryContext {

    @Nonnull
    @Override
    public ImmutableConfig config() {
        return new InMemoryConfig();
    }
}