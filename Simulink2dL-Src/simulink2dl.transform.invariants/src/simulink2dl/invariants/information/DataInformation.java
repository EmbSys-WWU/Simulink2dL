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
package simulink2dl.invariants.information;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.graph.InvariantGraph;

public class DataInformation implements InvariantInformation {

	private InvariantGraph invGraph;
	private Term conditions = null;
	private Relation information;

	private boolean inValid = false;
	private boolean isDone = false;

	public DataInformation(InvariantGraph invGraph, Relation signal, Term conditions) {
		this.invGraph = invGraph;
		information = signal;
		this.conditions = conditions;
	}

	public DataInformation(InvariantGraph invGraph, Relation signal) {
		this.invGraph = invGraph;
		information = signal;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public Operator getInformationAsFormula() {
		if (conditions != null) {
			return new Implication((Operator) conditions, information);
		}
		return information;
	}

	@Override
	public void finalizeInformation(Variable replaceTerm) {
		// TODO
	}

	@Override
	public void changeReplaceableTerm(ReplaceableTerm toReplace) {
		// TODO
	}

	@Override
	public int reduceInformation() {
		return 0;
	}

	@Override
	public boolean isValid() {
		return !inValid;
	}

	@Override
	public void inValid() {
		inValid = true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void done() {
		isDone = true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataInformation) {
			DataInformation casted = (DataInformation) obj;
			Relation otherWay = new Relation(casted.getData().getRightSide(), casted.getData().getType(),
					casted.getData().getLeftSide());

			if (conditions != null && casted.getConditions() != null) {
				if (!conditions.equals(casted.getConditions()))
					return false;
			}

			return information.equals(casted.getData()) || information.equals(otherWay);
		}
		return false;
	}

	@Override
	public Object clone() {
		if (conditions != null)
			return new DataInformation(invGraph, information.createDeepCopy(), conditions.createDeepCopy());
		else
			return new DataInformation(invGraph, information.createDeepCopy());
	}

	@Override
	public String toString() {
		if (conditions != null) {
			Implication impl = new Implication((Operator) conditions, information);
			return impl.toString();
		} else {
			return information.toString();
		}
	}

	public Term getConditions() {
		return conditions;
	}

	public Relation getData() {
		return information;
	}

	// For Macros
	@Override
	public boolean containsTerm(Term term) {
		boolean value = true;
		if (conditions != null) {
			value = conditions.containsTerm(term);
		}
		return information.containsTerm(term) || value;
	}

	@Override
	public Operator createDeepCopy() {
		return (DataInformation) this.clone();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return null;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if (conditions != null)
			conditions.replaceTermRecursive(toReplace, replaceWith);
		information.replaceTermRecursive(toReplace, replaceWith);
	}
}
