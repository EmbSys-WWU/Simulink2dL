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
package simulink2dl.util.parser;

import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.BracketTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.util.PluginLogger;

/**
 * This parser takes a String and returns a representation of the content as a
 * dL-Model Term (simulink2dl.dlmodel).
 * 
 * @author Timm Liebrenz
 *
 */
public class StringToTerm {

	/**
	 * For debugging purpose
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String toParse = "0 + 2 * 2 - #out1";

		Term result = parseString(toParse);

		PluginLogger.info("input : " + toParse);
		PluginLogger.info("result: " + result.toString());
	}

	/**
	 * Parses the given String and returns a representation as
	 * simulink2dl.dlmodel.term.Term
	 * 
	 * @param toParse
	 * @return
	 */
	public static Term parseString(String toParse) {

		// pre-processing
		toParse = removeWhitespaces(toParse);

		// parse string
		return parseInternal(toParse);
	}

	private static String removeWhitespaces(String toParse) {
		return toParse.replace(" ", "");
	}

	private enum Operator {
		SIMPLE, BRACKET, ADDITION, MULTIPLICATION, EXPONENT
	}

	private static Term parseInternal(String toParse) {
		Operator operator = getOuterOperator(toParse);

		switch (operator) {
		case SIMPLE:
			return parseSimple(toParse);
		case BRACKET:
			return parseBracket(toParse);
		case ADDITION:
			return parseAddition(toParse);
		case MULTIPLICATION:
			return parseMultiplication(toParse);
		case EXPONENT:
			return parseExponent(toParse);
		default:
			PluginLogger.error("cannot parse String to Term: " + toParse);
			return new StringTerm(toParse);
		}

	}

	private static Operator getOuterOperator(String toParse) {
		Operator result = Operator.SIMPLE;
		if (toParse.startsWith("(")) {
			result = Operator.BRACKET;
		}

		int level = 0;

		for (int i = 0; i < toParse.length(); i++) {
			char c = toParse.charAt(i);

			if (level == 0) {
				switch (c) {
				case '(':
					level++;
					break;
				case '+':
				case '-':
					result = Operator.ADDITION;
					break;
				case '*':
				case '/':
					if (result != Operator.ADDITION) {
						result = Operator.MULTIPLICATION;
					}
					break;
				case '^':
					if (result != Operator.ADDITION | result != Operator.MULTIPLICATION) {
						result = Operator.EXPONENT;
					}
					break;
				default:
					// empty
				}
			} else {
				// ignore everything in brackets
				switch (c) {
				case '(':
					level++;
					break;
				case ')':
					level--;
					break;
				default:
					// empty
				}
			}
		}

		return result;
	}

	private static BracketTerm parseBracket(String toParse) {
		return new BracketTerm(parseInternal(toParse.substring(1, toParse.length() - 1)));
	}

	private static AdditionTerm parseAddition(String toParse) {
		AdditionTerm result = new AdditionTerm();

		int i = 0;
		boolean subtract = false;

		if (toParse.startsWith("-")) {
			subtract = true;
			i++;
		}

		int currentStart = i;
		int level = 0;

		for (; i < toParse.length(); i++) {
			char c = toParse.charAt(i);

			if (level == 0) {
				switch (c) {
				case '(':
					level++;
					break;
				case '+':
					if (subtract) {
						result.subtract(parseInternal(toParse.substring(currentStart, i)));
					} else {
						result.add(parseInternal(toParse.substring(currentStart, i)));
					}
					currentStart = i + 1;
					subtract = false;
					break;
				case '-':
					if (subtract) {
						result.subtract(parseInternal(toParse.substring(currentStart, i)));
					} else {
						result.add(parseInternal(toParse.substring(currentStart, i)));
					}
					currentStart = i + 1;
					subtract = true;
					break;
				default:
					// empty
				}
			} else {
				// ignore everything in brackets
				switch (c) {
				case '(':
					level++;
					break;
				case ')':
					level--;
					break;
				default:
					// empty
				}
			}
		}

		// add last element
		if (subtract) {
			result.subtract(parseInternal(toParse.substring(currentStart, i)));
		} else {
			result.add(parseInternal(toParse.substring(currentStart, i)));
		}
		return result;
	}

	private static MultiplicationTerm parseMultiplication(String toParse) {
		MultiplicationTerm result = new MultiplicationTerm();

		int i = 0;
		boolean divide = false;

		int currentStart = i;
		int level = 0;

		for (; i < toParse.length(); i++) {
			char c = toParse.charAt(i);

			if (level == 0) {
				switch (c) {
				case '(':
					level++;
					break;
				case '*':
					if (divide) {
						result.dividedBy(parseInternal(toParse.substring(currentStart, i)));
					} else {
						result.multiplyBy(parseInternal(toParse.substring(currentStart, i)));
					}
					currentStart = i + 1;
					divide = false;
					break;
				case '/':
					if (divide) {
						result.dividedBy(parseInternal(toParse.substring(currentStart, i)));
					} else {
						result.multiplyBy(parseInternal(toParse.substring(currentStart, i)));
					}
					currentStart = i + 1;
					divide = true;
					break;
				default:
					// empty
				}
			} else {
				// ignore everything in brackets
				switch (c) {
				case '(':
					level++;
					break;
				case ')':
					level--;
					break;
				default:
					// empty
				}
			}
		}

		// add last element
		if (divide) {
			result.dividedBy(parseInternal(toParse.substring(currentStart, i)));
		} else {
			result.multiplyBy(parseInternal(toParse.substring(currentStart, i)));
		}
		return result;
	}

	private static ExponentTerm parseExponent(String toParse) {
		ExponentTerm result = new ExponentTerm();

		int i = 0;
		boolean base = true;

		int currentStart = i;
		int level = 0;

		for (; i < toParse.length(); i++) {
			char c = toParse.charAt(i);

			if (level == 0) {
				switch (c) {
				case '(':
					level++;
					break;
				case '^':
					if (base) {
						result.setBase(parseInternal(toParse.substring(currentStart, i)));
					} else {
						result.powerOf(parseInternal(toParse.substring(currentStart, i)));
					}
					currentStart = i + 1;
					base = false;
					break;
				default:
					// empty
				}
			} else {
				// ignore everything in brackets
				switch (c) {
				case '(':
					level++;
					break;
				case ')':
					level--;
					break;
				default:
					// empty
				}
			}
		}

		// add last element
		if (base) {
			result.setBase(parseInternal(toParse.substring(currentStart, i)));
		} else {
			result.powerOf(parseInternal(toParse.substring(currentStart, i)));
		}
		return result;
	}

	private static Term parseSimple(String toParse) {
		if (isRealNumber(toParse)) {
			double real = Double.parseDouble(toParse);
			return new RealTerm(real);
		} else if (isReplaceable(toParse)) {
			ReplaceableTerm result;
			return new PortIdentifier(toParse);
		}
		PluginLogger.error("Could not parse String: \"" + toParse + "\"");
		return new StringTerm(toParse);
	}

	private static boolean isRealNumber(String toParse) {
		// variable to store the information if a delimiter ('.') is in the
		// given String
		boolean delimiterPresent = false;
		boolean leadingNumber = false;
		boolean isFirst = true;
		for (char c : toParse.toCharArray()) {
			switch (c) {
			// for digits, continue the analysis
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				leadingNumber = true;
				break;
			// for a delimiter, check if it is the only one
			case '.':
				// at least one digit must be in front of the delimiter
				if (!leadingNumber) {
					return false;
				}
				// only one delimiter is allowed
				if (!delimiterPresent) {
					delimiterPresent = true;
				} else {
					return false;
				}
				break;
			// the first char can be a sign ('-')
			case '-':
				if (!isFirst) {
					return false;
				}
				break;
			// for all other chars, this is not a number
			default:
				return false;
			}
			isFirst = false;
		}
		// at least one digit must be in the number
		return leadingNumber;
	}

	private static boolean isReplaceable(String toParse) {
		return toParse.startsWith("#");
	}

}
