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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Test for Simulink functions in Stateflow.
 */
public class SimulinkFunctionTest extends SimulinkTestBase {

	/** Tests for MDL format model. */
	@Test
	public void testForMdl() throws Exception {
		runTest(SimulinkModelBuilder.MDL_FILE_EXTENSION);
	}

	/** Tests for SLX format model. */
	@Test
	public void testForSlx() throws Exception {
		runTest(SimulinkModelBuilder.SLX_FILE_EXTENSION);
	}

	/** Executes the actual tests. */
	private void runTest(String extension) throws Exception {
		StateflowChart chart = ((StateflowBlock) loadModel("simulink_in_stateflow" + extension)
				.getBlock("simulink_in_stateflow/MyChart")).getChart();

		List<StateflowNodeBase> functionStates = CollectionUtils.filter(chart.getNodes(),
				node -> node instanceof StateflowState && ((StateflowState) node).isFunctionState());
		assertThat(functionStates).hasSize(1);

		StateflowState state = (StateflowState) functionStates.get(0);
		assertThat(state.isSimulinkFunction()).isTrue();

		SimulinkBlock block = state.getSimulinkBlock();
		assertThat(block).isNotNull();
		assertThat(block.getSubBlocks().size()).isEqualTo(4);
		assertThat(block.getId()).isEqualTo("simulink_in_stateflow/MyChart/MySimFunc");
	}

	/** Tests the case of . */
	@Test
	public void testWithFunction() throws Exception {
		StateflowChart chart = ((StateflowBlock) loadModel("simulink_function.slx").getBlock("SimulinkFunction/Chart"))
				.getChart();
		StateflowChart subChart = chart.getStateByLabel("state").getSubViewer();

		StateflowState state2 = subChart.getStateByLabel("state2");
		StateflowState enable = subChart.getStateByLabel("enable");
		StateflowState disable = subChart.getStateByLabel("disable");

		assertThat(state2.isFunctionState()).isFalse();
		assertThat(state2.isSimulinkFunction()).isFalse();
		assertThat(state2.getSimulinkBlock()).isNull();
		assertThat(state2.getResolvedId()).isNotNull();

		assertThat(enable.isFunctionState()).isTrue();
		assertThat(enable.isSimulinkFunction()).isTrue();
		assertThat(enable.getSimulinkBlock()).isNotNull();
		assertThat(enable.getResolvedId()).isNotNull();

		assertThat(disable.isFunctionState()).isTrue();
		assertThat(disable.isSimulinkFunction()).isTrue();
		assertThat(disable.getSimulinkBlock()).isNotNull();
		assertThat(disable.getResolvedId()).isNotNull();
	}

}
