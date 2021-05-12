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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.AnnotationLayoutData;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.EInterpreter;
import org.conqat.lib.simulink.model.datahandler.ESimulinkStrokeStyle;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.model.datahandler.LineLayoutData;
import org.conqat.lib.simulink.model.datahandler.PortLayoutData;

/**
 * Class for rendering a simulink subsystem block (i.e. its canvas and children)
 * as an image. This is not intended as a full Simulink visualization support,
 * but rather as a proof of concept and to simplify testing.
 */
public class SimulinkBlockRenderer extends RendererBase {

	/** The polygon used for arrow heads of lines. */
	private static final Polygon ARROW_HEAD_POLYGON = new Polygon(new int[] { 0, -6, -6 }, new int[] { 0, 4, -4 }, 3);

	/** Size of the port arrows in pixel. */
	private static final int PORT_ARROW_SIZE = 4;

	/** Size of the arrows for unconnected lines in pixel. */
	private static final int UNCONNECTED_LINE_ARROW_SIZE = 6;

	/** Renders a single block as a {@link BufferedImage}. */
	public static BufferedImage renderBlock(SimulinkBlock block) {

		Rectangle canvasRectangle = determineCanvasRectangle(block);
		BufferedImage image = new BufferedImage(canvasRectangle.width, canvasRectangle.height,
				BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D graphics = createGraphics(canvasRectangle, image);

		for (SimulinkAnnotation annotation : block.getAnnotations()) {
			renderAnnotation(annotation, graphics);
		}

		for (SimulinkBlock subBlock : block.getSubBlocks()) {
			renderSubBlock(subBlock, graphics);
		}

		// sort lines to ensure stable rendering results; the exact order does
		// not matter, as long as it is stable between calls.
		List<SimulinkLine> sortedLines = CollectionUtils.sort(block.getContainedLines(),
				new Comparator<SimulinkLine>() {
					@Override
					public int compare(SimulinkLine line1, SimulinkLine line2) {
						return line1.toLineString().compareTo(line2.toLineString());
					}
				});
		for (SimulinkLine line : sortedLines) {
			renderLine(line, graphics);
		}

		return image;
	}

	/** Renders an annotation. */
	private static void renderAnnotation(SimulinkAnnotation annotation, Graphics2D graphics) {
		AnnotationLayoutData layoutData = annotation.obtainLayoutData();

		Rectangle position = layoutData.getPosition();
		graphics.setColor(layoutData.getBackgroundColor());
		graphics.fillRect(position.x, position.y, position.width, position.height);

		if (layoutData.isBorderVisible()) {
			graphics.setColor(layoutData.getForegroundColor());
			graphics.drawRect(position.x, position.y, position.width, position.height);
		}

		LabelLayoutData labelData = annotation.obtainLabelData();
		if (labelData != null) {
			renderLabel(labelData, layoutData.getHorizontalAlignment(), graphics,
					annotation.getInterpreter() == EInterpreter.RICH);
		}

		if (annotation.containsImage()) {
			BufferedImage image = annotation.getImage();
			if (image != null) {
				graphics.drawImage(image.getScaledInstance(position.width, position.height, 1), position.x, position.y,
						null);
			}
		}
	}

	/** Renders a single sub block. */
	private static void renderSubBlock(SimulinkBlock subBlock, Graphics2D graphics) {
		renderPorts(subBlock.getInPorts(), graphics);
		renderPorts(subBlock.getOutPorts(), graphics);

		BlockLayoutData layoutData = subBlock.obtainLayoutData();
		Rectangle position = layoutData.getPosition();

		boolean rounded = SimulinkUtils.isRoundSum(subBlock) || SimulinkUtils.isFunctionCallSplitBlock(subBlock);

		graphics.setColor(layoutData.getBackgroundColor());
		if (rounded) {
			graphics.fillOval(position.x, position.y, position.width, position.height);
		} else {
			graphics.fillRect(position.x, position.y, position.width, position.height);
		}
		graphics.setColor(layoutData.getForegroundColor());

		Stroke oldStroke = graphics.getStroke();
		graphics.setStroke(layoutData.getStroke());
		if (rounded) {
			graphics.drawOval(position.x, position.y, position.width, position.height);
		} else {
			graphics.drawRect(position.x, position.y, position.width, position.height);
		}
		graphics.setStroke(oldStroke);

		renderLabel(subBlock.obtainLabelData(), graphics);
		renderLabel(subBlock.obtainSubLabelData(), graphics);
		renderLabel(subBlock.obtainInnerLabelData(), graphics);
		renderPortLabels(subBlock.getInPorts(), graphics);
		renderPortLabels(subBlock.getOutPorts(), graphics);
	}

	/** Renders the given ports. */
	private static void renderPorts(Collection<? extends SimulinkPortBase> ports, Graphics2D graphics) {
		for (SimulinkPortBase port : ports) {
			renderPort(port, graphics);
		}
	}

	/** Renders a single port. */
	private static void renderPort(SimulinkPortBase port, Graphics2D graphics) {
		// only unconnected ports have a graphical representation
		if (port.isConnected()) {
			return;
		}

		PortLayoutData layoutData = port.obtainLayoutData();
		graphics.setColor(layoutData.getColor());

		int xOffset = 0;
		if (port instanceof SimulinkOutPort) {
			xOffset = PORT_ARROW_SIZE + 1;
		}

		AffineTransform oldTransform = graphics.getTransform();
		graphics.translate(layoutData.getPosition().x, layoutData.getPosition().y);
		graphics.rotate(-layoutData.getDirection() * Math.PI / 180.);
		graphics.translate(xOffset, 0);

		Stroke oldStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2f));

		graphics.drawLine(-PORT_ARROW_SIZE, PORT_ARROW_SIZE, 0, 0);
		graphics.drawLine(-PORT_ARROW_SIZE, -PORT_ARROW_SIZE, 0, 0);

		graphics.setStroke(oldStroke);
		graphics.setTransform(oldTransform);
	}

	/** Renders the labels of the given ports. */
	private static void renderPortLabels(Collection<? extends SimulinkPortBase> ports, Graphics2D graphics) {
		for (SimulinkPortBase port : ports) {
			renderLabel(port.obtainLabelData(), graphics);
		}
	}

	/** Renders a single line. */
	private static void renderLine(SimulinkLine line, Graphics2D graphics) {
		LineLayoutData layoutData = line.obtainLayoutData();

		graphics.setColor(layoutData.getColor());

		Stroke oldStroke = graphics.getStroke();
		if (line.getSrcPort() == null || line.getDstPort() == null) {
			graphics.setStroke(ESimulinkStrokeStyle.UNCONNECTED_LINE_STROKE.getStroke());
		}

		List<Point> points = layoutData.getPoints();
		for (int i = 1; i < points.size(); ++i) {
			Point from = points.get(i - 1);
			Point to = points.get(i);
			graphics.drawLine(from.x, from.y, to.x, to.y);
		}

		if (points.size() > 1) {
			Point last = CollectionUtils.getLast(points);
			Point previous = points.get(points.size() - 2);
			renderArrow(graphics, last, previous, last, line.getDstPort() != null);

			if (line.getSrcPort() == null) {
				renderArrow(graphics, points.get(0), points.get(0), points.get(1), false);
			}
		}

		graphics.setStroke(oldStroke);

		renderLabel(line.obtainLabelData(), graphics);
	}

	/** Renders an arrow for a line. */
	private static void renderArrow(Graphics2D graphics, Point position, Point lineStart, Point lineEnd,
			boolean filled) {
		AffineTransform oldTransform = graphics.getTransform();

		double theta = Math.atan2(lineEnd.y - lineStart.y, lineEnd.x - lineStart.x);
		graphics.translate(position.x, position.y);
		graphics.rotate(theta);

		if (filled) {
			graphics.fillPolygon(ARROW_HEAD_POLYGON);
		} else {
			graphics.drawLine(-UNCONNECTED_LINE_ARROW_SIZE, UNCONNECTED_LINE_ARROW_SIZE, 0, 0);
			graphics.drawLine(-UNCONNECTED_LINE_ARROW_SIZE, -UNCONNECTED_LINE_ARROW_SIZE, 0, 0);
		}

		graphics.setTransform(oldTransform);
	}

	/** Returns the rectangle enclosing all children of the given block. */
	private static Rectangle determineCanvasRectangle(SimulinkBlock block) {
		Rectangle canvasRectangle = null;

		for (SimulinkBlock subBlock : block.getSubBlocks()) {
			canvasRectangle = enlargeCanvasRectangle(subBlock, canvasRectangle);
		}

		for (SimulinkAnnotation annotation : block.getAnnotations()) {
			AnnotationLayoutData layoutData = annotation.obtainLayoutData();
			if (canvasRectangle == null) {
				canvasRectangle = layoutData.getPosition();
			} else {
				canvasRectangle.add(layoutData.getPosition());
			}
		}

		if (canvasRectangle == null) {
			return new Rectangle(OUTPUT_CANVAS_PADDING, OUTPUT_CANVAS_PADDING);
		}

		for (SimulinkLine line : block.getContainedLines()) {
			LineLayoutData layoutData = line.obtainLayoutData();
			for (Point point : layoutData.getPoints()) {
				canvasRectangle.add(point);
			}
		}

		canvasRectangle.grow(OUTPUT_CANVAS_PADDING, OUTPUT_CANVAS_PADDING);
		return canvasRectangle;
	}

	/** Enlarges a given canvas rectangle to also include the given block. */
	private static Rectangle enlargeCanvasRectangle(SimulinkBlock subBlock, Rectangle canvasRectangle) {
		BlockLayoutData layoutData = subBlock.obtainLayoutData();
		if (canvasRectangle == null) {
			canvasRectangle = layoutData.getPosition();
		} else {
			canvasRectangle.add(layoutData.getPosition());
		}

		enlargeCanvasRectangleForLabel(canvasRectangle, subBlock.obtainLabelData());
		enlargeCanvasRectangleForLabel(canvasRectangle, subBlock.obtainSubLabelData());
		return canvasRectangle;
	}

	/** Enlarges a canvas rectangle based on layout data. */
	private static void enlargeCanvasRectangleForLabel(Rectangle canvasRectangle, LabelLayoutData labelData) {
		if (labelData == null) {
			return;
		}

		Point labelPosition = labelData.getPosition();
		canvasRectangle.add(labelPosition);

		Rectangle bounds = LayoutHandlerBase.determineTextBounds(labelData.getText(), labelData.getFont().getAwtFont());
		canvasRectangle.add(new Point(labelPosition.x + bounds.width, labelPosition.y + bounds.height));
	}

}
