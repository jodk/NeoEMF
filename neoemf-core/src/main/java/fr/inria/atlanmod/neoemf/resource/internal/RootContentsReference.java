/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.resource.internal;

import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.impl.EClassifierImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The {@link org.eclipse.emf.ecore.EReference} that represents the {@link Resource#getContents()} feature.
 */
@ParametersAreNonnullByDefault
final class RootContentsReference extends EReferenceImpl {

    /**
     * The classifier of all {@link RootContentsReference} instances.
     */
    @Nonnull
    private static final EClassifier CLASSIFIER = new EClassifierImpl() {};

    /**
     * Constructs a new {@code RootContentsReference}.
     */
    public RootContentsReference() {
        setFeatureID(PersistentResource.ROOT_REFERENCE_ID);
        setName(PersistentResource.ROOT_REFERENCE_NAME);
        setLowerBound(0);
        setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
        setEType(CLASSIFIER);
    }
}
