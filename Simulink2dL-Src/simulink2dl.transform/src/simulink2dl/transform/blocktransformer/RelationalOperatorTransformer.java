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
import simulink2dl.dlmodel.operator.ExistentialQuantifier;
import simulink2dl.dlmodel.operator.UniversalQuantifier;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;

public class RelationalOperatorTransformer extends BlockTransformer {

	public RelationalOperatorTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel,
			Environment environment) {
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
		String type = "RelationalOperator";
		checkBlock(type, block);

		// get operator type
		String operator = block.getParameter("Operator");

		// some operator type only have one input and are handled differently
		Term relationTerm;
		if (operator.equals("isInf") | operator.equals("isNaN") | operator.equals("isFinite")) {
			relationTerm = transformSingleRelation(block, operator);
		} else {
			relationTerm = transformRelation(block, operator);
		}

		// add macro
		Term replaceWith = relationTerm;
		macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));
		return macros;
	}

	private Term transformSingleRelation(SimulinkBlock block, String operator) {
		// get connected port
		SimulinkOutPort inputPort = environment.getConnectedOuputPort(block, 1);
		ReplaceableTerm inputTerm = new PortIdentifier(environment.getPortID(inputPort));

		Term result;
		Variable x = new Variable("x", "R");

		switch (operator) {
		case "isInf":
			result = new Disjunction(new UniversalQuantifier(x, new Relation(inputTerm, RelationType.GREATER_THAN, x)),
					new UniversalQuantifier(x, new Relation(inputTerm, RelationType.LESS_THAN, x)));
			break;
		case "isNaN":
			result = new UniversalQuantifier(x, new Relation(inputTerm, RelationType.NOT_EQUAL, x));
			break;
		case "isFinite":
			result = new ExistentialQuantifier(x, new Relation(inputTerm, RelationType.EQUAL, x));
			break;
		default:
			PluginLogger.error("Invalid operator in relational operator block \"" + block.getName() + "\"");
			result = new StringTerm("ERROR");
		}

		return result;
	}

	private Term transformRelation(SimulinkBlock block, String operator) {
		// get connected ports
		SimulinkOutPort firstInputPort = environment.getConnectedOuputPort(block, 1);
		ReplaceableTerm firstInputTerm = new PortIdentifier(environment.getPortID(firstInputPort));
		SimulinkOutPort secondInputPort = environment.getConnectedOuputPort(block, 2);
		ReplaceableTerm secondInputTerm = new PortIdentifier(environment.getPortID(secondInputPort));

		Term result;
		switch (operator) {
		case "==":
			result = new Relation(firstInputTerm, RelationType.EQUAL, secondInputTerm);
			break;
		case "~=":
			result = new Relation(firstInputTerm, RelationType.NOT_EQUAL, secondInputTerm);
			break;
		case "<":
			result = new Relation(firstInputTerm, RelationType.LESS_THAN, secondInputTerm);
			break;
		case "<=":
			result = new Relation(firstInputTerm, RelationType.LESS_EQUAL, secondInputTerm);
			break;
		case ">":
			result = new Relation(firstInputTerm, RelationType.GREATER_THAN, secondInputTerm);
			break;
		case ">=":
			result = new Relation(firstInputTerm, RelationType.GREATER_EQUAL, secondInputTerm);
			break;
		default:
			PluginLogger.error("Invalid operator in relational operator block \"" + block.getName() + "\"");
			result = new StringTerm("ERROR");
		}

		return result;
	}

}
