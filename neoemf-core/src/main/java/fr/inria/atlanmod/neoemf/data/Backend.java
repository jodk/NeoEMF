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

package fr.inria.atlanmod.neoemf.data;

import fr.inria.atlanmod.neoemf.data.mapping.DataMapper;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link DataMapper} that stores data.
 */
@ParametersAreNonnullByDefault
public interface Backend extends DataMapper {

    /**
     * Returns whether this back-end is persistent, i.e., if it is stored in a database.
     *
     * @return {@code true} if the back-end is persistent, {@code false} otherwise.
     */
    boolean isPersistent();

    /**
     * Returns whether this back-end is distributed.
     *
     * @return {@code true} if the back-end is distributed, {@code false} otherwise.
     */
    boolean isDistributed();
}
