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
package simulink2dl.dlmodel;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.util.PluginLogger;

/**
 * This class contains a model given as representation in differential dynamic
 * logic (dL).
 * 
 * @author Timm Liebrenz
 *
 */
public class DLModel {

	/**
	 * List that contains all functions of the system, which do not change their
	 * value
	 */
	private List<Constant> constants;

	/**
	 * List that contains all variables of the system
	 * Protected to allow access during vector expansion
	 */
	protected List<Variable> variables;

	/**
	 * Problem that contains the system behavior
	 */
	private Operator problem;

	/**
	 * Constructor for a dL-Model
	 */
	public DLModel() {
		// initialize member variables
		constants = new LinkedList<Constant>();
		variables = new LinkedList<Variable>();
	}

	/**
	 * Adds the given constant to the system
	 * 
	 * @param constant
	 */
	public void addConstant(Constant constant) {
		if (!this.constants.contains(constant)){
			this.constants.add(constant);
		}
	}

	/**
	 * Adds the given variable to the system
	 * 
	 * @param constant
	 */
	public void addVariable(Variable variable) {
		if (!this.variables.contains(variable)){
			this.variables.add(variable);
		}
	}

	/**
	 * Returns the constant with the given name or null if no such constant exists.
	 * 
	 * @param toSearch
	 */
	public Constant getConstantByName(String toSearch) {
		for (Constant constant : constants) {
			if (constant.getName().equals(toSearch)) {
				return constant;
			}
		}
		PluginLogger.error("Could not find constant with name: " + toSearch);
		return null;
	}

	/**
	 * Returns the variable with the given name or null if no such variable exists.
	 * 
	 * @param toSearch
	 */
	public Variable getVariableByName(String toSearch) {
		for (Variable variable : variables) {
			if (variable.getName().equals(toSearch)) {
				return variable;
			}
		}
		PluginLogger.error("Could not find variable with name: " + toSearch);
		return null;
	}

	/**
	 * Sets the problem of the model to the given.
	 * 
	 * @param newProblem
	 */
	public void setProblem(Operator newProblem) {
		this.problem = newProblem;
	}

	/**
	 * Generates the function String of the model.
	 * 
	 * @return
	 */
	private String getFunctionsString() {
		// write "Functions", constants etc. are in this category
		// function symbols cannot change their value
		String result = "Functions.\n";
		// constants
		for (Constant constant : constants) {
			result += "  " + constant.toDefString() + ".\n";
		}
		result += "End.\n\n";

		return result;
	}

	/**
	 * Generates the variables String of the model.
	 * 
	 * @return
	 */
	private String getVariablesString() {
		// write "ProgramVariables"
		// program variables may change their value over time
		String result = "ProgramVariables.\n";
		// variables (e.g. Signal lines)
		for (Variable variable : variables) {
			result += "  " + variable.toDefString() + ".\n";
		}
		result += "End.\n\n";

		return result;
	}

	/**
	 * Generates the problem String of the model.
	 * 
	 * @return
	 */
	private String getProblemString(boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		// write "Problem"
		// conjecture in differential dynamic logic
		String result = "Problem.\n";

		String indent = "  ";
		
		result += indent + problem.toStringFormatted(indent, multiLineTestFormulas, multiLineEvolutionDomains) + "\n";

		result += "End.\n";

		return result;
	}

	/**
	 * Generates the model as output String.
	 * 
	 * @return
	 */
	public String createOutputString(boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String result = getFunctionsString();
		result += getVariablesString();
		result += getProblemString(multiLineTestFormulas, multiLineEvolutionDomains);

		return result;
	}

	/**
	 * @param constants the constants to set
	 */
	public void setConstants(List<Constant> constants) {
		this.constants = constants;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

}
