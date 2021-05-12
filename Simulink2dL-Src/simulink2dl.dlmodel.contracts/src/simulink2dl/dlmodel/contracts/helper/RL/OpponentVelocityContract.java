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
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;

public class OpponentVelocityContract extends ConcurrentContract{
		
		public OpponentVelocityContract(String serviceName) {
			super();
			serviceName = serviceName.replace("RLService", "");
			serviceName = serviceName.replace("Opponent", "");
			Variable XRL = new Variable("R","XRL");
			Variable YRL = new Variable("R","YRL");
			Variable XGoal = new Variable("R",serviceName+"XGoal");
			Variable YGoal = new Variable("R",serviceName+"YGoal");
			
			inputs.add(XGoal);
			inputs.add(YGoal);
			inputs.add(XRL);
			inputs.add(YRL);
			
			Constant STEPTS = new Constant("R", "STEPTS"+serviceName);
			Constant THRESHOLD = new Constant("R", "THRESHOLD"+serviceName);
			Constant VMAX = new Constant("R", "VMAX"+serviceName);
			Constant SAFETYDISTANCE = new Constant("R", "SAFETYDISTANCE"+serviceName);
			
			constants.add(STEPTS);
			constants.add(THRESHOLD);
			constants.add(VMAX);
			constants.add(SAFETYDISTANCE);
			

			Variable X = new Variable("R",serviceName+"X");
			Variable Y = new Variable("R",serviceName+"Y");
			outputs.add(X);
			outputs.add(Y);
			
			// ghost variables to store positions before loop iteration
			GhostVariable XOld = new GhostVariable(X);
			GhostVariable YOld = new GhostVariable(Y);
			GhostVariable XRLOld = new GhostVariable(XRL);
			GhostVariable YRLOld = new GhostVariable(YRL);	
			ghostVariables.add(XRLOld);
			ghostVariables.add(YRLOld);
			ghostVariables.add(XOld);
			ghostVariables.add(YOld);
			
			// ghost variables that store timing behavior
			Variable simTime = new Variable("R", "simTime");
			GhostVariable simTimeOld = new GhostVariable(simTime);

			// tDiff = simTime-simTimeOld
			AdditionTerm tDiffCalc = new AdditionTerm(simTime); tDiffCalc.subtract(simTimeOld);
			GhostVariable tDiff = new GhostVariable("tDiff", tDiffCalc);
			ghostVariables.add(tDiff);
			ghostVariables.add(simTimeOld);
			
			// no assumptions on arrived
			Variable Arrived = new Variable("R","Arrived"+serviceName);
			outputs.add(Arrived);
			
			/*Contract2:
	        ?(
	          ( (THRESHOLDA>=0) &
	            (VMAXA>=0) &
	            (STEPTSA>0))
	          ->((
	              ((((XA-XAOld)^2.0)+((YA-YAOld)^2.0))^0.5) <= (VMAXA*tDiff))));
			 */
			Conjunction assumptionConstants2 = new Conjunction(new Relation(THRESHOLD, GREATER_EQUAL, ZERO),new Relation(VMAX, GREATER_EQUAL, ZERO),new Relation(STEPTS, GREATER_THAN, ZERO));
			Relation velocity = new Relation(RLContractUtil.getEuclideanDistance(X,Y,XOld,YOld), LESS_EQUAL, new MultiplicationTerm(VMAX, tDiff));

			setAssumptionGuaranteePair(assumptionConstants2, velocity);
		}
}
