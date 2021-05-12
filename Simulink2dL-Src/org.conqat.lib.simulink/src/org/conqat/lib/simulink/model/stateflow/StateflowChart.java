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

import java.util.ArrayList;
import java.util.Collection;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.simulink.model.SimulinkConstants;

/**
 * This class represents Stateflow charts. There is a one-to-one association
 * between {@link StateflowBlock}s and {@link StateflowChart}s.
 */
public class StateflowChart extends StateflowDeclContainerBase<IStateflowChartContainer<?>>
		implements IStateflowNodeContainer<IStateflowChartContainer<?>> {

	/** The Stateflow block associated with this chart (or null). */
	private StateflowBlock stateflowBlock;

	/** List of top level nodes. */
	private final IdentityHashSet<StateflowNodeBase> nodes = new IdentityHashSet<>();

	/** List of unconnected transitions. */
	private final IdentityHashSet<StateflowTransition> unconnectedTransitions = new IdentityHashSet<>();

	/** Create new Stateflow block. */
	public StateflowChart() {
		super();
	}

	/**
	 * Create new Stateflow chart from existing chart (for deep cloning).
	 */
	/* package */ StateflowChart(StateflowChart origChart) {
		super(origChart);

		for (StateflowNodeBase element : origChart.getNodes()) {
			addNode(element.deepClone());
		}

		TransitionCloneUtils.cloneTransitions(origChart, this);
	}

	/** Add node. */
	@Override
	public void addNode(StateflowNodeBase node) {
		nodes.add(node);
		node.setParent(this);
	}

	/** Deep clone this chart. */
	@Override
	public StateflowChart deepClone() {
		return new StateflowChart(this);
	}

	/** Get the Stateflow machine this chart belongs to. */
	public StateflowMachine getMachine() {
		return getParent().getMachine();
	}

	/** Returns the name of the chart. */
	public String getName() {
		if (stateflowBlock == null) {
			return "subviewer for " + getParameter(SimulinkConstants.PARAM_ID);
		}

		return getParameter(SimulinkConstants.PARAM_NAME_STATEFLOW);
	}

	/** Returns the nodes of this chart. */
	@Override
	public UnmodifiableSet<StateflowNodeBase> getNodes() {
		return CollectionUtils.asUnmodifiable(nodes);
	}

	/** Get Stateflow block this chart belongs to (or null for sub charts). */
	public StateflowBlock getStateflowBlock() {
		if (stateflowBlock != null) {
			return stateflowBlock;
		}

		// this is a subviewer
		if (super.getParent() instanceof StateflowState) {
			return ((StateflowState) super.getParent()).getParentChart().getStateflowBlock();
		}

		return null;
	}

	/**
	 * This method throws an {@link UnsupportedOperationException}. You must
	 * remove the associated {@link StateflowBlock} to remove a chart.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove chart without removing Stateflow block!");
	}

	/** Returns the name of the chart. */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Remove all nodes from this chart.
	 */
	/* package */
	void removeNodes() {
		for (StateflowNodeBase node : new ArrayList<StateflowNodeBase>(nodes)) {
			node.remove();
		}
	}

	/** Remove node. */
	/* package */
	void removeNode(StateflowNodeBase node) {
		CCSMAssert.isTrue(node.getParent() == this, "Node does not belong to this chart.");
		nodes.remove(node);
		node.setParent(null);
	}

	/** Set Stateflow block this chart belongs to. */
	/* package */
	void setStateflowBlock(StateflowBlock stateflowBlock) {
		if (stateflowBlock != null) {
			CCSMAssert.isTrue(this.stateflowBlock == null, "Cannot set new Stateflow block.");
		}
		this.stateflowBlock = stateflowBlock;
	}

	/** Returns the {@link StateflowChart} this belongs to. */
	@Override
	public StateflowChart getParentChart() {
		// This looks strange but is correct, as this is the endpoint of
		// getParentChart-call hierarchies starting in e.g., StateflowStates
		return this;
	}

	/** Adds an unconnected transition to the chart. */
	public void addUnconnectedTransition(StateflowTransition transition) {
		unconnectedTransitions.add(transition);
		transition.setParentChart(this);
	}

	/**
	 * @see #unconnectedTransitions
	 */
	public UnmodifiableSet<StateflowTransition> getUnconnectedTransitions() {
		return CollectionUtils.asUnmodifiable(unconnectedTransitions);
	}

	/** {@inheritDoc} */
	@Override
	public String getResolvedId() {
		if (stateflowBlock != null) {
			return stateflowBlock.getResolvedId();
		}

		if (getParent() instanceof StateflowState) {
			return ((StateflowState) getParent()).getResolvedId();
		}

		return getName();
	}

	/** Returns the state of given label or null. */
	public StateflowState getStateByLabel(String label) {
		return findByLabel(nodes, label);
	}

	/** Returns the state of given label or null. */
	private static StateflowState findByLabel(Collection<StateflowNodeBase> nodes, String label) {
		for (StateflowNodeBase node : nodes) {
			if (!(node instanceof StateflowState)) {
				continue;
			}

			StateflowState state = (StateflowState) node;
			if (label.equals(state.getLabel()) || label.equals(state.getParameter("simulink.blockName"))) {
				return state;
			}

			StateflowState subState = findByLabel(state.getNodes(), label);
			if (subState != null) {
				return subState;
			}
		}
		return null;
	}
}