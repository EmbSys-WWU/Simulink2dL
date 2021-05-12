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

import java.util.LinkedList;
import java.util.List;

/**
 * @author nick
 *
 */
public class MultiplicationTerm implements Term {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((divisors == null) ? 0 : divisors.hashCode());
		result = prime * result + ((factors == null) ? 0 : factors.hashCode());
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
		MultiplicationTerm other = (MultiplicationTerm) obj;
		if (divisors == null) {
			if (other.divisors != null)
				return false;
		} else if (!divisors.equals(other.divisors))
			return false;
		if (factors == null) {
			if (other.factors != null)
				return false;
		} else if (!factors.equals(other.factors))
			return false;
		return true;
	}

	private static final Term neutralElement = new RealTerm(1.0);

	private List<Term> factors = new LinkedList<Term>();
	private List<Term> divisors = new LinkedList<Term>();

	/**
	 * 
	 */
	public MultiplicationTerm(Term... factors) {
		// if (factors.length == 0) {
		// this.factors.add(neutralElement);
		// } else {
		for (Term term : factors) {
			this.factors.add(term);
		}
		// }
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	public MultiplicationTerm multiplyBy(Term... factors) {
		// remove redundancy
		// if (this.factors.size() == 1 &&
		// this.factors.get(0).equals(neutralElement)) {
		// this.factors = new LinkedList<Term>();
		// }

		for (Term term : factors) {
			this.factors.add(term);
		}
		return this;
	}

	public MultiplicationTerm dividedBy(Term... divisors) {
		if (divisors.length == 0) {
			this.divisors.add(neutralElement);
		} else {
			for (Term term : divisors) {
				this.divisors.add(term);
			}
		}
		return this;
	}

	public String toString() {
		if (this.divisors.isEmpty() || (this.divisors.size() == 1 && this.divisors.get(0).equals(neutralElement))) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			sb.append(this.atorString(true));
			sb.append(")");
			return sb.toString();
		}

		boolean enclose = this.factors.size() != 1;
		StringBuilder strb = new StringBuilder();
		if (enclose) {
			strb.append("(").append(this.atorString(true)).append(")");
		} else {
			strb.append(this.atorString(true));
		}

		strb.append("/");

		enclose = this.divisors.size() != 1;
		if (enclose) {
			strb.append("(").append(this.atorString(false)).append(")");
		} else {
			strb.append(this.atorString(false));
		}

		return strb.toString();
	}

	/**
	 * Generates a String representation of the numerator (if called with true) or
	 * denominator (if called with false) of this multiplication.
	 * 
	 * @param factor
	 * @return
	 */
	private String atorString(Boolean numerator) {
		List<Term> list = numerator ? this.factors : this.divisors;
		if (list.size() == 0) {
			return "1";
		}

		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (Term fac : list) {
			sb.append(prefix);
			prefix = "*";
			sb.append(fac.toString());
		}
		return sb.toString();
	}

	/**
	 * @return the factors
	 */
	public List<Term> getFactors() {
		return factors;
	}

	/**
	 * @param factors the factors to set
	 */
	public void setFactors(List<Term> factors) {
		this.factors = factors;
	}

	/**
	 * @return the divisors
	 */
	public List<Term> getDivisors() {
		return divisors;
	}

	/**
	 * @param divisors the divisors to set
	 */
	public void setDivisors(List<Term> divisors) {
		this.divisors = divisors;
	}

	/**
	 * @return the neutralelement
	 */
	public static Term getNeutralelement() {
		return neutralElement;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (int i = 0; i < factors.size(); i++) {
			Term factor = factors.get(i);
			if (factor.equals(toReplace)) {
				factors.set(i, replaceWith);
			} else {
				factor.replaceTermRecursive(toReplace, replaceWith);
			}
		}
		for (int i = 0; i < divisors.size(); i++) {
			Term divisor = divisors.get(i);
			if (divisor.equals(toReplace)) {
				divisors.set(i, replaceWith);
			} else {
				divisor.replaceTermRecursive(toReplace, replaceWith);
			}
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (Term factor : factors) {
			if (factor.equals(term)) {
				return true;
			}
			if (factor.containsTerm(term)) {
				return true;
			}
		}
		for (Term divisor : divisors) {
			if (divisor.equals(term)) {
				return true;
			}
			if (divisor.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MultiplicationTerm createDeepCopy() {
		MultiplicationTerm result = new MultiplicationTerm();

		for (Term factor : factors) {
			result.multiplyBy(factor.createDeepCopy());
		}
		for (Term divisor : divisors) {
			result.dividedBy(divisor.createDeepCopy());
		}

		return result;
	}

}
