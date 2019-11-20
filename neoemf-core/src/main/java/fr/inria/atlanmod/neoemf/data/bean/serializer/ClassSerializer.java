/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.bean.serializer;

import fr.inria.atlanmod.neoemf.data.bean.ClassBean;
import org.atlanmod.commons.io.serializer.AbstractBinarySerializer;
import org.atlanmod.commons.io.serializer.BinarySerializer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A {@link BinarySerializer} for {@link ClassBean}s.
 */
@ParametersAreNonnullByDefault
final class ClassSerializer extends AbstractBinarySerializer<ClassBean> {

    private static final long serialVersionUID = 2381621024821659111L;

    @Override
    public void serialize(ClassBean metaClass, @WillNotClose DataOutput out) throws IOException {
        out.writeUTF(metaClass.name());
        out.writeUTF(metaClass.uri());
    }

    @Nonnull
    @Override
    public ClassBean deserialize(@WillNotClose DataInput in) throws IOException {
        return ClassBean.of(
                in.readUTF(),
                in.readUTF()
        );
    }
}
