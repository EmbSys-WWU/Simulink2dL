/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id$            
 |                                                                       |
 | Copyright (c)  2009-2015 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.ui;

import java.util.List;
import java.util.Set;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.SimulinkRegistry;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.ESimulinkBlockType;

/**
 * May contain text and icons that are on a simulink block.
 */
public class SimulinkLabelOnBlockData {

	/** Data types that are passed through. */
	private static final Set<String> OUT_DATA_TYPE_PASS_THROUGH = CollectionUtils.asHashSet("double", "single", "int8",
			"uint8", "int16", "uint16", "int32", "uint32", "boolean");

	/** The text (may be empty) */
	private final String text;

	/** The file name of the icon for the block */
	private final String iconFileName;

	/** Constructor. */
	public SimulinkLabelOnBlockData(String text, String iconFileName) {
		this.text = text;
		this.iconFileName = iconFileName;
	}

	/** Initializes text and icons for the label on the given block. */
	public SimulinkLabelOnBlockData(SimulinkBlock block, ESimulinkBlockType type) {
		String blockType = block.getType();
		this.text = getLabelText(block, type, blockType);
		this.iconFileName = getLabelIconFilename(block, type, blockType);
	}

	/** Determines the text on the Simulink Block label */
	private static String getLabelText(SimulinkBlock block, ESimulinkBlockType type, String typeString) {
		String text = SimulinkRegistry.getInstance().getLabelText(typeString);
		if (text != null) {
			return text;
		}
		text = block.getParameter(SimulinkRegistry.getInstance().getLabelParameter(typeString));
		if (text != null) {
			return text;
		}
		return type.getBlockTextLabel(block);
	}

	/** Determines the label text for a ActionPort block */
	public static String determineActionPortData(SimulinkBlock block) {
		String actionType = block.getParameter(SimulinkConstants.PARAM_ACTION_TYPE);
		if ("unset".equals(actionType)) {
			return "Action";
		} else if ("then".equals(actionType)) {
			return "if {}";
		} else {
			return actionType + " {}";
		}
	}

	/** Determines the label text for a DataTypeConversion block */
	public static String obtainDataTypeConversionBlockData(SimulinkBlock block) {
		boolean storedInteger = StringUtils.emptyIfNull(block.getParameter(SimulinkConstants.PARAM_CONVERT_REAL_WORLD))
				.contains(SimulinkConstants.VALUE_STORED_INTEGER);
		String outDataSrc = block.getParameter(SimulinkConstants.PARAM_OUT_DATA_TYPE);
		String result = "";
		if (OUT_DATA_TYPE_PASS_THROUGH.contains(outDataSrc)) {
			result = outDataSrc;
		} else {
			result = "Convert";
		}

		if (storedInteger) {
			result += "\n(SI)";
		}
		return result;
	}

	/** Determines the label text for a PermuteDimensions block */
	public static String obtainPermuteDimensionsBlockData(SimulinkBlock block) {
		String order = block.getParameter(SimulinkConstants.PARAM_ORDER);
		return "P:" + order;
	}

	/** Determines the label text for a TriggerPort block */
	public static String obtainTriggerPortBlockData(SimulinkBlock block) {
		if (SimulinkConstants.VALUE_FUNCTION_CALL.equals(block.getParameter(SimulinkConstants.PARAM_TRIGGER_TYPE))) {
			return SimulinkLabelOnBlockDataConstants.FUNCTION_TEXT;
		}
		return null;
	}

	/** Determines the label text for an Assignment or Selector block */
	public static String obtainAssignmenOrSelectortBlockData(SimulinkBlock block, String inputText) {
		String dimensions = block.getParameter(SimulinkConstants.PARAM_NUMBER_OF_DIMENSIONS);
		String inputPortSize = block.getParameter(SimulinkConstants.PARAM_INPUT_PORT_WIDTH);
		String indices = StringUtils.emptyIfNull(block.getParameter(SimulinkConstants.PARAM_INDICES))
				.replaceAll("\\s+", ",").replaceAll("[\\[\\]]", "");

		if ("1".equals(dimensions) && block.getInPorts().size() == 1 && !"-1".equals(inputPortSize)
				&& indices.matches("[0-9,]+")) {
			int index = 0;
			if (SimulinkConstants.VALUE_ONE_BASED.equals(block.getParameter(SimulinkConstants.PARAM_INDEX_MODE))) {
				index = 1;
			}
			return inputPortSize + "*" + indices + "*" + index;
		}

		return dimensions + "-D\n" + inputText;
	}

	/** Determines the label text for a Reshape block */
	public static String obtainReshapeBlockData(SimulinkBlock block) {
		String dimensionality = block.getParameter(SimulinkConstants.PARAM_OUTPUT_DIMENSIONALITY);
		if (SimulinkConstants.VALUE_1D_ARRAY.equals(dimensionality)) {
			return "U( : )";
		} else if (SimulinkConstants.VALUE_CUSTOMIZE.equals(dimensionality)
				|| SimulinkConstants.VALUE_DERIVE_FROM_REF.equals(dimensionality)) {
			return "Reshape";
		}
		return null;
	}

	/** Determines the label text for a RealImagToComplex block */
	public static String obtainRealImagToComplexBlockData(SimulinkBlock block) {
		String input = block.getParameter(SimulinkConstants.PARAM_INPUT);
		if (SimulinkConstants.VALUE_REAL.equalsIgnoreCase(input)) {
			return "u + j K";
		} else if (SimulinkConstants.VALUE_IMAG.equals(input)) {
			return "K + j u";
		}
		return null;
	}

	/** Determines the label text for a Math block */
	public static String obtainMathBlockData(SimulinkBlock block) {
		String operator = block.getParameter(SimulinkConstants.PARAM_OPERATOR);
		if (SimulinkConstants.VALUE_LOG10.equals(operator) || SimulinkConstants.VALUE_HYPOT.equals(operator)
				|| SimulinkConstants.VALUE_REM.equals(operator) || SimulinkConstants.VALUE_MOD.equals(operator)) {
			return operator;
		} else if (SimulinkConstants.VALUE_LOG.equals(operator)) {
			return "ln";
		}
		return null;
	}

	/** Determines the label text for a ComplexToRealImag block */
	public static String obtainComplexToRealImagBlockData(SimulinkBlock block) {
		String output = block.getParameter(SimulinkConstants.PARAM_OUTPUT);
		if (SimulinkConstants.VALUE_REAL.equalsIgnoreCase(output)) {
			return "Re(u)";
		} else if (SimulinkConstants.VALUE_IMAG.equals(output)) {
			return "Im(u)";
		}
		return StringUtils.EMPTY_STRING;
	}

	/** Determines the label text for a ComplexToMagnitudeAngle block */
	public static String obtainComplexToMagnitudeAngleBlockData(SimulinkBlock block) {
		String output = block.getParameter(SimulinkConstants.PARAM_OUTPUT);
		if (SimulinkConstants.VALUE_MAGNITUDE.equals(output)) {
			return "|u|";
		}
		return StringUtils.EMPTY_STRING;

	}

	/** Determines the label text for a SampleTimeMath block */
	public static String obtainSampleTimeMathBlockData(SimulinkBlock block) {
		String operator = block.getParameter(SimulinkConstants.PARAM_SAMPLE_TIME_MATH_OPERATOR);
		String result = "";
		if (SimulinkConstants.VALUE_TS_ONLY.equals(operator)) {
			result += "Ts";
		} else if (SimulinkConstants.VALUE_1_TS_ONLY.equals(operator)) {
			result += "1/Ts";
		} else {
			result += "u" + operator + "Ts";
		}
		int weight = Integer.parseInt(block.getParameter(SimulinkConstants.PARAM_SAMPLE_TIME_MATH_WEIGHT_VALUE));
		if (weight != 1) {
			if (SimulinkConstants.VALUE_1_TS_ONLY.equals(operator) || "/".equals(operator)) {
				result += "/w";
			} else {
				result += "*w";
			}
		}
		return result;
	}

	/** Determines the label text for a Bias block */
	public static String obtainBiasBlockData(SimulinkBlock block) {
		return "u+" + block.getParameter(SimulinkConstants.TYPE_BIAS);
	}

	/** Determines the label text for a LookupND block */
	public static String obtainLookupNDBlockcData(SimulinkBlock block) {
		String dimensions = block.getParameter(SimulinkConstants.PARAM_TABLE_DIMENSIONS);
		return dimensions + "-D T[k]";
	}

	/** Determines the label text for a WhileIterator block */
	public static String getWhileIteratorBlockData(SimulinkBlock block) {
		String whileBlockType = block.getParameter(SimulinkConstants.PARAM_WHILE_BLOCK_TYPE);
		if (SimulinkConstants.VALUE_DO_WHILE.equals(whileBlockType)) {
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_WHILE_ITERATOR_BLOCK_DO;
		}
		return SimulinkLabelOnBlockDataConstants.TEXT_ON_WHILE_ITERATOR_BLOCK;
	}

	/** Determines the label text for a subsystem */
	public static String getSubSystemBlockData(SimulinkBlock block) {
		if (block.hasSubBlockOfType(SimulinkConstants.TYPE_FOR_ITERATOR) != null) {
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_FOR_ITERATOR_SUBSYSTEM;
		}
		SimulinkBlock whileSubBlock = block.hasSubBlockOfType(SimulinkConstants.TYPE_WHILE_ITERATOR);
		if (whileSubBlock != null) {
			if (SimulinkConstants.VALUE_DO_WHILE
					.equals(whileSubBlock.getParameter(SimulinkConstants.PARAM_WHILE_BLOCK_TYPE))) {
				return SimulinkLabelOnBlockDataConstants.TEXT_ON_WHILE_ITERATOR_SUBSYSTEM_DO;
			}
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_WHILE_ITERATOR_SUBSYSTEM;
		}
		SimulinkBlock eventListenerBlock = block.hasSubBlockOfType(SimulinkConstants.TYPE_EVENT_LISTENER_TYPE);
		if (eventListenerBlock != null) {
			return eventListenerBlock.getParameter(SimulinkConstants.PARAM_EVENT_LISTENER_TYPE);
		}
		return StringUtils.EMPTY_STRING;
	}

	/** Determines the label icon for a subsystem */
	public static String getSubSystemBlockIcon(SimulinkBlock block) {
		if (block.hasSubBlockOfType(SimulinkConstants.TYPE_FOR_EACH) != null) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_EACH_SUBSYSTEM;
		}
		return null;
	}

	/**
	 * Sets the text to long if the format is set to long and short otherwise. The
	 * set text is not used as a label to be displayed on the block, but rather as a
	 * parameter to be used in the drawing of the block in the file DisplayBlock.js
	 */
	public static String getDisplayBlockFormat(SimulinkBlock block) {
		String format = block.getParameter(SimulinkConstants.PARAM_DISPLAY_FORMAT);
		if (SimulinkConstants.VALUE_LONG.equals(format)) {
			return SimulinkConstants.VALUE_LONG;
		}
		return SimulinkConstants.VALUE_SHORT;
	}

	/** Determines data for Mux/Demux block */
	public static String obtainMuxBlockData(SimulinkBlock block) {
		String displayOption = block.getParameter(SimulinkConstants.MUX_DISPLAY_OPTION);
		if (SimulinkConstants.VALUE_NONE.equals(displayOption)) {
			return block.getType();
		}
		return null;
	}

	/** Determine data for Gain block */
	public static String obtainGainBlockData(SimulinkBlock block) {
		String mult = block.getParameter(SimulinkConstants.PARAM_MULTIPLICATION);
		String gain = block.getParameter(SimulinkConstants.PARAM_GAIN);
		if (mult == null) {
			return block.getParameter(SimulinkConstants.PARAM_GAIN.toLowerCase());
		}
		switch (mult) {
		case "Matrix(K*u)":
			return gain + "* u";
		case "Matrix(u*K)":
			return "u *" + gain;
		case "Matrix(K*u) (u vector)":
			return gain + "* uvec";
		default:
			return gain;
		}
	}

	/** Determines data for the ArithShift block */
	public static String obtainArithShiftData(SimulinkBlock block) {
		String direction = block.getParameter(SimulinkConstants.PARAM_BIT_SHIFT_DIRECTION);
		String source = block.getParameter(SimulinkConstants.PARAM_BIT_SHIFT_NUM_SOURCE);
		if (SimulinkConstants.VALUE_AS_INPUT_PORT.equals(source)) {
			switch (direction) {
			case "Left":
				return "Qu<<s";
			case "Right":
				return "Qu>>s";
			default:
				return "Shift";
			}
		}
		String binPoints = block.getParameter(SimulinkConstants.PARAM_BIN_PT_SHIFT_NUM);
		String shiftBits = block.getParameter(SimulinkConstants.PARAM_BIT_SHIFT_NUMBER);
		String shiftSign = "";
		int value = 0;
		if ("Left".equals(direction)) {
			shiftSign = "<<";
			value = Integer.parseInt(binPoints) + Integer.parseInt(shiftBits);
		} else {
			shiftSign = ">>";
			value = Integer.parseInt(binPoints) - Integer.parseInt(shiftBits);
		}
		return "Qy = Qu " + shiftSign + " " + shiftBits + "\nVy = Vu * 2^" + value + "\nEy = Eu + " + binPoints;
	}

	/** Determines data for the SignalSpecification block */
	public static String obtainSignalSpecificationData(SimulinkBlock block) {
		String dataType = block.getParameter(SimulinkConstants.PARAM_OUT_DATA_TYPE);
		String signalType = block.getParameter(SimulinkConstants.PARAM_SIGNAL_TYPE);
		String samplingMode = block.getParameter(SimulinkConstants.PARAM_SAMPLING_MODE);
		return determineSignalSpecificationData(dataType, signalType, samplingMode);
	}

	/** Helper method to determine the data for SignalSpecification blocks. */
	public static String determineSignalSpecificationData(String dataType, String signalType, String samplingMode) {
		String result = "";
		if (StringUtils.emptyIfNull(dataType).contains("Bus")) {
			return "Dt:" + dataType + " BT:VB";
		}
		if (SimulinkConstants.VALUE_INHERIT_AUTO.equals(dataType)) {
			if (SimulinkConstants.VALUE_AUTO.equals(signalType) && SimulinkConstants.VALUE_AUTO.equals(samplingMode)) {
				result = result.concat("inherit");
			}
		} else {
			result = result.concat("Dt:" + dataType);
		}
		if (!SimulinkConstants.VALUE_AUTO.equals(signalType)) {
			if (SimulinkConstants.VALUE_REAL.equals(signalType)) {
				result = result.concat(" C:0");
			} else {
				result = result.concat(" C:1");
			}
		}
		if (!SimulinkConstants.VALUE_AUTO.equals(samplingMode)) {
			if (SimulinkConstants.VALUE_SAMPLE_BASED.equals(samplingMode)) {
				result = result.concat(" S:S");
			} else {
				result = result.concat(" S:F");
			}
		}
		return result;
	}

	/** Determines the label text for a MinMaxRunningResettable block */
	public static String obtainMinMaxResettableBlockData(SimulinkBlock block) {
		String function = block.getParameter(SimulinkConstants.PARAM_FUNCTION);
		return function + "(u,y)";
	}

	/** Determines the label text for a CompareToConstant block */
	public static String obtainCompareToConstantBlockData(SimulinkBlock block) {
		return block.getParameter(SimulinkConstants.PARAM_RELOP) + " "
				+ block.getParameter(SimulinkConstants.PARAM_CTC_CONST);
	}

	/** Determines the label text for a CompareToZero block */
	public static String obtainCompareToZeroBlockData(SimulinkBlock block) {
		return block.getParameter(SimulinkConstants.PARAM_RELOP) + " " + SimulinkConstants.VALUE_0;
	}

	/** Determines the label text for a FunctionCallGenerator block */
	public static String obtainFunctionCallGeneratorBlockData(SimulinkBlock block) {
		String iterations = block.getParameter(SimulinkConstants.PARAM_ITERATIONS);
		if (SimulinkConstants.VALUE_1.equals(iterations)) {
			return SimulinkLabelOnBlockDataConstants.FUNCTION_TEXT;
		}
		return "iterate I = 1:" + iterations;
	}

	/** Determines the label text for a BitwiseOperator block */
	public static String obtainBitwiseOperatorBlockData(SimulinkBlock block) {
		String operator = block.getParameter(SimulinkConstants.PARAM_LOGIC_OPERATOR);
		String result = "Bitwise\n" + operator;
		String useBitMask = block.getParameter(SimulinkConstants.PARAM_USE_BIT_MASK);
		if (SimulinkConstants.VALUE_ON.equals(useBitMask)) {
			result += "\nMask";
		}
		return result;
	}

	/** Determines the label text for a ExtractBits block */
	public static String obtainExtractBitsBlockData(SimulinkBlock block) {
		String bitsToExtract = block.getParameter(SimulinkConstants.PARAM_BITS_TO_EXTRACT);
		switch (bitsToExtract) {
		case SimulinkConstants.VALUE_UPPER_HALF:
			return "Extract Bits\nUpper Half";
		case SimulinkConstants.VALUE_LOWER_HALF:
			return "Extract Bits\nLower Half";
		case SimulinkConstants.VALUE_MSB:
			return "Extract " + block.getParameter(SimulinkConstants.PARAM_NUM_BITS_TO_EXTRACT) + " Bits\nUpper End";
		case SimulinkConstants.VALUE_LSB:
			return "Extract " + block.getParameter(SimulinkConstants.PARAM_NUM_BITS_TO_EXTRACT) + " Bits\nLower End";
		case SimulinkConstants.VALUE_RANGE_OF_BITS:
			return "Extract Bits\n" + block.getParameter(SimulinkConstants.PARAM_BITS_RANGE);
		default:
			return StringUtils.EMPTY_STRING;
		}
	}

	/** Determines the icon on the Simulink Block label */
	private static String getLabelIconFilename(SimulinkBlock block, ESimulinkBlockType type, String typeString) {
		String iconFileName = SimulinkRegistry.getInstance().getLabelIcon(typeString);
		if (iconFileName != null) {
			return iconFileName;
		}
		return type.getBlockIconLabel(block);
	}

	/** Determines the icon on Concatenate block */
	public static String obtainConcatenateIcon(SimulinkBlock block) {
		String mode = block.getParameter(SimulinkConstants.PARAM_MODE);
		if (SimulinkConstants.VALUE_MODE_MATRIX.equals(mode)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_MATRIX_CONCATENATE;
		}
		return null;
	}

	/** Determines the icon on ManualSwitch block */
	public static String obtainManualSwitchIcon(SimulinkBlock block) {
		String currentSetting = block.getParameter(SimulinkConstants.PARAM_CURRENT_SETTING);
		if (SimulinkConstants.VALUE_0.equals(currentSetting)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_MANUAL_SWITCH_0;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_MANUAL_SWITCH_1;
	}

	/** Determines the icon on DiscretePulseGenerator block */
	public static String obtainDiscretePulseGeneratorIcon(SimulinkBlock block) {
		String pulseType = block.getParameter(SimulinkConstants.PARAM_PULSE_TYPE);
		if (SimulinkConstants.VALUE_SAMPLE_BASED.equals(pulseType)) {
			if (block.getInPorts().size() == 1) {
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_PULSE_GENERATOR_SAMPLE_T;
			}
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_PULSE_GENERATOR_SAMPLE;
		}
		if (block.getInPorts().size() == 1) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_PULSE_GENERATOR_TIME_T;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_PULSE_GENERATOR_TIME;
	}

	/** Determines the icon on Product block */
	public static String obtainProductBlockIcon(SimulinkBlock block) {
		String inputs = StringUtils.emptyIfNull(block.getParameter(SimulinkConstants.PARAM_INPUTS));
		String multType = block.getParameter(SimulinkConstants.PARAM_MULTIPLICATION);
		if (inputs.length() == 1 && !SimulinkConstants.VALUE_MATRIX_MULTIPLICATION.equals(multType)) {
			if (inputs.equals("*") || inputs.equals("1")) {
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRODUCT_MULTIPLY;
			} else if (inputs.equals("/")) {
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRODUCT_DIVIDE;
			}
		}
		return null;
	}

	/** Determines the icon on Sum block */
	public static String obtainSumBlockIcon(SimulinkBlock block) {
		String inputs = block.getParameter(SimulinkConstants.PARAM_INPUTS);
		if (inputs.length() == 1) {
			switch (inputs) {
			case "+":
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_SUM_SIGMA;
			case "-":
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_MINUS_SIGMA;
			default:
				return null;
			}
		}
		return null;
	}

	/** Determines the icon on Reshape block */
	public static String obtainReshapeBlockIcon(SimulinkBlock block) {
		String dimensionality = block.getParameter(SimulinkConstants.PARAM_OUTPUT_DIMENSIONALITY);
		if (SimulinkConstants.VALUE_2D_COLUMN.equals(dimensionality)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RESHAPE_COLUMN;
		} else if (SimulinkConstants.VALUE_2D_ROW.equals(dimensionality)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RESHAPE_ROW;
		}
		return null;
	}

	/**
	 * Determines the icon on the Math Complex blocks (MagnitudeAngle and RealImag).
	 * 
	 * @param parameter
	 *            the parameter that determines the icon to be displayed
	 * @param iconOptions
	 *            the available icons for the different parameter values listed in a
	 *            specific order. For MagnitudeAngle blocks the corresponding icons
	 *            must be ordered as follows: {"Magnitude", "Angle", "Magnitude and
	 *            angle"}. For RealImag blocks, it's a list of one icon filename
	 *            corresponding to "Real and imag".
	 */
	public static String obtainComplexBlocksIcons(String parameter, List<String> iconOptions) {
		if (SimulinkConstants.VALUE_MAGNITUDE.equals(parameter)
				|| SimulinkConstants.VALUE_REAL_AND_IMAG.equals(parameter)) {
			return iconOptions.get(0);
		} else if (SimulinkConstants.VALUE_ANGLE.equals(parameter)) {
			return iconOptions.get(1);
		} else if (SimulinkConstants.VALUE_MAGNITUDE_AND_ANGLE.equals(parameter)) {
			return iconOptions.get(2);
		}
		return StringUtils.EMPTY_STRING;
	}

	/** Determines the icon on Math block */
	public static String obtainMathBlockIcon(SimulinkBlock block) {
		String operator = block.getParameter(SimulinkConstants.PARAM_OPERATOR);
		switch (operator) {
		case SimulinkConstants.VALUE_10U:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_10U;
		case SimulinkConstants.VALUE_MAGNITUDE2:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_MAGNITUDE2;
		case SimulinkConstants.VALUE_SQUARE:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_SQUARE;
		case SimulinkConstants.VALUE_POW:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_POW;
		case SimulinkConstants.VALUE_CONJ:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_CONJ;
		case SimulinkConstants.VALUE_RECIPROCAL:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RECIPROCAL;
		case SimulinkConstants.VALUE_TRANSPOSE:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_TRANSPOSE;
		case SimulinkConstants.VALUE_HERMITIAN:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_HERMITIAN;
		case SimulinkConstants.VALUE_EXP:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_EXP;
		default:
			return null;
		}
	}

	/**
	 * Determines the block label data for State Reader and Writer block, by
	 * retrieving the StateIdentifier/StateName parameter of the referenced
	 * StateOwnerBlock. The StateOwnerBlock parameter's value is in a relative path
	 * format, thus a form of path resolution is performed to identify the correct
	 * block.
	 */
	public static String obtainStateReaderWriterBlockData(SimulinkBlock block) {
		String blockPath = block.getParameter(SimulinkConstants.PARAM_STATE_OWNER_BLOCK);
		if (blockPath == null) {
			return null;
		}
		SimulinkBlock parent = block;
		while (blockPath.startsWith("../") && parent != null) {
			blockPath = blockPath.substring(3);
			parent = parent.getParent();
		}
		if (parent == null) {
			return null;
		}
		SimulinkBlock stateOwnerBlock = parent.getSubBlock(blockPath);
		if (stateOwnerBlock == null) {
			return null;
		}
		String stringIdentifier = stateOwnerBlock.getParameter(SimulinkConstants.PARAM_STATE_IDENTIFIER);
		if (stringIdentifier == null) {
			stringIdentifier = stateOwnerBlock.getParameter(SimulinkConstants.PARAM_STATE_NAME);
		}
		return stringIdentifier;
	}

	/** Determines the icon on Sqrt block */
	public static String obtainSqrtBlockIcon(SimulinkBlock block) {
		String operator = block.getParameter(SimulinkConstants.PARAM_OPERATOR);
		if (SimulinkConstants.VALUE_RSQRT.equals(operator)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RSQRT;
		} else if (SimulinkConstants.VALUE_SIGNED_SQRT.equals(operator)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_SIGNED_SQRT;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_SQRT;
	}

	/** Determines the icon on PreLookup block */
	public static String obtainPreLookupBlockIcon(SimulinkBlock block) {
		String dataSrc = block.getParameter(SimulinkConstants.PARAM_BREAK_POINTS_DATA_SRC);
		String onlyIndex = block.getParameter(SimulinkConstants.PARAM_OUTPUT_ONLY_INDEX);
		if (SimulinkConstants.VALUE_DIALOG.equals(dataSrc)) {
			if (SimulinkConstants.VALUE_ON.equals(onlyIndex)) {
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRELOOKUP_DIALOG_INDEX;
			}
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRELOOKUP_DIALOG_FRACTION;
		}
		if (SimulinkConstants.VALUE_ON.equals(onlyIndex)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRELOOKUP_INPORT_INDEX;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_PRELOOKUP_INPORT_FRACTION;
	}

	/** Determines the icon on Lookup ND block */
	public static String obtainNDLookupBlockIcon(SimulinkBlock block) {
		String inputObject = block.getParameter(SimulinkConstants.PARAM_INPUT_SELECT_OBJECT);
		if (SimulinkConstants.VALUE_COLUMN.equals(inputObject)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_COLUMN;
		}
		if (SimulinkConstants.VALUE_2D_MATRIX.equals(inputObject)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_MATRIX;
		}
		String dimensions = block.getParameter(SimulinkConstants.PARAM_TABLE_DIMENSIONS);
		switch (dimensions) {
		case "1":
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_ELEMENT_1;
		case "2":
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_ELEMENT_2;
		case "3":
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_ELEMENT_3;
		default:
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_NDLOOKUP_ELEMENT_4;
		}
	}

	/** Determines the icon on Step block */
	public static String obtainStepBlockIcon(SimulinkBlock block) {
		int before = Integer.parseInt(block.getParameter(SimulinkConstants.PARAM_BEFORE));
		int after = Integer.parseInt(block.getParameter(SimulinkConstants.PARAM_AFTER));
		if (before > after) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_STEP_DEC;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_STEP_INC;
	}

	/** Determines the icon on Assertion block */
	public static String obtainAssertionBlockIcon(SimulinkBlock block) {
		String enabled = block.getParameter(SimulinkConstants.PARAM_ENABLED);
		if (SimulinkConstants.VALUE_OFF.equals(enabled)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_ASSERTION_DISABLED;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_FOR_ASSERTION_ENABELD;
	}

	/** Determines the icon on Reference block */
	public static String obtainReferenceBlockIcon(SimulinkBlock block) {
		String sourceType = block.getParameter(SimulinkConstants.PARAM_SOURCE_TYPE);
		String sourceBlock = block.getParameter(SimulinkConstants.PARAM_SOURCE_BLOCK);
		if (sourceType.contains("Unit Delay")) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_UNIT_DELAY_BLOCK;
		} else if (sourceBlock.contains(SimulinkConstants.VALUE_BACE_UNIT_DELAY)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_UNIT_DELAY_BLOCK;
		} else if (sourceType.contains(SimulinkConstants.TYPE_BACE_TURN_ON_DELAY)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_TURN_ON_DELAY;
		} else if (sourceType.contains(SimulinkConstants.TYPE_BACE_TURN_OFF_DELAY)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_TURN_OFF_DELAY;
		} else if (sourceBlock.contains("BACE")
				&& (sourceBlock.contains("ArraySum") || sourceBlock.contains("ArrayMean"))) {
			return "bace_array_" + sourceBlock.split("/")[1].split("Array")[1].toLowerCase() + ".png";
		} else if (sourceBlock.contains("BACE_DiscreteTimeIntegrator")) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_DISCRETE_TIME_INTEGRATOR;
		} else if (sourceBlock.contains("BACE") && sourceBlock.contains("Integrator")) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_INTEGRATOR;
		}
		return null;
	}

	/** Determines the icon on Mux/Demux block */
	public static String obtainMuxBlockIcon(SimulinkBlock block) {
		String displayOption = block.getParameter(SimulinkConstants.MUX_DISPLAY_OPTION);
		if (SimulinkConstants.VALUE_BAR.equals(displayOption)) {
			return SimulinkLabelOnBlockDataConstants.MUX_BAR_ICON;
		}
		return null;
	}

	/** Determines the icon on TriggerPort block */
	public static String obtainTriggerPortBlockIcon(SimulinkBlock block) {
		String triggerType = block.getParameter(SimulinkConstants.PARAM_TRIGGER_TYPE);
		if (SimulinkConstants.VALUE_RISING.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.RESET_RISING;
		}
		if (SimulinkConstants.VALUE_FALLING.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.RESET_FALLING;
		}
		if (SimulinkConstants.VALUE_EITHER.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.RESET_EITHER;
		}
		return StringUtils.EMPTY_STRING;
	}

	/** Determines the icon on the Integrator Block */
	public static String obtainIntegratorBlockIcon(SimulinkBlock block) {
		String limitOutputParam = block.getParameter(SimulinkConstants.PARAM_LIMIT_OUTPUT);
		String wrapStateParam = block.getParameter(SimulinkConstants.PARAM_WRAP_STATE);
		String iconFileName;
		if (limitOutputParam != null && SimulinkConstants.VALUE_ON.equals(limitOutputParam)) {
			iconFileName = SimulinkLabelOnBlockDataConstants.ICON_FOR_INTEGRATOR_LIMIT_OUTPUT;
		} else if (wrapStateParam != null && SimulinkConstants.VALUE_ON.equals(wrapStateParam)) {
			iconFileName = SimulinkLabelOnBlockDataConstants.ICON_FOR_INTEGRATOR_WRAP_STATE;
		} else {
			iconFileName = SimulinkLabelOnBlockDataConstants.ICON_FOR_INTEGRATOR_DEFAULT;
		}
		return iconFileName;
	}

	/** Determines the text on the SignalConversion Block */
	public static String obtainSignalConversionData(SimulinkBlock block) {
		String conversionOutput = block.getParameter(SimulinkConstants.PARAM_CONVERSION_OUTPUT);
		if (SimulinkConstants.VALUE_VIRTUAL_BUS.equals(conversionOutput)) {
			return SimulinkLabelOnBlockDataConstants.ICON_CONVERSION_VIRTUAL_BUS;
		} else if (SimulinkConstants.VALUE_NONVIRTUAL_BUS.equals(conversionOutput)) {
			return SimulinkLabelOnBlockDataConstants.ICON_CONVERSION_NONVIRTUAL_BUS;
		}
		return SimulinkLabelOnBlockDataConstants.ICON_CONVERSION_SIGNAL_COPY;
	}

	/** Determines the text on the Product Block */
	public static String obtainProductData(SimulinkBlock block) {
		String multType = block.getParameter(SimulinkConstants.PARAM_MULTIPLICATION);
		String inputs = block.getParameter(SimulinkConstants.PARAM_INPUTS);
		if (SimulinkConstants.VALUE_MATRIX_MULTIPLICATION.equals(multType)) {
			if ("/".equals(inputs)) {
				return null;
			}
			try {
				Integer.parseInt(inputs);
				return SimulinkLabelOnBlockDataConstants.TEXT_ON_MATRIX_PRODUCT_BLOCK;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		try {
			int numberOfInputs = Integer.parseInt(inputs);
			if (numberOfInputs == 1) {
				return null;
			}
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_PRODUCT_BLOCK;
		} catch (NumberFormatException e) {
			if (!inputs.contains("/") && inputs.length() > 1) {
				return SimulinkLabelOnBlockDataConstants.TEXT_ON_PRODUCT_BLOCK;
			}
			return null;
		}
	}

	/** Determines the text on the Delay Block */
	public static String obtainDelayData(SimulinkBlock block) {
		if (SimulinkConstants.VALUE_DIALOG.equals(block.getParameter(SimulinkConstants.PARAM_DELAY_LENGTH_SOURCE))) {
			String delayLength = block.getParameter(SimulinkConstants.PARAM_DELAY_LENGTH);
			return "Z^(-" + delayLength + ")";
		}
		return "Z^(-d)";

	}

	/**
	 * Determine text for switch blocks.
	 *
	 * @param block
	 *            a switch block
	 * @return the text for the label on block
	 */
	public static String determineTextForSwitch(SimulinkBlock block) {
		String criterion = block.getParameter(SimulinkConstants.PARAM_CRITERIA);
		criterion = criterion.replace(SimulinkConstants.VALUE_U2 + StringUtils.SPACE, StringUtils.EMPTY_STRING);

		if (criterion.contains(SimulinkConstants.VALUE_THRESHOLD)) {
			criterion = criterion.replace(SimulinkConstants.VALUE_THRESHOLD, StringUtils.EMPTY_STRING);

			String threshold = block.getParameter(SimulinkConstants.PARAM_THRESHOLD);
			return criterion + threshold;
		}
		return criterion;

	}

	/**
	 * @param block
	 *            A block of type Inport or Outport
	 * @return the label of the port
	 */
	public static String determineTextForPorts(SimulinkBlock block) {
		String portLabel = StringUtils.EMPTY_STRING;
		String paramIconDisplay = block.getParameter(SimulinkConstants.PARAM_ICON_DISPLAY);
		SimulinkPortBase port = getPort(block);

		if (paramIconDisplay != null
				&& StringUtils.containsIgnoreCase(paramIconDisplay, SimulinkConstants.VALUE_Port_NUMBER)) {
			String paramPort = block.getParameter(SimulinkConstants.PARAM_PORT);
			if (paramPort != null) {
				portLabel = paramPort;
			} else if (port != null) {
				portLabel = port.getParameter(SimulinkConstants.PARAM_PORT_NUMBER);
			}
		}

		if (port != null && StringUtils.containsIgnoreCase(paramIconDisplay, SimulinkConstants.VALUE_SIGNAL_NAME)) {
			portLabel += " " + port.getParameter(SimulinkConstants.PARAM_NAME);
		}
		return portLabel;
	}

	/**
	 * @param block
	 *            the Inport or Outport block from which the port is needed to
	 *            obtain label data (that may be encapsulated by the port).
	 * @return Depending on the input type the corresponding {@link SimulinkInPort}
	 *         or {@link SimulinkOutPort}. May be <code>null</code> when there is no
	 *         port.
	 */
	public static SimulinkPortBase getPort(SimulinkBlock block) {

		if (SimulinkConstants.TYPE_INPORT.equals(block.getParameter(SimulinkConstants.PARAM_BLOCK_TYPE))) {
			UnmodifiableCollection<SimulinkOutPort> ports = block.getOutPorts();

			// There is always one and only one port
			CCSMAssert.isTrue(ports.size() == 1, "Expected Inport block "
					+ block.getParameter(SimulinkConstants.PARAM_NAME) + "to have exactly one port");
			SimulinkPortBase portsArray[] = new SimulinkPortBase[1];
			return ports.toArray(portsArray)[0];

		} else if (SimulinkConstants.TYPE_OUTPORT.equals(block.getParameter(SimulinkConstants.PARAM_BLOCK_TYPE))) {
			UnmodifiableCollection<SimulinkInPort> ports = block.getInPorts();

			// There is always one and only one port
			CCSMAssert.isTrue(ports.size() == 1, "Expected Inport block "
					+ block.getParameter(SimulinkConstants.PARAM_NAME) + "to have exactly one port");
			SimulinkPortBase portsArray[] = new SimulinkPortBase[1];
			return ports.toArray(portsArray)[0];
		}
		return null;
	}

	/** Determines the text on BACE BMW_FunctionCallGenerator block */
	public static String obtainBmwFunctionCallGeneratorBlockData(SimulinkBlock block) {
		boolean nkw = SimulinkConstants.VALUE_ON.equals(block.getParameter(SimulinkConstants.PARAM_NKW));
		boolean state = SimulinkConstants.VALUE_ON.equals(block.getParameter(SimulinkConstants.TYPE_STATE));
		if (nkw ^ state) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BMW_FUNCTION_CALL_GENERATOR;
		}
		if (state) {
			return "???";
		}
		return null;
	}

	/** Determines the text on BitClear or BitSet blocks */
	public static String obtainBitClearOrSetBlockData(SimulinkBlock block, String inputText) {
		String bit = block.getParameter(SimulinkConstants.PARAM_IBIT);
		return inputText + "\nbit " + bit;
	}

	/** Determines the text on Reference blocks */
	public static String obtainReferenceBlockLabelData(SimulinkBlock block) {
		String sourceBlock = block.getParameter(SimulinkConstants.PARAM_SOURCE_BLOCK);
		if (sourceBlock.contains("BACE") && (sourceBlock.contains("Mapping") || sourceBlock.contains("Unmap"))) {
			return sourceBlock.split("/")[1].replaceAll("_", "\n");
		} else if (sourceBlock.contains("putbit") || sourceBlock.contains("getbit") || sourceBlock.contains("setbit")) {
			return sourceBlock.split("/")[1];
		} else if (SimulinkConstants.TYPE_BACE_FIXPOINT_PRODUCT.equals(sourceBlock)) {
			return "X";
		} else if (sourceBlock.contains("BACE_RequireUnscaledVariable")) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_REQUIRE_UNSCALED_VARIABLE;
		} else if (sourceBlock.contains("BACE_PIDControl")) {
			return "PID\ncontrol";
		}
		return null;
	}

	/** Determines the text on DivByZero BACE block */
	public static String obtainBaceDivByZeroBlockData(SimulinkBlock block) {
		boolean useEps = SimulinkConstants.VALUE_ON.equals(block.getParameter(SimulinkConstants.PARAM_BACE_USE_EPS));
		String labelText = "0";
		if (useEps) {
			labelText = "EPS";
		}
		return "PROTECT\nDIV-" + labelText;
	}

	/**
	 * @see #text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @see #iconFileName
	 */
	public String getIcon() {
		return iconFileName;
	}

	/** Determines the text on DiscreteFilter block */
	public static String obtainDiscreteFilterBlockData(SimulinkBlock block) {
		String numeratorSource = block.getParameter(SimulinkConstants.PARAM_NUMERATOR_SOURCE);
		String denominatorSource = block.getParameter(SimulinkConstants.PARAM_DENOMINATOR_SOURCE);
		if (numeratorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)
				|| denominatorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)) {
			return "IIR";
		}
		String[] numeratorValues = block.getParameter(SimulinkConstants.PARAM_NUMERATOR).replaceAll("[\\[\\]]", "")
				.split(" ");
		String[] denominatorValues = block.getParameter(SimulinkConstants.PARAM_DENOMINATOR).replaceAll("[\\[\\]]", "")
				.split(" ");
		String numeratorResult = createDiscreteFilterFunction(numeratorValues, false);
		String denominatorResult = createDiscreteFilterFunction(denominatorValues, false);

		return numeratorResult + "\n" + denominatorResult;
	}

	/**
	 * Returns the polynomial function for discrete filter blocks.
	 * 
	 * @param coefficients
	 *            coefficients to be used in the polynomial function.
	 * @param highestDegreeFirst
	 *            whether the polynomial starts with the highest degree or not. If
	 *            true, then fn = Z^2 + Z + 1. Otherwise, fn = 1 + Z + Z^2
	 * @return the created polynomial function
	 */
	private static String createDiscreteFilterFunction(String[] coefficients, boolean highestDegreeFirst) {
		String result = coefficients[0];
		for (int i = 1; i < coefficients.length; i++) {
			if (highestDegreeFirst) {
				result += "z^" + (coefficients.length - i) + "+" + coefficients[i];
			} else {
				result += "+" + coefficients[i] + "z^-" + i;
			}
		}
		return result.replaceAll("\\^1", "");
	}

	/** Determines the text on DiscreteFir block */
	public static String obtainDiscreteFirBlockData(SimulinkBlock block) {
		String coefficientSource = block.getParameter("CoefSource");
		if (coefficientSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)) {
			return "num(z)\n1";
		}
		String[] coefficients = block.getParameter("Coefficients").replaceAll("[\\[\\]]", "").split(" ");
		return createDiscreteFilterFunction(coefficients, false) + "\n1";
	}

	/** Determines the text on DiscreteTransferFcn block */
	public static String obtainDiscreteTransferFcnBlockData(SimulinkBlock block) {
		String numeratorSource = block.getParameter(SimulinkConstants.PARAM_NUMERATOR_SOURCE);
		String denominatorSource = block.getParameter(SimulinkConstants.PARAM_DENOMINATOR_SOURCE);
		String initialStateSource = block.getParameter(SimulinkConstants.PARAM_INITIAL_STATES_SOURCE);
		if (numeratorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)
				|| denominatorSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)
				|| initialStateSource.equals(SimulinkConstants.VALUE_AS_INPUT_PORT)) {
			return "DTF";
		}
		String[] numeratorValues = block.getParameter(SimulinkConstants.PARAM_NUMERATOR).replaceAll("[\\[\\]]", "")
				.split(" ");
		String[] denominatorValues = block.getParameter(SimulinkConstants.PARAM_DENOMINATOR).replaceAll("[\\[\\]]", "")
				.split(" ");
		String numeratorResult = createDiscreteFilterFunction(numeratorValues, true);
		String denominatorResult = createDiscreteFilterFunction(denominatorValues, true);
		return numeratorResult + "\n" + denominatorResult;
	}

	/** Determines the text on FunctionCaller block */
	public static String obtainFunctionCallerBlockData(SimulinkBlock block) {
		return "caller\n" + block.getParameter(SimulinkConstants.PARAM_FUNCTION_NAME);
	}

	/**
	 * Determines the icon to be displayed on the ResetPort block depending on the
	 * trigger type.
	 */
	public static String obtainResetPortBlockIcon(SimulinkBlock block) {
		String triggerType = block.getParameter(SimulinkConstants.PARAM_RESET_TRIGGER_TYPE);
		if (SimulinkConstants.VALUE_RISING.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RISING_RESET_PORT;
		}
		if (SimulinkConstants.VALUE_FALLING.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_FALLING_RESET_PORT;
		}
		if (SimulinkConstants.VALUE_EITHER.equals(triggerType)) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_EITHER_RESET_PORT;
		}
		return null;
	}
}
