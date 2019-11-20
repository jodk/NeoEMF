/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.core.internal.collect;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.ListIterator;

/**
 * A stateless helper to iterate on a {@link ListIterator}.
 */
@ParametersAreNonnullByDefault
interface IterationHelper {

    /**
     * Returns {@code true} if the {@code iterator} has more elements when traversing the list in the defined
     * direction.
     *
     * @param iterator an iterator
     * @param <E>      the type of elements in the iterator
     *
     * @return {@code true} if the {@code iterator} has more elements
     *
     * @see ListIterator#hasNext()
     * @see ListIterator#hasPrevious()
     */
    <E> boolean hasMoreElements(ListIterator<E> iterator);

    /**
     * Returns the next element in the {@code iterator} when traversing the list in the defined direction.
     *
     * @param iterator an iterator
     * @param <E>      the type of elements in the iterator
     *
     * @return the next element
     *
     * @see ListIterator#next()
     * @see ListIterator#previous()
     */
    @Nonnull
    <E> E advance(ListIterator<E> iterator);

    /**
     * Moves back the {@code iterator}. This is the reverse operation of {@link #advance(ListIterator)}.
     *
     * @param iterator an iterator
     * @param <E>      the type of elements in the iterator
     */
    <E> void reverse(ListIterator<E> iterator);

    /**
     * Adapts the current cursor of an iterator when traversing the list in the defined direction.
     *
     * @param cursor the cursor; the current position on an iterator
     *
     * @return the adapted cursor
     */
    @Nonnegative
    int adaptCursor(int cursor);

    /**
     * Returns the next cursor of an iterator when traversing the list in the defined direction.
     *
     * @param cursor the cursor; the current position on an iterator
     *
     * @return the next cursor
     */
    @Nonnegative
    int advanceCursor(int cursor);

    /**
     * Returns the first position to use when creating a new {@link ListIterator}.
     *
     * @param list the list on which the list iterator will be created
     *
     * @return the first position
     *
     * @see List#listIterator(int)
     */
    @Nonnegative
    int getFirstIndex(List<?> list);
}
