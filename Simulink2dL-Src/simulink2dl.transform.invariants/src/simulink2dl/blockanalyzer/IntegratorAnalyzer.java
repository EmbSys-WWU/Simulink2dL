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

import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.ControlInformation;
import simulink2dl.invariants.information.DataInformation;
import simulink2dl.invariants.information.DiscreteSignalInformation;
import simulink2dl.invariants.information.IntervalInformation;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.information.SignalboundaryInformation;

public class IntegratorAnalyzer extends BlockAnalyzer {

	public IntegratorAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		LinkedList<InvariantInformation> infos = new LinkedList<>();

		String upperLimit = block.getBlock().getParameter("UpperSaturationLimit");
		String lowerLimit = block.getBlock().getParameter("LowerSaturationLimit");

		String lowerWrap = block.getBlock().getParameter("WrappedStateLowerValue");
		String upperWrap = block.getBlock().getParameter("WrappedStateUpperValue");

		ReplaceableTerm toReplace = invGraph.getTransformer().getEnvironment().getToReplace(block.getBlock());

		SignalboundaryInformation boundary = new SignalboundaryInformation(invGraph);

		Relation upper = new Relation(toReplace, RelationType.LESS_THAN, new StringTerm("inf"));
		Relation lower = new Relation(toReplace, RelationType.GREATER_THAN, new StringTerm("-inf"));
		boolean oneChanged = false;
		if (upperLimit != null && !upperLimit.equals("inf")) {
			upper = new Relation(toReplace, RelationType.LESS_EQUAL, new RealTerm(upperLimit));
			oneChanged = true;
		}
		if (lowerLimit != null && !lowerLimit.equals("-inf")) {
			lower = new Relation(toReplace, RelationType.GREATER_EQUAL, new RealTerm(lowerLimit));
			oneChanged = true;
		}
		if (lowerWrap != null && !lowerWrap.equals("-pi")) {
			lower = new Relation(toReplace, RelationType.GREATER_EQUAL, new RealTerm(lowerWrap));
			oneChanged = true;
		}
		if (upperWrap != null && !upperWrap.equals("pi")) {
			upper = new Relation(toReplace, RelationType.LESS_EQUAL, new RealTerm(upperWrap));
			oneChanged = true;
		}

		IntervalInformation interval = new IntervalInformation(invGraph, lower, upper);
		if (oneChanged)
			boundary.getDisjunction().addElement(interval);

		if (boundary.getDisjunction().getElements().size() > 0) {
			infos.add(boundary);
		}

		return infos;
	}

	@Override
	public int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION_IN_NODE;
	}

	@Override
	public int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION_IN_NODE;
	}

	@Override
	public int applyControlInformation(ControlInformation information, InvariantEdge edge) {
		return END_OF_INFORMATION;
	}

	@Override
	public int applyDataInformation(DataInformation information, InvariantEdge edge) {
		return 0;
	}

	@Override
	public List<InvariantInformation> generateInformationForFeedbackSimulation() {
		return generateInformation();
	}

	@Override
	public List<InvariantInformation> noInformationApplied(InvariantEdge edge, String informationString) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<InvariantInformation> evaluateConditionForFeedbackSimulation() {
		return Collections.EMPTY_LIST;
	}

}