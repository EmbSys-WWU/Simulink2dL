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
package simulink2dl.dlmodel.operator.formula;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a logical implication.
 * 
 * @author Timm Liebrenz
 *
 */
public class Implication implements Formula {

	private Operator antecedent;

	private Operator consequent;

	/**
	 * Constructor for an implication.
	 * 
	 * @param antecedent
	 * @param consequent
	 */
	public Implication(Operator antecedent, Operator consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}

	@Override
	public String toString() {
		return antecedent.toString() + " -> (" + consequent.toString() + ")";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return antecedent.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains) + "\n" + indent
				+ "->\n" + indent
				+ consequent.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains);
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	public Operator getAntecedent() {
		return antecedent;
	}

	public Operator getConsequent() {
		return consequent;
	}

	public void setAntecedent(Operator op) {
		this.antecedent = op;
	}

	public void setConsequent(Operator op) {
		this.consequent = op;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (antecedent.equals(toReplace)) {
			antecedent = (Operator) replaceWith;
		} else {
			antecedent.replaceTermRecursive(toReplace, replaceWith);
		}
		if (consequent.equals(toReplace)) {
			consequent = (Operator) replaceWith;
		} else {
			consequent.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (antecedent.equals(term)) {
			return true;
		}
		if (consequent.equals(term)) {
			return true;
		}
		if (antecedent.containsTerm(term)) {
			return true;
		}
		if (consequent.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public Implication createDeepCopy() {
		return new Implication(antecedent.createDeepCopy(), consequent.createDeepCopy());
	}

	@Override
	public Conjunction createNegation() {
		// !(p=>q) <=> !(!p|q) <=> p&!q
		Conjunction newAnd = new Conjunction(this.antecedent.createDeepCopy());
		return newAnd.addLiterals(new Negation(this.consequent.createDeepCopy()));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Implication) {
			Implication otherImplication = (Implication) obj;
			return this.getAntecedent().equals(otherImplication.getAntecedent())
					&& this.getConsequent().equals(otherImplication.getConsequent());
		}
		return false;
	}
	
	@Override
	public Formula expand() {
		antecedent = antecedent.expand();
		consequent = consequent.expand();
		return this;
	}
}
