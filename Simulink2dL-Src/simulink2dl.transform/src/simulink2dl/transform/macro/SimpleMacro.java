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
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.transform.dlmodel.term.ExpandedTerm;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

/**
 * This class is a macro that represents a text replacement. It contains a
 * String that should be replaced by the other contained String.
 * 
 * @author Timm Liebrenz
 *
 */

public class SimpleMacro extends Macro {

	private ReplaceableTerm toReplace;

	private Term replaceWith;

	public SimpleMacro(ReplaceableTerm toReplace, Term replaceWith) {
		this.toReplace = toReplace;
		this.replaceWith = replaceWith;
	}

	@Override
	public ReplaceableTerm getToReplace() {
		return toReplace;
	}

	@Override
	public Term getReplaceWith() {
		return replaceWith;
	}

	@Override
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		if (this.replaceWith.equals(other.getToReplace())) {
			this.replaceWith = other.getReplaceWith();
		} else {
			this.replaceWith.replaceTermRecursive(other.getToReplace(), other.getReplaceWith());
		}

		resultList.add(this);
		return resultList;
	}

	@Override
	protected List<Macro> applyConditionalMacro(ConditionalMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		// create new conditional macro
		ConditionalMacro newConditionalMacro = new ConditionalMacro(this.toReplace);

		for (MacroContainer outerContainer : other.getMacroContainers()) {
			Macro toApply = outerContainer.getMacro();
			SimpleMacro newMacro = this.createDeepCopy();

			// apply old macro to new one
			List<Macro> newMacroList = newMacro.applyOtherMacro(toApply);
			if (newMacroList.size() != 1) {
				PluginLogger
						.error("Application of conditional macro to simple macro created more than one macro or none.");
			}
			newMacro = (SimpleMacro) newMacroList.get(0);

			MacroContainer newContainer = new MacroContainer(newMacro, outerContainer.getCondition().createDeepCopy(),
					null);
			newConditionalMacro.addMacroContainer(newContainer);
		}

		resultList.add(newConditionalMacro);
		return resultList;
	}

	@Override
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		VectorTerm replaceWithList;
		/* ReplaceableTerm of this Macro equals ReplaceWith of other Macro:
		 * Create new VectorMacro that replaces the ReplaceableTerm of
		 * the VectorMacro with the VectorTerm of the VectorMacro*/
		if(this.replaceWith.equals(other.getToReplace())) {
			replaceWithList = other.getReplaceWithVector().createDeepCopy();
		} else {
		/* If this Macro contains ReplaceWith of the other Macro create a copy of ReplaceWith for each VectorEntry and
		 * replace ReplaceWith in each copy with the corresponding entry*/
			replaceWithList = new ExpandedTerm(); //ExpandedTerm handles combining multiple Expanded Macros
			for (Term toApplyReplaceWith : other.getReplaceWithVector()) {
				Term newReplaceWith = this.replaceWith.createDeepCopy();
				newReplaceWith.replaceTermRecursive(other.getToReplace(),toApplyReplaceWith.createDeepCopy());
				replaceWithList.add(newReplaceWith);
			}
		}
		
		VectorMacro newVectorMacro = new VectorMacro(toReplace, replaceWithList);
		resultList.add(newVectorMacro);
		return resultList;
	}

	@Override
	public boolean containsTerm(Term toCompare) {
		if (this.replaceWith.equals(toCompare)) {
			return true;
		}
		if (this.replaceWith.containsTerm(toCompare)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return toReplace + " : " + replaceWith;
	}

	public SimpleMacro createDeepCopy() {
		return new SimpleMacro(toReplace.createDeepCopy(), replaceWith.createDeepCopy());
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		continuousBehavior.applySimpleMacro(this);
	}

	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection hybridProgram) {
		hybridProgram.replaceTermRecursive(toReplace, replaceWith);
	}

	@Override
	public void applyToInitialConditions(Conjunction initialConditions) {
		initialConditions.replaceTermRecursive(toReplace, replaceWith);
		
	}

}
