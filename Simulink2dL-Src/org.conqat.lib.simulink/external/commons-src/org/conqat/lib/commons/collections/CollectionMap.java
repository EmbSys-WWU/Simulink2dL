/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A collection map deals with a map of collections, i.e. each key can store
 * multiple elements. Depending on the collection implementation chosen, this
 * can also be interpreted as a multi map.
 * <p>
 * If you deal with the basic case of {@link Map}s and {@link List}s, use the
 * {@link ListMap}, which is much easier to apply.
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the value type (i.e. the values stored in the collections).
 * @param <C>
 *            the collection type, which is made explicit at the interface.
 */
public abstract class CollectionMap<K, V, C extends Collection<V>> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The underlying map. */
	private final Map<K, C> map;

	/** Constructor. */
	public CollectionMap() {
		map = createUnderlyingMap();
	}

	/**
	 * Create a new instance of the underlying map, called exactly once during the
	 * constructor. Can be used to prepopulate the list with values if necessary.
	 */
	protected Map<K, C> createUnderlyingMap() {
		return new HashMap<K, C>();
	}

	/**
	 * Returns a new instance of the underlying collection to be used as value for
	 * the underlaying map.
	 */
	protected abstract C createNewCollection();

	/**
	 * Returns the collection stored under the given key (or null). Modifying the
	 * collection will directly affect this object.
	 */
	public C getCollection(K key) {
		return map.get(key);
	}

	/**
	 * Returns the collection stored under the given key (or the given default if no
	 * collection is stored). Modifying the collection will directly affect this
	 * object.
	 */
	public C getCollectionOrElse(K key, C defaultCollection) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return defaultCollection;
	}

	/**
	 * Returns the collection stored under the given key (or an empty collection).
	 * If there was a collection stored under the key, modifying the collection will
	 * directly affect this object.
	 */
	public C getCollectionOrEmpty(K key) {
		C result = getCollection(key);
		if (result == null) {
			return createNewCollection();
		}
		return result;
	}

	/**
	 * Adds a value to the collection associated with the given key.
	 * 
	 * @return <code>true</code> if the collection associated with the given key
	 *         changed as a result of the call.
	 */
	public boolean add(K key, V value) {
		return getOrCreateCollection(key).add(value);
	}

	/**
	 * Adds all values to the collection associated with the given key.
	 * 
	 * @return <code>true</code> if the collection associated with the given key
	 *         changed as a result of the call.
	 */
	public boolean addAll(K key, Collection<? extends V> values) {
		return getOrCreateCollection(key).addAll(values);
	}

	/**
	 * Returns the collection stored under the given key (or creates a new one).
	 */
	/* package */C getOrCreateCollection(K key) {
		C collection = map.get(key);
		if (collection == null) {
			collection = createNewCollection();
			map.put(key, collection);
		}
		return collection;
	}

	/** Adds all elements from another collection map. */
	public void addAll(CollectionMap<K, V, C> other) {
		for (K key : other.getKeys()) {
			C collection = other.getCollection(key);
			if (collection != null) {
				addAll(key, collection);
			}
		}
	}

	/** Returns whether an element is contained. */
	public boolean contains(K key, V value) {
		C collection = map.get(key);
		if (collection == null) {
			return false;
		}
		return collection.contains(value);
	}

	/**
	 * Removes an element.
	 * 
	 * @return true if an element was removed as a result of this call.
	 */
	public boolean remove(K key, V value) {
		C collection = map.get(key);
		if (collection == null) {
			return false;
		}
		return collection.remove(value);
	}

	/**
	 * Check if a (possibly empty) collection is present for a given key.
	 */
	public boolean containsCollection(K key) {
		return map.containsKey(key);
	}

	/**
	 * Removes the collection stored for a key.
	 * 
	 * @return true if a collection was removed as a result of this call.
	 */
	public boolean removeCollection(K key) {
		return map.remove(key) != null;
	}

	/** Get the keys. */
	public UnmodifiableSet<K> getKeys() {
		return CollectionUtils.asUnmodifiable(map.keySet());
	}

	/** Return all values from all collections. */
	public C getValues() {
		C result = createNewCollection();
		for (C values : map.values()) {
			result.addAll(values);
		}
		return result;
	}

	/**
	 * Returns the size of this map, i.e. number of key-value mappings in this map.
	 */
	public int size() {
		return map.size();
	}

	/** Return the total count of values over all collections. */
	public int getValueCount() {
		int result = 0;
		for (C values : map.values()) {
			result += values.size();
		}
		return result;
	}

	/** Clears the underlying map and thus all contents. */
	public void clear() {
		map.clear();
	}

	/**
	 * Converts the {@link CollectionMap} to a map with arrays
	 * 
	 * @param type
	 *            Type of the target array
	 */
	public Map<K, V[]> collectionsToArrays(Class<V> type) {
		Map<K, V[]> map = new HashMap<K, V[]>();
		for (K key : getKeys()) {
			map.put(key, CollectionUtils.toArray(getCollection(key), type));
		}
		return map;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	/**
	 * Helper method to create a new CollectionMap with a given type of Collection
	 * initializer. Usage e.g.:
	 * <code>CollectionMap.createWithCollectionInitializer(() -> new LinkedList<RatingPartition>());</code>
	 */
	public static <K, V, C extends Collection<V>> CollectionMap<K, V, C> createWithCollectionSupplier(
			Supplier<C> collectionInitializer) {
		return new CollectionMap<K, V, C>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected C createNewCollection() {
				return collectionInitializer.get();
			}
		};
	}

}
