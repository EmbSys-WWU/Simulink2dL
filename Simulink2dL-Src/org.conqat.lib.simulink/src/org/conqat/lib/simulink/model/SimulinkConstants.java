/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
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
package org.conqat.lib.simulink.model;

/**
 * This class contains constants used by the Simulink model builder. These
 * constants are section and parameter names that refer to the MDL file. Section
 * and parameters are distinguished by the prefix of the constants (SECTION vs
 * PARAM). The remainder of the constant is just like the name in the MDL file.
 * We use mixed case here to express the case differences found in the MDL file,
 * e.g. 'Name' vs 'name'.
 */
public class SimulinkConstants {

	/** Color code red. */
	public static final String COLOR_RED = "red";

	/** Color code yellow. */
	public static final String COLOR_YELLOW = "yellow";

	/** Color code white. */
	public static final String COLOR_WHITE = "white";

	/** Color code green. */
	public static final String COLOR_GREEN = "green";

	/** Color code cyan. */
	public static final String COLOR_CYAN = "cyan";

	/** Color code blue. */
	public static final String COLOR_BLUE = "blue";

	/** Color code black. */
	public static final String COLOR_BLACK = "black";

	/** Color code orange. */
	public static final String COLOR_ORANGE = "orange";

	/** Color code light blue. */
	public static final String COLOR_LIGHT_BLUE = "lightBlue";

	/** Model section. */
	public static final String SECTION_MODEL = "Model";

	/** Model information. */
	public static final String SECTION_MODEL_INFORMATION = "ModelInformation";

	/** MatResources section. */
	public static final String SECTION_MAT_RESOURCES = "MatResources";

	/** MatData section. */
	public static final String SECTION_MAT_DATA = "MatData";

	/** Stateflow section. */
	public static final String SECTION_STATEFLOW = "Stateflow";

	/** Children section. */
	public static final String SECTION_CHILDREN = "Children";

	/** Library section. */
	public static final String SECTION_LIBRARY = "Library";

	/** Destination section in Stateflow transitions. */
	public static final String SECTION_DST = "dst";

	/** Source section in Stateflow transitions. */
	public static final String SECTION_SRC = "src";

	/** eml section in Stateflow transitions. */
	public static final String SECTION_EML = "eml";

	/** Transition section (Stateflow) */
	public static final String SECTION_TRANSITION = "transition";

	/** Junction section (Stateflow) */
	public static final String SECTION_JUNCTION = "junction";

	/** Event section (Stateflow) */
	public static final String SECTION_EVENT = "event";

	/** Data section (Stateflow) */
	public static final String SECTION_DATA = "data";

	/** Target section (Stateflow) */
	public static final String SECTION_TARGET = "target";

	/** Instance section (Stateflow) */
	public static final String SECTION_INSTANCE = "instance";

	/** State section (Stateflow) */
	public static final String SECTION_STATE = "state";

	/** Chart section (Stateflow) */
	public static final String SECTION_CHART = "chart";

	/** Machine section (Stateflow) */
	public static final String SECTION_MACHINE = "machine";

	/** Block diagram defaults section. */
	public static final String SECTION_BLOCK_DIAGRAM_DEFAULTS = "BlockDiagramDefaults";

	/** Block parameter defaults section. */
	public static final String SECTION_BLOCK_PARAMETER_DEFAULTS = "BlockParameterDefaults";

	/** Block defaults section. */
	public static final String SECTION_BLOCK_DEFAULTS = "BlockDefaults";

	/** System defaults section. */
	public static final String SECTION_SYSTEM_DEFAULTS = "SystemDefaults";

	/** Annotation defaults section. */
	public static final String SECTION_ANNOTATION_DEFAULTS = "AnnotationDefaults";

	/** Line defaults section. */
	public static final String SECTION_LINE_DEFAULTS = "LineDefaults";

	/** Block section. */
	public static final String SECTION_BLOCK = "Block";

	/** Array section. */
	public static final String SECTION_ARRAY = "Array";

	/** SimulationSettings section. */
	public static final String SECTION_SIMULATION_SETTINGS = "SimulationSettings";

	/** Parameter section. */
	public static final String SECTION_PARAMETER = "P";

	/** System section. */
	public static final String SECTION_SYSTEM = "System";

	/** Branch section. */
	public static final String SECTION_BRANCH = "Branch";

	/** Line section. */
	public static final String SECTION_LINE = "Line";

	/** Annotation section. */
	public static final String SECTION_ANNOTATION = "Annotation";

	/** Object section. */
	public static final String SECTION_OBJECT = "Object";

	/** Port section. */
	public static final String SECTION_PORT = "Port";

	/** InstanceData section. */
	public static final String SECTION_INSTANCE_DATA = "InstanceData";

	/** Name parameter. */
	public static final String PARAM_NAME = "Name";

	/** Ref parameter. */
	public static final String PARAM_REF = "Ref";

	/** Slx model name parameter. */
	public static final String PARAM_SLX_MODEL_NAME = "model_";

	/** Class parameter. */
	public static final String PARAM_CLASS = "Class";

	/** Tree node parameter for parent relationship (Stateflow) */
	public static final String PARAM_TREE_NODE = "treeNode";

	/** Link node parameter for parent relationship (Stateflow) */
	public static final String PARAM_LINK_NODE = "linkNode";

	/** State label (Stateflow) */
	public static final String PARAM_LABEL_STRING = "labelString";

	/** Junction type (Stateflow) */
	public static final String PARAM_TYPE = "type";

	/** Machine parameter (Stateflow) */
	public static final String PARAM_MACHINE = "machine";

	/** Icon display. */
	public static final String PARAM_ICON_DISPLAY = "IconDisplay";

	/** Icon shape. */
	public static final String PARAM_ICON_SHAPE = "IconShape";

	/** Id parameter (Stateflow) */
	public static final String PARAM_ID = "id";

	/** Inputs */
	public static final String PARAM_INPUTS = "Inputs";

	/** Name parameter (Stateflow) */
	public static final String PARAM_NAME_STATEFLOW = "name";

	/** Points (used for lines). */
	public static final String PARAM_POINTS = "Points";

	/** Intersection (used for stateflow transitions). */
	public static final String PARAM_INTERSECTION = "intersection";

	/** Position (of blocks). */
	public static final String PARAM_POSITION = "Position";

	/** Foreground color (of blocks) */
	public static final String PARAM_FOREGROUND_COLOR = "ForegroundColor";

	/** Background color (of blocks) */
	public static final String PARAM_BACKGROUND_COLOR = "BackgroundColor";

	/** Block type parameter. */
	public static final String PARAM_BLOCK_TYPE = "BlockType";

	/** SID parameter. */
	public static final String PARAM_SID = "SID";

	/** SSID parameter. */
	public static final String PARAM_SSID = "SSID";

	/** Subviewer parameter. */
	public static final String PARAM_SUBVIEWER = "subviewer";

	/** PropName parameter. */
	public static final String PARAM_PROP_NAME = "PropName";

	/** $ObjectID parameter. */
	public static final String PARAM_$OBJECT_ID = "$ObjectID";

	/** ObjectID parameter. */
	public static final String PARAM_OBJECT_ID = "ObjectID";

	/** ClassName parameter. */
	public static final String PARAM_CLASS_NAME = "ClassName";

	/** $ClassName parameter. */
	public static final String PARAM_$CLASS_NAME = "$ClassName";

	/** Dimension parameter. */
	public static final String PARAM_DIMENSION = "Dimension";

	/** Destination block parameter. */
	public static final String PARAM_DST_BLOCK = "DstBlock";

	/** Source parameter. */
	public static final String PARAM_SRC = "Src";

	/** Source port parameter. */
	public static final String PARAM_SRC_PORT = "SrcPort";

	/** Destination parameter. */
	public static final String PARAM_DST = "Dst";

	/** Destination port parameter. */
	public static final String PARAM_DST_PORT = "DstPort";

	/** Source block parameter. */
	public static final String PARAM_SRC_BLOCK = "SrcBlock";

	/** Ports parameter. */
	public static final String PARAM_PORTS = "Ports";

	/** Port parameter. */
	public static final String PARAM_PORT = "Port";

	/** Targetlink data parameter. */
	public static final String PARAM_TARGETLINK_DATA = "data";

	/** The parameter that specifies the referenced type for a reference. */
	public static final String PARAM_SOURCE_TYPE = "SourceType";

	/** Mask value string parameter */
	public static final String PARAM_MASK_VALUE_STRING = "MaskValueString";

	/** Mask variables parameter */
	public static final String PARAM_MASK_VARIABLES = "MaskVariables";

	/** Model name parameter. */
	public static final String PARAM_MODEL_NAME = "ModelName";

	/** Model name dialog parameter. */
	public static final String PARAM_MODEL_NAME_DIALOG = "ModelNameDialog";

	/** Port type parameter. */
	public static final String PARAM_PORT_TYPE = "PortType";

	/** Source block parameter. */
	public static final String PARAM_SOURCE_BLOCK = "SourceBlock";

	/** Value parameter. */
	public static final String PARAM_VALUE = "Value";

	/** Chart reference parameter. */
	public static final String PARAM_CHART = "chart";

	/** "Simulink mask parameter" parameter used in objects. */
	public static final String PARAM_SIMULINK_MASK_PARAMETER = "Simulink.MaskParameter";

	/** Simulink mask parameter. */
	public static final String PARAM_SIMULINK_MASK = "Simulink.Mask";

	/** Parameter storing the computed model version. */
	public static final String PARAM_COMPUTED_MODEL_VERSION = "ComputedModelVersion";

	/** Parameter storing the model's version. */
	public static final String PARAM_VERSION = "Version";

	/** Parameter for whether the name is shown. */
	public static final String PARAM_SHOW_NAME = "ShowName";

	/** Parameter for the name of the font used. */
	public static final String PARAM_FONT_NAME = "FontName";

	/** Parameter for the size of the font used. */
	public static final String PARAM_FONT_SIZE = "FontSize";

	/** Parameter for the weight of the font (i.e. bold). */
	public static final String PARAM_FONT_WEIGHT = "FontWeight";

	/** Parameter for the angle of the font (i.e. italic). */
	public static final String PARAM_FONT_ANGLE = "FontAngle";

	/** Parameter for the placement of the name label. */
	public static final String PARAM_NAME_PLACEMENT = "NamePlacement";

	/** Parameter for the labels of a line. */
	public static final String PARAM_LABELS = "Labels";

	/** Parameter for drop shadow. */
	public static final String PARAM_DROP_SHADOW = "DropShadow";

	/** Parameter for block mirroring. */
	public static final String PARAM_BLOCK_MIRROR = "BlockMirror";

	/** Parameter for block orientation. */
	public static final String PARAM_ORIENTATION = "Orientation";

	/** Parameter for block rotation. */
	public static final String PARAM_BLOCK_ROTATION = "BlockRotation";

	/** Parameter for port label visibility. */
	public static final String PARAM_SHOW_PORT_LABELS = "ShowPortLabels";

	/** Port number parameter. */
	public static final String PARAM_PORT_NUMBER = "PortNumber";

	/** Whether propagated signals should be shown. */
	public static final String PARAM_SHOW_PROPAGATED_SIGNALS = "ShowPropagatedSignals";

	/** The signals being propagated. */
	public static final String PARAM_PROPAGATED_SIGNALS = "PropagatedSignals";

	/** If expression. */
	public static final String PARAM_IF_EXPRESSION = "IfExpression";

	/** Else if expressions. */
	public static final String PARAM_ELSE_IF_EXPRESSIONS = "ElseIfExpressions";

	/** Conditions for switch/case. */
	public static final String PARAM_CASE_CONDITIONS = "CaseConditions";

	/** Parameter containing the delay length of the delay block */
	public static final String PARAM_DELAY_LENGTH = "DelayLength";

	/**
	 * Parameter containing the operator, e.g. of a relational operator block
	 */
	public static final String PARAM_OPERATOR = "Operator";

	/** Trigger type parameter. */
	public static final String PARAM_TRIGGER_TYPE = "TriggerType";

	/** Display parameter for mask object. */
	public static final String PARAM_DISPLAY = "Display";

	/** Parameter for horizontal alignment. */
	public static final String PARAM_HORIZONTAL_ALIGNMENT = "HorizontalAlignment";

	/** Parameter for vertical alignment. */
	public static final String PARAM_VERTICAL_ALIGNMENT = "VerticalAlignment";

	/** Parameter for interpreter of annotations. */
	public static final String PARAM_INTERPRETER = "Interpreter";

	/** Parameter containing the mask display for older models. */
	public static final String PARAM_MASK_DISPLAY = "MaskDisplay";

	/** Parameter containing the information displayed in Model Info Blocks */
	public static final String PARAM_MASK_DISPLAY_STRING = "MaskDisplayString";

	/** Parameter containing the information displayed in Gain blocks */
	public static final String PARAM_GAIN = "Gain";

	/** Parameter for library version. */
	public static final String PARAM_LIBRARY_VERSION = "LibraryVersion";

	/**
	 * Parameter containing information about switching criteria of a switch block
	 */
	public static final String PARAM_CRITERIA = "Criteria";

	/**
	 * Parameter containing information about the threshold of a switch block
	 */
	public static final String PARAM_THRESHOLD = "Threshold";

	/** Parameter of Delay block */
	public static final String PARAM_DELAY_LENGTH_SOURCE = "DelayLengthSource";

	/** Parameter for function name. */
	public static final String PARAM_IS_SIMULINK_FUNCTION = "IsSimulinkFunction";

	/** Parameter for function name. */
	public static final String PARAM_FUNCTION_NAME = "FunctionName";

	/** Parameter for Stateflow block type. */
	public static final String PARAM_SF_BLOCK_TYPE = "SFBlockType";

	/** Simulink block type 'Abs'. */
	public static final String TYPE_ABS = "Abs";

	/** Simulink block type 'Assertion'. */
	public static final String TYPE_ASSERTION = "Assertion";

	/** Simulink block type 'Assignment'. */
	public static final String TYPE_ASSIGNMENT = "Assignment";

	/** Simulink block type 'Backlash'. */
	public static final String TYPE_BACKLASH = "Backlash";

	/** Simulink block type 'Bias'. */
	public static final String TYPE_BIAS = "Bias";

	/** Simulink block type 'BusAssignment'. */
	public static final String TYPE_BUS_ASSIGNMENT = "BusAssignment";

	/** Simulink block type 'BusCreator'. */
	public static final String TYPE_BUS_CREATOR = "BusCreator";

	/** Simulink block type 'BusSelector'. */
	public static final String TYPE_BUS_SELECTOR = "BusSelector";

	/** Simulink block type 'Clock'. */
	public static final String TYPE_CLOCK = "Clock";

	/** Simulink block type 'CombinatorialLogic'. */
	public static final String TYPE_COMBINATORIAL_LOGIC = "CombinatorialLogic";

	/** Simulink block type 'ComplexToMagnitudeAngle'. */
	public static final String TYPE_COMPLEX_TO_MAGNITUDE_ANGLE = "ComplexToMagnitudeAngle";

	/** Simulink block type 'ComplexToRealImag'. */
	public static final String TYPE_COMPLEX_TO_REAL_IMAG = "ComplexToRealImag";

	/** Simulink block type 'Constant'. */
	public static final String TYPE_CONSTANT = "Constant";

	/** Simulink block type 'DataStoreMemory'. */
	public static final String TYPE_DATA_STORE_MEMORY = "DataStoreMemory";

	/** Simulink block type 'DataStoreRead'. */
	public static final String TYPE_DATA_STORE_READ = "DataStoreRead";

	/** Simulink block type 'DataStoreWrite'. */
	public static final String TYPE_DATA_STORE_WRITE = "DataStoreWrite";

	/** Simulink block type 'DataTypeConversion'. */
	public static final String TYPE_DATA_TYPE_CONVERSION = "DataTypeConversion";

	/** Simulink block type 'DeadZone'. */
	public static final String TYPE_DEAD_ZONE = "DeadZone";

	/** Simulink block type 'Demux'. */
	public static final String TYPE_DEMUX = "Demux";

	/** Simulink block type 'Derivative'. */
	public static final String TYPE_DERIVATIVE = "Derivative";

	/** Simulink block type 'DigitalClock'. */
	public static final String TYPE_DIGITAL_CLOCK = "DigitalClock";

	/** Simulink block type 'DiscreteFilter'. */
	public static final String TYPE_DISCRETE_FILTER = "DiscreteFilter";

	/** Simulink block type 'DiscreteIntegrator'. */
	public static final String TYPE_DISCRETE_INTEGRATOR = "DiscreteIntegrator";

	/** Simulink block type 'DiscretePulseGenerator'. */
	public static final String TYPE_DISCRETE_PULSE_INTEGRATOR = "DiscretePulseGenerator";

	/** Simulink block type 'DiscreteStateSpace'. */
	public static final String TYPE_DISCRETE_STATE_SPACE = "DiscreteStateSpace";

	/** Simulink block type 'DiscreteTransferFcn'. */
	public static final String TYPE_DISCRETE_TRANSFER_FCN = "DiscreteTransferFcn";

	/** Simulink block type 'DiscreteZeroPole'. */
	public static final String TYPE_DISCRETE_ZERO_POLE = "DiscreteZeroPole";

	/** Simulink block type 'Display'. */
	public static final String TYPE_DISPLAY = "Display";

	/** Simulink block type 'EnablePort'. */
	public static final String TYPE_ENABLE_PORT = "EnablePort";

	/** Simulink block type 'Fcn'. */
	public static final String TYPE_FCN = "Fcn";

	/** Simulink block type 'From'. */
	public static final String TYPE_FROM = "From";

	/** Simulink block type 'FromFile'. */
	public static final String TYPE_FROM_FILE = "FromFile";

	/** Simulink block type 'FromWorkspace'. */
	public static final String TYPE_FROM_WORKSPACE = "FromWorkspace";

	/** Simulink block type 'Gain'. */
	public static final String TYPE_GAIN = "Gain";

	/** Simulink block type 'Goto'. */
	public static final String TYPE_GOTO = "Goto";

	/** Simulink block type 'GotoTagVisibility'. */
	public static final String TYPE_GOTO_TAG_VISIBILITY = "GotoTagVisibility";

	/** Simulink block type 'Ground'. */
	public static final String TYPE_GROUND = "Ground";

	/** Simulink block type 'HitCross'. */
	public static final String TYPE_HIT_CROSS = "HitCross";

	/** Simulink block type 'InitialCondition'. */
	public static final String TYPE_INITIAL_CONDITION = "InitialCondition";

	/** Simulink block type 'Inport'. */
	public static final String TYPE_INPORT = "Inport";

	/** Simulink block type 'Integrator'. */
	public static final String TYPE_INTEGRATOR = "Integrator";

	/** Simulink block type 'Logic'. */
	public static final String TYPE_LOGIC = "Logic";

	/** Simulink block type 'Lookup'. */
	public static final String TYPE_LOOKUP = "Lookup";

	/** Simulink block type 'Lookup2D'. */
	public static final String TYPE_LOOKUP_2D = "Lookup2D";

	/** Simulink block type 'LookupND'. */
	public static final String TYPE_LOOKUP_ND = "Lookup_n-D";

	/** Simulink block type 'M-S-Function'. */
	public static final String TYPE_M_S_FUNCTION = "M-S-Function";

	/** Simulink block type 'MATLABFcn'. */
	public static final String TYPE_MATLAB_FCN = "MATLABFcn";

	/** Simulink block type 'MagnitudeAngleToComplex'. */
	public static final String TYPE_MAGNITUDE_ANGLE_TO_COMPLEX = "MagnitudeAngleToComplex";

	/** Simulink block type 'Math'. */
	public static final String TYPE_MATH = "Math";

	/** Simulink block type 'Memory'. */
	public static final String TYPE_MEMORY = "Memory";

	/** Simulink block type 'Merge'. */
	public static final String TYPE_MERGE = "Merge";

	/** Simulink block type 'Manual Switch'. */
	public static final String TYPE_MANUAL_SWITCH = "ManualSwitch";

	/** Simulink block type 'MinMax'. */
	public static final String TYPE_MIN_MAX = "MinMax";

	/** Simulink block type 'Model'. */
	public static final String TYPE_MODEL = "Model";

	/** Simulink block type 'MultiPortSwitch'. */
	public static final String TYPE_MULTI_PORT_SWITCH = "MultiPortSwitch";

	/** Simulink block type 'Mux'. */
	public static final String TYPE_MUX = "Mux";

	/** Simulink block type 'Outport'. */
	public static final String TYPE_OUTPORT = "Outport";

	/** Simulink block type 'Probe'. */
	public static final String TYPE_PROBE = "Probe";

	/** Simulink block type 'Product'. */
	public static final String TYPE_PRODUCT = "Product";

	/** Simulink block type 'Quantizer'. */
	public static final String TYPE_QUANTIZER = "Quantizer";

	/** Simulink block type 'RandomNumber'. */
	public static final String TYPE_RANDOM_NUMBER = "RandomNumber";

	/** Simulink block type 'RateLimiter'. */
	public static final String TYPE_RATE_LIMITER = "RateLimiter";

	/** Simulink block type 'RateTransition'. */
	public static final String TYPE_RATE_TRANSITION = "RateTransition";

	/** Simulink block type 'RealImagToComplex'. */
	public static final String TYPE_REAL_IMAG_TO_COMPLEX = "RealImagToComplex";

	/** Simulink block type 'Reference'. */
	public static final String TYPE_REFERENCE = "Reference";

	/** Simulink block type 'RelationalOperator'. */
	public static final String TYPE_RELATIONAL_OPERATOR = "RelationalOperator";

	/** Simulink block type 'Relay'. */
	public static final String TYPE_RELAY = "Relay";

	/** Simulink block type 'Rounding'. */
	public static final String TYPE_ROUNDING = "Rounding";

	/** Simulink block type 'S-Function'. */
	public static final String TYPE_S_FUNCTION = "S-Function";

	/** Simulink block type 'Saturate'. */
	public static final String TYPE_SATURATE = "Saturate";

	/** Simulink block type 'Selector'. */
	public static final String TYPE_SELECTOR = "Selector";

	/** Simulink block type 'SignalConversion'. */
	public static final String TYPE_SIGNAL_CONVERSION = "SignalConversion";

	/** Simulink block type 'SignalGenerator'. */
	public static final String TYPE_SIGNAL_GENERATOR = "SignalGenerator";

	/** Simulink block type 'SignalSpecification'. */
	public static final String TYPE_SIGNAL_SPECIFICATION = "SignalSpecification";

	/** Simulink block type 'Signum'. */
	public static final String TYPE_SIGNUM = "Signum";

	/** Simulink block type 'Sin'. */
	public static final String TYPE_SIN = "Sin";

	/** Simulink block type 'StateSpace'. */
	public static final String TYPE_STATE_SPACE = "StateSpace";

	/** Simulink block type 'Step'. */
	public static final String TYPE_STEP = "Step";

	/** Simulink block type 'Stop'. */
	public static final String TYPE_STOP = "Stop";

	/** Simulink block type 'SubSystem'. */
	public static final String SUBSYSTEM = "SubSystem";

	/** Simulink block type 'Sum'. */
	public static final String TYPE_SUM = "Sum";

	/** Simulink block type 'Switch'. */
	public static final String TYPE_SWITCH = "Switch";

	/** Simulink block type 'Terminator'. */
	public static final String TYPE_TERMINATOR = "Terminator";

	/** Simulink block type 'ToFile'. */
	public static final String TYPE_TO_FILE = "ToFile";

	/** Simulink block type 'ToWorkspace'. */
	public static final String TYPE_TO_WORKSPACE = "ToWorkspace";

	/** Simulink block type 'TransferFcn'. */
	public static final String TYPE_TRANSFER_FCN = "TransferFcn";

	/** Simulink block type 'TransportDelay'. */
	public static final String TYPE_TRANSPORT_DELAY = "TransportDelay";

	/** Simulink block type 'TriggerPort'. */
	public static final String TYPE_TRIGGER_PORT = "TriggerPort";

	/** Simulink block type 'Trigonometry'. */
	public static final String TYPE_TRIGONOMETRY = "Trigonometry";

	/** Simulink block type 'UniformRandomNumber'. */
	public static final String TYPE_UNIFORM_RANDOM_NUMBER = "UniformRandomNumber";

	/** Simulink block type 'UnitDelay'. */
	public static final String TYPE_UNIT_DELAY = "UnitDelay";

	/** Simulink block type 'VariableTransportDelay'. */
	public static final String TYPE_VARIABLE_TRANSPORT_DELAY = "VariableTransportDelay";

	/** Simulink block type 'Width'. */
	public static final String TYPE_WIDTH = "Width";

	/** Simulink block type 'ZeroOrderHold'. */
	public static final String TYPE_ZERO_ORDER_HOLD = "ZeroOrderHold";

	/** Simulink block type 'ZeroPole'. */
	public static final String TYPE_ZERO_POLE = "ZeroPole";

	/** Simulink port type 'trigger'. */
	public static final String TYPE_TRIGGER = "trigger";

	/** Simulink port type 'reset'. Written in upper case for some reason. */
	public static final String TYPE_RESET = "Reset";

	/** Simulink port type 'enable'. */
	public static final String TYPE_ENABLE = "enable";

	/** Simulink port type 'ifaction'. */
	public static final String TYPE_IFACTION = "ifaction";

	/** Simulink port type 'state'. */
	public static final String TYPE_STATE = "state";

	/** Simulink port type 'in'. */
	public static final String TYPE_IN = "in";

	/** Simulink port type 'out'. */
	public static final String TYPE_OUT = "out";

	/** Simulink type 'ModelReference'. */
	public static final String TYPE_MODEL_REFERENCE = "ModelReference";

	/** If type. */
	public static final String TYPE_IF = "If";

	/** Switch case type. */
	public static final String TYPE_SWITCH_CASE = "SwitchCase";

	/** Type of delay blocks */
	public static final Object TYPE_DELAY = "Delay";

	/** Type of function call split blocks. */
	public static final String TYPE_FUNCTION_CALL_SPLIT = "FunctionCallSplit";

	/** Value for "off". */
	public static final String VALUE_OFF = "off";

	/** Value for "on". */
	public static final String VALUE_ON = "on";

	/** Value for bold font. */
	public static final String VALUE_BOLD = "bold";

	/** Value for italic font. */
	public static final String VALUE_ITALIC = "italic";

	/** Value for alternate placement. */
	public static final String VALUE_ALTERNATE = "alternate";

	/** Value for port label visibility "none". */
	public static final String VALUE_NONE = "none";

	/** Value for port label visibility "FromPortIcon". */
	public static final String VALUE_FROM_PORT_ICON = "FromPortIcon";

	/** Value for object class of mask object". */
	public static final String VALUE_SIMULINK_MASK = "Simulink.Mask";

	/** Value for display of signal name. */
	public static final String VALUE_SIGNAL_NAME = "Signal name";

	/** Function call trigger type. */
	public static final String VALUE_FUNCTION_CALL = "function-call";

	/** Round shape (for {@link #PARAM_ICON_SHAPE}). */
	public static final String VALUE_SHAPE_ROUND = "round";

	/** The value signal name */
	public static final String VALUE_Signal_NAME = "Signal name";

	/** The value Port number */
	public static final String VALUE_Port_NUMBER = "Port number";

	/** Value Threshold */
	public static final String VALUE_THRESHOLD = "Threshold";

	/**
	 * constant for second input of switch block which is saved in the threshold but
	 * not displayed on labels on block
	 */
	public static final String VALUE_U2 = "u2";

	/** Dialog value of DelayLengthSource parameter */
	public static final String VALUE_DIALOG = "Dialog";

	/** Value for stateflow block type "chart". */
	public static final String VALUE_CHART = "Chart";

	/**
	 * Simulink block name 'Subsystem' (Used by target link for structuring
	 * synthesized blocks)
	 **/
	public static final String NAME_SUBSYSTEM = "Subsystem";

	/**
	 * Simulink block name 'DocBlock', which is a value of the parameter
	 * 'SourceType' of blocks having the type 'Reference'
	 */
	public static final String NAME_DOC_BLOCK = "DocBlock";

	/**
	 * Value of state parameter "type" that identifies AND-States (are displayed
	 * with dashed outline).
	 */
	public static final String AND_STATE_TYPE = "AND_STATE";

	/**
	 * The value "SUBCHART" of the parameter "superState".
	 */
	public static final String VALUE_SUBCHART = "SUBCHART";

	/** Parameter for Stateflow. */
	public static final String PARAM_SUPER_STATE = "superState";

	/** Value for function state types in Stateflow. */
	public static final String VALUE_FUNC_STATE = "FUNC_STATE";

	/** Parameter for Simulink function in Stateflow state. */
	public static final String PARAM_SIMULINK_IS_SIMULINK_FCN = "simulink.isSimulinkFcn";

	/** Parameter for Matlab function in Stateflow state (EML). */
	public static final String PARAM_EML_IS_EML = "eml.isEML";

	/** Parameter for Simulink block name in Stateflow state. */
	public static final String PARAM_SIMULINK_BLOCK_NAME = "simulink.blockName";

	/** Value "1". */
	public static final String VALUE_1 = "1";

	/** Value "0". */
	public static final String VALUE_0 = "0";

	/** Script parameter. */
	public static final String PARAM_SCRIPT = "script";

	/** Multiplication parameter */
	public static final String PARAM_MULTIPLICATION = "Multiplication";

	/** Value for Multiplication parameter */
	public static final String VALUE_MATRIX_MULTIPLICATION = "Matrix(*)";

	/** Value for "external" */
	public static final String VALUE_EXTERNAL = "external";

	/** FromWorkspace VariableName parameter */
	public static final String PARAM_VARIABLE_NAME = "VariableName";

	/** Goto Tag parameter */
	public static final String PARAM_GOTO_TAG = "GotoTag";

	/** ConversionOutput parameter for SignalConversion block */
	public static final String PARAM_CONVERSION_OUTPUT = "ConversionOutput";

	/** Value for ConversionOutput parameter */
	public static final String VALUE_SIGNAL_COPY = "Signal copy";

	/** Value for ConversionOutput parameter */
	public static final String VALUE_VIRTUAL_BUS = "Virtual bus";

	/** Value for ConversionOutput parameter */
	public static final String VALUE_NONVIRTUAL_BUS = "Nonvirtual bus";

	/** Integrator limit out parameter */
	public static final String PARAM_LIMIT_OUTPUT = "LimitOutput";

	/** Integrator wrap state parameter */
	public static final String PARAM_WRAP_STATE = "WrapState";

	/** ExternalReset parameter */
	public static final String PARAM_EXTERNAL_RESET = "ExternalReset";

	/** Value for integrator reset parameter */
	public static final String VALUE_RISING = "rising";

	/** Value for integrator reset parameter */
	public static final String VALUE_FALLING = "falling";

	/** Value for integrator reset parameter */
	public static final String VALUE_EITHER = "either";

	/** Value for integrator reset parameter */
	public static final String VALUE_RESET_LEVEL = "level";

	/** Value for integrator reset parameter */
	public static final String VALUE_RESET_LEVEL_HOLD = "level hold";

	/** Integrator InitialConditionSource parameter */
	public static final String PARAM_INITIAL_CONDITION_SOURCE = "InitialConditionSource";

	/** SourceType value for Reference block */
	public static final String TYPE_COMPARE_TO_CONSTANT = "Compare To Constant";

	/** SourceType value for Reference block */
	public static final String TYPE_COMPARE_TO_ZERO = "Compare To Zero";

	/** Parameter for Compare to Constant block */
	public static final String PARAM_RELOP = "relop";

	/** Parameter for Compare to Constant block */
	public static final String PARAM_CTC_CONST = "const";

	/** Parameter for SignalSpecification block */
	public static final String PARAM_OUT_DATA_TYPE = "OutDataTypeStr";

	/** Parameter for SignalSpecification block */
	public static final String PARAM_SIGNAL_TYPE = "SignalType";

	/** Parameter for SignalSpecification block */
	public static final String PARAM_SAMPLING_MODE = "SamplingMode";

	/** Value for "auto" */
	public static final String VALUE_AUTO = "auto";

	/** Value for "real" */
	public static final String VALUE_REAL = "real";

	/** Value for "complex" */
	public static final String VALUE_COMPLEX = "complex";

	/** Value for SamplingMode parameter */
	public static final String VALUE_SAMPLE_BASED = "Sample based";

	/** Value for SamplingMode parameter */
	public static final String VALUE_FRAME_BASED = "Frame based";

	/** Value for DataType parameter */
	public static final String VALUE_INHERIT_AUTO = "Inherit: auto";

	/** SourceType value for Reference block */
	public static final String TYPE_FN_CALL_GENERATOR = "Function-Call Generator";

	/** Parameter for ArithShift block */
	public static final String PARAM_BIT_SHIFT_NUM_SOURCE = "BitShiftNumberSource";

	/** Parameter for ArithShift block */
	public static final String PARAM_BIT_SHIFT_DIRECTION = "BitShiftDirection";

	/** Parameter for ArithShift block */
	public static final String PARAM_BIT_SHIFT_NUMBER = "BitShiftNumber";

	/** Parameter for ArithShift block */
	public static final String PARAM_BIN_PT_SHIFT_NUM = "BinPtShiftNumber";

	/** Value for ArithShift block source */
	public static final String VALUE_AS_INPUT_PORT = "Input port";

	/** Value for ArithShift block source */
	public static final String VALUE_AS_DIALOG = "Dialog";

	/** Parameter for Fn Call Generator block */
	public static final String PARAM_ITERATIONS = "numberOfIterations";

	/** SourceType value for Reference block */
	public static final String TYPE_BITWISE_OPERATOR = "Bitwise Operator";

	/** Logic operator parameter */
	public static final String PARAM_LOGIC_OPERATOR = "logicop";

	/** UseBitMask parameter for Bitwise operator block */
	public static final String PARAM_USE_BIT_MASK = "UseBitMask";

	/** BitMask parameter for Bitwise operator block */
	public static final String PARAM_BIT_MASK = "BitMask";

	/** BitMaskRealWorld parameter for Bitwise operator block */
	public static final String PARAM_BIT_MASK_REAL_WORLD = "BitMaskRealWorld";

	/** Stored Integer value for Bitwise operator block */
	public static final String VALUE_STORED_INTEGER = "Stored Integer";

	/** DisplayOption parameter for MUX and DEMUX blocks */
	public static final String MUX_DISPLAY_OPTION = "DisplayOption";

	/** Value for "bar" for Mux/Demux blocks */
	public static final String VALUE_BAR = "bar";

	/** Value for "signals" for Mux/Demux blocks */
	public static final String VALUE_SIGNALS = "signals";

	/** SourceType value for Reference block */
	public static final String TYPE_SATURATION_DYNAMIC = "Saturation Dynamic";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_RESETTABLE = "Unit Delay Resettable";

	/** Parameter for Assertion block */
	public static final String PARAM_ENABLED = "Enabled";

	/** Parameter for Step block */
	public static final String PARAM_BEFORE = "Before";

	/** Parameter for Step block */
	public static final String PARAM_AFTER = "After";

	/** SourceType value for Reference block */
	public static final String TYPE_INTERVAL_TEST_DYNAMIC = "Interval Test Dynamic";

	/** Parameter for IntervalTestDynamic block */
	public static final String PARAM_INTERVAL_CLOSED_RIGHT = "IntervalClosedRight";

	/** Parameter for IntervalTestDynamic block */
	public static final String PARAM_INTERVAL_CLOSED_LEFT = "IntervalClosedLeft";

	/** SourceType value for Reference block */
	public static final String TYPE_EXTRACT_BITS = "Extract Bits";

	/** Parameter for ExtractBits block */
	public static final String PARAM_BITS_TO_EXTRACT = "bitsToExtract";

	/** Parameter for ExtractBits block */
	public static final String PARAM_NUM_BITS_TO_EXTRACT = "numBits";

	/** Parameter for ExtractBits block */
	public static final String PARAM_BITS_RANGE = "bitIdxRange";

	/** Value for bitsToExtract parameter */
	public static final String VALUE_UPPER_HALF = "Upper half";

	/** Value for bitsToExtract parameter */
	public static final String VALUE_LOWER_HALF = "Lower half";

	/** Value for bitsToExtract parameter */
	public static final String VALUE_MSB = "Range starting with most significant bit";

	/** Value for bitsToExtract parameter */
	public static final String VALUE_LSB = "Range ending with least significant bit";

	/** Value for bitsToExtract parameter */
	public static final String VALUE_RANGE_OF_BITS = "Range of bits";

	/** Parameter for MultiPortSwitch block */
	public static final String PARAM_DATA_PORT_ORDER = "DataPortOrder";

	/** Value for DataPortOrder parameter */
	public static final String VALUE_DATA_PORT_ONE_BASED = "One-based contiguous";

	/** Value for DataPortOrder parameter */
	public static final String VALUE_DATA_PORT_ZERO_BASED = "Zero-based contiguous";

	/** Value for DataPortOrder parameter */
	public static final String VALUE_DATA_PORT_SPECIFY_INDICES = "Specify indices";

	/** Parameter for MultiPortSwitch block */
	public static final String PARAM_DATA_PORT_INDICES = "DataPortIndices";

	/** Parameter for saving the processed DataPortIndices */
	public static final String PARAM_DATA_PORT_INDICES_STRING = "DataPortIndicesString";

	/** Parameter for MultiPortSwitch block */
	public static final String PARAM_DATA_PORT_FOR_DEFAULT = "DataPortForDefault";

	/** Value for DataPortForDefault parameter */
	public static final String VALUE_LAST_DATA_PORT = "Last data port";

	/** Value for DataPortForDefault parameter */
	public static final String VALUE_ADDITIONAL_DATA_PORT = "Additional data port";

	/** Parameter for Display block */
	public static final String PARAM_DISPLAY_FORMAT = "Format";

	/** Value for "long" */
	public static final String VALUE_LONG = "long";

	/** Value for "short" */
	public static final String VALUE_SHORT = "short";

	/** Type for ForIterator block */
	public static final String TYPE_FOR_ITERATOR = "ForIterator";

	/** Parameter for ForIterator index mode */
	public static final String PARAM_INDEX_MODE = "IndexMode";

	/** Parameter for ForIterator iteration source */
	public static final String PARAM_ITERATION_SOURCE = "IterationSource";

	/** Parameter for ForIterator external increment */
	public static final String PARAM_EXTERNAL_INCREMENT = "ExternalIncrement";

	/** Value for "One-based" */
	public static final String VALUE_ONE_BASED = "One-based";

	/** Type for WhileIterator block */
	public static final String TYPE_WHILE_ITERATOR = "WhileIterator";

	/** Parameter for WhileIterator type */
	public static final String PARAM_WHILE_BLOCK_TYPE = "WhileBlockType";

	/** Value for WhileIterator block type */
	public static final String VALUE_DO_WHILE = "do-while";

	/** Parameter for NDLookup input select object */
	public static final String PARAM_INPUT_SELECT_OBJECT = "InputsSelectThisObjectFromTable";

	/** Parameter for NDLookup number of table dimensions */
	public static final String PARAM_TABLE_DIMENSIONS = "NumberOfTableDimensions";

	/** Value for NDLookup input select object */
	public static final String VALUE_COLUMN = "Column";

	/** Value for NDLookup input select object */
	public static final String VALUE_2D_MATRIX = "2-D Matrix";

	/** Parameter for NDLookup whether table is input */
	public static final String PARAM_TABLE_IS_INPUT = "TableIsInput";

	/** Parameter for PreLookup block */
	public static final String PARAM_BREAK_POINTS_DATA_SRC = "BreakpointsDataSource";

	/** Parameter for PreLookup block */
	public static final String PARAM_OUTPUT_ONLY_INDEX = "OutputOnlyTheIndex";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_EXTERNAL_IC = "Unit Delay External Initial Condition";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_ENABLED = "Unit Delay Enabled";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_ENABLED_RESETTABLE = "Unit Delay Enabled Resettable";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_ENABLED_RESETTABLE_EXTERNAL_IC = "Unit Delay Enabled Resettable External Initial Condition";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_ENABLED_EXTERNAL_IC = "Unit Delay Enabled External Initial Condition";

	/** SourceType value for Reference block */
	public static final String TYPE_UNIT_DELAY_RESETTABLE_EXTERNAL_IC = "Unit Delay Resettable External Initial Condition";

	/** Function parameter */
	public static final String PARAM_FUNCTION = "Function";

	/** SourceType value for Reference block */
	public static final String TYPE_ALGEBRAIC_CONSTRAINT = "Algebraic Constraint";

	/** SourceType value for Reference block */
	public static final String TYPE_MIN_MAX_RUNNING_RESETTABLE = "MinMax Running Resettable";

	/** Operator parameter for SampleTimeMath block */
	public static final String PARAM_SAMPLE_TIME_MATH_OPERATOR = "TsampMathOp";

	/** WeightValue parameter for SampleTimeMath block */
	public static final String PARAM_SAMPLE_TIME_MATH_WEIGHT_VALUE = "weightValue";

	/** Value for SampleMathTime operator parameter */
	public static final String VALUE_TS_ONLY = "Ts Only";

	/** Value for SampleMathTime operator parameter */
	public static final String VALUE_1_TS_ONLY = "1/Ts Only";

	/** Input parameter */
	public static final String PARAM_INPUT = "Input";

	/** Output parameter */
	public static final String PARAM_OUTPUT = "Output";

	/** Value for "Magnitude" */
	public static final String VALUE_MAGNITUDE = "Magnitude";

	/** Value for "Angle" */
	public static final String VALUE_ANGLE = "Angle";

	/** Value for "Magnitude and angle" */
	public static final String VALUE_MAGNITUDE_AND_ANGLE = "Magnitude and angle";

	/** Value for "Imag" */
	public static final String VALUE_IMAG = "Imag";

	/** Value for "Real and imag" */
	public static final String VALUE_REAL_AND_IMAG = "Real and imag";

	/** SourceType value for Reference block */
	public static final String TYPE_SLIDER_GAIN = "Slider Gain";

	/** Value for Reciprocal Sqrt */
	public static final String VALUE_RSQRT = "rSqrt";

	/** Value for Reciprocal Sqrt */
	public static final String VALUE_SIGNED_SQRT = "signedSqrt";

	/** Value for "log" (Math block) */
	public static final String VALUE_LOG = "log";

	/** Value for "10^u" (Math block) */
	public static final String VALUE_10U = "10^u";

	/** Value for "log10" (Math block) */
	public static final String VALUE_LOG10 = "log10";

	/** Value for "magnitude^2" (Math block) */
	public static final String VALUE_MAGNITUDE2 = "magnitude^2";

	/** Value for "square" (Math block) */
	public static final String VALUE_SQUARE = "square";

	/** Value for "pow" (Math block) */
	public static final String VALUE_POW = "pow";

	/** Value for "conj" (Math block) */
	public static final String VALUE_CONJ = "conj";

	/** Value for "reciprocal" (Math block) */
	public static final String VALUE_RECIPROCAL = "reciprocal";

	/** Value for "hypot" (Math block) */
	public static final String VALUE_HYPOT = "hypot";

	/** Value for "rem" (Math block) */
	public static final String VALUE_REM = "rem";

	/** Value for "mod" (Math block) */
	public static final String VALUE_MOD = "mod";

	/** Value for "transpose" (Math block) */
	public static final String VALUE_TRANSPOSE = "transpose";

	/** Value for "hermitian" (Math block) */
	public static final String VALUE_HERMITIAN = "hermitian";

	/** Value for "exp" (Math block) */
	public static final String VALUE_EXP = "exp";

	/** OutputDimensionality parameter for Reshape block */
	public static final String PARAM_OUTPUT_DIMENSIONALITY = "OutputDimensionality";

	/** OutputDimensionality value for Reshape block */
	public static final String VALUE_1D_ARRAY = "1-D array";

	/** OutputDimensionality value for Reshape block */
	public static final String VALUE_2D_COLUMN = "Column vector (2-D)";

	/** OutputDimensionality value for Reshape block */
	public static final String VALUE_2D_ROW = "Row vector (2-D)";

	/** OutputDimensionality value for Reshape block */
	public static final String VALUE_CUSTOMIZE = "Customize";

	/** OutputDimensionality value for Reshape block */
	public static final String VALUE_DERIVE_FROM_REF = "Derive from reference input port";

	/** SourceType value for Reference block */
	public static final String TYPE_DETECT_CHANGE = "Detect Change";

	/** SourceType value for Reference block */
	public static final String TYPE_DEAD_ZONE_DYNAMIC = "Dead Zone Dynamic";

	/** SourceType value for Reference block */
	public static final String TYPE_RAMP = "Ramp";

	/** SourceType value for Reference block */
	public static final String TYPE_CONVERSION_INHERITED = "Conversion Inherited";

	/** SourceType value for Reference block */
	public static final String TYPE_DATA_TYPE_PROPAGATION = "Data Type Propagation";

	/** SourceType value for Reference block */
	public static final String TYPE_BAND_LIMITED_NOISE = "Band-Limited White Noise.";

	/** SourceType value for Reference block */
	public static final String TYPE_COUNTER_FREE_RUNNING = "Counter Free-Running";

	/** CurrentSetting parameter for ManualSwitch block */
	public static final String PARAM_CURRENT_SETTING = "CurrentSetting";

	/** PulseType parameter for DiscretePulseGenerator block */
	public static final String PARAM_PULSE_TYPE = "PulseType";

	/** IndexOptions parameter for Assignment block */
	public static final String PARAM_INDEX_OPTIONS = "IndexOptions";

	/** PortIndices parameter for Assignment block */
	public static final String PARAM_ASSIGNMENT_PORT_INDICES = "PortIndices";

	/** NumberOfDimensions parameter for Assignment block */
	public static final String PARAM_NUMBER_OF_DIMENSIONS = "NumberOfDimensions";

	/** OutputInitialize parameter for Assignment block */
	public static final String PARAM_OUTPUT_INITIALIZE = "OutputInitialize";

	/** InputPortMap parameter for Delay block */
	public static final String PARAM_INPUT_PORT_MAP = "InputPortMap";

	/** Parameter for the processed Case Conditions for SwitchCase block */
	public static final String PARAM_PROCESSED_CASE_CONDITIONS = "ProcessedCaseConditions";

	/** Order parameter for PermuteDimensions block */
	public static final String PARAM_ORDER = "Order";

	/** Mode parameter for Concatenate block */
	public static final String PARAM_MODE = "Mode";

	/** Value for Mode parameter for Concatenate block */
	public static final String VALUE_MODE_MATRIX = "Multidimensional array";

	/** ConvertRealWorld parameter for DataTypeConversion block */
	public static final String PARAM_CONVERT_REAL_WORLD = "ConvertRealWorld";

	/** Parameter for storing an arbitrary block's width */
	public static final String PARAM_BLOCK_WIDTH = "BlockWidth";

	/** OverrideUsingVariant parameter for Subsystem block */
	public static final String PARAM_OVERRIDE_VARIANT = "OverrideUsingVariant";

	/** Variant parameter for Subsystem block */
	public static final String PARAM_VARIANT = "Variant";

	/** VariantControl parameter for Subsystem block */
	public static final String PARAM_VARIANT_CONTROL = "VariantControl";

	/** ActionType parameter for ActionPort block */
	public static final String PARAM_ACTION_TYPE = "ActionType";

	/** Type for BACE CountDown block */
	public static final String BACE_TYPE_COUNT_DOWN = "CountDownResetEnabled";

	/** Type for BACE Counter block */
	public static final String BACE_TYPE_COUNTER = "CounterResetEnabled";

	/** Type for BACE Timer block */
	public static final String BACE_TYPE_TIMER = "TimerResetEnabled";

	/** Type for BACE TimerRetriggered block */
	public static final String BACE_TYPE_TIMER_RETRIGGERED = "TimerRetriggerResetEnabled";

	/** Type for BACE Mask DT block */
	public static final String BACE_TYPE_MASK_DT = "BACE_Mask_dt";

	/** Value for group states. */
	public static final String VALUE_GROUP_STATE = "GROUP_STATE";

	/** Value for note box. */
	public static final String PARAM_isNoteBox = "isNoteBox";

	/** Type for FixedStepDelay BACE block */
	public static final String TYPE_BACE_FIXED_STEP_DELAY = "BACE_FixedStepDelay";

	/** Type for Hysteresis BACE block */
	public static final String TYPE_BACE_HYSTERESIS = "Hysteresis_LSP_RSP";

	/** Type for VariableInputDelay BACE block */
	public static final String TYPE_BACE_VARIABLE_INPUT_DELAY = "VariableInputDelay";

	/** Type for TurnOnDelay BACE block */
	public static final String TYPE_BACE_TURN_ON_DELAY = "TurnOnDelay";

	/** Type for TurnOffDelay BACE block */
	public static final String TYPE_BACE_TURN_OFF_DELAY = "TurnOffDelay";

	/** Type for FixedMovingAverage BACE block */
	public static final String TYPE_BACE_FIXED_MOVING_AVERAGE = "FixedMovingAverage";

	/** Type for VariableMovingAverage BACE block */
	public static final String TYPE_BACE_VARIABLE_MOVING_AVERAGE = "variable moving average";

	/** Value for Debounce BACE block */
	public static final String VALUE_BACE_DEBOUNCE = "Debounce";

	/** Value for UnitDelay BACE block */
	public static final String VALUE_BACE_UNIT_DELAY = "BACE_UnitDelay";

	/** Type for ClosedInterval BACE block */
	public static final String TYPE_BACE_CLOSED_INTERVAL = "BACE_ClosedInterval";

	/** Type for RateLimiter BACE block */
	public static final String TYPE_BACE_RATE_LIMITER = "BACE_RateLimiter";

	/** Type for Fader BACE block */
	public static final String TYPE_BACE_FADER = "BACE Fader";

	/** Type for Fader with Constant Parameter BACE block */
	public static final String TYPE_BACE_FADER_CONST = "BACE Fader with Constant Parameter";

	/** Type for LowPass filter with initial value BACE block */
	public static final String TYPE_BACE_LOWPASS_IV = "BACE LowPass_IV";

	/**
	 * Type for LowPass filter with initial value (unsafe) external BACE block
	 */
	public static final String TYPE_BACE_LOWPASS_IV_EXTERNAL = "BACE LowPass (unsafe) external";

	/** Type for LowPass filter with constant parameters BACE block */
	public static final String TYPE_BACE_LOWPASS_CONST = "BACE LowPass with Constant Parameters (safe)";

	/**
	 * Type for LowPass filter with constant parameters and initial value BACE block
	 */
	public static final String TYPE_BACE_LOWPASS_CONST_IV = "BACE_LowPass_constIV";

	/**
	 * Type for LowPass filter with constant parameters and initial value (safe)
	 * BACE block
	 */
	public static final String TYPE_BACE_LOWPASS_CONSTK_IV = "BACE LowPass with Constant Parameters  and initial value (safe)";

	/** Type for Rampe BACE block */
	public static final String TYPE_BACE_RAMPE = "rampe timebased";

	/** Type for DFlipFLop block */
	public static final String TYPE_D_FLIP_FLOP = "DFlipFlop";

	/** Type for JKFlipFLop block */
	public static final String TYPE_JK_FLIP_FLOP = "JKFlipFlop";

	/** Type for SRFlipFLop block */
	public static final String TYPE_SR_FLIP_FLOP = "SRFlipFlop";

	/** Type for DLatch block */
	public static final String TYPE_D_LATCH = "DLatch";

	/** Type for Extras Digital clock block (Different from DigitalClock) */
	public static final String TYPE_FLIP_FLOP_DIGITAL_CLOCK = "Digital clock";

	/** Type for BACE SRFlipFLop block */
	public static final String BACE_TYPE_SR_FLIP_FLOP = "BACE_SR_FlipFlop";

	/** Dominance parameter for BACE SRFlipFLop block */
	public static final String BACE_PARAM_DOMINANCE = "dominance";

	/** Type for BACE SignalChangeDetector block */
	public static final String BACE_TYPE_SIGNAL_CHANGE_DETECTOR = "BACE SignalChangeDetector";

	/** Type for BACE CertifiedSubsystem block */
	public static final String BACE_TYPE_CERTIFIED_SUBSYSTEM = "BACE_CertifiedSubsystem";

	/** Type for ForEach block */
	public static final String TYPE_FOR_EACH = "ForEach";

	/** Type for BMW FunctionCallGenerator block */
	public static final String TYPE_BMW_FUNCTION_CALL_GENERATOR = "BMW_FunctionCallGenerator";

	/** NKW parameter for BMW FunctionCallGenerator block */
	public static final String PARAM_NKW = "nkw";

	/** Type for EnumeratedConstant block */
	public static final String TYPE_ENUMERATED_CONSTANT = "Enumerated Constant";

	/** Type for MinMaxEpsilon BACE block */
	public static final String TYPE_BACE_MIN_MAX_EPSILON = "Datatype MinMaxEpsilon";

	/** mmV parameter for BACE MinMaxEpsilon block */
	public static final String BACE_PARAM_MMV = "mmV";

	/** Value for "true" */
	public static final String VALUE_TRUE = "true";

	/** Value for "false" */
	public static final String VALUE_FALSE = "false";

	/** Type for TrueConstant BACE block */
	public static final String TYPE_BACE_TRUE_CONSTANT = "true_mask";

	/** Type for FalseConstant BACE block */
	public static final String TYPE_BACE_FALSE_CONSTANT = "false_mask";

	/** AssignedSignals parameter for BusAssignment block */
	public static final String PARAM_ASSIGNED_SIGNALS = "AssignedSignals";

	/** Indices parameter for Selector block */
	public static final String PARAM_INDICES = "Indices";

	/** InputPortWidth parameter for Selector block */
	public static final String PARAM_INPUT_PORT_WIDTH = "InputPortWidth";

	/** Type for FeedbackLatch BACE block */
	public static final String TYPE_BACE_FEEDBACK_LATCH = "BACE_FeedbackLatch";

	/** Type for EdgeBi BACE block */
	public static final String TYPE_BACE_EDGE_BI = "Edge_Bi";

	/** Type for EdgeRising BACE block */
	public static final String TYPE_BACE_EDGE_RISING = "Edge_Rising";

	/** Type for EdgeFalling BACE block */
	public static final String TYPE_BACE_EDGE_FALLING = "Edge_Falling";

	/** Type for BitClear block */
	public static final String TYPE_BIT_CLEAR = "Bit Clear";

	/** Type for BitSet block */
	public static final String TYPE_BIT_SET = "Bit Set";

	/** iBit parameter for BitClear and BitSet blocks */
	public static final String PARAM_IBIT = "iBit";

	/** Type for BitwiseNot BACE block */
	public static final String TYPE_BACE_BITWISE_NOT = "Bitwise NOT with bitmask";

	/** Type for IfThen BACE block */
	public static final String TYPE_BACE_IF_THEN = "If Then";

	/** Type for SafeFloatCompare BACE block */
	public static final String TYPE_BACE_SAFE_FLOAT_COMPARE = "safe_float_compare";

	/** Type for BitShift BACE block */
	public static final String TYPE_BACE_BIT_SHIFT = "BACE_BitShift/BitShift";

	/** Type for DivByZero BACE block */
	public static final String TYPE_BACE_DIV_BY_ZERO = "BACE DivBy0Prot";

	/** UseEpsilonComparison parameter for DivByZero BACE block */
	public static final String PARAM_BACE_USE_EPS = "UseEpsilonComparison";

	/** Type for FixpointProduct BACE block */
	public static final String TYPE_BACE_FIXPOINT_PRODUCT = "BACE_Fixpoint_Product/Safe Fixpoint Product";

	/** Type for FixpointGain BACE block */
	public static final String TYPE_BACE_GAIN = "BACE_Fixpoint_Gain";

	/** Type for Integrator ASAM BACE block */
	public static final String TYPE_BACE_INTEGRATOR_ASAM = "BACE_Integrator_ASAM/Integrator_ASAM";

	/** Type for Integrator Simple ASAM BACE block */
	public static final String TYPE_BACE_INTEGRATOR_ASAM_SIMPLE = "BACE_Simple_Integrator_ASAM/Simple_Integrator_ASAM";

	/** Type for Integrator ASAM_PAR BACE block */
	public static final String TYPE_BACE_INTEGRATOR_ASAM_PAR = "BACE_Integrator_ASAM_PAR_IV/Integrator_ASAM_PAR_IV";

	/** Type for Integrator ASAM_EXT BACE block */
	public static final String TYPE_BACE_INTEGRATOR_ASAM_EXT = "BACE_Integrator_ASAM_ext/Integrator_ASAM_ext";

	/** Type for StaticAssertion BACE block */
	public static final String TYPE_BACE_STATIC_ASSERTION = "BACE_StaticAssertion";

	/** Type for IsFloat BACE block */
	public static final String TYPE_BACE_IS_FLOAT = "BACE_is_float";

	/** Type for ScalingStrip block */
	public static final String TYPE_SCALING_STRIP = "Scaling Strip";

	/** Type for BACE Twice Bits Converter block */
	public static final String TYPE_BACE_TWICE_BITS_CONVERTER = "Twice Bits Same Range Converter";

	/** Numerator Source parameter for discrete filter blocks */
	public static final String PARAM_NUMERATOR_SOURCE = "NumeratorSource";

	/** Denominator Source parameter for discrete filter blocks */
	public static final String PARAM_DENOMINATOR_SOURCE = "DenominatorSource";

	/** Numerator parameter for discrete filter blocks */
	public static final String PARAM_NUMERATOR = "Numerator";

	/** Denominator parameter for discrete filter blocks */
	public static final String PARAM_DENOMINATOR = "Denominator";

	/** Initial States Source parameter for discrete filter blocks */
	public static final String PARAM_INITIAL_STATES_SOURCE = "InitialStatesSource";

	/** Coefficient Source parameter for DiscreteFir blocks= */
	public static final String PARAM_COEFFICIENT_SOURCE = "CoefSource";

	/** Function Input parameter */
	public static final String PARAM_FUNCTION_INPUT = "FunctionInput";

	/** Function Output parameter */
	public static final String PARAM_FUNCTION_OUTPUT = "FunctionOutput";

	/** Type for BACE Matlab function block */
	public static final String TYPE_BACE_MATLAB_FUNCTION = "BACELib_Userdefined/MATLAB Function";

	/** Trigger type parameter for ResetPort block */
	public static final String PARAM_RESET_TRIGGER_TYPE = "ResetTriggerType";

	/** Type for ResetPort block. It's different than the Reset PortType! */
	public static final String TYPE_RESET_PORT_BLOCK = "ResetPort";

	/** Type for EventListener block */
	public static final String TYPE_EVENT_LISTENER_TYPE = "EventListener";

	/** EventType parameter for EventListener block */
	public static final String PARAM_EVENT_LISTENER_TYPE = "EventType";

	/** EventName parameter for EventListener block */
	public static final String PARAM_EVENT_LISTENER_NAME = "EventName";

	/** StateOwnerBlock parameter for StateReader and StateWriter blocks */
	public static final String PARAM_STATE_OWNER_BLOCK = "StateOwnerBlock";

	/** StateIdentifier parameter for Discrete-Time Integrator block */
	public static final String PARAM_STATE_IDENTIFIER = "StateIdentifier";

	/** StateName parameter for Discrete-Time Integrator block */
	public static final String PARAM_STATE_NAME = "StateName";

	/** VariantControls parameter for VariantSink and VariantSource blocks */
	public static final String PARAM_VARIANT_CONTROLS = "VariantControls";

	/** Parameter for proc name */
	public static final String PARAM_PROC_NAME = "procName";
}