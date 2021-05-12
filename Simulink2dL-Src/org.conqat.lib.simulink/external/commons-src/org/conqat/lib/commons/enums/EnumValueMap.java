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
package org.conqat.lib.commons.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a map from enum constant names to its actual enum values.
 * <p>
 * This can be used instead of EnumUtils.valueOf() (or <enumclass>.valueOf) for
 * performance reasons, especially if the lookup of constant names is expected
 * to fail often. Otherwise EnumUtils.valueOf() (or <enumclass>.valueOf) have a
 * similar performance.
 * <p>
 * In case of looking up strings that do not exist as enum constant EnumUtils
 * has to catch an IllegalArgumentException which has an even bigger performance
 * impact. This implementation simply returns <code>null</code> if the element
 * is not found without this overhead.
 */
public class EnumValueMap<E extends Enum<E>> {

	/** Lookup map from element names to enum values. */
	private final Map<String, E> nameToValues = new HashMap<String, E>();

	/** Flag that determines whether the name lookup is case sensitive. */
	private final boolean caseSensisitve;

	/** Constructor for a case sensitive map. */
	public EnumValueMap(Class<E> enumType) {
		this(enumType, true);
	}

	/** Constructor. */
	public EnumValueMap(Class<E> enumType, boolean caseSensisitve) {
		this.caseSensisitve = caseSensisitve;
		for (E e : enumType.getEnumConstants()) {
			nameToValues.put(getKey(e.name()), e);
		}
	}

	/** Returns the lookup map key depending on the case-sensitivity. */
	private String getKey(String name) {
		if (caseSensisitve) {
			return name;
		}
		return name.toLowerCase();
	}

	/** Gets the enum value with the given name from an internal lookup map. */
	public E valueOf(String name) {
		return nameToValues.get(getKey(name));
	}

}
