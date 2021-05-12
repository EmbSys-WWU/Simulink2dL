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

import simulink2dl.dlmodel.contracts.ConcurrentContract;
import simulink2dl.dlmodel.contracts.GhostVariable;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;

public class RLRobotContract extends ConcurrentContract{
		
		public RLRobotContract(String serviceName) {
			super();
			Variable XGoal = new Variable("R","RLXGoal");
			Variable YGoal = new Variable("R","RLYGoal");
			inputs.add(XGoal);
			inputs.add(YGoal);
			
			//SamplingTime
			Constant STEPTS = new Constant("R", "STEPTSRL");
			constants.add(STEPTS);
			
			//Pos
			Variable XRL = new Variable("R","XRL");
			Variable YRL = new Variable("R","YRL");
			outputs.add(XRL);
			outputs.add(YRL);
	
			//ghost variables for time
			Variable simTime = new Variable("R", "simTime");
			GhostVariable simTimeOld = new GhostVariable(simTime);

			AdditionTerm TDiffCalc = new AdditionTerm(simTime); TDiffCalc.subtract(simTimeOld);
			GhostVariable TDiff = new GhostVariable("tDiff", TDiffCalc);
			ghostVariables.add(TDiff);
			ghostVariables.add(simTimeOld);
		
			//Arrived
			Variable Arrived = new Variable("R","ArrivedRL");
			outputs.add(Arrived);
			
			//ghost variables for position
			GhostVariable XRLOld = new GhostVariable(XRL);
			GhostVariable YRLOld = new GhostVariable(YRL);
			
			serviceName=serviceName.replace("RLService", "");
			serviceName=serviceName.replace("RLRobot", "");
			if(serviceName.equals("6")){
				String[] opponents = {"A", "B", "C", "D", "E", "F"};
				for (String opp : opponents) {
					getContract(opp, TDiff, XRL, YRL, XRLOld, YRLOld, STEPTS);
				}
			} else {
				String[] opponents = {"A", "B"};
				for (String opp : opponents) {
					getContract(opp, TDiff, XRL, YRL, XRLOld, YRLOld, STEPTS);
				}
			}
					
			
			
			
			ghostVariables.add(XRLOld);
			ghostVariables.add(YRLOld);
		}
		
        /** adds contract the following contract for each opponent: 
         *  ?(
         *       (
         *           VMAXA >= 0.0) &
         *         (
         *           STEPTSRL >= 0.0) &
         *         (
         *           ((((XA-XAOld)^2.0)+((YA-YAOld)^2.0))^0.5) <= (VMAXA*tDiff))
         *       ->(((
         *             ((((XAOld-XRL)^2.0)+((YAOld-YRL)^2.0))^0.5) > THRESHOLDA) |
         *           ((
         *               XRL = XRLOld) &
         *             (
         *               YRL = YRLOld)))));
         */
		private void getContract(String oppName, Variable tDiff, Variable XRL, Variable YRL, Variable XRLOld, Variable YRLOld, Constant STEPTS) {
			Variable XOpp = new Variable("R",oppName+"X");
			Variable YOpp = new Variable("R",oppName+"Y");
			
			inputs.add(XOpp);
			inputs.add(YOpp);
			
			Constant THRESHOLD = new Constant("R", "THRESHOLD"+oppName);
			Constant VMAX = new Constant("R", "VMAX"+oppName);
			constants.add(THRESHOLD);
			
			GhostVariable XOppOld = new GhostVariable(XOpp);
			GhostVariable YOppOld = new GhostVariable(YOpp);
			ghostVariables.add(XOppOld);
			ghostVariables.add(YOppOld);
			Conjunction constantAssumptions = new Conjunction( new Relation(VMAX, GREATER_EQUAL, ZERO), new Relation(STEPTS, GREATER_EQUAL, ZERO));
			Relation velocity = new Relation(RLContractUtil.getEuclideanDistance(XOpp,YOpp,XOppOld,YOppOld), LESS_EQUAL, new MultiplicationTerm(VMAX, tDiff));
			
			Relation distGreaterThreshold = new Relation(RLContractUtil.getEuclideanDistance(XOppOld,YOppOld,XRL,YRL), GREATER_THAN, THRESHOLD);
			Conjunction oldPosEqNewPos = new Conjunction(new Relation(XRL, EQUAL, XRLOld), new Relation(YRL, EQUAL, YRLOld));
			Disjunction guarantee = new Disjunction(distGreaterThreshold, oldPosEqNewPos);
			
			setAssumptionGuaranteePair(new Conjunction(constantAssumptions, velocity), guarantee);
			
		}
}
