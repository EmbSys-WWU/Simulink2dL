/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2017 The ConQAT Project                               |
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
|                                                                          |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.string;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * Compares two strings by recursively comparing their prefixes. If the prefixes
 * are both numbers they are compared by their value.
 */
public class NumbersAwareStringComparator implements Comparator<String> {

	/** Singleton instance. */
	public static final NumbersAwareStringComparator INSTANCE = new NumbersAwareStringComparator();

	/** {@inheritDoc} */
	@Override
	public int compare(String s1, String s2) {
		if (StringUtils.isEmpty(s1) || StringUtils.isEmpty(s2)) {
			return StringUtils.compare(s1, s2);
		}
		String prefix1 = getPrefix(s1);
		String prefix2 = getPrefix(s2);
		if (prefix1.equals(prefix2)) {
			return compare(StringUtils.stripPrefix(s1, prefix1), StringUtils.stripPrefix(s2, prefix2));
		}
		if (startsWithDigit(prefix1) && startsWithDigit(prefix2)) {
			int comparisonResult = new BigInteger(prefix1).compareTo(new BigInteger(prefix2));
			if (comparisonResult == 0) {
				return compare(StringUtils.stripPrefix(s1, prefix1), StringUtils.stripPrefix(s2, prefix2));
			}
			return comparisonResult;
		}
		return prefix1.compareTo(prefix2);
	}

	/** Tests if the first character is digit. */
	private static boolean startsWithDigit(String s) {
		char first = s.charAt(0);
		return Character.isDigit(first);
	}

	/**
	 * Returns the prefix, which can be leading digit characters or leading
	 * non-digit characters.
	 */
	private static String getPrefix(String s) {
		if (startsWithDigit(s)) {
			return s.replaceAll("[\\D]+.*", "");
		}
		return s.replaceAll("[\\d]+.*", "");
	}

}
