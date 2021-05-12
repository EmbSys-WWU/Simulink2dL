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
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;

public class AbstandsrechnerNoOverflowContract extends SimpleContract {

	public AbstandsrechnerNoOverflowContract(String serviceName) {

		super();

		// TODO Correct Gain Contract
		// TODO No Overflow Contract
		PortIdentifier vRelMpsIn = new PortIdentifier("#input0");

		PortIdentifier aktRelDisOut = new PortIdentifier("#output0");
		PortIdentifier aktAbsOut = new PortIdentifier("#output1");

		Variable integrator = new Variable("R", "Integrator" + serviceName);
		this.addIntermediateVariable(integrator);

		// contract specific variables and constants
		// constant for desired temperature
		Constant maxValue = new Constant("R", "MAXVALUE" + serviceName);
		this.addConstant(maxValue);

		// create assumptions
		// bounds for heatOn
		this.addAssumption(new Relation(vRelMpsIn, Relation.RelationType.GREATER_EQUAL, new RealTerm(-10)));
		this.addAssumption(new Relation(vRelMpsIn, Relation.RelationType.LESS_EQUAL, new RealTerm(10)));

		// create guarantees
		// bounds for output
		this.addGuarantee(new Relation(maxValue, Relation.RelationType.LESS_EQUAL, integrator));
		this.addGuarantee(new Relation(integrator, Relation.RelationType.LESS_EQUAL, maxValue));
	}

}
