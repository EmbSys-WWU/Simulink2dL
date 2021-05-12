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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages a map of lists, i.e. each key can store multiple elements.
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the value type (i.e. the values stored in the collections).
 */
public class ListMap<K, V> extends CollectionMap<K, V, List<V>> {

	private static final long serialVersionUID = 1L;

	/** Create new hashed list map. */
	public ListMap() {
		// Nothing to do
	}

	/** Copy constructor. */
	public ListMap(ListMap<K, V> other) {
		this();
		addAll(other);
	}

	/**
	 * Creates a new ListMap of the given key/values.
	 */
	public static <K, V> ListMap<K, V> of(K key, Collection<? extends V> values) {
		ListMap<K, V> listMap = new ListMap<>();
		listMap.addAll(key, values);
		return listMap;
	}

	/** {@inheritDoc} */
	@Override
	protected List<V> createNewCollection() {
		return new ArrayList<>();
	}

}