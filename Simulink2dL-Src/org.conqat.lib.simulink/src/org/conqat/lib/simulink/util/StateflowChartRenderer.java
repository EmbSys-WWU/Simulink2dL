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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.RectangleLayoutData;
import org.conqat.lib.simulink.model.datahandler.TransitionLayoutData;
import org.conqat.lib.simulink.model.datahandler.stateflow.StateflowLayoutHandler;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowJunction;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

/**
 * Class for rendering a stateflow chart (i.e. its canvas and states) as an
 * image. This is not intended as a full Simulink visualization support, but
 * rather as a proof of concept and to simplify testing.
 */
public class StateflowChartRenderer extends RendererBase {

	/** Size of the arc of a state's corners. */
	private static final int STATE_ARC_SIZE = 20;

	/** Renders a single block as a {@link BufferedImage}. */
	public static BufferedImage renderChart(StateflowChart chart) {

		Rectangle canvasRectangle = determineCanvasRectangle(chart);
		BufferedImage image = new BufferedImage(canvasRectangle.width, canvasRectangle.height,
				BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D graphics = createGraphics(canvasRectangle, image);

		renderNodes(graphics, chart.getNodes());

		for (StateflowTransition transition : SimulinkUtils.getAllTransitions(chart)) {
			renderTransition(transition, graphics);
		}
		return image;
	}

	/** Draws a set of nodes. */
	private static void renderNodes(Graphics2D graphics, Collection<StateflowNodeBase> nodes) {
		for (StateflowNodeBase node : CollectionUtils.sort(nodes,
				Comparator.comparing(StateflowNodeBase::getStateflowId))) {
			renderNode(graphics, node);

			if (node instanceof StateflowState) {
				StateflowState state = (StateflowState) node;
				renderNodes(graphics, state.getNodes());
			}
		}
	}

	/** Draws a single node. */
	private static void renderNode(Graphics2D graphics, StateflowNodeBase node) {
		RectangleLayoutData layoutData = node.obtainLayoutData();
		Rectangle position = layoutData.getPosition();

		graphics.setColor(layoutData.getBackgroundColor());
		if (node instanceof StateflowState) {
			StateflowState state = (StateflowState) node;
			if (state.isNoteBox()) {
				// nothing to do
			} else if (state.isFunctionState() || state.isGroupState()) {
				graphics.fillRect(position.x, position.y, position.width, position.height);
			} else {
				graphics.fillRoundRect(position.x, position.y, position.width, position.height, STATE_ARC_SIZE,
						STATE_ARC_SIZE);
			}
		} else if (node instanceof StateflowJunction) {
			graphics.fillOval(position.x, position.y, position.width, position.height);
		}

		graphics.setColor(layoutData.getForegroundColor());
		if (node instanceof StateflowState) {
			renderStateflowStateBackgroundAndLabel(graphics, (StateflowState) node, layoutData);
		} else if (node instanceof StateflowJunction) {
			graphics.drawOval(position.x, position.y, position.width, position.height);
		}
	}

	/**
	 * Renders the background color and label of a StateflowState. If the state
	 * is a subchart, it gets a different background color and we add an
	 * additional horizontal line below the label.
	 */
	private static void renderStateflowStateBackgroundAndLabel(Graphics2D graphics, StateflowState state,
			RectangleLayoutData stateLayoutData) {
		Rectangle statePosition = stateLayoutData.getPosition();
		Stroke oldStroke = graphics.getStroke();
		graphics.setStroke(stateLayoutData.getStroke());

		if (state.isNoteBox()) {
			// nothing to do
		} else if (state.isFunctionState() || state.isGroupState()) {
			graphics.drawRect(statePosition.x, statePosition.y, statePosition.width, statePosition.height);
		} else {
			graphics.drawRoundRect(statePosition.x, statePosition.y, statePosition.width, statePosition.height,
					STATE_ARC_SIZE, STATE_ARC_SIZE);
		}

		graphics.setStroke(oldStroke);
		LabelLayoutData labelData = state.obtainLabelData();
		renderLabel(labelData, graphics);
		if (state.isSubChart()) {
			Font font = labelData.getFont().getAwtFont();
			float lineHeight = font.getLineMetrics("One Line", graphics.getFontRenderContext()).getHeight()
					* (float) 1.5;
			graphics.drawLine(statePosition.x, (int) (statePosition.y + lineHeight),
					statePosition.x + statePosition.width, (int) (statePosition.y + lineHeight));
		}
	}

	/** Renders a transition. */
	private static void renderTransition(StateflowTransition transition, Graphics2D graphics) {
		TransitionLayoutData layoutData = transition.obtainLayoutData();

		List<Point> points = layoutData.getPoints();

		GeneralPath path = new GeneralPath();
		path.moveTo(points.get(0).x, points.get(0).y);

		for (int i = 1; i < points.size() - 1; i += 2) {
			path.quadTo(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
		}

		graphics.setColor(StateflowLayoutHandler.TRANSITION_COLOR);
		if (transition.getDst() == null) {
			graphics.setColor(Color.RED);
		}

		graphics.draw(path);
		renderArrow(graphics, CollectionUtils.getLast(points), points.get(points.size() - 2),
				layoutData.getArrowheadSize());

		if (transition.getSrc() == null) {
			graphics.fillArc(points.get(0).x - 2, points.get(0).y - 2, 4, 4, 0, 360);
		}

		renderLabel(transition.obtainLabelData(), graphics);
	}

	/** Renders an arrow for a transition. */
	private static void renderArrow(Graphics2D graphics, Point position, Point lineStart, int arrowheadSize) {
		AffineTransform oldTransform = graphics.getTransform();
		double theta = Math.atan2(position.y - lineStart.y, position.x - lineStart.x);
		graphics.translate(position.x, position.y);
		graphics.rotate(theta);
		graphics.drawPolygon(createArrowHeadPolygon(arrowheadSize));
		graphics.setTransform(oldTransform);
	}

	/** Creates polygons used for arrow heads of lines. */
	private static Polygon createArrowHeadPolygon(int size) {
		return new Polygon(new int[] { 0, -size, -size }, new int[] { 0, size / 4, -size / 4 }, 3);
	}

	/** Returns the rectangle enclosing all children of the given block. */
	private static Rectangle determineCanvasRectangle(StateflowChart chart) {
		Rectangle canvasRectangle = null;

		for (StateflowNodeBase node : chart.getNodes()) {
			canvasRectangle = enlargeCanvasRectangle(node.obtainLayoutData(), canvasRectangle);
			if (node instanceof StateflowState) {
				canvasRectangle = enlargeCanvasRectangle(((StateflowState) node).obtainLabelData(), canvasRectangle);
			}
		}

		if (canvasRectangle == null) {
			return new Rectangle(OUTPUT_CANVAS_PADDING, OUTPUT_CANVAS_PADDING);
		}

		for (StateflowTransition transition : SimulinkUtils.getAllTransitions(chart)) {
			enlargeCanvasRectangle(transition.obtainLabelData(), canvasRectangle);
			TransitionLayoutData layoutData = transition.obtainLayoutData();
			for (Point point : layoutData.getPoints()) {
				canvasRectangle.add(point);
			}
		}

		canvasRectangle.grow(OUTPUT_CANVAS_PADDING, OUTPUT_CANVAS_PADDING);
		return canvasRectangle;
	}

	/**
	 * Enlarges the canvas such that the whole label can be displayed. Also for
	 * multi-line labels.
	 */
	private static Rectangle enlargeCanvasRectangle(LabelLayoutData labelData, Rectangle canvasRectangle) {
		if (labelData != null) {
			Font font = labelData.getFont().getAwtFont();
			/*
			 * using a default FontRendererContext here. The FRC from the
			 * graphics object would be better, but we need the rectangle
			 * computed here to create the graphics object...
			 */
			Dimension labelDimensions = determineLabelDimensions(labelData.getText(), font,
					new FontRenderContext(null, false, false));
			// add two corner points of the labelRectangle (in global
			// coordinates)
			Rectangle labelPosition = new Rectangle(labelData.getPosition().x, labelData.getPosition().y,
					(int) labelDimensions.getWidth(), (int) labelDimensions.getHeight());
			if (canvasRectangle == null) {
				canvasRectangle = labelPosition;
			} else {
				canvasRectangle.add(labelPosition);
			}
		}
		return canvasRectangle;
	}

	/** Enlarges a given canvas rectangle to also include the given node. */
	private static Rectangle enlargeCanvasRectangle(RectangleLayoutData layoutData, Rectangle canvasRectangle) {
		if (canvasRectangle == null) {
			canvasRectangle = layoutData.getPosition();
		} else {
			canvasRectangle.add(layoutData.getPosition());
		}
		return canvasRectangle;
	}
}
