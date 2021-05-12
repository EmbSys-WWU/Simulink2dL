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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public class VectorMacro extends Macro {

	private ReplaceableTerm toReplace;

	private VectorTerm replaceWithVector;
	
	public VectorMacro(ReplaceableTerm toReplace, Term... replaceWith) {
		this.toReplace = toReplace;

		replaceWithVector = new VectorTerm();
		for (Term newReplaceWith : replaceWith) {
			replaceWithVector.add(newReplaceWith);
		}
	}
	
	public VectorMacro(ReplaceableTerm toReplace, VectorTerm replaceWith) {
		this.toReplace = toReplace;

		replaceWithVector = replaceWith;
	}

	public void addElement(Term replaceWith) {
		this.replaceWithVector.add(replaceWith);
	}

	@Override
	public ReplaceableTerm getToReplace() {
		return toReplace;
	}

	@Override
	public Term getReplaceWith() {
		PluginLogger.error("getReplaceWith() should not be called for VectorMacro.");
		return replaceWithVector;
	}

	public VectorTerm getReplaceWithVector() {
		return replaceWithVector;
	}

	@Override
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		for (int i = 0; i < replaceWithVector.size(); i++) {
			Term replaceWith = replaceWithVector.get(i);
			if (replaceWith.equals(other.getToReplace())) {
				replaceWithVector.set(i, other.getReplaceWith());
			} else if (replaceWith.containsTerm(other.getToReplace())) {
				replaceWith.replaceTermRecursive(other.getToReplace(), other.getReplaceWith());
			}
		}

		resultList.add(this);
		return resultList;
	}

	@Override
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		
		this.replaceWithVector.replaceTermRecursive(other.toReplace, other.getReplaceWithVector());

		VectorMacro result = new VectorMacro(toReplace, this.replaceWithVector);
		
		List<Macro> resultList = new LinkedList<Macro>();
		resultList.add(result);
		return resultList;
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		continuousBehavior.applyVectorMacro(this);
	}
	
	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection hybridProgram) {
		hybridProgram.replaceTermRecursive(toReplace, replaceWithVector);
		
	}

	@Override
	public boolean containsTerm(Term term) {
		for (Term replaceWith : replaceWithVector) {
			if (replaceWith.equals(term)) {
				return true;
			}
			if (replaceWith.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * For debugging purposes.
	 */
	@Override
	public String toString() {
		String result = "\"" + toReplace.toString() + "\":\n  {";
		for (Term term : replaceWithVector) {
			result += "  " + term.toString() + ", ";
		}
		return result + "}";
	}

	@Override
	public VectorMacro createDeepCopy() {
		VectorMacro result = new VectorMacro(toReplace.createDeepCopy());

		for (Term oldReplaceWith : replaceWithVector) {
			Term newReplaceWith = oldReplaceWith.createDeepCopy();

			result.addElement(newReplaceWith);
		}

		return result;
	}

	@Override
	public void applyToInitialConditions(Conjunction additionalInitialConditions) {
		additionalInitialConditions.replaceTermRecursive(toReplace, replaceWithVector);
	}
}
