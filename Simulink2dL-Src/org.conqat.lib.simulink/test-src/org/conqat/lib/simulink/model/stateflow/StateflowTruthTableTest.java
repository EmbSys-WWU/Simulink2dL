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
package org.conqat.lib.simulink.model.stateflow;

import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Test for the truth tables in the model.
 */
public class StateflowTruthTableTest extends SimulinkTestBase {

	/**
	 * Script code expected in the truth tables of TruthTable.mdl and
	 * TruthTable.slx
	 */
	private static final String EXPECTED_SCRIPT = "function y = fcn(u)\n\naVarTruthTableCondition_1 = false;\naVarTruthTableCondition_2 = false;\n\n\n% Example condition 1\n\naVarTruthTableCondition_1 = logical(u >= 0);\n\n% Example condition 2\n\naVarTruthTableCondition_2 = logical(u ^ 2 <= 1);\n\nif (aVarTruthTableCondition_1 && aVarTruthTableCondition_2)\n    aFcnTruthTableAction_1();\nelseif (~aVarTruthTableCondition_1 && ~aVarTruthTableCondition_2)\n    aFcnTruthTableAction_1();\nelse % Default\n    aFcnTruthTableAction_2();\nend\n\nfunction aFcnTruthTableAction_1()\n\n% Example action 1 called from D1 & D2 column in condition table\n\ny = u;\n\nfunction aFcnTruthTableAction_2()\n\n% Example action 2 called from D3 column in condition table\n\ny = 1 / u;";

	/** Test if truthTable is as expected. */
	@Test
	public void testMDLTruthTableScript() throws ZipException, SimulinkModelBuildingException, IOException {
		checkTruthTableScript("TruthTable.mdl");
	}

	/** Test if truthTable is as expected. */
	@Test
	public void testSLXTruthTableScript() throws ZipException, SimulinkModelBuildingException, IOException {
		checkTruthTableScript("TruthTable.slx");
	}

	/**
	 * Check if the chart contains a truth table and if the script in the truth
	 * table is equal to the expected script.
	 */
	private void checkTruthTableScript(String modelName)
			throws ZipException, SimulinkModelBuildingException, IOException {
		StateflowChart chart = loadModel(modelName).getStateflowMachine().getChart("TruthTable/Truth Table");

		Set<StateflowNodeBase> nodes = chart.getNodes();
		Assertions.assertThat(nodes).size().isEqualTo(2);

		Assertions.assertThat(nodes).areExactly(1, new Condition<StateflowNodeBase>("instanceofTruthTable") {
			/** {@inheritDoc} */
			@Override
			public boolean matches(StateflowNodeBase value) {
				return value instanceof StateflowTruthTable;
			}
		});

		nodes.stream().filter(node -> node instanceof StateflowTruthTable).forEach(node -> Assertions
				.assertThat(((StateflowTruthTable) node).getScript()).isEqualToIgnoringWhitespace(EXPECTED_SCRIPT));
	}
}
