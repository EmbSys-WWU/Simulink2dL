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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.datahandler.ESimulinkColor;
import org.conqat.lib.simulink.model.datahandler.FontData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.model.datahandler.LineLayoutData;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utility method for layouting Simulink lines and labels.
 */
/* package */ class SimulinkLineLayoutUtils {

	/** Returns the layout data used for rendering a line. */
	public static LineLayoutData extractLineLayoutData(SimulinkLine line, ILogger logger) {
		List<Point> points = extractLinePoints(line, logger);

		Color color;
		if (line.getSrcPort() == null || line.getDstPort() == null) {
			color = ESimulinkColor.RED.getColor();
		} else {
			color = SimulinkColorUtils.extractColor(line.getSrcPort().getBlock(),
					SimulinkConstants.PARAM_FOREGROUND_COLOR, Color.BLACK, logger);
		}

		return new LineLayoutData(points, color);
	}

	/** Extracts the layout points for a line. */
	private static List<Point> extractLinePoints(SimulinkLine line, ILogger logger) {
		List<Point> points = new ArrayList<>();

		if (line.getSrcPort() != null) {
			points.add(line.getSrcPort().obtainLayoutData().getPosition());

			// Simulink has an implicit bend point directly after the source
			// port
			Point sourceBend = SimulinkPortLayoutUtils.getPrePortPoint(line.getSrcPort(),
					line.getSrcPort().getBlock().obtainLayoutData());
			points.add(sourceBend);
		}

		extractPoints(line, points, logger);

		if (line.getDstPort() != null) {
			SimulinkBlock destinationBlock = line.getDstPort().getBlock();
			points.add(SimulinkPortLayoutUtils.getPrePortPoint(line.getDstPort(), destinationBlock.obtainLayoutData()));
			points.add(line.getDstPort().obtainLayoutData().getPosition());
		}

		if (isInportToSubsystemLine(line)) {
			Collections.reverse(points);
			straightenLine(points);
			Collections.reverse(points);
		} else if (lineCanBeFixed(line)) {
			straightenLine(points);
		}
		return points;
	}

	/**
	 * Returns whether the given line is a straight connection from inport to
	 * subsystem.
	 */
	private static boolean isInportToSubsystemLine(SimulinkLine line) {
		return line.getSrcPort() != null && line.getDstPort() != null
				&& SimulinkConstants.TYPE_INPORT.equals(line.getSrcPort().getBlock().getType())
				&& (SimulinkConstants.SUBSYSTEM.equals(line.getDstPort().getBlock().getType())
						|| isBigBlock(line.getDstPort().getBlock()));
	}

	/**
	 * Returns whether the given block is counted as "big" w.r.t. line fixing.
	 */
	private static boolean isBigBlock(SimulinkBlock block) {
		Rectangle position = block.obtainLayoutData().getPosition();
		return position.getWidth() > 100 && position.getHeight() > 100;
	}

	/** Returns whether the line can be fixed by straigthening. */
	private static boolean lineCanBeFixed(SimulinkLine line) {
		return line.getSrcPort() != null && line.getDstPort() != null && line.getSrcPort().getLines().size() == 1
				&& !SimulinkConstants.TYPE_INPORT.equals(line.getSrcPort().getBlock().getType());
	}

	/** Attempts to straighten the layout of the given line. */
	private static void straightenLine(List<Point> points) {
		if (points.size() < 3) {
			return;
		}

		Point last = points.get(points.size() - 1);
		Point last1 = points.get(points.size() - 2);
		Point last2 = points.get(points.size() - 3);

		int deltaX = last.x - last1.x;
		int deltaY = last.y - last1.y;

		int deltaX1 = last1.x - last2.x;
		int deltaY1 = last1.y - last2.y;

		if (deltaX == 0) {
			if (deltaX1 != 0 && Math.abs(deltaX1) <= SimulinkPortLayoutUtils.PORT_SPACING_GRID) {
				fixLine(points, deltaX1, 0);
			}
		} else if (deltaY == 0) {
			if (deltaY1 != 0 && Math.abs(deltaY1) <= SimulinkPortLayoutUtils.PORT_SPACING_GRID) {
				fixLine(points, 0, deltaY1);
			}
		}
	}

	/**
	 * Fixes the line by applying a delta to all points (excluding the end
	 * points)
	 */
	private static void fixLine(List<Point> points, int deltaX, int deltaY) {
		// do not fix last 2 points
		int end = points.size() - 2;
		for (int i = 0; i < end; ++i) {
			points.get(i).translate(deltaX, deltaY);
		}
	}

	/**
	 * Extracts the (inner) points for a line and adds them to the given points
	 * list.
	 */
	private static void extractPoints(SimulinkLine line, List<Point> points, ILogger logger) {
		String pointsText = line.getParameter(SimulinkConstants.PARAM_POINTS);
		if (pointsText == null) {
			// this happens for lines without extra bend points
			return;
		}

		try {
			int[] pointsArray = SimulinkUtils.getIntParameterArray(pointsText);
			for (int i = 0; i < pointsArray.length / 2; i++) {
				Point point = new Point(pointsArray[2 * i], pointsArray[2 * i + 1]);
				if (!points.isEmpty()) {
					Point lastPoint = CollectionUtils.getLast(points);
					point.x += lastPoint.x;
					point.y += lastPoint.y;
				}
				points.add(point);
			}
		} catch (NumberFormatException e) {
			logger.error("Points array contained invalid number: " + pointsText + ". Skipping points.");
		}
	}

	/**
	 * Returns the layout data used for rendering a line's label or null if no
	 * label should be shown.
	 */
	public static LabelLayoutData extractLineLabelLayoutData(SimulinkLine line, LineLayoutData lineLayoutData,
			FontData font, ILogger logger) {
		int[] labels = extractLineLabels(line, logger);
		if (labels == null) {
			return null;
		}

		List<Point> points = lineLayoutData.getPoints();
		if (line.getSrcPort() == null) {
			// replicate artificial start segment for unconnected lines
			points.add(0, points.get(0));
		}

		String text = determineLineLabelText(line);
		if (StringUtils.isEmpty(text)) {
			return null;
		}
		text = SimulinkUtils.replaceSimulinkLineBreaks(text);

		int segment = labels[0];
		if (segment + 1 >= points.size()) {
			logger.error("Invalid segment " + segment + " used for line " + text);
			return null;
		}

		Point position = calculateLineLabelPosition(segment, labels[1], points,
				LayoutHandlerBase.determineTextBounds(text, font.getAwtFont()));

		Color color = Color.BLACK;
		if (line.getSrcPort() != null && line.getDstPort() != null) {
			color = lineLayoutData.getColor();
		}

		return new LabelLayoutData(text, true, font, position, color, 1.0);
	}

	/** Returns the label text to use for the line (or null). */
	private static String determineLineLabelText(SimulinkLine line) {
		SimulinkOutPort srcPort = line.getSrcPort();
		if (srcPort != null && SimulinkConstants.VALUE_ON
				.equals(srcPort.getParameter(SimulinkConstants.PARAM_SHOW_PROPAGATED_SIGNALS))) {
			String signalName = srcPort.getParameter(SimulinkConstants.PARAM_PROPAGATED_SIGNALS);
			// display empty label only for non-top-level blocks
			// (http://de.mathworks.com/help/simulink/ug/signal-label-propagation.html)
			if (signalName != null || srcPort.getBlock().getParent() != srcPort.getBlock().getModel()) {
				return "<" + StringUtils.emptyIfNull(signalName) + ">";
			}
		}
		return line.getParameter(SimulinkConstants.PARAM_NAME);
	}

	/**
	 * Extracts the values of the labels parameter for a line. If no labels
	 * parameter is found or an error occurs, null is returned.
	 */
	private static int[] extractLineLabels(SimulinkLine line, ILogger logger) {
		String labelsString = line.getParameter(SimulinkConstants.PARAM_LABELS);
		if (labelsString == null) {
			return null;
		}
		int[] labels;
		try {
			labels = SimulinkUtils.getIntParameterArray(labelsString);
			if (labels.length < 2) {
				logger.error("Invalid line labels: " + labelsString);
				return null;
			}
		} catch (NumberFormatException e) {
			logger.error("Had invalid labels parameter: " + labelsString);
			return null;
		}
		return labels;
	}

	/**
	 * Calculates the position of the line label.
	 * 
	 * @param segment
	 *            the segment of the line the label is centered for.
	 * @param side
	 *            the side of the line (0 or 1). This is interpreted
	 *            differently, depending on the direction of the line.
	 */
	private static Point calculateLineLabelPosition(int segment, int side, List<Point> points, Rectangle textBounds) {
		boolean rightAligned = segment < 0;
		if (rightAligned) {
			segment = points.size() - 2;
		}

		int x1 = points.get(segment).x;
		int x2 = points.get(segment + 1).x;
		double x = (x2 + x1) / 2.;

		int y1 = points.get(segment).y;
		int y2 = points.get(segment + 1).y;
		double y = (y2 + y1) / 2.;

		if (x2 == x1) {
			// vertical line -> left or right of line
			y -= textBounds.height / 2.;
			if (side == 0 ^ (y1 > y2)) {
				x += SimulinkLayoutHandler.LABEL_DISTANCE;
			} else {
				x -= textBounds.width - SimulinkLayoutHandler.LABEL_DISTANCE;
			}
		} else {
			// other lines: center on top or bottom
			if (rightAligned || (segment == 0 && x1 > x2)) {
				x = x2 - textBounds.width - SimulinkLayoutHandler.LABEL_DISTANCE;
			} else if (segment > 0) {
				// do not center first
				x -= textBounds.width / 2.;
			} else {
				x += SimulinkLayoutHandler.LABEL_DISTANCE;
			}

			if (side == 0 ^ (x1 < x2) ^ (segment == 0) ^ rightAligned) {
				y += SimulinkLayoutHandler.LABEL_DISTANCE;
			} else {
				y -= textBounds.height + SimulinkLayoutHandler.LABEL_DISTANCE;
			}
		}

		return new Point((int) x, (int) y);
	}
}