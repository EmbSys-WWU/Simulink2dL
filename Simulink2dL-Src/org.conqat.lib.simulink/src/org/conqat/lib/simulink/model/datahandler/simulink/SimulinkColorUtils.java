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
package org.conqat.lib.simulink.model.datahandler.simulink;

import java.awt.Color;

import org.conqat.lib.commons.enums.EnumUtils;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.datahandler.ESimulinkColor;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utility methods for dealing with Simulink colors.
 */
/* package */ class SimulinkColorUtils {

	/**
	 * Extracts a color from an element's parameters.
	 * 
	 * @param parameter
	 *            the name of the parameter.
	 * @param defaultColor
	 *            the color to use if no color information was found.
	 */
	public static Color extractColor(SimulinkElementBase element, String parameter, Color defaultColor,
			ILogger logger) {
		String colorString = element.getParameter(parameter);
		if (colorString == null) {
			return defaultColor;
		}

		if (colorString.startsWith("[")) {
			return parseArrayColor(element, colorString, defaultColor, logger);
		}

		return parsePredefinedColor(element, colorString, defaultColor, logger);
	}

	/**
	 * Parses and returns a color from a (RGB) color array.
	 * 
	 * @param element
	 *            the element (used for error reporting).
	 * @param colorString
	 *            the color string to parse (Matlab array representation).
	 * @param defaultColor
	 *            the default color to return in case of errors.
	 */
	private static Color parseArrayColor(SimulinkElementBase element, String colorString, Color defaultColor,
			ILogger logger) {
		try {
			double[] colorArray = SimulinkUtils.getDoubleParameterArray(colorString);
			if (colorArray.length != 3) {
				logger.error("Unsupported color array found in element " + element.getId() + " (length = "
						+ colorArray.length + " instead of 3). Using default color.");
				return defaultColor;
			}
			return new Color((float) colorArray[0], (float) colorArray[1], (float) colorArray[2]);
		} catch (NumberFormatException e) {
			logger.error("Color array in element " + element.getId() + " contained invalid number: " + colorString
					+ ". Using default color.");
			return defaultColor;
		}
	}

	/**
	 * Parses and returns a predefined color (i.e. explicit color string).
	 * 
	 * @param element
	 *            the element (used for error reporting).
	 * @param colorString
	 *            the color string to parse.
	 * @param defaultColor
	 *            the default color to return in case of errors.
	 */
	private static Color parsePredefinedColor(SimulinkElementBase element, String colorString, Color defaultColor,
			ILogger logger) {
		ESimulinkColor simulinkColor = EnumUtils.valueOfIgnoreCase(ESimulinkColor.class, colorString);
		if (simulinkColor == null) {
			logger.error("Unsupported color string found in element " + element.getId() + " (" + colorString
					+ "). Using default color.");
			return defaultColor;
		}
		return simulinkColor.getColor();
	}
}
