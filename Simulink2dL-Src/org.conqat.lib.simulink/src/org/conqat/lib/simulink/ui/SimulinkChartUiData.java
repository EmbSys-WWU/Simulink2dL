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
package org.conqat.lib.simulink.ui;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Represents all data needed to render a simulink block and its subblocks.
 *
 * @ConQAT.Rating GREEN Hash: A15849BB17B9EBD38320CAF5F127895A
 */
public class SimulinkChartUiData extends SimulinkUiDataBase {

	/** Nodes contained in this chart.. */
	private final List<StateflowNodeUiData> nodes = new ArrayList<>();

	/** Transitions contained in this chart. */
	private final List<StateflowTransitionUiData> transitions = new ArrayList<>();

	/** Constructor. */
	public SimulinkChartUiData(StateflowChart chart) {
		super(StringUtils.getLastPart(chart.getResolvedId(), '/'), chart.getResolvedId().replaceAll("\\\\\\\\", "\\"));

		for (StateflowNodeBase node : chart.getNodes()) {
			nodes.add(new StateflowNodeUiData(node));
		}

		for (StateflowTransition transition : SimulinkUtils.getAllTransitions(chart)) {
			transitions.add(new StateflowTransitionUiData(transition));
		}
	}

}
