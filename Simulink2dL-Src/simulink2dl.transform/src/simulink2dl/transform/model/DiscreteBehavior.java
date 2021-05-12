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
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.util.parser.StringParser;

public class DiscreteBehavior {

	private String stepSize;
	private Constant stepSizeConstant;

	private Variable stepClock;

	private HybridProgramCollection onStepOutputAssignmnets;

	private HybridProgramCollection onStepBehavior;

	private ConditionalChoice stepOutputChoice;
	private ConditionalChoice stepChoice;

	public DiscreteBehavior(String stepSize) {

		this.stepSize = stepSize;
		
		//remove dot from numeric stepsize names to avoid invalid KeYmaera syntax
		if(StringParser.isNumber(stepSize)) {
			stepSize = stepSize.replace(".", "");
		}
		this.stepSizeConstant = new Constant("R", "STEP"+stepSize);

		this.stepClock = new Variable("R", "stepClock"+stepSize);

		// create nondeterministic choice to describe step behavior
		Relation stepTrue = new Relation(stepClock, RelationType.GREATER_EQUAL, stepSizeConstant);
		Relation stepFalse = stepTrue.createNegation();

		// output assignments
		this.onStepOutputAssignmnets = new HybridProgramCollection();

		HybridProgramCollection offStepOutputAssignmnets = new HybridProgramCollection();

		this.stepOutputChoice = new ConditionalChoice();
		this.stepOutputChoice.addChoice(stepTrue, onStepOutputAssignmnets);
		this.stepOutputChoice.addChoice(stepFalse, offStepOutputAssignmnets);

		// case step
		this.onStepBehavior = new HybridProgramCollection();

		// case no step
		HybridProgramCollection offStepBehavior = new HybridProgramCollection();

		this.stepChoice = new ConditionalChoice();
		this.stepChoice.addChoice(stepTrue, onStepBehavior);
		this.stepChoice.addChoice(stepFalse, offStepBehavior);
	}

	public Constant getStepSizeConstant() {
		return stepSizeConstant;
	}

	public Variable getClockVariable() {
		return stepClock;
	}
	
	public String getStepSize() {
		return stepSize;
	}
	
	public ConditionalChoice getStepChoice() {
		return stepChoice;
	}

	public boolean isStepTime(String toCompare) {
		return stepSize.equals(toCompare);
	}

	public void addBehavior(HybridProgram newBehavior) {
		onStepBehavior.addElement(newBehavior);
	}

	public void addOutputUpdate(HybridProgram newBehavior) {
		onStepOutputAssignmnets.addElementFront(newBehavior);
	}

	protected void addStepTimeReset() {
		onStepBehavior.addElementFront(new DiscreteAssignment(stepClock, new RealTerm(0.0)));
	}

	public void addToModel(DLModelSimulink dlModel) {
		addStepTimeReset();

		dlModel.addConstant(stepSizeConstant);
		//Check whether the stepSize is a number
		//Values for stepSizes represented by variables have to be filled in manually
		if(StringParser.isNumber(stepSize)) {
			Relation initCondition = new Relation(stepSizeConstant, RelationType.EQUAL, new RealTerm(stepSize));
			dlModel.addInitialCondition(initCondition);
		}

		//do not perform discrete action on start, instead set initial conditions and start with stepClock = 0
		Relation initCondition = new Relation(stepClock, RelationType.EQUAL, new RealTerm(0.0));

		dlModel.addVariable(stepClock);
		dlModel.addInitialCondition(initCondition);
		dlModel.addContinuousEvolution(stepClock, new RealTerm(1.0));

		dlModel.addBehaviorFront(stepOutputChoice);
		dlModel.addBehavior(stepChoice);
	}

}
