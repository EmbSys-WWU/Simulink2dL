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
public class AdditionTerm implements Term {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subtrahends == null) ? 0 : subtrahends.hashCode());
		result = prime * result + ((summands == null) ? 0 : summands.hashCode());
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
		AdditionTerm other = (AdditionTerm) obj;
		if (subtrahends == null) {
			if (other.subtrahends != null)
				return false;
		} else if (!subtrahends.equals(other.subtrahends))
			return false;
		if (summands == null) {
			if (other.summands != null)
				return false;
		} else if (!summands.equals(other.summands))
			return false;
		return true;
	}

	private static final Term neutralElement = new RealTerm(0.0);

	private List<Term> summands = new LinkedList<Term>();
	private List<Term> subtrahends = new LinkedList<Term>();

	/**
	 * 
	 */
	public AdditionTerm(Term... summands) {
		for (Term term : summands) {
			this.summands.add(term);
		}
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	public AdditionTerm add(Term... additions) {
		for (Term term : additions) {
			this.summands.add(term);
		}
		return this;
	}

	public AdditionTerm subtract(Term... subtrahends) {
		for (Term term : subtrahends) {
			this.subtrahends.add(term);
		}
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		String prefix = "";
		for (Term fac : this.summands) {
			sb.append(prefix);
			prefix = "+";
			sb.append(fac.toString());
		}

		prefix = "-";
		for (Term fac : this.subtrahends) {
			sb.append(prefix);
			sb.append(fac.toString());
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * @return the summands
	 */
	public List<Term> getSummands() {
		return summands;
	}

	/**
	 * @param summands the summands to set
	 */
	public void setSummands(List<Term> summands) {
		this.summands = summands;
	}

	/**
	 * @return the subtrahends
	 */
	public List<Term> getSubtrahends() {
		return subtrahends;
	}

	/**
	 * @param subtrahends the subtrahends to set
	 */
	public void setSubtrahends(List<Term> subtrahends) {
		this.subtrahends = subtrahends;
	}

	/**
	 * @return the neutralelement
	 */
	public static Term getNeutralelement() {
		return neutralElement;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (int i = 0; i < summands.size(); i++) {
			Term summand = summands.get(i);
			if (summand.equals(toReplace)) {
				summands.set(i, replaceWith);
			} else {
				summand.replaceTermRecursive(toReplace, replaceWith);
			}
		}
		for (int i = 0; i < subtrahends.size(); i++) {
			Term subtrahend = subtrahends.get(i);
			if (subtrahend.equals(toReplace)) {
				subtrahends.set(i, replaceWith);
			} else {
				subtrahend.replaceTermRecursive(toReplace, replaceWith);
			}
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (Term summand : summands) {
			if (summand.equals(term)) {
				return true;
			}
			if (summand.containsTerm(term)) {
				return true;
			}
		}
		for (Term subtrahend : subtrahends) {
			if (subtrahend.equals(term)) {
				return true;
			}
			if (subtrahend.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AdditionTerm createDeepCopy() {
		AdditionTerm result = new AdditionTerm();

		for (Term summand : summands) {
			result.add(summand.createDeepCopy());
		}
		for (Term subtrahend : subtrahends) {
			result.subtract(subtrahend.createDeepCopy());
		}

		return result;
	}

}
