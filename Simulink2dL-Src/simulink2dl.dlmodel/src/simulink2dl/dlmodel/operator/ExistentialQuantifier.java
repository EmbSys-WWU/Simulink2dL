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
package simulink2dl.dlmodel.operator;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents an existential quantifier for a given variable.
 * 
 * @author Timm Liebrenz
 *
 */
public class ExistentialQuantifier implements Operator {

	private Variable variable;

	private Operator innerOperator;

	/**
	 * Constructor for a universal quantifier.
	 * 
	 * @param variable
	 * @param innerOperator
	 */
	public ExistentialQuantifier(Variable variable, Operator innerOperator) {
		this.variable = variable;
		this.innerOperator = innerOperator;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String toString() {
		// TODO: check this for the correct operator
		return "exists " + variable.toString() + " (" + innerOperator.toString() + ")";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		// TODO: check this for the correct operator
		return "exists " + variable.toString() + indent + "("
				+ innerOperator.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains)
				+ ")";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (innerOperator.equals(toReplace)) {
			innerOperator = (Operator) replaceWith;
		} else {
			innerOperator.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (innerOperator.equals(term)) {
			return true;
		}
		return innerOperator.containsTerm(term);
	}

	@Override
	public ExistentialQuantifier createDeepCopy() {
		return new ExistentialQuantifier(variable.createDeepCopy(), innerOperator.createDeepCopy());
	}

}
