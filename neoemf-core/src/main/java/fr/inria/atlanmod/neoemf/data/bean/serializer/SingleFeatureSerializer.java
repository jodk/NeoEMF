/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.bean.serializer;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.data.bean.SingleFeatureBean;
import org.atlanmod.commons.io.serializer.AbstractBinarySerializer;
import org.atlanmod.commons.io.serializer.BinarySerializer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A {@link BinarySerializer} for {@link SingleFeatureBean}s.
 */
@ParametersAreNonnullByDefault
final class SingleFeatureSerializer extends AbstractBinarySerializer<SingleFeatureBean> {

    private static final long serialVersionUID = -6425763366179041775L;

    @Override
    public void serialize(SingleFeatureBean feature, @WillNotClose DataOutput out) throws IOException {
        out.writeLong(feature.owner().toLong());
        out.writeInt(feature.id());
    }

    @Nonnull
    @Override
    public SingleFeatureBean deserialize(@WillNotClose DataInput in) throws IOException {
        return SingleFeatureBean.of(
                Id.getProvider().fromLong(in.readLong()),
                in.readInt()
        );
    }
}
