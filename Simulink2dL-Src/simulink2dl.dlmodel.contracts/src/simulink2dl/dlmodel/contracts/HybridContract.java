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

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.DLModelDefaultStructure;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
/** Contract Superclass used for the SimulinkRL2dL project.
 * TODO: Rework contract implementation.
 * - Use parser instead of hard-coded contracts.
 * - Delete Contract interface, which only exists to support deprecated SimpleContract.
*/
public class HybridContract {
	// useful terms for easing hard coded contract definition
	// TODO remove when implementing parser
	protected static RealTerm ZERO = new RealTerm(0);
	protected static RealTerm ONE = new RealTerm(1);
	protected static RealTerm NEGONE = new RealTerm(-1);
	
	protected static Relation.RelationType GREATER_THAN = Relation.RelationType.GREATER_THAN;
	protected static Relation.RelationType GREATER_EQUAL = Relation.RelationType.GREATER_EQUAL;
	protected static Relation.RelationType EQUAL = Relation.RelationType.EQUAL;
	protected static Relation.RelationType LESS_EQUAL = Relation.RelationType.LESS_EQUAL;
	protected static Relation.RelationType LESS_THAN = Relation.RelationType.LESS_THAN;
	protected static Relation.RelationType NOT_EQUAL = Relation.RelationType.NOT_EQUAL;
	
	protected static BooleanConstant TRUE = new BooleanConstant(true);

	// contract identifier 
	// TODO not used
	private String id;

	// contract flags
	private boolean containsContinuousVariable;

	// actual assumption-guarantee pair
	private Conjunction assumptions;
	private Conjunction guarantees;

	// lists for intermediate variables and constants
	protected LinkedList<Variable> intermediateVariables;
	protected LinkedList<Constant> constants;
	private LinkedList<Constant> referencedConstants;
	
	protected LinkedList<GhostVariable> ghostVariables;
	protected LinkedList<Variable> inputs;
	protected LinkedList<Variable> outputs;

	public HybridContract() {
		inputs = new LinkedList<Variable>();
		outputs = new LinkedList<Variable>();
		
		intermediateVariables = new LinkedList<Variable>();
		constants = new LinkedList<Constant>();
		referencedConstants = new LinkedList<Constant>();
		ghostVariables = new LinkedList<GhostVariable>();
		
		assumptions = new Conjunction();
		guarantees = new Conjunction();
	}

	public void setAssumptionGuaranteePair(Formula assumptions, Formula guarantees) {
		this.assumptions = new Conjunction();
		this.assumptions.addElement(assumptions);
		this.guarantees = new Conjunction();
		this.guarantees.addElement(guarantees);
	}

	protected void setContainsContinuousVariable(boolean value) {
		containsContinuousVariable = value;
	}

	protected void addIntermediateVariable(Variable variable) {
		intermediateVariables.add(variable);
	}

	public LinkedList<Variable> getIntermediateVariables() {
		return intermediateVariables;
	}

	protected void addConstant(Constant constant) {
		constants.add(constant);
	}

	public List<Constant> getConstants() {
		return constants;
	}

	protected void addReferencedConstant(Constant constant) {
		referencedConstants.add(constant);
	}

	public HybridProgramCollection createContractProgram() {
		HybridProgramCollection result = new HybridProgramCollection();
		return result;
	}
	
	public String toString() {
		Implication contract = new Implication(assumptions.createDeepCopy(), guarantees.createDeepCopy());
		return contract.toString();
	}
	
	public HybridProgram asHybridProgram() {
		return new TestFormula(this.asFormula());
	}
	
	public Implication asFormula() {
		return new Implication(assumptions.createDeepCopy(), guarantees.createDeepCopy());
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * Creates a full dL model for a contract.
	 * Used e.g. for generating runtime monitors for RL contracts
	 */
	public DLModel todLModel() {
		DLModelDefaultStructure contractDL = new DLModelDefaultStructure();
		for(Variable var : inputs)
			contractDL.addVariable(var);
		for(Variable var : outputs)
			contractDL.addVariable(var);
		for(Constant constant : constants)
			contractDL.addConstant(constant);
		HybridProgramCollection hp = new HybridProgramCollection();
		for(Variable var : outputs) {
			hp.addElement(new NondeterministicAssignment(var));
		}
		hp.addElement(this.asHybridProgram());
		contractDL.addBehavior(hp);
		contractDL.addInitialCondition(new BooleanConstant(true));
		return contractDL;
	}
	
	public LinkedList<Variable>  getInputs() {
		return inputs;
	}

	public LinkedList<Variable>  getOutputVariables() {
		return outputs;
	}
	
	public LinkedList<GhostVariable>  getGhostVariable() {
		return ghostVariables;
	}
	
	public void addAssumption(Formula assumption) {
		assumptions.addElement(assumption);
	}
	
	public void addGuarantee(Formula guarantee) {
		guarantees.addElement(guarantee);
	}

}
