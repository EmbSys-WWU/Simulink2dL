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
 * This class represents a logical equivalence (bi-implication).
 * 
 * @author Timm Liebrenz
 *
 */
public class Equivalence implements Formula {

	private Operator leftSide;

	private Operator rightSide;

	/**
	 * Constructor for an equivalence.
	 * 
	 * @param antecedent
	 * @param consequent
	 */
	public Equivalence(Operator leftSide, Operator rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String toString() {
		return leftSide.toString() + " <-> " + rightSide.toString();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return leftSide.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains) + "\n" + indent
				+ "<->\n" + indent
				+ rightSide.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains);
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (leftSide.equals(toReplace)) {
			leftSide = (Operator) replaceWith;
		} else {
			leftSide.replaceTermRecursive(toReplace, replaceWith);
		}
		if (rightSide.equals(toReplace)) {
			rightSide = (Operator) replaceWith;
		} else {
			rightSide.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (leftSide.equals(term)) {
			return true;
		}
		if (rightSide.equals(term)) {
			return true;
		}
		if (leftSide.containsTerm(term)) {
			return true;
		}
		if (rightSide.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public Equivalence createDeepCopy() {
		return new Equivalence(leftSide.createDeepCopy(), rightSide.createDeepCopy());
	}

	@Override
	public Disjunction createNegation() {
		// !(a<=>b) <=> (!a&b)|(a&!b)
		Conjunction and1 = new Conjunction(new Negation(this.leftSide.createDeepCopy()),
				this.rightSide.createDeepCopy());
		Conjunction and2 = new Conjunction(this.leftSide.createDeepCopy(),
				new Negation(this.rightSide.createDeepCopy()));
		return new Disjunction(and1, and2);
	}
	
	@Override
	public Formula expand() {
		leftSide = leftSide.expand();
		rightSide = rightSide.expand();
		return this;
	}

}
