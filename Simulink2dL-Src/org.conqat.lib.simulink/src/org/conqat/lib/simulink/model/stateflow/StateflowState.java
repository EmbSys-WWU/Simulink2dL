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

import java.util.Collections;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.StateLayoutData;

/**
 * This class represents Stateflow states.
 */
public class StateflowState extends StateflowNodeBase implements IStateflowNodeContainer<IStateflowNodeContainer<?>>,
		IStateflowChartContainer<IStateflowNodeContainer<?>> {

	/** Set of child states. */
	private final IdentityHashSet<StateflowNodeBase> nodes = new IdentityHashSet<StateflowNodeBase>();

	/** subviewer chart if this Node is acutally a subchart */
	private StateflowChart subviewer;

	/** Create state. */
	public StateflowState() {
		super();
	}

	/** Create new state from existing one (for deep cloning). */
	protected StateflowState(StateflowState orig) {
		super(orig);

		for (StateflowNodeBase child : orig.nodes) {
			addNode(child.deepClone());
		}

		if (orig.subviewer != null) {
			setSubViewer(orig.subviewer.deepClone());
		}

		TransitionCloneUtils.cloneTransitions(orig, this);
	}

	/** Sets the subchart. */
	public void setSubViewer(StateflowChart subChart) {
		CCSMAssert.isTrue(subviewer == null, "May not set subchart twice!");
		subviewer = subChart;
		subviewer.setParent(this);
	}

	/** Add a node to this state. */
	@Override
	public void addNode(StateflowNodeBase node) {
		nodes.add(node);
		node.setParent(this);
	}

	/** Get state label. */
	public String getLabel() {
		return getParameter(SimulinkConstants.PARAM_LABEL_STRING);
	}

	/** Get child nodes. */
	@Override
	public UnmodifiableSet<StateflowNodeBase> getNodes() {
		return CollectionUtils.asUnmodifiable(nodes);
	}

	/** Remove node. */
	/* package */
	void removeNode(StateflowNodeBase node) {
		CCSMAssert.isTrue(node.getParent() == this, "Node does not belong to this chart.");
		nodes.remove(node);
		node.setParent(null);
	}

	/** Returns label and id. */
	@Override
	public String toString() {
		return getLabel() + " (" + getStateflowId() + ")";
	}

	/** Deep clone this state. */
	@Override
	public StateflowState deepClone() {
		return new StateflowState(this);
	}

	/** Returns if this chart state is actually a subchart. */
	public boolean isSubChart() {
		return SimulinkConstants.VALUE_SUBCHART.equals(getParameter(SimulinkConstants.PARAM_SUPER_STATE));
	}

	/**
	 * Returns a chart (subview) if this chart state is actually a subchart. If
	 * this state does not wrap a subchart (isSubChart()==false) this method
	 * returns null. In stateflow, a subchart is a chart that is shown in
	 * another (parent) chart. We model subcharts as stateflow states because
	 * they are rendered similar to states and the subchart's contents are not
	 * rendered. You can render the subchart by passing it to
	 * StateflowChartRenderer.renderChart().
	 */
	public StateflowChart getSubViewer() {
		return subviewer;
	}

	/** {@inheritDoc} */
	@Override
	public UnmodifiableCollection<StateflowChart> getCharts() {
		if (subviewer == null) {
			return CollectionUtils.emptyList();
		}
		return CollectionUtils.asUnmodifiable(Collections.singleton(subviewer));
	}

	/** {@inheritDoc} */
	@ADeepCloneTestExclude
	@Override
	public StateLayoutData obtainLayoutData() {
		return getMachine().getModelDataHandler().getStateflowLayoutHandler().obtainStateLayoutData(this);
	}

	/**
	 * Returns the layout data for the label of this block. This data is parsed
	 * from the model with each call, so repeated access should be avoided by
	 * storing the result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return getMachine().getModelDataHandler().getStateflowLayoutHandler().obtainStateLabelData(this);
	}

	/** Returns whether this state is a function state. */
	public boolean isFunctionState() {
		return SimulinkConstants.VALUE_FUNC_STATE.equals(getParameter(SimulinkConstants.PARAM_TYPE));
	}

	/** Returns whether this state is a group state. */
	public boolean isGroupState() {
		return SimulinkConstants.VALUE_GROUP_STATE.equals(getParameter(SimulinkConstants.PARAM_TYPE));
	}

	/** Returns whether this state is implemented by a Simulink function. */
	public boolean isSimulinkFunction() {
		return SimulinkConstants.VALUE_1.equals(getParameter(SimulinkConstants.PARAM_SIMULINK_IS_SIMULINK_FCN));
	}

	/** Returns whether this state is implemented by a matlab function. */
	public boolean isMatlabFunction() {
		return SimulinkConstants.VALUE_1.equals(getParameter(SimulinkConstants.PARAM_EML_IS_EML));
	}

	/**
	 * Returns whether the state is just used as a "note box", i.e.
	 * annotation/label.
	 */
	public boolean isNoteBox() {
		return SimulinkConstants.VALUE_1.equals(getParameter(SimulinkConstants.PARAM_isNoteBox));
	}

	/**
	 * Returns the Simulink block implementing this function state (or null).
	 */
	public SimulinkBlock getSimulinkBlock() {
		String blockName = getParameter(SimulinkConstants.PARAM_SIMULINK_BLOCK_NAME);
		if (blockName == null) {
			return null;
		}

		StateflowBlock parentBlock = getParentChart().getStateflowBlock();
		if (parentBlock == null) {
			return null;
		}

		return parentBlock.getSubBlock(blockName);
	}

	/** {@inheritDoc} */
	@Override
	protected String getResolvedLabel() {
		if (isSimulinkFunction()) {
			return getSimulinkBlock().getName();
		}
		return getLabel();
	}
}
