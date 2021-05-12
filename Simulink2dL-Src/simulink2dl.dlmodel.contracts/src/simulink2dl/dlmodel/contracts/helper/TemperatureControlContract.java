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

import simulink2dl.dlmodel.contracts.GhostVariable;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.RealTerm;

public class TemperatureControlContract extends HybridContract {

	public TemperatureControlContract(String serviceName, Constant smallstep) {

		super();

		Variable heatOn = new Variable("R","heatOn");
		Variable heatOff = new Variable("R","heatOff");
		inputs.add(heatOn);
		inputs.add(heatOff);

		Variable tOut = new Variable("R","tOut");
		GhostVariable tOutPrevious = new GhostVariable(tOut);
		ghostVariables.add(tOutPrevious);
		outputs.add(tOut);

		// contract specific variables and constants
		// constant for desired temperature
		Constant desiredTemp = new Constant("R", "DESIREDTEMP" + serviceName);
		this.addConstant(desiredTemp);

		// referenced constant for smallstep size
		this.addReferencedConstant(smallstep);

		// create assumptions
		// bounds for SMALLSTEPSIZE
		this.addAssumption(new Relation(smallstep, Relation.RelationType.LESS_EQUAL, new RealTerm("0.01")));
		this.addAssumption(new Relation(smallstep, Relation.RelationType.GREATER_EQUAL, new RealTerm("0.0")));

		// bounds for previous output
		AdditionTerm lowerBoundPrevious = new AdditionTerm();
		lowerBoundPrevious.add(desiredTemp);
		lowerBoundPrevious.subtract(new RealTerm("1.0"));
		this.addAssumption(new Relation(lowerBoundPrevious, Relation.RelationType.LESS_EQUAL, tOutPrevious));

		AdditionTerm upperBoundPrevious = new AdditionTerm();
		upperBoundPrevious.add(desiredTemp);
		upperBoundPrevious.add(new RealTerm("1.0"));
		this.addAssumption(new Relation(tOutPrevious, Relation.RelationType.LESS_EQUAL, upperBoundPrevious));

		// bounds for heatOn
		this.addAssumption(new Relation(heatOn, Relation.RelationType.GREATER_EQUAL, new RealTerm(0)));
		this.addAssumption(new Relation(heatOn, Relation.RelationType.LESS_EQUAL, new RealTerm(10)));

		// bounds for heatOff
		this.addAssumption(new Relation(heatOff, Relation.RelationType.GREATER_EQUAL, new RealTerm(-10)));
		this.addAssumption(new Relation(heatOff, Relation.RelationType.LESS_EQUAL, new RealTerm(0)));

		// create guarantees
		// bounds for output
		AdditionTerm lowerBound = new AdditionTerm();
		lowerBound.add(desiredTemp);
		lowerBound.subtract(new RealTerm("1.0"));
		this.addGuarantee(new Relation(lowerBound, Relation.RelationType.LESS_EQUAL, tOut));

		AdditionTerm upperBound = new AdditionTerm();
		upperBound.add(desiredTemp);
		upperBound.add(new RealTerm("1.0"));
		this.addGuarantee(new Relation(tOut, Relation.RelationType.LESS_EQUAL, upperBound));
	}

}
