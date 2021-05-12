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
package simulink2dl.blockanalyzer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkLine;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.ControlInformation;
import simulink2dl.invariants.information.DataInformation;
import simulink2dl.invariants.information.DiscreteSignalInformation;
import simulink2dl.invariants.information.EqualityInformation;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.information.SignalboundaryInformation;
import simulink2dl.invariants.util.AnalyzerUtil;
import simulink2dl.transform.Environment;

public class LogicalAnalyzer extends BlockAnalyzer {

	public LogicalAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		List<InvariantInformation> infos = new LinkedList<InvariantInformation>();

		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(block.getBlock());

		ControlInformation information = new ControlInformation(invGraph);

		String operator = block.getBlock().getParameter("Operator");
		String inputsString = block.getBlock().getParameter("Inputs");
		int numberOfInputs;
		if (operator.equals("NOT")) {
			// special handling for "NOT" (only one input port)
			numberOfInputs = 1;
		} else {
			try {
				numberOfInputs = Integer.parseInt(inputsString);
			} catch (NumberFormatException e) {
			}
		}

		List<Operator> elements = new LinkedList<>();
		for (SimulinkLine edge : block.getBlock().getInLines()) {
			ReplaceableTerm replace = env.getToReplace(edge.getSrcPort().getBlock());
			elements.add(new SignalboundaryInformation(invGraph,
					new EqualityInformation(invGraph, new Relation(replace, RelationType.NOT_EQUAL, new RealTerm(0)))));
		}

		Operator antecedent = new Conjunction();
		switch (operator) {
		case "AND":
			antecedent = new Conjunction(elements.toArray(new Operator[elements.size()]));
			break;
		case "NAND":
			antecedent = new Negation(new Conjunction(elements.toArray(new Operator[elements.size()])));
			break;
		case "OR":
			antecedent = new Disjunction(elements.toArray(new Operator[elements.size()]));
			break;
		case "NOR":
			antecedent = new Negation(new Disjunction(elements.toArray(new Operator[elements.size()])));
			break;
		case "XOR":
			// TODO
			break;
		case "NXOR":
			// TODO
			break;
		case "NOT":
			// antecedent = new Negation(elements.get(0)); TODO
			break;
		default:
			break;
		}

		SignalboundaryInformation consequent = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, new Relation(toReplace, RelationType.EQUAL, new RealTerm(1))));
		SignalboundaryInformation oppConsequent = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, new Relation(toReplace, RelationType.EQUAL, new RealTerm(0))));

		information.addImplication(antecedent, consequent);
		information.addImplication(new Negation(antecedent), oppConsequent);

		infos.add(information);
		return infos;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION;
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION;
	}

	@Override
	public int applyControlInformation(ControlInformation information, InvariantEdge edge) {
		return AnalyzerUtil.applyControlInformation(this, information, invGraph);
	}

	@Override
	public int applyDataInformation(DataInformation information, InvariantEdge edge) {
		return 0;
	}

	@Override
	public List<InvariantInformation> noInformationApplied(InvariantEdge edge, String informationString) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<InvariantInformation> generateInformationForFeedbackSimulation() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<InvariantInformation> evaluateConditionForFeedbackSimulation() {
		return Collections.EMPTY_LIST;
	}

}
