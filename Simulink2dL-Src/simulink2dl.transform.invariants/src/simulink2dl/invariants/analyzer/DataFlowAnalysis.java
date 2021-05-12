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
package simulink2dl.invariants.analyzer;

import java.util.HashMap;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkLine;

import simulink2dl.blockanalyzer.SwitchAnalyzer;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.DataInformation;
import simulink2dl.invariants.util.AnalyzerUtil;
import simulink2dl.transform.Transformer;

public class DataFlowAnalysis {

	private DataFlowAnalysis() {
		// empty
	}

	public static void dataflowAnalysis(Transformer transformer, InvariantGraph invGraph, SimulinkBlock block) {
		InvariantNode node = invGraph.getNode(block);
		HashMap<Integer, List<InvariantNode>> sameInputBlocks = invGraph.getOutBlocksWithSameSignalLine(block);

		for (Integer key : sameInputBlocks.keySet()) {
			List<InvariantNode> sameBlocks = sameInputBlocks.get(key);
			for (InvariantNode oneBlock : sameBlocks) {
				for (InvariantNode otherBlock : sameBlocks) {
					if (otherBlock != oneBlock) {
						handleEqualInputForBlocks(transformer, invGraph, oneBlock.getBlock(), otherBlock.getBlock());
					}
				}
			}
		}
	}

	private static void handleEqualInputForBlocks(Transformer transformer, InvariantGraph invGraph, SimulinkBlock first,
			SimulinkBlock second) {
		Variable firstVariable = AnalyzerUtil.getOutVariableOfBlock(first, invGraph);
		Variable secondVariable = AnalyzerUtil.getOutVariableOfBlock(second, invGraph);

		String firstType = first.getType().toLowerCase();
		String secondType = second.getType().toLowerCase();

		boolean controlBlockHandled = false;

		if (AnalyzerUtil.controlBlocks.contains(firstType) && AnalyzerUtil.controlBlocks.contains(secondType)) {
			// set equal if all inputs are equal
			boolean equalInports = false;
			for (int i = 0; i < first.getInPorts().size(); i++) {
				SimulinkLine sigFirst = first.getInPort(Integer.toString(i + 1)).getLine();
				SimulinkLine sigSecond = second.getInPort(Integer.toString(i + 1)).getLine();

				if (sigFirst.getSrcPort().getBlock() == sigSecond.getSrcPort().getBlock()
						|| sigFirst.getDstPort().getBlock() == sigSecond.getDstPort().getBlock()) {
					equalInports = true;
				} else {
					equalInports = false;
					break;
				}
			}
			if (first.getParameter("Threshold") == second.getParameter("Threshold")
					|| first.getParameter("Criteria") == second.getParameter("Criteria")
					|| first.getParameter("OffSwitchValue") == second.getParameter("OffSwitchValue")
					|| first.getParameter("OnSwitchValue") == second.getParameter("OnSwitchValue")
					|| first.getParameter("OffOutputValue") == second.getParameter("OffOutputValue")
					|| first.getParameter("OnOutputValue") == second.getParameter("OnOutputValue")
					|| first.getParameter("Operator") == second.getParameter("Operator")
					|| first.getParameter("Inputs") == second.getParameter("Inputs")) {
				if (equalInports) {
					Relation equ = new Relation(firstVariable, RelationType.EQUAL, secondVariable);
					DataInformation dataInformation = new DataInformation(invGraph, equ);

					addInformationToGraph(invGraph, dataInformation);
					controlBlockHandled = true;
				}
			}
		} else if (AnalyzerUtil.discreteBlocks.contains(firstType)
				&& AnalyzerUtil.discreteBlocks.contains(secondType)) {
			if (first.getParameter("InitialCondition") != null && second.getParameter("InitialCondition") != null
					&& Double.parseDouble(first.getParameter("InitialCondition")) != Double
							.parseDouble(second.getParameter("InitialCondition"))) {
				// TODO
				return;
			} else {
				// get delays
				int firstDelay = getDiscreteDelay(first);
				int secondDelay = getDiscreteDelay(second);

				// get correct variables
				firstVariable = null;
				secondVariable = null;
				if (firstDelay == secondDelay) {
					firstVariable = transformer.getDLModel().getVariableByName(first.getName() + "Out");
					secondVariable = transformer.getDLModel().getVariableByName(second.getName() + "Out");
				} else if (firstDelay < secondDelay) {
					firstVariable = transformer.getDLModel().getVariableByName(first.getName() + "Out");
					secondVariable = transformer.getDLModel()
							.getVariableByName(second.getName() + "State" + (firstDelay));
				} else if (firstDelay > secondDelay) {
					firstVariable = transformer.getDLModel()
							.getVariableByName(first.getName() + "State" + (secondDelay));
					secondVariable = transformer.getDLModel().getVariableByName(second.getName() + "Out");
				}

				// create new DataInformation for equal states
				if (firstVariable != null && secondVariable != null) {
					Relation equality = new Relation(firstVariable, RelationType.EQUAL, secondVariable);
					DataInformation dataEquality = new DataInformation(invGraph, equality);

					addInformationToGraph(invGraph, dataEquality);
				}
			}
		} else if (AnalyzerUtil.feedtroughBlocks.contains(firstType)
				&& AnalyzerUtil.feedtroughBlocks.contains(secondType)) {
			// Dont handle feedtroughBlocks
		}

		if (!controlBlockHandled && firstType.equals(secondType) && secondType.equals("switch")) { // special case
			InvariantEdge sig1First = invGraph.getEdge(first.getInPort("1").getLine());
			InvariantEdge sig3First = invGraph.getEdge(first.getInPort("3").getLine());
			InvariantEdge sig1Second = invGraph.getEdge(second.getInPort("1").getLine());
			InvariantEdge sig3Second = invGraph.getEdge(second.getInPort("3").getLine());

			handleSwitchInputs(invGraph, sig1First, sig1Second);
			handleSwitchInputs(invGraph, sig1First, sig3Second);
			handleSwitchInputs(invGraph, sig3First, sig1Second);
			handleSwitchInputs(invGraph, sig3First, sig3Second);
		}
	}

	private static void addInformationToGraph(InvariantGraph invGraph, DataInformation dataInformation) {
		invGraph.addDataInformation(dataInformation);
	}

	private static void handleSwitchInputs(InvariantGraph invGraph, InvariantEdge sigFirst, InvariantEdge sigSecond) {
		if (sigFirst.getSignal().getSrcPort().getBlock() == sigSecond.getSignal().getSrcPort().getBlock()) {
			SimulinkBlock first = sigFirst.getSignal().getDstPort().getBlock();
			SimulinkBlock second = sigSecond.getSignal().getDstPort().getBlock();

			Variable firstVariable = AnalyzerUtil.getOutVariableOfBlock(first, invGraph);
			Variable secondVariable = AnalyzerUtil.getOutVariableOfBlock(second, invGraph);

			SwitchAnalyzer analyzerFirst = new SwitchAnalyzer(invGraph.getNode(first), invGraph);
			SwitchAnalyzer analyzerSecond = new SwitchAnalyzer(invGraph.getNode(second), invGraph);

			Conjunction conditions = new Conjunction();
			if (sigFirst.getDstID() == 1) {
				conditions.addElement(analyzerFirst.getConditionOfFirstInput());
			} else if (sigFirst.getDstID() == 3) {
				conditions.addElement(analyzerFirst.getConditionOfThirdInput());
			}
			if (sigSecond.getDstID() == 1) {
				conditions.addElement(analyzerSecond.getConditionOfFirstInput());
			} else if (sigSecond.getDstID() == 3) {
				conditions.addElement(analyzerSecond.getConditionOfThirdInput());
			}
			Relation signal = new Relation(firstVariable, RelationType.EQUAL, secondVariable);
			DataInformation equalInports = new DataInformation(invGraph, signal, conditions);

			addInformationToGraph(invGraph, equalInports);
		}
	}

	private static int getDiscreteDelay(SimulinkBlock block) {
		switch (block.getType().toLowerCase()) {
		case "unitdelay":
			return 1;
		case "zeroorderhold":
			return 0;
		case "delay":
			return Integer.parseInt(block.getParameter("DelayLength"));
		default:
			// TODO
			break;
		}
		// TODO Error Logger.info("");
		return -1;
	}

}
