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

package fr.inria.atlanmod.neoemf.io.writer;

import fr.inria.atlanmod.commons.annotation.Beta;
import fr.inria.atlanmod.neoemf.io.util.XmlConstants;

import org.codehaus.stax2.XMLOutputFactory2;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A {@link AbstractXmiStreamWriter} that uses a StAX implementation with cursors for writing XMI files.
 */
@Beta
@ParametersAreNonnullByDefault
public class XmiStreamWriter extends AbstractXmiStreamWriter {

    /**
     * The XML writer on the {@link #target}.
     */
    @Nonnull
    private final XMLStreamWriter writer;

    /**
     * Constructs a new {@code XmiStreamWriter} on the given {@code stream}.
     *
     * @param stream the output stream to write
     */
    public XmiStreamWriter(OutputStream stream) throws IOException {
        super(stream);

        XMLOutputFactory factory = XMLOutputFactory2.newInstance();
        configure(factory);

        try {
            writer = factory.createXMLStreamWriter(new BufferedOutputStream(stream), XmlConstants.ENCODING);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Configures the specified {@code factory} with default options.
     *
     * @param factory the XML factory to configure
     */
    private void configure(XMLOutputFactory factory) {
        // Use namespace support
        factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        // Automatically close empty elements
        factory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, true);
    }

    @Override
    protected void writeStartDocument() throws IOException {
        try {
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeStartElement(String name) throws IOException {
        try {
            writer.writeStartElement(name);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeNamespace(String prefix, String uri) throws IOException {
        try {
            writer.writeNamespace(prefix, uri);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeAttribute(String name, String value) throws IOException {
        try {
            writer.writeAttribute(name, value);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeCharacters(String characters) throws IOException {
        try {
            writer.writeCharacters(characters);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeEndElement() throws IOException {
        try {
            writer.writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeEndDocument() throws IOException {
        try {
            writer.writeEndDocument();
            writer.close();
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}
