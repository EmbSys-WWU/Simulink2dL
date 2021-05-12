/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of Münster
 * SESE Software and Embedded Systems Engineering, TU Berlin
 * 
 * Authors:
 * 	Paula Herber
 * 	Sabine Glesner
 * 	Timm Liebrenz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package simulink2dl.transform;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.simulink_transformer.PortMapping;

/**
 * This class represents the environment for the transformation. It contains
 * transformation flags and general information about transformed blocks, like
 * macro names for outputs.
 * 
 * @author Timm Liebrenz
 *
 */
public class Environment {

	private DLModelSimulink dlModel;

	private SimulinkModel simulinkModel;

	private Set<HybridContract> contracts;

	private boolean useSmallStep;
	private boolean useEpsilon;
	private boolean useOverlappingBounds;

	private List<PortMapping> portIDMappings;

	// special system variables
	private Variable epsilon;
	private String epsValue = "0.01";

	/**
	 * Private constructor
	 */
	public Environment(DLModelSimulink dlModel, SimulinkModel simulinkModel, Set<HybridContract> contracts) {
		this.dlModel = dlModel;
		this.simulinkModel = simulinkModel;
		this.contracts = contracts;

		// TODO get Flags as input parameter and set them in the gui
		this.useSmallStep = true;
		this.useEpsilon = false;
		this.useOverlappingBounds = false;

		this.portIDMappings = new LinkedList<PortMapping>();

		epsilon = new Variable("R", "epsilon");
	}

	/**
	 * Returns the unique String that is associated with the given port. If no such
	 * String exist, a new one is created and mapped to the port for future use.
	 */
	public String getPortID(SimulinkPortBase port) {
		checkPort(port);

		// search for existing ID
		for (PortMapping portIDMapping : portIDMappings) {
			if (portIDMapping.samePort(port)) {
				return portIDMapping.getIdentifier();
			}
		}

		// create new ID
		String newID = "#out" + portIDMappings.size();
		portIDMappings.add(new PortMapping(port, newID));

		return newID;
	}

	public boolean useEpsilon() {
		return useEpsilon;
	}

	public Variable getEpsilonVariable() {
		return epsilon;
	}

	public boolean useOverlappingBounds() {
		return useOverlappingBounds;
	}

	public Relation.RelationType transformRelationType(Relation.RelationType input) {
		switch (input) {
		case LESS_THAN:
		case LESS_EQUAL:
			return Relation.RelationType.LESS_EQUAL;
		case GREATER_EQUAL:
		case GREATER_THAN:
			return Relation.RelationType.GREATER_EQUAL;
		case EQUAL:
			return Relation.RelationType.EQUAL;
		case NOT_EQUAL:
			return Relation.RelationType.NOT_EQUAL;
		default:
			PluginLogger.error("Invalid relation type");
			return null;
		}
	}

	public void prepareModel() {
		if (useSmallStep) {
			Constant stepSize;
			Variable smallStep;
			Relation initCondition;

			// constant small step size
			stepSize = new Constant("R", "SMALLSTEPSIZE");
			initCondition = new Relation(stepSize, Relation.RelationType.EQUAL, new RealTerm(0.01));
			dlModel.addConstant(stepSize);
			dlModel.addInitialCondition(initCondition);
		}
	}

	public void finalizeEnvironment() {
		if (useSmallStep) {
			Constant stepSize = dlModel.getConstantByName("SMALLSTEPSIZE");
			Variable smallStep;
			Relation initCondition;

			// variable small step
			smallStep = new Variable("R", "smallStep");
			initCondition = new Relation(smallStep, Relation.RelationType.EQUAL, new RealTerm(0.0));
			dlModel.addVariable(smallStep);
			dlModel.addInitialCondition(initCondition);
			dlModel.addContinuousEvolution(smallStep, new RealTerm(1.0));

			// add reset of small step variable
			DiscreteAssignment reset = new DiscreteAssignment(smallStep, new RealTerm(0.0));
			dlModel.addBehaviorFront(reset);

			// add small step to all evolution domains
			Formula evolutionDomain = new Relation(smallStep, Relation.RelationType.LESS_EQUAL, stepSize);
			dlModel.addAlternativeToAllEvolutionDomains(evolutionDomain);
		}
		if (useEpsilon) {
			// constant EPS
			Constant eps = new Constant("R", "EPS");
			Relation initCondition = new Relation(eps, Relation.RelationType.EQUAL, new RealTerm(epsValue));
			dlModel.addConstant(eps);
			dlModel.addInitialCondition(initCondition);

			// variable epsilon
			dlModel.addVariable(epsilon);

			// add epsilon assignment
			HybridProgramCollection epsilonAssignment = new HybridProgramCollection();

			epsilonAssignment.addElement(new NondeterministicAssignment(epsilon));
			AdditionTerm negativeEps = new AdditionTerm();
			negativeEps.subtract(eps);
			Formula lowerBound = new Relation(epsilon, Relation.RelationType.GREATER_EQUAL, negativeEps);
			Formula upperBound = new Relation(epsilon, Relation.RelationType.LESS_EQUAL, eps);
			Formula epsilonBounds = new Conjunction(lowerBound, upperBound);
			epsilonAssignment.addElement(new TestFormula(epsilonBounds));

			dlModel.addBehaviorFront(epsilonAssignment);
		}
	}

	// TODO: DEBUG
	public void checkModelIntegrity() {
		UnmodifiableCollection<SimulinkBlock> blocks = simulinkModel.getSubBlocks();
		for (SimulinkLine signalLine : simulinkModel.getContainedLinesRecursively()) {
			if (!blocks.contains(signalLine.getSrcPort().getBlock())) {
				PluginLogger
						.error("Source Block " + signalLine.getSrcPort().getBlock().toString() + " of signal line "
								+ signalLine.toString() + " is not present in Simulink model. Destination Block is "
								+ signalLine.getDstPort().getBlock().toString());
			}
			if (!blocks.contains(signalLine.getDstPort().getBlock())) {
				PluginLogger
						.error("Destination Block " + signalLine.getDstPort().getBlock().toString() + " of signal line "
								+ signalLine.toString() + " is not present in Simulink model. Source Block is "
								+ signalLine.getSrcPort().getBlock().toString());
			}
		}
	}

	private void checkPort(SimulinkPortBase toCheck) {
		// TODO DEBUG
		boolean found = false;
		for (SimulinkBlock block : simulinkModel.getSubBlocks()) {
			for (SimulinkPortBase blockPort : block.getInPorts()) {
				if (toCheck.equals(blockPort)) {
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
			for (SimulinkPortBase blockPort : block.getOutPorts()) {
				if (toCheck.equals(blockPort)) {
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}

		if (!found) {
			PluginLogger.error("Could not find port in system: " + toCheck.toString());
		}

	}

	/**
	 * @return the contracts
	 */
	public Set<HybridContract> getContracts() {
		return contracts;
	}

	/**
	 * Returns the port that is connected to the input port of the given block at
	 * the given position. (i.e. the source port of the signal that is connected to
	 * the in port is returned)
	 * 
	 * @param block
	 * @param portNumber
	 * @return
	 */
	public SimulinkOutPort getConnectedOuputPort(SimulinkBlock block, int portNumber) {
		SimulinkInPort inPort = block.getInPort(Integer.toString(portNumber));

		return getConnectedOuputPort(inPort);
	}

	/**
	 * Returns the port that is connected to the given input port. (i.e. the source
	 * port of the signal that is connected to the in port is returned)
	 * 
	 * @param inPort
	 * @return
	 */
	public SimulinkOutPort getConnectedOuputPort(SimulinkInPort inPort) {
		return inPort.getLine().getSrcPort();
	}

	/**
	 * Returns the toReplace term of the output port of this block. Only applicable
	 * for blocks with exactly one output port.
	 * 
	 * @param block
	 * @return
	 */
	public ReplaceableTerm getToReplace(SimulinkBlock block) {
		// get toReplace term
		SimulinkOutPort outputPort = block.getOutPort("1");
		return getToReplace(outputPort);
	}

	/**
	 * Returns the toReplace term of the given output port.
	 * 
	 * @param outputPort
	 * @return
	 */
	public ReplaceableTerm getToReplace(SimulinkOutPort outputPort) {
		// get toReplace term
		String outputPortID = getPortID(outputPort);

		ReplaceableTerm toReplace = new PortIdentifier(outputPortID);
		return toReplace;
	}

	/**
	 * Returns the toReplace term of the given input port.
	 * 
	 * @param inputPort
	 * @return
	 */
	public ReplaceableTerm getToReplace(SimulinkInPort inputPort) {
		// get toReplace term
		String inputPortID = getPortID(inputPort);

		ReplaceableTerm toReplace = new PortIdentifier(inputPortID);
		return toReplace;
	}
}
