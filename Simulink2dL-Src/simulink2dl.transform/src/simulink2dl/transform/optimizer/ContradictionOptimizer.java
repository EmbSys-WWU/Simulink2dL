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

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;

// Optimizations
// 1. remove choices that have a contradiction in their conditions
public class ContradictionOptimizer extends Optimizer {

	@Override
	protected void handleConditionalChoice(ConditionalChoice condChoice) {
		// perform the optimization
		List<ConditionalHybridProgram> toCheckList = new LinkedList<ConditionalHybridProgram>();
		List<ConditionalHybridProgram> checkedList = new LinkedList<ConditionalHybridProgram>();
		boolean replace = false;

		toCheckList.addAll(condChoice.getChoices());

		for (ConditionalHybridProgram conditionalProgram : toCheckList) {
			Formula condition = conditionalProgram.getCondition();
			if (!isFalse(condition)) {
				checkedList.add(conditionalProgram);
			} else {
				// at least one change, replace is necessary
				replace = true;
			}
		}

		// replace the original program by the new one
		if (replace) {
			if (checkedList.isEmpty()) {
				// no valid choices, therefore add test for false
				Formula newCondition = new BooleanConstant(false);
				HybridProgram newProgram = new HybridProgramCollection();
				checkedList.add(new ConditionalHybridProgram(newCondition, newProgram));
			} else {
				condChoice.setChoices(checkedList);
			}
		}
	}

	private boolean isFalse(Formula condition) {
		Formula falseElement = new BooleanConstant(false);
		if (condition.equals(falseElement)) {
			return true;
		}

		if (condition instanceof Conjunction) {
			Conjunction conjunction = (Conjunction) condition;

			for (Operator currentFormula : conjunction.getElements()) {
				// if the negation of this formula is also in this conjunction,
				// it contains a contradiction
				if (currentFormula.equals(falseElement)) {
					return true;
				}
			}
		}

		if (condition instanceof Disjunction) {
			Disjunction disjunction = (Disjunction) condition;

			List<Operator> toCheck = new LinkedList<Operator>();
			toCheck.addAll(disjunction.getElements());
			while (!toCheck.isEmpty()) {
				Operator currentFormula = toCheck.remove(0);

				// if the negation of this formula is also in this conjunction,
				// it
				// contains a contradiction
				if (currentFormula.equals(falseElement)) {
					return true;
				}
			}
		}

		return false;
	}

}
