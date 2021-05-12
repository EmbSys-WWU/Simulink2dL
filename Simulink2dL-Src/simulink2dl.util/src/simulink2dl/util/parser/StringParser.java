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

import java.util.LinkedList;
import java.util.List;

public class StringParser {

	public static double parseScalar(String scalarString) {
		if (scalarString.contains("[")) {
			scalarString = scalarString.replace("[", "");
			scalarString = scalarString.replace("]", "");
		}
		return Double.parseDouble(scalarString);
	}

	public static List<Double> parseVector(String vectorString) {
		List<Double> result = new LinkedList<Double>();

		vectorString = vectorString.replace("[", " ");
		vectorString = vectorString.replace("]", " ");
		vectorString = vectorString.replace(";", " ");
		vectorString = vectorString.replace(",", " ");

		String elements[] = vectorString.split(" ");
		for (String element : elements) {
			if (element.isEmpty()) {
				continue;
			}
			result.add(parseScalar(element));
		}

		return result;
	}

	public static boolean isScalar(String toTest) {
		if (!toTest.contains("[")) {
			return true;
		}
		// TODO the following is also a scalar:
		// "[1;]"

		return false;
	}

	public static boolean isDigit(char toTest) {
		switch (toTest) {
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
			return true;
		}
		return false;
	}

	public static boolean isVector(String toTest) {
		if (!toTest.contains(";")) {
			return true;
		}
		// TODO the following is also a vector:
		// "[1 2 3;]"
		return false;
	}

	public static boolean containsNumber(String inputs) {
		for (int i = 0; i < inputs.length(); i++) {
			if (isDigit(inputs.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * checks whether the String to test is a number
	 * mainly used to differentiate between numerical and workspace constants
	 * TODO: implement method to check validity of non-numerical constant names
	 */
	public static boolean isNumber(String toTest) {
	    try {
	        Double.parseDouble(toTest);
	    } catch (NumberFormatException e) {
	        return false;
	    }
		return true;
	}
}
