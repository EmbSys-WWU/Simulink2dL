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
package org.conqat.lib.commons.lang;

import java.util.Comparator;
import java.util.function.IntSupplier;

/**
 * Utility methods that help working with Java objects.
 */
public class ObjectUtils {

	/**
	 * Compares the two given objects. First checks if they are reference-equal.
	 * If not, checks each for <code>null</code>. If both are non-null, calls
	 * <code>object1.compareTo(object2)</code>.
	 * 
	 * <code>null</code> values will be ordered before non-null values.
	 */
	public static <T extends Comparable<T>> int compareNullSafe(T object1, T object2) {
		return compareNullSafe(object1, object2, T::compareTo);
	}

	/**
	 * Compares the two given objects. First checks if they are reference-equal.
	 * If not, checks each for <code>null</code>. If both are non-null, passes
	 * them to the given comparator.
	 * 
	 * <code>null</code> values will be ordered before non-null values.
	 */
	public static <T> int compareNullSafe(T object1, T object2, Comparator<T> comparator) {
		if (object1 == object2) {
			return 0;
		}
		if (object1 == null) {
			return -1;
		}
		if (object2 == null) {
			return 1;
		}
		return comparator.compare(object1, object2);
	}

	/**
	 * Evaluates the given comparisons in order. If all of them evaluate to 0,
	 * returns 0, otherwise returns the first non-zero result.
	 */
	public static int compareInOrder(IntSupplier... comparisonSuppliers) {
		for (IntSupplier comparisonSupplier : comparisonSuppliers) {
			int comparisonResult = comparisonSupplier.getAsInt();
			if (comparisonResult != 0) {
				return comparisonResult;
			}
		}
		return 0;
	}

}
