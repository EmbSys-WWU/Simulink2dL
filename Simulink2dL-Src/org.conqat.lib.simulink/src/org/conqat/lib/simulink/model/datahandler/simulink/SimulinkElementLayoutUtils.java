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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.EHorizontalAlignment;
import org.conqat.lib.simulink.model.datahandler.EOrientation;
import org.conqat.lib.simulink.model.datahandler.FontData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utility methods for element layout, i.e. blocks, annotations, etc.
 */
/* package */ class SimulinkElementLayoutUtils {

	/**
	 * Default position (which in Simulink terms also contains dimensions) used
	 * for blocks in case of errors while parsing block position.
	 */
	private static final Rectangle BLOCK_DEFAULT_POSITION = new Rectangle(10, 10, 30, 30);

	/** The pattern used for matching placeholders in the format string. */
	private static final Pattern FORMAT_STRING_PLACEHOLDER_PATTERN = Pattern.compile("%<(.*?)>");

	/** Pattern describing the parts of a mark variables string. */
	private static final Pattern MASK_VARIABLE_PART_PATTERN = Pattern.compile("(.*)=[@&](\\d+)");

	/**
	 * Extracts an elements's position (which in Simulink terms also contains
	 * the size.
	 */
	public static Rectangle extractPosition(SimulinkElementBase element, ILogger logger) {
		// model itself has no position
		if (element instanceof SimulinkModel) {
			return BLOCK_DEFAULT_POSITION;
		}

		String positionString = element.getParameter(SimulinkConstants.PARAM_POSITION);
		if (positionString == null) {
			logger.error("No position contained in model for element " + element.getId() + ". Using default position.");
			return BLOCK_DEFAULT_POSITION;
		}
		int[] positionArray = SimulinkUtils.getIntParameterArray(positionString);
		if (positionArray.length == 2) {
			// can happen for annotations; determine size based on text
			positionArray = Arrays.copyOf(positionArray, 4);
			fixAnnotationPositionArray(element, positionArray, true, logger);
		} else if (positionArray.length == 4) {
			// annotations have a slightly different layout interpretation
			if (element instanceof SimulinkAnnotation
					&& element.getParameter(SimulinkConstants.PARAM_INTERPRETER) == null) {
				fixAnnotationPositionArray(element, positionArray, false, logger);
			}
		} else {
			logger.error("Unsupported position array found in model (length = " + positionArray.length
					+ " instead of 4) for element " + element.getId() + ". Using default position.");
			return BLOCK_DEFAULT_POSITION;
		}
		return new Rectangle(positionArray[0], positionArray[1], positionArray[2] - positionArray[0],
				positionArray[3] - positionArray[1]);
	}

	/**
	 * Fixes the position array for an annotation with a two dimensional
	 * coordinate.
	 */
	private static void fixAnnotationPositionArray(SimulinkElementBase element, int[] positionArray,
			boolean boundsFromText, ILogger logger) {

		Rectangle textBounds = new Rectangle(positionArray[2] - positionArray[0] - SimulinkLayoutHandler.LABEL_DISTANCE,
				positionArray[3] - positionArray[1] - SimulinkLayoutHandler.LABEL_DISTANCE);
		if (boundsFromText) {
			String text = SimulinkUtils.replaceSimulinkLineBreaks(element.getName());
			FontData font = LayoutHandlerBase.extractFontData(element, logger);
			textBounds = LayoutHandlerBase.determineTextBounds(text, font.getAwtFont());
		}

		EHorizontalAlignment horizontalAlignment = EHorizontalAlignment
				.parse(element.getParameter(SimulinkConstants.PARAM_HORIZONTAL_ALIGNMENT));

		switch (horizontalAlignment) {
		case LEFT:
			break;
		case RIGHT:
			positionArray[0] -= textBounds.width;
			break;
		case CENTER:
		default:
			positionArray[0] -= (textBounds.width + SimulinkLayoutHandler.LABEL_DISTANCE) / 2;
		}

		String verticalAlignment = element.getParameter(SimulinkConstants.PARAM_VERTICAL_ALIGNMENT);
		if (verticalAlignment == null) {
			verticalAlignment = "middle";
		}

		switch (verticalAlignment) {
		case "top":
			break;
		case "bottom":
			positionArray[1] -= textBounds.height;
			break;
		case "middle":
		default:
			positionArray[1] -= (textBounds.height + SimulinkLayoutHandler.LABEL_DISTANCE) / 2;
		}

		positionArray[2] = positionArray[0] + textBounds.width + SimulinkLayoutHandler.LABEL_DISTANCE;
		positionArray[3] = positionArray[1] + textBounds.height + SimulinkLayoutHandler.LABEL_DISTANCE;
	}

	/** Returns the label data used for rendering a block's label. */
	public static LabelLayoutData extractBlockLabelData(SimulinkBlock block, BlockLayoutData blockLayoutData,
			ILogger logger) {
		String text = SimulinkUtils.replaceSimulinkLineBreaks(block.getName());

		FontData font = LayoutHandlerBase.extractFontData(block, logger);
		boolean alternatePlacement = SimulinkConstants.VALUE_ALTERNATE
				.equals(block.getParameter(SimulinkConstants.PARAM_NAME_PLACEMENT));
		Point position = calculteBlockLabelPosition(blockLayoutData,
				LayoutHandlerBase.determineTextBounds(text, font.getAwtFont()), alternatePlacement);

		boolean visible = !SimulinkConstants.VALUE_OFF.equals(block.getParameter(SimulinkConstants.PARAM_SHOW_NAME));
		return new LabelLayoutData(text, visible, font, position, blockLayoutData.getForegroundColor(),
				blockLayoutData.getOpacity());
	}

	/** Calculates the position of the block's label. */
	private static Point calculteBlockLabelPosition(BlockLayoutData blockLayoutData, Rectangle textBounds,
			boolean alternatePlacement) {
		double yPosition;
		double xPosition;
		if (blockLayoutData.getOrientation() == EOrientation.LEFT
				|| blockLayoutData.getOrientation() == EOrientation.RIGHT) {
			xPosition = blockLayoutData.getPosition().getCenterX() - textBounds.width / 2.;
			if (alternatePlacement) {
				yPosition = blockLayoutData.getPosition().getMinY() - textBounds.height
						- SimulinkLayoutHandler.LABEL_DISTANCE;
			} else {
				yPosition = blockLayoutData.getPosition().getMaxY() + SimulinkLayoutHandler.LABEL_DISTANCE;
			}
		} else {
			yPosition = blockLayoutData.getPosition().getCenterY() - textBounds.height / 2.;
			if (alternatePlacement) {
				xPosition = blockLayoutData.getPosition().getMinX() - textBounds.width
						- SimulinkLayoutHandler.LABEL_DISTANCE;
			} else {
				xPosition = blockLayoutData.getPosition().getMaxX() + SimulinkLayoutHandler.LABEL_DISTANCE;
			}
		}
		return new Point((int) xPosition, (int) yPosition);
	}

	/** Returns the label used inside of a block (or null). */
	public static LabelLayoutData extractBlockInnerLabelData(SimulinkBlock block, BlockLayoutData blockLayoutData,
			ILogger logger) {
		if (block.getParameter(SimulinkConstants.PARAM_MASK_DISPLAY_STRING) != null) {
			return obtainBlockInnerLabelDataFromDisplayString(block, blockLayoutData, logger);
		}

		MaskObjectParameter maskObjectParameter = MaskObjectParameter.extractFromBlock(block);
		if (maskObjectParameter == null || StringUtils.isEmpty(maskObjectParameter.getLabelText())) {
			return null;
		}
		String text = maskObjectParameter.getLabelText();
		FontData font = LayoutHandlerBase.extractFontData(block, logger);
		Rectangle bounds = LayoutHandlerBase.determineTextBounds(text, font.getAwtFont());

		Rectangle blockPosition = blockLayoutData.getPosition();
		Point position = new Point(blockPosition.x + (blockPosition.width - bounds.width) / 2,
				blockPosition.y + (blockPosition.height - bounds.height) / 2);

		return new LabelLayoutData(text, true, font, position, maskObjectParameter.getLabelColor(),
				blockLayoutData.getOpacity());
	}

	/**
	 * Obtain the LabelLayoutData for blocks having a DisplayStringWithTags
	 * parameter
	 */
	private static LabelLayoutData obtainBlockInnerLabelDataFromDisplayString(SimulinkBlock block,
			BlockLayoutData blockLayoutData, ILogger logger) {

		String text = block.getParameter(SimulinkConstants.PARAM_MASK_DISPLAY_STRING);

		// mdl file
		text = text.replace("\\\\n", "\n");
		// slx file, does not match in mdl files anymore
		text = text.replace("\\n", "\n");

		FontData font = LayoutHandlerBase.extractFontData(block, logger);
		Rectangle bounds = LayoutHandlerBase.determineTextBounds(text, font.getAwtFont());

		Rectangle blockPosition = blockLayoutData.getPosition();
		Point position = new Point(blockPosition.x + (blockPosition.width - bounds.width) / 2,
				blockPosition.y + (blockPosition.height - bounds.height) / 2);

		return new LabelLayoutData(text, true, font, position, Color.BLACK, blockLayoutData.getOpacity());
	}

	/**
	 * Returns the label data used for rendering a block's sub label (may return
	 * null).
	 */
	public static LabelLayoutData extractBlockSubLabelData(SimulinkBlock block, String formatString,
			BlockLayoutData blockLayoutData, ILogger logger) {
		LabelLayoutData mainLabel = extractBlockLabelData(block, blockLayoutData, logger);

		Map<String, String> placeholderSubstitutes = new HashMap<>();
		String text = SimulinkUtils
				.replaceSimulinkLineBreaks(resolveFormatPlaceholder(formatString, block, placeholderSubstitutes));
		Rectangle subLabelBounds = LayoutHandlerBase.determineTextBounds(text, mainLabel.getFont().getAwtFont());

		if (!mainLabel.isVisible()) {
			boolean alternatePlacement = SimulinkConstants.VALUE_ALTERNATE
					.equals(block.getParameter(SimulinkConstants.PARAM_NAME_PLACEMENT));
			Point position = calculteBlockLabelPosition(blockLayoutData, subLabelBounds, alternatePlacement);
			return new LabelLayoutData(text, true, mainLabel.getFont(), position, Color.GRAY,
					blockLayoutData.getOpacity(), placeholderSubstitutes);
		}

		Rectangle mainLabelBounds = LayoutHandlerBase.determineTextBounds(mainLabel.getText(),
				mainLabel.getFont().getAwtFont());
		mainLabelBounds.setLocation(mainLabel.getPosition());
		int x = (int) (mainLabelBounds.getCenterX() - subLabelBounds.getWidth() / 2);

		int y = (int) (mainLabelBounds.getMaxY() + SimulinkLayoutHandler.LABEL_DISTANCE);
		if (mainLabel.getPosition().y < blockLayoutData.getPosition().y) {
			y = (int) (mainLabelBounds.getMinY() - SimulinkLayoutHandler.LABEL_DISTANCE - subLabelBounds.getHeight());
		}

		if (placeholderSubstitutes.isEmpty()) {
			placeholderSubstitutes = null;
		}
		return new LabelLayoutData(text, true, mainLabel.getFont(), new Point(x, y), Color.GRAY,
				blockLayoutData.getOpacity(), placeholderSubstitutes);
	}

	/** Resolves any format strings contained in the format string. */
	private static String resolveFormatPlaceholder(String formatString, SimulinkBlock block,
			Map<String, String> placeholderSubstitutes) {
		Matcher matcher = FORMAT_STRING_PLACEHOLDER_PATTERN.matcher(formatString);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String replacement = findFormatPlaceholderReplacement(placeholder, block);
			placeholderSubstitutes.put(placeholder, replacement);
			matcher.appendReplacement(buffer, replacement);
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}

	/**
	 * Returns the value to replace the given placeholder with. Fallback is to
	 * just print the placeholder itself.
	 */
	private static String findFormatPlaceholderReplacement(String placeholderName, SimulinkBlock block) {
		// find first object with given name using DFS
		Stack<SimulinkObject> stack = new Stack<>();
		stack.addAll(block.getObjects());
		while (!stack.isEmpty()) {
			SimulinkObject object = stack.pop();
			if (placeholderName.equals(object.getName())
					&& object.getParameter(SimulinkConstants.PARAM_VALUE) != null) {
				return object.getParameter(SimulinkConstants.PARAM_VALUE);
			}
			stack.addAll(object.getObjects());
		}

		// fallback 1: MaskVariables and MaskValueString
		String value = getMaskVariableToValueMap(block).get(placeholderName);
		if (value != null) {
			return value;
		}

		// fallback 2: try block property
		if (block.getParameterIgnoreCase(placeholderName) != null) {
			return block.getParameterIgnoreCase(placeholderName);
		}

		// fallback 3: some names have been renamed in SLX
		if ("X0".equals(placeholderName)) {
			return findFormatPlaceholderReplacement("InitialCondition", block);
		}

		return "%<" + placeholderName + ">";
	}

	/** Returns a map from mask variable names to values. */
	private static Map<String, String> getMaskVariableToValueMap(SimulinkBlock block) {

		String valueString = block.getParameter(SimulinkConstants.PARAM_MASK_VALUE_STRING);
		String maskVariables = block.getParameter(SimulinkConstants.PARAM_MASK_VARIABLES);
		if (valueString == null || maskVariables == null) {
			return Collections.emptyMap();
		}

		String[] values = valueString.split("[|]");
		Map<String, String> result = new HashMap<>();
		for (String part : maskVariables.split(";")) {
			Matcher matcher = MASK_VARIABLE_PART_PATTERN.matcher(part);
			if (matcher.matches()) {
				String name = matcher.group(1);
				int index = Integer.parseInt(matcher.group(2)) - 1;
				if (index >= 0 && index < values.length) {
					result.put(name, values[index]);
				}
			}
		}
		return result;
	}
}
