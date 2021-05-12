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
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.TransitionLayoutData;

/**
 * A Stateflow transition.
 */
public class StateflowTransition extends ParameterizedElement {

	/** Source node. May be null for default transitions. */
	private StateflowNodeBase src;

	/** Destination node. Never null. */
	private StateflowNodeBase dst;

	/** The parent chart. This is only non-null for unconnected transitions. */
	private StateflowChart parentChart;

	/** Create new transition. */
	public StateflowTransition(StateflowNodeBase src, StateflowNodeBase dst) {
		this.src = src;
		this.dst = dst;

		if (src != null) {
			src.addOutTransition(this);
		}

		if (dst != null) {
			dst.addInTransition(this);
		}
	}

	/** Sets the parent chart. */
	/* package */ void setParentChart(StateflowChart chart) {
		CCSMAssert.isTrue(parentChart == null, "May not set parent chart twice!");
		parentChart = chart;
	}

	/** Get destination node. */
	public StateflowNodeBase getDst() {
		return dst;
	}

	/** Get label. */
	public String getLabel() {
		return getParameter(SimulinkConstants.PARAM_LABEL_STRING);
	}

	/** Get source node. This may be null to indicate default transitions. */
	public StateflowNodeBase getSrc() {
		return src;
	}

	/** Returns the id. */
	public String getId() {
		return getParameter(SimulinkConstants.PARAM_ID);
	}

	/** Remove this transition from the model. */
	public void remove() {
		if (src != null) {
			src.removeOutTransition(this);
			src = null;
		}
		dst.removeInTransition(this);
		dst = null;
	}

	/** toString() includes source and destination. */
	@Override
	public String toString() {
		if (src == null) {
			return "-> " + dst;
		}
		return src + " -> " + dst;
	}

	/**
	 * Returns the layout data for this transition. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public TransitionLayoutData obtainLayoutData() {
		return accessChart().getMachine().getModelDataHandler().getStateflowLayoutHandler()
				.obtainTransitionLayoutData(this);
	}

	/** Returns a reference to the chart this belongs to. */
	private StateflowChart accessChart() {
		if (dst != null) {
			return dst.getParentChart();
		}
		if (src != null) {
			return src.getParentChart();
		}
		if (parentChart != null) {
			return parentChart;
		}
		throw new AssertionError("Expecting at least one way to access the chart!");
	}

	/**
	 * Returns the layout data for the label of this transition or null if no
	 * label is available. This data is parsed from the model with each call, so
	 * repeated access should be avoided by storing the result in a local
	 * variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return accessChart().getMachine().getModelDataHandler().getStateflowLayoutHandler()
				.obtainTransitionLabelData(this);
	}
}