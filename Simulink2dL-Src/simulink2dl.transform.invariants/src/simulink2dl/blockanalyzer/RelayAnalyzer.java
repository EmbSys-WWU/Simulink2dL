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
import simulink2dl.util.parser.StringToTerm;

public class RelayAnalyzer extends BlockAnalyzer {

	public RelayAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		super(block, invGraph);
	}

	@Override
	public List<InvariantInformation> generateInformation() {
		List<InvariantInformation> infos = new LinkedList<InvariantInformation>();

		Environment env = invGraph.getTransformer().getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(block.getBlock());

		ReplaceableTerm incomingEdge = env
				.getToReplace(block.getBlock().getInPort("1").getLine().getSrcPort().getBlock());

		ControlInformation information = new ControlInformation(invGraph);

		String switchOn = block.getBlock().getParameter("OnSwitchValue");
		String outputOn = block.getBlock().getParameter("OnOutputValue");
		String switchOff = block.getBlock().getParameter("OffSwitchValue");
		String outputOff = block.getBlock().getParameter("OffOutputValue");

		Relation antecedentOff = new Relation(incomingEdge, RelationType.LESS_EQUAL,
				StringToTerm.parseString(switchOff));
		SignalboundaryInformation sigOFF = new SignalboundaryInformation(invGraph, new EqualityInformation(invGraph,
				new Relation(toReplace, RelationType.EQUAL, new RealTerm(outputOff))));
		information.addImplication(antecedentOff, sigOFF);

		Relation antecedentOn = new Relation(incomingEdge, RelationType.GREATER_EQUAL,
				StringToTerm.parseString(switchOn));
		SignalboundaryInformation sigOn = new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, new Relation(toReplace, RelationType.EQUAL, new RealTerm(outputOn))));
		information.addImplication(antecedentOn, sigOn);

		Relation valueOn = new Relation(incomingEdge, RelationType.EQUAL, new RealTerm(outputOn));
		Relation valueOff = new Relation(incomingEdge, RelationType.EQUAL, new RealTerm(outputOff));
		DiscreteSignalInformation outSignalInfo = new DiscreteSignalInformation(new SignalboundaryInformation(invGraph,
				new EqualityInformation(invGraph, valueOff), new EqualityInformation(invGraph, valueOn)));

		infos.add(information);
		infos.add(outSignalInfo);

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
		return generateInformation();
	}

	@Override
	public List<InvariantInformation> evaluateConditionForFeedbackSimulation() {
		return Collections.EMPTY_LIST;
	}

}
