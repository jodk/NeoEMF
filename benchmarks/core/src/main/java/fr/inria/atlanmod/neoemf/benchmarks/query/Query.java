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

package fr.inria.atlanmod.neoemf.benchmarks.query;

import fr.inria.atlanmod.neoemf.util.log.Log;

import java.util.concurrent.Callable;

import static java.util.Objects.nonNull;

/**
 * @param <V> the result type of method {@code call}
 */
@FunctionalInterface
public interface Query<V> extends Callable<V> {

    @Override
    V call() throws Exception;

    default V callWithResult() throws Exception {
        V result;

        Log.info("Start query");

        result = call();

        Log.info("End query");

        if (nonNull(result) && !Void.class.isInstance(result)) {
            Log.info("Query returns: {0}", result);
        }

        return result;
    }
}
