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
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;

public class OpponentControllerContract extends HybridContract{
	
	public OpponentControllerContract(String serviceName) {
		String oppName = serviceName;
		oppName = oppName.replace("RLService", "");
		oppName = oppName.replace("OpponentController", "");
		Variable X = new Variable("R","X");
		Variable Y = new Variable("R","Y");
		
		Variable XGoal = new Variable("R", "XGoal");
		Variable YGoal = new Variable("R", "YGoal");
		
		Variable XRL = new Variable("R","XRL");
		Variable YRL = new Variable("R","YRL");
		
		Variable MaxVelo = new Variable("R", "MaxVelo");
		Variable TS = new Variable("R", "TS");
		Variable THRESHOLD = new Variable("R", "THRESHOLD");
		
		inputs.add(X);
		inputs.add(Y);
		inputs.add(XGoal);
		inputs.add(YGoal);
		
		inputs.add(XRL);
		inputs.add(YRL);
		
		inputs.add(MaxVelo);
		inputs.add(TS);
		inputs.add(THRESHOLD);
		
		Variable MoveX = new Variable("R", "VX"+oppName);
		Variable MoveY = new Variable("R", "VY"+oppName);
		Variable Arrived = new Variable("R", "Arrived"+oppName);
		outputs.add(MoveX);
		outputs.add(MoveY);
		outputs.add(Arrived);
		
		Relation VeloGreaterZero = new Relation(MaxVelo, GREATER_THAN, ZERO);
		Relation TSGreaterZero = new Relation(TS, GREATER_THAN, ZERO);
		Relation THRESHOLDGreaterEQZero = new Relation(THRESHOLD, GREATER_EQUAL, ZERO);
		addAssumption(new Conjunction(VeloGreaterZero,TSGreaterZero,THRESHOLDGreaterEQZero));
		
		//MaxSpeed
		ExponentTerm speed = new ExponentTerm(new AdditionTerm(new ExponentTerm(MoveX,new RealTerm(2)),new ExponentTerm(MoveY,new RealTerm(2))),new RealTerm(0.5));
		Relation maxSpeed = new Relation(speed, LESS_EQUAL, MaxVelo);
		addGuarantee(maxSpeed);
		
		//Safety of moves
		createSafetyContract(MoveX, MoveY, X, Y, XRL, YRL, THRESHOLD);
		
	}
	/**
	 * Create contract:
	 * (
     *         VMAXA >= 0.0) &
     *       (
     *         STEPTSA > 0.0) &
     *       (
     *         THRESHOLDA >= 0.0)
     *     ->((
     *         (((VXA^2.0)+(VYA^2.0))^0.5) <= VMAXA) &
     *       (
     *         
     *           ((((XRLHold-XAHold)^2.0)+((YRLHold-YAHold)^2.0))^0.5) <= THRESHOLDA
     *         ->(
     *           (((XRLHold-XAHold)*VXA)+((YRLHold-YAHold)*VYA)) <= 0.0))));
	 */
	private void createSafetyContract(Term MoveX, Term MoveY, Term X, Term Y, Term XRL, Term YRL, Term SafetyThreshold) {
		AdditionTerm DiffX = new AdditionTerm(XRL); DiffX.subtract(X);
		AdditionTerm DiffY = new AdditionTerm(YRL); DiffY.subtract(Y);
		Term Distance = new ExponentTerm((new AdditionTerm(new ExponentTerm(DiffX,new RealTerm(2)),new ExponentTerm(DiffY,new RealTerm(2)))), new RealTerm(0.5));
		System.out.println(Distance.toString());
		Relation stopCondition = new Relation(Distance, LESS_EQUAL, SafetyThreshold);
		
		MultiplicationTerm productX = new MultiplicationTerm(DiffX,MoveX);
		MultiplicationTerm productY = new MultiplicationTerm(DiffY,MoveY);
		AdditionTerm dotProcuct = new AdditionTerm(productX,productY);
		Relation safeMove = new Relation(dotProcuct, LESS_EQUAL, ZERO);
		
		addGuarantee(new Implication(stopCondition,safeMove));
		
	}
}
