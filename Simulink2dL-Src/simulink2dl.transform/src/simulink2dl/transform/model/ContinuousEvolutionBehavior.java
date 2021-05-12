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
package simulink2dl.transform.model;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.SingleEvolution;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.MacroContainer;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.macro.VectorMacro;
import simulink2dl.transform.model.container.ContinuousEvolutionContainer;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.satisfiability.FormulaChecker;
import simulink2dl.util.satisfiability.FormulaChecker.ResultType;

/**
 * This class manages all continuous behavior of the system. The behavior
 * consists of continuous evolutions that are contain a condition to be
 * executed.
 * 
 * @author Timm Liebrenz
 *
 */
public class ContinuousEvolutionBehavior {

	private List<ContinuousEvolutionContainer> evolutions;

	public ContinuousEvolutionBehavior() {
		this.evolutions = new LinkedList<ContinuousEvolutionContainer>();
	}

	/**
	 * Adds a single evolution to all continuous evolutions.
	 * 
	 * @param evolution
	 */
	public void addNewSingleEvolution(SingleEvolution evolution) {
		if (evolutions.isEmpty()) {
			ContinuousEvolution newEvolution = new ContinuousEvolution();
			newEvolution.addSingleEvolution(evolution);
			Conjunction newCondition = new Conjunction();
			newCondition.addElement(new BooleanConstant(true));
			evolutions.add(new ContinuousEvolutionContainer(newEvolution, newCondition));
		} else {
			for (ContinuousEvolutionContainer evolutionContainer : evolutions) {
				evolutionContainer.getEvolution().addSingleEvolution(evolution);
			}
		}
	}

	/**
	 * Adds new continuous evolution to the continuous behavior. Each given element
	 * can contain a number of evolution domains and each element can have an
	 * individual condition. The new evolution alternatives are added
	 * 
	 * @param evolution
	 */
	public void addNewEvolutionAlternatives(List<ContinuousEvolutionContainer> toAddEvolutionsAlternatives) {
		// clear old evolutions
		List<ContinuousEvolutionContainer> oldEvolutionContainers = new LinkedList<ContinuousEvolutionContainer>();
		oldEvolutionContainers.addAll(evolutions);
		evolutions.clear();

		// add each new evolution container to all old evolution containers
		for (ContinuousEvolutionContainer toAddEvolutionContainer : toAddEvolutionsAlternatives) {
			Formula toAddCondition = toAddEvolutionContainer.getCondition();
			ContinuousEvolution toAddEvolution = toAddEvolutionContainer.getEvolution();
			Formula toAddEvolutionDomain = toAddEvolution.getEvolutionDomain();

			for (ContinuousEvolutionContainer oldEvolutionContainer : oldEvolutionContainers) {
				// create new Evolution container
				Conjunction newCondition = new Conjunction(oldEvolutionContainer.getCondition().createDeepCopy(),
						toAddCondition);

				ContinuousEvolution newEvolution = oldEvolutionContainer.getEvolution().createDeepCopy();
				Conjunction newEvolutionDomain = new Conjunction(newEvolution.getEvolutionDomain().createDeepCopy(),
						toAddEvolutionDomain.createDeepCopy());

				newEvolution.setEvolutionDomain(newEvolutionDomain);

				for (SingleEvolution toAddSingleEvolution : toAddEvolution.getEvolutionFormulas()) {
					newEvolution.addSingleEvolution(toAddSingleEvolution);
				}

				// add new evolution container to evolutions of behavior object
				evolutions.add(new ContinuousEvolutionContainer(newEvolution, newCondition));
			}
		}
	}

	/**
	 * Applies the given macro to all continuous evolutions.
	 * 
	 * @param macro
	 */
	public void applySimpleMacro(SimpleMacro macro) {
		for (ContinuousEvolutionContainer container : evolutions) {
			container.replace(macro.getToReplace(), macro.getReplaceWith());
		}
	}
	
	public void applyVectorMacro(VectorMacro macro) {
		for (ContinuousEvolutionContainer container : evolutions) {
			container.replace(macro.getToReplace(), macro.getReplaceWithVector());
		}
	}

	public void applyConditionalMacro(ConditionalMacro conditionalMacro) {
		// Term toReplace = conditionalMacro.getToReplace();
		List<ContinuousEvolutionContainer> oldEvolutions = new LinkedList<ContinuousEvolutionContainer>();
		oldEvolutions.addAll(evolutions);

		evolutions.clear();

		for (ContinuousEvolutionContainer oldEvolutionContainer : oldEvolutions) {
			for (MacroContainer macroContainer : conditionalMacro.getMacroContainers()) {
				ContinuousEvolution newEvolution = oldEvolutionContainer.getEvolution().createDeepCopy();
				Formula newConditionPart = oldEvolutionContainer.getCondition().createDeepCopy();
				Formula extraCondition = macroContainer.getExtraCondition();

				// add macro condition to evolution condition
				Conjunction newCondition = new Conjunction(newConditionPart,
						macroContainer.getCondition().createDeepCopy());

				if (extraCondition != null) {
					extraCondition = extraCondition.createDeepCopy();
					newCondition = new Conjunction(newCondition, extraCondition);
				}

				// check whether new condition is satisfiable
				FormulaChecker checker = new FormulaChecker();
				ResultType checkResult = checker.checkSingleFormula(newCondition);
				if (checkResult.equals(ResultType.UNSATISFIABLE)) {
					// skip contradictions
					PluginLogger.info("Condtitional macro created unsatisfiable formula for continous evolution.");
					PluginLogger.info("Formula: " + newCondition.toString());
					continue;
				}

				Macro macro = macroContainer.getMacro();

				// apply macro
				newEvolution.replaceTermRecursive(macro.getToReplace(), macro.getReplaceWith());
				newConditionPart.replaceTermRecursive(macro.getToReplace(), macro.getReplaceWith());

				// add macro condition to evolution domain
				Conjunction newEvolutionDomain = new Conjunction(newEvolution.getEvolutionDomain().createDeepCopy(),
						macroContainer.getCondition().createDeepCopy());
				newEvolution.setEvolutionDomain(newEvolutionDomain);

				evolutions.add(new ContinuousEvolutionContainer(newEvolution, newCondition));
			}
		}
	}

	/**
	 * Creates the hybrid programs that represent the continuous behavior.
	 */
	public HybridProgram asHybridProgram() {
		ConditionalChoice conditionalChoice = new ConditionalChoice();

		for (ContinuousEvolutionContainer container : evolutions) {
			// create condition
			Formula condition = container.getCondition();
			// create content
			ContinuousEvolution evolution = container.getEvolution().createDeepCopy();
			evolution.setEvolutionDomain(container.getCondition());

			// add choice
			conditionalChoice.addChoice(condition, container.getEvolution());
		}
		return conditionalChoice;
	}

	/**
	 * For debugging purpose.
	 */
	public String toString() {
		return asHybridProgram().toStringFormatted("", false, false);
	}

	public void addAlternativeToAllEvolutionDomains(Formula evolutionDomainExtension) {
		for (ContinuousEvolutionContainer evolutionContainer : evolutions) {
			ContinuousEvolution evolution = evolutionContainer.getEvolution();

			Formula evolutionDomain = new Disjunction(evolution.getEvolutionDomain(),
					evolutionDomainExtension.createDeepCopy());

			evolution.setEvolutionDomain(evolutionDomain);
		}
	}

	public void addConjunctionToAllEvolutionDomains(Formula evolutionDomainExtension) {
		for (ContinuousEvolutionContainer evolutionContainer : evolutions) {
			ContinuousEvolution evolution = evolutionContainer.getEvolution();

			Formula evolutionDomain = new Conjunction(evolution.getEvolutionDomain(),
					evolutionDomainExtension.createDeepCopy());

			evolution.setEvolutionDomain(evolutionDomain);
		}
	}
	
	public ContinuousEvolutionBehavior expand() {
		for(ContinuousEvolutionContainer evolution : evolutions) {
			evolution.expand();
		}
		return this;
	}

}
