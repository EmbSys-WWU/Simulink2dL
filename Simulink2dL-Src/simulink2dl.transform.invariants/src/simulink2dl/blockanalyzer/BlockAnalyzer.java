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
import java.util.List;
import org.conqat.lib.simulink.model.SimulinkBlock;

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
import simulink2dl.transform.Transformer;

public abstract class BlockAnalyzer {

	public static final int END_OF_INFORMATION = 0;
	public static final int END_OF_INFORMATION_IN_NODE = 0x000F;
	public static final int VALID_INFORMATION = 0x0010;
	public static final int INVALID_INFORMATION = 0;
	public static final int NON_VALID_INFORMATION = 0x0012;

	protected InvariantNode block;
	protected Transformer transformer;
	protected InvariantGraph invGraph;

	protected EqualityInformation zeroEquality;
	protected SignalboundaryInformation minusEquality;
	protected DiscreteSignalInformation minusEqualityDiscrete;

	protected EqualityInformation oneEquality;
	protected SignalboundaryInformation divisionEquality;
	protected DiscreteSignalInformation divisionEqualityDiscrete;

	public BlockAnalyzer(InvariantNode block, InvariantGraph invGraph) {
		this.block = block;
		this.transformer = transformer;
		this.invGraph = invGraph;

		zeroEquality = new EqualityInformation(invGraph,
				new Relation(new Variable("R", "dummy"), RelationType.EQUAL, new RealTerm(0)));
		minusEquality = new SignalboundaryInformation(invGraph, zeroEquality);
		minusEqualityDiscrete = new DiscreteSignalInformation(minusEquality);

		oneEquality = new EqualityInformation(invGraph,
				new Relation(new Variable("R", "dummy"), RelationType.EQUAL, new RealTerm(1)));
		divisionEquality = new SignalboundaryInformation(invGraph, oneEquality);
		divisionEqualityDiscrete = new DiscreteSignalInformation(divisionEquality);
	}

	public List<InvariantInformation> generateInformation() {
		return Collections.EMPTY_LIST;
	}

	public int applyInformation(InvariantInformation info, InvariantEdge edge) {
		if (info.getClass() == SignalboundaryInformation.class) {
			SignalboundaryInformation casted = (SignalboundaryInformation) info;
			return applySignalBoundary(casted, edge);
		} else if (info.getClass() == DiscreteSignalInformation.class) {
			DiscreteSignalInformation casted = (DiscreteSignalInformation) info;
			return applyDiscreteSignal(casted, edge);
		} else if (info.getClass() == ControlInformation.class) {
			ControlInformation casted = (ControlInformation) info;
			return applyControlInformation(casted, edge);
		} else if (info.getClass() == DataInformation.class) {
			DataInformation casted = (DataInformation) info;
			return applyDataInformation(casted, edge);
		}
		return INVALID_INFORMATION;
	}

	public abstract int applySignalBoundary(SignalboundaryInformation information, InvariantEdge edge);

	public abstract int applyDiscreteSignal(DiscreteSignalInformation information, InvariantEdge edge);

	public abstract int applyControlInformation(ControlInformation information, InvariantEdge edge);

	public abstract int applyDataInformation(DataInformation information, InvariantEdge edge);

	public abstract List<InvariantInformation> noInformationApplied(InvariantEdge edge, String informationString);

	public abstract List<InvariantInformation> generateInformationForFeedbackSimulation();

	public abstract List<InvariantInformation> evaluateConditionForFeedbackSimulation();

	public String getMacroIdentifier() {
		return block.getBlock().getName() + "_inv";
	}
}
