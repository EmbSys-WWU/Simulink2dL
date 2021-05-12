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
package simulink2dl.dlmodel;

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicRepetition;
import simulink2dl.dlmodel.operator.BoxModality;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;

public class DLModelDefaultStructure extends DLModel {

	protected Conjunction initialConditions;

	private BoxModality outerModality;

	private NondeterministicRepetition loop;

	protected HybridProgramCollection behavior;

	public DLModelDefaultStructure() {
		super();

		// create the default problem structure
		// initial condition
		// ->
		// [behavior]true
		initialConditions = new Conjunction();
		behavior = new HybridProgramCollection();
		loop = new NondeterministicRepetition(behavior);
		outerModality = new BoxModality(loop, new BooleanConstant(true));

		Implication problem = new Implication(initialConditions, outerModality);
		this.setProblem(problem);
	}

	public void addInitialCondition(Operator newElement) {
		if(!initialConditions.containsTerm(newElement))
		initialConditions.addElement(newElement);
	}

	public void addBehavior(HybridProgram newBehavior) {
		if(newBehavior!=null)
		behavior.addElement(newBehavior);
	}

	public void addBehaviorFront(HybridProgram newBehavior) {
		behavior.addElementFront(newBehavior);
	}

	public HybridProgramCollection getBehavior() {
		return behavior;
	}

	public NondeterministicRepetition getLoop() {
		return loop;
	}
}
