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

import java.io.FileReader;
import java.util.ArrayList;

import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.BracketTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.Term;

/**
 * While parsing a "KeYmaera X" script, the KYXParser may encounter string
 * representation of Formulas. This class handles the parsing of these.
 * 
 * @author nick
 *
 */
public class FormulaParser {

	/**
	 * The calling KYXParser, holds Variables and constants.
	 */
	private KYXParser kyxp;

	/**
	 * Expects a KYXParser which holds references to Variables and Constants of the
	 * problem. Parses expressions like "((x>5+1/2-v | 2*x>2*5-v^2&v<1) & v>=0)"
	 * correctly.
	 * 
	 * @param kyxparser
	 */
	public FormulaParser(KYXParser kyxparser) {
		this.kyxp = kyxparser;
	}

	/**
	 * Parses expressions like "((x>5+1/2-v | 2*x>2*5-v^2&v<1) & v>=0)" correctly
	 * and returns a Formula representing a given expression.
	 * 
	 * @param termString
	 * @return
	 */
	public Formula parse(String termString) {
		return this.splitOnDisjunction(termString);
	}

	/**
	 * Splits a string representation on "|" and parses the results. Treats round
	 * brackets as atoms. Since this operation has the lowest precedence, this
	 * method is the entry point for parsing.
	 * 
	 * @param termString
	 * @return
	 */
	private Formula splitOnDisjunction(String termString) {
		Disjunction dis = new Disjunction();
		int start = 0;
		Formula tmp;
		for (int i = 0; i < termString.length(); i++) {
			switch (termString.charAt(i)) {
			case '(':
				i = closingBracket(termString, i);
				break;

			case '|':
				tmp = this.splitOnConjunction(termString.substring(start, i));
				dis.addLiterals(tmp);
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				break;

			default:
				break;
			}
		}

		tmp = this.splitOnConjunction(termString.substring(start));
		dis.addLiterals(tmp);

		// don't encapsulate, if unnecessary
		if (!dis.getElements().isEmpty() && dis.getElements().size() == 1) {
			return (Formula) dis.getElements().get(0);
		}

		return dis;
	}

	/**
	 * Splits a string representation on "&".
	 * 
	 * @param termString
	 * @return
	 */
	private Formula splitOnConjunction(String termString) {
		Conjunction con = new Conjunction();
		int start = 0;
		Formula tmp;
		for (int i = 0; i < termString.length(); i++) {
			switch (termString.charAt(i)) {
			case '(':
				i = closingBracket(termString, i);
				break;

			case '&':
				tmp = this.branchOnNegation(termString.substring(start, i));
				con.addLiterals(tmp);
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				break;

			default:
				break;
			}
		}

		tmp = this.branchOnNegation(termString.substring(start));
		con.addLiterals(tmp);

		// don't encapsulate, if unnecessary
		if (!con.getElements().isEmpty() && con.getElements().size() == 1) {
			return (Formula) con.getElements().get(0);
		}

		return con;
	}

	/**
	 * As negation has the highest precedence within logical operators, it will be
	 * considered last during the parsing. This method decides, whether an
	 * expression is a negation or a (bracketed) logical formula and acts
	 * accordingly.
	 * 
	 * @param termString
	 * @return
	 */
	private Formula branchOnNegation(String termString) {
		if (termString.startsWith("!(")) {
			int closingBrack = closingBracket(termString, 1);
			assert termString.length() - 1 == closingBrack;
			Formula tmp = this.splitOnDisjunction(termString.substring(2, termString.length() - 1));
			return new Negation(tmp);
		}

		// unwrap brackets if necessary
		if (termString.startsWith("(")) {
			int closingBrack = closingBracket(termString, 0);
			assert termString.length() - 1 == closingBrack;
			Formula tmp = this.splitOnDisjunction(termString.substring(1, termString.length() - 1));
			return tmp;
		}

		return this.parseRelation(termString);
	}

	/**
	 * Parses a string representation of a Relation.
	 * 
	 * @param termString
	 * @return
	 */
	private Relation parseRelation(String termString) {
		Term rhs;
		Term lhs;
		RelationType rel;
		int eq = termString.indexOf('=');
		if (-1 < eq) {
			rhs = this.parseTerm(termString.substring(eq + 1));
			switch (termString.charAt(eq - 1)) {
			case '<':
				lhs = this.parseTerm(termString.substring(0, eq - 1));
				rel = RelationType.LESS_EQUAL;
				break;

			case '>':
				lhs = this.parseTerm(termString.substring(0, eq - 1));
				rel = RelationType.GREATER_EQUAL;
				break;

			case '!':
				lhs = this.parseTerm(termString.substring(0, eq - 1));
				rel = RelationType.NOT_EQUAL;
				break;

			default:
				lhs = this.parseTerm(termString.substring(0, eq));
				rel = RelationType.EQUAL;
			}
		} else {
			String[] lt = termString.split("<");
			if (lt.length == 2) {
				lhs = this.parseTerm(lt[0]);
				rel = RelationType.LESS_THAN;
				rhs = this.parseTerm(lt[1]);
			} else {
				String[] gt = termString.split(">");
				assert lt.length == 1 && gt.length == 2;
				lhs = this.parseTerm(gt[0]);
				rel = RelationType.GREATER_THAN;
				rhs = this.parseTerm(gt[1]);
			}
		}
		return new Relation(lhs, rel, rhs);
	}

	/**
	 * Generates a Formula from a string representation.
	 * 
	 * @param str
	 * @return
	 */
	public Term parseTerm(String str) {
		return this.splitOnPlusMinus(str);
	}

	/**
	 * Splits a string representation on plus/minus and parses the results. Treats
	 * round brackets as atoms. Since these operations have the lowest precedence,
	 * this method is the entry point for parsing.
	 * 
	 * @param termString
	 * @return
	 */
	private Term splitOnPlusMinus(String termString) {
		AdditionTerm sum = new AdditionTerm();
		int start = 0;
		boolean addContext = true;
		Term tmp;
		String parsee;
		for (int i = 0; i < termString.length(); i++) {
			switch (termString.charAt(i)) {
			case '(':
				i = closingBracket(termString, i);
				break;

			case '+':
				parsee = termString.substring(start, i);
				if (!parsee.isEmpty()) {
					tmp = this.splitOnTimesDivision(parsee);
					if (addContext) {
						sum.add(tmp);
					} else {
						sum.subtract(tmp);
					}
				}
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				addContext = true;
				break;

			case '-':
				parsee = termString.substring(start, i);
				if (!parsee.isEmpty()) {
					tmp = this.splitOnTimesDivision(parsee);
					if (addContext) {
						sum.add(tmp);
					} else {
						sum.subtract(tmp);
					}
				}
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				addContext = false;
				break;

			default:
				break;
			}
		}

		tmp = this.splitOnTimesDivision(termString.substring(start));
		if (addContext) {
			sum.add(tmp);
		} else {
			sum.subtract(tmp);
		}

		// don't encapsulate, if unnecessary
		if (sum.getSubtrahends().isEmpty() && sum.getSummands().size() == 1) {
			return sum.getSummands().get(0);
		}

		return sum;
	}

	/**
	 * Splits the given string on occurences of "*" and "/", while treating
	 * bracketed terms as atoms. Returns a Term representing the
	 * division/multiplication described in in the string.
	 * 
	 * @param termString
	 * @return
	 */
	private Term splitOnTimesDivision(String termString) {
		MultiplicationTerm product = new MultiplicationTerm();
		int start = 0;
		boolean timesContext = true;
		Term tmp;
		for (int i = 0; i < termString.length(); i++) {
			switch (termString.charAt(i)) {
			case '(':
				i = closingBracket(termString, i);
				break;

			case '*':
				tmp = this.splitOnExponentiation(termString.substring(start, i));
				if (timesContext) {
					product.multiplyBy(tmp);
				} else {
					product.dividedBy(tmp);
				}
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				timesContext = true;
				break;

			case '/':
				tmp = this.splitOnExponentiation(termString.substring(start, i));
				if (timesContext) {
					product.multiplyBy(tmp);
				} else {
					product.dividedBy(tmp);
				}
				assert i + 1 < termString.length(); // operations are followed
													// by operands
				start = i + 1;
				timesContext = false;
				break;

			default:
				break;
			}
		}

		tmp = this.splitOnExponentiation(termString.substring(start));
		if (timesContext) {
			product.multiplyBy(tmp);
		} else {
			product.dividedBy(tmp);
		}

		// don't encapsulate, if unnecessary
		if (product.getDivisors().isEmpty() && product.getFactors().size() == 1) {
			return product.getFactors().get(0);
		}

		return product;
	}

	/**
	 * Splits the given string on occurences of "^", while treating bracketed terms
	 * as atoms. Handles expressions like a^b^c. Returns a Term representing the
	 * exponential described in in the string. Exponentials have the highest
	 * precedence, therefore splitting on them must be done last.
	 * 
	 * @param termString
	 * @return
	 */
	private Term splitOnExponentiation(String termString) {
		ArrayList<String> splits = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < termString.length(); i++) {
			switch (termString.charAt(i)) {
			case '(':
				i = closingBracket(termString, i);
				break;

			case '^':
				splits.add(termString.substring(start, i));
				i++;
				start = i;
				break;

			default:
				break;
			}
		}

		splits.add(termString.substring(start));

		Term base = parseAtomicTerm(splits.remove(0));
		// don't encapsulate, if unnecessary
		if (splits.isEmpty()) {
			return base;
		}

		ExponentTerm exponentiation = new ExponentTerm(base);
		for (String str : splits) {
			exponentiation.powerOf(this.parseAtomicTerm(str));
		}
		return exponentiation;
	}

	/**
	 * Parses "atomic" Terms: Bracketed terms, Constants and Variables.
	 * 
	 * @param str
	 * @return
	 */
	private Term parseAtomicTerm(String str) {
		if (str.startsWith("(")) {
			assert closingBracket(str, 0) == str.length() - 1;
			String unwrapped = str.substring(1, str.length() - 1);
			return new BracketTerm(this.splitOnPlusMinus(unwrapped));
		}

		assert !str.contains("|") && !str.contains("&") && !str.contains("(") && !str.contains(")")
				&& !str.contains("+") && !str.contains("-") && !str.contains("*") && !str.contains("/")
				&& !str.contains("^");

		Term atom = this.kyxp.getTermByName(str);
		if (atom != null) {
			return atom;
		}

		return AtomicTermParser.parseToRealTerm(str);
	}

	/**
	 * Starts from the opening bracket at the given position, finds the
	 * corresponding closing bracket and returns its position.
	 * 
	 * @param str
	 * @param start
	 * @return
	 */
	private static int closingBracket(String str, int start) {
		if (str.length() < 2 || str.length() <= start) {
			System.out.println("[ERR] Malformed string: " + str + "\n Finding ')' at " + start + " impossible!");
			return Integer.MIN_VALUE;
		}

		if (str.charAt(start) != '(') {
			return -1;
		}

		int braceStatus = 1;
		for (int i = start + 1; i < str.length(); i++) {
			if (str.charAt(i) == '(') {
				braceStatus++;
			} else if (str.charAt(i) == ')') {
				braceStatus--;
			}

			if (braceStatus == 0) {
				return i;
			}
		}

		System.out.println("[ERR] String: " + str + "\n Contains an opening, but no closing bracket!");
		return Integer.MAX_VALUE;
	}

	/**
	 * A simple test case for this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FileReader fr = null;
		try {
			fr = new FileReader("G:\\keymaerax\\09-ping-pong.kyx");
		} catch (Exception e) {
			e.printStackTrace();
		}
		KYXParser kyxParser = new KYXParser(fr);
		FormulaParser formParser = new FormulaParser(kyxParser);
		Term parsed = formParser.parseTerm("5+1/2-v");
		System.out.println(parsed);
		Formula parse = formParser.parse("(x>5+1/2-v | 2*x>2*5-v^2&v<1) & v>=0".replaceAll(" ", ""));
		System.out.println(parse);
	}
}
