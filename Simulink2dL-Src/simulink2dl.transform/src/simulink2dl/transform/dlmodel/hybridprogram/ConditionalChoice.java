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
package simulink2dl.transform.dlmodel.hybridprogram;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * A conditional choice is a construct to provide the behavior of a conditional
 * choice as hybrid program. It uses a nondeterministic choice and the first
 * element of each choice is a test formula, which represents the condition of
 * this branch.
 * 
 * @author Timm Liebrenz
 *
 */
public class ConditionalChoice implements HybridProgram {

	private List<ConditionalHybridProgram> choices;

	/**
	 * Constructor without adding elements.
	 */
	public ConditionalChoice() {
		this.choices = new LinkedList<ConditionalHybridProgram>();
	}

	/**
	 * Constructor that adds given elements to the conditional choice.
	 * 
	 * @param newElements
	 */
	public ConditionalChoice(ConditionalHybridProgram... newElements) {
		this.choices = new LinkedList<ConditionalHybridProgram>();

		for (ConditionalHybridProgram element : newElements) {
			choices.add(element);
		}
	}

	public List<ConditionalHybridProgram> getChoices() {
		return this.choices;
	}

	/**
	 * Adds the given choice.
	 * 
	 * @param choice
	 */
	public void addChoice(ConditionalHybridProgram choice) {
		choices.add(choice);
	}

	/**
	 * Adds the given choice.
	 * 
	 * @param condition
	 * @param choice
	 */
	public void addChoice(Formula condition, HybridProgram choice) {
		choices.add(new ConditionalHybridProgram(condition, choice));
	}

	@Override
	public String toString() {
		String choiceString = "{";
		boolean isFirst = true;

		for (ConditionalHybridProgram choice : choices) {
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
		String choiceString = "{\n" + indent + "  ";
		boolean isFirst = true;

		for (ConditionalHybridProgram choice : choices) {
			if (!isFirst) {
				choiceString += "\n" + indent + "++\n" + indent + "  ";
			} else {
				isFirst = false;
			}
			choiceString += choice.toStringFormatted(indent + "  ", multiLineTestFormulas, multiLineEvolutionDomains);
		}

		return choiceString + "\n" + indent + "}";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (ConditionalHybridProgram choice : choices) {
			choice.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		for (ConditionalHybridProgram choice : choices) {
			if (choice.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ConditionalChoice createDeepCopy() {
		ConditionalChoice result = new ConditionalChoice();

		for (ConditionalHybridProgram choice : choices) {
			result.addChoice(choice.createDeepCopy());
		}

		return result;
	}

	public void setChoices(List<ConditionalHybridProgram> newChoices) {
		this.choices = newChoices;
	}
	

	@Override
	public HybridProgram expand() {
		for(int i = 0; i<choices.size();i++) {
			choices.set(i, (ConditionalHybridProgram) choices.get(i).expand());
		}
		return this;
	}

}
