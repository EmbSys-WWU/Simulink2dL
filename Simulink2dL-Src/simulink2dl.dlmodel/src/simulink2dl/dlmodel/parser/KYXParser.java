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
package simulink2dl.dlmodel.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.DLModelDefaultStructure;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.hybridprogram.DebugString;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.NondeterministicChoice;
import simulink2dl.dlmodel.hybridprogram.NondeterministicRepetition;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * Parser for plain text kyx files.
 *
 * @author nick
 *
 */
public class KYXParser {

	/**
	 * List that contains all functions of the system, which do not change their
	 * value
	 */
	private List<Constant> constants = new LinkedList<Constant>();

	/**
	 * List that contains all variables of the system
	 */
	private List<Variable> variables = new LinkedList<Variable>();

	/**
	 * Problem that contains the system behavior
	 */
	private Operator problem;

	private DLModelDefaultStructure defStruct;

	private DLModel dlmodel;

	private FormulaParser formulaParser;

	/**
	 * Creates new parser from an uncompressed kyx file.
	 *
	 * @param kyxFile
	 */
	public KYXParser(FileReader kyxFile) {
		StringBuilder strb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(kyxFile)) {
			String line = br.readLine();
			while (line != null) {
				// remove comments, cut leading/trailing whitespace
				line = line.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
				line = line.trim();

				if (!line.isEmpty()) {
					strb.append(line);
				}
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String section : strb.toString().split("End.")) {
			String[] lines = section.split("\\.");
			switch (lines[0]) {
			case "Functions":
				for (int i = 1; i < lines.length; i++) {
					this.constants.add(AtomicTermParser.parseToConstant(lines[i]));
				}
				break;
			case "ProgramVariables":
				for (int i = 1; i < lines.length; i++) {
					this.variables.add(AtomicTermParser.parseToVariable(lines[i]));
				}
				break;
			case "Problem":
				this.formulaParser = new FormulaParser(this);
				this.parseProblem(section.replaceFirst("Problem.", "").replaceAll(" ", ""));
				break;
			default:
				System.out.println("[ERR] Unexpected case found, while trying to evaluate sections. \n " + lines[0]);
				break;
			}
		}
	}

	private Operator parseProblem(String problemString) {
		String[] problem = problemString.split("->\\[");
		if (problem.length == 2) { // DLModelDefaultStructure
			// initial condition -> [behavior]true
			this.defStruct = new DLModelDefaultStructure();
			this.defStruct.setConstants(this.constants);
			this.defStruct.setVariables(this.variables);

			Formula initCondFormula = this.formulaParser.parse(problem[0]);
			this.defStruct.addInitialCondition(initCondFormula);

			int closingBracket = problem[1].lastIndexOf(']');
			String programInsideBox = problem[1].substring(0, closingBracket);
			// String postCondition = problem[1].substring(closingBracket + 1);
			// Formula postConditionFormula = this.formulaParser.parse(postCondition);
			// TODO is postCondition ever used?
			NondeterministicRepetition loop = parseNondeterministicRepetition(programInsideBox);
			this.defStruct.addBehavior(loop.getInnerProgram());
			this.defStruct.getLoop().setInvariant(loop.getInvariant());
		} else { // DLModel
			// TODO
			// this.dlmodel = new DLModel();
		}
		return null;
	}

	private NondeterministicRepetition parseNondeterministicRepetition(String repetitionString) {
		int i = repetitionString.lastIndexOf("*@invariant");
		if (i > 0) {
			assert closingBracket(repetitionString, 0) == i - 1;
			String wrappedRepitition = repetitionString.substring(0, i);
			HybridProgram repitition = this.parseInnerProgram(unwrap(wrappedRepitition));

			String wrappedInvariant = repetitionString.substring(i + "*@invariant".length());
			Formula invariantForm = this.formulaParser.parse(wrappedInvariant);

			return new NondeterministicRepetition(repitition, invariantForm);
		}
		int end = closingBracket(repetitionString, 0);
		assert (repetitionString.charAt(end + 1) == '*' && repetitionString.length() == end + 1);
		return (NondeterministicRepetition) this.parseInnerProgram(repetitionString);
	}

	private HybridProgram parseInnerProgram(String programString) {
		// anchors
		if (!programString.startsWith("{")) {
			return this.parseNonNestedProgram(programString);
		}
		int end = closingBracket(programString, 0);
		if (end + 1 == programString.length()) {
			// call parseInnerProgram on unwrapped content, if corresponding '}' is
			// the last character
			return this.parseInnerProgram(unwrap(programString));
		}
		if (programString.charAt(end + 1) == '*' && programString.length() == end + 1) {
			return new NondeterministicRepetition(new DebugString(unwrap(programString)));
		}

		// recursion
		HybridProgramCollection coll = new HybridProgramCollection();
		int start = 0;
		while (end != -1) {
			String subProgStr = programString.substring(start + 1, end);
			coll.addElement(this.parseInnerProgram(subProgStr));
			if (end + 1 < programString.length()) {
				start = end + 1;
				end = closingBracket(programString, start);
			} else {
				end = -1;
			}
		}
		return coll;
	}

	private HybridProgram parseNonNestedProgram(String str) {
		assert str.indexOf('{') == -1;
		assert str.indexOf('}') == -1;
		// HybridProgram simple = this.parseNonNestedProgram(str.substring(0,
		// start));
		// HybridProgram complex = this.parseInnerProgram(str.substring(start));
		// return new HybridProgramCollection(simple, complex);
		if (str.contains("++")) {
			return this.parseNonNestedNondeterministicChoice(str);
		}
		return this.parseNonNestedSequence(str);
	}

	private HybridProgram parseNonNestedNondeterministicChoice(String prog) {
		NondeterministicChoice chzr = new NondeterministicChoice();
		for (String str : prog.split("\\+\\+")) {
			chzr.addChoice(this.parseNonNestedSequence(str));
		}
		return chzr;
	}

	private HybridProgram parseNonNestedSequence(String program) {
		String[] splits = program.split(";");
		if (splits.length == 1) {
			return this.parsePrimitive(splits[0]);
		}

		HybridProgramCollection coll = new HybridProgramCollection();
		for (String str : splits) {
			coll.addElement(this.parsePrimitive(str));
		}
		return coll;
	}

	private HybridProgram parsePrimitive(String program) {
		if (program.contains(":=")) {
			return this.parsePrimitiveAssignment(program);
		}

		if (program.startsWith("?")) {
			return this.parsePrimitiveTestFormula(program);
		}

		return this.parsePrimitiveEvolution(program);
	}

	private static String unwrap(String str) {
		return unwrap(str, 0);
	}

	/**
	 * Starts from the curly opening bracket at the given position, finds the
	 * corresponding closing bracket and returns everything inbetween.
	 * 
	 * @param str
	 * @param start
	 * @return
	 */
	private static String unwrap(String str, int start) {
		int end = closingBracket(str, start);
		if (end == -1) {
			return "";
		}
		return str.substring(start + 1, end);
	}

	/**
	 * Starts from the curly opening bracket at the given position, finds the
	 * corresponding closing bracket and returns its position.
	 * 
	 * @param str
	 * @param start
	 * @return
	 */
	static int closingBracket(String str, int start) {
		if (str.length() < 2 || str.length() <= start) {
			System.out.println("[ERR] Malformed string: " + str + "\n Finding } at " + start + " impossible!");
			return Integer.MIN_VALUE;
		}

		if (str.charAt(start) != '{') {
			return -1;
		}

		int braceStatus = 1;
		for (int i = start + 1; i < str.length(); i++) {
			if (str.charAt(i) == '{') {
				braceStatus++;
			} else if (str.charAt(i) == '}') {
				braceStatus--;
			}

			if (braceStatus == 0) {
				return i;
			}
		}

		System.out.println("[ERR] String: " + str + "\n Contains an opening, but no closing bracket!");
		return Integer.MIN_VALUE;
	}

	/**
	 * Parses a string representation of a assignment (nondeterministic as well as
	 * discrete). Take note: No additional checks or transformations are applied.
	 * (primitive statement 1|2)
	 * 
	 * @param assignment
	 * @return
	 */
	private HybridProgram parsePrimitiveAssignment(String assignment) {
		String[] varTerm = assignment.split(":=");
		assert varTerm.length == 2;
		String valStr = varTerm[1].replaceFirst(";", "");
		if (valStr.equals("*")) {
			return new NondeterministicAssignment(this.getVariableByName(varTerm[0]));
		}
		return new DiscreteAssignment(this.getVariableByName(varTerm[0]), this.formulaParser.parseTerm(valStr));
	}

	/**
	 * Parses a string representation of a continuous evolution. Take note: No
	 * additional checks or transformations are applied. (primitive statement 3)
	 * 
	 * @param prog
	 * @return
	 */
	private ContinuousEvolution parsePrimitiveEvolution(String prog) {
		String[] evoDom = prog.split("&", 2);
		ContinuousEvolution evo = new ContinuousEvolution(this.formulaParser.parse(evoDom[1]));
		for (String singleEvo : evoDom[0].split(",")) {
			String[] varVal = singleEvo.split("'=");
			Variable var = getVariableByName(varVal[0]);
			Term val = getTermByName(varVal[1]);
			if (val == null) {
				evo.addSingleEvolution(var, this.formulaParser.parseTerm(varVal[1]));
			} else {
				evo.addSingleEvolution(var, val);
			}
		}
		return evo;
	}

	/**
	 * Parses a string representation of a test formula. Take note: No additional
	 * checks or transformations are applied. (primitive statement 4)
	 * 
	 * @param formula
	 * @return
	 */
	private HybridProgram parsePrimitiveTestFormula(String formula) {
		if (formula.startsWith("?")) {
			formula = formula.replaceFirst("\\?", "");
		}
		return new TestFormula(this.formulaParser.parse(formula));
	}

	@SuppressWarnings("unused")
	private HybridProgram parseIfStatement(String ifstate) {
		return null; // TODO implement
	}

	public static void main(String[] args) {
		FileReader fr = null;
		try {
			fr = new FileReader("G:\\keymaerax\\09-ping-pong.kyx");
		} catch (Exception e) {
			e.printStackTrace();
		}
		KYXParser parser = new KYXParser(fr);

		if (parser.defStruct != null) {
			System.out.println("[MAIN] out:\n" + parser.defStruct.createOutputString(true, true));
		}
		if (parser.dlmodel != null) {
			System.out.println("[MAIN] out:\n" + parser.dlmodel.createOutputString(true, true));
		}
	}

	/**
	 * @return the constants
	 */
	public List<Constant> getConstants() {
		return constants;
	}

	/**
	 * @return the variables
	 */
	public List<Variable> getVariables() {
		return variables;
	}

	/**
	 * @return the problem
	 */
	public Operator getProblem() {
		return problem;
	}

	/**
	 * Returns the constant with the given name or null if no such constant exists.
	 * 
	 * @param toSearch
	 */
	Constant getConstantByName(String toSearch) {
		for (Constant constant : constants) {
			if (constant.getName().equals(toSearch)) {
				return constant;
			}
		}
		return null;
	}

	/**
	 * Returns the variable with the given name or null if no such variable exists.
	 * 
	 * @param toSearch
	 */
	Variable getVariableByName(String toSearch) {
		for (Variable variable : variables) {
			if (variable.getName().equals(toSearch)) {
				return variable;
			}
		}
		return null;
	}

	/**
	 * Returns a constant with the given name, a variable with the given name or
	 * null, iff neither exit.
	 * 
	 * @param toSearch
	 * @return
	 */
	Term getTermByName(String toSearch) {
		Constant con = getConstantByName(toSearch);
		if (con == null) {
			return getVariableByName(toSearch);
		}
		return con;
	}
}
