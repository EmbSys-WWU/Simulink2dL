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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.hybridprogram.SingleEvolution;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;

/**
 * This optimizer handles the evolution domains of continuous evolutions.
 * Elements in the conjunction of the evolution domain, which are not changed by
 * the continuous evolutions, are removed.
 * 
 * @author Timm Liebrenz
 *
 */
public class EvolutionDomainOptimizer extends Optimizer {

	@Override
	protected void handleContinuousEvolution(ContinuousEvolution continuousEvolution) {
		List<Variable> evolutionVariables = new LinkedList<Variable>();

		for (SingleEvolution evolution : continuousEvolution.getEvolutionFormulas()) {
			evolutionVariables.add(evolution.getVariable());
		}

		Formula domain = continuousEvolution.getEvolutionDomain();

		this.handleDomain(domain, evolutionVariables);
	}

	private void handleDomain(Formula formula, List<Variable> evolutionVariables) {
		if (formula instanceof Conjunction) {
			handleConjunction((Conjunction) formula, evolutionVariables);
		} else if (formula instanceof Disjunction) {
			handleDisjunction((Disjunction) formula, evolutionVariables);
		}
	}

	private void handleConjunction(Conjunction conjunction, List<Variable> evolutionVariables) {
		List<Operator> elements = conjunction.getElements();
		List<Operator> newElements = new LinkedList<Operator>();

		for (Operator element : elements) {
			if (containsEvolutionVariable(element, evolutionVariables)) {
				newElements.add(element);
			}
		}

		if (newElements.isEmpty()) {
			newElements.add(new BooleanConstant(true));
		}

		conjunction.setElements(newElements);
	}

	private void handleDisjunction(Disjunction disjunction, List<Variable> evolutionVariables) {
		for (Operator element : disjunction.getElements()) {
			// TODO extend this
			if (element instanceof Formula) {
				handleDomain((Formula) element, evolutionVariables);
			}
		}
	}

	private boolean containsEvolutionVariable(Operator formula, List<Variable> evolutionVariables) {
		for (Variable variable : evolutionVariables) {
			if (formula.containsTerm(variable)) {
				return true;
			}
		}
		return false;
	}

}
