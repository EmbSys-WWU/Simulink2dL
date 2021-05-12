/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 the ConQAT Project                                   |
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map implementation that supports case-insensitive String keys. Note that
 * while the method signatures of the {@link Map} interface allows arbitrary
 * Objects as keys, this class will throw {@link IllegalArgumentException}s when
 * using non String keys (null keys are supported). Also note that {link
 * {@link #keySet()}} will return lower cased keys.
 */
public class CaseInsensitiveMap<T> implements Map<String, T> {

	/** The delegate map */
	private final Map<String, T> delegateMap = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public int size() {
		return delegateMap.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return delegateMap.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(Object key) {
		return delegateMap.containsKey(preprocessKey(key));
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsValue(Object value) {
		return delegateMap.containsValue(value);
	}

	/** {@inheritDoc} */
	@Override
	public T get(Object key) {
		return delegateMap.get(preprocessKey(key));
	}

	/** {@inheritDoc} */
	@Override
	public T put(String key, T value) {
		return delegateMap.put(preprocessKey(key), value);
	}

	/** {@inheritDoc} */
	@Override
	public T remove(Object key) {
		return delegateMap.remove(preprocessKey(key));
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(Map<? extends String, ? extends T> m) {
		for (java.util.Map.Entry<? extends String, ? extends T> entry : m.entrySet()) {
			delegateMap.put(preprocessKey(entry.getKey()), entry.getValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		delegateMap.clear();
	}

	/**
	 * Returns the keys of this map in <b>lower case</b>. Note that this (mutable)
	 * set does no longer possess the case-insensitivity feature.
	 */
	@Override
	public Set<String> keySet() {
		return delegateMap.keySet();
	}

	/** {@inheritDoc} */
	@Override
	public Collection<T> values() {
		return delegateMap.values();
	}

	/**
	 * Returns the entries of this map. The keys of the entries will we in lower
	 * case.
	 */
	/** {@inheritDoc} */
	@Override
	public Set<java.util.Map.Entry<String, T>> entrySet() {
		return delegateMap.entrySet();
	}

	/**
	 * Preprocesses a key by converting it to lower case. This can handle
	 * <code>null</code> keys.
	 */
	private static String preprocessKey(Object key) {
		if (key == null) {
			return null;
		}
		if (key instanceof String) {
			return ((String) key).toLowerCase();
		}
		throw new IllegalArgumentException("Only keys of type String allowed");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return delegateMap.toString();
	}

}
