/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io.writer;

import fr.inria.atlanmod.commons.Throwables;
import fr.inria.atlanmod.commons.annotation.Beta;
import fr.inria.atlanmod.commons.primitive.Strings;
import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.io.bean.BasicAttribute;
import fr.inria.atlanmod.neoemf.io.bean.BasicElement;
import fr.inria.atlanmod.neoemf.io.bean.BasicMetaclass;
import fr.inria.atlanmod.neoemf.io.bean.BasicNamespace;
import fr.inria.atlanmod.neoemf.io.bean.BasicReference;
import fr.inria.atlanmod.neoemf.io.processor.ValueConverter;
import fr.inria.atlanmod.neoemf.io.util.XmiConstants;

import org.eclipse.emf.ecore.EAttribute;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AbstractStreamWriter} that writes data into an XMI file.
 */
@Beta
@ParametersAreNonnullByDefault
public abstract class AbstractXmiStreamWriter extends AbstractStreamWriter {

    /**
     * Constructs a new {@code AbstractXmiStreamWriter} with the given {@code stream}.
     *
     * @param stream the stream where to write data
     */
    protected AbstractXmiStreamWriter(OutputStream stream) {
        super(stream);
    }

    @Override
    public final void onInitialize() {
        try {
            writeStartDocument();
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public final void onComplete() {
        try {
            writeEndDocument();
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public final void onStartElement(BasicElement element) {
        super.onStartElement(element);

        BasicMetaclass metaClass = element.metaClass();
        BasicNamespace ns = metaClass.ns();

        try {
            if (element.isRoot()) {
                writeStartElement(XmiConstants.format(ns.prefix(), element.name()));

                // Namespaces
                writeNamespace(ns.prefix(), ns.uri());
                writeNamespace(XmiConstants.XMI_NS, XmiConstants.XMI_URI);

                // XMI version
                writeAttribute(XmiConstants.XMI_VERSION_ATTR, XmiConstants.XMI_VERSION);
            }
            else {
                writeStartElement(element.name());
            }

            // TODO Write the meta-class only if EReference#getEType() != EClass
            writeAttribute(XmiConstants.XMI_TYPE, XmiConstants.format(metaClass.ns().prefix(), metaClass.name()));

            writeAttribute(XmiConstants.XMI_ID, element.id().toHexString());
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public final void onEndElement() {
        super.onEndElement();

        try {
            writeEndElement();
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public final void onAttribute(BasicAttribute attribute, List<Object> values) {
        try {
            EAttribute eAttribute = attribute.eFeature();
            if (!attribute.isMany()) {
                writeAttribute(attribute.name(), ValueConverter.INSTANCE.revert(values.get(0), eAttribute));
            }
            else {
                // TODO Check the behavior of multi-valued attributes
                for (Object v : values) {
                    writeStartElement(attribute.name());
                    writeCharacters(ValueConverter.INSTANCE.revert(v, eAttribute));
                    writeEndElement();
                }
            }
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public final void onReference(BasicReference reference, List<Id> values) {
        if (reference.isContainment()) {
            return;
        }

        try {
            if (!reference.isMany()) {
                writeAttribute(reference.name(), values.get(0).toHexString());
            }
            else {
                writeAttribute(reference.name(), values.stream().map(Id::toHexString).collect(Collectors.joining(Strings.SPACE)));
            }
        }
        catch (IOException e) {
            handleException(e);
        }
    }

    /**
     * Writes the start of a document, including the general header.
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeStartDocument() throws IOException;

    /**
     * Writes the start of an element {@code name}
     *
     * @param name the name of the element
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeStartElement(String name) throws IOException;

    /**
     * Writes a namespace.
     *
     * @param prefix the prefix of the namespace
     * @param uri    the URI of the namespace
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeNamespace(String prefix, String uri) throws IOException;

    /**
     * Writes an attribute of the current element.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeAttribute(String name, String value) throws IOException;

    /**
     * Writes characters.
     *
     * @param characters the characters
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeCharacters(String characters) throws IOException;

    /**
     * Writes the end of the current element.
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeEndElement() throws IOException;

    /**
     * Writes the end of the document and finalizes the migration.
     *
     * @throws IOException if an I/O error occurs when writing
     */
    protected abstract void writeEndDocument() throws IOException;

    /**
     * TODO
     *
     * @param e the exception to handle
     */
    private void handleException(IOException e) {
        throw Throwables.wrap(e, RuntimeException.class);
    }
}
