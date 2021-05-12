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
package org.conqat.lib.commons.assessment;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.conqat.lib.commons.clone.IDeepCloneable;
import org.conqat.lib.commons.string.StringUtils;

/**
 * This class stores an assessment. An assessment is a multiset of traffic light
 * colors (i.e. a mapping from traffic light colors to non-negative integers).
 */
public class Assessment implements Cloneable, IDeepCloneable, Serializable, Comparable<Assessment> {

	/** Version used for serialization. */
	private static final long serialVersionUID = 1;

	/** The "multimap". */
	private final int[] mapping = new int[ETrafficLightColor.values().length];

	/**
	 * Creates an empty assessment (i.e. one with all entries set to 0).
	 */
	public Assessment() {
		// Do nothing, but keep it to have a default constructor.
	}

	/**
	 * Create an assessment with a single color entry.
	 *
	 * @param color
	 *            the color included in this assessment.
	 */
	public Assessment(ETrafficLightColor color) {
		add(color);
	}

	/**
	 * Returns the size of the assessment, i.e. the sum of values over all
	 * colors.
	 */
	public int getSize() {
		return Arrays.stream(mapping).sum();
	}

	/**
	 * Add a single entry of this color to this assessment.
	 *
	 * @param color
	 *            the color added to this assessment.
	 */
	public final void add(ETrafficLightColor color) {
		add(color, 1);
	}

	/**
	 * Add a single entry of this color to this assessment.
	 *
	 * @param color
	 *            the color added to this assessment.
	 * @param count
	 *            how often to add this color to the assessment.
	 */
	public final void add(ETrafficLightColor color, int count) {
		if (count < 0) {
			throw new IllegalArgumentException("Count must be non-negative!");
		}
		mapping[color.ordinal()] += count;
	}

	/**
	 * Merge the provided assessment into this, i.e. increase all traffic light
	 * color counts by the values in the provided assessment.
	 *
	 * @param a
	 *            the assessment to merge in.
	 */
	public final void add(Assessment a) {
		for (int i = 0; i < mapping.length; ++i) {
			mapping[i] += a.mapping[i];
		}
	}

	/**
	 * Subtracts the provided assessment from this one, i.e. decreases all
	 * traffic light color counts by the values in the provided assessment.
	 *
	 * @param a
	 *            the assessment to merge in.
	 */
	public final void subtract(Assessment a) {
		for (int i = 0; i < mapping.length; ++i) {
			mapping[i] -= a.mapping[i];

			// Assessments are always non-negative
			if (mapping[i] < 0) {
				mapping[i] = 0;
			}
		}
	}

	/**
	 * @param color
	 *            the color whose frequency to read.
	 * @return the number of occurrences of the provided color in this
	 *         assessment.
	 */
	public int getColorFrequency(ETrafficLightColor color) {
		return mapping[color.ordinal()];
	}

	/**
	 * Returns the first color of the {@link ETrafficLightColor} enumeration for
	 * which this assessment has a positive count. The enumeration is ordered in
	 * a way, that more dominant colors are on top. For example the dominant
	 * color is red, if at least one red value is in the assessment.
	 */
	public ETrafficLightColor getDominantColor() {
		for (ETrafficLightColor color : ETrafficLightColor.values()) {
			if (mapping[color.ordinal()] > 0) {
				return color;
			}
		}

		return ETrafficLightColor.UNKNOWN;
	}

	/**
	 * @return the color that is most frequent in this assessment. If all
	 *         frequencies are 0, UNKNOWN is returned. If there are ties, the
	 *         more dominant (see {@link #getDominantColor()}) one is returned.
	 */
	public ETrafficLightColor getMostFrequentColor() {
		ETrafficLightColor result = ETrafficLightColor.UNKNOWN;
		int bestCount = 0;

		for (ETrafficLightColor color : ETrafficLightColor.values()) {
			int count = mapping[color.ordinal()];
			if (count > bestCount) {
				bestCount = count;
				result = color;
			}
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		int sum = getSize();
		if (sum == 0) {
			return StringUtils.EMPTY_STRING;
		}

		if (sum == 1) {
			return getDominantColor().toString();
		}

		StringBuilder builder = new StringBuilder("[");
		appendColor(builder, ETrafficLightColor.GREEN);
		builder.append(", ");
		appendColor(builder, ETrafficLightColor.YELLOW);
		builder.append(", ");
		appendColor(builder, ETrafficLightColor.RED);
		if (getColorFrequency(ETrafficLightColor.BASELINE) > 0) {
			builder.append(", ");
			appendColor(builder, ETrafficLightColor.BASELINE);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Append a string containing the color and its frequency to the given
	 * builder.
	 */
	private void appendColor(StringBuilder builder, ETrafficLightColor color) {
		builder.append(color.toString().substring(0, 1));
		builder.append(": ");
		builder.append(getColorFrequency(color));
	}

	/** Return a formatted string with relative values. */
	public String toFormattedColorDistributionString() {
		int sum = getSize();
		if (sum == 0) {
			return "[]";
		}

		NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
		percentFormat.setMinimumFractionDigits(1);
		percentFormat.setMaximumFractionDigits(1);

		// in the same order as the assessment bar
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		appendColorWithRelativeValue(builder, sum, percentFormat, ETrafficLightColor.RED);
		builder.append(", ");
		appendColorWithRelativeValue(builder, sum, percentFormat, ETrafficLightColor.YELLOW);
		builder.append(", ");
		appendColorWithRelativeValue(builder, sum, percentFormat, ETrafficLightColor.GREEN);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * Append a string containing the color and its frequency to the given
	 * builder.
	 */
	private void appendColorWithRelativeValue(StringBuilder builder, int size, NumberFormat percentFormat,
			ETrafficLightColor color) {
		builder.append(color.getDisplayText());
		builder.append(": ");

		double relativeValue = getColorFrequency(color) / (double) size;
		builder.append(percentFormat.format(relativeValue));
	}

	/** {@inheritDoc} */
	@Override
	protected Assessment clone() {
		return deepClone();
	}

	/** {@inheritDoc} */
	@Override
	public Assessment deepClone() {
		Assessment a = new Assessment();
		a.add(this);
		return a;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Assessment)) {
			return false;
		}

		Assessment a = (Assessment) obj;
		return Arrays.equals(mapping, a.mapping);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < mapping.length; ++i) {
			/*
			 * primes taken from http://planetmath.org/goodhashtableprimes
			 */
			hash *= 97;
			hash += mapping[i];
			hash %= 50331653;
		}
		return hash;
	}

	/**
	 * Compares assessment values lexicographically, i.e. from most dominant
	 * color to least dominant color.
	 */
	@Override
	public int compareTo(Assessment other) {
		for (int i = 0; i < mapping.length; i++) {
			if (mapping[i] != other.mapping[i]) {
				return mapping[i] - other.mapping[i];
			}
		}
		return 0;
	}

	/**
	 * Compares both assessments by the percentage of their dominant colors. If
	 * equal, returns zero.
	 */
	public int compareToRelative(Assessment other) {
		int thisSum = getSize();
		int otherSum = other.getSize();

		// Prohibit division by zero
		if (thisSum == 0 || otherSum == 0) {
			return thisSum - otherSum;
		}

		for (int i = 0; i < mapping.length; i++) {
			int compareResult = Double.compare((double) mapping[i] / thisSum, (double) other.mapping[i] / otherSum);
			if (compareResult != 0) {
				return compareResult;
			}
		}
		return 0;
	}

	/** Aggregate assessments based on sum of assessment values. */
	public static Assessment aggregate(Collection<Assessment> values) {
		Assessment result = new Assessment();
		for (Assessment a : values) {
			result.add(a);
		}
		return result;
	}
}
