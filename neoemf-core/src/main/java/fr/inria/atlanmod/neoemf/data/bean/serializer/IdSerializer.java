/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.bean.serializer;

import fr.inria.atlanmod.neoemf.core.Id;
import org.atlanmod.commons.io.serializer.AbstractBinarySerializer;
import org.atlanmod.commons.io.serializer.BinarySerializer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A {@link BinarySerializer} for {@link Id}s.
 */
@ParametersAreNonnullByDefault
final class IdSerializer extends AbstractBinarySerializer<Id> {

    private static final long serialVersionUID = 4870583637926304450L;

    @Override
    public void serialize(Id id, @WillNotClose DataOutput out) throws IOException {
        out.writeLong(id.toLong());
    }

    @Nonnull
    @Override
    public Id deserialize(@WillNotClose DataInput in) throws IOException {
        return Id.getProvider().fromLong(in.readLong());
    }
}
