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

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.datahandler.RectangleLayoutData;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Base class for Stateflow nodes (elements that can be connected by
 * transitions).
 */
public abstract class StateflowNodeBase extends StateflowDeclContainerBase<IStateflowNodeContainer<?>> {

	/** Incoming transitions. */
	private final IdentityHashSet<StateflowTransition> inTransitions = new IdentityHashSet<StateflowTransition>();

	/** Outgoing transitions. */
	private final IdentityHashSet<StateflowTransition> outTransitions = new IdentityHashSet<StateflowTransition>();

	/** Create new node. */
	protected StateflowNodeBase() {
		super();
	}

	/** Create new element from existing one (for deep cloning). */
	protected StateflowNodeBase(StateflowNodeBase element) {
		super(element);
	}

	/** Add incoming transition. */
	/* package */void addInTransition(StateflowTransition transition) {
		CCSMAssert.isTrue(transition.getDst() == this, "Transition destination element does not match");
		inTransitions.add(transition);
	}

	/** Add outgoing transition. */
	/* package */void addOutTransition(StateflowTransition transition) {
		CCSMAssert.isTrue(transition.getSrc() == this, "Transition source element does not match");
		outTransitions.add(transition);
	}

	/** Get incoming transitions. */
	public UnmodifiableSet<StateflowTransition> getInTransitions() {
		return CollectionUtils.asUnmodifiable(inTransitions);
	}

	/** Get outgoing transitions. */
	public UnmodifiableSet<StateflowTransition> getOutTransitions() {
		return CollectionUtils.asUnmodifiable(outTransitions);
	}

	/** Remove this node from the model. */
	@Override
	public void remove() {
		IStateflowNodeContainer<?> parent = getParent();

		CCSMAssert.isFalse(parent == null, "Node has no parent to be removed from.");

		// The reason for this instanceof-constrcut is the following: Java
		// interfaces support only public methods. Therefore adding the
		// removeNode-method to IStateFlowNodeContainr would make it visible to
		// all clients. As we usually only make the parameterless
		// remove()-method visible, this is undesirable.
		if (parent instanceof StateflowChart) {
			((StateflowChart) parent).removeNode(this);
		} else if (parent instanceof StateflowState) {
			((StateflowState) parent).removeNode(this);
		} else {
			CCSMAssert.fail("Unknown Stateflow container: " + parent);
		}

		for (StateflowTransition transition : new ArrayList<StateflowTransition>(inTransitions)) {
			transition.remove();
		}

		for (StateflowTransition transition : new ArrayList<StateflowTransition>(outTransitions)) {
			transition.remove();
		}
	}

	/** Defines covariant returnt type. */
	@Override
	public abstract StateflowNodeBase deepClone();

	/** Remove in transition. */
	/* package */void removeInTransition(StateflowTransition transition) {
		CCSMAssert.isTrue(inTransitions.contains(transition), "Transition does not belong to this node.");
		inTransitions.remove(transition);
	}

	/** Remove out transition. */
	/* package */void removeOutTransition(StateflowTransition transition) {
		CCSMAssert.isTrue(outTransitions.contains(transition), "Transition does not belong to this node.");
		outTransitions.remove(transition);
	}

	/** Returns the id. */
	public String getId() {
		return getParameter(SimulinkConstants.PARAM_ID);
	}
	
	/**
	 * Returns the layout data for this node. This data is parsed from the model
	 * with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public abstract RectangleLayoutData obtainLayoutData();

	/** {@inheritDoc} */
	@Override
	public String getResolvedId() {
		StateflowChart parent = this.getParentChart();
		if (parent == null) {
			return SimulinkUtils.escapeSlashes(getResolvedLabel());
		}
		return parent.getResolvedId() + SimulinkUtils.SIMULINK_ID_SEPARATOR
				+ SimulinkUtils.escapeSlashes(getResolvedLabel());
	}

	/** Get resolved node label. */
	protected abstract String getResolvedLabel();

	/** Returns access to the {@link StateflowMachine} of this model. */
	public StateflowMachine getMachine() {
		return getParentChart().getMachine();
	}
}
