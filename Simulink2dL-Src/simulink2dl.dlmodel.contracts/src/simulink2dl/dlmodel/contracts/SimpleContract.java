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
package simulink2dl.dlmodel.contracts;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
/** 
 * Contract class used for previous projects
*/
@Deprecated
public class SimpleContract {

	// contract identifier
	private String id;

	// contract flags
	private boolean containsContinuousVariable;

	// assumptions
	private Conjunction assumptions;

	// guarantees
	private Conjunction guarantees;

	// lists for intermediate variables and constants
	private LinkedList<Variable> intermediateVariables;
	private LinkedList<Constant> constants;
	private LinkedList<Constant> referencedConstants;

	public SimpleContract() {
		this.assumptions = new Conjunction();
		this.guarantees = new Conjunction();

		this.intermediateVariables = new LinkedList<Variable>();
		this.constants = new LinkedList<Constant>();
		this.referencedConstants = new LinkedList<Constant>();
	}

	public void addAssumption(Formula newAssumption) {
		this.assumptions.addElement(newAssumption);
	}

	public void addGuarantee(Formula newGuarantee) {
		this.guarantees.addElement(newGuarantee);
	}

	protected void setContainsContinuousVariable(boolean value) {
		this.containsContinuousVariable = value;
	}

	protected void addIntermediateVariable(Variable variable) {
		this.intermediateVariables.add(variable);
	}

	public List<Variable> getIntermediateVariables() {
		return this.intermediateVariables;
	}

	protected void addConstant(Constant constant) {
		this.constants.add(constant);
	}

	public List<Constant> getConstants() {
		return this.constants;
	}

	protected void addReferencedConstant(Constant constant) {
		this.referencedConstants.add(constant);
	}

	public HybridProgramCollection createContractProgram() {
		HybridProgramCollection result = new HybridProgramCollection();
		return result;
	}

	public void setVariable(String varType, int port, Variable variable) {
//		ReplaceableTerm toReplace = new ReplaceableTerm("#" + varType + port);
//
//		assumptions.replaceTermRecursive(toReplace, variable);
//		guarantees.replaceTermRecursive(toReplace, variable);
	}

	// For debugging purpose
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("(");
		result.append(assumptions.toStringFormatted("  \n", true, true));
		result.append(")\n->\n(");
		result.append(guarantees.toStringFormatted("  \n", true, true));
		result.append(")");

		return result.toString();
	}

	public HybridProgram asHybridProgram() {
		return new TestFormula(new Implication(assumptions, guarantees));
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

}
