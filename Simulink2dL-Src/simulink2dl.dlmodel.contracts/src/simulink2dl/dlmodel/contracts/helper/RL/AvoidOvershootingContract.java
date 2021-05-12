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
package simulink2dl.dlmodel.contracts.helper.RL;

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.RealTerm;

public class AvoidOvershootingContract  extends HybridContract{
	
	public AvoidOvershootingContract(String serviceName) {
		super();
		Variable VX = new Variable("R","VX");
		Variable VY = new Variable("R","VY");
		Variable DIFFGOALX = new Variable("R","DIFFGOALX");
		Variable DIFFGOALY = new Variable("R","DIFFGOALY");
		Variable TS = new Variable("R","TS");
		inputs.add(VX);
		inputs.add(VY);
		inputs.add(DIFFGOALX);
		inputs.add(DIFFGOALY);
		inputs.add(TS);
		
		Variable VXNOOVERSHOOT = new Variable("R","VXNOOVERSHOOT");
		Variable VYNOOVERSHOOT = new Variable("R","VYNOOVERSHOOT");
		outputs.add(VXNOOVERSHOOT);
		outputs.add(VYNOOVERSHOOT);
		
		Conjunction XLZERO = new Conjunction(new Relation(VX, LESS_THAN, ZERO), new Relation(DIFFGOALX, LESS_THAN, ZERO));
		Conjunction YLZERO = new Conjunction(new Relation(VY, LESS_THAN, ZERO), new Relation(DIFFGOALY, LESS_THAN, ZERO));
		Conjunction XGEZERO = new Conjunction(new Relation(VX, GREATER_EQUAL, ZERO), new Relation(DIFFGOALX, GREATER_EQUAL, ZERO));
		Conjunction YGEZERO = new Conjunction(new Relation(VY, GREATER_EQUAL, ZERO), new Relation(DIFFGOALY, GREATER_EQUAL, ZERO));
		Conjunction VXVYEQZERO = new Conjunction(new Relation(VX, EQUAL, ZERO), new Relation(VY, EQUAL, ZERO));
		Disjunction XSAME = new Disjunction(XLZERO, XGEZERO);
		Disjunction YSAME = new Disjunction(YLZERO, YGEZERO);
		Relation TSGZERO = new Relation(TS, GREATER_THAN, ZERO);
		Conjunction PreserveDir = new Conjunction(XSAME,YSAME);
		Disjunction PRESERVEORZERO = new Disjunction(PreserveDir,VXVYEQZERO);
		Conjunction Assumption = new Conjunction(PRESERVEORZERO,TSGZERO);
		
		ExponentTerm OutputSpeed = new ExponentTerm(new AdditionTerm(new ExponentTerm(VXNOOVERSHOOT,new RealTerm(2)),new ExponentTerm(VYNOOVERSHOOT,new RealTerm(2))),new RealTerm(0.5));
		ExponentTerm InputSpeed = new ExponentTerm(new AdditionTerm(new ExponentTerm(VX,new RealTerm(2)),new ExponentTerm(VY,new RealTerm(2))),new RealTerm(0.5));
		Relation SpeedLower = new Relation(OutputSpeed, LESS_EQUAL, InputSpeed);
		
		setAssumptionGuaranteePair(Assumption, SpeedLower);
		
	}
}