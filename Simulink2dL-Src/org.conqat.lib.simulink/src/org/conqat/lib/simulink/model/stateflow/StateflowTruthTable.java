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

/**
 * Element representing a stateflow truth table. This class extends
 * StateflowState since it also has position information.
 */
public class StateflowTruthTable extends StateflowState {

	/**
	 * Script representing the functionality of this truth table. (stateflow
	 * generates this script based on the truth table values)
	 */
	private String script;

	/** Create new truthTable */
	public StateflowTruthTable() {
		super();
	}

	/** Create new truthTable from existing one (for deep cloning). */
	private StateflowTruthTable(StateflowTruthTable orig) {
		super(orig);
		this.script = orig.script;
	}

	/** Deep clone this state. */
	@Override
	public StateflowTruthTable deepClone() {
		return new StateflowTruthTable(this);
	}

	/** @see #script */
	public String getScript() {
		return script;
	}

	/** @see #script */
	public void setScript(String script) {
		this.script = script;
	}

}
