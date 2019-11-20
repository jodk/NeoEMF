/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.mongodb.document;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.IdConverters;
import fr.inria.atlanmod.neoemf.data.bean.SingleFeatureBean;
import org.atlanmod.commons.function.Converter;
import org.bson.codecs.pojo.annotations.BsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a container.
 */
@ParametersAreNonnullByDefault
public class ContainerDocument {

    /**
     * The field name of the owner of a container.
     */
    @Nonnull
    public static final String F_OWNER = "owner";

    /**
     * The field name of the containing feature.
     */
    @Nonnull
    public static final String F_ID = "id";

    // TODO Use the converter of the calling Backend
    @Nonnull
    private static final Converter<Id, String> CONVERTER = IdConverters.withHexString();

    /**
     * The identifier of the owner of this container.
     */
    @BsonProperty(F_OWNER)
    private String owner;

    /**
     * The identifier of the containing feature.
     */
    @BsonProperty(F_ID)
    private int id;

    /**
     * Converts the specified {@code bean} in a {@link ContainerDocument}.
     *
     * @param bean the bean
     *
     * @return the document
     */
    @Nonnull
    public static ContainerDocument fromBean(SingleFeatureBean bean) {
        ContainerDocument c = new ContainerDocument();
        c.setOwner(CONVERTER.convert(bean.owner()));
        c.setId(bean.id());
        return c;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Converts this document in a {@link SingleFeatureBean}.
     *
     * @return a bean
     */
    @Nonnull
    public SingleFeatureBean toBean() {
        return SingleFeatureBean.of(CONVERTER.revert(owner), id);
    }
}
