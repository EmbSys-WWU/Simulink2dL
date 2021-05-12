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
package org.conqat.lib.simulink.builder;

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_INTERSECTION;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_LINK_NODE;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_MACHINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_NAME_STATEFLOW;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_TREE_NODE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_CHART;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_DATA;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_DST;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_EML;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_EVENT;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_JUNCTION;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_MACHINE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SRC;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_STATE;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TARGET;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_TRANSITION;

import java.util.HashMap;
import java.util.Map;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandler;
import org.conqat.lib.simulink.model.stateflow.IStateflowNodeContainer;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowDeclBase;
import org.conqat.lib.simulink.model.stateflow.StateflowDeclContainerBase;
import org.conqat.lib.simulink.model.stateflow.StateflowElementBase;
import org.conqat.lib.simulink.model.stateflow.StateflowEvent;
import org.conqat.lib.simulink.model.stateflow.StateflowJunction;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.model.stateflow.StateflowTarget;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.model.stateflow.StateflowTruthTable;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This class builds the Stateflow part of the Simulink models.
 */
/* package */class StateflowBuilder {

	/** Logger. */
	private final ILogger logger;

	/**
	 * Whether to preserve unconnected lines. Otherwise unconnected lines are
	 * logged and discarded.
	 */
	private final boolean preserveUnconnectedLines;

	/** Maps from id to Statflow elements. */
	private final HashMap<String, StateflowElementBase<?>> elements = new HashMap<String, StateflowElementBase<?>>();

	/** The model. */
	private final SimulinkModel model;

	/**
	 * Create new Stateflow builder.
	 *
	 * @param model
	 *            model the parsed Stateflow parts belong to.
	 */
	public StateflowBuilder(ModelBuildingParameters modelBuildingParameters, SimulinkModel model) {
		this.model = model;
		this.logger = modelBuildingParameters.getLogger();
		this.preserveUnconnectedLines = modelBuildingParameters.isPreserveUnconnectedLines();
	}

	/**
	 * Build Stateflow parts based on Stateflow section from MDL file.
	 *
	 * @param stateflowSection
	 *            the section from the MDL file that describes the Stateflow
	 *            part
	 * @throws SimulinkModelBuildingException
	 *             if a problem occurred during building the Stateflow part
	 */
	public void buildStateflow(MDLSection stateflowSection, ModelDataHandler modelDataHandler)
			throws SimulinkModelBuildingException {

		buildMachine(stateflowSection.getFirstSubSection(SECTION_MACHINE), modelDataHandler);

		// the Stateflow section in the MDL file is organized in a
		// non-hierarchical manner, therefore we simply process one item after
		// each other, store it and build the relations in a second step
		for (MDLSection section : stateflowSection.getSubSections()) {
			StateflowElementBase<?> element = createElementFromSection(section);
			if (element != null) {
				process(section, element);
			}
		}

		for (StateflowElementBase<?> element : elements.values()) {
			buildRelation(element);
		}

		for (MDLSection transition : stateflowSection.getSubSections(SECTION_TRANSITION)) {
			buildTransition(transition);
		}
	}

	/**
	 * Create Stateflow element from MDL section name. This does not create
	 * elements for all sections as some, e.g. transitions are created
	 * elsewhere.
	 *
	 * @return <code>null</code> for unknown section name.
	 */
	private static StateflowElementBase<?> createElementFromSection(MDLSection section) {
		String name = section.getName();
		if (name.equals(SECTION_CHART)) {
			return new StateflowChart();
		}
		if (name.equals(SECTION_STATE)) {
			if (section.hasSubSections("truthTable")) {
				return new StateflowTruthTable();
			}
			return new StateflowState();
		}
		if (name.equals(SECTION_JUNCTION)) {
			return new StateflowJunction();
		}
		if (name.equals(SECTION_EVENT)) {
			return new StateflowEvent();
		}
		if (name.equals(SECTION_DATA)) {
			return new StateflowData();
		}
		if (name.equals(SECTION_TARGET)) {
			return new StateflowTarget();
		}
		return null;
	}

	/** Build machine. */
	private void buildMachine(MDLSection machineSection, ModelDataHandler modelDataHandler)
			throws SimulinkModelBuildingException {
		if (machineSection == null) {
			throw new SimulinkModelBuildingException("No Stateflow machine defined!");
		}

		StateflowMachine machine = new StateflowMachine(model, modelDataHandler);
		process(machineSection, machine);
	}

	/**
	 * Recursively add parameter defined in the section to the Stateflow element
	 * and store it in the map {@link #elements}.
	 *
	 * @throws SimulinkModelBuildingException
	 *             if id of the element could be determined or multiple elements
	 *             with the same id were detected.
	 */
	private void process(MDLSection section, StateflowElementBase<?> element) throws SimulinkModelBuildingException {
		addParameters(section, element);

		String id = element.getParameter(PARAM_ID);

		// add stateflowChart to state if this is a subchart node
		if (element instanceof StateflowState && ((StateflowState) element).isSubChart()) {
			StateflowChart chart = new StateflowChart();
			SimulinkUtils.copyParameters(element, chart);
			((StateflowState) element).setSubViewer(chart);
		}

		if (element instanceof StateflowTruthTable) {
			StateflowTruthTable truthTable = (StateflowTruthTable) element;
			if (section.hasSubSections(SECTION_EML)) {
				String script = section.getFirstSubSection(SECTION_EML).getParameter(SimulinkConstants.PARAM_SCRIPT,
						StringUtils.EMPTY_STRING);
				truthTable.setScript(script.replaceAll("\\\\n", "\n"));
			}
		}

		if (id == null) {
			throw new SimulinkModelBuildingException("Element has no id.", section);
		}
		if (elements.containsKey(id)) {
			throw new SimulinkModelBuildingException("Duplicate id " + id + ".", section);
		}
		elements.put(id, element);
	}

	/**
	 * Recursively add parameter defined in the section to the element.
	 */
	private static void addParameters(MDLSection section, ParameterizedElement element) {
		for (Map.Entry<String, String> parameter : section.getParameterMapRecursively().entrySet()) {
			element.setParameter(parameter.getKey(), parameter.getValue());
		}
	}

	/** Build relation for Stateflow element. */
	private void buildRelation(StateflowElementBase<?> element) throws SimulinkModelBuildingException {
		if (element instanceof StateflowState) {
			if (isTopLevelElementInSubView(element, PARAM_TREE_NODE)) {
				addToChart(element);
			} else {
				buildNodeRelation((StateflowState) element, PARAM_TREE_NODE);
			}
			return;
		}
		if (element instanceof StateflowJunction) {
			if (isTopLevelElementInSubView(element, PARAM_LINK_NODE)) {
				addToChart(element);
			} else {
				buildNodeRelation((StateflowJunction) element, PARAM_LINK_NODE);
			}
			return;
		}
		if (element instanceof StateflowEvent) {
			buildEventRelation((StateflowEvent) element);
			return;
		}
		if (element instanceof StateflowData) {
			buildDataRelation((StateflowData) element);
			return;
		}
		if (element instanceof StateflowTarget) {
			buildTargetRelation((StateflowTarget) element);
			return;
		}
		if (element instanceof StateflowChart) {
			buildChartRelation((StateflowChart) element);
			return;
		}
		if (element instanceof StateflowMachine) {
			// the relation for the machine is already built by
			// StateflowMachine.setSimulinkModel()
			return;
		}

		CCSMAssert.fail("Unkown case: " + element.getClass().getName());
	}

	/**
	 * Returns whether element is a at the top level in a subviewer (or in the
	 * main viewer)
	 */
	private boolean isTopLevelElementInSubView(StateflowElementBase<?> element, String primaryRelationshipParam)
			throws SimulinkModelBuildingException {
		StateflowElementBase<?> primaryParent = getRelatedElementFromArrayParameter(element, primaryRelationshipParam);
		StateflowElementBase<?> parentSubview = getRelatedElementFromParameter(element,
				SimulinkConstants.PARAM_SUBVIEWER);
		return primaryParent == parentSubview;
	}

	/**
	 * Adds this element as top-level element to the corresponding chart (or
	 * subchart).
	 */
	private void addToChart(StateflowElementBase<?> element) throws SimulinkModelBuildingException {
		StateflowElementBase<?> parentElement = getRelatedElementFromParameter(element,
				SimulinkConstants.PARAM_SUBVIEWER);

		StateflowChart parentChart;
		if (parentElement instanceof StateflowChart) {
			parentChart = (StateflowChart) parentElement;
		} else if (parentElement instanceof StateflowState) {
			CCSMAssert.isTrue(((StateflowState) parentElement).isSubChart(),
					"StateflowChart is child of non-subchart StateflowState!");
			parentChart = ((StateflowState) parentElement).getSubViewer();
		} else {
			logger.error(
					"Found a simulink element with \"subviewer\" param that is not pointing to a stateflowState. StateflowId: "
							+ element.getStateflowId());
			return;
		}

		if (element instanceof StateflowNodeBase) {
			parentChart.addNode((StateflowNodeBase) element);
		} else if (element instanceof StateflowData) {
			parentChart.addData((StateflowData) element);
		} else if (element instanceof StateflowEvent) {
			parentChart.addEvent((StateflowEvent) element);
		} else {
			logger.warn("New StateflowNodeBase subclass is not handled in StateFlowBuilder.addToChart");
		}
	}

	/** Build node relation. */
	private void buildNodeRelation(StateflowNodeBase node, String relationParam) throws SimulinkModelBuildingException {
		StateflowElementBase<?> relatedElement = getRelatedElementFromArrayParameter(node, relationParam);
		if (!(relatedElement instanceof IStateflowNodeContainer<?>)) {
			throw new SimulinkModelBuildingException(relatedElement + " cannot be parent of " + node);
		}
		IStateflowNodeContainer<?> parent = (IStateflowNodeContainer<?>) relatedElement;
		if (parent instanceof StateflowState && ((StateflowState) parent).isSubChart()) {
			((StateflowState) parent).getSubViewer().addNode(node);
		} else {
			parent.addNode(node);
		}
	}

	/**
	 * Get element related to the given element from an parameter array.
	 *
	 * @param element
	 *            relationship origin
	 * @param relationshipParameter
	 *            parameter that specifies the relationship in the MDL file.
	 * @return the related element
	 * @throws SimulinkModelBuildingException
	 *             if relationship could not be established.
	 */
	private StateflowElementBase<?> getRelatedElementFromArrayParameter(StateflowElementBase<?> element,
			String relationshipParameter) throws SimulinkModelBuildingException {
		String array = element.getParameter(relationshipParameter);
		if (array == null) {
			throw new SimulinkModelBuildingException("Relationsship parameter " + relationshipParameter
					+ " not found for element with id " + element.getStateflowId() + ".");
		}
		String[] relationship = SimulinkUtils.getStringParameterArray(array);
		if (relationship.length == 0) {
			throw new SimulinkModelBuildingException("Relationsship parameter " + relationshipParameter
					+ " not found for element with id " + element.getStateflowId() + ".");
		}
		return elements.get(relationship[0]);
	}

	/**
	 * Get element related to the given element.
	 *
	 * @throws SimulinkModelBuildingException
	 *             if relationship could not be established.
	 */
	private StateflowElementBase<?> getRelatedElementFromParameter(StateflowElementBase<?> element,
			String relationshipParameter) throws SimulinkModelBuildingException {
		String parameter = element.getParameter(relationshipParameter);
		if (parameter == null) {
			throw new SimulinkModelBuildingException("Relationsship parameter " + relationshipParameter
					+ " not found for element with id " + element.getStateflowId() + ".");
		}
		return elements.get(parameter);
	}

	/** Build event relation. */
	private void buildEventRelation(StateflowEvent element) throws SimulinkModelBuildingException {
		StateflowDeclContainerBase<?> parent = determineParent(element);
		parent.addEvent(element);
	}

	/** Build relation for data. */
	private void buildDataRelation(StateflowData element) throws SimulinkModelBuildingException {
		StateflowDeclContainerBase<?> parent = determineParent(element);
		parent.addData(element);
	}

	/** Determine parent of a Stateflow declaration (event or data). */
	private StateflowDeclContainerBase<?> determineParent(StateflowDeclBase element)
			throws SimulinkModelBuildingException {
		StateflowElementBase<?> relatedElement = getRelatedElementFromArrayParameter(element, PARAM_LINK_NODE);
		if (!(relatedElement instanceof StateflowDeclContainerBase<?>)) {
			throw new SimulinkModelBuildingException(relatedElement + " cannot be parent of " + element);
		}
		return (StateflowDeclContainerBase<?>) relatedElement;
	}

	/** Build target relation. */
	private void buildTargetRelation(StateflowTarget element) throws SimulinkModelBuildingException {
		StateflowElementBase<?> relatedElement = getRelatedElementFromArrayParameter(element, PARAM_LINK_NODE);
		StateflowMachine parent = castToMachine(relatedElement, element);
		parent.addTarget(element);
	}

	/** Build relation for charts. */
	private void buildChartRelation(StateflowChart element) throws SimulinkModelBuildingException {
		StateflowElementBase<?> relatedElement = elements.get(element.getParameter(PARAM_MACHINE));
		StateflowMachine parent = castToMachine(relatedElement, element);
		String fqName = model.getName() + "/" + element.getParameter(PARAM_NAME_STATEFLOW);
		parent.addChart(fqName, element);
	}

	/**
	 * Cast <code>machineElement</code> to machine and check that it is the
	 * machine associated with the Simulink model.
	 *
	 * @param element
	 *            the element related to the machine (this is used for possible
	 *            error message only)
	 * @throws SimulinkModelBuildingException
	 *             if <code>machineElement</code> does not refer to the only
	 *             existant machine.
	 */
	private StateflowMachine castToMachine(StateflowElementBase<?> machineElement, StateflowElementBase<?> element)
			throws SimulinkModelBuildingException {
		if (machineElement != model.getStateflowMachine()) {
			throw new SimulinkModelBuildingException(
					element + " must belong to machine " + model.getStateflowMachine());
		}
		return (StateflowMachine) machineElement;
	}

	/** Build transition. */
	private void buildTransition(MDLSection section) throws SimulinkModelBuildingException {
		String srcId = getId(section, SECTION_SRC);
		String dstId = getId(section, SECTION_DST);

		if (!preserveUnconnectedLines && srcId == null && dstId == null) {
			logger.warn("Found null->null transition. Ignoring transition.");
		}

		if (!preserveUnconnectedLines && dstId == null) {
			logger.warn("Found transition without destination. Ignoring transition.");
			return;
		}

		StateflowTransition transition = new StateflowTransition(getNode(srcId, section), getNode(dstId, section));
		addParameters(section, transition);

		if (srcId == null && dstId == null) {
			String chartId = transition.getParameter(SimulinkConstants.PARAM_CHART);
			String subviewer = transition.getParameter(SimulinkConstants.PARAM_SUBVIEWER);
			if (subviewer == null && chartId == null) {
				logger.error("Missing chart id for unconnected transition. Ignoring the transition.");
				return;
			}
			// subviewer has preference over chartId. We add the transition to
			// the subchart instead of to the parent chart.
			if (subviewer != null && !subviewer.isEmpty()) {
				chartId = subviewer;
			}

			StateflowElementBase<?> chart = elements.get(chartId);
			if (chart instanceof StateflowChart) {
				((StateflowChart) chart).addUnconnectedTransition(transition);
			} else if (chart instanceof StateflowState) {
				CCSMAssert.isTrue(((StateflowState) chart).isSubChart(),
						"StateflowChart is child of non-subchart StateflowState!");
				chart.getParentChart().addUnconnectedTransition(transition);
			} else {
				logger.error("Chart id " + chartId + " for unconnected transition does not refer to a chart!");
			}
		}

		copyIntersection(section, SECTION_SRC, transition);
		copyIntersection(section, SECTION_DST, transition);
	}

	/**
	 * Copies the intersection parameter from the src/dst section to the
	 * transition (using src/dst prefix).
	 */
	private void copyIntersection(MDLSection section, String subSectionName, StateflowTransition transition) {
		MDLSection subSection = section.getFirstSubSection(subSectionName);
		if (subSection == null || !subSection.hasParameter(PARAM_INTERSECTION)) {
			logger.error("Missing intersection. Transition " + transition.getLabel() + " is missing a " + subSectionName
					+ " intersection.");
			return;
		}
		transition.setParameter(subSectionName + "_" + PARAM_INTERSECTION, subSection.getParameter(PARAM_INTERSECTION));
	}

	/**
	 * Get parameter 'id' in the first sub section with a given name.
	 */
	private static String getId(MDLSection section, String subSectionName) {
		MDLSection subSection = section.getFirstSubSection(subSectionName);
		if (subSection == null) {
			return null;
		}
		return subSection.getParameter(PARAM_ID);
	}

	/**
	 * Get Stateflow node with given id. Returns null on a null id.
	 *
	 * @throws SimulinkModelBuildingException
	 *             if no node was found for the given id.
	 */
	private StateflowNodeBase getNode(String id, MDLSection section) throws SimulinkModelBuildingException {
		if (id == null) {
			return null;
		}

		StateflowElementBase<?> element = elements.get(id);

		if (element == null) {
			throw new SimulinkModelBuildingException("Stateflow element with id " + id + " not found.", section);
		}

		if (!(element instanceof StateflowNodeBase)) {
			throw new SimulinkModelBuildingException(
					"Only Stateflow nodes can be source or destination of transitions.", section);
		}

		return (StateflowNodeBase) element;
	}
}
