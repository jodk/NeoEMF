/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.store;

import fr.inria.atlanmod.commons.annotation.VisibleForReflection;
import fr.inria.atlanmod.commons.log.Level;
import fr.inria.atlanmod.commons.log.Log;
import fr.inria.atlanmod.commons.log.Logger;
import fr.inria.atlanmod.neoemf.data.store.listener.AbstractCallReport;
import fr.inria.atlanmod.neoemf.data.store.listener.BackendReport;
import fr.inria.atlanmod.neoemf.data.store.listener.FailureCallReport;
import fr.inria.atlanmod.neoemf.data.store.listener.StoreListener;
import fr.inria.atlanmod.neoemf.data.store.listener.SuccessCallReport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.Objects.nonNull;

/**
 * A {@link Store} wrapper that logs every calls in the {@link fr.inria.atlanmod.commons.log.Log}.
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("unused") // Called dynamically
// TODO Declare this class as a simple listener, and registers it in a ListeningStore
public class LoggingStore extends AbstractListeningStore implements StoreListener {

    /**
     * The default logging level.
     */
    @Nonnull
    public static final Level DEFAULT_LEVEL = Level.INFO;

    /**
     * The logger to use.
     */
    @Nonnull
    private final Logger logger;

    /**
     * The default {@link Level} for logging.
     */
    @Nonnull
    private final Level level;

    /**
     * Constructs a new {@code LoggingStore}.
     *
     * @param store the inner store
     */
    @VisibleForReflection
    public LoggingStore(Store store) {
        this(store, DEFAULT_LEVEL);
    }

    /**
     * Constructs a new {@code LoggingStore} with the given logging {@code level}.
     *
     * @param store the underlying store
     * @param level the logging level
     */
    @VisibleForReflection
    public LoggingStore(Store store, Level level) {
        super(store);

        this.logger = Log.forName(store.backend().toString());
        this.level = level;
    }

    @Override
    public void onInitialize() {
        // Do nothing
    }

    @Override
    public <K, V, R> void onSuccess(SuccessCallReport<K, V, R> callReport) {
        logger.log(level, toString(callReport));
    }

    @Override
    public <K, V> void onFailure(FailureCallReport<K, V> callReport) {
        logger.error(toString(callReport));
    }

    @Override
    public void onClose() {
        // Do nothing
    }

    /**
     * Returns the string representation of a succeeded call.
     *
     * @param info information about the call
     * @param <K>  the type of the key used during the call; nullable
     * @param <V>  the type of the value used during the call; nullable
     * @param <R>  the type of the result of the call; nullable
     *
     * @return a string
     */
    @Nonnull
    private <K, V, R> String toString(SuccessCallReport<K, V, R> info) {
        StringBuilder sb = initBuilder(info);
        if (nonNull(info.result())) {
            sb.append(" = ").append(info.result());
        }
        return sb.toString();
    }

    /**
     * Returns the string representation of a failed call.
     *
     * @param info information about the call
     * @param <K>  the type of the key used during the call; nullable
     * @param <V>  the type of the value used during the call; nullable
     *
     * @return a string
     */
    @Nonnull
    private <K, V> String toString(FailureCallReport<K, V> info) {
        StringBuilder sb = initBuilder(info);
        sb.append(" but failed");
        if (nonNull(info.thrownException())) {
            sb.append(" with ").append(info.thrownException());
        }
        return sb.toString();
    }

    /**
     * Returns the string representation of a backend.
     *
     * @param backendReport information about the backend
     *
     * @return a string
     */
    @Nonnull
    private String toString(BackendReport backendReport) {
        return "[" + backendReport.name() + "] ";
    }

    /**
     * Returns a string builder, initialized with the representation of a call.
     *
     * @param info information about the call
     * @param <K>  the type of the key used during the call; nullable
     * @param <V>  the type of the value used during the call; nullable
     *
     * @return a initialized string builder
     */
    @Nonnull
    private <K, V> StringBuilder initBuilder(AbstractCallReport<K, V> info) {
        StringBuilder sb = new StringBuilder();

        // Append the name of the concerned backend
        sb.append(toString(info.backend()));

        // Append the name of the called method
        sb.append("Called ").append(info.method()).append("()");

        // Append the key used during the call
        if (nonNull(info.key())) {
            sb.append(" for ").append(info.key());
        }

        // Append the value used during the call
        if (nonNull(info.value())) {
            sb.append(" with ").append(info.value());
        }

        return sb;
    }
}
