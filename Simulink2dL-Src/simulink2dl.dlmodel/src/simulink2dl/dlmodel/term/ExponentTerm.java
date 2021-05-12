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
 * A representation of exponentiation and roots.
 * 
 * @author nick
 *
 */
public class ExponentTerm implements Term {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + ((exponent == null) ? 0 : exponent.hashCode());
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
		ExponentTerm other = (ExponentTerm) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		if (exponent == null) {
			if (other.exponent != null)
				return false;
		} else if (!exponent.equals(other.exponent))
			return false;
		return true;
	}

	private Term base;
	private Term exponent;

	public ExponentTerm(Term... terms) {
		if (terms.length == 0) {
			System.out.println("[INFO] Empty ExponentTerm created!");
			return;
		}

		this.base = terms[0];
		for (int i = 1; i < terms.length; i++) {
			Term term = terms[i];
			this.powerOf(term);
		}
	}

	public ExponentTerm(Term base, Term exponent) {
		this.base = base;
		this.exponent = exponent;
	}

	public ExponentTerm powerOf(Term exp) {
		if (this.exponent == null) {
			this.exponent = exp;
			return this;
		}
		ExponentTerm newExp = new ExponentTerm(this.exponent, exp);
		this.exponent = newExp;
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(base.toString());
		sb.append("^");
		sb.append(this.exponent == null ? "null" : this.exponent.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	/**
	 * @return the base
	 */
	public Term getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(Term base) {
		this.base = base;
	}

	/**
	 * @return the exponent
	 */
	public Term getExponent() {
		return exponent;
	}

	/**
	 * @param exponent the exponent to set
	 */
	public void setExponent(Term exponent) {
		this.exponent = exponent;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (base.equals(toReplace)) {
			base = replaceWith;
		} else {
			base.replaceTermRecursive(toReplace, replaceWith);
		}
		if (exponent.equals(toReplace)) {
			exponent = replaceWith;
		} else {
			exponent.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (base.equals(term)) {
			return true;
		}
		if (exponent.equals(term)) {
			return true;
		}
		if (base.containsTerm(term)) {
			return true;
		}
		if (exponent.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public ExponentTerm createDeepCopy() {
		return new ExponentTerm(base.createDeepCopy(), exponent.createDeepCopy());
	}

}
