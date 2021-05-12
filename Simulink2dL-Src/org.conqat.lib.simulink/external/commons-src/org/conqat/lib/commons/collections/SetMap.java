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

import java.util.HashSet;
import java.util.Set;

/**
 * Manages a map of sets, i.e. each key can store multiple elements.
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the value type (i.e. the values stored in the collections).
 */
public class SetMap<K, V> extends CollectionMap<K, V, Set<V>> {

	private static final long serialVersionUID = 1L;

	/** Constructor. */
	public SetMap() {
		// Nothing to do
	}

	/** Copy constructor. */
	public SetMap(SetMap<K, V> other) {
		addAll(other);
	}

	/** {@inheritDoc} */
	@Override
	protected Set<V> createNewCollection() {
		return new HashSet<>();
	}
}