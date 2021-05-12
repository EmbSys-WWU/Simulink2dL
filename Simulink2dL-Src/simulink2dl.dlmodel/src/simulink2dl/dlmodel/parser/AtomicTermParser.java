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

import java.text.NumberFormat;
import java.text.ParsePosition;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.RealTerm;

/**
 * A collection of functions to parse string representations of reals, Constants
 * and Variables.
 * 
 * @author nick
 *
 */
public class AtomicTermParser {

	/**
	 * Parses a string to a Constant. Set ("R A = (5)") and unset ("R A") Constant
	 * strings are supported.
	 * 
	 * @param str String representation without the "." at the end.
	 * @return
	 */
	static Constant parseToConstant(String str) {
		assert !str.contains("|") && !str.contains("&") && !str.contains("!");

		if (str.charAt(1) != ' ') {
			System.out.println("[ERR] Unexpected format found: " + str);
			return null;
		}

		String type = str.substring(0, 1);
		str = str.substring(2).replaceAll("\\s", ""); // remove spaces
		String[] def = str.split("=");
		String name = def[0];

		if (def.length == 2) {
			String valueStr = def[1];
			valueStr = valueStr.substring(1, valueStr.length() - 1);
			assert !valueStr.contains("(") && !valueStr.contains(")");
			RealTerm value = parseToRealTerm(valueStr);
			return new Constant(type, name, value);
		}

		return new Constant(type, name);
	}

	/**
	 * Parses a string to a Variable.
	 * 
	 * @param str
	 * @return
	 */
	static Variable parseToVariable(String str) {
		assert !str.contains("|") && !str.contains("&") && !str.contains("!");

		if (str.charAt(1) != ' ') {
			System.out.println("[ERR] Unexpected format found: " + str);
			return null;
		}

		String type = str.substring(0, 1);
		str = str.substring(2).replaceAll("\\s", ""); // remove spaces
		String name = str;

		return new Variable(type, name);
	}

	/**
	 * Parses a string into a RealTerm.
	 * 
	 * @param str String representation of a number.
	 * @return
	 */
	static RealTerm parseToRealTerm(String str) {
		assert !str.contains("|") && !str.contains("&") && !str.contains("(") && !str.contains(")")
				&& !str.contains("+") && !str.contains("-") && !str.contains("*") && !str.contains("/")
				&& !str.contains("^");

		if (!isNumeric(str)) { // Only print NaN strings!
			System.out.println(
					"[INFO] Unable to find: '" + str + "' in Constants or Variables. RealTerm created instead!");
			return null;
		}
		return new RealTerm(str); // other Term classes refer to StringTerms, too, but this should probably be a
									// RealTerm
	}

	/**
	 * Checks whether a given string is representing a number.
	 * 
	 * @param str
	 * @return
	 */
	static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	public static void main(String[] args) {
		System.out.println(parseToConstant("R A=(5)"));
	}
}
