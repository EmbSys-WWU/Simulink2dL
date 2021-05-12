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

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.simulink.model.SimulinkConstants;

/**
 * A Stateflow target object. Stateflow uses targets for generating C-Code which
 * is used for simulation.
 */
public class StateflowTarget extends StateflowElementBase<StateflowMachine> {

	/** Create new target. */
	public StateflowTarget() {
		super();
	}

	/** Create new target from existing one (for deep cloning) */
	private StateflowTarget(StateflowTarget orig) {
		super(orig);
	}

	/** Remove this target from the model. */
	@Override
	public void remove() {
		CCSMAssert.isFalse(getParent() == null, "Target has no parent to be removed from.");
		getParent().removeTarget(this);
	}

	/** Deep clone the target. */
	@Override
	public StateflowTarget deepClone() {
		return new StateflowTarget(this);
	}

	/** Get name. */
	public String getName() {
		return getParameter(SimulinkConstants.PARAM_NAME_STATEFLOW);
	}
}