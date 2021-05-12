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
package org.conqat.lib.simulink.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.conqat.lib.commons.collections.CaseInsensitiveStringSet;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.MemoryEfficientStringMap;
import org.conqat.lib.commons.collections.UnmodifiableSet;

/**
 * This class usually serves as base class for all classes that have a
 * key-value-mechanism for parameters. It supports a default parameter mechanism
 * that is often found withing the Simulink library.
 */
public class ParameterizedElement {

	/** The parameters map. */
	private final Map<String, String> parameters = new MemoryEfficientStringMap<String>();

	/** Create new element. */
	protected ParameterizedElement() {
		// nothing to do
	}

	/**
	 * Creates new element from another parameterized element. This copies all
	 * parameters.
	 */
	protected ParameterizedElement(ParameterizedElement other) {
		copyFrom(other);
	}

	/**
	 * Get parameter specified by name. This does <em>not</em> take default
	 * parameters into account. Returns null if the parameter is not set.
	 */
	public String getDeclaredParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Get parameter names. This does <em>not</em> take default parameters into
	 * account.
	 */
	public UnmodifiableSet<String> getDeclaredParameterNames() {
		return CollectionUtils.asUnmodifiable(parameters.keySet());
	}

	/**
	 * Get parameter specified by name. This takes default parameters into account.
	 */
	public String getParameter(String name) {
		String value = parameters.get(name);
		if (value != null) {
			return value;
		}
		return getDefaultParameter(name);
	}

	/**
	 * Returns the parameter specified by the name without regarding case. This is
	 * potentially expensive, as all parameter names (including default parameters)
	 * have to be inspected.
	 */
	public String getParameterIgnoreCase(String name) {
		CaseInsensitiveStringSet parameters = new CaseInsensitiveStringSet(getParameterNames());
		if (parameters.contains(name)) {
			return getParameter(parameters.get(name));
		}
		return null;
	}

	/**
	 * Get the names of all parameters. This takes default parameters into account.
	 */
	public UnmodifiableSet<String> getParameterNames() {
		if (getDefaultParameterNames().isEmpty()) {
			return CollectionUtils.asUnmodifiable(parameters.keySet());
		}
		HashSet<String> parametersNames = new HashSet<String>(getDefaultParameterNames());
		parametersNames.addAll(parameters.keySet());
		return CollectionUtils.asUnmodifiable(parametersNames);
	}

	/**
	 * Add a parameter.
	 */
	public void setParameter(String name, String value) {
		parameters.put(name.intern(), value.intern());
	}

	/**
	 * Get default parameter. This implementation always returns <code>null</code>.
	 */
	/* package */String getDefaultParameter(@SuppressWarnings("unused") String name) {
		return null;
	}

	/**
	 * Get names of default parameters. This implementation always returns an empty
	 * set.
	 */
	/* package */Set<String> getDefaultParameterNames() {
		return CollectionUtils.emptySet();
	}

	/** Copies all parameters from another element. */
	protected void copyFrom(ParameterizedElement other) {
		parameters.putAll(other.parameters);
	}
}