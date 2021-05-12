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

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a collection of other hybrid programs.
 * 
 * @author Timm Liebrenz
 *
 */
public class HybridProgramCollection implements HybridProgram {

	private List<HybridProgram> sequence;

	/**
	 * Constructor without adding elements.
	 */
	public HybridProgramCollection() {
		this.sequence = new LinkedList<HybridProgram>();
	}

	/**
	 * Constructor that adds given elements to the collection.
	 * 
	 * @param newElements
	 */
	public HybridProgramCollection(HybridProgram... newElements) {
		this.sequence = new LinkedList<HybridProgram>();

		for (HybridProgram element : newElements) {
			sequence.add(element);
		}
	}

	public List<HybridProgram> getInnerPrograms() {
		return sequence;
	}
	
	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	/**
	 * Adds the given element to the collection.
	 * 
	 * @param element
	 */
	public void addElement(HybridProgram element) {
		sequence.add(element);
	}

	/**
	 * Adds the given element to at the front of the collection list.
	 * 
	 * @param element
	 */
	public void addElementFront(HybridProgram element) {
		sequence.add(0, element);
	}

	@Override
	public String toString() {
		String sequenceString = "{";

		for (HybridProgram element : sequence) {
			sequenceString += element.toString();
		}

		return sequenceString + "}";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String sequenceString = "";
		boolean isFirst = true;

		for (HybridProgram element : sequence) {
			if (isFirst) {
				isFirst = false;
			} else {
				sequenceString += "\n" + indent;
			}
			sequenceString += element.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains);
		}

		return sequenceString;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (HybridProgram element : sequence) {
			element.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (HybridProgram element : sequence) {
			if (element.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public HybridProgramCollection createDeepCopy() {
		HybridProgramCollection result = new HybridProgramCollection();

		for (HybridProgram innerProgram : sequence) {
			result.addElement(innerProgram.createDeepCopy());
		}

		return result;
	}

	@Override
	public HybridProgram expand() {
		for (int i = 0; i<sequence.size(); i++) {
			HybridProgram element = sequence.get(i);
			element = element.expand();
			sequence.set(i, element);
		}
		return this;
	}

}
