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
package org.conqat.lib.simulink.builder;

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BLOCK_TYPE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ANNOTATION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ANNOTATION_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ARRAY;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK_PARAMETER_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_LINE_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_OBJECT;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SYSTEM;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.model.stateflow.StateflowBlock;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This class is responsible for building the Simulink part (as opposed to
 * Stateflow) of the models. It delegates some building tasks to sub builders.
 * 
 * @see SimulinkLineBuilder
 * @see SimulinkPortBuilder
 */
/* package */class SimulinkBuilder {

	/** Builder for Simulink lines. */
	private final SimulinkLineBuilder lineBuilder;

	/** Builder for Simulink ports. */
	private final SimulinkPortBuilder portBuilder = new SimulinkPortBuilder();

	/** The model to build. */
	private final SimulinkModel model;

	/** Create new builder. */
	public SimulinkBuilder(SimulinkModel model, ModelBuildingParameters parameters, boolean isSlx) {
		this.model = model;
		lineBuilder = new SimulinkLineBuilder(parameters, isSlx);
	}

	/**
	 * Builds the simulink parts of the model (i.e. blocks, parameters, etc.).
	 * 
	 * @param modelSection
	 *            the 'Model' section of the MDL file.
	 * @throws SimulinkModelBuildingException
	 *             if a parsing error occurred.
	 */
	public void buildSimulink(MDLSection modelSection) throws SimulinkModelBuildingException {
		// Order of calls is important given that the default parameters might
		// be needed while building the blocks.
		buildBlockTypeDefaultParams(modelSection);
		buildBlockDefaultParams(modelSection);
		buildLineDefaultParams(modelSection);
		buildAnnotationDefaultParams(modelSection);
		buildSimulinkBlocks(modelSection, model);
	}

	/**
	 * Get 'block' sub section of the specified sections and call
	 * {@link #buildSimulinkBlock(MDLSection, SimulinkBlock)} for each of them.
	 */
	private void buildSimulinkBlocks(MDLSection section, SimulinkBlock parent) throws SimulinkModelBuildingException {
		MDLSection systemSection = section.getFirstSubSection(SECTION_SYSTEM);
		if (systemSection == null) {
			return;
		}
		List<MDLSection> blocks = systemSection.getSubSections(SECTION_BLOCK);
		for (MDLSection block : blocks) {
			buildSimulinkBlock(block, parent);
		}

		// we trash system and block sections here to free space for GC when building
		// huge models
		section.removeSubSections(SECTION_SYSTEM);
		section.removeSubSections(SECTION_BLOCK);

		lineBuilder.buildLines(systemSection, parent);
		buildAnnotations(systemSection, parent);
	}

	/**
	 * Build Simulink block.
	 * 
	 * @param section
	 *            MDL section that describes the block
	 * @param parent
	 *            parent block
	 * @throws SimulinkModelBuildingException
	 *             if an error occurs
	 */
	private void buildSimulinkBlock(MDLSection section, SimulinkBlock parent) throws SimulinkModelBuildingException {

		if (section.getParameter(SimulinkConstants.PARAM_NAME) == null) {
			throw new SimulinkModelBuildingException("Block at line " + section.getLineNumber() + " has no name.");
		}

		if (section.getParameter(SimulinkConstants.PARAM_BLOCK_TYPE) == null) {
			throw new SimulinkModelBuildingException("Block at line " + section.getLineNumber() + " has no type.");
		}

		SimulinkBlock simulinkBlock = createBlock(section, parent);

		SimulinkModelBuilder.addParameters(simulinkBlock, section);
		portBuilder.buildPorts(simulinkBlock, section);

		parent.addSubBlock(simulinkBlock);

		// if this block contains a System section also add those parameters
		MDLSection systemSection = section.getFirstSubSection(SECTION_SYSTEM);
		if (systemSection != null) {
			for (String paramName : systemSection.getParameterNames()) {
				simulinkBlock.setParameter(SECTION_SYSTEM + "." + paramName, systemSection.getParameter(paramName));
			}
		}

		MDLSection instanceDataSection = section.getFirstSubSection(SimulinkConstants.SECTION_INSTANCE_DATA);
		if (instanceDataSection != null) {
			for (String paramName : instanceDataSection.getParameterNames()) {
				simulinkBlock.setParameter(paramName, instanceDataSection.getParameter(paramName));
			}
		}

		createCustomParameters(simulinkBlock);

		buildObjects(section, simulinkBlock);
		buildSimulinkBlocks(section, simulinkBlock);
	}

	/**
	 * Some blocks require special processing of block information such as
	 * performing calculations, parsing expressions, formatting strings, etc. This
	 * method processes such data and saves it in a parameter to be later retrieved
	 * directly.
	 */
	private static void createCustomParameters(SimulinkBlock block) {
		String blockType = block.getType();
		if (SimulinkConstants.TYPE_MULTI_PORT_SWITCH.equals(blockType)) {
			processInputSignalsList(block, block.getParameter(SimulinkConstants.PARAM_DATA_PORT_INDICES),
					SimulinkConstants.PARAM_DATA_PORT_INDICES_STRING, true);
		} else if (SimulinkConstants.TYPE_ASSIGNMENT.equals(blockType)
				|| SimulinkConstants.TYPE_SELECTOR.equals(blockType)) {
			processIndexOptions(block);
		} else if (SimulinkConstants.TYPE_SWITCH_CASE.equals(blockType)) {
			processInputSignalsList(block, block.getParameter(SimulinkConstants.PARAM_CASE_CONDITIONS),
					SimulinkConstants.PARAM_PROCESSED_CASE_CONDITIONS, false);
		} else if ("FunctionCaller".equals(blockType)) {
			processFunctionArguments(block, block.getParameter("FunctionPrototype"));
		}
	}

	/**
	 * Processes the function string and saves it into 3 parameters: FunctionName,
	 * FunctionInput and FunctionOutput.
	 */
	private static void processFunctionArguments(SimulinkBlock block, String functionPrototype) {
		String[] arguments = functionPrototype.split("\\s*=\\s*", 2);
		String output = "";
		String call = functionPrototype;
		if (arguments.length > 1) {
			output = arguments[0].replaceAll("[\\]\\[\\s]", "");
			call = arguments[1];
		}

		Matcher m = Pattern.compile("\\((.*)\\)").matcher(call);
		String inputs = "";
		if (m.find()) {
			inputs = m.group(1);
		}
		String functionName = call.replaceAll(inputs, "");
		block.setParameter(SimulinkConstants.PARAM_FUNCTION_NAME, functionName);
		block.setParameter(SimulinkConstants.PARAM_FUNCTION_INPUT, inputs);
		block.setParameter(SimulinkConstants.PARAM_FUNCTION_OUTPUT, output);
	}

	/**
	 * Processes the list of IndexOptions of the Assignment block and saves the
	 * indices which will be used as a port label to a parameter in the format of a
	 * concatenated string of values separated by a white space. For ex. a list of
	 * options <"Index vector (dialog),Starting index (port),Index vector
	 * (port),Index vector (dialog)"> will be saved as "2 3".
	 */
	private static void processIndexOptions(SimulinkBlock block) {
		String indexOptions = block.getParameter(SimulinkConstants.PARAM_INDEX_OPTIONS);
		String resultingIndices = "";
		if (indexOptions != null) {
			String[] splitIndexOptions = indexOptions.split(",");
			for (int i = 0; i < splitIndexOptions.length; i++) {
				if (splitIndexOptions[i].contains("port")) {
					resultingIndices += (i + 1) + " ";
				}
			}
		}
		block.setParameter(SimulinkConstants.PARAM_ASSIGNMENT_PORT_INDICES, resultingIndices);
	}

	/**
	 * Processes the list of DataPortIndices of the MultiPortSwitch block as well as
	 * the case conditions for the switch block and prepares them in a format to
	 * ease direct retrieval later. The cases could be separated by spaces, commas
	 * or both. Ex: {1, 2 3 6 + [4,5 8 9* 6] -7 11- 6} --> [1],[2],[3],[10 11 14
	 * 60],[-7],[5]. The method processes the input and saves it as a block
	 * parameter in the format of a string where the individual case conditions are
	 * separated by commas.
	 *
	 * @param block
	 *            the block whose input is to be processed.
	 * @param input
	 *            the block data to be processed.
	 * @param parameterName
	 *            the new parameter name to which the processed and modified input
	 *            data is stored.
	 * @param addAsterisk
	 *            flag indicating whether to add "*" at the end of the modified
	 *            input or not.
	 */
	private static void processInputSignalsList(SimulinkBlock block, String input, String parameterName,
			boolean addAsterisk) {

		if (input == null) {
			block.setParameter(parameterName, StringUtils.EMPTY_STRING);
			return;
		}

		// Remove curly brackets and trailing white spaces and/or commas
		String modifiedInput = input.replaceAll("\\{\\s*,?|,?\\s*\\}", StringUtils.EMPTY_STRING).trim();

		// Matches the mathematical operations in the cases and removes the
		// spaces. For ex. 5 * 4 becomes 5*4.
		Matcher matcher = Pattern.compile(RegexConstants.MATHEMATICAL_OPERATIONS_PATTERN).matcher(modifiedInput);
		StringBuffer inputSignalBuffer = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(inputSignalBuffer, matcher.group(0).replaceAll("\\s+", ""));
		}
		matcher.appendTail(inputSignalBuffer);
		modifiedInput = inputSignalBuffer.toString();

		// Matches the individual cases separated by white spaces and replaces
		// the white spaces with commas.
		matcher = Pattern.compile(RegexConstants.INDIVIDUAL_INPUTS_PATTERN).matcher(modifiedInput);
		inputSignalBuffer = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(inputSignalBuffer, matcher.group(0).replaceAll("\\s+", ","));
		}
		matcher.appendTail(inputSignalBuffer);
		modifiedInput = inputSignalBuffer.toString();

		// In the case of nested brackets, we start with the innermost bracketed
		// list and continue outwards until all the bracketed lists are matched.
		while (modifiedInput.contains("[")) {
			modifiedInput = processBracketedInputs(modifiedInput);
		}

		if (addAsterisk) {
			modifiedInput = modifiedInput.concat(",*");
		}
		block.setParameter(parameterName, modifiedInput);
	}

	/**
	 * Matches the string between square brackets and performs distribution on
	 * multiple cases if needed (ex. 7 + [1,2] becomes [7+1, 7+2]). And the commas
	 * between the cases are replaced with white spaces.
	 */
	private static String processBracketedInputs(String inputSignals) {
		Matcher matcher = Pattern.compile(RegexConstants.GROUPED_INPUTS_PATTERN).matcher(inputSignals);
		StringBuffer inputSignalBuffer = new StringBuffer();
		while (matcher.find()) {
			String operandsBeforeBracket = matcher.group(1).replaceAll("\\s+", "");
			String inputsBetweenBrackets = matcher.group(2);
			String operandAfterBracket = matcher.group(3).replaceAll("\\s+", "");
			String replacement = inputsBetweenBrackets.replaceAll(",",
					operandAfterBracket + " " + operandsBeforeBracket);
			replacement = operandsBeforeBracket + replacement + operandAfterBracket;
			matcher.appendReplacement(inputSignalBuffer, replacement);
		}
		matcher.appendTail(inputSignalBuffer);
		return inputSignalBuffer.toString();
	}

	/**
	 * If the model has a Stateflow chart associated with this block, create a
	 * StateflowBlock, otherwise create a SimulinkBlock.
	 */
	private SimulinkBlock createBlock(MDLSection section, SimulinkBlock parent) {
		String name = section.getParameter(SimulinkConstants.PARAM_NAME);
		String fqName = SimulinkUtils.buildId(parent, name);

		StateflowMachine machine = model.getStateflowMachine();
		if (machine != null) {
			StateflowChart chart = machine.getChart(fqName);
			if (chart != null) {
				return new StateflowBlock(chart);
			}
		}
		return new SimulinkBlock();
	}

	/** Build annotations. */
	private static void buildAnnotations(MDLSection section, SimulinkBlock simulinkBlock) {
		for (MDLSection annotationSection : section.getSubSections(SECTION_ANNOTATION)) {
			SimulinkAnnotation annotation = new SimulinkAnnotation();
			SimulinkModelBuilder.addParameters(annotation, annotationSection);
			simulinkBlock.addAnnotation(annotation);
		}
	}

	/** Build objects. */
	private void buildObjects(MDLSection section, SimulinkElementBase simulinkElement) {
		for (MDLSection objectSection : section.getSubSections(SECTION_OBJECT)) {
			SimulinkObject object = new SimulinkObject();
			SimulinkModelBuilder.addParameters(object, objectSection);
			simulinkElement.addObject(object);

			// objects can be recursive
			buildObjects(objectSection, object);
		}

		// additionally parse objects contained in arrays
		for (MDLSection arraySection : section.getSubSections(SECTION_ARRAY)) {
			buildObjects(arraySection, simulinkElement);
		}

	}

	/** Build block parameter defaults. */
	private void buildBlockTypeDefaultParams(MDLSection modelSection) {
		MDLSection blockDefaults = modelSection.getFirstSubSection(SECTION_BLOCK_DEFAULTS);

		if (blockDefaults == null) {
			return;
		}

		for (String name : blockDefaults.getParameterNames()) {
			model.setBlockDefaultParameter(name, blockDefaults.getParameter(name));
		}
	}

	/** Build type-specific block parameter defaults. */
	private void buildBlockDefaultParams(MDLSection modelSection) {
		MDLSection blockParameterDefaults = modelSection.getFirstSubSection(SECTION_BLOCK_PARAMETER_DEFAULTS);

		if (blockParameterDefaults == null) {
			return;
		}

		for (MDLSection childBlock : blockParameterDefaults.getSubSections(SECTION_BLOCK)) {
			String type = childBlock.getParameter(PARAM_BLOCK_TYPE);

			for (String name : childBlock.getParameterNames()) {
				// don't add type itself
				if (!name.equals(PARAM_BLOCK_TYPE)) {
					model.setBlockTypeDefaultParameter(type, name, childBlock.getParameter(name));
				}
			}
		}
	}

	/** Build line parameter defaults. */
	private void buildLineDefaultParams(MDLSection modelSection) {
		MDLSection lineDefaults = modelSection.getFirstSubSection(SECTION_LINE_DEFAULTS);

		if (lineDefaults == null) {
			return;
		}

		for (String name : lineDefaults.getParameterNames()) {
			model.setLineDefaultParameter(name, lineDefaults.getParameter(name));
		}
	}

	/** Build annotation parameter defaults. */
	private void buildAnnotationDefaultParams(MDLSection modelSection) {
		MDLSection annotationDefaults = modelSection.getFirstSubSection(SECTION_ANNOTATION_DEFAULTS);

		if (annotationDefaults == null) {
			return;
		}

		for (String name : annotationDefaults.getParameterNames()) {
			model.setAnnotationDefaultParameter(name, annotationDefaults.getParameter(name));
		}
	}

	/**
	 * Class which contains constants used by pattern matching for the Simulink
	 * blocks which require special handling and processing.
	 */
	class RegexConstants {
		/**
		 * Pattern that matches the mathematical operations (+, -, /, * or ^) in the
		 * list of inputs. The pattern takes into consideration the difference between
		 * signs and operators.
		 */
		private final static String MATHEMATICAL_OPERATIONS_PATTERN = "\\d+((\\s*[\\\\*/\\^/&&[^,]]\\s*|\\s*[-+]\\s+)\\d+)+";
		/**
		 * Pattern that matches the individual inputs. The pattern takes into
		 * consideration the difference between signs and operators; it also handles the
		 * case of performing distribution operations on inputs (ex. 7 + [1,2,3]) and
		 * considers it as an individual input signal.
		 */
		private final static String INDIVIDUAL_INPUTS_PATTERN = "(\\]\\s+[-+]?\\d+((\\s+\\d*)+)*|\\d+(\\s+\\d*)+)(?!(,|[+-\\\\*]\\s*\\[))";
		/**
		 * Pattern that matches the input signals grouped by square brackets, which do
		 * not contain nested bracketed groups. The pattern also considers the
		 * distribution operations if they exist (ex. 7 + [1,2]).
		 */
		private final static String GROUPED_INPUTS_PATTERN = "([^,\\[]*)\\[([^\\[\\]]*)\\]([^,\\]]*)";
	}

}