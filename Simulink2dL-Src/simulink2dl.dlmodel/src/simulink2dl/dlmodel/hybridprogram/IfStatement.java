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
 * This class represents if statements as well as if-else statements.
 * 
 * @author Timm Liebrenz
 *
 */
public class IfStatement implements HybridProgram {

	private Formula condition;

	private HybridProgram ifProgram;

	private HybridProgram elseProgram;

	public IfStatement(Formula condition, HybridProgram ifProgram) {
		this.condition = condition;
		this.ifProgram = ifProgram;
		this.elseProgram = null;
	}

	public IfStatement(Formula condition, HybridProgram ifProgram, HybridProgram elseProgram) {
		this.condition = condition;
		this.ifProgram = ifProgram;
		this.elseProgram = elseProgram;
	}

	@Override
	public String toString() {
		String conditional = "if " + condition.toString() + " then " + ifProgram.toString();
		if (elseProgram != null) {
			conditional += "else " + elseProgram.toString();
		}
		conditional += " fi";
		return conditional;
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String conditional = "if " + condition.toString() + "\n" + indent + "then "
				+ ifProgram.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains);
		if (elseProgram != null) {
			conditional += "\n" + indent + "else "
					+ elseProgram.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains);
		}
		conditional += "\n" + indent + "fi";
		return conditional;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (condition.equals(toReplace)) {
			condition = (Formula) replaceWith;
		} else {
			condition.replaceTermRecursive(toReplace, replaceWith);
		}
		ifProgram.replaceTermRecursive(toReplace, replaceWith);
		if (elseProgram != null) {
			elseProgram.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (condition.containsTerm(term)) {
			return true;
		}
		if (ifProgram.containsTerm(term)) {
			return true;
		}
		if (elseProgram.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public IfStatement createDeepCopy() {
		return new IfStatement(condition.createDeepCopy(), ifProgram.createDeepCopy(),
				elseProgram != null ? elseProgram.createDeepCopy() : null);
	}

	public HybridProgram getIfProgram() {
		return ifProgram;
	}

	public HybridProgram getElseProgram() {
		return elseProgram;
	}

	public boolean hasElse() {
		return elseProgram != null;
	}

	public Formula getCondition() {
		return condition;
	}

	@Override
	public HybridProgram expand() {
		condition.expand();
		this.ifProgram = ifProgram.expand();
		if (elseProgram != null) {
			this.elseProgram = elseProgram.expand();
		}
		return this;
	}


}
