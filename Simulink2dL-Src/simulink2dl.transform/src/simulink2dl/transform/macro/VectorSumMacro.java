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
package simulink2dl.transform.macro;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public class VectorSumMacro extends Macro {

	private ReplaceableTerm toReplace;

	// the replace with of this macro is sum of the elements of the input vector
	private boolean positiveSum;
	private ReplaceableTerm input;

	public VectorSumMacro(ReplaceableTerm toReplace, ReplaceableTerm input, boolean positiveSum) {
		this.toReplace = toReplace;
		this.input = input;
		this.positiveSum = positiveSum;
	}

	@Override
	public ReplaceableTerm getToReplace() {
		return toReplace;
	}

	@Override
	public Term getReplaceWith() {
		PluginLogger.error("getReplaceWith() should not be called for VectorSumMacro");
		return null;
	}

	@Override
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		// application of simple macro to vector sum means that the sum has only
		// one element
		if (positiveSum) {
			resultList.add(new SimpleMacro(toReplace.createDeepCopy(), other.getReplaceWith().createDeepCopy()));
		} else {
			AdditionTerm replaceWith = new AdditionTerm();
			replaceWith.subtract(other.getReplaceWith().createDeepCopy());

			resultList.add(new SimpleMacro(toReplace, replaceWith));
		}

		return resultList;
	}

	@Override
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		AdditionTerm replaceWith = new AdditionTerm();
		for (Term replaceWithElement : other.getReplaceWithVector()) {
			if (positiveSum) {
				replaceWith.add(replaceWithElement);
			} else {
				replaceWith.subtract(replaceWithElement);
			}
		}

		resultList.add(new SimpleMacro(toReplace, replaceWith));
		return resultList;
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		// do nothing
	}

	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection behavior) {
		// do nothing
	}
	
	@Override
	public void applyToInitialConditions(Conjunction initialConditions) {
		// do nothing	
	}


	@Override
	public boolean containsTerm(Term toReplace) {
		return input.equals(toReplace);
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {
		String result = "\"" + toReplace.toString() + "\": " + (!positiveSum ? "-" : "") + "sum(" + input.toString()
				+ ")";
		return result;
	}

	@Override
	public VectorSumMacro createDeepCopy() {
		VectorSumMacro result = new VectorSumMacro(toReplace.createDeepCopy(), input.createDeepCopy(), positiveSum);
		return result;
	}
}
