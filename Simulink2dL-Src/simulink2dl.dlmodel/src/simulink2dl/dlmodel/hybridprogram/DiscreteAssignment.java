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
 * This class represents a discrete assignment of a term to a variable.
 * 
 * @author Timm Liebrenz
 *
 */
public class DiscreteAssignment implements HybridProgram {

	private Variable variable;

	private Term assignmentTerm;

	/**
	 * Constructor for a discrete assignment.
	 * 
	 * @param var
	 * @param term
	 */
	public DiscreteAssignment(Variable variable, Term term) {
		this.variable = variable;
		this.assignmentTerm = term;
	}
	
	public String toString() {
		return variable.toString() + ":=" + assignmentTerm + ";";
	}
	
	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return variable.toString() + ":=" + assignmentTerm + ";";
	}
	
	public Variable getVariable() {
		return variable;
	}
	
	public Term getAssignmentTerm () {
		return assignmentTerm;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (assignmentTerm.equals(toReplace)) {
			assignmentTerm = replaceWith;
		} else {
			assignmentTerm.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {
		if (assignmentTerm.equals(term)) {
			return true;
		}
		return assignmentTerm.containsTerm(term);
	}

	@Override
	public DiscreteAssignment createDeepCopy() {
		return new DiscreteAssignment(variable.createDeepCopy(), assignmentTerm.createDeepCopy());
	}

	/**
	 * Replaces DiscreteAssignment with a collection of assignments for each pair of vector entries.
	 * @return HPCollection of expanded DiscreteAssignments or the old DiscreteAssignment if no expansion was necessary
	 */
	@Override
	public HybridProgram expand() {
		if(variable.getSize()>0) {
			HybridProgramCollection assignments = new HybridProgramCollection();
			VectorTerm vectorLeft = variable.getVector();
			VectorTerm vectorRight = null;
			if (assignmentTerm instanceof Variable) {
				vectorRight = ((Variable) assignmentTerm).getVector();
			} else if (assignmentTerm instanceof VectorTerm) {
				vectorRight = (VectorTerm) assignmentTerm;
			}
			
			if(vectorRight != null && vectorLeft.size() == vectorRight.size()) {
				for(int i = 0; i<variable.getSize();i++) {
					assignments.addElement(new DiscreteAssignment((Variable)vectorLeft.get(i), vectorRight.get(i)));
				}
				return assignments;
			} else {
				PluginLogger.error("Vector expansion of " + this.getClass().toString()+ " failed: size of variable != size of assignment");
			}
		}
		return this;
		
	}
	
	public boolean equals(DiscreteAssignment other) {
		if(other==null)
			return false;
		else if(this.variable!=null && this.variable.equals(other.variable))
				if(this.assignmentTerm!=null && this.assignmentTerm.equals(other.assignmentTerm))
					return true;
		return false;
	}
}
