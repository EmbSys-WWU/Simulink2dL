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

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkLine;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
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

public class SumAnalyzer extends BlockAnalyzer {

	public SumAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		String signs = block.getBlock().getParameter("Inputs");
		SignalboundaryInformation newInfo = (SignalboundaryInformation) information.clone();

		LinkedList<InvariantEdge> incomingEdges = (LinkedList<InvariantEdge>) invGraph.getAllIncomingEdges(block);

		if (String.valueOf(signs.charAt(edge.getDstID() - 1)).equals("-")) {
			newInfo = SignalboundaryInformation.calcSignal(minusEquality, "-", newInfo);
		}

		List<InvariantInformation> used = new LinkedList<InvariantInformation>();
		for (InvariantEdge e : incomingEdges) {
			LinkedList<InvariantInformation> allInfos = (LinkedList<InvariantInformation>) e.getAllInformation();
			int position = e.getDstID();
			String sign = String.valueOf(signs.charAt(position - 1));

			boolean infoUsed = false;
			for (InvariantInformation a : allInfos) {
				if (a instanceof SignalboundaryInformation) {
					SignalboundaryInformation casted = (SignalboundaryInformation) a;
					infoUsed = true;
					if (casted.equals(information) || e == edge)
						continue;
					used.add(a);

					newInfo.getDisjunction().setElements(
							SignalboundaryInformation.calcSignal(newInfo, sign, casted).getDisjunction().getElements());
					break;
				}
				if (a instanceof DiscreteSignalInformation) {
					return END_OF_INFORMATION;
				}
			}
			if (!infoUsed && e != edge) {
				Variable v = AnalyzerUtil.getOutVariableOfBlock(e.getSignal().getSrcPort().getBlock(), invGraph);
				SignalboundaryInformation variable = new SignalboundaryInformation(invGraph,
						new EqualityInformation(invGraph, new Relation(v, RelationType.EQUAL, v)));
				if (!variable.equals(information)) {
					newInfo.getDisjunction().setElements(SignalboundaryInformation.calcSignal(newInfo, sign, variable)
							.getDisjunction().getElements());
				}
			}
		}

		for (InvariantInformation a : used) {
			a.done();
		}

		information.getDisjunction().setElements(newInfo.getDisjunction().getElements());
		return VALID_INFORMATION;
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		String signs = block.getBlock().getParameter("Inputs");
		DiscreteSignalInformation newInfo = (DiscreteSignalInformation) information.clone();

		LinkedList<InvariantEdge> incomingEdges = (LinkedList<InvariantEdge>) invGraph.getAllIncomingEdges(block);

		if (String.valueOf(signs.charAt(edge.getDstID() - 1)).equals("-")) {
			newInfo = DiscreteSignalInformation.calcDiscrete(minusEqualityDiscrete, "-", newInfo);
		}

		List<InvariantInformation> used = new LinkedList<InvariantInformation>();
		for (InvariantEdge e : incomingEdges) {
			LinkedList<InvariantInformation> allInfos = (LinkedList<InvariantInformation>) e.getAllInformation();
			int position = e.getDstID();
			String sign = String.valueOf(signs.charAt(position - 1));

			boolean infoUsed = false;
			for (InvariantInformation a : allInfos) {
				if (a instanceof SignalboundaryInformation) {
					SignalboundaryInformation casted = (SignalboundaryInformation) a;
					newInfo.setElements(DiscreteSignalInformation
							.calcSignal(newInfo, String.valueOf(signs.charAt(position - 1)), casted).getElements());
					used.add(a);
					infoUsed = true;
					break;
				} else if (a instanceof DiscreteSignalInformation) {
					DiscreteSignalInformation casted = (DiscreteSignalInformation) a;
					infoUsed = true;
					if (information.equals(casted) || e == edge)
						continue;
					used.add(a);
					newInfo.setElements(DiscreteSignalInformation
							.calcDiscrete(newInfo, String.valueOf(signs.charAt(position - 1)), casted).getElements());
					break;
				}
			}
			if (!infoUsed && e != edge) {
				Variable v = AnalyzerUtil.getOutVariableOfBlock(e.getSignal().getSrcPort().getBlock(), invGraph);
				SignalboundaryInformation variable = new SignalboundaryInformation(invGraph,
						new EqualityInformation(invGraph, new Relation(v, RelationType.EQUAL, v)));

				newInfo.setElements(DiscreteSignalInformation.calcSignal(newInfo, sign, variable).getElements());
			}
		}

		for (InvariantInformation a : used) {
			a.done();
		}

		information.setElements(newInfo.getElements());
		return VALID_INFORMATION;
	}

	@Override
	public int applyControlInformation(ControlInformation information, InvariantEdge edge) {
		for (Operator op : information.getElements()) {
			if (op instanceof Implication) {
				Operator op2 = ((Implication) op).getConsequent();
				if (op2 instanceof SignalboundaryInformation) {
					SignalboundaryInformation sig = (SignalboundaryInformation) op2;
					int returnvalue = applySignalBoundary(sig, edge);
					if (returnvalue == END_OF_INFORMATION) {
						return END_OF_INFORMATION;
					}
				}
			}
		}
		return VALID_INFORMATION;
	}

	@Override
	public int applyDataInformation(DataInformation information, InvariantEdge edge) {
		return 0;
	}

	@Override
	public List<InvariantInformation> noInformationApplied(InvariantEdge edge, String informationString) {
		List<InvariantInformation> newInformation = new LinkedList<InvariantInformation>();

		if (informationString.equals(SignalboundaryInformation.class.toString())) {
			SimulinkLine signalLineOfFirstInput = block.getBlock().getInPort("1").getLine();
			SimulinkBlock blockOfFirstInput = signalLineOfFirstInput.getSrcPort().getBlock();
			Variable variable = AnalyzerUtil.getOutVariableOfBlock(blockOfFirstInput, invGraph);
			SignalboundaryInformation information = new SignalboundaryInformation(invGraph,
					new EqualityInformation(invGraph, new Relation(variable, RelationType.EQUAL, variable)));

			if (applySignalBoundary(information, invGraph.getEdge(signalLineOfFirstInput)) == VALID_INFORMATION) {
				newInformation.add(information);
			}
		}

		return newInformation;
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
