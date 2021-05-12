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
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Equivalence;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;

public class CounterContract extends SimpleContract {

	public CounterContract(String serviceName) {

		super();

		// get output variables
		PortIdentifier counterOut = new PortIdentifier("#output0");

		// add new intermediate variable
		Variable counterOutNext = new Variable("R", "counterOutNext");
		this.addIntermediateVariable(counterOutNext);

		SimpleContract newContract = new SimpleContract();

		// constants

		// create assumptions
		// always true
		newContract.addAssumption(new BooleanConstant(true));

		// create guarantees
		// possible values for counterOut
		Disjunction counterOutValues = new Disjunction();
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-9")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-8")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-7")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-6")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-5")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-4")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-3")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-2")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("-1")));
		counterOutValues.addElement(new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("0")));

		newContract.addGuarantee(counterOutValues);

		// possible values for counterOutNext
		Disjunction counterOutNextValues = new Disjunction();
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-9")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-8")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-7")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-6")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-5")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-4")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-3")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-2")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-1")));
		counterOutNextValues.addElement(new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("0")));

		newContract.addGuarantee(counterOutNextValues);

		// relation between counterOut and counterOutNext
		// (counterOutNext = 0 <-> counterOut = 9)
		Equivalence test = new Equivalence(
				new Relation(counterOutNext, Relation.RelationType.EQUAL, new RealTerm("-9")),
				new Relation(counterOut, Relation.RelationType.EQUAL, new RealTerm("0")));

		newContract.addGuarantee(test);

		// (counterOutNext != 0 <-> counterOut = counterOutNext-1)
		Equivalence test2 = new Equivalence(
				new Relation(counterOutNext, Relation.RelationType.NOT_EQUAL, new RealTerm("-9")),
				new Relation(counterOut, Relation.RelationType.EQUAL, new Relation(counterOut,
						Relation.RelationType.NOT_EQUAL, new AdditionTerm(counterOut, new RealTerm("-9")))));

		newContract.addGuarantee(test2);
	}

}
