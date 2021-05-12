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
package simulink2dl.dlmodel.term;

/**
 * @author nick
 *
 */
public class BracketTerm implements Term {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((innerTerm == null) ? 0 : innerTerm.hashCode());
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
		BracketTerm other = (BracketTerm) obj;
		if (innerTerm == null) {
			if (other.innerTerm != null)
				return false;
		} else if (!innerTerm.equals(other.innerTerm))
			return false;
		return true;
	}

	private Term innerTerm;

	/**
	 * 
	 */
	public BracketTerm(Term term) {
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
		return "(" + this.innerTerm.toString() + ")";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (innerTerm.equals(toReplace)) {
			innerTerm = replaceWith;
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
	public BracketTerm createDeepCopy() {
		return new BracketTerm(innerTerm.createDeepCopy());
	}

}
