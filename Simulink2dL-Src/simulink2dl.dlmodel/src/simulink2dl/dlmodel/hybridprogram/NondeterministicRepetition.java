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
 * This class represents a nondeterministic repetition.
 * 
 * @author Timm Liebrenz
 *
 */
public class NondeterministicRepetition implements HybridProgram {

	private HybridProgram innerProgram;

	private Formula invariant;

	/**
	 * Constructor for a nondeterministic choice without an invariant.
	 * 
	 * @param innerProgram
	 */
	public NondeterministicRepetition(HybridProgram innerProgram) {
		this.innerProgram = innerProgram;
		this.invariant = null;
	}

	/**
	 * Constructor for a nondeterministic choice with an invariant.
	 * 
	 * @param innerProgram
	 * @param invariant
	 */
	public NondeterministicRepetition(HybridProgram innerProgram, Formula invariant) {
		this.innerProgram = innerProgram;
		this.invariant = invariant;
	}

	public HybridProgram getInnerProgram() {
		return innerProgram;
	}

	@Override
	public String toString() {
		String repetition = "{" + innerProgram.toString() + "}*";
		if (invariant != null) {
			repetition += "@" + invariant.toString();
		}
		return repetition;
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String repetition = "{\n" + indent + "  "
				+ innerProgram.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains) + "\n"
				+ indent + "}*";
		if (invariant != null) {
			repetition += "@"
					+ invariant.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains);
		}
		return repetition;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		innerProgram.replaceTermRecursive(toReplace, replaceWith);
		invariant.replaceTermRecursive(toReplace, replaceWith);
	}

	@Override
	public boolean containsTerm(Term term) {
		if (innerProgram.containsTerm(term)) {
			return true;
		}
		return invariant.containsTerm(term);
	}

	@Override
	public NondeterministicRepetition createDeepCopy() {
		return new NondeterministicRepetition(innerProgram.createDeepCopy(), invariant.createDeepCopy());
	}

	/**
	 * @return the invariant
	 */
	public Formula getInvariant() {
		return invariant;
	}

	/**
	 * @param invariant the invariant to set
	 */
	public void setInvariant(Formula invariant) {
		this.invariant = invariant;
	}

	@Override
	public HybridProgram expand() {
		innerProgram.expand();
		return this;
	}

}
