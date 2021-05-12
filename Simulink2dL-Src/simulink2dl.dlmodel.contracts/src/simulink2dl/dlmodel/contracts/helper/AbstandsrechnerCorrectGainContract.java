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
package simulink2dl.dlmodel.contracts.helper;

import simulink2dl.dlmodel.contracts.SimpleContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;

public class AbstandsrechnerCorrectGainContract extends SimpleContract {

	public AbstandsrechnerCorrectGainContract(String serviceName) {

		super();

		// TODO Correct Gain Contract
		// TODO No Overflow Contract
		PortIdentifier vRelMpsIn = new PortIdentifier("#input0");

		PortIdentifier aktRelDisOut = new PortIdentifier("#output0");
		PortIdentifier aktAbsOut = new PortIdentifier("#output1");

		Variable gainPrevious = new Variable("R", "gainPrevious" + serviceName);
		this.addIntermediateVariable(gainPrevious);
		Variable zeroCrossingPrevious = new Variable("R", "zeroCrossingPrevious" + serviceName);
		this.addIntermediateVariable(zeroCrossingPrevious);

		// contract specific variables and constants
		// constant for desired temperature
		Constant maxValue = new Constant("R", "MAXVALUE" + serviceName);
		this.addConstant(maxValue);

		// create assumptions
		// bounds for heatOn
		this.addAssumption(new BooleanConstant(true));

		Relation relation1A = new Relation(gainPrevious, RelationType.GREATER_THAN, new RealTerm(0));
		Relation relation1B = new Relation(zeroCrossingPrevious, RelationType.EQUAL, new RealTerm(0));
		Relation relation1C = new Relation(aktAbsOut, RelationType.GREATER_EQUAL, new RealTerm(0));
		Conjunction conjunction1 = new Conjunction(relation1A, relation1B);
		Formula guarantee1 = new Implication(conjunction1, relation1C);

		// create guarantees
		// bounds for output
		this.addGuarantee(guarantee1);
	}

}
