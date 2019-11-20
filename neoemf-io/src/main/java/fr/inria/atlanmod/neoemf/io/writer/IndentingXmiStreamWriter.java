/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io.writer;

import org.atlanmod.commons.primitive.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An indenting {@link XmiStreamWriter}.
 */
@ParametersAreNonnullByDefault
public class IndentingXmiStreamWriter extends XmiStreamWriter {

    /**
     * A LIFO that holds the ownerchip of each elements. If an element has at least one child, its associated value is
     * {@code true}.
     *
     * @see #writeEndElement()
     */
    @Nonnull
    private final Deque<AtomicBoolean> hasChildren = new ArrayDeque<>();

    /**
     * The base string used to indent the file.
     */
    @Nonnull
    private final String indent;

    /**
     * The current indentation level.
     */
    @Nonnegative
    private int level;

    /**
     * Constructs a new {@code IndentingXmiStreamWriter} on the given {@code stream}.
     *
     * @param stream the output stream to write
     *
     * @throws IOException if an I/O error occurs when writing
     */
    public IndentingXmiStreamWriter(OutputStream stream) throws IOException {
        this(stream, 2);
    }

    /**
     * Constructs a new {@code IndentingXmiStreamWriter} on the given {@code stream}.
     *
     * @param stream the output stream to write
     *
     * @throws IOException if an I/O error occurs when writing
     */
    public IndentingXmiStreamWriter(OutputStream stream, @Nonnegative int indent) throws IOException {
        super(stream);

        this.indent = IntStream.range(0, indent).mapToObj(i -> Strings.SPACE).collect(Collectors.joining());
        this.hasChildren.addLast(new AtomicBoolean());
    }

    @Override
    protected void writeStartElement(String name) throws IOException {
        try {
            hasChildren.getLast().set(true);
            hasChildren.addLast(new AtomicBoolean());

            writer.writeCharacters(indent());

            level++;

            super.writeStartElement(name);
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeEndElement() throws IOException {
        try {
            --level;

            if (hasChildren.removeLast().get()) {
                writer.writeCharacters(indent());
            }

            super.writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Returns an indentation string.
     *
     * @return an indentation string
     */
    @Nonnull
    private String indent() {
        return IntStream.range(0, level)
                .mapToObj(i -> indent)
                .collect(Collectors.joining(Strings.EMPTY, Strings.LR, Strings.EMPTY));
    }
}
