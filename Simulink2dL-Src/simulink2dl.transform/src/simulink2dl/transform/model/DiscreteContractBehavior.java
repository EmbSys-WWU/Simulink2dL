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
package simulink2dl.transform.model;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.util.parser.StringParser;

public class DiscreteContractBehavior extends DiscreteBehavior{

	public DiscreteContractBehavior(String stepSize) {
		super(stepSize);
	}
	public void addDiscreteContract(DLModelSimulink dlModel) {
		addStepTimeReset();
		
		Constant stepSizeConstant = getStepSizeConstant();
		Variable stepClock = getClockVariable();
		String stepSize = getStepSize();
		
		dlModel.addConstant(getStepSizeConstant());
		//Check whether the stepSize is a number
		//Values for stepSizes represented by variables have to be filled in manually
		if(StringParser.isNumber(stepSize)) {
			Relation initCondition = new Relation(stepSizeConstant, RelationType.EQUAL, new RealTerm(stepSize));
			dlModel.addInitialCondition(initCondition);
		}

		//do not perform discrete action on start, instead set initial conditions and start with stepClock = 0
		Relation initCondition = new Relation(stepClock, RelationType.EQUAL, new RealTerm(0.0));

		dlModel.addVariable(getClockVariable());
		dlModel.addInitialCondition(initCondition);
		dlModel.addContinuousEvolution(stepClock, new RealTerm(1.0));

		dlModel.addBehavior(getStepChoice());
		dlModel.addDiscreteBehavior(this);
	}
	public void addToModel(DLModelSimulink dlModel) {
		// do nothing
	}
	
}
