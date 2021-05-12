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
package org.conqat.lib.simulink.model.datahandler;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;

/**
 * Base class for layout handlers.
 */
public abstract class LayoutHandlerBase {

	/** Default font size in the Simulink IDE. */
	protected static final int DEFAULT_FONT_SIZE = 10;

	/** The font render context used for determining text width. */
	public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, false, false);

	/** Logger used for reporting any problems during model data extraction. */
	protected final ILogger logger;

	/** Constructor. */
	protected LayoutHandlerBase(ModelBuildingParameters parameters) {
		logger = parameters.getLogger();
	}

	/** Extracts the font information to be used. */
	public static FontData extractFontData(ParameterizedElement element, ILogger logger) {
		String fontName = element.getParameter(SimulinkConstants.PARAM_FONT_NAME);
		if (fontName == null) {
			logger.error("Missing font name for element! Using default.");
			fontName = Font.SANS_SERIF;
		}

		int fontSize = DEFAULT_FONT_SIZE;
		String fontSizeValue = element.getParameter(SimulinkConstants.PARAM_FONT_SIZE);
		if (fontSizeValue != null) {
			try {
				fontSize = Integer.parseInt(fontSizeValue);
			} catch (NumberFormatException e) {
				logger.error("Invalid font size value: " + fontSizeValue);
			}
		}

		boolean bold = SimulinkConstants.VALUE_BOLD.equals(element.getParameter(SimulinkConstants.PARAM_FONT_WEIGHT));
		boolean italic = SimulinkConstants.VALUE_ITALIC
				.equals(element.getParameter(SimulinkConstants.PARAM_FONT_ANGLE));

		return new FontData(fontName, fontSize, bold, italic);
	}

	/**
	 * Returns the bounds of a text for a given font. As height indicator the
	 * height of the actual rendered line is used.
	 */
	public static Rectangle determineTextBounds(String text, Font font) {
		double maxWidth = 0;
		double sumHeight = 0;
		LineMetrics lineMetrics = font.getLineMetrics(text, FONT_RENDER_CONTEXT);
		for (String line : StringUtils.splitLinesAsList(text)) {
			Rectangle2D bounds = getTextBounds(line, font);
			maxWidth = Math.max(maxWidth, bounds.getWidth());
			sumHeight += lineMetrics.getHeight();
		}
		return new Rectangle((int) maxWidth, (int) sumHeight);
	}

	/** Returns text bounds for the given text. */
	public static Rectangle2D getTextBounds(String line, Font font) {
		if (StringUtils.isEmpty(line)) {
			return new Rectangle();
		}
		return new TextLayout(line, font, FONT_RENDER_CONTEXT).getBounds();
	}
}
