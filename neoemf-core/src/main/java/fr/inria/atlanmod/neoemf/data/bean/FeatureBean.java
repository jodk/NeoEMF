/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.bean;

import fr.inria.atlanmod.neoemf.core.Id;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * The simple representation of a {@link org.eclipse.emf.ecore.EStructuralFeature} of a {@link
 * fr.inria.atlanmod.neoemf.core.PersistentEObject}.
 */
public interface FeatureBean extends Comparable<FeatureBean>, Serializable {

    /**
     * Returns the identifier of the object using the feature.
     *
     * @return the identifier
     */
    @Nonnull
    Id owner();

    /**
     * Returns the identifier of the feature.
     *
     * @return the name
     */
    int id();

    /**
     * Returns the position of the feature in the {@link #owner()}. If {@code isMany() == false}, then returns {@code
     * -1}.
     *
     * @return the position
     */
    @Nonnegative
    int position();

    /**
     * Checks if this feature is multi-valued.
     *
     * @return {@code true} if this feature is multi-valued
     */
    boolean isMany();
}
