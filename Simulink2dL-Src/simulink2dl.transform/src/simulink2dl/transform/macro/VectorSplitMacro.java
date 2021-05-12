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
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public class VectorSplitMacro extends Macro {

	private List<ReplaceableTerm> toReplaceList;

	private ReplaceableTerm replaceWith;

	public VectorSplitMacro(List<ReplaceableTerm> toReplaceList, ReplaceableTerm replaceWith) {
		this.toReplaceList = toReplaceList;
		this.replaceWith = replaceWith;
	}

	@Override
	public ReplaceableTerm getToReplace() {
		PluginLogger.error("getToReplace() should not be called for VectorSplitMacro");
		return new PortIdentifier("none");
	}
	
	public List<ReplaceableTerm> getToReplaceList() {
		return toReplaceList;
	}

	@Override
	public Term getReplaceWith() {
		return replaceWith;
	}

	@Override
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		// only possible if this split macro has exactly one output
		if (toReplaceList.size() > 1) {
			PluginLogger.error("Cannot apply a simple macro to a vector macro with multiple outputs.");
			resultList.add(this); //Macro should not be deleted on error
		}else {
			resultList.add(new SimpleMacro(toReplaceList.get(0), other.getReplaceWith().createDeepCopy()));
		}
		// create a new simple macro that represents the feed through of the
		// single value
		return resultList;
	}

	@Override
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		List<Term> replaceWithList = other.getReplaceWithVector();

		if (toReplaceList.size() > replaceWithList.size()) {
			PluginLogger.error("Vector to apply to a split macro is smaller than the number of output elements.");
			return null;
		}

		// calculate mod and div for the correct mapping
		int mod = replaceWithList.size();
		int div = 0;
		int n = toReplaceList.size();

		while (mod >= n) {
			div++;
			mod -= n;
		}

		// first mod elements have div+1 number of signals
		int outerIndex = 0;
		for (/* outerIndex = 0 */; outerIndex < mod; outerIndex++) {
			// this loop always produces vector macros
			VectorMacro newMacro = new VectorMacro(toReplaceList.get(outerIndex));

			for (int innerIndex = 0; innerIndex < div + 1; innerIndex++) {
				newMacro.addElement(replaceWithList.get(outerIndex * (div + 1) + innerIndex).createDeepCopy());
			}

			resultList.add(newMacro);
		}
		// the remaining elements have div number of signals
		for (/* outerIndex = mod */; outerIndex < n; outerIndex++) {
			// this loop can either produce vector macros if div > 1
			// or simple macros for div == 1
			if (div > 1) {
				// create a new vector macro for div > 1
				VectorMacro newMacro = new VectorMacro(toReplaceList.get(outerIndex));

				for (int innerIndex = 0; innerIndex < div; innerIndex++) {
					newMacro.addElement(replaceWithList.get(mod + outerIndex * (div) + innerIndex).createDeepCopy());
				}

				resultList.add(newMacro);
			} else {
				// otherwise the result is a simple macro
				SimpleMacro newMacro = new SimpleMacro(toReplaceList.get(outerIndex),
						replaceWithList.get(mod + outerIndex).createDeepCopy());

				resultList.add(newMacro);
			}
		}
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
		return replaceWith.equals(toReplace);
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {

		String result = "";
		boolean isFirst = true;
		for (Term toReplace : toReplaceList) {
			if (isFirst) {
				isFirst = false;
			} else {
				result += ", ";
			}
			result += "\"" + toReplace.toString() + "\"";
		}
		result += ": " + replaceWith.toString();
		return result;
	}

	@Override
	public VectorSplitMacro createDeepCopy() {
		List<ReplaceableTerm> newToReplaceList = new LinkedList<ReplaceableTerm>();

		for (ReplaceableTerm oldToReplace : toReplaceList) {
			newToReplaceList.add(oldToReplace.createDeepCopy());
		}

		VectorSplitMacro result = new VectorSplitMacro(newToReplaceList, replaceWith.createDeepCopy());
		return result;
	}
}
