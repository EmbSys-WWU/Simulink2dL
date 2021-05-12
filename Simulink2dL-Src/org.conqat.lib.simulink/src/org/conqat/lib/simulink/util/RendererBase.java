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
package org.conqat.lib.simulink.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.datahandler.EHorizontalAlignment;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;

/**
 * Base class for renderers.
 */
public class RendererBase {

	/** The padding applied to the output canvas. */
	protected static final int OUTPUT_CANVAS_PADDING = 10;

	/** Prepares the graphics for given image. */
	protected static Graphics2D createGraphics(Rectangle canvasRectangle, BufferedImage image) {
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, canvasRectangle.width, canvasRectangle.height);

		graphics.translate(-canvasRectangle.x, -canvasRectangle.y);
		return graphics;
	}

	/** Renders a label. */
	protected static void renderLabel(LabelLayoutData labelData, Graphics2D graphics) {
		renderLabel(labelData, EHorizontalAlignment.CENTER, graphics, false);
	}

	/** Renders a label. */
	protected static void renderLabel(LabelLayoutData labelData, EHorizontalAlignment horizontalAlignment,
			Graphics2D graphics, boolean normalizeHtml) {
		if (labelData == null || !labelData.isVisible()) {
			return;
		}

		graphics.setColor(labelData.getColor());
		Font font = labelData.getFont().getAwtFont();

		graphics.setFont(font);

		
		String text = labelData.getText();
		if (StringUtils.isEmpty(text)) {
			return;
		}

		if (normalizeHtml) {
			text = text.replaceAll("(?s)<style.*?</style>", StringUtils.EMPTY_STRING);
			text = text.replaceAll("<.*?>", StringUtils.EMPTY_STRING);
			text = text.replaceAll("\\s+", StringUtils.SPACE).trim();
		}

		LineMetrics lineMetrics = font.getLineMetrics(text, graphics.getFontRenderContext());

		int y = (int) (labelData.getPosition().y + lineMetrics.getAscent());

		double maxWidth = determineLabelDimensions(text, font, graphics.getFontRenderContext()).getWidth();

		for (String line : StringUtils.splitLinesAsList(text)) {
			double width = font.getStringBounds(line, graphics.getFontRenderContext()).getWidth();

			int offset = 0;
			switch (horizontalAlignment) {
			case LEFT:
				break;
			case RIGHT:
				offset = (int) (maxWidth - width);
				break;
			case CENTER:
			default:
				offset = (int) ((maxWidth - width) / 2);
			}

			graphics.drawString(line, labelData.getPosition().x + offset, y);
			y += lineMetrics.getHeight();
		}
	}

	/**
	 * Returns the height of this label and the width of the longest line of
	 * this label.
	 */
	protected static Dimension determineLabelDimensions(String text, Font font, FontRenderContext fontRenderContext) {
		if (StringUtils.isEmpty(text)) {
			return new Dimension(0, 0);
		}
		double maxWidth = 0;
		double height = 0;
		double defaultLineHeight = new TextLayout("Line", font, fontRenderContext).getBounds().getHeight();
		for (String line : StringUtils.splitLinesAsList(text)) {
			if (line.isEmpty()) {
				height += defaultLineHeight;
			} else {
				Rectangle2D bounds = new TextLayout(line, font, fontRenderContext).getBounds();
				maxWidth = Math.max(maxWidth, bounds.getWidth());
				height += bounds.getHeight();
			}
		}
		return new Dimension((int) maxWidth, (int) height);
	}
}
