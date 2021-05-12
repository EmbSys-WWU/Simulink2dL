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
package org.conqat.lib.simulink.model.datahandler.stateflow;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.datahandler.ESimulinkStrokeStyle;
import org.conqat.lib.simulink.model.datahandler.FontData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.model.datahandler.StateLayoutData;
import org.conqat.lib.simulink.model.datahandler.TransitionLayoutData;
import org.conqat.lib.simulink.model.stateflow.StateflowJunction;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Layout handler for stateflow.
 */
public class StateflowLayoutHandler extends LayoutHandlerBase {

	/** Default size of arrowheads in stateflow. */
	private static final int DEFAULT_ARROWHEAD_SIZE = 8;

	/** Factor used in the calculation of transition bend points. */
	private static final double TRANSITION_BEND_POINT_FACTOR = .25;

	/** Color used for transitions. */
	public static final Color TRANSITION_COLOR = new Color(102, 152, 202);

	/** Background color for states. */
	private static final Color STATE_BACKGROUND_COLOR = new Color(255, 248, 240);

	/** Background color for states. */
	private static final Color SUBCHART_BACKGROUND_COLOR = new Color(237, 233, 211);

	/** Background color for function states. */
	private static final Color FUNCTION_BACKGROUND_COLOR = new Color(235, 235, 235);

	/** Border color for junctions. */
	private static final Color JUNCTION_BORDER_COLOR = new Color(207, 140, 26);

	/**
	 * Default position (which in Simulink terms also contains dimensions) used
	 * for nodes in case of errors while parsing position.
	 */
	private static final Rectangle NODE_DEFAULT_POSITION = new Rectangle(10, 10, 10, 10);

	/** Offset for state labels. */
	private static final int STATE_LABEL_OFFSET = 4;

	/** Constructor. */
	public StateflowLayoutHandler(ModelBuildingParameters parameters) {
		super(parameters);
	}

	/** Returns layout information for the given state. */
	public StateLayoutData obtainStateLayoutData(StateflowState state) {
		Color backgroundColor = STATE_BACKGROUND_COLOR;
		ESimulinkStrokeStyle strokeStyle = ESimulinkStrokeStyle.DEFAULT;
		if (state.isSubChart()) {
			strokeStyle = ESimulinkStrokeStyle.SUBCHART_OUTLINE_STROKE;
			backgroundColor = SUBCHART_BACKGROUND_COLOR;
		}

		if (state.isSimulinkFunction() || state.isMatlabFunction()) {
			backgroundColor = FUNCTION_BACKGROUND_COLOR;
		}

		if (SimulinkConstants.AND_STATE_TYPE.equals(state.getParameter(SimulinkConstants.PARAM_TYPE))) {
			strokeStyle = ESimulinkStrokeStyle.DASHED;
		}

		return new StateLayoutData(extractStateflowPosition(state), Color.BLACK, backgroundColor, strokeStyle);
	}

	/** Extracts the position data from a stateflow node. */
	private Rectangle extractStateflowPosition(StateflowNodeBase node) {
		String positionString = node.getParameter("position");
		if (positionString == null) {
			logger.error("No position contained in model for element " + node.getStateflowId()
					+ ". Using default position.");
			return NODE_DEFAULT_POSITION;
		}
		double[] positionArray = SimulinkUtils.getDoubleParameterArray(positionString);

		if (positionArray.length == 3) {
			// junctions have only 3 coordinates, which are interpreted as
			// center and radius
			positionArray = new double[] { positionArray[0] - positionArray[2], positionArray[1] - positionArray[2],
					2 * positionArray[2], 2 * positionArray[2] };
		} else if (positionArray.length != 4) {
			logger.error("Unsupported position array found in model (length = " + positionArray.length
					+ " instead of 4) for element " + node.getStateflowId() + ". Using default position.");
			return NODE_DEFAULT_POSITION;
		}

		return new Rectangle((int) positionArray[0], (int) positionArray[1], (int) positionArray[2],
				(int) positionArray[3]);
	}

	/** Returns layout information for the given junction. */
	public StateLayoutData obtainJunctionLayoutData(StateflowJunction junction) {
		return new StateLayoutData(extractStateflowPosition(junction), JUNCTION_BORDER_COLOR, STATE_BACKGROUND_COLOR);
	}

	/** Returns label information for the state. */
	public LabelLayoutData obtainStateLabelData(StateflowState state) {
		String text = state.getParameter(SimulinkConstants.PARAM_LABEL_STRING);

		Rectangle statePosition = obtainStateLayoutData(state).getPosition();

		Point position = new Point(statePosition.x + STATE_LABEL_OFFSET, statePosition.y + STATE_LABEL_OFFSET);
		return new LabelLayoutData(text, true, extractStateflowFontData(state), position, Color.BLACK, 1.0);
	}

	/** Returns layout information for transitions. */
	public TransitionLayoutData obtainTransitionLayoutData(StateflowTransition transition) {
		List<Point> points = new ArrayList<>();

		double[] src = SimulinkUtils.getDoubleParameterArray(transition.getParameter("src_intersection"));
		double[] mid = SimulinkUtils.getDoubleParameterArray(transition.getParameter("midPoint"));
		double[] dst = SimulinkUtils.getDoubleParameterArray(transition.getParameter("dst_intersection"));
		int arrowheadSize = getArrowHeadSizeForDestination(transition.getDst());

		if (src.length < 6 || mid.length < 2 || dst.length < 6) {
			logger.error("Missing layout data for transition " + transition);
		} else {
			Point srcPoint = new Point((int) src[4], (int) src[5]);
			Point midPoint = new Point((int) mid[0], (int) mid[1]);
			Point dstPoint = new Point((int) dst[4], (int) dst[5]);

			double factor = TRANSITION_BEND_POINT_FACTOR * srcPoint.distance(dstPoint);

			points.add(srcPoint);
			points.add(new Point((int) (srcPoint.x + src[1] * factor), (int) (srcPoint.y + src[2] * factor)));
			points.add(midPoint);
			points.add(new Point((int) (dstPoint.x + dst[1] * factor), (int) (dstPoint.y + dst[2] * factor)));
			points.add(dstPoint);
		}

		return new TransitionLayoutData(points, arrowheadSize);
	}

	/**
	 * Extracts the arrowhead size of a transition based on the destination of
	 * the transition. Arrowhead size is stored in the destination node. All
	 * arrows to this node have the same head size.
	 */
	private int getArrowHeadSizeForDestination(StateflowNodeBase transitionDestination) {
		if (transitionDestination == null) {
			return DEFAULT_ARROWHEAD_SIZE;
		}
		int arrowheadSize = DEFAULT_ARROWHEAD_SIZE;
		String arrowheadSizeString = transitionDestination.getDeclaredParameter("arrowSize");
		if (arrowheadSizeString != null) {
			try {
				arrowheadSize = (int) Math.round(Double.parseDouble(arrowheadSizeString));
				if (arrowheadSize <= 0) {
					arrowheadSize = DEFAULT_ARROWHEAD_SIZE;
				}
			} catch (NumberFormatException e) {
				logger.warn(
						"Found non-integer arrowhead size: " + arrowheadSizeString + " Using default size 8 instead.");
			}
		}
		return arrowheadSize;
	}

	/** Returns layout information for transition labels. */
	public LabelLayoutData obtainTransitionLabelData(StateflowTransition transition) {
		String text = transition.getLabel();
		if (StringUtils.isEmpty(text)) {
			return null;
		}

		String labelPositionString = transition.getParameter("labelPosition");
		if (labelPositionString == null) {
			logger.error("Missing label position for transition " + transition.toString());
			return null;
		}

		double[] labelPosition = SimulinkUtils.getDoubleParameterArray(labelPositionString);
		if (labelPosition.length < 2) {
			logger.error("Invalid label position for transition " + transition.toString());
			return null;
		}

		Point position = new Point((int) labelPosition[0], (int) labelPosition[1]);
		return new LabelLayoutData(text, true, extractStateflowFontData(transition), position, TRANSITION_COLOR, 1.0);
	}

	/** Extracts the font information to be used. */
	private FontData extractStateflowFontData(ParameterizedElement element) {
		String fontName = Font.SANS_SERIF;
		int fontSize = DEFAULT_FONT_SIZE;
		String fontSizeValue = element.getParameter("fontSize");
		if (fontSizeValue != null) {
			try {
				fontSize = Integer.parseInt(fontSizeValue);
			} catch (NumberFormatException e) {
				logger.error("Invalid font size value: " + fontSizeValue);
			}
		}

		return new FontData(fontName, fontSize, false, false);
	}

}
