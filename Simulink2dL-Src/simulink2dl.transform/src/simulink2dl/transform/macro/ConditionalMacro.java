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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.satisfiability.FormulaChecker;
import simulink2dl.util.satisfiability.FormulaChecker.ResultType;

/**
 * This macro creates a nondeterministic choice, where in each path a different
 * macro is applied.
 * 
 * @author Timm Liebrenz
 *
 */
public class ConditionalMacro extends Macro {

	private ReplaceableTerm toReplace;

	private List<MacroContainer> macroContainers;

	public ConditionalMacro(ReplaceableTerm toReplace, MacroContainer... macroContainers) {
		this.toReplace = toReplace;
		this.macroContainers = new LinkedList<MacroContainer>();
		for (MacroContainer newContainer : macroContainers) {
			this.macroContainers.add(newContainer);
		}
	}

	public void addMacroContainer(MacroContainer... newContainers) {
		for (MacroContainer newContainer : newContainers) {
			this.macroContainers.add(newContainer);
		}
	}

	public ReplaceableTerm getToReplace() {
		return toReplace;
	}

	@Override
	public ReplaceableTerm getReplaceWith() {
		PluginLogger.error("getReplaceWith() should not be called for ConditionalMacro.");
		return null;
	}

	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		FormulaChecker checker = new FormulaChecker();
		List<MacroContainer> removeContainerList = new ArrayList<>();

		for (MacroContainer container : macroContainers) {
			Relation relation = new Relation(other.getToReplace(), RelationType.EQUAL, other.getReplaceWith());
			Conjunction conjunction = new Conjunction(container.getCondition(), relation);
			ResultType checkResult = checker.checkSingleFormula(conjunction);
			if (checkResult.equals(ResultType.UNSATISFIABLE)) {
				removeContainerList.add(container);
				continue;
			}
			container.applySimpleMacro(other);
		}
		if (removeContainerList.size() > 0) {
			macroContainers.removeAll(removeContainerList);
		}

		resultList.add(this);
		return resultList;
	}
	
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		//TODO: can VectorMacro influence conditions
		for (MacroContainer container : macroContainers) {
			container.applyVectorMacro(other);
		}

		resultList.add(this);
		return resultList;
	}

	protected List<Macro> applyConditionalMacro(ConditionalMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		Term toCompare = other.getToReplace();
		List<MacroContainer> oldList = new LinkedList<MacroContainer>();
		oldList.addAll(macroContainers);
		macroContainers.clear();

		// apply to all macro containers of this conditional macro
		while (!oldList.isEmpty()) {
			MacroContainer container = oldList.remove(0);

			if (container.contains(toCompare)) {
				// apply all alternative macros and create new conditions
				for (MacroContainer toApplyContainer : other.getMacroContainers()) {
					//
					Macro toApply = toApplyContainer.getMacro();

					// create new condition
					Formula newConditionPart = container.getCondition().createDeepCopy();
					newConditionPart.replaceTermRecursive(toCompare, toApply.getReplaceWith());

					Conjunction newCondition = new Conjunction(newConditionPart,
							toApplyContainer.getCondition().createDeepCopy());

					// check whether the new condition is satisfiable
					FormulaChecker checker = new FormulaChecker();

					ResultType checkResult = checker.checkSingleFormula(newCondition);
					if (checkResult.equals(ResultType.UNSATISFIABLE)) {
						// skip unsatisfiable conditions
						PluginLogger.info("Conditional macro created unsatisfiable formula.");
						PluginLogger.info("Formula: " + newCondition.toString());
						continue;
					}

					// create new macro content
					Macro newMacro = container.getMacro().createDeepCopy();

					// result should be a list with one element
					List<Macro> newMacroList = newMacro.applyOtherMacro(toApply);

					// TODO evaluate result list
					if (newMacroList.size() < 1) {
						PluginLogger.error(
								"Result of the application of a conditional macro to another macro created an empty result.");
						continue;
					} else if (newMacroList.size() > 1) {
						PluginLogger.error(
								"Application of conditional macro to other conditional macro created more than one macro.");
					}
					newMacro = (SimpleMacro) newMacroList.get(0);

					MacroContainer newContainer = new MacroContainer(newMacro, newCondition, null);
					macroContainers.add(newContainer);
				}
			} else {
				// just add this container again
				macroContainers.add(container);
			}
		}

		resultList.add(this);
		return resultList;
	}

	public boolean containsTerm(Term toCompare) {
		for (MacroContainer container : macroContainers) {
			if (container.contains(toCompare)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {
		String result = "\"" + toReplace.toString() + "\":\n";
		for (MacroContainer macroContainer : macroContainers) {
			result += "  " + macroContainer.toString() + "\n";
		}
		return result;
	}

	public List<MacroContainer> getMacroContainers() {
		return macroContainers;
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		continuousBehavior.applyConditionalMacro(this);
	}

	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection hybridProgram) {
		// handle discrete behavior
		List<HybridProgram> programList = hybridProgram.getInnerPrograms();

		for (int i = 0; i < programList.size(); i++) {
			HybridProgram innerProgram = programList.get(i);

			if (innerProgram.containsTerm(toReplace)) {
				ConditionalChoice newProgram = new ConditionalChoice();
				for (MacroContainer container : macroContainers) {
					// create condition
					Formula condition = container.getCondition();

					// create behavior
					HybridProgram choiceProgram = innerProgram.createDeepCopy();

					// replace macro content
					Term toReplace = container.getMacro().getToReplace();
					Term replaceWith = container.getMacro().getReplaceWith();
					choiceProgram.replaceTermRecursive(toReplace, replaceWith);

					// add choice to nondeterministic choice
					newProgram.addChoice(condition, choiceProgram);
				}
				// replace old behavior with new choice
				programList.set(i, newProgram);
			}
		}
	}
	
	@Override
	public void applyToInitialConditions(Conjunction initialConditions) {
		List<Operator> elements = initialConditions.getElements();
		for (int i = 0; i < elements.size(); i++) {
			Operator element = elements.get(i);
			if(element.containsTerm(toReplace)) {
				Conjunction switchConjunction = new Conjunction();
				for (MacroContainer container : macroContainers) {
					
					// create condition
					Formula condition = container.getCondition();
					
					// replace macro content
					Term toReplace = container.getMacro().getToReplace();
					Term replaceWith = container.getMacro().getReplaceWith();
					Operator elementCopy = element.createDeepCopy();
					elementCopy.replaceTermRecursive(toReplace, replaceWith);
					// add choice to initial conditions
					Implication implication = new Implication(condition, elementCopy);
					switchConjunction.addElement(implication);
				}
				elements.set(i, switchConjunction);
			}
		}
	}

	@Override
	public ConditionalMacro createDeepCopy() {
		ConditionalMacro result = new ConditionalMacro(toReplace.createDeepCopy());

		for (MacroContainer oldMacroContainer : macroContainers) {
			MacroContainer newMacroContainer = new MacroContainer(oldMacroContainer.getMacro().createDeepCopy(),
					oldMacroContainer.getCondition().createDeepCopy(), null);
			result.addMacroContainer(newMacroContainer);
		}

		return result;
	}
}
