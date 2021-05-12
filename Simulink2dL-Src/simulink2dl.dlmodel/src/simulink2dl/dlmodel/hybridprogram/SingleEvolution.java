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
package simulink2dl.dlmodel.hybridprogram;

import java.util.LinkedList;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.util.PluginLogger;

/**
 * This class represent the continuous evolution of a single variable described
 * by a given term.
 * 
 * @author Timm Liebrenz
 *
 */
public class SingleEvolution {

	private Variable variable;

	private Term evolution;

	/**
	 * Constructor for a evolution of a variable along a term.
	 * 
	 * @param variable
	 * @param evolution
	 */
	public SingleEvolution(Variable variable, Term evolution) {
		this.variable = variable;
		this.evolution = evolution;
	}

	public Variable getVariable() {
		return variable;
	}

	@Override
	public String toString() {
		return variable.toString() + "' = " + evolution.toString();
	}

	public void replace(Term toReplace, Term replaceWith) {
		if (evolution.equals(toReplace)) {
			evolution = replaceWith;
		} else {
			evolution.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	public boolean containsTerm(Term term) {
		if (evolution.equals(term)) {
			return true;
		}
		return evolution.containsTerm(term);
	}

	public SingleEvolution createDeepCopy() {
		return new SingleEvolution(variable.createDeepCopy(), evolution.createDeepCopy());
	}
	
	/**
	 * Generates a List of SingleEvolutions for each Variable-Term pair
	 * @return list of evolutions of vector entries
	 */
	public LinkedList<SingleEvolution> expand() {
		LinkedList<SingleEvolution> evolutions = new LinkedList<SingleEvolution>();
		if(variable.getSize()>0) {
			VectorTerm vectorLeft = variable.getVector();
			VectorTerm vectorRight = null;
			if (evolution instanceof Variable) {
				vectorRight = ((Variable) evolution).getVector();
			} else if (evolution instanceof VectorTerm) {
				vectorRight = (VectorTerm) evolution;
			}
			
			if (vectorRight != null && vectorLeft.size() == vectorRight.size()) {
				for(int i = 0; i<variable.getSize();i++) {
					evolutions.add(new SingleEvolution( (Variable)(vectorLeft.get(i)) , vectorRight.get(i) ));
				}
			} else {
				PluginLogger.error("Vector expansion of " + this.getClass().toString()+ " failed: size of variable != size of assignment");
			}
		} else {
			evolutions.add(this);
		}
		return evolutions;
	}
}
