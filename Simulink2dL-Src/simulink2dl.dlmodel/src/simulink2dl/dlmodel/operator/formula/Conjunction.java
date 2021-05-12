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

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a logical conjunction.
 * 
 * @author Timm Liebrenz
 *
 */
public class Conjunction implements Formula {

	/**
	 * List that contains all elements of this conjunction
	 */
	private List<Operator> elements = new LinkedList<>();

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
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
		Conjunction other = (Conjunction) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

	/**
	 * Constructor for a conjunction with an arbitrary number of elements.
	 * 
	 * @param newElements
	 */
	public Conjunction(Operator... newElements) {
		for (Operator element : newElements) {
			if (element instanceof Conjunction) {
				this.elements.addAll(((Conjunction) element).elements);
			} else {
				this.elements.add(element);
			}
		}
	}

	/**
	 * Adds a new element to this conjunction. If the element is a conjunction, its
	 * elements are added to the list of this conjunction.
	 */
	public void addElement(Operator newElement) {
		if (newElement instanceof Conjunction) {
			this.elements.addAll(((Conjunction) newElement).elements);
		} else {
			this.elements.add(newElement);
		}
	}

	@Override
	public String toString() {
		String result = "";
		boolean isFirst = true;
		for (Operator element : elements) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += " & ";
			}
			result += "(" + element.toString() + ")";
		}

		return result;
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String result = "";
		boolean isFirst = true;
		for (Operator element : elements) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += "\n" + indent + "& ";
			}
			result += "(" + element.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains)
					+ ")";
		}

		return result;
	}

	public Conjunction addLiterals(Operator... operator) {
		for (Operator formula : operator) {
			this.elements.add(formula);
		}
		return this;
	}

	/**
	 * @return the elements
	 */
	public List<Operator> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<Operator> elements) {
		this.elements = elements;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (int i = 0; i < elements.size(); i++) {
			Operator element = elements.get(i);
			if (element.equals(toReplace)) {
				elements.set(i, (Operator) replaceWith);
			} else {
				element.replaceTermRecursive(toReplace, replaceWith);
			}
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (Operator element : elements) {
			if (element.equals(term)) {
				return true;
			}
			if (element.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Formula createDeepCopy() {
		Conjunction result = new Conjunction();

		for (Operator element : elements) {
			result.addElement(element.createDeepCopy());
		}

		return result;
	}

	@Override
	public Disjunction createNegation() {
		Disjunction result = new Disjunction();
		for (Operator element : elements) {
			result.addElement(((Formula) element).createNegation());
		}
		return result;
	}
	
	@Override
	public Formula expand() {
		for (int i = 0; i < elements.size(); i++) {
			Operator element = elements.get(i);
			elements.set(i, element.expand());
		}
		return this;
	}

}
