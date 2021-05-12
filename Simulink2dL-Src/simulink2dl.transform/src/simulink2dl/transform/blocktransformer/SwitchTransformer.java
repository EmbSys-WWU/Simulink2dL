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

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.MacroContainer;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.macro.SizePropagationMacro;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringParser;

public class SwitchTransformer extends BlockTransformer {

	public SwitchTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		// add alternative to model
		for (Macro macro : macros) {
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		String type = "Switch";
		checkBlock(type, block);

		// add variable
		Variable variable = new Variable("R", block.getName());
		dlModel.addVariable(variable);

		// get connected ports
		SimulinkOutPort caseTruePort = environment.getConnectedOuputPort(block, 1);
		String caseTruePortID = environment.getPortID(caseTruePort);
		SimulinkOutPort casefalsePort = environment.getConnectedOuputPort(block, 3);
		String caseFalsePortID = environment.getPortID(casefalsePort);
		SimulinkOutPort conditionPort = environment.getConnectedOuputPort(block, 2);
		String conditionPortID = environment.getPortID(conditionPort);

		// get condition
		Relation testTrue = generateSwitchCondition(conditionPortID, block, simulinkModel);
		Relation testFalse = testTrue.createNegation(environment.useOverlappingBounds());

		// TODO implement size propagation for control flow blocks
		//SizePropagationMacro SizePropagationMacroTrue = new SizePropagationMacro(new ReplaceableTerm(caseTruePortID), environment.getToReplace(block));
		//dlModel.addMacro(SizePropagationMacroTrue);
		
		SimpleMacro macroTrue = new SimpleMacro(environment.getToReplace(block), new PortIdentifier(caseTruePortID));
		MacroContainer macroContainerTrue = new MacroContainer(macroTrue, testTrue, null);

		// TODO implement size propagation for control flow blocks
		//SizePropagationMacro SizePropagationMacroFalse = new SizePropagationMacro(new ReplaceableTerm(caseFalsePortID), environment.getToReplace(block));
		//dlModel.addMacro(SizePropagationMacroFalse);
		
		SimpleMacro macroFalse = new SimpleMacro(environment.getToReplace(block), new PortIdentifier(caseFalsePortID));
		MacroContainer macroContainerFalse = new MacroContainer(macroFalse, testFalse, null);

		macros.add(new ConditionalMacro(environment.getToReplace(block), macroContainerTrue, macroContainerFalse));
		return macros;
	}

	private Relation generateSwitchCondition(String inputSignal, SimulinkBlock block, SimulinkModel simulinkModel) {
		String thresholdValue = block.getParameter("Threshold");
		Term threshold;
		if(StringParser.isNumber(thresholdValue)) {
			threshold = new RealTerm(thresholdValue);
		} else {
			threshold = new Constant("R", thresholdValue);
			dlModel.addConstant((Constant)threshold);
		}
		String criteria = block.getParameter("Criteria");

		Term inputTerm = new PortIdentifier(inputSignal);

		if (environment.useEpsilon()) {
			AdditionTerm addEps = new AdditionTerm();
			addEps.add(inputTerm, environment.getEpsilonVariable());
			inputTerm = addEps;
		}

		// with overlapping bounds, change the criteria
		if (environment.useOverlappingBounds() && criteria.equals("u2 > Threshold")) {
			criteria = "u2 >= Threshold";
		}

		switch (criteria) {
		case "u2 >= Threshold":
			return new Relation(inputTerm, Relation.RelationType.GREATER_EQUAL, threshold);
		case "u2 > Threshold":
			return new Relation(inputTerm, Relation.RelationType.GREATER_THAN, threshold);
		case "u2 ~= 0":
			return new Relation(inputTerm, Relation.RelationType.NOT_EQUAL, threshold);
		default:
			PluginLogger.error("Invalid switch condition type.");
			return null;
		}
	}
}
