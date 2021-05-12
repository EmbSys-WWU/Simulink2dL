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
package org.conqat.lib.commons.predicate;

import java.util.function.Predicate;

/**
 * Predicate that inverts another predicate.
 * 
 * @param <T>
 *            the element type the predicate works on.
 */
public class InvertingPredicate<T> implements Predicate<T> {

	/** The delegate predicate. */
	private final Predicate<T> inner;

	/** Constructor. */
	public InvertingPredicate(Predicate<T> inner) {
		this.inner = inner;
	}

	/** {@inheritDoc} */
	@Override
	public boolean test(T element) {
		return !inner.test(element);
	}

	/**
	 * Factory method for creating an inverting predicate. This is used to exploit
	 * generic type inference (i.e. syntactic reasons).
	 */
	public static <T> InvertingPredicate<T> create(Predicate<T> inner) {
		return new InvertingPredicate<T>(inner);
	}
}