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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Predicate that combines other predicates with boolean "or". This supports
 * lazy evaluation. This is immutable and stateless if the inner predicates are
 * immutable and stateless.
 * 
 * @param <T>
 *            the element type the predicate works on.
 */
public class OrPredicate<T> implements Predicate<T> {

	/** The delegate predicates. */
	private final List<Predicate<T>> innerPredicates;

	/** Constructor. */
	@SafeVarargs
	public OrPredicate(Predicate<T>... innerPredicates) {
		this.innerPredicates = Arrays.asList(innerPredicates);
	}

	/** {@inheritDoc} */
	@Override
	public boolean test(T element) {
		for (Predicate<T> inner : innerPredicates) {
			if (inner.test(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Factory method for creating an or-predicate. This is used to exploit generic
	 * type inference (i.e. syntactic reasons).
	 */
	@SafeVarargs
	public static <T> OrPredicate<T> create(Predicate<T>... innerPredicates) {
		return new OrPredicate<T>(innerPredicates);
	}
}