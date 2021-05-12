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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.enums.EnumUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.model.datahandler.ESimulinkColor;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Class for representing and parsing mask object parameters.
 */
/* package */class MaskObjectParameter {

	/** Pattern for parsing commands. */
	private static final Pattern COMMAND_PATTERN = Pattern.compile("([a-zA-Z_]+)\\s*\\((.*)\\)");

	/** The current color (null indicates default color). */
	private Color currentColor;

	/** Color of the main label (null indicates default color). */
	private Color labelColor;

	/** Main label text (null indicates no label). */
	private String labelText;

	/**
	 * Colors of the ports (null values indicate default color). Keys are "type"
	 * (inport/ouptut) followed by index.
	 */
	private final Map<String, Color> portColors = new HashMap<>();

	/** Label texts of the ports (null values indicate no label). */
	private final Map<String, String> portTexts = new HashMap<>();

	/** Constructor. */
	private MaskObjectParameter() {
		// empty
	}

	/** Parses the given string and returns "this" for convenience. */
	private MaskObjectParameter parse(String displayString) {
		String[] commands = SimulinkUtils.replaceSimulinkLineBreaks(displayString).split("[;\n\r]+");
		for (String command : commands) {
			interpret(command);
		}
		return this;
	}

	/** Interprets a single command. */
	private void interpret(String command) {
		Matcher matcher = COMMAND_PATTERN.matcher(command);
		if (!matcher.matches()) {
			return;
		}

		String[] parameters = matcher.group(2).split(",");
		switch (matcher.group(1).toLowerCase()) {
		case "color":
			setCurrentColor(parameters);
			break;
		case "disp":
		case "fprintf":
			display(parameters);
			break;
		case "port_label":
			portLabel(parameters);
			break;
		}
	}

	/** Sets the current color. */
	private void setCurrentColor(String[] parameters) {
		if (parameters.length != 1) {
			return;
		}

		String colorString = extractString(parameters[0]);
		if (colorString.startsWith("[")) {
			double[] colorArray = SimulinkUtils.getDoubleParameterArray(colorString);
			if (colorArray.length == 3) {
				currentColor = new Color((float) colorArray[0], (float) colorArray[1], (float) colorArray[2]);
			}
		} else {
			ESimulinkColor color = EnumUtils.valueOfIgnoreCase(ESimulinkColor.class, colorString);
			if (color != null) {
				currentColor = color.getColor();
			}
		}
	}

	/** Performs a display or fprintf command. */
	private void display(String[] parameters) {
		// only support string parameters, not other function calls
		if (parameters.length > 0 && parameters[0].trim().startsWith("'")) {
			labelColor = currentColor;
			labelText = extractString(parameters[0]);
		}
	}

	/** Extracts a string parameter. */
	private static String extractString(String string) {
		string = string.trim();
		string = StringUtils.stripPrefix(string, "'");
		string = StringUtils.stripSuffix(string, "'");
		return string;
	}

	/** Sets a port label. */
	private void portLabel(String[] parameters) {
		if (parameters.length != 3) {
			return;
		}

		String direction = extractString(parameters[0]);
		String key = direction + parameters[1];

		portColors.put(key, currentColor);
		portTexts.put(key, extractString(parameters[2]));
	}

	/** Returns the parsed mask object parameters of the block or null. */
	public static MaskObjectParameter extractFromBlock(SimulinkBlock block) {
		for (SimulinkObject object : block.getObjects()) {
			if (isMaskObject(object)) {
				return parseMaskObject(object);
			}
		}

		String maskDisplay = block.getParameter(SimulinkConstants.PARAM_MASK_DISPLAY);
		if (maskDisplay != null) {
			return new MaskObjectParameter().parse(maskDisplay);
		}

		return null;
	}

	/**
	 * @see #labelColor
	 */
	public Color getLabelColor() {
		if (labelColor == null) {
			return Color.BLACK;
		}
		return labelColor;
	}

	/**
	 * @see #labelText
	 */
	public String getLabelText() {
		return labelText;
	}

	/** Returns whether the given object is a mask object. */
	/* package */static boolean isMaskObject(SimulinkObject object) {
		return SimulinkConstants.VALUE_SIMULINK_MASK.equals(object.getParameter(SimulinkConstants.PARAM_$CLASS_NAME))
				|| SimulinkConstants.VALUE_SIMULINK_MASK
						.equals(object.getParameter(SimulinkConstants.PARAM_CLASS_NAME));
	}

	/** Parses a mask object. */
	private static MaskObjectParameter parseMaskObject(SimulinkObject object) {
		String displayString = object.getParameter(SimulinkConstants.PARAM_DISPLAY);
		if (displayString == null) {
			return null;
		}
		return new MaskObjectParameter().parse(displayString);
	}

	/** Returns the text label for the given port (or null). */
	public String getPortText(boolean isInport, String index) {
		return portTexts.get(makePortKey(isInport, index));
	}

	/** Returns the color for the given port (never null). */
	public Color getPortColor(boolean isInport, String index) {
		Color color = portColors.get(makePortKey(isInport, index));
		if (color == null) {
			return Color.BLACK;
		}
		return color;
	}

	/** Creates the key used in the map for a given port direction and index. */
	private static String makePortKey(boolean isInport, String index) {
		if (isInport) {
			return "input" + index;
		}
		return "output" + index;
	}
}
