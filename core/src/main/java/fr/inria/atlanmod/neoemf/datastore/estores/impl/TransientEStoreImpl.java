/*
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.datastore.estores.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link InternalEObject.EStore} implementation that uses collections to store the data in memory.
 */
public class TransientEStoreImpl implements InternalEObject.EStore {

	protected Map<EStoreKey, Object> singleMap;
	protected Map<EStoreKey, List<Object>> manyMap;

	public TransientEStoreImpl() {
		singleMap = new HashMap<>();
		manyMap = new HashMap<>();
	}

	@Override
	public Object get(InternalEObject eObject, EStructuralFeature feature, int index) {
		Object returnValue;
		EStoreKey entry = new EStoreKey(eObject, feature);
		if (index == NO_INDEX) {
			returnValue = singleMap.get(entry);
		} else {
			List<Object> saved = manyMap.get(entry);
			if (saved != null) {
				returnValue = saved.get(index);
			} else {
			    // The list is empty (since it is not persisted in the manyMap object)
			    throw new IndexOutOfBoundsException();
			}
		}
		return returnValue;
	}

	@Override
	public Object set(InternalEObject eObject, EStructuralFeature feature, int index, Object value) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		return index == NO_INDEX ? singleMap.put(entry, value) : manyMap.get(entry).set(index, value);
	}

	@Override
	public void add(InternalEObject eObject, EStructuralFeature feature, int index, Object value) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> saved = manyMap.get(entry);
		if (saved != null) {
		    if(index == InternalEObject.EStore.NO_INDEX) {
		        /*
		         * Handle NO_INDEX index, which represent direct-append feature.
		         * The call to size should not cause an overhead because it would have been done in regular
		         * addUnique() otherwise.
		         */
		        saved.add(size(eObject, feature),value);
		    }
		    else {
		        saved.add(index, value);
		    }
		} else {
			List<Object> list = createValue();
			list.add(value);
			manyMap.put(entry, list);
		}
	}

	protected List<Object> createValue() {
		return new ArrayList<>();
	}

	@Override
	public Object remove(InternalEObject eObject, EStructuralFeature feature, int index) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> saved = manyMap.get(entry);
        if (saved != null) {
            return saved.remove(index);
        } else {
            // The list is empty (since it is not persisted in the manyMap object)
            throw new IndexOutOfBoundsException();
        }
	}

	@Override
	public Object move(InternalEObject eObject, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		Object movedObject = list.remove(sourceIndex);
		list.add(targetIndex, movedObject);
		return movedObject;
	}

	@Override
	public void clear(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		if (list != null) {
			list.clear();
		}
	}

	@Override
	public boolean isSet(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		return !feature.isMany() ? singleMap.containsKey(entry) : manyMap.containsKey(entry);
	}

	@Override
	public void unset(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		Map<EStoreKey, ?> concernedMap = !feature.isMany() ? singleMap : manyMap;
		concernedMap.remove(entry);
	}

	@Override
	public int size(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.size() : 0;
	}

	@Override
	public int indexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.indexOf(value) : -1;
	}

	@Override
	public int lastIndexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.lastIndexOf(value) : -1;
	}

	@Override
	public Object[] toArray(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.toArray() : new Object[] {};
	}

	@Override
	public <T> T[] toArray(InternalEObject eObject, EStructuralFeature feature, T[] array) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.toArray(array) : Arrays.copyOf(array, 0);
	}

	@Override
	public boolean isEmpty(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> res = manyMap.get(entry);
		return res == null || res.isEmpty();
	}

	@Override
	public boolean contains(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null && list.contains(value);
	}

	@Override
	public int hashCode(InternalEObject eObject, EStructuralFeature feature) {
		EStoreKey entry = new EStoreKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		// Return the default hashCode value if the list is empty
		return list != null ? list.hashCode() : 1;
	}

	@Override
	public InternalEObject getContainer(InternalEObject eObject) {
		return null;
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject eObject) {
		throw new IllegalStateException("This method should not be called");
	}

	@Override
	public EObject create(EClass eClass) {
		// TODO Unimplemented - In which case is needed?
		throw new UnsupportedOperationException();
	}

	private static class EStoreKey {

		private final InternalEObject eObject;
		private final EStructuralFeature eStructuralFeature;

		public EStoreKey(InternalEObject eObject, EStructuralFeature eStructuralFeature) {
			this.eObject = eObject;
			this.eStructuralFeature = eStructuralFeature;
		}

		@Override
		public int hashCode() {
			return Objects.hash(eObject, eStructuralFeature);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			EStoreKey other = (EStoreKey) obj;
			return Objects.equals(eObject, other.eObject)
					&& Objects.equals(eStructuralFeature, other.eStructuralFeature);
		}
	}
}
