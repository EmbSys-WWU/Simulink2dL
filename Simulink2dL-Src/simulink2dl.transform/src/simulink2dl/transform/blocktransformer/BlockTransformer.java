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
package simulink2dl.transform.blocktransformer;

import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.macro.SizePropagationMacro;
import simulink2dl.transform.model.DiscreteBehavior;
import simulink2dl.util.PluginLogger;

public abstract class BlockTransformer {

	protected SimulinkModel simulinkModel;
	protected DLModelSimulink dlModel;

	protected boolean handleControlFlow;

	protected Environment environment;

	public abstract void transformBlock(SimulinkBlock block);

	public abstract List<Macro> createMacro(SimulinkBlock block);

	public BlockTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		this.simulinkModel = simulinkModel;
		this.dlModel = dlModel;
		this.environment = environment;
		this.handleControlFlow = false;
	}

	protected void createDelay(SimulinkBlock block, int delay, String[] initialConditions) {
		// get connected port
		SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, 1);
		String connectedPortID = environment.getPortID(connectedPort);
		ReplaceableTerm connectedPortToReplace = new PortIdentifier(connectedPortID);

		// get discrete behavior element with sample time of this block
		String sampleTime = getBlockSampleTime(block);
		DiscreteBehavior discreteBehavior = dlModel.getDiscreteBehavior(sampleTime);

		// add variable
		Variable delayOut = new Variable("R", block.getName() + "Out");
		dlModel.addVariable(delayOut);

		// add delay states
		Variable[] delayStates = new Variable[delay];

		// create delay variables
		for (int i = 0; i < delay; i++) {
			delayStates[i] = new Variable("R", block.getName() + "State" + i);
			dlModel.addVariable(delayStates[i]);
			// add initial condition for each variable
			if(i==0) {
				// initial value of the first delay state is equal to the input signal of the delay
				dlModel.addInitialCondition(
						new Relation(delayStates[i], RelationType.EQUAL, new PortIdentifier(connectedPortID)));
			} else {
				dlModel.addInitialCondition(
						new Relation(delayStates[i], RelationType.EQUAL, new RealTerm(initialConditions[i-1])));
			}
			// Size propagation from input to delay states
			dlModel.addMacro(new SizePropagationMacro(connectedPortToReplace, delayStates[i]));

		}

		// add assignment to output
		if (delay > 0) {
			discreteBehavior.addOutputUpdate(new DiscreteAssignment(delayOut, delayStates[delay - 1]));
			// output is initially equal to last delay variable
			dlModel.addInitialCondition(new Relation(delayOut, RelationType.EQUAL, new RealTerm(initialConditions[delay-1])));
		} else {
			discreteBehavior.addOutputUpdate(new DiscreteAssignment(delayOut, new PortIdentifier(connectedPortID)));
			// output is initially equal to input
			dlModel.addInitialCondition(new Relation(delayOut, RelationType.EQUAL,  new PortIdentifier(connectedPortID)));
		}

		// add delay assignments
		for (int i = delay - 1; i >= 0; i--) {
			// add assignment
			if (i == 0) {
				discreteBehavior
						.addBehavior(new DiscreteAssignment(delayStates[0], new PortIdentifier(connectedPortID)));
			} else {
				discreteBehavior.addBehavior(new DiscreteAssignment(delayStates[i], delayStates[i - 1]));
			}
		}

		// add output macro
		Term replaceWith = delayOut;
		// Size propagation from input to output
		dlModel.addMacro(new SizePropagationMacro(connectedPortToReplace, delayOut));
		dlModel.addMacro(new SimpleMacro(environment.getToReplace(block), replaceWith));
	}

	/**
	 * Checks whether the given type equals the type of the given block. Returns
	 * true if the types are equal, otherwise returns false.
	 * 
	 * @param type
	 * @param block
	 * @return
	 */
	protected boolean checkBlock(String type, SimulinkBlock block) {
		if (type.equals(block.getType())) {
			return true;
		}
		PluginLogger.error("Trying to transform block of type \"" + block.getType()
				+ "\" with transformation for type \"" + type + "\".");
		return false;
	}

	/**
	 * Returns the sample time of the given block. For sample times of inherited
	 * ("-1") the appropriate outer sample time is determined.
	 * 
	 * @param block
	 * @return
	 */
	protected String getBlockSampleTime(SimulinkBlock block) {
		String sampleTime = block.getParameter("SampleTime");

		if (sampleTime == null || sampleTime.isEmpty()) {
			PluginLogger.error("Block " + block.getName() + " has no given sample time.");
			sampleTime = "-1";
		}

		if (sampleTime.equals("-1")) {
			// TODO consider possible outer subsystem block
			PluginLogger.error("No Sample time given, check in configuration not yet implemented!");
			sampleTime = "0.2";
			// i.e. sampleTime = getSampleTime(subsystemblock);
//			Set<Configuration> configs = simulinkModel.getConfigurations();
//			for (Configuration config : configs) {
//				if (!config.getName().equals("Simulink.SolverCC")) {
//					continue;
//				}
//				sampleTime = config.getParameter().get("FixedStep");
//				break;
//			}
		}

		if (sampleTime.equals("auto")) {
			PluginLogger.error("Found sample time \"auto\".");
			sampleTime = "0.2";
		}

		return sampleTime;
	}

	public void setHandleControlFlow(boolean value) {
		this.handleControlFlow = value;
	}

	public boolean getHandleStructures() {
		return this.handleControlFlow;
	}

}
