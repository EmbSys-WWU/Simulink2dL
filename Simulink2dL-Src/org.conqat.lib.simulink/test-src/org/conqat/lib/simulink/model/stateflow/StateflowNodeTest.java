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
package org.conqat.lib.simulink.model.stateflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipException;

import org.assertj.core.api.Assertions;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Tests the {@link SimulinkBlock}.
 */
public class StateflowNodeTest extends SimulinkTestBase {

	/** Tests the resolved Id. */
	@Test
	public void testResolvedId() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("stateflow_subchart.mdl", new ModelBuildingParameters());
		List<String> expectedIDs = new ArrayList<>(
				Arrays.asList("stateflow_subchart/Chart/Subchart", "stateflow_subchart/Chart/Subchart/State_lvl2_2",
						"stateflow_subchart/Chart/Subchart/State_lvl2_1", "stateflow_subchart/Chart/State_lvl1"));
		List<String> foundIDs = listStateflowElementIDsUpToSecondLevel(
				model.getStateflowMachine().getChart("stateflow_subchart/Chart").getNodes());
		Assertions.assertThat(foundIDs).hasSameElementsAs(expectedIDs);
	}

	/**
	 * Returns the resolvedIds of stateflow elements that are reachable from
	 * nodes in two steps.
	 */
	private static List<String> listStateflowElementIDsUpToSecondLevel(UnmodifiableSet<StateflowNodeBase> nodes) {
		List<String> foundIDs = new ArrayList<>();
		for (StateflowNodeBase node : nodes) {
			foundIDs.add(node.getResolvedId());
			if (node instanceof StateflowState && ((StateflowState) node).isSubChart()) {
				for (StateflowNodeBase nodeLevel2 : ((StateflowState) node).getSubViewer().getNodes()) {
					foundIDs.add(nodeLevel2.getResolvedId());
				}
			}
		}
		return foundIDs;
	}

	/** Tests the resolved Id. */
	@Test
	public void testResolvedIdWithBlockRename() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("stateflow_subchart.mdl",
				new ModelBuildingParameters().setParentBlockId("some/other/ref-block"));
		List<String> expectedIDs = new ArrayList<>(
				Arrays.asList("some/other/ref-block/Chart/Subchart", "some/other/ref-block/Chart/Subchart/State_lvl2_2",
						"some/other/ref-block/Chart/Subchart/State_lvl2_1", "some/other/ref-block/Chart/State_lvl1"));
		List<String> foundIDs = listStateflowElementIDsUpToSecondLevel(
				model.getStateflowMachine().getChart("stateflow_subchart/Chart").getNodes());
		Assertions.assertThat(foundIDs).hasSameElementsAs(expectedIDs);
	}

	/** Tests the resolved Id. */
	@Test
	public void testResolvedIdWithSimulinkFunctions() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("simulink_in_stateflow.mdl");

		StateflowChart chart = CollectionUtils.getAny(model.getStateflowMachine().getCharts());
		Assertions.assertThat(chart.getName()).isEqualTo("MyChart");

		Optional<StateflowNodeBase> functionState = chart.getNodes().stream()
				.filter(node -> node instanceof StateflowState && ((StateflowState) node).isSimulinkFunction())
				.findFirst();
		Assertions.assertThat(functionState.isPresent()).isTrue();

		Assertions.assertThat(functionState.get().getResolvedId()).isEqualTo("simulink_in_stateflow/MyChart/MySimFunc");

		SimulinkBlock block = ((StateflowState) functionState.get()).getSimulinkBlock();
		Assertions.assertThat(block.getResolvedId()).isEqualTo("simulink_in_stateflow/MyChart/MySimFunc");
	}
}
