/*
 * Copyright (c) 2013-2017 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.eclipse.ui.wizard;

import fr.inria.atlanmod.neoemf.eclipse.ui.importer.NeoModelImporter;

import org.eclipse.emf.converter.ModelConverter;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterDetailPage;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterPackagePage;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterWizard;

public class NeoModelImporterWizard extends ModelImporterWizard {

    @Override
    protected ModelConverter createModelConverter() {
        return new NeoModelImporter();
    }

    @Override
    public void addPages() {
        ModelImporterDetailPage detailPage = new ModelImporterDetailPage(getModelImporter(), "EcoreModel");
        detailPage.setTitle("Ecore &Import");
        detailPage.setDescription(detailPage.showGenModel()
                ? "Specify one or more '.ecore' or '.emof' URIs, try to load them, and choose a file name for the generator model"
                : "Specify one or more '.ecore' or '.emof' URIs and try to load them");
        addPage(detailPage);

        ModelImporterPackagePage packagePage = new ModelImporterPackagePage(getModelImporter(), "EcorePackages");
        packagePage.setShowReferencedGenModels(true);
        addPage(packagePage);
    }
}
