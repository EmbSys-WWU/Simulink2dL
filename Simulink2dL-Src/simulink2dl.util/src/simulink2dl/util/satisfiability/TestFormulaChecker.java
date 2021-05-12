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
package simulink2dl.util.satisfiability;

import java.io.FileReader;

import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.parser.FormulaParser;
import simulink2dl.dlmodel.parser.KYXParser;

public class TestFormulaChecker {

	public static void main(String[] args) {
		FileReader fr = null;
		try {
			fr = new FileReader("G:\\keymaerax\\09-ping-pong.kyx");
		} catch (Exception e) {
			e.printStackTrace();
		}
		KYXParser kyxParser = new KYXParser(fr);
		FormulaParser formParser = new FormulaParser(kyxParser);

		Formula parsed = formParser.parse("4<v&v<4");
		System.out.println(parsed);
		FormulaChecker fc = new FormulaChecker();
		System.out.println(fc.checkSingleFormula(parsed));

		System.out.println("");
		parsed = formParser.parse("4+1<v&v<4");
		System.out.println(parsed);
		System.out.println(fc.checkSingleFormula(parsed));

		System.out.println("");
		parsed = formParser.parse("4-1<v&v<4");
		System.out.println(parsed);
		System.out.println(fc.checkSingleFormula(parsed));

		System.out.println("");
		parsed = formParser.parse("4*v<4&v<4");
		System.out.println(parsed);
		System.out.println(fc.checkSingleFormula(parsed));

		System.out.println("");
		parsed = formParser.parse("4*v<4&v<=1&v>0.25");
		System.out.println(parsed);
		System.out.println(fc.checkSingleFormula(parsed));
	}

}
