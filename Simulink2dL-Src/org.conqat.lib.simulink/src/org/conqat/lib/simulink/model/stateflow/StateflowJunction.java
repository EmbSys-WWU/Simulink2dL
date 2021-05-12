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

import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.datahandler.StateLayoutData;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This class represents Stateflow junctions.
 */
public class StateflowJunction extends StateflowNodeBase {

	/** Create junction. */
	public StateflowJunction() {
		super();
	}

	/** Create new junction from existing one (for deep cloning). */
	private StateflowJunction(StateflowJunction orig) {
		super(orig);

		// duplicate all default transitions to this one
		for (StateflowTransition transition : orig.getInTransitions()) {
			if (transition.getSrc() == null) {
				SimulinkUtils.copyParameters(transition, new StateflowTransition(null, this));
			}
		}
	}

	/** Get junction type. */
	public String getType() {
		return getParameter(SimulinkConstants.PARAM_TYPE);
	}

	/** Returns junction type and id. */
	@Override
	public String toString() {
		return getType() + " (" + getStateflowId() + ")";
	}

	/** {@inheritDoc} */
	@Override
	public StateflowJunction deepClone() {
		return new StateflowJunction(this);
	}

	/** {@inheritDoc} */
	@ADeepCloneTestExclude
	@Override
	public StateLayoutData obtainLayoutData() {
		return getMachine().getModelDataHandler().getStateflowLayoutHandler().obtainJunctionLayoutData(this);
	}

	/** {@inheritDoc} */
	@Override
	public String getResolvedLabel() {
		String label = getParameter(SimulinkConstants.PARAM_LABEL_STRING);
		if (label != null) {
			return label;
		}
		return "Junction" + getStateflowId();
	}
}