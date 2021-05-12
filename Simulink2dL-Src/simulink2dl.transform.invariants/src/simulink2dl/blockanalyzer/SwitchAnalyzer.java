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
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
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
import simulink2dl.util.PluginLogger;

public class SwitchAnalyzer extends BlockAnalyzer {

	public SwitchAnalyzer(InvariantNode block, InvariantGraph invGraph) {
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
		ReplaceableTerm incomingEdge3 = env
				.getToReplace(block.getBlock().getInPort("3").getLine().getSrcPort().getBlock());

		ControlInformation information = new ControlInformation(invGraph);

		SignalboundaryInformation newInfo = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, new Relation(toReplace, RelationType.EQUAL, incomingEdge1)));
		information.addImplication(getConditionOfFirstInput(), newInfo);

		SignalboundaryInformation oppositeNewInfo = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, new Relation(toReplace, RelationType.EQUAL, incomingEdge3)));
		information.addImplication(getConditionOfThirdInput(), oppositeNewInfo);

		infos.add(information);

		return infos;
	}

	public Relation getConditionOfFirstInput() {
		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm incomingEdge1 = env
				.getToReplace(block.getBlock().getInPort("1").getLine().getSrcPort().getBlock());
		ReplaceableTerm incomingEdge2 = env
				.getToReplace(block.getBlock().getInPort("2").getLine().getSrcPort().getBlock());

		String value = block.getBlock().getParameter("Threshold");
		String criteria = block.getBlock().getParameter("Criteria");
		RelationType type = RelationType.GREATER_EQUAL;
		switch (criteria) {
		case "u2 > Threshold":
			type = RelationType.GREATER_THAN;
			break;
		case "u2 ~= 0":
			type = RelationType.NOT_EQUAL;
			break;
		case "u2 >= Threshold":
			type = RelationType.GREATER_EQUAL;
			break;
		default:
			PluginLogger.error("Invalid switch condition type.");
			return null;
		}

		Relation antecedent = new Relation(incomingEdge2, type, new RealTerm(value));
		return antecedent;
	}

	public Relation getConditionOfThirdInput() {
		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm incomingEdge2 = env
				.getToReplace(block.getBlock().getInPort("2").getLine().getSrcPort().getBlock());

		String value = block.getBlock().getParameter("Threshold");
		String criteria = block.getBlock().getParameter("Criteria");
		RelationType type = RelationType.GREATER_EQUAL;
		switch (criteria) {
		case "u2 > Threshold":
			type = RelationType.GREATER_THAN;
			break;
		case "u2 ~= 0":
			type = RelationType.NOT_EQUAL;
			break;
		case "u2 >= Threshold":
			type = RelationType.GREATER_EQUAL;
			break;
		default:
			PluginLogger.error("Invalid switch condition type.");
			return null;
		}

		RelationType oppositeType = null;
		if (type == RelationType.GREATER_EQUAL) {
			oppositeType = RelationType.LESS_THAN;
		} else if (type == RelationType.GREATER_THAN) {
			oppositeType = RelationType.LESS_EQUAL;
		} else if (type == RelationType.NOT_EQUAL) {
			oppositeType = RelationType.EQUAL;
		}

		Relation oppositeAntecedent = new Relation(incomingEdge2, oppositeType, new RealTerm(value));
		return oppositeAntecedent;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(block.getBlock());

		if (edge.getDstID() == 2) {
			return END_OF_INFORMATION;
		}

		for (ControlInformation info : getControlInformationOfBlock()) {
			for (Operator implication : info.getElements()) {
				if (implication instanceof Implication) {
					Implication castedImplication = ((Implication) implication);
					InvariantInformation casted = (InvariantInformation) castedImplication.getConsequent();
					ReplaceableTerm srcTerm = env.getToReplace(edge.getSrcNode().getBlock());
					if (casted.containsTerm(srcTerm)) {
						InvariantInformation newConsequent = (InvariantInformation) information.clone();
						newConsequent.changeReplaceableTerm(toReplace);
						castedImplication.setConsequent(newConsequent);
					}
				}
			}
		}
		return END_OF_INFORMATION;
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(block.getBlock());

		if (edge.getDstID() == 2) {
			return END_OF_INFORMATION;
		}

		for (ControlInformation info : getControlInformationOfBlock()) {
			for (Operator implication : info.getElements()) {
				if (implication instanceof Implication) {
					Implication castedImplication = ((Implication) implication);
					InvariantInformation casted = (InvariantInformation) castedImplication.getConsequent();
					ReplaceableTerm srcTerm = env.getToReplace(edge.getSrcNode().getBlock());
					if (casted.containsTerm(srcTerm)) {
						InvariantInformation newConsequent = (InvariantInformation) information.clone();
						newConsequent.changeReplaceableTerm(toReplace);
						castedImplication.setConsequent(newConsequent);
					}
				}
			}
		}
		return END_OF_INFORMATION;
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
		InvariantEdge conditionInput = invGraph.getEdge(block.getBlock().getInPort("2").getLine());
		for (InvariantInformation condiInformation : conditionInput.getAllInformation()) {
			if (condiInformation instanceof SignalboundaryInformation) {
				ReplaceableTerm v = new PortIdentifier("x");

				SignalboundaryInformation casted = (SignalboundaryInformation) condiInformation.clone();
				casted.changeReplaceableTerm(v);
				Disjunction castedAsFormula = casted.getInformationAsFormula();

				Relation conditionFirstInput = getConditionOfFirstInput();
				Relation conditionThirdInput = getConditionOfThirdInput();

				boolean resultFirst = casted.crossesValue(conditionFirstInput.getType(),
						conditionFirstInput.getRightSide().toString());
				boolean resultThird = casted.crossesValue(conditionThirdInput.getType(),
						conditionThirdInput.getRightSide().toString());

				InvariantEdge firstOrThird = null;
				if (resultFirst && !resultThird) {
					firstOrThird = invGraph.getEdge(block.getBlock().getInPort("1").getLine());
				} else if (!resultFirst && resultThird) {
					firstOrThird = invGraph.getEdge(block.getBlock().getInPort("3").getLine());
				}
				if (firstOrThird != null) {
					return firstOrThird.getAllInformation();
				}
			} else if (condiInformation instanceof DiscreteSignalInformation) {
				// TODO
			}
		}

		return Collections.EMPTY_LIST;
	}
}
