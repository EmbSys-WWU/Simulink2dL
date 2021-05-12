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

/**
 * Enum for traffic light colors.
 * 
 * Note that the order is relevant: The first Color (RED) is considered the most
 * dominant color (see {@link Assessment#getDominantColor()}).
 * 
 * The mapping to actual colors is defined in
 * {@link AssessmentUtils#getColor(ETrafficLightColor)}, so typically this
 * method should be adjusted when a new constant is introduced here.
 */
public enum ETrafficLightColor {

	/** Red signals errors or incompleteness. */
	RED("#FF6633"),

	/**
	 * Orange is an intermediate state between {@link #RED} and {@link #YELLOW}.
	 */
	ORANGE("#FFA500"),

	/** Yellow signals warning or lack of control. */
	YELLOW("#FFFF22"),

	/** Green signals the absence of errors or correctness. */
	GREEN("#66CC66"),

	/**
	 * Baseline indicates a baseline entry, i.e. there has been no change compared
	 * to a given baseline and thus no color applies.
	 */
	BASELINE,

	/** This is used if no information is available. */
	UNKNOWN;

	/** Display text of the color (name with normal casing) */
	private final String displayText;

	/** Short, one-character display text of the color (e.g., G, Y, R) */
	private final String shortDisplayText;

	/** Hex representation of the color */
	private final String hexValue;

	/**
	 * This is hex value is used if no color is associated with the
	 * ETrafficLightColor.
	 */
	private static final String HEX_VALUE_BLACK = "#000000";

	/**
	 * Constructor for ETrafficLightColors that do not have an associated color.
	 */
	private ETrafficLightColor() {
		this(HEX_VALUE_BLACK);
	}

	/** Constructor for ETrafficLightColors that have an associated color. */
	private ETrafficLightColor(String hexValue) {
		this.displayText = this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
		this.shortDisplayText = this.name().substring(0, 1);
		this.hexValue = hexValue;
	}

	/** @see #displayText */
	public String getDisplayText() {
		return displayText;
	}

	/** @see #shortDisplayText */
	public String getShortDisplayText() {
		return shortDisplayText;
	}

	/** @see #hexValue */
	public String getHexValue() {
		return hexValue;
	}

	/**
	 * Returns the more dominant color, which is the enum literal with smaller index
	 * (as they are sorted by dominance).
	 */
	public static ETrafficLightColor getDominantColor(ETrafficLightColor color1, ETrafficLightColor color2) {
		if (color2.ordinal() < color1.ordinal()) {
			return color2;
		}
		return color1;
	}
}