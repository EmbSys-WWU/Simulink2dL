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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
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

public class UnitdelayAnalyzer extends BlockAnalyzer {

	public UnitdelayAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		/*
		 * SignalboundaryInformation cloned = (SignalboundaryInformation)
		 * information.clone(); Variable v = new Variable("R", "x");
		 * cloned.finalizeInformation(v);
		 * 
		 * String initCond = this.block.getBlock().getParameter("InitialCondition");
		 * 
		 * Relation rel = new Relation(v, RelationType.EQUAL, new StringTerm(initCond));
		 * Conjunction con = new Conjunction(rel, cloned);
		 * 
		 * FormulaChecker checker = new FormulaChecker(); if (
		 * checker.checkSingleFormula(con) != ResultType.SATISFIABLE ) { return
		 * END_OF_INFORMATION; } TODO
		 */
		return VALID_INFORMATION;
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		return VALID_INFORMATION;
	}

	@Override
	public int applyControlInformation(ControlInformation information, InvariantEdge edge) {
		return 0;
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
		Variable v = AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invGraph);
		String initCond = this.block.getBlock().getParameter("InitialCondition");
		Relation rel = new Relation(v, RelationType.EQUAL, new RealTerm(initCond));
		SignalboundaryInformation sigInfo = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, rel));

		List<InvariantInformation> results = new LinkedList<>();
		results.add(sigInfo);
		return results;
	}

	@Override
	public List<InvariantInformation> evaluateConditionForFeedbackSimulation() {
		return Collections.EMPTY_LIST;
	}

}
