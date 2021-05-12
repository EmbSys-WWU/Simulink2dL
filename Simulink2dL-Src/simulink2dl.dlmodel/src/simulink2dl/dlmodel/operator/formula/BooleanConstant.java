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

import simulink2dl.dlmodel.term.Term;

/**
 * This class represents constant operators "true" and "false".
 * 
 * @author Timm Liebrenz
 *
 */
public class BooleanConstant implements Formula {

	private boolean isTrue;

	public BooleanConstant(boolean isTrue) {
		this.isTrue = isTrue;
	}

	@Override
	public String toString() {
		return this.isTrue ? "true" : "false";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return isTrue ? "true" : "false";
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		// do nothing
	}

	@Override
	public boolean containsTerm(Term term) {
		return false;
	}

	@Override
	public Formula createDeepCopy() {
		return new BooleanConstant(isTrue);
	}

	@Override
	public Formula createNegation() {
		return new BooleanConstant(!isTrue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isTrue ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanConstant other = (BooleanConstant) obj;
		if (isTrue != other.isTrue)
			return false;
		return true;
	}

	/**
	 * @return the isTrue
	 */
	public boolean isTrue() {
		return isTrue;
	}

	/**
	 * @param isTrue the isTrue to set
	 */
	public void setTrue(boolean isTrue) {
		this.isTrue = isTrue;
	}

}
