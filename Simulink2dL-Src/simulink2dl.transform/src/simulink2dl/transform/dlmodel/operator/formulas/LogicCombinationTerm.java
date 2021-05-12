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
package simulink2dl.transform.dlmodel.operator.formulas;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;
/**
 * Term for combining Relation-Terms with other Operators
 * The Term is a placeholder consisting of brackets and an inner Term, which can be replaced by macros.
 *
 */
public class LogicCombinationTerm implements Formula {

	private Term innerTerm;

	public LogicCombinationTerm(Term term) {
		this.innerTerm = term;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	/**
	 * @return the innerTerm
	 */
	public Term getInnerTerm() {
		return innerTerm;
	}

	/**
	 * @param innerTerm the innerTerm to set
	 */
	public void setInnerTerm(Term innerTerm) {
		this.innerTerm = innerTerm;
	}

	public String toString() {
		return this.innerTerm.toString();
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (innerTerm.equals(toReplace)) {
			if (replaceWith instanceof Formula) {
				innerTerm = replaceWith;
			} else {
				innerTerm = new Relation(replaceWith, Relation.RelationType.NOT_EQUAL, new RealTerm(0));
			}
		} else {
			innerTerm.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (innerTerm.equals(term)) {
			return true;
		}
		return innerTerm.containsTerm(term);
	}

	@Override
	public LogicCombinationTerm createDeepCopy() {
		return new LogicCombinationTerm(innerTerm.createDeepCopy());
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		if (innerTerm instanceof Operator)
			return ((Operator) innerTerm).toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains);
		else
			return innerTerm.toString();
	}

	@Override
	public Formula createNegation() {
		if (innerTerm instanceof Operator)
			return new Negation(((Operator) innerTerm).createDeepCopy());
		else
			return this;
	}

}
