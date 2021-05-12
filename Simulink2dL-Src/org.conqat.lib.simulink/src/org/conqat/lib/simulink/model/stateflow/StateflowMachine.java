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
import java.util.HashMap;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandler;

/**
 * The Stateflow machine is the container element of all Stateflow elements of a
 * Simulink model. There is only one machine per model and Stateflow machines do
 * not have a parent element.
 */
public class StateflowMachine extends StateflowDeclContainerBase<IStateflowElement<?>>
		implements IStateflowChartContainer<IStateflowElement<?>> {

	/** The Simulink model this machine belongs to. */
	private SimulinkModel model;

	/** Data handler used for interpreting the model's data. */
	private final ModelDataHandler dataHandler;

	/** Targets of this machine. */
	private final IdentityHashSet<StateflowTarget> targets = new IdentityHashSet<StateflowTarget>();

	/** Charts of this machine. */
	private final HashMap<String, StateflowChart> charts = new HashMap<String, StateflowChart>();

	/** Create new machine. */
	public StateflowMachine(SimulinkModel model, ModelDataHandler dataHandler) {
		this.model = model;
		this.dataHandler = dataHandler;
		model.setStateflowMachine(this);
	}

	/**
	 * Copy constructor. This does not clone the charts as these are cloned via the
	 * {@link StateflowBlock}s they belong to.
	 *
	 * @param orig
	 *            original machine
	 * @param model
	 *            model the clone belongs to.
	 */
	public StateflowMachine(StateflowMachine orig, SimulinkModel model, ModelDataHandler dataHandler) {
		super(orig);
		this.model = model;
		this.dataHandler = dataHandler;
		model.setStateflowMachine(this);

		for (StateflowTarget target : orig.targets) {
			addTarget(target.deepClone());
		}

		// Charts are cloned via the Stateflow blocks
	}

	/**
	 * Add chart to the machine.
	 *
	 * @param fqName
	 *            full qualified name of the Stateflow block this chart belongs to.
	 * @param chart
	 *            the chart.
	 */
	public void addChart(String fqName, StateflowChart chart) {
		charts.put(fqName, chart);
		chart.setParent(this);
	}

	/** Add a target. */
	public void addTarget(StateflowTarget target) {
		targets.add(target);
		target.setParent(this);
	}

	/**
	 * This throws a {@link UnsupportedOperationException} as the machine can only
	 * be deep cloned by cloning the {@link SimulinkModel} it belongs to.
	 */
	@Override
	public StateflowMachine deepClone() {
		throw new UnsupportedOperationException("Clone model to clone machine.");
	}

	/**
	 * Get chart for full qualified name of the Stateflow block the chart belongs
	 * to.
	 */
	public StateflowChart getChart(String fqName) {
		return charts.get(fqName);
	}

	/** {@inheritDoc} */
	@Override
	public UnmodifiableCollection<StateflowChart> getCharts() {
		return CollectionUtils.asUnmodifiable(charts.values());
	}

	/** {@inheritDoc} */
	@Override
	public StateflowMachine getMachine() {
		return this;
	}

	/** Get the Simulink model this machine belongs to. */
	public SimulinkModel getModel() {
		return model;
	}

	/** Get targets of this machine. */
	public UnmodifiableSet<StateflowTarget> getTargets() {
		return CollectionUtils.asUnmodifiable(targets);
	}

	/** {@inheritDoc} */
	@Override
	public String getResolvedId() {
		// machine has no name, we use the resolvedId of the model instead
		return this.getModel().getResolvedId();
	}

	/**
	 * Removes the machine from the model. The Machine can be removed from the model
	 * only after all {@link StateflowBlock}s were removed.
	 */
	@Override
	public void remove() {
		CCSMAssert.isTrue(charts.isEmpty(), "All charts must be removed first (via removing the Stateflow blocks).");
		model.setStateflowMachine(null);
		model = null;

		for (StateflowTarget target : new ArrayList<StateflowTarget>(targets)) {
			target.remove();
		}
	}

	/** Remove chart. */
	/* package */void removeChart(StateflowChart chart) {
		CCSMAssert.isTrue(chart.getMachine() == this, "Machine does not contain chart with name " + chart);

		String fqName = chart.getStateflowBlock().getId();
		CCSMAssert.isTrue(charts.get(fqName) == chart, "Error in chart storage.");

		charts.remove(fqName);
		chart.removeNodes();
		chart.setStateflowBlock(null);
		chart.setParent(null);
	}

	/** Remove target. */
	/* package */void removeTarget(StateflowTarget target) {
		targets.remove(target);
		target.setParent(null);
	}

	/** Returns the model's data handler. */
	@ADeepCloneTestExclude
	/* package */ModelDataHandler getModelDataHandler() {
		return dataHandler;
	}
}