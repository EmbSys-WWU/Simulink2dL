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

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.AnnotationLayoutData;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.EHorizontalAlignment;
import org.conqat.lib.simulink.model.datahandler.EOrientation;
import org.conqat.lib.simulink.model.datahandler.FontData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.model.datahandler.LineLayoutData;
import org.conqat.lib.simulink.model.datahandler.PortLayoutData;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Layout handler for Simulink.
 */
public abstract class SimulinkLayoutHandler extends LayoutHandlerBase {

	/** Distance of a label from the block or line. */
	/* package */ static final int LABEL_DISTANCE = 4;

	/** Constructor. */
	/* package */ SimulinkLayoutHandler(ModelBuildingParameters parameters) {
		super(parameters);
	}

	/** Extracts the font information to be used. */
	public FontData extractFontData(ParameterizedElement element) {
		return extractFontData(element, logger);
	}

	/** Returns the layout data used for rendering a block. */
	public BlockLayoutData obtainBlockLayoutData(SimulinkBlock block) {
		Color foregroundColor = SimulinkColorUtils.extractColor(block, SimulinkConstants.PARAM_FOREGROUND_COLOR,
				Color.BLACK, logger);
		Color backgroundColor = SimulinkColorUtils.extractColor(block, SimulinkConstants.PARAM_BACKGROUND_COLOR,
				Color.WHITE, logger);
		double opacity = setOpacityLevel(block);
		return new BlockLayoutData(SimulinkElementLayoutUtils.extractPosition(block, logger), extractOrientation(block),
				foregroundColor, backgroundColor, opacity);
	}

	/**
	 * Sets the opacity level of the block's layout and labels. For inactive
	 * blocks, the opacity is set to 0.5, otherwise it is 1.0.
	 */
	private static double setOpacityLevel(SimulinkBlock block) {
		String blockSourceType = block.getSourceType();
		if (SimulinkConstants.TYPE_BACE_STATIC_ASSERTION.equals(blockSourceType)) {
			return 0.5;
		}
		SimulinkBlock parent = block.getParent();
		if (parent == null) {
			return 1.0;
		}
		// Handling the case of inactive blocks in Variant Subsystems
		if (parent.getType().equals(SimulinkConstants.SUBSYSTEM)
				&& SimulinkConstants.VALUE_ON.equals(parent.getParameter(SimulinkConstants.PARAM_VARIANT))) {
			String overridingVariant = parent.getParameter(SimulinkConstants.PARAM_OVERRIDE_VARIANT);
			if (overridingVariant == null || (block.getType().equals(SimulinkConstants.SUBSYSTEM)
					&& !block.getParameter(SimulinkConstants.PARAM_VARIANT_CONTROL).equals(overridingVariant))) {
				return 0.5;
			}
		}
		return 1.0;
	}

	/** Returns the layout data used for rendering an annotation. */
	public AnnotationLayoutData obtainAnnotationLayoutData(SimulinkAnnotation annotation) {
		boolean borderVisible = SimulinkConstants.VALUE_ON
				.equals(annotation.getParameter(SimulinkConstants.PARAM_DROP_SHADOW));
		EHorizontalAlignment horizontalAlignment = EHorizontalAlignment
				.parse(annotation.getParameter(SimulinkConstants.PARAM_HORIZONTAL_ALIGNMENT));

		return new AnnotationLayoutData(SimulinkElementLayoutUtils.extractPosition(annotation, logger),
				SimulinkColorUtils.extractColor(annotation, SimulinkConstants.PARAM_FOREGROUND_COLOR, Color.BLACK,
						logger),
				SimulinkColorUtils.extractColor(annotation, SimulinkConstants.PARAM_BACKGROUND_COLOR, Color.WHITE,
						logger),
				borderVisible, horizontalAlignment);
	}

	/** Calculates the orientation. */
	protected abstract EOrientation extractOrientation(SimulinkElementBase element);

	/** Returns the layout data used for rendering a port. */
	public PortLayoutData obtainPortLayoutData(SimulinkPortBase port) {
		BlockLayoutData blockLayoutData = obtainBlockLayoutData(port.getBlock());
		return new PortLayoutData(SimulinkPortLayoutUtils.getPortLocation(port, blockLayoutData),
				SimulinkPortLayoutUtils.determineDirection(port, blockLayoutData), blockLayoutData.getForegroundColor(),
				blockLayoutData.getOpacity());
	}

	/** Returns the layout data used for rendering a line. */
	public LineLayoutData obtainLineLayoutData(SimulinkLine line) {
		return SimulinkLineLayoutUtils.extractLineLayoutData(line, logger);
	}

	/**
	 * Returns the layout data used for rendering a line's label or null if no
	 * label should be shown.
	 */
	public LabelLayoutData obtainLineLabelData(SimulinkLine line) {
		return SimulinkLineLayoutUtils.extractLineLabelLayoutData(line, obtainLineLayoutData(line),
				extractFontData(line, logger), logger);
	}

	/** Returns the label data used for rendering a block's label. */
	public LabelLayoutData obtainBlockLabelData(SimulinkBlock block) {
		return SimulinkElementLayoutUtils.extractBlockLabelData(block, obtainBlockLayoutData(block), logger);
	}

	/** Returns the label used inside of a block (or null). */
	public LabelLayoutData obtainBlockInnerLabelData(SimulinkBlock block) {
		return SimulinkElementLayoutUtils.extractBlockInnerLabelData(block, obtainBlockLayoutData(block), logger);
	}

	/**
	 * Returns the label data used for rendering a block's sub label (may return
	 * null).
	 */
	public LabelLayoutData obtainBlockSubLabelData(SimulinkBlock block) {
		String formatString = block.getParameter("AttributesFormatString");
		if (StringUtils.isEmpty(formatString)) {
			return null;
		}

		return SimulinkElementLayoutUtils.extractBlockSubLabelData(block, formatString, obtainBlockLayoutData(block),
				logger);
	}

	/** Returns the label data used for rendering a port's label or null. */
	public LabelLayoutData obtainPortLabelData(SimulinkPortBase port) {
		return SimulinkPortLabelUtils.extractPortLabelData(port);
	}

	/** Calculates the label data for the annotation's label or null. */
	public LabelLayoutData obtainAnnotationLabelData(SimulinkAnnotation annotation) {
		String text = annotation.getParameter(SimulinkConstants.PARAM_NAME);
		if (StringUtils.isEmpty(text)) {
			return null;
		}
		text = SimulinkUtils.replaceSimulinkLineBreaks(text);
		text = text.replaceAll("\\\\\"", "\"");

		AnnotationLayoutData layoutData = obtainAnnotationLayoutData(annotation);

		Point position = new Point(layoutData.getPosition().x + LABEL_DISTANCE,
				layoutData.getPosition().y + LABEL_DISTANCE / 2);
		return new LabelLayoutData(text, true, extractFontData(annotation), position, layoutData.getForegroundColor(),
				1.0);
	}
}
