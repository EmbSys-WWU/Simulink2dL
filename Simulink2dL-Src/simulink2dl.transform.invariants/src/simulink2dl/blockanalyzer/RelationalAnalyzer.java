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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
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

public class RelationalAnalyzer extends BlockAnalyzer {

	public RelationalAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		List<InvariantInformation> infos = new LinkedList<InvariantInformation>();

		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(block.getBlock());

		// get incoming edges
		ReplaceableTerm incomingEdge1 = env
				.getToReplace(block.getBlock().getInPort("1").getLine().getSrcPort().getBlock());
		ReplaceableTerm incomingEdge2 = env
				.getToReplace(block.getBlock().getInPort("2").getLine().getSrcPort().getBlock());

		ControlInformation information = new ControlInformation(invGraph);

		String operator = block.getBlock().getParameter("Operator");

		Variable x = new Variable("x", "R");

		ReplaceableTerm firstInput = incomingEdge1;
		Term secondInput = incomingEdge2;

		RelationType type = null;
		RelationType oppositeType = null;
		switch (operator) {
		case "==":
			type = RelationType.EQUAL;
			oppositeType = RelationType.NOT_EQUAL;
			break;
		case "~=":
			type = RelationType.NOT_EQUAL;
			oppositeType = RelationType.EQUAL;
			break;
		case "<":
			type = RelationType.LESS_THAN;
			oppositeType = RelationType.GREATER_EQUAL;
			break;
		case "<=":
			type = RelationType.LESS_EQUAL;
			oppositeType = RelationType.GREATER_THAN;
			break;
		case ">":
			type = RelationType.GREATER_THAN;
			oppositeType = RelationType.LESS_EQUAL;
			break;
		case ">=":
			type = RelationType.GREATER_EQUAL;
			oppositeType = RelationType.LESS_THAN;
			break;
		case "isInf":

			break;
		case "isNaN":
			type = RelationType.NOT_EQUAL;
			oppositeType = RelationType.EQUAL;
			secondInput = x;
			break;
		case "isFinite":
			type = RelationType.EQUAL;
			oppositeType = RelationType.NOT_EQUAL;
			secondInput = x;
			break;
		default:
		}

		if (type == null || oppositeType == null) {
			// TODO ERROR
		}

		Relation antecedent = new Relation(firstInput, type, secondInput);
		EqualityInformation eq = new EqualityInformation(invGraph,
				new Relation(toReplace, RelationType.EQUAL, new RealTerm(1)));
		information.addImplication(antecedent, eq);

		Relation oppAntecedent = new Relation(firstInput, oppositeType, secondInput);
		EqualityInformation oppEq = new EqualityInformation(invGraph,
				new Relation(toReplace, RelationType.EQUAL, new RealTerm(0)));
		information.addImplication(oppAntecedent, oppEq);

		infos.add(information);
		return infos;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION_IN_NODE; // STOP
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION_IN_NODE; // STOP
	}

	@Override
	public int applyControlInformation(ControlInformation information, InvariantEdge edge) {
		return AnalyzerUtil.applyControlInformation(this, information, invGraph);
	}

	public List<ControlInformation> getControlInformationOfBlock() {
		List<ControlInformation> allInfos = new LinkedList<ControlInformation>();
		for (SimulinkLine line : block.getBlock().getOutPort("1").getLines()) {
			InvariantEdge outEdge = invGraph.getEdge(line);

			for (InvariantInformation info : outEdge.getAllInformation()) {
				if (info instanceof ControlInformation)
					allInfos.add((ControlInformation) info);
			}
		}
		return allInfos;
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
