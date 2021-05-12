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

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.model.DiscreteBehavior;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringParser;

public class DiscreteIntegratorTransformer extends BlockTransformer {

	public DiscreteIntegratorTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel,
			Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "DiscreteIntegrator";
		checkBlock(type, block);

		String gainString = block.getParameter("gainval");

		double gain = StringParser.parseScalar(gainString);

		// get connected port
		SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, 1);
		String connectedPortID = environment.getPortID(connectedPort);

		// get discrete behavior element with sample time of this block
		String sampleTimeString = getBlockSampleTime(block);
		DiscreteBehavior discreteBehavior = dlModel.getDiscreteBehavior(sampleTimeString);
		double sampleTime = StringParser.parseScalar(sampleTimeString);

		// add variables
		// integrator state and output
		Variable discreteIntegratorState = new Variable("R", block.getName() + "State");
		dlModel.addVariable(discreteIntegratorState);
		// input signal state
		Variable inputState = new Variable("R", block.getName() + "Input");
		dlModel.addVariable(inputState);

		// add initial condition
		String initCondition = block.getParameter("InitialCondition");
		dlModel.addInitialCondition(
				new Relation(discreteIntegratorState, RelationType.EQUAL, new RealTerm(initCondition)));
		//input ID
		Term inputSignal = new PortIdentifier(connectedPortID);
		dlModel.addInitialCondition(new Relation(inputState, RelationType.EQUAL, inputSignal));


		// discrete integrator behavior:
		// Forward Euler method:
		// y(n) = y(n-1) + K*[t(n)-t(n-1)]*u(n-1)
		Term behavior = new AdditionTerm(discreteIntegratorState,
				new MultiplicationTerm(new RealTerm(gain), new RealTerm(sampleTime), inputState));
		discreteBehavior.addBehavior(new DiscreteAssignment(discreteIntegratorState, behavior));
		// for forward euler, update the input after the calculation
		discreteBehavior.addBehavior(new DiscreteAssignment(inputState, inputSignal));

		// TODO check selected behavior and add missing
		// Backward Euler method:
		// y(n) = y(n-1) + K*[t(n)-t(n-1)]*u(n)
		//
		// Trapezoidal method:
		// y(n) = y(n-1) + K*[t(n)-t(n-1)]*[u(n)+u(n-1)]/2

		// add output macro
		Term replaceWith = discreteIntegratorState;
		dlModel.addMacro(new SimpleMacro(environment.getToReplace(block), replaceWith));
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

}
