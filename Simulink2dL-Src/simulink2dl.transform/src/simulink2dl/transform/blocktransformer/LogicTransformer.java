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
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.StringFormula;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.dlmodel.operator.formulas.LogicCombinationTerm;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;

public class LogicTransformer extends BlockTransformer {

	public LogicTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
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
		String type = "Logic";
		checkBlock(type, block);

		// get operator type
		String operator = block.getParameter("Operator");

		// get number of input ports
		List<Formula> elements = new LinkedList<Formula>();
		String inputsString = block.getParameter("Inputs");
		int numberOfInputs;
		if (operator.equals("NOT")) {
			// special handling for "NOT" (only one input port)
			numberOfInputs = 1;
		} else {
			try {
				numberOfInputs = Integer.parseInt(inputsString);
			} catch (NumberFormatException e) {
				PluginLogger.error("Could not parse inputs string \"" + inputsString + "\" of logic block \""
						+ block.getName() + "\"");
				return macros;
			}
		}

		// get connected ports
		for (int i = 1; i <= numberOfInputs; i++) {
			SimulinkOutPort inputPort = environment.getConnectedOuputPort(block, i);

			elements.add(new LogicCombinationTerm(new PortIdentifier(environment.getPortID(inputPort))));
		}

		Formula result;
		switch (operator) {
		case "AND":
			result = createConjunction(elements);
			break;
		case "NAND":
			result = new Negation(createConjunction(elements));
			break;
		case "OR":
			result = createDisjunction(elements);
			break;
		case "NOR":
			result = new Negation(createDisjunction(elements));
			break;
		case "XOR":
			result = createXOR(elements);
			break;
		case "NXOR":
			result = new Negation(createXOR(elements));
			break;
		case "NOT":
			result = new Negation(elements.get(0));
			break;
		default:
			PluginLogger.error("Invalid operator in logic operator block \"" + block.getName() + "\"");
			result = new StringFormula("ERROR");
			break;
		}

		// add macro
		Formula replaceWith = result;
		macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));
		return macros;
	}

	private Conjunction createConjunction(List<Formula> elements) {
		Conjunction result = new Conjunction();
		for (Formula element : elements) {
			result.addElement(element);
		}
		return result;
	}

	private Disjunction createDisjunction(List<Formula> elements) {
		Disjunction result = new Disjunction();
		for (Formula element : elements) {
			result.addElement(element);
		}
		return result;
	}

	/**
	 * Creates a XOR formula for an arbitrary number of input elements or formulas.
	 * 
	 * @param elements
	 * @return
	 */
	private Formula createXOR(List<Formula> elements) {
		// check the number of elements
		if (elements.size() < 2) {
			PluginLogger.error("Not enough elements to create XOR.");
			return new StringFormula("ERROR");
		}

		// create first xor
		Formula result = createSingleXOR(elements.get(0), elements.get(1));

		// for more than two elements
		for (int i = 2; i < elements.size(); i++) {
			result = createSingleXOR(result, elements.get(i));
		}

		return result;
	}

	/**
	 * Creates an XOR formula by using AND and OR for two input elements or formula.
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private Formula createSingleXOR(Formula first, Formula second) {
		// (A & !B) | (!A & B)
		return new Disjunction(new Conjunction(first, new Negation(second)),
				new Conjunction(new Negation(first), second));
	}

}
