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
package simulink2dl.dlmodel.hybridprogram;

import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represent a test for a given formula.
 * 
 * @author Timm Liebrenz
 *
 */
public class TestFormula implements HybridProgram {

	private Formula formula;

	/**
	 * Constructor that takes a formula to test.
	 * 
	 * @param formula
	 */
	public TestFormula(Formula formula) {
		this.formula = formula;
	}

	public Formula getFormula() {
		return formula;
	}

	@Override
	public String toString() {
		return "?(" + formula.toString() + ");";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		if (multiLineTestFormulas) {
			return "?(" + formula.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains)
					+ ");";
		} else {
			return toString();
		}
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (formula.equals(toReplace)) {
			formula = (Formula) replaceWith;
		} else {
			formula.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (formula.equals(term)) {
			return true;
		}
		return formula.containsTerm(term);
	}

	@Override
	public TestFormula createDeepCopy() {
		return new TestFormula(formula.createDeepCopy());
	}

	@Override
	public HybridProgram expand() {
		/*formula = formula.expand();*/
		return this;
	}

}
