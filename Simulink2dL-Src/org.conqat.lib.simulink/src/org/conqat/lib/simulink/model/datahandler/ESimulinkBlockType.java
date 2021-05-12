/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id$            
 |                                                                       |
 | Copyright (c)  2009-2015 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.model.datahandler;

import java.util.Arrays;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkPortLabelUtils;
import org.conqat.lib.simulink.ui.SimulinkLabelOnBlockData;
import org.conqat.lib.simulink.ui.SimulinkLabelOnBlockDataConstants;

/**
 * Simulink block types. This enum lists only those blocks with special shapes,
 * icons, or special text. All other blocks will get type DEFAULT.
 */
public enum ESimulinkBlockType {

	// Simulink blocks

	/** Type for ActionPort block */
	ACTION_PORT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.determineActionPortData(block);
		}
	},

	/** Type for AlgebraicConstraint reference block */
	ALGEBRAIC_CONSTRAINT(SimulinkConstants.TYPE_ALGEBRAIC_CONSTRAINT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_ALGEBRAIC_CONSTRAINT_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainAlgebraicConstraintPortData(port);
		}
	},

	/** Type for ArgIn block */
	ARG_IN,

	/** Type for ArgOut block */
	ARG_OUT,

	/** Type for ArithShift block */
	ARITH_SHIFT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainArithShiftData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainArithShiftPortData(port);
		}
	},

	/** Type for Assertion block */
	ASSERTION {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainAssertionBlockIcon(block);
		}
	},

	/** Type for Assignment block */
	ASSIGNMENT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainAssignmenOrSelectortBlockData(block, "Assignment");
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainAssignmentPortData(port);
		}
	},

	/** Type for BandLimitedNoise block */
	BAND_LIMITED_NOISE(SimulinkConstants.TYPE_BAND_LIMITED_NOISE) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BAND_LIMITED_NOISE;
		}
	},

	/** Type for Bias block */
	BIAS {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBiasBlockData(block);
		}
	},

	/** Type for BitClear block */
	BIT_CLEAR(SimulinkConstants.TYPE_BIT_CLEAR) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBitClearOrSetBlockData(block, "Clear");
		}
	},

	/** Type for BitSet block */
	BIT_SET(SimulinkConstants.TYPE_BIT_SET) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBitClearOrSetBlockData(block, "Set");
		}
	},

	/** Type for BitwiseOperator reference block */
	BITWISE_OPERATOR(SimulinkConstants.TYPE_BITWISE_OPERATOR) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBitwiseOperatorBlockData(block);
		}
	},

	/** Type for BusAssignment block */
	BUS_ASSIGNMENT {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBusAssignmentPortData(port);
		}
	},

	/** Type for BusCreator block */
	BUS_CREATOR,

	/** Type for BusSelector block */
	BUS_SELECTOR,

	/** Clock block is rounded */
	CLOCK,

	/** Type for CompareToConstant reference block */
	COMPARE_TO_CONSTANT(SimulinkConstants.TYPE_COMPARE_TO_CONSTANT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainCompareToConstantBlockData(block);
		}
	},

	/** Type for CompareToZero reference block */
	COMPARE_TO_ZERO(SimulinkConstants.TYPE_COMPARE_TO_ZERO) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainCompareToZeroBlockData(block);
		}
	},

	/** Type for ComplexToMagnitudeAngle block */
	COMPLEX_TO_MAGNITUDE_ANGLE {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexToMagnitudeAngleBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexBlocksIcons(block.getParameter(SimulinkConstants.PARAM_OUTPUT),
					Arrays.asList(null, SimulinkLabelOnBlockDataConstants.ICON_FOR_COMPLEX_TO_MA_ANGLE,
							SimulinkLabelOnBlockDataConstants.ICON_FOR_COMPLEX_TO_MA_BOTH));
		}
	},

	/** Type for ComplexToRealImag block */
	COMPLEX_TO_REAL_IMAG {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexToRealImagBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexBlocksIcons(block.getParameter(SimulinkConstants.PARAM_OUTPUT),
					Arrays.asList(SimulinkLabelOnBlockDataConstants.ICON_FOR_COMPLEX_TO_RI_BOTH));
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainComplexToRealImagPortData(port);
		}
	},

	/** Type for Concatenate block */
	CONCATENATE {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainConcatenateIcon(block);
		}
	},

	/** Constants show the constant value */
	CONSTANT,

	/** Type for ConversionInherited reference block */
	CONVERSION_INHERITED(SimulinkConstants.TYPE_CONVERSION_INHERITED) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_DATA_TYPE_CONVERSION_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainConversionInheritedPortData(port);
		}
	},

	/** Type for CounterFreeRunning block */
	COUNTER_FREE_RUNNING(SimulinkConstants.TYPE_COUNTER_FREE_RUNNING) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_COUNTER_FREE_RUNNING;
		}
	},

	/** Type for DFlipFlop block */
	D_FLIP_FLOP(SimulinkConstants.TYPE_D_FLIP_FLOP) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFlipFlopPortData(port, Arrays.asList("D", "CLK", "!CLR"));
		}
	},

	/** Type for DLatch block */
	D_LATCH(SimulinkConstants.TYPE_D_LATCH) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFlipFlopPortData(port, Arrays.asList("D", "C"));
		}
	},

	/** Type for DataStoreMemory block */
	DATA_STORE_MEMORY,

	/** Type for DataStoreRead block */
	DATA_STORE_READ,

	/** Type for DataStoreWrite block */
	DATA_STORE_WRITE,

	/** Type for DataTypeConversion block */
	DATA_TYPE_CONVERSION {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDataTypeConversionBlockData(block);
		}
	},

	/** Type for DataTypeDuplicate block */
	DATA_TYPE_DUPLICATE,

	/** Type for DataTypePropagation block */
	DATA_TYPE_PROPAGATION(SimulinkConstants.TYPE_DATA_TYPE_PROPAGATION) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDataTypePropagationPortData(port,
					SimulinkConstants.TYPE_DATA_TYPE_PROPAGATION);
		}
	},

	/** Type for DeadZoneDynamic block */
	DEAD_ZONE_DYNAMIC(SimulinkConstants.TYPE_DEAD_ZONE_DYNAMIC) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_DEAD_ZONE_DYNAMIC_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("up", "u", "b"),
					Arrays.asList("y"));
		}
	},

	/** Delay displays information about delay */
	DELAY {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDelayData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDelayBlockPortData(port);
		}
	},

	/** Type for Demux block */
	DEMUX {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMuxBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMuxBlockIcon(block);
		}
	},

	/** Type for DetectChange reference block */
	DETECT_CHANGE(SimulinkConstants.TYPE_DETECT_CHANGE) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_ON_DETECT_CHANGE_BLOCK;
		}
	},

	/** Type for DiscreteFilter block */
	DISCRETE_FILTER {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDiscreteFilterBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDiscreteFilterPortData(port);
		}
	},

	/** Type for DiscreteFir block */
	DISCRETE_FIR {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDiscreteFirBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDiscreteFirPortData(port);
		}
	},

	/** Type for DiscretePulseGenerator block */
	DISCRETE_PULSE_GENERATOR {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDiscretePulseGeneratorIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDiscretePulseGeneratorPortData(port);
		}
	},

	/** Type for DiscreteStateSpace block **/
	DISCRETE_STATE_SPACE {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_DISCRETE_STATE_SPACE;
		}
	},

	/** Type for DiscreteTransferFcn block */
	DISCRETE_TRANSFER_FCN {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainDiscreteTransferFcnBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainDiscreteFilterPortData(port);
		}
	},

	/** Type for Display block */
	DISPLAY {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.getDisplayBlockFormat(block);
		}
	},

	/** Type for EnumeratedConstant reference block */
	ENUMERATED_CONSTANT(SimulinkConstants.TYPE_ENUMERATED_CONSTANT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.PARAM_VALUE);
		}
	},

	EVENT_LISTENER {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			String eventType = block.getParameter(SimulinkConstants.PARAM_EVENT_LISTENER_TYPE);
			switch (eventType) {
			case "Reset":
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_EVENT_LISTENER_RESET;
			case "Terminate":
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_EVENT_LISTENER_TERMIINATE;
			case "Initialize":
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_EVENT_LISTENER_INITIALIZE;
			default:
				return null;
			}
		}
	},

	/** Type for ExtractBits reference block */
	EXTRACT_BITS(SimulinkConstants.TYPE_EXTRACT_BITS) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainExtractBitsBlockData(block);
		}
	},

	/** Type for Fcn block */
	FCN,

	/** Type for FlipFlopDigitalClock block */
	FLIP_FLOP_DIGITAL_CLOCK(SimulinkConstants.TYPE_FLIP_FLOP_DIGITAL_CLOCK) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_EXTRAS_DIGITAL_CLOCK;
		}
	},

	/** Type for ForIterator block */
	FOR_ITERATOR {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainForIteratorPortData(port);
		}
	},

	/** Type for From block */
	FROM,

	/** Type for FromWorkspace block */
	FROM_WORKSPACE,

	/** Type for FunctionCaller block */
	FUNCTION_CALLER {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainFunctionCallerBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFunctionCallerPortData(port);
		}
	},

	/** Type for FunctionCallGenerator reference block */
	FUNCTION_CALL_GENERATOR(SimulinkConstants.TYPE_FN_CALL_GENERATOR) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainFunctionCallGeneratorBlockData(block);
		}
	},

	/** Function call split block is rounded */
	FUNCTION_CALL_SPLIT {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFunctionCallSplitPortLabelData(port);
		}
	},

	/** Type for Goto block */
	GOTO,

	/** Gain is triangular and has text */
	GAIN {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainGainBlockData(block);
		}
	},

	/** Type for Ground block */
	GROUND,

	/** Type for IF block */
	IF {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainIfPortLabelData(port);
		}
	},

	/** Inport is rectangular with rounded edges */
	INPORT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.determineTextForPorts(block);
		}
	},

	/** Type for Integrator block */
	INTEGRATOR {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainIntegratorBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainIntegratorPortData(port);
		}
	},

	/** Type for IntervalTestDynamic block */
	INTERVAL_TEST_DYNAMIC(SimulinkConstants.TYPE_INTERVAL_TEST_DYNAMIC) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			String closedRight = block.getParameter(SimulinkConstants.PARAM_INTERVAL_CLOSED_RIGHT);
			String closedLeft = block.getParameter(SimulinkConstants.PARAM_INTERVAL_CLOSED_LEFT);
			if (closedRight.equals(SimulinkConstants.VALUE_ON)) {
				if (closedLeft.equals(SimulinkConstants.VALUE_ON)) {
					return SimulinkLabelOnBlockDataConstants.ICON_FOR_INTERVAL_TEST_DYNAMIC_LC_RC;
				}
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_INTERVAL_TEST_DYNAMIC_LO_RC;
			}
			if (closedLeft.equals(SimulinkConstants.VALUE_ON)) {
				return SimulinkLabelOnBlockDataConstants.ICON_FOR_INTERVAL_TEST_DYNAMIC_LC_RO;
			}
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_INTERVAL_TEST_DYNAMIC_LO_RO;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("up", "u", "lo"),
					Arrays.asList("y"));
		}
	},

	/** Type for JKFlipFlop block */
	JK_FLIP_FLOP(SimulinkConstants.TYPE_JK_FLIP_FLOP) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFlipFlopPortData(port, Arrays.asList("J", "CLK", "K"));
		}
	},

	/** Type for Logic block */
	LOGIC,

	/** Type for LookupNDDirect block */
	LOOKUP_N_D_DIRECT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainLookupNDBlockcData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainNDLookupBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainLookupNDPortData(port);
		}
	},

	/** Type for LookupTableDynamic block */
	LOOKUP_TABLE_DYNAMIC("Lookup Table Dynamic") {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainLookupTableDynamicPortData(port);
		}
	},

	/** Type for MagnitudeAngleToComplex block */
	MAGNITUDE_ANGLE_TO_COMPLEX {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexBlocksIcons(block.getParameter(SimulinkConstants.PARAM_INPUT),
					Arrays.asList(SimulinkLabelOnBlockDataConstants.ICON_FOR_MA_TO_COMPLEX_MAGNITUDE,
							SimulinkLabelOnBlockDataConstants.ICON_FOR_MA_TO_COMPLEX_ANGLE,
							SimulinkLabelOnBlockDataConstants.ICON_FOR_MA_TO_COMPLEX_BOTH));
		}
	},

	/** Type for ManualSwitch block */
	MANUAL_SWITCH {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainManualSwitchIcon(block);
		}
	},

	/** Type for Math block */
	MATH {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMathBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMathBlockIcon(block);
		}
	},

	/** Type for MinMaxRunningResettable reference block */
	MIN_MAX_RUNNING_RESETTABLE(SimulinkConstants.TYPE_MIN_MAX_RUNNING_RESETTABLE) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMinMaxResettableBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainMinMaxResettablePortData(port);
		}
	},

	/** Type for MultiPortSwitch block */
	MULTI_PORT_SWITCH("MultiPortSwitch") {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainMultiPortSwitchPortData(port);
		}
	},

	/** Type for Mux block */
	MUX {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMuxBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainMuxBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainMuxPortData(port);
		}
	},

	/** Outport is rectangular with rounded edges */
	OUTPORT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.determineTextForPorts(block);
		}
	},

	/** Type for PermuteDimensions block */
	PERMUTE_DIMENSIONS {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainPermuteDimensionsBlockData(block);
		}
	},

	/** Type for PreLookup block */
	PRE_LOOKUP {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainPreLookupBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainPreLookupPortData(port);
		}
	},

	/** Type for Product block */
	PRODUCT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainProductData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainProductBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainProductPortData(port);
		}
	},

	/** Type for Ramp block */
	RAMP(SimulinkConstants.TYPE_RAMP) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_RAMP_BLOCK;
		}
	},

	/** Type for RealImagToComplex block */
	REAL_IMAG_TO_COMPLEX {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainRealImagToComplexBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainComplexBlocksIcons(block.getParameter(SimulinkConstants.PARAM_INPUT),
					Arrays.asList(SimulinkLabelOnBlockDataConstants.ICON_FOR_RI_TO_COMPLEX_BOTH));
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainRealImagToComplexPortData(port);
		}
	},

	/** Type for reference block */
	REFERENCE {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainReferenceBlockLabelData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainReferenceBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainReferencePortData(port);
		}
	},

	/** RelationalOperator shows the operator */
	RELATIONAL_OPERATOR,

	/** Type for ResetPort block */
	RESET_PORT {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainResetPortBlockIcon(block);
		}
	},

	/** Type for Reshape block */
	RESHAPE {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainReshapeBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainReshapeBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainReshapePortData(port);
		}
	},

	/** Type for SampleTimeMath block */
	SAMPLE_TIME_MATH {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainSampleTimeMathBlockData(block);
		}
	},

	/** Type for SaturationDynamic block */
	SATURATION_DYNAMIC(SimulinkConstants.TYPE_SATURATION_DYNAMIC) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_SATURATION_DYNAMIC_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("up", "u", "b"),
					Arrays.asList("y"));
		}
	},

	/** Type for ScalingStrip block */
	SCALING_STRIP(SimulinkConstants.TYPE_SCALING_STRIP) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkConstants.TYPE_SCALING_STRIP.replaceAll("\\s", "\n");
		}
	},

	/** Type for Selector block */
	SELECTOR {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainAssignmenOrSelectortBlockData(block, "Selector");
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainSelectorPortData(port);
		}
	},

	/** Type for SignalConversion block */
	SIGNAL_CONVERSION {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainSignalConversionData(block);
		}
	},

	/** Type for SignalSpecification block */
	SIGNAL_SPECIFICATION {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainSignalSpecificationData(block);
		}
	},

	/** Type for Sin block */
	SIN {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainSinPortData(port);
		}
	},

	/** Type for SliderGain reference block */
	SLIDER_GAIN(SimulinkConstants.TYPE_SLIDER_GAIN) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.PARAM_GAIN.toLowerCase());
		}
	},

	/** Type for Sqrt block */
	SQRT {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainSqrtBlockIcon(block);
		}
	},

	/** Type for SRFlipFlop block */
	SR_FLIP_FLOP(SimulinkConstants.TYPE_SR_FLIP_FLOP) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFlipFlopPortData(port, Arrays.asList("S", "R", "i"));
		}
	},

	/** Type for StateReader block */
	STATE_READER {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainStateReaderWriterBlockData(block);
		}
	},

	/** Type for StateWriter block */
	STATE_WRITER {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainStateReaderWriterBlockData(block);
		}
	},

	/** Type for Step block */
	STEP {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainStepBlockIcon(block);
		}
	},

	/** Type for subsystems */
	SUB_SYSTEM {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.getSubSystemBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.getSubSystemBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainSubSystemPortLabelData(port);
		}
	},

	/** Type for Sum block */
	SUM {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainSumBlockIcon(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainRectangularSumInPortLabelData(port);
		}
	},

	/**
	 * Type for rounded sum blocks (other rectangular sum blocks have type DEFAULT)
	 */
	SUM_ROUND {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainSumInPortLabelData(port);
		}
	},

	/** Type for Switch block */
	SWITCH {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.determineTextForSwitch(block);
		}
	},

	/** Switches have icons and text */
	SWITCH_CASE {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainSwitchCasePortLabelData(port);
		}
	},

	/** Terminators have icons */
	TERMINATOR,

	/** Type for TriggerPort block */
	TRIGGER_PORT {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainTriggerPortBlockData(block);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainTriggerPortBlockIcon(block);
		}
	},

	/** Type for UnitDelay block */
	UNIT_DELAY,

	/** Type for WhileIterator block */
	WHILE_ITERATOR {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.getWhileIteratorBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainWhileIteratorPortData(port);
		}
	},

	/** Type for VariantSink block */
	VARIANT_SINK {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainVariantSinkPortData(port);
		}
	},

	/** Type for VariantSource block */
	VARIANT_SOURCE {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainVariantSourcePortData(port);
		}
	},

	// BACE blocks

	/** Type for BACE ArrayMinMax block */
	BACE_ARRAY_MIN_MAX(SimulinkConstants.TYPE_MIN_MAX) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.TYPE_MIN_MAX.toLowerCase());
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceArrayMinMaxPortData(port);
		}
	},

	/** Type for BACE BitShift block */
	BACE_BIT_SHIFT(SimulinkConstants.TYPE_BACE_BIT_SHIFT) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceBitShiftPortData(port);
		}
	},

	/** Type for BACE BitwiseNot block */
	BACE_BITWISE_NOT(SimulinkConstants.TYPE_BACE_BITWISE_NOT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_BITWISE_NOT;
		}
	},

	/** Type for BACE BMW_FunctionCallGenerator block */
	BACE_BMW_FUNCTION_CALL_GENERATOR(SimulinkConstants.TYPE_BMW_FUNCTION_CALL_GENERATOR) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBmwFunctionCallGeneratorBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBmwFunctionCallGeneratorPortData(port);
		}

	},

	/** Type for BACE ClosedInterval block */
	BACE_CERTIFIED_SUBSYSTEM(SimulinkConstants.BACE_TYPE_CERTIFIED_SUBSYSTEM) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_CERTIFIED_SUBSYSTEM;
		}
	},

	/** Type for BACE ClosedInterval block */
	BACE_CLOSED_INTERVAL(SimulinkConstants.TYPE_BACE_CLOSED_INTERVAL) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_CLOSED_INTERVAL_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("A", "X", "C"),
					Arrays.asList("==>"));
		}
	},

	/** Type for BACE CountDown block */
	BACE_COUNT_DOWN(SimulinkConstants.BACE_TYPE_COUNT_DOWN) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_COUNTDOWN_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceCounterPortData(port);
		}
	},

	/** Type for BACE Counter block */
	BACE_COUNTER(SimulinkConstants.BACE_TYPE_COUNTER) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_COUNTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceCounterPortData(port);
		}
	},

	/** Type for BACE DivByZero block */
	BACE_DIV_BY_ZERO(SimulinkConstants.TYPE_BACE_DIV_BY_ZERO) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainBaceDivByZeroBlockData(block);
		}
	},

	/** Type for BACE Mask DT block */
	BACE_DT(SimulinkConstants.BACE_TYPE_MASK_DT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return "dt";
		}
	},

	/** Type for EdgeBi BACE block */
	BACE_EDGE_BI(SimulinkConstants.TYPE_BACE_EDGE_BI) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_EDGE_BI;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceEdgePortData(port);
		}
	},

	/** Type for EdgeFalling BACE block */
	BACE_EDGE_FALLING(SimulinkConstants.TYPE_BACE_EDGE_FALLING) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_EDGE_FALLING;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceEdgePortData(port);
		}
	},

	/** Type for EdgeRising BACE block */
	BACE_EDGE_RISING(SimulinkConstants.TYPE_BACE_EDGE_RISING) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_EDGE_RISING;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceEdgePortData(port);
		}
	},

	/** Type for FixpointGain BACE block */
	BACE_GAIN(SimulinkConstants.TYPE_BACE_GAIN) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockData.obtainGainBlockData(block);
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFixPointPortLabel(port);
		}
	},

	/** Type for Fader BACE block */
	BACE_FADER(SimulinkConstants.TYPE_BACE_FADER) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_FADER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("In1", "In0", "F"),
					Arrays.asList("Out"));
		}
	},

	/** Type for Fader with Constant Parameter BACE block */
	BACE_FADER_CONST(SimulinkConstants.TYPE_BACE_FADER_CONST) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_FADER_CONST_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("In1", "In0", "F"),
					Arrays.asList("Out"));
		}
	},

	/** Type for BACE FalseConstant block */
	BACE_FALSE_CONSTANT(SimulinkConstants.TYPE_BACE_FALSE_CONSTANT) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.positionPortLabel(port, SimulinkConstants.VALUE_FALSE);
		}
	},

	/** Type for FeedbackLatch BACE block */
	BACE_FEEDBACK_LATCH(SimulinkConstants.TYPE_BACE_FEEDBACK_LATCH) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_FEEDBACK_LATCH;
		}
	},

	/** Type for BACE FixedStepDelay block */
	BACE_FIXED_STEP_DELAY(SimulinkConstants.TYPE_BACE_FIXED_STEP_DELAY) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_FIXED_VARIABLE_DELAY_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceFixedVariableDelayPortData(port);
		}
	},

	/** Type for BACE FixedMovingAverage block */
	BACE_FIXED_MOVING_AVERAGE(SimulinkConstants.TYPE_BACE_FIXED_MOVING_AVERAGE) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceMovingAveragePortData(port,
					SimulinkConstants.TYPE_BACE_FIXED_MOVING_AVERAGE);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_MOVING_AVERAGE;
		}

	},

	/** Type for BACE FixpointProduct block */
	BACE_FIXPOINT_PRODUCT(SimulinkConstants.TYPE_BACE_FIXPOINT_PRODUCT) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFixPointPortLabel(port);
		}
	},

	/** Type for BACE Hysteresis block */
	BACE_HYSTERESIS(SimulinkConstants.TYPE_BACE_HYSTERESIS) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_HYSTERESIS_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceHysteresisPortData(port);
		}
	},

	/** Type for BACE IfThen block */
	BACE_IF_THEN(SimulinkConstants.TYPE_BACE_IF_THEN) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceIfThenPortData(port);
		}
	},

	/** Type for BACE IsFloat block */
	BACE_IS_FLOAT(SimulinkConstants.TYPE_BACE_IS_FLOAT) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_IS_FLOAT;
		}
	},

	/** Type for LowPass filter with constant parameters BACE block */
	BACE_LOWPASS_CONST(SimulinkConstants.TYPE_BACE_LOWPASS_CONST) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_LOWPASS_FILTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceLowPassConstPortData(port);
		}
	},

	/**
	 * Type for LowPass filter with constant parameters and initial value BACE block
	 */
	BACE_LOWPASS_CONST_IV(SimulinkConstants.TYPE_BACE_LOWPASS_CONST_IV) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_LOWPASS_FILTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceLowPassPortData(port);
		}
	},

	/**
	 * Type for LowPass filter with constant parameters and initial value (safe)
	 * BACE block
	 */
	BACE_LOWPASS_CONSTK_IV(SimulinkConstants.TYPE_BACE_LOWPASS_CONSTK_IV) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_LOWPASS_FILTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceLowPassConstPortData(port);
		}
	},

	/** Type for LowPass filter with initial value BACE block */
	BACE_LOWPASS_IV(SimulinkConstants.TYPE_BACE_LOWPASS_IV) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_LOWPASS_FILTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceLowPassPortData(port);
		}
	},

	/**
	 * Type for LowPass filter with initial value (unsafe) external BACE block
	 */
	BACE_LOWPASS_IV_EXTERNAL(SimulinkConstants.TYPE_BACE_LOWPASS_IV_EXTERNAL) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_LOWPASS_FILTER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceLowPassPortData(port);
		}
	},

	/** Type for BACE MatlabFunction block */
	BACE_MATLAB_FUNCTION(SimulinkConstants.TYPE_BACE_MATLAB_FUNCTION) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceMatlabFnPortData(port);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_MATLAB_FUNCTION;
		}
	},

	/** Type for MinMaxEpsilon BACE block */
	BACE_MIN_MAX_EPSILON(SimulinkConstants.TYPE_BACE_MIN_MAX_EPSILON) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.BACE_PARAM_MMV).toUpperCase().substring(0, 3);
		}
	},

	/** Type for Rampe BACE block */
	BACE_RAMPE(SimulinkConstants.TYPE_BACE_RAMPE) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_RAMPE_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port,
					Arrays.asList("IN1", "IN2", "INIT_C", "INIT_V", "RAMP2IN1", "RAMP", "i"), Arrays.asList("OUT"));
		}
	},

	/** Type for BACE RateLimiter block */
	BACE_RATE_LIMITER(SimulinkConstants.TYPE_BACE_RATE_LIMITER) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_RATE_LIMITER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port,
					Arrays.asList("grdUP", "U", "grdDN", "U_ini", "ini_cdn"), Arrays.asList("Y"));
		}
	},

	/** Type for SafeFloatCompare BACE block */
	BACE_SAFE_FLOAT_COMPARE(SimulinkConstants.TYPE_BACE_SAFE_FLOAT_COMPARE) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.PARAM_OPERATOR.toLowerCase());
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceSafeFloatComparePortData(port);
		}
	},

	/** Type for BACE SignalChangeDetector block */
	BACE_SIGNAL_CHANGE_DETECTOR(SimulinkConstants.BACE_TYPE_SIGNAL_CHANGE_DETECTOR) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_SIGNAL_CHANGE_DETECTOR;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.extractPortLabelForBlockPort(port, Arrays.asList("u", "i"),
					Arrays.asList("y"));
		}
	},

	/** Type for BACE SRFlipFlop block */
	BACE_SR_FLIP_FLOP(SimulinkConstants.BACE_TYPE_SR_FLIP_FLOP) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return block.getParameter(SimulinkConstants.BACE_PARAM_DOMINANCE) + "\ndominant";
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainFlipFlopPortData(port, Arrays.asList("S", "R", "i"));
		}
	},

	/** Type for BACE StaticAssertion block */
	BACE_STATIC_ASSERTION(SimulinkConstants.TYPE_BACE_STATIC_ASSERTION) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return "assert";
		}
	},

	/** Type for BACE Timer block */
	BACE_TIMER(SimulinkConstants.BACE_TYPE_TIMER) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_TIMER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceTimerPortData(port);
		}
	},

	/** Type for BACE TimerRetriggered block */
	BACE_TIMER_RETRIGGER(SimulinkConstants.BACE_TYPE_TIMER_RETRIGGERED) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_TIMER_RETRIGGER_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceTimerPortData(port);
		}
	},

	/** Type for BACE TrueConstant block */
	BACE_TRUE_CONSTANT(SimulinkConstants.TYPE_BACE_TRUE_CONSTANT) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.positionPortLabel(port, SimulinkConstants.VALUE_TRUE);
		}
	},

	/** Type for BACE Twice Bits Converter block */
	BACE_TWICE_BITS_CONVERTER(SimulinkConstants.TYPE_BACE_TWICE_BITS_CONVERTER) {
		@Override
		public String getBlockTextLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.TEXT_FOR_BACE_TWICE_BITS_CONVERTER;
		}
	},
	/** Type for BACE VariableInputDelay block */
	BACE_VARIABLE_INPUT_DELAY(SimulinkConstants.TYPE_BACE_VARIABLE_INPUT_DELAY) {
		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_FIXED_VARIABLE_DELAY_BLOCK;
		}

		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceFixedVariableDelayPortData(port);
		}
	},

	/** Type for BACE VariableMovingAverage block */
	BACE_VARIABLE_MOVING_AVERAGE(SimulinkConstants.TYPE_BACE_VARIABLE_MOVING_AVERAGE) {
		@Override
		public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
			return SimulinkPortLabelUtils.obtainBaceMovingAveragePortData(port,
					SimulinkConstants.TYPE_BACE_VARIABLE_MOVING_AVERAGE);
		}

		@Override
		public String getBlockIconLabel(SimulinkBlock block) {
			return SimulinkLabelOnBlockDataConstants.ICON_FOR_BACE_MOVING_AVERAGE;
		}
	},

	/** Default type for all other blocks. */
	DEFAULT;

	/**
	 * The SourceType for a Reference block
	 */
	private String sourceType;

	/**
	 * Determines the text to be displayed on the Simulink Block label depending on
	 * the ENUM block type.
	 */
	@SuppressWarnings("unused")
	public String getBlockTextLabel(SimulinkBlock block) {
		return null;
	}

	/**
	 * Determines the icon to be displayed on the Simulink Block label depending on
	 * the ENUM block type.
	 */
	@SuppressWarnings("unused")
	public String getBlockIconLabel(SimulinkBlock block) {
		return null;
	}

	/**
	 * Determines the port label data to be displayed on the Simulink Block port
	 * labels depending on the ENUM block type.
	 */
	@SuppressWarnings("unused")
	public LabelLayoutData getBlockPortLabels(SimulinkPortBase port) {
		return null;
	}

	/** Default constructor */
	private ESimulinkBlockType() {
		this.sourceType = null;
	}

	/** Constructor */
	private ESimulinkBlockType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns the ENUM block type corresponding to the given SourceType or
	 * SourceBlock if it exists. This method is used only with Reference blocks.
	 */
	public static ESimulinkBlockType getReferenceBlockEnumType(String sourceTypeValue, String sourceBlockValue) {
		for (ESimulinkBlockType blockType : values()) {
			String blockSourceType = blockType.sourceType;
			if (blockSourceType == null) {
				continue;
			}
			if (blockSourceType.equals(sourceTypeValue) || blockSourceType.equals(sourceBlockValue)) {
				return blockType;
			}
		}
		return ESimulinkBlockType.REFERENCE;
	}

}
