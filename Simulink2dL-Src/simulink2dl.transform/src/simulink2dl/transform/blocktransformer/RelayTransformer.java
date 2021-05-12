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
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.MacroContainer;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.parser.StringToTerm;

public class RelayTransformer extends BlockTransformer {

	public RelayTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		for (Macro macro : macros) {
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();

		String type = "Relay";
		checkBlock(type, block);

		// add variable
		Variable variable = new Variable("R", block.getName());
		dlModel.addVariable(variable);

		// get connected port
		SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, 1);
		String connectedPortID = environment.getPortID(connectedPort);

		// get conditions
		String switchOn = block.getParameter("OnSwitchValue");
		String outputOn = block.getParameter("OnOutputValue");
		Relation testOnTrue = new Relation(new PortIdentifier(connectedPortID),
				environment.transformRelationType(Relation.RelationType.GREATER_EQUAL),
				StringToTerm.parseString(switchOn));
		Relation testOnFalse = testOnTrue.createNegation(environment.useOverlappingBounds());

		String switchOff = block.getParameter("OffSwitchValue");
		String outputOff = block.getParameter("OffOutputValue");
		Relation testOffTrue = new Relation(new PortIdentifier(connectedPortID),
				environment.transformRelationType(Relation.RelationType.LESS_EQUAL),
				StringToTerm.parseString(switchOff));
		Relation testOffFalse = testOffTrue.createNegation(environment.useOverlappingBounds());

		// case on
		DiscreteAssignment caseOn = new DiscreteAssignment(variable, new RealTerm(outputOn));

		// case off
		DiscreteAssignment caseOff = new DiscreteAssignment(variable, new RealTerm(outputOff));

		// case hold
		HybridProgramCollection caseHold = new HybridProgramCollection();

		// add conditional choice
		ConditionalChoice choice = new ConditionalChoice();
		choice.addChoice(testOnTrue, caseOn);
		choice.addChoice(testOffTrue, caseOff);
		choice.addChoice(new Conjunction(testOnFalse, testOffFalse), caseHold);
		dlModel.addBehavior(choice);
		
		//add initial conditions:
		Relation initialOnValue = new Relation(variable,  RelationType.EQUAL, new RealTerm(outputOn));
		Relation initialOffValue = new Relation(variable,  RelationType.EQUAL, new RealTerm(outputOff));
		dlModel.addInitialCondition(new Implication(testOnTrue, initialOnValue));
		dlModel.addInitialCondition(new Implication(testOffTrue, initialOffValue));
		dlModel.addInitialCondition(new Implication(new Conjunction(testOnFalse, testOffFalse), initialOffValue));

		// add macro
		Term replaceWith = variable;
		macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));

		// consider different cases for continuous evolutions
		// get switch output port identifier
		ReplaceableTerm dummy1 = new PortIdentifier("DummyString1");
		ReplaceableTerm dummy2 = new PortIdentifier("DummyString2");

		SimpleMacro dummyMacro = new SimpleMacro(dummy1, dummy2);

		Relation relationOn = null;
		Relation relationOff = null;
		if (this.getHandleStructures()) {
			relationOn = new Relation(variable, RelationType.EQUAL, new RealTerm(outputOn));
			relationOff = new Relation(variable, RelationType.EQUAL, new RealTerm(outputOff));
		}
		// case on
		MacroContainer macroContainerOn = new MacroContainer(dummyMacro, testOnTrue, relationOn);
		// case off
		MacroContainer macroContainerOff = new MacroContainer(dummyMacro, testOffTrue, relationOff);
		// case hold
		MacroContainer macroContainerHold = new MacroContainer(dummyMacro, new Conjunction(testOnFalse, testOffFalse),
				null);

		// add alternative to model
		ConditionalMacro macro = new ConditionalMacro(dummy1, macroContainerOn, macroContainerOff, macroContainerHold);
		macros.add(macro);

		return macros;
	}

}
