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

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a box modality "[*]"
 * 
 * @author Timm Liebrenz
 *
 */
public class BoxModality implements Operator {

	private HybridProgram innerProgram;

	private Operator postCondition;

	/**
	 * Constructor for a box modality.
	 * 
	 * @param innerProgram
	 * @param postCondition
	 */
	public BoxModality(HybridProgram innerProgram, Operator postCondition) {
		this.innerProgram = innerProgram;
		this.postCondition = postCondition;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String toString() {
		return "[" + innerProgram.toString() + "]" + postCondition.toString();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return "[\n" + indent + "  "
				+ innerProgram.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains) + "\n"
				+ indent + "] "
				+ postCondition.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains);
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (innerProgram.equals(toReplace)) {
			innerProgram = (HybridProgram) replaceWith;
		} else {
			innerProgram.replaceTermRecursive(toReplace, replaceWith);
		}
		if (postCondition.equals(toReplace)) {
			postCondition = (Operator) replaceWith;
		} else {
			postCondition.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (innerProgram.equals(term)) {
			return true;
		}
		if (postCondition.equals(term)) {
			return true;
		}
		if (innerProgram.containsTerm(term)) {
			return true;
		}
		if (postCondition.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public BoxModality createDeepCopy() {
		return new BoxModality(innerProgram.createDeepCopy(), postCondition.createDeepCopy());
	}

}
