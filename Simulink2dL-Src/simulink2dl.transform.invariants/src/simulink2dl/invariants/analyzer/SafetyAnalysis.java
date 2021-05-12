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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkLine;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.DiscreteSignalInformation;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.information.SignalboundaryInformation;
import simulink2dl.invariants.util.AnalyzerUtil;
import simulink2dl.transform.Environment;
import simulink2dl.transform.Transformer;
import simulink2dl.util.satisfiability.FormulaChecker;
import simulink2dl.util.satisfiability.FormulaChecker.ResultType;

public class SafetyAnalysis {

	private static String[] feedtroughBlockStrings = new String[] {};

	private SafetyAnalysis() {
	}

	public static void checkDivisions(InvariantGraph invGraph) {
		List<InvariantNode> nodes = invGraph.getAllNodes();

		Transformer transformer = invGraph.getTransformer();

		for (InvariantNode node : nodes) {
			SimulinkBlock block = node.getBlock();
			if (block.getType().equals("Product")) {
				String signs = block.getParameter("Inputs");
				int signsCount = signs.length();
				if (signsCount == 1) {
					signs = "";
					for (int i = 0; i < block.getInLines().size(); i++)
						signs += "*";
					signsCount = signs.length();
				}
				List<InvariantEdge> incomingEdges = invGraph.getAllIncomingEdges(block);
				for (InvariantEdge inEdge : incomingEdges) {
					int position = inEdge.getDstID();
					String sign = String.valueOf(signs.charAt(position - 1));
					if (sign.equals("/")) {
						boolean isZero = false;
						boolean isBoundaryInformation = false;
						for (InvariantInformation info : inEdge.getAllInformation()) {
							if (info instanceof DiscreteSignalInformation) {
								isBoundaryInformation = true;
								if (((DiscreteSignalInformation) info).crossesZero()) {
									isZero = true;
									break;
								}
							} else if (info instanceof SignalboundaryInformation) {
								isBoundaryInformation = true;
								if (((SignalboundaryInformation) info).crossesZero()) {
									isZero = true;
									break;
								}
							}
						}

						if (isZero || !isBoundaryInformation) {
							Variable v = AnalyzerUtil.getOutVariableOfBlock(inEdge.getSignal().getSrcPort().getBlock(),
									invGraph);

							Relation security = new Relation(v, RelationType.NOT_EQUAL, new RealTerm(0));
							invGraph.addSecurityProperty(security);
						}
					}
				}
			}
		}
	}

	public static void checkOverflow(InvariantGraph invGraph) {
		List<InvariantNode> nodes = invGraph.getAllNodes();
		Transformer transformer = invGraph.getTransformer();
		Environment env = transformer.getEnvironment();

		// Check for integrator
		for (InvariantNode node : nodes) {
			SimulinkBlock block = node.getBlock();
			Variable v = AnalyzerUtil.getOutVariableOfBlock(block, invGraph);

			if (block.getInPorts().size() == 0) {
				continue;
			}
			SimulinkLine inSignalPortOne = block.getInPort("1").getLine();

			if (block.getType().equals("DiscreteIntegrator")) {
				checkOverflowIntegrator(invGraph, block, v, inSignalPortOne);
				// TODO Check discreteIntegrator
			} else if (block.getType().equals("Integrator")) {
				checkOverflowIntegrator(invGraph, block, v, inSignalPortOne);
			}

			// check feedtrough blocks
			checkOverflowFeedtrough(invGraph, block, v, inSignalPortOne);
		}

		// check for feedback-loops
		checkOverflowLoop(invGraph);
	}

	// TODO rework with crossesValue
	private static void checkOverflowIntegrator(InvariantGraph invGraph, SimulinkBlock block, Variable v,
			SimulinkLine inSignalPortOne) {
		String lowerLimit = block.getParameter("LowerSaturationLimit");
		String upperLimit = block.getParameter("UpperSaturationLimit");
		String lowerWrap = block.getParameter("WrappedStateLowerValue");
		String upperWrap = block.getParameter("WrappedStateUpperValue");

		if ((lowerLimit == null || lowerLimit.equals("-inf") && upperLimit == null
				|| upperLimit.equals("inf") && lowerWrap == null || lowerWrap.equals("-pi") && upperWrap == null
				|| upperWrap.equals("pi"))) {
			Environment env = invGraph.getTransformer().getEnvironment();

			Relation antecedent = new Relation(env.getToReplace(inSignalPortOne.getSrcPort().getBlock()),
					RelationType.LESS_THAN, new RealTerm(0));
			Relation oppositeAntecedent = new Relation(env.getToReplace(inSignalPortOne.getSrcPort().getBlock()),
					RelationType.GREATER_THAN, new RealTerm(0));
			Relation consequent = new Relation(v, RelationType.GREATER_EQUAL, new Variable("R", "MIN_INT"));
			Relation oppositeConsequent = new Relation(v, RelationType.LESS_EQUAL, new Variable("R", "MAX_INT"));
			Relation consequentWithValue = new Relation(v, RelationType.LESS_THAN, new RealTerm(invGraph.getMinInt()));
			Relation oppositeConsequentWithValue = new Relation(v, RelationType.GREATER_THAN,
					new RealTerm(invGraph.getMaxInt()));

			InvariantEdge inEdge = invGraph.getEdge(inSignalPortOne);
			ResultType consequentResult = ResultType.UNKNOWN;
			ResultType oppositeResult = ResultType.UNKNOWN;
			ResultType consequentValueResult = ResultType.UNKNOWN;
			ResultType oppositeValueResult = ResultType.UNKNOWN;
			boolean isBounded = false;
			for (InvariantInformation information : inEdge.getAllInformation()) {
				if (information instanceof SignalboundaryInformation) {
					FormulaChecker checker = new FormulaChecker();

					consequentResult = checker.checkSingleFormula(new Conjunction(information, antecedent));
					oppositeResult = checker.checkSingleFormula(new Conjunction(information, oppositeAntecedent));
					consequentValueResult = checker
							.checkSingleFormula(new Conjunction(information, consequentWithValue));
					oppositeValueResult = checker
							.checkSingleFormula(new Conjunction(information, oppositeConsequentWithValue));

					isBounded = true;
					break;
				}
			}

			Implication impli = new Implication(antecedent, consequent);
			Implication oppositeImplication = new Implication(oppositeAntecedent, oppositeConsequent);

			if (consequentResult == ResultType.SATISFIABLE || consequentValueResult == ResultType.SATISFIABLE) {
				invGraph.addSecurityProperty(impli);
			} else if (oppositeResult == ResultType.SATISFIABLE || oppositeValueResult == ResultType.SATISFIABLE) {
				invGraph.addSecurityProperty(oppositeImplication);
			} else if (isBounded) {
			} else {
				invGraph.addSecurityProperty(impli);
				invGraph.addSecurityProperty(oppositeImplication);
			}
		}
	}

	private static void checkOverflowLoop(InvariantGraph invGraph) {
		List<InvariantNode> previousBlocks = new LinkedList<InvariantNode>();
		List<InvariantNode> allBlocks = new LinkedList<InvariantNode>();

		Set<SimulinkBlock> unsortedBlocks = new HashSet<SimulinkBlock>();
		List<SimulinkBlock> blockOrder = invGraph.getTransformer()
				.generateBlockOrder(invGraph.getTransformer().getSimulinkModel().getSubBlocks(), unsortedBlocks);
		for (SimulinkBlock iterateBlock : blockOrder) {
			List<InvariantNode> feedbackLoop = new LinkedList<>();
			previousBlocks = new LinkedList<InvariantNode>();
			InvariantNode iterate = invGraph.getNode(iterateBlock);
			if (!allBlocks.contains(iterate.getBlock()))
				feedbackLoop = checkOverflowLoopRecursion(invGraph, iterate, previousBlocks, allBlocks);

			if (feedbackLoop.size() > 0) { // security analysis here
				for (InvariantNode feedNode : feedbackLoop) {
					SimulinkBlock feedBlock = feedNode.getBlock();
					if (feedBlock.getType().equals("Gain") || feedBlock.getType().equals("Product")
							|| feedBlock.getType().equals("Sum")) {
						addSecurityPropertyForLoopBlock(invGraph, feedNode);
					}
				}
			}
		}
	}

	private static List<InvariantNode> checkOverflowLoopRecursion(InvariantGraph invGraph, InvariantNode nodeBlock,
			List<InvariantNode> previousBlocks, List<InvariantNode> allBlocks) {
		// check if the block is already in the loop
		if (previousBlocks.contains(nodeBlock)) {
			return previousBlocks;
		}
		previousBlocks.add(nodeBlock);

		for (SimulinkLine signal : nodeBlock.getBlock().getInLines()) {
			SimulinkBlock srcBlock = signal.getSrcPort().getBlock();
			InvariantNode oldNode = invGraph.getNode(srcBlock);
			if (allBlocks.contains(oldNode)) {
				continue;
			}
			List<InvariantNode> nodes = new LinkedList<InvariantNode>(previousBlocks);
			List<InvariantNode> result = checkOverflowLoopRecursion(invGraph,
					invGraph.getNode(signal.getSrcPort().getBlock()), nodes, allBlocks);
			if (result.size() > 0)
				return result;
		}
		allBlocks.add(nodeBlock);
		return Collections.EMPTY_LIST;
	}

	private static void addSecurityPropertyForLoopBlock(InvariantGraph invGraph, InvariantNode node) {
		Transformer transformer = invGraph.getTransformer();
		Environment env = transformer.getEnvironment();
		Variable v = AnalyzerUtil.getOutVariableOfBlock(node.getBlock(), invGraph);

		Relation lowerMAX_INT = new Relation(v, RelationType.LESS_EQUAL, new Variable("R", "MAX_INT"));
		Relation greaterMIN_INT = new Relation(v, RelationType.GREATER_EQUAL, new Variable("R", "MIN_INT"));

		invGraph.addSecurityProperty(lowerMAX_INT);
		invGraph.addSecurityProperty(greaterMIN_INT);
	}

	private static void checkOverflowFeedtrough(InvariantGraph invGraph, SimulinkBlock block, Variable v,
			SimulinkLine inSignalPortOne) {
		for (SimulinkLine signal : block.getOutLines()) {
			InvariantEdge edge = invGraph.getEdge(signal);
			for (InvariantInformation info : edge.getAllInformation()) {
				if (info instanceof SignalboundaryInformation) {
					if (((SignalboundaryInformation) info).crossesValue(RelationType.GREATER_THAN,
							String.valueOf(invGraph.getMaxInt()))) {
						Relation lowerMAX_INT = new Relation(v, RelationType.LESS_EQUAL, new Variable("R", "MAX_INT"));
						invGraph.addSecurityProperty(lowerMAX_INT);
					}
					if (((SignalboundaryInformation) info).crossesValue(RelationType.LESS_THAN,
							String.valueOf(invGraph.getMinInt()))) {
						Relation greaterMIN_INT = new Relation(v, RelationType.GREATER_EQUAL,
								new Variable("R", "MIN_INT"));
						invGraph.addSecurityProperty(greaterMIN_INT);
					}
				}
				if (info instanceof DiscreteSignalInformation) {
					if (((DiscreteSignalInformation) info).crossesValue(RelationType.GREATER_THAN,
							String.valueOf(invGraph.getMaxInt()))) {
						Relation lowerMAX_INT = new Relation(v, RelationType.LESS_EQUAL, new Variable("R", "MAX_INT"));
						invGraph.addSecurityProperty(lowerMAX_INT);
					}
					if (((DiscreteSignalInformation) info).crossesValue(RelationType.LESS_THAN,
							String.valueOf(invGraph.getMinInt()))) {
						Relation greaterMIN_INT = new Relation(v, RelationType.GREATER_EQUAL,
								new Variable("R", "MIN_INT"));
						invGraph.addSecurityProperty(greaterMIN_INT);
					}
				}
			}
		}
	}
}
