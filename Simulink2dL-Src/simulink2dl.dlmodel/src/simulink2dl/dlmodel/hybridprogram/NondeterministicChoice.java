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

import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a nondeterministic choice between hybrid programs.
 * 
 * @author Timm Liebrenz
 *
 */
public class NondeterministicChoice implements HybridProgram {

	private List<HybridProgram> choices;

	/**
	 * Constructor without adding elements.
	 */
	public NondeterministicChoice() {
		this.choices = new LinkedList<HybridProgram>();
	}

	/**
	 * Constructor that adds given elements to the nondeterministic choice.
	 * 
	 * @param newElements
	 */
	public NondeterministicChoice(HybridProgram... newElements) {
		this.choices = new LinkedList<HybridProgram>();

		for (HybridProgram element : newElements) {
			choices.add(element);
		}
	}

	public List<HybridProgram> getChoices() {
		return choices;
	}

	/**
	 * Adds the given choice.
	 * 
	 * @param choice
	 */
	public void addChoice(HybridProgram choice) {
		choices.add(choice);
	}

	@Override
	public String toString() {
		String choiceString = "{";
		boolean isFirst = true;

		for (HybridProgram choice : choices) {
			if (!isFirst) {
				choiceString += "++";
			} else {
				isFirst = false;
			}
			choiceString += choice.toString();
		}

		return choiceString + "}";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String choiceString = "{\n" + indent;
		boolean isFirst = true;

		for (HybridProgram choice : choices) {
			if (!isFirst) {
				choiceString += "++\n" + indent;
			} else {
				isFirst = false;
			}
			choiceString += choice.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains)
					+ "\n" + indent;
		}

		return choiceString + indent + "}";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (HybridProgram choice : choices) {
			choice.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (HybridProgram choice : choices) {
			if (choice.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public NondeterministicChoice createDeepCopy() {
		NondeterministicChoice result = new NondeterministicChoice();

		for (HybridProgram choice : choices) {
			result.addChoice(choice.createDeepCopy());
		}

		return result;
	}

	@Override
	public HybridProgram expand() {
		for (int i = 0; i< choices.size(); i++) {
			HybridProgram choice = choices.get(i);
			choice = choice.expand();
			choices.set(i, choice);
		}
		return this;
	}

}
