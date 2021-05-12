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
package org.conqat.lib.simulink.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Test class for {@link SimulinkUtils}.
 */
public class SimulinkUtilsTest extends SimulinkTestBase {

	/** Test {@link SimulinkUtils#splitSimulinkId(String)} */
	@Test
	public void testSplitSimulinkId() {
		assertSplit("hallo/ccsm", "hallo", "ccsm");
		assertSplit("hallo/ccsm/tum", "hallo", "ccsm", "tum");
		assertSplit("hallo/ccsm//tum", "hallo", "ccsm/tum");
		assertSplit("sl_subsys_fcncallerr7/f/Unit Delay", "sl_subsys_fcncallerr7", "f", "Unit Delay");
	}

	/** Test {@link SimulinkUtils#createSimulinkId(Iterable)}. */
	@Test
	public void testCreateSimulinkId() {
		String[] nameArray = { "name1", "name2", "name3" };
		List<String> names = new ArrayList<String>();
		Collections.addAll(names, nameArray);
		assertEquals("name1/name2/name3", SimulinkUtils.createSimulinkId(names));

		names.add("test/name");
		assertEquals("name1/name2/name3/test//name", SimulinkUtils.createSimulinkId(names));

		try {
			names.add("name/");
			SimulinkUtils.createSimulinkId(names);
			fail();
		} catch (AssertionError ex) {
			// expected
		}

		try {
			names.add("/name");
			SimulinkUtils.createSimulinkId(names);
			fail();
		} catch (AssertionError ex) {
			// expected
		}
	}

	/**
	 * Test for {@link SimulinkUtils#getStateName(StateflowState)} and
	 * {@link SimulinkUtils#getFQStateName(StateflowState)}.
	 */
	@Test
	public void testGetStateName() throws Exception {
		SimulinkModel model = loadModel("internal_sf_tictacflow.mdl");

		StateflowChart chart = model.getStateflowMachine()
				.getChart("internal_sf_tictacflow/simulink io/scheduled stuff");
		StateflowState state = (StateflowState) getStateflowNode(chart, "333");
		assertEquals("sleep", SimulinkUtils.getStateName(state));
		assertEquals("internal_sf_tictacflow/simulink io/scheduled stuff/sleep", SimulinkUtils.getFQStateName(state));
		state = (StateflowState) getStateflowNode(state, "335");
		assertEquals("asleep", SimulinkUtils.getStateName(state));
		assertEquals("internal_sf_tictacflow/simulink io/scheduled stuff/sleep.asleep",
				SimulinkUtils.getFQStateName(state));
	}

	/** Assert correct split. */
	private static void assertSplit(String id, String... expectedNames) {
		List<String> actualNames = SimulinkUtils.splitSimulinkId(id);
		assertEquals(expectedNames.length, actualNames.size());
		for (int i = 0; i < actualNames.size(); i++) {
			assertEquals(expectedNames[i], actualNames.get(i));
		}
	}
}