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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.conqat.lib.commons.collections.Pair;

/**
 * Utility methods for working with predicates.
 */
public class PredicateUtils {

	/**
	 * Returns all input elements that are contained in the set described by the
	 * predicate.
	 * 
	 * @param <T>
	 *            Type in collection.
	 */
	public static <T> List<T> obtainContained(Collection<T> input, Predicate<? super T> predicate) {
		return input.stream().filter(predicate).collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Efficiently splits the list of input elements into two lists.<br/>
	 * 
	 * The first consists of all input elements that are contained in the set
	 * described by the predicate.<br/>
	 * The second consists of all input elements that are <b>not</b> contained in
	 * the set described by the predicate.<br />
	 * 
	 * Efficiently in this case means that only one iteration through the list is
	 * needed to split into two lists.
	 * 
	 * @param input
	 *            the list of input elements.
	 * @param predicate
	 *            the {@link Predicate} used to decide containment.
	 */
	public static <T> Pair<List<T>, List<T>> splitContainedNonContained(Collection<T> input,
			Predicate<? super T> predicate) {

		List<T> contained = new ArrayList<T>();
		List<T> nonContained = new ArrayList<T>();

		for (T t : input) {
			if (predicate.test(t)) {
				contained.add(t);
			} else {
				nonContained.add(t);
			}
		}
		return new Pair<List<T>, List<T>>(contained, nonContained);
	}

	/**
	 * Returns all input elements that are <b>not</b> contained in the set described
	 * by the predicate.
	 */
	public static <T> List<T> obtainNonContained(Collection<T> input, Predicate<? super T> predicate) {
		return obtainContained(input, predicate.negate());
	}

	/**
	 * Returns a predicate containing everything.
	 * 
	 * @deprecated use {@link #createAlwaysTruePredicate()} instead.
	 */
	@Deprecated
	public static <T> Predicate<T> createAllContainingPredicate() {
		return t -> true;
	}

	/** Returns a predicate that always returns true. */
	public static <T> Predicate<T> createAlwaysTruePredicate() {
		return t -> true;
	}
}
