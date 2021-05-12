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
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.graph.InvariantGraph;

public class ControlInformation extends Disjunction implements InvariantInformation {

	private InvariantGraph invGraph;

	private boolean inValid = false;
	private boolean isDone = false;

	public ControlInformation(InvariantGraph invGraph) {
		this.invGraph = invGraph;
	}

	@Override
	public Operator getInformationAsFormula() {
		return this; // TODO
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		for (Operator op : getElements()) {
			if (op instanceof Implication) {
				Implication casted = (Implication) op;
				((InvariantInformation) casted.getConsequent()).finalizeInformation(replaceTerm);
			}
		}
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		for (Operator op : getElements()) {
			if (op instanceof Implication) {
				Implication casted = (Implication) op;
				((InvariantInformation) casted.getConsequent()).changeReplaceableTerm(toReplace);
			}
		}
	}

	public void addImplication(Operator antecedent, InvariantInformation consequent) {
		Implication implication = new Implication(antecedent, consequent);
		this.addElement(implication);
	}

	public List<Implication> getImplications() {
		List<Implication> Implis = new LinkedList<>();
		for (Operator op : getElements()) {
			if (op instanceof Implication)
				Implis.add((Implication) op);
		}
		return Implis;
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	public Object clone() {
		ControlInformation newControl = new ControlInformation(invGraph);
		for (Implication impli : getImplications()) {
			InvariantInformation consequent = ((InvariantInformation) impli.getConsequent());
			consequent = (InvariantInformation) consequent.clone();
			newControl.addImplication(impli.getAntecedent().createDeepCopy(), consequent);
		}

		return newControl;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ControlInformation) {
			ControlInformation casted = (ControlInformation) obj;
			for (Implication impli : this.getImplications()) {
				boolean isIn = false;
				for (Implication impli2 : casted.getImplications()) {
					if (impli.equals(impli2)) {
						isIn = true;
						break;
					}
				}
				if (!isIn)
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isValid() {
		return !inValid;
	}

	@Override
	public void inValid() {
		inValid = true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void done() {
		isDone = true;
	}

	// For macros
	@Override
	public Disjunction createDeepCopy() {
		return (Disjunction) this.clone();
	}

	@Override
	public boolean containsTerm(Term term) {
		for (Implication element : getImplications()) {
			Operator antecedent = element.getAntecedent();
			if (antecedent.equals(term)) {
				return true;
			}
			if (antecedent.containsTerm(term)) {
				return true;
			}

			InvariantInformation consequent = (InvariantInformation) element.getConsequent();
			if (consequent.equals(term)) {
				return true;
			}
			if (consequent.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (int i = 0; i < getImplications().size(); i++) {
			Implication element = getImplications().get(i);
			Operator antecedent = element.getAntecedent();
			if (!antecedent.equals(toReplace) || antecedent.containsTerm(toReplace)) {
				antecedent.replaceTermRecursive(toReplace, replaceWith);
			}

			InvariantInformation consequent = (InvariantInformation) element.getConsequent();
			if (!consequent.equals(toReplace) || consequent.containsTerm(toReplace)) {
				consequent.replaceTermRecursive(toReplace, replaceWith);
			}
		}
	}

}
