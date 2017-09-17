package fr.inria.atlanmod.neoemf.core.internal;

import fr.inria.atlanmod.commons.Copier;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;
import fr.inria.atlanmod.neoemf.data.store.adapter.StoreAdapter;
import fr.inria.atlanmod.neoemf.util.EObjects;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A {@link Copier} that recursively copies the content related to a {@link PersistentEObject}, from a {@link
 * StoreAdapter} to another, by using the EMF methods.
 */
@ParametersAreNonnullByDefault
public final class ContentsCopier implements Copier<StoreAdapter> {

    @Nonnull
    private final PersistentEObject object;

    /**
     * Contructs a new {@code ContentsCopier} for the specified {@code object}.
     *
     * @param object the object to copy
     */
    public ContentsCopier(PersistentEObject object) {
        this.object = object;
    }

    @Override
    public void copy(StoreAdapter source, StoreAdapter target) {
        PersistentEObject container = source.getContainer(object);
        if (nonNull(container)) {
            target.updateContainment(object, source.getContainingFeature(object), container);
        }
        else {
            target.removeContainment(object);
        }

        object.eClass().getEAllStructuralFeatures().forEach(feature -> {
            if (!feature.isMany()) {
                Object value = source.get(object, feature, InternalEObject.EStore.NO_INDEX);
                if (nonNull(value)) {
                    if (requireAttachment(feature)) {
                        value = attach(value);
                    }

                    target.set(object, feature, InternalEObject.EStore.NO_INDEX, value);
                }
            }
            else {
                List<Object> values = source.getAll(object, feature);
                if (!values.isEmpty()) {
                    if (requireAttachment(feature)) {
                        values = values.stream().map(this::attach).collect(Collectors.toList());
                    }

                    target.setAll(object, feature, values);
                }
            }
        });
    }

    /**
     * Checks that a {@code feature} is candidate for attachment.
     *
     * @param feature the feature to test
     *
     * @return {@code true} is the {@code feature} is candidate for attachment
     *
     * @see #attach(Object)
     */
    private boolean requireAttachment(EStructuralFeature feature) {
        return EObjects.isReference(feature) && EObjects.asReference(feature).isContainment();
    }

    /**
     * Attachs the {@code value} to {@link PersistentEObject#resource()} if it is assignable to a {@link
     * PersistentEObject}.
     *
     * @param value the value to attach
     *
     * @return the {@code value}
     *
     * @see PersistentEObject#resource(Resource.Internal)
     */
    @Nullable
    private Object attach(@Nullable Object value) {
        if (isNull(value)) {
            return null;
        }

        PersistentEObject o = PersistentEObject.from(value);
        o.resource(object.resource());
        return o;
    }
}