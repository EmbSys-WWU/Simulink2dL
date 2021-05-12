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

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.util.PluginLogger;

/**
 * This class represents a nondeterministic assignment of an arbitrary value to
 * a variable.
 * 
 * @author Timm Liebrenz
 *
 */
public class NondeterministicAssignment implements HybridProgram {

	private Variable variable;

	/**
	 * Constructor for a nondeterministic assignment to the given variable.
	 * 
	 * @param variable
	 */
	public NondeterministicAssignment(Variable variable) {
		this.variable = variable;
	}

	@Override
	public String toString() {
		return variable.toString() + ":= *;";
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return variable.toString() + ":= *;";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		// do nothing
	}

	@Override
	public boolean containsTerm(Term term) {
		return false;
	}

	@Override
	public NondeterministicAssignment createDeepCopy() {
		return new NondeterministicAssignment(variable.createDeepCopy());
	}

	@Override
	public HybridProgram expand() {
		if(variable.getSize()>0) {
			HybridProgramCollection assignments = new HybridProgramCollection();
			VectorTerm vectorLeft = variable.getVector();
			for(int i = 0; i<variable.getSize();i++) {
					assignments.addElement(new NondeterministicAssignment((Variable)vectorLeft.get(i)));
			}
			return assignments;
		}
		return this;
	}


}
