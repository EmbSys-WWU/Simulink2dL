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

import java.util.List;

import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.util.PluginLogger;

/**
 * Container class that contains a single macro and a condition when this macro
 * should be applied. This container class is a helper used as parameter to
 * specify conditional macros.
 * 
 * @author Timm Liebrenz
 *
 */
public class MacroContainer {
	private Macro macro;
	private Formula condition;
	private Formula extraCondition;

	public MacroContainer(Macro macro, Formula condition, Formula extraCondition) {
		this.macro = macro;
		this.condition = condition;
		this.extraCondition = extraCondition;
	}

	public Macro getMacro() {
		return macro;
	}

	public Formula getCondition() {
		return condition;
	}

	public Formula getExtraCondition() {
		return extraCondition;
	}

	public void applySimpleMacro(SimpleMacro other) {
		Term toReplace = other.getToReplace();
		Term replaceWith = other.getReplaceWith();
		if (condition.equals(toReplace)) {
			condition = (Formula) replaceWith;
		} else {
			condition.replaceTermRecursive(toReplace, replaceWith);
		}
		// also applying the other macro to the extraCondition if there is any
		if (extraCondition != null) {
			if (extraCondition.equals(toReplace)) {
				extraCondition = (Formula) replaceWith;
			} else {
				extraCondition.replaceTermRecursive(toReplace, replaceWith);
			}
		}

		// result should be a list with one element
		List<Macro> newMacroList = macro.applySimpleMacro(other);
		if (newMacroList.size() != 1) {
			PluginLogger
					.error("Application of simple macro to macro container created more than one macro or none.");
		}
		macro = (SimpleMacro) newMacroList.get(0);
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {
		return "(?" + condition.toString() + "): " + macro.toString();
	}

	public boolean contains(Term toCompare) {
		if (macro.containsTerm(toCompare)) {
			return true;
		}
		if (condition.equals(toCompare)) {
			return true;
		}
		if (condition.containsTerm(toCompare)) {
			return true;
		}
		return false;
	}

	public void applyVectorMacro(VectorMacro other) {
		//TODO: can VectorMacro influence condition?
		// result should be a list with one element
		List<Macro> newMacroList = macro.applyVectorMacro(other);
		//TODO: ConditionalMacros are not yet applicable to VectorSplitMacros
		if (newMacroList.size() != 1) {
			PluginLogger
					.error("Application of vector macro to macro container created more than one macro or none.");
		}
		macro = newMacroList.get(0);
	}

}

