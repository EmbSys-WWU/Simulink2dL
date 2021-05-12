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
package simulink2dl.invariants.information;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.satisfiability.FormulaChecker;
import simulink2dl.util.satisfiability.FormulaChecker.ResultType;

public class IntervalInformation implements InvariantInformation {

	private InvariantGraph invGraph;

	private Relation lowerBound;
	private Relation upperBound;

	private boolean invalid = false;
	private boolean isDone = false;

	public IntervalInformation(InvariantGraph invGraph, Relation lowerBound, Relation upperBound) {
		this.invGraph = invGraph;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public IntervalInformation(InvariantGraph invGraph, Term leftSideLow, RelationType typeLow, Term rightSideLow,
			Term leftSideUp, RelationType typeUp, Term rightSideUp) {
		this.invGraph = invGraph;
		this.lowerBound = new Relation(leftSideLow, typeLow, rightSideLow);
		this.upperBound = new Relation(leftSideUp, typeUp, rightSideUp);
	}

	@Override
	public boolean isValid() {
		return !invalid;
	}

	public void inValid() {
		invalid = true;
	}

	@Override
	public Conjunction getInformationAsFormula() {
		return new Conjunction(lowerBound, upperBound);
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		getLowerBound().setLeftSide(replaceTerm);
		getUpperBound().setLeftSide(replaceTerm);
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		lowerBound.setLeftSide(toReplace);
		upperBound.setLeftSide(toReplace);
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	@Override
	public Object clone() {
		return new IntervalInformation(invGraph,
				new Relation(lowerBound.getLeftSide().createDeepCopy(), lowerBound.getType(),
						lowerBound.getRightSide().createDeepCopy()),
				new Relation(upperBound.getLeftSide().createDeepCopy(), upperBound.getType(),
						upperBound.getRightSide().createDeepCopy()));
	}

	public Relation getLowerBound() {
		return this.lowerBound;
	}

	public Relation getUpperBound() {
		return this.upperBound;
	}

	public boolean crossesZero() {
		if (this.crossesValue(RelationType.EQUAL, "0"))
			return true;
		return false;
	}

	public boolean crossesValue(RelationType type, String value) {
		IntervalInformation clonedInfo2 = (IntervalInformation) this.clone();
		Variable leftSide = new Variable("R", "x");
		clonedInfo2.getLowerBound().setLeftSide(leftSide);
		clonedInfo2.getUpperBound().setLeftSide(leftSide);
		Relation isZero = new Relation(leftSide, type, new RealTerm(value));
		Conjunction zeroCrossingFormula = new Conjunction(clonedInfo2.getLowerBound(), clonedInfo2.getUpperBound(),
				isZero);

		FormulaChecker zeroCrossing = new FormulaChecker();

		if (zeroCrossing.checkSingleFormula(zeroCrossingFormula) == ResultType.SATISFIABLE) {
			return true;
		} else {
			return false;
		}
	}

	public void setLowerBound(Relation lowerBound) {
		this.lowerBound = lowerBound;
	}

	public void setUpperBound(Relation upperBound) {
		this.upperBound = upperBound;
	}

	@Override
	public String toString() {
		return "(" + getLowerBound().toString() + " & " + getUpperBound().toString() + ")";
	}

	public boolean equals(Object info) {
		if (info.getClass() == IntervalInformation.class) {
			IntervalInformation casted = (IntervalInformation) info;
			return lowerBound.equals(casted.getLowerBound()) && upperBound.equals(casted.getUpperBound());
		}
		return false;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void done() {
		isDone = true;
	}

	// Utility
	public static List<IntervalInformation> calcInterval(IntervalInformation info1, String sign,
			IntervalInformation info2) {
		List<IntervalInformation> newInfos = new LinkedList<>();

		IntervalInformation newInfo = (IntervalInformation) info1.clone();
		if (sign.charAt(0) == '+') {
			AdditionTerm lower = new AdditionTerm(info1.getLowerBound().getRightSide());
			lower.add(info2.getLowerBound().getRightSide());
			AdditionTerm upper = new AdditionTerm(info1.getUpperBound().getRightSide());
			upper.add(info2.getUpperBound().getRightSide());

			RelationType lowerType = info1.getLowerBound().getType() == RelationType.GREATER_THAN
					? RelationType.GREATER_THAN
					: info2.getLowerBound().getType() == RelationType.GREATER_THAN ? RelationType.GREATER_THAN
							: RelationType.GREATER_EQUAL;
			RelationType upperType = info1.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
					: info2.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
							: RelationType.LESS_EQUAL;

			newInfo.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(), lowerType, lower));
			newInfo.setUpperBound(new Relation(info1.getLowerBound().getLeftSide(), upperType, upper));

			newInfos.add(newInfo);
		} else if (sign.charAt(0) == '-') {
			AdditionTerm lower = new AdditionTerm(info1.getLowerBound().getRightSide());
			lower.subtract(info2.getUpperBound().getRightSide());
			AdditionTerm upper = new AdditionTerm(info1.getUpperBound().getRightSide());
			upper.subtract(info2.getLowerBound().getRightSide());

			RelationType lowerType = info1.getLowerBound().getType() == RelationType.GREATER_THAN
					? RelationType.GREATER_THAN
					: info2.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.GREATER_THAN
							: RelationType.GREATER_EQUAL;
			RelationType upperType = info1.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
					: info2.getLowerBound().getType() == RelationType.GREATER_THAN ? RelationType.LESS_THAN
							: RelationType.LESS_EQUAL;

			newInfo.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(), lowerType, lower));
			newInfo.setUpperBound(new Relation(info1.getLowerBound().getLeftSide(), upperType, upper));

			newInfos.add(newInfo);
		} else if (sign.charAt(0) == '*') {
			MultiplicationTerm lower = new MultiplicationTerm(info1.getLowerBound().getRightSide());
			lower.multiplyBy(info2.getLowerBound().getRightSide());
			MultiplicationTerm upper = new MultiplicationTerm(info1.getUpperBound().getRightSide());
			upper.multiplyBy(info2.getUpperBound().getRightSide());

			RelationType lowerType = info1.getLowerBound().getType() == RelationType.GREATER_THAN
					? RelationType.GREATER_THAN
					: info2.getLowerBound().getType() == RelationType.GREATER_THAN ? RelationType.GREATER_THAN
							: RelationType.GREATER_EQUAL;
			RelationType upperType = info1.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
					: info2.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
							: RelationType.LESS_EQUAL;

			newInfo.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(), lowerType, lower));
			newInfo.setUpperBound(new Relation(info1.getLowerBound().getLeftSide(), upperType, upper));

			newInfos.add(newInfo);
		} else if (sign.charAt(0) == '/') {
			// do calculation
			MultiplicationTerm lower = new MultiplicationTerm(info1.getLowerBound().getRightSide());
			lower.dividedBy(info2.getUpperBound().getRightSide());
			MultiplicationTerm upper = new MultiplicationTerm(info1.getUpperBound().getRightSide());
			upper.dividedBy(info2.getLowerBound().getRightSide());

			RelationType lowerType = info1.getLowerBound().getType() == RelationType.GREATER_THAN
					? RelationType.GREATER_THAN
					: info2.getLowerBound().getType() == RelationType.GREATER_THAN ? RelationType.GREATER_THAN
							: RelationType.GREATER_EQUAL;
			RelationType upperType = info1.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
					: info2.getUpperBound().getType() == RelationType.LESS_THAN ? RelationType.LESS_THAN
							: RelationType.LESS_EQUAL;

			if (newInfo.crossesZero()) {
				/*
				 * newInfo.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(),
				 * lowerType, lower)); newInfo.setUpperBound(new
				 * Relation(info1.getLowerBound().getLeftSide(), RelationType.LESS_THAN, new
				 * RealTerm(0)));
				 * 
				 * IntervalInformation newInfo2 = (IntervalInformation) newInfo.clone();
				 * newInfo2.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(),
				 * RelationType.GREATER_THAN, new RealTerm(0))); newInfo2.setUpperBound(new
				 * Relation(info1.getLowerBound().getLeftSide(), upperType, lower));
				 * 
				 * newInfos.add(newInfo); newInfos.add(newInfo2);
				 */
				// TODO
			} else {
				newInfo.setLowerBound(new Relation(info1.getLowerBound().getLeftSide(), lowerType, lower));
				newInfo.setUpperBound(new Relation(info1.getLowerBound().getLeftSide(), upperType, upper));
				newInfos.add(newInfo);
			}
		}

		if (newInfos.isEmpty()) {
			info1.inValid();
			newInfos.add(info1);
			info2.inValid();
			newInfos.add(info2);
		}

		return newInfos;
	}

	public static IntervalInformation calcEquality(IntervalInformation info1, String sign, EqualityInformation info2) {
		IntervalInformation newInfo = (IntervalInformation) info1.clone();

		if (info2.getEquality().getType() == RelationType.NOT_EQUAL) {
			newInfo.inValid();
			return newInfo;
		}

		if (sign.charAt(0) == '+') {
			AdditionTerm lower = new AdditionTerm(info1.getLowerBound().getRightSide());
			lower.add(info2.getEquality().getRightSide());
			AdditionTerm upper = new AdditionTerm(info1.getUpperBound().getRightSide());
			upper.add(info2.getEquality().getRightSide());

			newInfo.setLowerBound(
					new Relation(info1.getLowerBound().getLeftSide(), info1.getLowerBound().getType(), lower));
			newInfo.setUpperBound(
					new Relation(info1.getUpperBound().getLeftSide(), info1.getUpperBound().getType(), upper));
		} else if (sign.charAt(0) == '-') {
			AdditionTerm lower = new AdditionTerm(info1.getLowerBound().getRightSide());
			lower.subtract(info2.getEquality().getRightSide());
			AdditionTerm upper = new AdditionTerm(info1.getUpperBound().getRightSide());
			upper.subtract(info2.getEquality().getRightSide());

			newInfo.setLowerBound(
					new Relation(info1.getLowerBound().getLeftSide(), info1.getLowerBound().getType(), lower));
			newInfo.setUpperBound(
					new Relation(info1.getUpperBound().getLeftSide(), info1.getUpperBound().getType(), upper));
		} else if (sign.charAt(0) == '*') {
			MultiplicationTerm lower = new MultiplicationTerm(info1.getLowerBound().getRightSide());
			lower.multiplyBy(info2.getEquality().getRightSide());
			MultiplicationTerm upper = new MultiplicationTerm(info1.getUpperBound().getRightSide());
			upper.multiplyBy(info2.getEquality().getRightSide());

			newInfo.setLowerBound(
					new Relation(info1.getLowerBound().getLeftSide(), info1.getLowerBound().getType(), lower));
			newInfo.setUpperBound(
					new Relation(info1.getUpperBound().getLeftSide(), info1.getUpperBound().getType(), upper));
		} else if (sign.charAt(0) == '/') {
			if (info2.crossesZero() && info2.getEquality().getType() == RelationType.EQUAL) {
				PluginLogger.warning("Dividing by 0");
				newInfo.inValid();
			} else {
				MultiplicationTerm lower = new MultiplicationTerm(info1.getLowerBound().getRightSide());
				lower.dividedBy(info2.getEquality().getRightSide());
				MultiplicationTerm upper = new MultiplicationTerm(info1.getUpperBound().getRightSide());
				upper.dividedBy(info2.getEquality().getRightSide());

				newInfo.setLowerBound(
						new Relation(info1.getLowerBound().getLeftSide(), info1.getLowerBound().getType(), lower));
				newInfo.setUpperBound(
						new Relation(info1.getUpperBound().getLeftSide(), info1.getUpperBound().getType(), upper));
			}
		}

		return newInfo;
	}

	// For Macros
	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (lowerBound.containsTerm(toReplace))
			lowerBound.replaceTermRecursive(toReplace, replaceWith);
		if (upperBound.containsTerm(toReplace))
			upperBound.replaceTermRecursive(toReplace, replaceWith);
	}

	@Override
	public boolean containsTerm(Term term) {
		return lowerBound.containsTerm(term) || upperBound.containsTerm(term);
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return null;
	}

	@Override
	public Operator createDeepCopy() {
		return (IntervalInformation) this.clone();
	}

}
