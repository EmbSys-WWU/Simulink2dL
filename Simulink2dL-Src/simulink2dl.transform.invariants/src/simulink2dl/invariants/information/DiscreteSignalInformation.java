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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;

public class DiscreteSignalInformation extends Disjunction implements InvariantInformation {

	private boolean inValid = false;
	private boolean isDone = false;

	public DiscreteSignalInformation(Operator... newElements) {
		super(newElements);
	}

	@Override
	public Operator getInformationAsFormula() {
		Disjunction dis = new Disjunction();
		for (Operator op : dis.getElements()) {
			if (op instanceof InvariantInformation) {
				dis.addElement(((InvariantInformation) op).getInformationAsFormula());
			} else {
				dis.addElement(op);
			}
		}
		return dis;
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		for (Operator op : getElements()) {
			if (SignalboundaryInformation.class == op.getClass()) {
				SignalboundaryInformation casted = (SignalboundaryInformation) op;
				casted.finalizeInformation(replaceTerm);
			}
		}
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		for (Operator op : getElements()) {
			if (op instanceof InvariantInformation) {
				((InvariantInformation) op).changeReplaceableTerm(toReplace);
			}
		}
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	@Override
	public boolean isValid() {
		if (inValid)
			return false;
		for (Operator op : getElements()) {
			if (op instanceof InvariantInformation) {
				InvariantInformation casted = (InvariantInformation) op;
				if (!casted.isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void inValid() {
		inValid = true;
	}

	public boolean crossesZero() {
		if (this.crossesValue(RelationType.EQUAL, "0"))
			return true;
		return false;
	}

	public boolean crossesValue(RelationType type, String value) {
		for (Operator op : this.getElements()) {
			if (op instanceof SignalboundaryInformation) {
				if (!((SignalboundaryInformation) op).crossesValue(type, value))
					return false;
			}
		}
		return true;
	}

	public Object clone() {
		DiscreteSignalInformation clone = new DiscreteSignalInformation();
		for (Operator op : getElements()) {
			SignalboundaryInformation clonedSig = (SignalboundaryInformation) op;
			clone.addElement((SignalboundaryInformation) clonedSig.clone());
		}
		return clone;
	}

	public boolean equals(Object obj) {
		if (DiscreteSignalInformation.class == obj.getClass()) {
			DiscreteSignalInformation casted = (DiscreteSignalInformation) obj;

			for (Operator op : getElements()) {
				if (op.getClass() == SignalboundaryInformation.class) {
					SignalboundaryInformation casted2 = (SignalboundaryInformation) op;
					boolean isIn = false;
					for (Operator op2 : casted.getElements()) {
						if (op2.getClass() == SignalboundaryInformation.class) {
							SignalboundaryInformation casted3 = (SignalboundaryInformation) op2;

							isIn = casted2.equals(casted3);
							if (isIn)
								break;
						}
					}
					if (!isIn)
						return false;
				}
			}
			return true;
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

	// Utitlity
	public static DiscreteSignalInformation calcDiscrete(DiscreteSignalInformation info1, String sign,
			DiscreteSignalInformation info2) {
		DiscreteSignalInformation newInfo = (DiscreteSignalInformation) info1.clone();
		newInfo.setElements(new LinkedList<Operator>());

		for (Operator op : info1.getElements()) {
			if (op.getClass() == SignalboundaryInformation.class) {
				SignalboundaryInformation casted = (SignalboundaryInformation) op;

				for (Operator op2 : info2.getElements()) {
					if (op2.getClass() == SignalboundaryInformation.class) {
						SignalboundaryInformation casted2 = (SignalboundaryInformation) op2;

						SignalboundaryInformation newSig = SignalboundaryInformation.calcSignal(casted, sign, casted2);
						newInfo.addElement(newSig);
					}
				}
			}
		}

		return newInfo;
	}

	public static DiscreteSignalInformation calcSignal(DiscreteSignalInformation info1, String sign,
			SignalboundaryInformation info2) {
		DiscreteSignalInformation newInfo = (DiscreteSignalInformation) info1.clone();
		newInfo.setElements(new LinkedList<Operator>());

		for (Operator op : info1.getElements()) {
			if (SignalboundaryInformation.class == op.getClass()) {
				SignalboundaryInformation casted = (SignalboundaryInformation) op;
				casted = (SignalboundaryInformation) casted.clone();

				SignalboundaryInformation newSignal = SignalboundaryInformation.calcSignal(casted, sign, info2);
				newInfo.addElement(newSignal);
			}
		}

		return newInfo;
	}

	// For Macros
	@Override
	public boolean containsTerm(Term term) {
		return super.containsTerm(term);
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		super.replaceTermRecursive(toReplace, replaceWith);
	}

}
