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

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_NAME;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BLOCK_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BRANCH;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHILDREN;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_LINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MODEL;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MODEL_INFORMATION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_OBJECT;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SIMULATION_SETTINGS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATEFLOW;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SYSTEM;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SYSTEM_DEFAULTS;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHART;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_JUNCTION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MACHINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TRANSITION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;

import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Tests for {@link SLXModelSanitizer}. This tests if a model in .slx format is
 * sanitized properly to match the .mdl format.
 */
public class SLXModelSanitizerTest extends SimulinkTestBase {

	/** Test if model name is set properly. */
	@Test
	public void testSanitizedModelName() throws Exception {

		MutableMDLSection modelInformation = new MutableMDLSection(SECTION_MODEL_INFORMATION, 0);
		MutableMDLSection model = new MutableMDLSection(SECTION_MODEL, 1);
		MutableMDLSection simulationSettings = new MutableMDLSection(SECTION_SIMULATION_SETTINGS, 2);
		MutableMDLSection dataLoggingOverride = new MutableMDLSection(SECTION_OBJECT, 3);
		dataLoggingOverride.setParameter("model_", "model01");

		MutableMDLSection system = new MutableMDLSection(SECTION_SYSTEM, 2);

		simulationSettings.addSubSection(dataLoggingOverride);
		model.addSubSection(system);
		model.addSubSection(simulationSettings);
		modelInformation.addSubSection(model);

		SLXModelSanitizer.sanitize(modelInformation);
		MutableMDLSection resolvedModel = modelInformation.getFirstSubSection(SECTION_MODEL);
		String resolvedModelName = resolvedModel.getParameter(PARAM_NAME);

		assertThat(resolvedModelName, equalTo("model01"));

		MutableMDLSection resolvedSystem = resolvedModel.getFirstSubSection(SECTION_SYSTEM);
		String resolvedSytemName = resolvedSystem.getParameter(PARAM_NAME);

		assertThat(resolvedSytemName, equalTo("model01"));

	}

	/** Test if stateflow model is sanitized properly. */
	@Test
	public void testSanitizedStateflowModel() throws Exception {
		MutableMDLSection modelInformation = createModelToBeSanitized();

		SLXModelSanitizer.sanitize(modelInformation);
		MutableMDLSection sanitizedStateflow = modelInformation.getFirstSubSection(SECTION_STATEFLOW);
		assertThat(sanitizedStateflow, notNullValue());

		MutableMDLSection sanitizedMachine = sanitizedStateflow.getFirstSubSection(SECTION_MACHINE);
		assertThat(sanitizedMachine, notNullValue());
		assertThat(sanitizedMachine.getSubSections().getValues(), hasSize(0));

		assertThat(sanitizedStateflow.getSubSections(SECTION_STATE), hasSize(2));

		assertThat(sanitizedStateflow.getSubSections(SECTION_TRANSITION), hasSize(3));

		assertThat(sanitizedStateflow.getSubSections(SECTION_JUNCTION), hasSize(2));
	}

	/** Creates the model for the #testSanitizedStateflowModel() method */
	private static MutableMDLSection createModelToBeSanitized() {
		MutableMDLSection modelInformation = new MutableMDLSection(SECTION_MODEL_INFORMATION, 0);

		MutableMDLSection stateflow = new MutableMDLSection(SECTION_STATEFLOW, 0);
		modelInformation.addSubSection(stateflow);

		MutableMDLSection machine = new MutableMDLSection(SECTION_MACHINE, 1);
		stateflow.addSubSection(machine);

		MutableMDLSection machineChildren = new MutableMDLSection(SECTION_CHILDREN, 2);
		machine.addSubSection(machineChildren);

		MutableMDLSection chart = new MutableMDLSection(SECTION_CHART, 3);
		machineChildren.addSubSection(chart);

		MutableMDLSection chartChildren = new MutableMDLSection(SECTION_CHILDREN, 4);
		chart.addSubSection(chartChildren);

		MutableMDLSection state1 = new MutableMDLSection(SECTION_STATE, 5);
		chartChildren.addSubSection(state1);

		state1.addSubSection(new MutableMDLSection(SECTION_CHILDREN, 6));

		state1.addSubSection(new MutableMDLSection(SECTION_TRANSITION, 8));
		state1.addSubSection(new MutableMDLSection(SECTION_TRANSITION, 9));
		state1.addSubSection(new MutableMDLSection(SECTION_TRANSITION, 10));
		state1.addSubSection(new MutableMDLSection(SECTION_JUNCTION, 11));

		MutableMDLSection state11 = new MutableMDLSection(SECTION_STATE, 7);
		state1.addSubSection(state11);
		state11.addSubSection(new MutableMDLSection(SECTION_JUNCTION, 12));
		return modelInformation;
	}

	/** Test sanitizing line. */
	@Test
	public void testSanitizedLine() throws Exception {
		MutableMDLSection line = new MutableMDLSection(SECTION_LINE, 0);
		setLineParameters(line, "partial", "9", "6#out:1", "4#in:2", "[62, 0]");

		SLXModelSanitizer.sanitizeLine(line);
		assertLineParameters(line, "6", "1", "4", "2");
	}

	/** Test sanitizing branch. */
	@Test
	public void testSanitizedBranch() throws Exception {
		MutableMDLSection branch = new MutableMDLSection(SECTION_BRANCH, 1);
		setLineParameters(branch, null, "4", null, "3#in:5", "[0, 80]");
		branch.setParameter("Labels", "[2, 1]");

		SLXModelSanitizer.sanitizeLine(branch);
		assertLineParameters(branch, null, null, "3", "5");
	}

	/** Test sanitizing trigger/enable line. */
	@Test
	public void testSanitizedTriggerEnableLine() throws Exception {
		MutableMDLSection line = new MutableMDLSection(SECTION_LINE, 0);
		setLineParameters(line, "partial", "9", "6#out:1", "4#trigger", "[62, 0]");

		SLXModelSanitizer.sanitizeLine(line);
		assertLineParameters(line, "6", "1", "4", "trigger");
	}

	/** Test sanitizing matlab ports. */
	@Test
	public void testSanitizedLineToMatlabPort() throws Exception {
		MutableMDLSection lineToMatlabPort = new MutableMDLSection(SECTION_LINE, 0);
		setLineParameters(lineToMatlabPort, null, "5", "696:1#out:1", "696:6#in:2", null);

		SLXModelSanitizer.sanitizeLine(lineToMatlabPort);
		assertLineParameters(lineToMatlabPort, "696:1", "1", "696:6", "2");
	}

	/**
	 * Sets the parameters of a section representing a line. Each parameter may
	 * be null, which indicates that it should not be set.
	 */
	private static void setLineParameters(MutableMDLSection line, String name, String zOrder, String src, String dst,
			String points) {
		if (name != null) {
			line.setParameter("Name", name);
		}
		if (zOrder != null) {
			line.setParameter("ZOrder", zOrder);
		}
		if (src != null) {
			line.setParameter("Src", src);
		}
		if (dst != null) {
			line.setParameter("Dst", dst);
		}
		if (points != null) {
			line.setParameter("Points", points);
		}
	}

	/**
	 * Asserts the values of the src and dst block/port parameter of a section
	 * representing a line. Each string may be null to indicate that this value
	 * is not to be checked.
	 */
	private static void assertLineParameters(MutableMDLSection line, String srcBlock, String srcPort, String dstBlock,
			String dstPort) {
		Map<String, String> lineParams = line.getParameters();

		if (srcBlock != null && srcPort != null) {
			assertThat(lineParams, hasEntry("SrcBlock", srcBlock));
			assertThat(lineParams, hasEntry("SrcPort", srcPort));
		}

		if (dstBlock != null && dstPort != null) {
			assertThat(lineParams, hasEntry("DstBlock", dstBlock));
			assertThat(lineParams, hasEntry("DstPort", dstPort));
		}

		assertThat(lineParams, not(hasKey("Src")));
		assertThat(lineParams, not(hasKey("Dst")));
	}

	/** Test sanitizing system defaults. */
	@Test
	public void testSanitizedSystemDefaults() throws Exception {
		MutableMDLSection model = new MutableMDLSection(SECTION_MODEL, 0);
		model.addSubSection(new MutableMDLSection(SECTION_BLOCK_DEFAULTS, 1));

		model.addSubSection(createSystemDefaults());

		SLXModelSanitizer.flattenSystemDefaults(model);
		Map<String, String> blockDefaultsParams = model.getFirstSubSection(SECTION_BLOCK_DEFAULTS).getParameters();

		assertThat(blockDefaultsParams, hasEntry("System.PaperOrientation", "landscape"));
		assertThat(blockDefaultsParams, hasEntry("System.PaperPositionMode", "auto"));
		assertThat(blockDefaultsParams, hasEntry("System.PaperType", "usletter"));
		assertThat(blockDefaultsParams, hasEntry("System.PaperUnits", "inches"));
		assertThat(blockDefaultsParams,
				hasEntry("System.TiledPaperMargins", "[0.500000, 0.500000, 0.500000, 0.500000]"));
		assertThat(blockDefaultsParams, hasEntry("System.TiledPageScale", "1"));
		assertThat(blockDefaultsParams, hasEntry("System.ShowPageBoundaries", "off"));
		assertThat(blockDefaultsParams, hasEntry("System.ModelBrowserVisibility", "off"));
		assertThat(blockDefaultsParams, hasEntry("System.ModelBrowserWidth", "200"));
		assertThat(blockDefaultsParams, hasEntry("System.ScreenColor", "white"));

		List<MutableMDLSection> sanitizedSystemDefaults = model.getSubSections(SECTION_SYSTEM_DEFAULTS);

		assertThat(sanitizedSystemDefaults, hasSize(0));
	}

	/**
	 * Creates the system defaults for method
	 * {@link #testSanitizedSystemDefaults()}.
	 */
	private static MutableMDLSection createSystemDefaults() {
		MutableMDLSection systemDefaults = new MutableMDLSection(SECTION_SYSTEM_DEFAULTS, 2);
		systemDefaults.setParameter("PaperOrientation", "landscape");
		systemDefaults.setParameter("PaperPositionMode", "auto");
		systemDefaults.setParameter("PaperType", "usletter");
		systemDefaults.setParameter("PaperUnits", "inches");
		systemDefaults.setParameter("TiledPaperMargins", "[0.500000, 0.500000, 0.500000, 0.500000]");
		systemDefaults.setParameter("TiledPageScale", "1");
		systemDefaults.setParameter("ShowPageBoundaries", "off");
		systemDefaults.setParameter("ModelBrowserVisibility", "off");
		systemDefaults.setParameter("ModelBrowserWidth", "200");
		systemDefaults.setParameter("ScreenColor", "white");
		return systemDefaults;
	}
}