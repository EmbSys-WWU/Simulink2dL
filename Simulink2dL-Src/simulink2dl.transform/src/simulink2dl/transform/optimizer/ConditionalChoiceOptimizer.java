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
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;

// Optimizations
// 1. move conditions of outer choices into conditions of inner choices
// 2. remove duplicates in conditions
// 3. remove duplicates in evolution domains

public class ConditionalChoiceOptimizer extends Optimizer {

	private boolean combineConditionalChoices = true;

	public ConditionalChoiceOptimizer() {
		// empty
	}

	@Override
	protected void handleConditionalChoice(ConditionalChoice condChoice) {
		if (combineConditionalChoices) {
			// perform the optimization
			optimizeConditionalChoices(condChoice);
		}
	}

	/**
	 * Combines conditional choices with inner conditional choices. Only applicable
	 * if the inner program of a conditional choice is also a conditional choice.
	 * 
	 * @param program
	 */
	private void optimizeConditionalChoices(ConditionalChoice program) {
		List<ConditionalHybridProgram> toCheckList = new LinkedList<ConditionalHybridProgram>();
		List<ConditionalHybridProgram> checkedList = new LinkedList<ConditionalHybridProgram>();
		boolean replace = false;

		toCheckList.addAll(program.getChoices());

		while (!toCheckList.isEmpty()) {
			// pop the first
			ConditionalHybridProgram conditionalProgram = toCheckList.remove(0);

			// only applicable if inner program is also a conditional choice
			HybridProgram innerProgram = conditionalProgram.getInnerProgram();
			if (!(innerProgram instanceof ConditionalChoice)) {
				// add this conditional program to the checked list
				checkedList.add(conditionalProgram);
				continue;
			}
			// at least one change, replace is necessary
			replace = true;

			ConditionalChoice innerChoice = (ConditionalChoice) innerProgram;
			List<ConditionalHybridProgram> toIntegrateList = innerChoice.getChoices();

			// add condition to conditions of all inner container
			for (int i = 0; i < toIntegrateList.size(); i++) {
				ConditionalHybridProgram toIntegrate = toIntegrateList.get(i);
				Conjunction newCondition = new Conjunction();

				// create new condition
				// add outer condition
				newCondition.addElement(conditionalProgram.getCondition().createDeepCopy());
				// add inner condition
				newCondition.addElement(toIntegrate.getCondition().createDeepCopy());

				// add inner program
				HybridProgram newProgram = toIntegrate.getInnerProgram().createDeepCopy();

				// add the new program to the check list
				toCheckList.add(i, new ConditionalHybridProgram(newCondition, newProgram));
			}
		}

		// replace the original program by the new one
		if (replace) {
			program.setChoices(checkedList);
		}
	}

}
