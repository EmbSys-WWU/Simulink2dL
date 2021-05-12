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
package simulink2dl.transform.model.container;

import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * Container class that encapsulates continuous evolutions with a set of single
 * evolutions and a formula that is later used as condition to execute this set
 * of evolutions and as evolution domain for these evolutions.
 * 
 * @author Timm Liebrenz
 *
 */
public class ContinuousEvolutionContainer {
	private ContinuousEvolution evolution;
	private Formula condition;

	public ContinuousEvolutionContainer(ContinuousEvolution evolution, Formula condition) {
		this.evolution = evolution;
		this.condition = condition;
	}

	public ContinuousEvolution getEvolution() {
		return evolution;
	}

	public Formula getCondition() {
		return condition;
	}

	public void replace(Term toReplace, Term replaceWith) {
		if (evolution.equals(toReplace)) {
			evolution = (ContinuousEvolution) replaceWith;
		} else {
			evolution.replaceTermRecursive(toReplace, replaceWith);
		}
		if (condition.equals(toReplace)) {
			condition = (Formula) replaceWith;
		} else {
			condition.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	public boolean containsTerm(Term toCompare) {
		if (evolution.equals(toCompare)) {
			return true;
		}
		if (condition.equals(toCompare)) {
			return true;
		}
		if (evolution.containsTerm(toCompare)) {
			return true;
		}
		if (condition.containsTerm(toCompare)) {
			return true;
		}
		return false;
	}
	
	public ContinuousEvolutionContainer expand() {
		evolution = (ContinuousEvolution) evolution.expand();
		condition = (Formula) condition.expand();
		return this;
	}

}
