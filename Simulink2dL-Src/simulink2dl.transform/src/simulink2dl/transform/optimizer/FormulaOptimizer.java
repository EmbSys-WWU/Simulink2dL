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
package simulink2dl.transform.optimizer;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;

/**
 * This optimizer updates conjunctions and disjunctions. For conjunctions, it
 * removes duplicated elements and "true" elements, and sets the whole
 * conjunction to false if it contains a contradiction or a "false" element. For
 * disjunctions, it removes duplicated elements and "false" elements, and sets
 * the whole disjunction ot true if it contains a tautology or a "true" element.
 * 
 * @author Timm Liebrenz
 *
 */
public class FormulaOptimizer extends Optimizer {

	protected void handleConjunctionFormula(Conjunction conjunction) {
		List<Operator> newElements = new LinkedList<Operator>();
		boolean replace = false;

		Formula trueElement = new BooleanConstant(true);
		Formula falseElement = new BooleanConstant(false);

		for (Operator toCheckOperator : conjunction.getElements()) {
			Formula toCheck = (Formula) toCheckOperator;
			if (toCheck.equals(falseElement)) {
				// if one element of the conjunction is false, the whole
				// conjunction is false
				newElements.clear();
				newElements.add(falseElement);
				replace = true;
				break;
			}
			if (toCheck.equals(trueElement)) {
				// remove true elements, i.e. do not add it to new element list
				replace = true;
				continue;
			}
			if (newElements.contains(toCheck.createNegation())) {
				// the conjunction contains a contradiction, therefore the whole
				// conjunction is false
				newElements.clear();
				newElements.add(falseElement);
				replace = true;
				break;
			}
			if (newElements.contains(toCheck)) {
				// found a duplicate, i.e. do not add it to new element list
				replace = true;
			} else {
				// element is not yet in list, so add it
				newElements.add(toCheck);
			}
		}

		if (replace) {
			if (newElements.isEmpty()) {
				// if no elements are remaining, set the conjunction to true
				newElements.add(trueElement);
			}
			conjunction.setElements(newElements);
		}
	}

	protected void handleDisjunctionFormula(Disjunction disjunction) {
		List<Operator> newElements = new LinkedList<Operator>();
		boolean replace = false;

		Formula trueElement = new BooleanConstant(true);
		Formula falseElement = new BooleanConstant(false);

		for (Operator toCheckOperator : disjunction.getElements()) {
			Formula toCheck = (Formula) toCheckOperator;

			if (toCheck.equals(trueElement)) {
				// if one element of the disjunction is true, the whole
				// conjunction is true
				newElements.clear();
				newElements.add(trueElement);
				replace = true;
				break;
			}
			if (toCheck.equals(falseElement)) {
				// remove false elements, i.e. do not add it to new element list
				replace = true;
				continue;
			}
			if (newElements.contains(toCheck.createNegation())) {
				// the conjunction contains a tautology, therefore the whole
				// conjunction is true
				newElements.clear();
				newElements.add(trueElement);
				replace = true;
				break;
			}
			if (newElements.contains(toCheck)) {
				// found a duplicate, i.e. do not add it to new element list
				replace = true;
			} else {
				// element is not yet in list, so add it
				newElements.add(toCheck);
			}
		}

		if (replace) {
			if (newElements.isEmpty()) {
				// if no elements are remaining, set the disjunction to false
				newElements.add(falseElement);
			}
			disjunction.setElements(newElements);
		}
	}

}
