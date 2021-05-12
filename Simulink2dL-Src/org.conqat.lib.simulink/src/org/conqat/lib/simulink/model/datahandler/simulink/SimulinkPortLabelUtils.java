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
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.EOrientation;
import org.conqat.lib.simulink.model.datahandler.ESimulinkBlockType;
import org.conqat.lib.simulink.model.datahandler.FontData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LayoutHandlerBase;
import org.conqat.lib.simulink.model.datahandler.PortLayoutData;
import org.conqat.lib.simulink.ui.SimulinkBlockUIData;
import org.conqat.lib.simulink.ui.SimulinkLabelOnBlockData;
import org.conqat.lib.simulink.ui.SimulinkLabelOnBlockDataConstants;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utilits methods for port label layout.
 */
public class SimulinkPortLabelUtils {

	/** The unicode "en dash". */
	private static final String EN_DASH = "\u2013";

	/** Font data used for port labels. */
	private static final FontData PORT_LABEL_FONT_DATA = new FontData(Font.SANS_SERIF, 9, false, false);

	/** Width of the port label when it has an icon displayed */
	private static final int PORT_LABEL_ICON_WIDTH = 12;

	/** Returns the label data used for rendering a port's label or null. */
	public static LabelLayoutData extractPortLabelData(SimulinkPortBase port) {
		ESimulinkBlockType blockType = SimulinkBlockUIData.determineBlockType(port.getBlock());
		return blockType.getBlockPortLabels(port);
	}

	/** Determines the data for the FlipFlop blocks ports */
	public static LabelLayoutData obtainFlipFlopPortData(SimulinkPortBase port, List<String> inPortOptions) {
		if (port instanceof SimulinkOutPort) {
			return extractPortLabelForBlockPort(port, Arrays.asList("Q", "!Q"));
		}
		return extractPortLabelForBlockPort(port, inPortOptions);
	}

	/** Determines the data for the LowPass filter BACE blocks ports */
	public static LabelLayoutData obtainBaceLowPassPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			if (port.getIndex().equals("2")) {
				return positionPortLabel(port, "state", Color.RED);
			}
			return positionPortLabel(port, "y");
		}
		String sourceType = port.getBlock().getSourceType();
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "k");
		case "2":
			return positionPortLabel(port, "u");
		case "3":
			if (SimulinkConstants.TYPE_BACE_LOWPASS_IV_EXTERNAL.equals(sourceType)) {
				return positionPortLabel(port, "state", Color.RED);
			} else if (SimulinkConstants.TYPE_BACE_LOWPASS_IV.equals(sourceType)) {
				return positionPortLabel(port, "IV");
			}
			return positionPortLabel(port, "i", Color.BLUE);
		case "4":
			return positionPortLabel(port, "R");
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/**
	 * Determines the data for the LowPass filter with constants BACE blocks ports
	 */
	public static LabelLayoutData obtainBaceLowPassConstPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "u");
		case "2":
			if (SimulinkConstants.TYPE_BACE_LOWPASS_CONSTK_IV.equals(port.getBlock().getSourceType())) {
				return positionPortLabel(port, "IV");
			}
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/** Determines the data for the Hysteresis BACE block ports */
	public static LabelLayoutData obtainBaceHysteresisPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "B");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "R", Color.BLUE);
		case "2":
			return positionPortLabel(port, "L", Color.BLUE);
		case "4":
			return positionPortLabel(port, "i", Color.BLUE);
		default:
			return null;
		}
	}

	/**
	 * Determines the data for the BACE FixedStep and VariableInput delay block
	 * ports
	 */
	public static LabelLayoutData obtainBaceFixedVariableDelayPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return null;
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "ln");
		case "2":
			if (port.getBlock().getParameter(SimulinkConstants.PARAM_SOURCE_TYPE).contains("Variable")) {
				return positionPortLabel(port, "n");
			}
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/**
	 * Determines the data for the BACE Timer and TimerRetriggered block ports
	 */
	public static LabelLayoutData obtainBaceTimerPortData(SimulinkPortBase port) {
		String label = getBaceCounterTimerPortLabel(port);
		if (label == null) {
			if (port.getIndex().equals("4")) {
				return positionPortLabel(port, "dt");
			}
			return positionPortLabel(port, "i", Color.BLUE);
		}
		return positionPortLabel(port, label);
	}

	/** Determines the data for the BACE CountDown and Counter block ports */
	public static LabelLayoutData obtainBaceCounterPortData(SimulinkPortBase port) {
		String label = getBaceCounterTimerPortLabel(port);
		if (label == null) {
			return positionPortLabel(port, "i", Color.BLUE);
		}
		return positionPortLabel(port, label);
	}

	/** Determines the data for the BACE Counter and Timer block ports */
	public static String getBaceCounterTimerPortLabel(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return "out";
		}
		switch (port.getIndex()) {
		case "1":
			return "Enable";
		case "2":
			return "Reset";
		case "3":
			return "IV";
		default:
			return null;
		}
	}

	/** Determines the data for the Delay block ports */
	public static LabelLayoutData obtainDelayBlockPortData(SimulinkPortBase port) {
		if (port.getBlock().getInPorts().size() == 1 || port instanceof SimulinkOutPort) {
			return null;
		}
		String inputPortMap = port.getBlock().getParameter(SimulinkConstants.PARAM_INPUT_PORT_MAP);
		return retrieveDelayPortLabel(port, inputPortMap.split(",")[Integer.parseInt(port.getIndex()) - 1]);
	}

	/**
	 * Decides the data for the Delay block InPort label depending on the data
	 * retrieved from the block's inputPortMap.
	 */
	public static LabelLayoutData retrieveDelayPortLabel(SimulinkPortBase port, String inputPortLabel) {
		switch (inputPortLabel) {
		case "u0":
			return positionPortLabel(port, "u");
		case "p1":
			return positionPortLabel(port, "d");
		case "p4":
			return positionPortLabel(port, "x0");
		case "e6":
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.ICON_FOR_ENABLE_PORT);
		case "r5":
			return getExternalResetIcon(port);
		default:
			return null;
		}
	}

	/** Returns the icon corresponding to the ExternalReset value */
	public static LabelLayoutData getExternalResetIcon(SimulinkPortBase port) {
		String resetParameter = port.getBlock().getParameter(SimulinkConstants.PARAM_EXTERNAL_RESET);
		if (resetParameter == null) {
			return null;
		}
		return extractExternalResetIcon(resetParameter.toLowerCase(), port);
	}

	/**
	 * Helper method to determine the icon corresponding to the ExternalReset value.
	 */
	private static LabelLayoutData extractExternalResetIcon(String resetParameter, SimulinkPortBase port) {
		if (resetParameter == null) {
			return null;
		}

		switch (resetParameter) {
		case SimulinkConstants.VALUE_RISING:
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_RISING);
		case SimulinkConstants.VALUE_FALLING:
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_FALLING);
		case SimulinkConstants.VALUE_EITHER:
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_EITHER);
		case SimulinkConstants.VALUE_RESET_LEVEL:
			if (SimulinkConstants.TYPE_DELAY.equals(port.getBlock().getType())
					|| SimulinkConstants.TYPE_DISCRETE_FILTER.equals(port.getBlock().getType())
					|| SimulinkConstants.TYPE_DISCRETE_TRANSFER_FCN.equals(port.getBlock().getType())) {
				return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_LEVEL_DELAY);
			}
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_LEVEL);
		case SimulinkConstants.VALUE_RESET_LEVEL_HOLD:
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.RESET_LEVEL_HOLD);
		default:
			return null;
		}
	}

	/** Determines the data for the Assignment block ports */
	public static LabelLayoutData obtainAssignmentPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "Y");
		}
		String indexOptions = port.getBlock().getParameter(SimulinkConstants.PARAM_ASSIGNMENT_PORT_INDICES);
		boolean inputInitialize = StringUtils
				.emptyIfNull(port.getBlock().getParameter(SimulinkConstants.PARAM_OUTPUT_INITIALIZE)).contains("port");
		int index = Integer.parseInt(port.getIndex());
		if (index == 1) {
			if (inputInitialize) {
				return positionPortLabel(port, "Y0");
			}
			return positionPortLabel(port, "U");
		}
		if (index == 2 && inputInitialize) {
			return positionPortLabel(port, "U");
		}
		int subtractionValue = 2;
		if (inputInitialize) {
			subtractionValue = 3;
		}
		int base = 0;
		String indexMode = port.getBlock().getParameter(SimulinkConstants.PARAM_INDEX_MODE);
		if (SimulinkConstants.VALUE_ONE_BASED.equals(indexMode)) {
			base = 1;
		}
		String indexOnPort = indexOptions.split(" ")[index - subtractionValue];
		return positionPortLabel(port, "ldx" + indexOnPort + "\n" + base);
	}

	/** Determines the data for the Selector block ports */
	public static LabelLayoutData obtainSelectorPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "Y");
		}
		String indexOptions = port.getBlock().getParameter(SimulinkConstants.PARAM_ASSIGNMENT_PORT_INDICES);
		int index = Integer.parseInt(port.getIndex());
		if (index == 1) {
			return positionPortLabel(port, "U");
		}
		int subtractionValue = 2;
		int base = 0;
		String indexMode = port.getBlock().getParameter(SimulinkConstants.PARAM_INDEX_MODE);
		if (SimulinkConstants.VALUE_ONE_BASED.equals(indexMode)) {
			base = 1;
		}
		String indexOnPort = indexOptions.split(" ")[index - subtractionValue];
		return positionPortLabel(port, "ldx" + indexOnPort + "\n" + base);
	}

	/** Determines the data for the DiscretePulseGenerator block ports */
	public static LabelLayoutData obtainDiscretePulseGeneratorPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "t");
		}
		return null;
	}

	/** Determines the data for the Product block ports */
	public static LabelLayoutData obtainProductPortData(SimulinkPortBase port) {
		if (!(port instanceof SimulinkInPort)) {
			return null;
		}
		String inputs = port.getBlock().getParameter(SimulinkConstants.PARAM_INPUTS);
		try {
			Integer.parseInt(inputs);
		} catch (NumberFormatException e) {
			String multiplicationType = port.getBlock().getParameter(SimulinkConstants.PARAM_MULTIPLICATION);
			return getProductPortLabel(port, inputs, multiplicationType);
		}
		return null;
	}

	/**
	 *
	 */
	public static LabelLayoutData getProductPortLabel(SimulinkPortBase port, String inputs, String multiplicationType) {
		char c = inputs.charAt(Integer.parseInt(port.getIndex()) - 1);
		if (SimulinkConstants.VALUE_MATRIX_MULTIPLICATION.equals(multiplicationType)) {
			if (c == '/') {
				return positionPortLabel(port, "Inv");
			}
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.ICON_FOR_MATRIX_MULTIPLY_PORT);
		}
		if (inputs.length() == 1 || !inputs.contains("/")) {
			return null;
		}
		if (c == '/') {
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.ICON_FOR_ELEMENT_DIVIDE_PORT);
		}
		return positionPortImage(port, SimulinkLabelOnBlockDataConstants.ICON_FOR_ELEMENT_MULTIPLY_PORT);
	}

	/** Determines the data for the Reshape block ports */
	public static LabelLayoutData obtainReshapePortData(SimulinkPortBase port) {
		String dimensionality = port.getBlock().getParameter(SimulinkConstants.PARAM_OUTPUT_DIMENSIONALITY);
		if (SimulinkConstants.VALUE_DERIVE_FROM_REF.equals(dimensionality)) {
			if (port instanceof SimulinkOutPort) {
				return positionPortLabel(port, "Y");
			}
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "U");
			}
			return positionPortLabel(port, "Ref");
		}
		return null;
	}

	/** Determines the data for the RealImagToComplex block ports */
	public static LabelLayoutData obtainRealImagToComplexPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort && SimulinkConstants.VALUE_REAL_AND_IMAG
				.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_INPUT))) {
			return setRealImagPortData(port);
		}
		return null;
	}

	/** Determines the data for the ComplexToRealImag block ports */
	public static LabelLayoutData obtainComplexToRealImagPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort && SimulinkConstants.VALUE_REAL_AND_IMAG
				.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_OUTPUT))) {
			return setRealImagPortData(port);
		}
		return null;
	}

	/**
	 * Sets the port labels for the RealImagToComplex and ComplexToRealImag blocks
	 * according to the port index.
	 */
	public static LabelLayoutData setRealImagPortData(SimulinkPortBase port) {
		if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return positionPortLabel(port, "Re");
		}
		return positionPortLabel(port, "Im");
	}

	/** Determines the data for the Sin block ports */
	public static LabelLayoutData obtainSinPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "t");
		}
		return null;
	}

	/** Determines the data for the MinMaxRunningResettable block ports */
	public static LabelLayoutData obtainMinMaxResettablePortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return positionPortLabel(port, "u");
		}
		return positionPortLabel(port, "R");
	}

	/** Determines the data for the AlgebraicConstraint block ports */
	public static LabelLayoutData obtainAlgebraicConstraintPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "f(z)");
		}
		return positionPortLabel(port, "z");
	}

	/** Determines the data for the PreLookup block ports */
	public static LabelLayoutData obtainPreLookupPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			if (SimulinkConstants.VALUE_ON
					.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_OUTPUT_ONLY_INDEX))) {
				return null;
			}
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "k");
			}
			return positionPortLabel(port, "f");
		}
		if (!SimulinkConstants.VALUE_DIALOG
				.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_BREAK_POINTS_DATA_SRC))) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "u");
			}
			return positionPortLabel(port, "bp");
		}
		return null;

	}

	/** Determines the data for the LookupNDDirect block ports */
	public static LabelLayoutData obtainLookupNDPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort && Integer.parseInt(port.getIndex()) == port.getBlock().getInPorts().size()
				&& SimulinkConstants.VALUE_ON
						.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_TABLE_IS_INPUT))) {
			return positionPortLabel(port, "T");
		}
		return null;
	}

	/** Determines the data for the WhileIterator block ports */
	public static LabelLayoutData obtainWhileIteratorPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "cond");
			}
			return positionPortLabel(port, "IC");
		}
		return null;
	}

	/** Determines the data for the ForIterator block ports */
	public static LabelLayoutData obtainForIteratorPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			String indexBase = port.getBlock().getParameter(SimulinkConstants.PARAM_INDEX_MODE);
			if (SimulinkConstants.VALUE_ONE_BASED.equals(indexBase)) {
				return positionPortLabel(port, "1 : N ");
			}
			return positionPortLabel(port, "0 : N-1 ");
		}
		String iterationSrc = port.getBlock().getParameter(SimulinkConstants.PARAM_ITERATION_SOURCE);
		if (SimulinkConstants.VALUE_EXTERNAL.equals(iterationSrc)
				&& port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return positionPortLabel(port, "N");
		}
		return positionPortLabel(port, "Next_i");
	}

	/** Determines the data for the MultiPortSwitch block ports */
	public static LabelLayoutData obtainMultiPortSwitchPortData(SimulinkPortBase port) {
		int portIndex = Integer.parseInt(port.getIndex());
		if (port instanceof SimulinkOutPort || portIndex == 1) {
			return null;
		}
		String dataPortOrder = port.getBlock().getParameter(SimulinkConstants.PARAM_DATA_PORT_ORDER);
		String portLabel = extractMultiPortSwitchPortLabelText(dataPortOrder, portIndex, port);
		if (StringUtils.isEmpty(portLabel)) {
			return null;
		}

		return extractMultiPortSwitchPortLabelLayout(portIndex, port, portLabel);
	}

	/**
	 * Helper method which returns the port label text for a given port in a
	 * MultiPortSwitchPort block
	 */
	private static LabelLayoutData extractMultiPortSwitchPortLabelLayout(int portIndex, SimulinkPortBase port,
			String portLabelText) {
		LabelLayoutData portTextLabelLayout;
		if (portIndex == port.getBlock().getInPorts().size() && portIndex != 2) {
			if (SimulinkConstants.VALUE_ADDITIONAL_DATA_PORT
					.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_DATA_PORT_FOR_DEFAULT))) {
				portTextLabelLayout = positionPortLabel(port, "*");
			} else {
				portTextLabelLayout = positionPortLabel(port, "*, " + portLabelText);
			}
		} else {
			portTextLabelLayout = positionPortLabel(port, portLabelText);
		}
		portTextLabelLayout.getPosition().y -= 10;
		return portTextLabelLayout;
	}

	/**
	 * Helper method which returns the port label layout for a given port in a
	 * MultiPortSwitchPort block
	 */
	private static String extractMultiPortSwitchPortLabelText(String dataPortOrder, int portIndex,
			SimulinkPortBase port) {
		if (SimulinkConstants.VALUE_DATA_PORT_ZERO_BASED.equals(dataPortOrder)) {
			return "" + (portIndex - 2);
		} else if (SimulinkConstants.VALUE_DATA_PORT_ONE_BASED.equals(dataPortOrder)) {
			return "" + (portIndex - 1);
		} else {
			String dataPortIndices = port.getBlock().getParameter(SimulinkConstants.PARAM_DATA_PORT_INDICES_STRING);
			if (StringUtils.isEmpty(dataPortIndices)) {
				return "";
			}
			return dataPortIndices.split(",")[portIndex - 2].trim().replaceAll("\\s+", ", ");
		}
	}

	/** Determines the data for the Reference block ports */
	public static LabelLayoutData obtainReferencePortData(SimulinkPortBase port) {
		String sourceType = port.getBlock().getParameter(SimulinkConstants.PARAM_SOURCE_TYPE);
		String sourceBlock = port.getBlock().getParameter(SimulinkConstants.PARAM_SOURCE_BLOCK);
		if (sourceType.contains("Unit Delay")) {
			return obtainUnitDelayBlocksPortData(port, sourceType);
		} else if (sourceType.contains(SimulinkConstants.TYPE_BACE_TURN_ON_DELAY)
				|| sourceType.contains(SimulinkConstants.TYPE_BACE_TURN_OFF_DELAY)) {
			return obtainBaceTurnOnOffDelayPortData(port, sourceType);
		} else if (sourceBlock.contains(SimulinkConstants.VALUE_BACE_DEBOUNCE)) {
			return obtainBaceDebouncePortData(port);
		} else if (sourceBlock.contains(SimulinkConstants.VALUE_BACE_UNIT_DELAY)) {
			return obtainBaceUnitDelayIndexPortData(port, sourceBlock);
		} else if (sourceBlock.contains("BACE") && (sourceBlock.contains("Mapping") || sourceBlock.contains("Unmap"))) {
			return obtainBaceMappingUnmapPortData(port, sourceBlock);
		} else if (sourceBlock.contains("putbit") || sourceBlock.contains("getbit") || sourceBlock.contains("setbit")) {
			return obtainBaceGetBitOrPutBitPortData(port, sourceBlock);
		} else if (sourceBlock.contains("BACE")
				&& (sourceBlock.contains("ArraySum") || sourceBlock.contains("ArrayMean"))) {
			return obtainBaceArraySumOrMeanPortData(port, sourceBlock);
		} else if (sourceBlock.contains("BACE") && sourceBlock.contains("Integrator")
				&& !sourceBlock.contains("BACE_DiscreteTimeIntegrator")) {
			return obtainBaceIntegratorPortData(port, sourceBlock);
		} else if (sourceBlock.contains("BACE") && sourceType.contains(SimulinkConstants.TYPE_DATA_TYPE_PROPAGATION)) {
			return obtainDataTypePropagationPortData(port, sourceType);
		} else if (sourceBlock.contains("BACE_PIDControl")) {
			return obtainBacePidControlPortData(port);
		}
		return null;
	}

	/** Returns the port labels for the BACE Matlab Function block */
	public static LabelLayoutData obtainBaceMatlabFnPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return SimulinkPortLabelUtils.positionPortLabel(port, "u");
		}
		return SimulinkPortLabelUtils.positionPortLabel(port, "y");
	}

	/** Determines the data for the BACE PID control block ports */
	private static LabelLayoutData obtainBacePidControlPortData(SimulinkPortBase port) {
		int portIndex = Integer.parseInt(port.getIndex());
		if (port instanceof SimulinkOutPort) {
			return obtainBacePidControlOutPortData(portIndex, port);
		}
		return obtainBacePidControlInPortData(portIndex, port);
	}

	/** Determines the data for the BACE PID control block InPorts */
	private static LabelLayoutData obtainBacePidControlInPortData(int portIndex, SimulinkPortBase port) {
		switch (portIndex) {
		case 1:
			return positionPortLabel(port, "IN");
		case 2:
			return positionPortLabel(port, "P", Color.RED);
		case 3:
			return positionPortLabel(port, "I", Color.RED);
		case 4:
			return positionPortLabel(port, "D", Color.RED);
		case 5:
			return positionPortLabel(port, "I_min", Color.BLUE);
		case 6:
			return positionPortLabel(port, "I_max", Color.BLUE);
		case 7:
			return positionPortLabel(port, "OUT_min", Color.BLUE);
		case 8:
			return positionPortLabel(port, "OUT_max", Color.BLUE);
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/** Determines the data for the BACE PID control block OutPorts */
	private static LabelLayoutData obtainBacePidControlOutPortData(int portIndex, SimulinkPortBase port) {
		switch (portIndex) {
		case 1:
			return positionPortLabel(port, "OUT");
		case 2:
			return positionPortLabel(port, "p", Color.RED);
		case 3:
			return positionPortLabel(port, "i", Color.RED);
		default:
			return positionPortLabel(port, "d", Color.RED);
		}
	}

	/** Determines the data for the Integrator BACE blocks' ports */
	private static LabelLayoutData obtainBaceIntegratorPortData(SimulinkPortBase port, String sourceBlock) {
		int portIndex = Integer.parseInt(port.getIndex());
		if (port instanceof SimulinkOutPort) {
			if (portIndex == 1) {
				return positionPortLabel(port, "y");
			}
			return positionPortLabel(port, "state", Color.RED);
		}
		boolean isStandardOrSimpleIntegrator = sourceBlock.equals(SimulinkConstants.TYPE_BACE_INTEGRATOR_ASAM)
				|| sourceBlock.equals(SimulinkConstants.TYPE_BACE_INTEGRATOR_ASAM_SIMPLE);
		if (!isStandardOrSimpleIntegrator && portIndex >= 3) {
			portIndex += 2;
		}
		return obtainBaceIntegratorInPortData(port, portIndex, sourceBlock);
	}

	/** Determines the data for the Integrator BACE blocks' InPorts */
	private static LabelLayoutData obtainBaceIntegratorInPortData(SimulinkPortBase port, int portIndex,
			String sourceBlock) {
		switch (portIndex) {
		case 1:
			return positionPortLabel(port, "MX");
		case 2:
			return positionPortLabel(port, "Mn");
		case 3:
			return positionPortLabel(port, "IV");
		case 4:
			return positionPortLabel(port, "R");
		case 5:
			return positionPortLabel(port, "E");
		case 6:
			return positionPortLabel(port, "K");
		case 7:
			return positionPortLabel(port, "u");
		case 8:
			return positionPortLabel(port, "dT");
		case 9:
			if (sourceBlock.equals(SimulinkConstants.TYPE_BACE_INTEGRATOR_ASAM_PAR)) {
				return positionPortLabel(port, "R");
			}
			if (sourceBlock.equals(SimulinkConstants.TYPE_BACE_INTEGRATOR_ASAM_EXT)) {
				return positionPortLabel(port, "state", Color.RED);
			}
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/** Determines the data for the Fixpoint BACE block ports */
	public static LabelLayoutData obtainFixPointPortLabel(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			LabelLayoutData portLabel = positionPortLabel(port, "fp", Color.RED);
			portLabel.getPosition().translate(-3, -15);
			return portLabel;
		}
		return null;
	}

	/** Determines the data for the ArraySum or ArrayMean BACE block ports */
	private static LabelLayoutData obtainBaceArraySumOrMeanPortData(SimulinkPortBase port, String sourceBlock) {
		if (port instanceof SimulinkInPort) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "Array");
			}
			return positionPortLabel(port, "Width");
		}
		return positionPortLabel(port, sourceBlock.split("/")[1].split("Array")[1]);
	}

	/** Determines the data for the GetBit, SetBit or PutBit BACE block ports */
	private static LabelLayoutData obtainBaceGetBitOrPutBitPortData(SimulinkPortBase port, String sourceBlock) {
		if (port instanceof SimulinkInPort) {
			switch (port.getIndex()) {
			case "1":
				return positionPortLabel(port, "in");
			case "2":
				return positionPortLabel(port, "pos", Color.RED);
			default:
				return positionPortLabel(port, "bed", Color.RED);
			}
		}
		if (sourceBlock.contains("putbit") || sourceBlock.contains("setbit")) {
			return positionPortLabel(port, "out");
		}
		if (SimulinkConstants.VALUE_OFF.equals(port.getBlock().getParameter("useMask"))) {
			return positionPortLabel(port, "bed");
		}
		String text = port.getBlock().getParameter("bitIdxRange").substring(1, 4).replaceAll("\\s", "-");
		return positionPortLabel(port, "Out " + text);
	}

	/** Determines the data for the BitShift BACE block ports */
	public static LabelLayoutData obtainBaceBitShiftPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "input");
			}
			return positionPortLabel(port, "shift", Color.RED);
		}
		return positionPortLabel(port, "output");
	}

	/** Determines the data for the Mapping and Unmap BACE block ports */
	private static LabelLayoutData obtainBaceMappingUnmapPortData(SimulinkPortBase port, String sourceBlock) {
		String[] blockType = sourceBlock.split("/")[1].split("_");
		if (blockType[1].equals("Mapping")) {
			return extractBaceMappingUnmapPortLabels(port, SimulinkOutPort.class, blockType[0], "");
		}
		return extractBaceMappingUnmapPortLabels(port, SimulinkInPort.class, blockType[0], " In");
	}

	/**
	 * Extracts the port label to be rendered on a Mapping or Unmap BACE block
	 */
	private static LabelLayoutData extractBaceMappingUnmapPortLabels(SimulinkPortBase port, Class<?> base, String uint,
			String in) {
		if (base.isAssignableFrom(port.getClass())) {
			return positionPortLabel(port, uint + in);
		}
		int index = Integer.parseInt(port.getIndex()) - 1;
		return positionPortLabel(port, "bit" + index);
	}

	/** Determines the port label data for UnitDelayIndex BACE blocks */
	public static LabelLayoutData obtainBaceUnitDelayIndexPortData(SimulinkPortBase port, String sourceBlock) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "u");
		case "2":
			if (sourceBlock.contains("External")) {
				return positionPortLabel(port, "IC");
			}
		default:
			return positionPortLabel(port, "Index");
		}
	}

	/** Determines the port label data for Debounce BACE block */
	public static LabelLayoutData obtainBaceDebouncePortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "Outdbncsignal");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "Inrawsignal");
		case "2":
			return positionPortLabel(port, "Indbnccycles");
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/** Determines the port label data for MovingAverage BACE blocks */
	public static LabelLayoutData obtainBaceMovingAveragePortData(SimulinkPortBase port, String sourceType) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "out");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "in");
		case "2":
			if (sourceType.contains("variable")) {
				return positionPortLabel(port, "n");
			}
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}

	}

	/**
	 * Determines the port label data for TurnOn and TurnOff Delay BACE blocks
	 */
	public static LabelLayoutData obtainBaceTurnOnOffDelayPortData(SimulinkPortBase port, String sourceType) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "out_B");
		}
		String index = port.getIndex();
		switch (index) {
		case "1":
			return positionPortLabel(port, "T", Color.BLUE);
		case "2":
			return positionPortLabel(port, "in_B");
		case "3":
			return positionPortLabel(port, "dt");
		case "4":
			if (sourceType.contains("Resettable")) {
				return positionPortLabel(port, "Reset");
			}
		default:
			return positionPortLabel(port, "i", Color.BLUE);
		}
	}

	/** Determines the port label data for DataTypePropagation block */
	public static LabelLayoutData obtainDataTypePropagationPortData(SimulinkPortBase port, String sourceType) {
		if (port instanceof SimulinkInPort) {
			int portIndex = Integer.parseInt(port.getIndex());
			boolean isTwiceBitsBaceBlock = sourceType.contains("3") || sourceType.contains("4");
			if (portIndex > 1 && isTwiceBitsBaceBlock) {
				portIndex += 1;
			}
			switch (portIndex) {
			case 1:
				if (isTwiceBitsBaceBlock) {
					return positionPortLabel(port, "Ref");
				}
				return positionPortLabel(port, "Ref1");
			case 2:
				return positionPortLabel(port, "Ref2");
			case 3:
				return positionPortLabel(port, "Prop");
			default:
				return null;
			}
		}
		return null;
	}

	/** Determines the port label data for DataTypeConversionInherited block */
	public static LabelLayoutData obtainConversionInheritedPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		if (port.getIndex().equals("2")) {
			return positionPortLabel(port, "u");
		}
		return null;
	}

	/** Determines the port label data for any UnitDelay block */
	public static LabelLayoutData obtainUnitDelayBlocksPortData(SimulinkPortBase port, String sourceType) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "u");
		case "2":
			if (sourceType.startsWith("Unit Delay Resettable")) {
				return positionPortLabel(port, "R");
			} else if (sourceType.startsWith("Unit Delay External")) {
				return positionPortLabel(port, "IC");
			} else {
				return positionPortLabel(port, "E");
			}
		case "3":
			if (sourceType.endsWith("Resettable") || (sourceType.endsWith("Initial Condition")
					&& Integer.parseInt(port.getIndex()) < port.getBlock().getInPorts().size())) {
				return positionPortLabel(port, "R");
			}
		default:
			return positionPortLabel(port, "IC");
		}
	}

	/** Determines the data for Mux block ports */
	public static LabelLayoutData obtainMuxPortData(SimulinkPortBase port) {
		if (!(port instanceof SimulinkInPort)) {
			return null;
		}
		String displayOption = port.getBlock().getParameter(SimulinkConstants.MUX_DISPLAY_OPTION);
		if (SimulinkConstants.VALUE_SIGNALS.equals(displayOption)) {
			return positionPortLabel(port, "signal" + port.getIndex());
		}
		return null;
	}

	/**
	 * Determines the text to be displayed (if any) on the ArithShift port labels.
	 */
	public static LabelLayoutData obtainArithShiftPortData(SimulinkPortBase port) {
		if (!(port instanceof SimulinkInPort)) {
			return null;
		}
		if (SimulinkConstants.VALUE_AS_INPUT_PORT
				.equals(port.getBlock().getParameter(SimulinkConstants.PARAM_BIT_SHIFT_NUM_SOURCE))) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "u");
			}
			return positionPortLabel(port, "s");
		}
		return null;
	}

	/** Determines the icon to be displayed on the Integrator port label. */
	public static LabelLayoutData obtainIntegratorPortData(SimulinkPortBase port) {
		if (port.getIndex().equals(SimulinkConstants.VALUE_1) || !(port instanceof SimulinkInPort)) {
			return null;
		}
		if (Integer.parseInt(port.getIndex()) == port.getBlock().getInPorts().size()) {
			String src = port.getBlock().getParameter(SimulinkConstants.PARAM_INITIAL_CONDITION_SOURCE);
			if (src != null && SimulinkConstants.VALUE_EXTERNAL.equals(src)) {
				return positionPortImage(port, SimulinkLabelOnBlockDataConstants.EXTERNAL_INITIAL_CONDITION_SOURCE);
			}
		}
		return getExternalResetIcon(port);
	}

	/**
	 * Extracts the port label for a given port from a provided list of port label
	 * options using the port index. E.g. a list {"A", "B"}: the port label for port
	 * with index 1 is A and that of index 2 is B and so forth.
	 * 
	 * @param port
	 *            the given port
	 * @param portLabelOptions
	 *            the available port label options sorted according to the port
	 *            indices of that port's block
	 */
	public static LabelLayoutData extractPortLabelForBlockPort(SimulinkPortBase port, List<String> portLabelOptions) {
		int portIndex = Integer.parseInt(port.getIndex()) - 1;
		return positionPortLabel(port, portLabelOptions.get(portIndex));
	}

	/**
	 * Extracts the port label for a given port from a provided list of InPort and
	 * OutPort label options using the port index.
	 */
	public static LabelLayoutData extractPortLabelForBlockPort(SimulinkPortBase port, List<String> inPortLabelOptions,
			List<String> outPortLabelOptions) {
		if (port instanceof SimulinkOutPort) {
			return extractPortLabelForBlockPort(port, outPortLabelOptions);
		}
		return extractPortLabelForBlockPort(port, inPortLabelOptions);
	}

	/** Returns the port label for a switch case block. */
	public static LabelLayoutData obtainSwitchCasePortLabelData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "u" + port.getIndex());
		}
		String label = "default:";
		String caseConditions = port.getBlock().getParameter(SimulinkConstants.PARAM_PROCESSED_CASE_CONDITIONS);
		caseConditions = caseConditions.replaceAll("[{}]", StringUtils.EMPTY_STRING);

		String[] caseParts = caseConditions.split(",");
		int index = Integer.parseInt(port.getIndex());
		if (index <= caseParts.length) {
			label = "case [ " + caseParts[index - 1].trim() + " ]:";
		}

		return positionOutPortLabelForSwitchCaseBlock(port, label,
				port.getBlock().obtainLayoutData().getPosition().width);

	}

	/** Returns the port label for an if block. */
	public static LabelLayoutData obtainIfPortLabelData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "u" + port.getIndex());
		}

		int index = Integer.parseInt(port.getIndex());
		String label = "else";
		if (index == 1) {
			label = "if(" + port.getBlock().getParameter(SimulinkConstants.PARAM_IF_EXPRESSION) + ")";
		} else {
			String elseIfExpressions = port.getBlock().getParameter(SimulinkConstants.PARAM_ELSE_IF_EXPRESSIONS);
			if (elseIfExpressions != null) {
				String[] elseIfs = elseIfExpressions.split(",");
				if (index - 2 < elseIfs.length) {
					label = "elseif(" + elseIfs[index - 2] + ")";
				}
			}
		}

		return positionPortLabel(port, label);
	}

	/**
	 * Returns label data for a subsystem's port or null if no label is used for
	 * this port.
	 */
	public static LabelLayoutData obtainSubSystemPortLabelData(SimulinkPortBase port) {
		String showPortLabels = port.getBlock().getParameter(SimulinkConstants.PARAM_SHOW_PORT_LABELS);

		boolean visible = !SimulinkConstants.VALUE_NONE.equals(showPortLabels);

		EOrientation orientation = port.getBlock().obtainLayoutData().getOrientation();
		if (orientation.isRotated()) {
			// top-down labels are not yet supported and thus are hidden
			visible = false;
		}

		boolean isInport = port instanceof SimulinkInPort;
		if (isInport && ((SimulinkInPort) port).isSpecialPort()) {
			// visibility of special ports only depends on rotation
			return obtainSpecialPortLabel((SimulinkInPort) port, orientation, !orientation.isRotated());
		}

		SimulinkBlock portBlock = findPortBlock(port.getBlock(), port.getIndex(), isInport);
		if (portBlock == null || portBlock.getName() == null) {
			return null;
		}

		String text = portBlock.getName();
		Color overrideColor = null;
		if (visible && SimulinkConstants.VALUE_FROM_PORT_ICON.equals(showPortLabels)) {
			if (SimulinkConstants.VALUE_SIGNAL_NAME
					.equals(portBlock.getParameter(SimulinkConstants.PARAM_ICON_DISPLAY))) {
				// keep visible
			} else if (SimulinkConstants.VALUE_OFF.equals(portBlock.getParameter(SimulinkConstants.PARAM_SHOW_NAME))) {
				visible = false;
			}
		}

		return positionPortLabel(port, text, orientation, visible, overrideColor);
	}

	/** Layouts special ports (action, trigger, etc.). */
	public static LabelLayoutData obtainSpecialPortLabel(SimulinkInPort port, EOrientation orientation,
			boolean visible) {
		if (port.isEnablePort()) {
			return positionPortImage(port, SimulinkLabelOnBlockDataConstants.ICON_FOR_ENABLE_PORT);
		}
		if (port.isResetPort()) {
			SimulinkBlock resetPortBlock = port.getBlock().hasSubBlockOfType(SimulinkConstants.TYPE_RESET_PORT_BLOCK);
			if (resetPortBlock == null) {
				return null;
			}
			String resetPortIcon = SimulinkLabelOnBlockData.obtainResetPortBlockIcon(resetPortBlock);
			if (resetPortIcon != null) {
				return positionPortImage(port, resetPortIcon);
			}
			return null;
		}
		String labelText;
		if (port.isActionPort()) {
			labelText = determineSpecialActionPortLabel(port);
			return layoutSpecialPortLabel(port, labelText, orientation, visible);
		}
		if (port.isTriggerPort()) {
			labelText = determineTriggerPortLabel(port);
			LabelLayoutData iconLabel = extractExternalResetIcon(labelText, port);
			if (iconLabel != null) {
				return iconLabel;
			}
			return layoutSpecialPortLabel(port, labelText, orientation, visible);
		}

		return null;
	}

	/** Returns the port label for Action ports. */
	public static String determineSpecialActionPortLabel(SimulinkInPort port) {
		if (port.getLine() != null && port.getLine().getSrcPort() != null) {
			LabelLayoutData srcPort = extractPortLabelData(port.getLine().getSrcPort());
			if (srcPort != null && !srcPort.getText().toLowerCase().contains(SimulinkConstants.TYPE_OUT)) {
				return srcPort.getText().replaceAll(" *[\\[(].*[)\\]]", StringUtils.EMPTY_STRING) + " {}";
			}
		}
		return "Action";
	}

	/** Returns the port label for Trigger ports. */
	public static String determineTriggerPortLabel(SimulinkInPort port) {
		SimulinkBlock triggerPortBlock = port.getBlock().hasSubBlockOfType(SimulinkConstants.TYPE_TRIGGER_PORT);

		String procName = triggerPortBlock.getParameter(SimulinkConstants.PARAM_PROC_NAME);
		if (procName != null) {
			return SimulinkUtils.replaceSimulinkLineBreaks(procName).trim() + "()";
		}

		String triggerType = triggerPortBlock.getParameter(SimulinkConstants.PARAM_TRIGGER_TYPE);
		if (SimulinkConstants.VALUE_FUNCTION_CALL.equals(triggerType)) {
			String functionName = triggerPortBlock.getName();
			if (SimulinkConstants.VALUE_ON
					.equals(triggerPortBlock.getParameter(SimulinkConstants.PARAM_IS_SIMULINK_FUNCTION))
					&& !StringUtils.isEmpty(triggerPortBlock.getParameter(SimulinkConstants.PARAM_FUNCTION_NAME))) {
				functionName = triggerPortBlock.getParameter(SimulinkConstants.PARAM_FUNCTION_NAME);
			}
			return SimulinkUtils.replaceSimulinkLineBreaks(functionName).trim() + "()";
		}
		return triggerType;
	}

	/** Returns port label layout data for the given special port. */
	public static LabelLayoutData layoutSpecialPortLabel(SimulinkInPort port, String text, EOrientation orientation,
			boolean visible) {
		Rectangle2D textBounds = LayoutHandlerBase.getTextBounds(text, PORT_LABEL_FONT_DATA.getAwtFont());
		PortLayoutData portLayoutData = port.obtainLayoutData();
		Point position = new Point(portLayoutData.getPosition());
		position.x -= textBounds.getWidth() / 2;

		if (orientation == EOrientation.LEFT) {
			position.y -= textBounds.getHeight() + SimulinkLayoutHandler.LABEL_DISTANCE;
		} else {
			position.y += SimulinkLayoutHandler.LABEL_DISTANCE;
		}

		return new LabelLayoutData(text, visible, PORT_LABEL_FONT_DATA, position, portLayoutData.getColor(),
				portLayoutData.getOpacity());
	}

	/**
	 * Returns port label for the given port and icon.
	 * 
	 * @param port
	 *            the given port
	 * @param imageFileIcon
	 *            the icon file name
	 * @return LabelLayoutData for the given port label.
	 */
	public static LabelLayoutData positionPortImage(SimulinkPortBase port, String imageFileIcon) {
		EOrientation orientation = EOrientation.getOrientationFromDirection(port.obtainLayoutData().getDirection());
		PortLayoutData portLayoutData = port.obtainLayoutData();
		Point position = new Point(portLayoutData.getPosition());

		if (orientation == EOrientation.RIGHT || orientation == EOrientation.LEFT) {
			position.y -= SimulinkLayoutHandler.LABEL_DISTANCE;
			position.x = adjustPortImagePosition(port, orientation, EOrientation.LEFT, position.x);
		} else {
			position.x -= SimulinkLayoutHandler.LABEL_DISTANCE;
			position.y = adjustPortImagePosition(port, orientation, EOrientation.UP, position.y);
		}
		return new LabelLayoutData("", imageFileIcon, PORT_LABEL_FONT_DATA, position, portLayoutData.getColor(),
				portLayoutData.getOpacity(), !orientation.isRotated());
	}

	/**
	 * Adjusts the layout of the port label containing an image depending on the
	 * port's orientation.
	 * 
	 * @param port
	 *            the given port.
	 * @param portOrientation
	 *            the port's orientation.
	 * @param comparableOrientation
	 *            the orientation to which the port's orientation will be compared
	 *            to.
	 * @param coordinate
	 *            the position coordinate to be adjusted (either X or Y coordinate
	 *            depending on the case).
	 */
	private static int adjustPortImagePosition(SimulinkPortBase port, EOrientation portOrientation,
			EOrientation comparableOrientation, int coordinate) {
		if ((port instanceof SimulinkInPort) ^ (portOrientation == comparableOrientation)) {
			coordinate += SimulinkLayoutHandler.LABEL_DISTANCE;
		} else {
			coordinate -= SimulinkLayoutHandler.LABEL_DISTANCE + PORT_LABEL_ICON_WIDTH;
		}
		return coordinate;
	}

	/** Returns a port label for the given port and text. */
	public static LabelLayoutData positionPortLabel(SimulinkPortBase port, String string) {
		EOrientation orientation = port.getBlock().obtainLayoutData().getOrientation();
		return positionPortLabel(port, string, orientation, !orientation.isRotated(), null);
	}

	/** Returns a port label for the given port, text and color. */
	public static LabelLayoutData positionPortLabel(SimulinkPortBase port, String string, Color color) {
		EOrientation orientation = port.getBlock().obtainLayoutData().getOrientation();
		return positionPortLabel(port, string, orientation, !orientation.isRotated(), color);
	}

	/** Returns a port label for the given port and text. */
	public static LabelLayoutData positionPortLabel(SimulinkPortBase port, String text, EOrientation orientation,
			boolean visible, Color color) {
		Rectangle2D textBounds = LayoutHandlerBase.getTextBounds(text, PORT_LABEL_FONT_DATA.getAwtFont());

		PortLayoutData portLayoutData = port.obtainLayoutData();
		Point position = new Point(portLayoutData.getPosition());
		position.y -= textBounds.getHeight() / 2;

		if ((port instanceof SimulinkInPort) ^ (orientation == EOrientation.LEFT)) {
			position.x += SimulinkLayoutHandler.LABEL_DISTANCE;
		} else {
			position.x -= SimulinkLayoutHandler.LABEL_DISTANCE + textBounds.getWidth();
		}

		if (color == null) {
			color = portLayoutData.getColor();
		}

		return new LabelLayoutData(text, visible, PORT_LABEL_FONT_DATA, position, color, portLayoutData.getOpacity());
	}

	/**
	 * Returns an OutPort label for the given port and text for the SwitchCase
	 * block. This method shortens the port label, if it is too long (ex. [ 1 2 3 4
	 * 5 6 7 8 ] --> [ 1 2 ... ])
	 */
	public static LabelLayoutData positionOutPortLabelForSwitchCaseBlock(SimulinkPortBase port, String text,
			int blockWidth) {
		EOrientation orientation = port.getBlock().obtainLayoutData().getOrientation();
		Rectangle2D textBounds = LayoutHandlerBase.getTextBounds(text, PORT_LABEL_FONT_DATA.getAwtFont());

		if (textBounds.getWidth() > blockWidth * 0.65) {
			Matcher matcher = Pattern.compile("(\\[\\s[^\\s]+\\s[^\\s]+\\s)(.+)(\\s\\])").matcher(text);
			StringBuffer portLabelBuffer = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(portLabelBuffer, matcher.group(1) + "..." + matcher.group(3));
			}
			matcher.appendTail(portLabelBuffer);
			text = portLabelBuffer.toString();
		}
		return positionPortLabel(port, text, orientation, !orientation.isRotated(), null);
	}

	/**
	 * Finds the block corresponding to a given port. Returns null if not found.
	 */
	public static SimulinkBlock findPortBlock(SimulinkBlock parent, String index, boolean inPort) {
		for (SimulinkBlock block : parent.getSubBlocks()) {
			if (inPort && !SimulinkConstants.TYPE_INPORT.equals(block.getType())) {
				continue;
			}
			if (!inPort && !SimulinkConstants.TYPE_OUTPORT.equals(block.getType())) {
				continue;
			}

			if (index.equals(block.getParameter(SimulinkConstants.PARAM_PORT))) {
				return block;
			}
		}

		return null;
	}

	/** Returns the label for an input port of a rectangular sum block. */
	public static LabelLayoutData obtainRectangularSumInPortLabelData(SimulinkPortBase port) {
		if (!(port instanceof SimulinkInPort)) {
			return null;
		}
		return obtainSumInPortLabelData(port);
	}

	/** Returns the label for an input port of a sum block. */
	public static LabelLayoutData obtainSumInPortLabelData(SimulinkPortBase port) {
		if (!(port instanceof SimulinkInPort)) {
			return null;
		}
		PortLayoutData portLayoutData = port.obtainLayoutData();
		String portsDescription = SimulinkPortLayoutUtils.getSumInputPortsDescription(port.getBlock());
		int index = Integer.parseInt(port.getIndex());
		String text = Character.toString(
				portsDescription.charAt(SimulinkPortLayoutUtils.getLogicalIndexForPort(portsDescription, index)));
		if ("-".equals(text)) {
			// we use a unicode "en dash", as the minus is too short
			text = EN_DASH;
		}

		Point position = SimulinkPortLayoutUtils.getInsetPortPoint(port, port.getBlock().obtainLayoutData(), -5);
		Rectangle bounds = LayoutHandlerBase.determineTextBounds(text, PORT_LABEL_FONT_DATA.getAwtFont());
		position.x -= bounds.width / 2;
		position.y -= bounds.height / 2;

		return new LabelLayoutData(text, true, PORT_LABEL_FONT_DATA, position, portLayoutData.getColor(),
				portLayoutData.getOpacity());
	}

	/** Returns the port labels for BACE BMW_FunctionCallGenerator blocks */
	public static LabelLayoutData obtainBmwFunctionCallGeneratorPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return null;
		}
		String nkw = port.getBlock().getParameter(SimulinkConstants.PARAM_NKW);
		String state = port.getBlock().getParameter(SimulinkConstants.TYPE_STATE);
		if (SimulinkConstants.VALUE_ON.equals(state)) {
			return positionPortLabel(port, SimulinkConstants.TYPE_STATE);
		}
		if (SimulinkConstants.VALUE_ON.equals(nkw)) {
			return positionPortLabel(port, SimulinkConstants.PARAM_NKW);
		}
		return positionPortLabel(port, SimulinkLabelOnBlockDataConstants.TEXT_FOR_BMW_FUNCTION_CALL_GENERATOR);
	}

	/** Returns the port labels for the FunctionCallSplit block */
	public static LabelLayoutData obtainFunctionCallSplitPortLabelData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort && port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return SimulinkPortLabelUtils.positionPortImage(port,
					SimulinkLabelOnBlockDataConstants.ICON_FOR_FUNCTION_CALL_SPLIT);
		}
		return null;
	}

	/** Returns the port labels for the BusAssignment block */
	public static LabelLayoutData obtainBusAssignmentPortData(SimulinkPortBase port) {
		int portIndex = Integer.parseInt(port.getIndex());
		if (portIndex == 1) {
			return positionPortLabel(port, "Bus");
		}
		String[] assignedSignals = port.getBlock().getParameter(SimulinkConstants.PARAM_ASSIGNED_SIGNALS).split(",");
		return positionPortLabel(port, ":= " + assignedSignals[portIndex - 2]);
	}

	/** Returns the port labels for the BACE Edge blocks */
	public static LabelLayoutData obtainBaceEdgePortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort && port.getIndex().equals("2")) {
			return positionPortLabel(port, "i");
		}
		return null;
	}

	/** Returns the port labels for the BACE IfThen block */
	public static LabelLayoutData obtainBaceIfThenPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "if_B");
			}
			return positionPortLabel(port, "Then");
		}
		return null;
	}

	/** Returns the port labels for the BACE SafeFloatCompare block */
	public static LabelLayoutData obtainBaceSafeFloatComparePortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return positionPortLabel(port, "real");
		}
		return positionPortLabel(port, "B");
	}

	/** Returns the port labels for the BACE ArrayMinMax block */
	public static LabelLayoutData obtainBaceArrayMinMaxPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
				return positionPortLabel(port, "Array");
			}
			return positionPortLabel(port, "Width");
		}
		if (port.getBlock().getOutPorts().size() > 1 && port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return positionPortLabel(port, "Index");
		}
		return positionPortLabel(port, "Value");
	}

	/**
	 * Returns the port labels for the DiscreteFilter and DiscreteTransferFcn blocks
	 */
	public static LabelLayoutData obtainDiscreteFilterPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return null;
		}
		int portIndex = Integer.parseInt(port.getIndex());

		String numeratorSource = port.getBlock().getParameter(SimulinkConstants.PARAM_NUMERATOR_SOURCE);
		String denominatorSource = port.getBlock().getParameter(SimulinkConstants.PARAM_DENOMINATOR_SOURCE);
		String initialStatesSource = port.getBlock().getParameter(SimulinkConstants.PARAM_INITIAL_STATES_SOURCE);
		boolean numeratorInputSource = numeratorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT);
		boolean denominatorInputSource = denominatorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT);
		boolean initialStateInputSource = false;
		if (initialStatesSource != null) {
			initialStateInputSource = initialStatesSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT);
		}

		if (portIndex == port.getBlock().getInPorts().size() && initialStateInputSource) {
			return positionPortLabel(port, "x0");
		}

		switch (portIndex) {
		case 1:
			return (numeratorInputSource || denominatorInputSource || initialStateInputSource)
					? positionPortLabel(port, "u")
					: null;
		case 2:
			if (numeratorInputSource) {
				return positionPortLabel(port, "Num");
			}
			if (denominatorInputSource) {
				return positionPortLabel(port, "Den");
			}

		case 3:
			if (denominatorInputSource && numeratorInputSource) {
				return positionPortLabel(port, "Den");
			}
		default:
			return getExternalResetIcon(port);
		}
	}

	/** Returns the port labels for the DiscreteFir block */
	public static LabelLayoutData obtainDiscreteFirPortData(SimulinkPortBase port) {
		String coefficientSource = port.getBlock().getParameter(SimulinkConstants.PARAM_COEFFICIENT_SOURCE);
		if (!coefficientSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)) {
			return null;
		}
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "Out");
		}
		if (port.getIndex().equals(SimulinkConstants.VALUE_1)) {
			return positionPortLabel(port, "In");
		}
		return positionPortLabel(port, "Num");
	}

	/** Returns the port labels for the FunctionCaller block */
	public static LabelLayoutData obtainFunctionCallerPortData(SimulinkPortBase port) {
		int portIndex = Integer.parseInt(port.getIndex()) - 1;
		String parameterName = (port instanceof SimulinkInPort) ? SimulinkConstants.PARAM_FUNCTION_INPUT
				: SimulinkConstants.PARAM_FUNCTION_OUTPUT;
		String[] portLabels = port.getBlock().getParameter(parameterName).split(",");
		return positionPortLabel(port, portLabels[portIndex]);
	}

	/** Returns the port labels for the LookupTableDynamic block */
	public static LabelLayoutData obtainLookupTableDynamicPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return positionPortLabel(port, "y");
		}
		switch (port.getIndex()) {
		case "1":
			return positionPortLabel(port, "x");
		case "2":
			return positionPortLabel(port, "xdat");
		default:
			return positionPortLabel(port, "ydat");
		}
	}

	/** Returns the port labels for the VariantSource blocks. */
	public static LabelLayoutData obtainVariantSourcePortData(SimulinkPortBase port) {
		if (port instanceof SimulinkInPort) {
			return getVariantControlDataForPort(port);
		}
		return null;
	}

	/** Returns the port labels for the VariantSink blocks. */
	public static LabelLayoutData obtainVariantSinkPortData(SimulinkPortBase port) {
		if (port instanceof SimulinkOutPort) {
			return getVariantControlDataForPort(port);
		}
		return null;
	}

	/**
	 * Returns the label for the port using the port index, VariantControls
	 * parameter and the model's mxData.
	 */
	private static LabelLayoutData getVariantControlDataForPort(SimulinkPortBase port) {
		SimulinkBlock variantBlock = port.getBlock();
		String variantControlsMxArrayFileName = variantBlock.getParameter(SimulinkConstants.PARAM_VARIANT_CONTROLS);
		if (variantControlsMxArrayFileName == null) {
			return null;
		}
		String variantControlsArray = variantBlock.getModel().getParameter(variantControlsMxArrayFileName);
		if (variantControlsArray == null) {
			return null;
		}
		String[] portVariants = variantControlsArray.split("@");
		int portIndex = Integer.parseInt(port.getIndex()) - 1;
		if (portIndex >= portVariants.length) {
			return null;
		}
		return positionPortLabel(port, portVariants[portIndex]);
	}

}
