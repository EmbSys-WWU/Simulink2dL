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
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.graph.InvariantGraph;

public class SignalboundaryInformation implements InvariantInformation {

	InvariantGraph invGraph;

	private boolean inValid = false;
	private boolean isDone = false;

	private Disjunction disjunction;

	public Disjunction getInformationAsFormula() {
		Disjunction dis = new Disjunction();
		for (Operator op : disjunction.getElements()) {
			if (op instanceof InvariantInformation) {
				dis.addElement(((InvariantInformation) op).getInformationAsFormula());
			} else {
				dis.addElement(op);
			}
		}
		return dis;
	}

	public SignalboundaryInformation(InvariantGraph invGraph, Operator... newElements) {
		this.invGraph = invGraph;
		disjunction = new Disjunction(newElements);
	}

	public SignalboundaryInformation(InvariantGraph invGraph, List<Operator> newElements) {
		this.invGraph = invGraph;
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		for (Operator op : disjunction.getElements()) {
			if (op instanceof Relation) {
				Relation relation = (Relation) op;
				relation.setLeftSide(replaceTerm);
			}
			if (op instanceof InvariantInformation) {
				((InvariantInformation) op).finalizeInformation(replaceTerm);
			}
		}
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		for (Operator op : disjunction.getElements()) {
			if (op instanceof InvariantInformation) {
				((InvariantInformation) op).changeReplaceableTerm(toReplace);
			}
		}
	}

	@Override
	public boolean isValid() {
		if (inValid)
			return false;
		for (Operator op : getDisjunction().getElements()) {
			if (op instanceof InvariantInformation)
				continue;
			InvariantInformation casted = (InvariantInformation) op;
			if (!casted.isValid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void inValid() {
		inValid = true;
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	public boolean crossesZero() {
		if (this.crossesValue(RelationType.EQUAL, "0"))
			return true;
		return false;
	}

	public boolean crossesValue(RelationType type, String value) {
		for (Operator op : disjunction.getElements()) {
			if (op instanceof EqualityInformation) {
				if (((EqualityInformation) op).crossesValue(type, value))
					return true;
			}
			if (op instanceof IntervalInformation) {
				if (((IntervalInformation) op).crossesValue(type, value))
					return true;
			}
		}
		return false;
	}

	@Override
	public Object clone() {
		SignalboundaryInformation sig = new SignalboundaryInformation(invGraph);
		for (Operator op : this.disjunction.getElements()) {
			try {
				InvariantInformation casted = (InvariantInformation) op;
				sig.disjunction.addElement((InvariantInformation) casted.clone());
			} catch (Exception e) {

			}
		}
		return sig;
	}

	public boolean equals(Object obj) {
		if (obj.getClass() == SignalboundaryInformation.class) {
			SignalboundaryInformation objCasted = (SignalboundaryInformation) obj;
			for (Operator op : disjunction.getElements()) { // for each element find a element in obj
				boolean contains = false;
				if (op.getClass() == EqualityInformation.class) { // if equality find equality
					EqualityInformation casted = (EqualityInformation) op;
					for (Operator op2 : objCasted.getDisjunction().getElements()) {
						if (op2.getClass() == EqualityInformation.class) {
							EqualityInformation casted2 = (EqualityInformation) op2;
							contains = casted.equals(casted2);
						}
						if (contains)
							return true; // equality found in obj
					}
					if (!contains)
						return false;
				}
				if (op.getClass() == IntervalInformation.class) { // if interval find interval
					IntervalInformation casted = (IntervalInformation) op;
					for (Operator op2 : objCasted.getDisjunction().getElements()) {
						if (op2.getClass() == IntervalInformation.class) {
							IntervalInformation casted2 = (IntervalInformation) op2;
							contains = casted.equals(casted2);
						}
						if (contains)
							return true; // interval found in obj
					}
					if (!contains)
						return false;
				}
			}
		}
		return false;
	}

	public Disjunction getDisjunction() {
		return this.disjunction;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void done() {
		isDone = true;
	}

	@Override
	public String toString() {
		return this.disjunction.toString();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return null;
	}

	@Override
	public Operator createDeepCopy() {
		return (SignalboundaryInformation) this.clone();
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	// Utility
	public static SignalboundaryInformation calcSignal(SignalboundaryInformation info1, String sign,
			SignalboundaryInformation info2) {
		SignalboundaryInformation newInfo = (SignalboundaryInformation) info1.clone();
		newInfo.getDisjunction().setElements(new LinkedList<Operator>());

		for (Operator op : info1.getDisjunction().getElements()) {
			if (op.getClass() == EqualityInformation.class) {
				EqualityInformation casted = (EqualityInformation) op;

				for (Operator op2 : info2.getDisjunction().getElements()) {
					if (op2 instanceof EqualityInformation) {
						EqualityInformation casted2 = (EqualityInformation) op2;

						EqualityInformation newEquality = EqualityInformation.calcEquality(casted, sign, casted2);
						newInfo.getDisjunction().addElement(newEquality);
					} else if (op2 instanceof IntervalInformation) {
						IntervalInformation casted2 = (IntervalInformation) op2;

						IntervalInformation newInterval = EqualityInformation.calcInterval(casted, sign, casted2);
						newInfo.getDisjunction().addElement(newInterval);
					}
				}
			} else if (op.getClass() == IntervalInformation.class) {
				IntervalInformation casted = (IntervalInformation) op;

				for (Operator op2 : info2.getDisjunction().getElements()) {
					if (op2 instanceof EqualityInformation) {
						EqualityInformation casted2 = (EqualityInformation) op2;

						IntervalInformation newInterval = IntervalInformation.calcEquality(casted, sign, casted2);
						newInfo.getDisjunction().addElement(newInterval);
					} else if (op2 instanceof IntervalInformation) {
						IntervalInformation casted2 = (IntervalInformation) op2;

						List<IntervalInformation> newInterval = IntervalInformation.calcInterval(casted, sign, casted2);
						for (IntervalInformation in : newInterval) {
							newInfo.getDisjunction().addElement(in);
						}
					}
				}
			}
		}

		/*
		 * for ( int i = 0; i < newInfo.getDisjunction().getElements().size(); i++ ) {
		 * if ( newInfo.getDisjunction().getElements().get(i) instanceof
		 * IntervalInformation ) { IntervalInformation sig = (IntervalInformation)
		 * newInfo.getDisjunction().getElements().get(i); if ( i+1 <
		 * newInfo.getDisjunction().getElements().size() ) { IntervalInformation sig2 =
		 * (IntervalInformation) newInfo.getDisjunction().getElements().get(i+1);
		 * Relation rel = sig2.getLowerBound();
		 * rel.setRightSide(sig.getUpperBound().getRightSide()); } } }
		 */

		return newInfo;
	}

	// For Macros
	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		this.disjunction.replaceTermRecursive(toReplace, replaceWith);
	}

	@Override
	public boolean containsTerm(Term term) {
		return this.disjunction.containsTerm(term);
	}

}
