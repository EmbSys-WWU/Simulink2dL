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
package org.conqat.lib.commons.assessment;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.conqat.lib.commons.color.ECCSMColor;

/**
 * Utility methods for dealing with ratings.
 */
public class AssessmentUtils {

	/** Number format that adds + or - as necessary. */
	public static final NumberFormat PLUS_MINUS_NUMBERFORMAT = new DecimalFormat("+#;-#");

	/** Returns the color used for visualizing a traffic light color. */
	public static Color getColor(ETrafficLightColor color) {
		switch (color) {
		case RED:
			return ECCSMColor.RED.getColor();
		case YELLOW:
			return ECCSMColor.YELLOW.getColor();
		case GREEN:
			return ECCSMColor.GREEN.getColor();
		case BASELINE:
			return ECCSMColor.LIGHT_BLUE.getColor();

		case UNKNOWN:
		default:
			return ECCSMColor.DARK_GRAY.getColor();
		}
	}

	/**
	 * Compares two {@link Assessment}s returning which is "better" than another
	 * one.
	 * 
	 * @return the value 0 if o1 is equal to o2; a value less than 0 if o1 is
	 *         worse than o2; and a value greater than 0 if o1 is better than
	 *         o2.
	 */
	public static int compareAssessments(Assessment o1, Assessment o2) {
		double red1 = o1.getColorFrequency(ETrafficLightColor.RED) / (double) o1.getSize();
		double red2 = o2.getColorFrequency(ETrafficLightColor.RED) / (double) o2.getSize();
		double yellow1 = o1.getColorFrequency(ETrafficLightColor.YELLOW) / (double) o1.getSize();
		double yellow2 = o2.getColorFrequency(ETrafficLightColor.YELLOW) / (double) o2.getSize();

		// chosen by a close look at the sorting result.
		double yFactor = 0.4;
		return Double.compare(red1 + yFactor * yellow1, red2 + yFactor * yellow2);
	}

	/**
	 * Calculates a delta string for two given assessments. Only colors RED,
	 * YELLOW and GREEN.
	 */
	public static String calculateAndFormatDelta(Assessment oldAssessment, Assessment newAssessment) {
		Assessment delta = newAssessment.deepClone();
		delta.subtract(oldAssessment);
		StringBuilder builder = new StringBuilder();
		builder.append("Red: ").append(PLUS_MINUS_NUMBERFORMAT.format(delta.getColorFrequency(ETrafficLightColor.RED)));
		builder.append(", Yellow: ")
				.append(PLUS_MINUS_NUMBERFORMAT.format(delta.getColorFrequency(ETrafficLightColor.YELLOW)));
		builder.append(", Green: ")
				.append(PLUS_MINUS_NUMBERFORMAT.format(delta.getColorFrequency(ETrafficLightColor.GREEN)));
		return builder.toString();
	}
}
