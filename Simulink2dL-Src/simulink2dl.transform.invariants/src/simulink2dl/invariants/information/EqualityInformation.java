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

public class EqualityInformation implements InvariantInformation {

	private InvariantGraph invGraph;

	private Relation equality;

	private boolean inValid = false;
	private boolean isDone = false;

	public EqualityInformation(InvariantGraph invGraph, Relation relation) {
		this.invGraph = invGraph;
		this.equality = relation;
	}

	@Override
	public Relation getInformationAsFormula() {
		return equality;
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		equality.setLeftSide(replaceTerm);
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		equality.setLeftSide(toReplace);
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	@Override
	public Object clone() {
		return new EqualityInformation(invGraph, new Relation(getEquality().getLeftSide().createDeepCopy(),
				getEquality().getType(), getEquality().getRightSide().createDeepCopy()));
	}

	public Relation getEquality() {
		return equality;
	}

	public boolean crossesZero() {
		if (this.crossesValue(RelationType.EQUAL, "0"))
			return true;
		return false;
	}

	public boolean crossesValue(RelationType type, String value) {
		EqualityInformation clonedInfo2 = (EqualityInformation) this.clone();
		Variable leftSide = new Variable("R", "x");
		clonedInfo2.getEquality().setLeftSide(leftSide);
		Relation isZero = new Relation(leftSide, type, new RealTerm(value));
		Conjunction zeroCrossingFormula = new Conjunction(clonedInfo2.getEquality(), isZero);

		FormulaChecker zeroCrossing = new FormulaChecker();
		if (zeroCrossing.checkSingleFormula(zeroCrossingFormula) == ResultType.SATISFIABLE) {
			return true;
		} else {
			return false;
		}
	}

	public void inValid() {
		inValid = true;
	}

	@Override
	public boolean isValid() {
		return !inValid;
	}

	@Override
	public String toString() {
		return getEquality().toString();
	}

	public boolean equals(Object info) {
		if (info.getClass() == EqualityInformation.class) {
			EqualityInformation casted = (EqualityInformation) info;
			return this.getEquality().equals(casted.getEquality());
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
	public static EqualityInformation calcEquality(EqualityInformation info1, String sign, EqualityInformation info2) {
		EqualityInformation newInfo = (EqualityInformation) info1.clone();

		// set correct type
		if (info1.getEquality().getType() == RelationType.NOT_EQUAL
				&& info2.getEquality().getType() == RelationType.NOT_EQUAL) {
			info1.inValid();
			return info1;
		}

		if (sign.charAt(0) == '+') {
			AdditionTerm add = new AdditionTerm(info1.getEquality().getRightSide());
			add.add(info2.getEquality().getRightSide());

			newInfo.getEquality().setRightSide(add);
		} else if (sign.charAt(0) == '-') {
			AdditionTerm add = new AdditionTerm(info1.getEquality().getRightSide());
			add.subtract(info2.getEquality().getRightSide());

			newInfo.getEquality().setRightSide(add);
		} else if (sign.charAt(0) == '*') {
			MultiplicationTerm add = new MultiplicationTerm(info1.getEquality().getRightSide());
			add.multiplyBy(info2.getEquality().getRightSide());

			newInfo.getEquality().setRightSide(add);
		}
		if (sign.charAt(0) == '/') {
			if (info2.crossesZero() && info2.getEquality().getType() == RelationType.EQUAL) {
				PluginLogger.warning("Dividing by 0");
				newInfo.inValid();
			} else {
				MultiplicationTerm add = new MultiplicationTerm(info1.getEquality().getRightSide());
				add.dividedBy(info2.getEquality().getRightSide());

				newInfo.getEquality().setRightSide(add);
			}
		}

		if (info1.getEquality().getType() == RelationType.NOT_EQUAL
				|| info2.getEquality().getType() == RelationType.NOT_EQUAL)
			newInfo.getEquality().setType(RelationType.NOT_EQUAL);

		return newInfo;
	}

	public static IntervalInformation calcInterval(EqualityInformation info1, String sign, IntervalInformation info2) {
		IntervalInformation newInfo = (IntervalInformation) info2.clone();

		if (info1.getEquality().getType() == RelationType.NOT_EQUAL) {
			newInfo.inValid();
			return newInfo;
		}

		if (sign.charAt(0) == '+') {
			AdditionTerm additionTerm = new AdditionTerm(info1.getEquality().getRightSide());
			additionTerm.add(info2.getLowerBound().getRightSide());
			newInfo.getLowerBound().setRightSide(additionTerm);

			additionTerm = new AdditionTerm(info1.getEquality().getRightSide());
			additionTerm.add(info2.getUpperBound().getRightSide());
			newInfo.getUpperBound().setRightSide(additionTerm);
		} else if (sign.charAt(0) == '-') {
			AdditionTerm additionTerm = new AdditionTerm(info1.getEquality().getRightSide());
			additionTerm.subtract(info2.getUpperBound().getRightSide());
			newInfo.getLowerBound().setRightSide(additionTerm);

			additionTerm = new AdditionTerm(info1.getEquality().getRightSide());
			additionTerm.subtract(info2.getLowerBound().getRightSide());
			newInfo.getUpperBound().setRightSide(additionTerm);
		} else if (sign.charAt(0) == '*') {
			MultiplicationTerm multiplicationTerm = new MultiplicationTerm(info1.getEquality().getRightSide());
			multiplicationTerm.multiplyBy(info2.getLowerBound().getRightSide());
			newInfo.getLowerBound().setRightSide(multiplicationTerm);

			multiplicationTerm = new MultiplicationTerm(info1.getEquality().getRightSide());
			multiplicationTerm.multiplyBy(info2.getUpperBound().getRightSide());
			newInfo.getUpperBound().setRightSide(multiplicationTerm);
		}
		if (sign.charAt(0) == '/') {
			if (info2.crossesZero()) {
				// TODO
			} else {
				MultiplicationTerm multiplicationTerm = new MultiplicationTerm(info1.getEquality().getRightSide());
				multiplicationTerm.dividedBy(info2.getLowerBound().getRightSide());
				newInfo.getLowerBound().setRightSide(multiplicationTerm);

				multiplicationTerm = new MultiplicationTerm(info1.getEquality().getRightSide());
				multiplicationTerm.dividedBy(info2.getUpperBound().getRightSide());
				newInfo.getUpperBound().setRightSide(multiplicationTerm);
			}
		}

		return newInfo;
	}

	// For Macros
	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		equality.replaceTermRecursive(toReplace, replaceWith);
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
		return (EqualityInformation) this.clone();
	}

	@Override
	public boolean containsTerm(Term term) {
		return equality.containsTerm(term);
	}

}
