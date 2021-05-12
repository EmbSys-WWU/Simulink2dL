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

import simulink2dl.dlmodel.contracts.RLAgentContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Equivalence;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;
/**
 * Class of the RL agents contract.
 * Note that the contract was derived manually for the experiments with 6 opponents.
 * This contract only features 2 opponents
 * @author julius
 */
public class RobotRLAgentContract extends RLAgentContract{
	
	public RobotRLAgentContract(String serviceName) {
		super("TSRL");
		
		serviceName=serviceName.replace("RL","");
		serviceName=serviceName.replace("Agent","");
		serviceName=serviceName.replace("Service","");
		
		Variable X = new Variable("R","XRL");
		Variable Y = new Variable("R","YRL");
		Variable XGoal = new Variable("R", "RLXGoal");
		Variable YGoal = new Variable("R", "RLYGoal");
		Variable Reward = new Variable("R", "RLReward");
		Variable IsDone = new Variable("R", "IsDone");
		Constant VMAXRL = new Constant("R", "VMAXRL");
		constants.add(VMAXRL);
		inputs.add(X);
		inputs.add(Y);
		inputs.add(XGoal);
		inputs.add(YGoal);
		
		String[] opponents;
		try {
			int numOpps = Integer.parseInt(serviceName);
			if(numOpps==6) {
				String[] opps = {"A","B","C","D","E","F"};
				opponents = opps;
			}
			else {
				String[] opps = {"A","B"};
				opponents = opps;
			}
		} catch (Exception e) {
			String[] opps = {"A","B"};
			opponents = opps;
		}

		//Handle Outputs
		Variable DirX = new Variable("R", "DirXRL");
		Variable DirY = new Variable("R", "DirYRl");
		Variable Velocity = new Variable("R", "VRL");
		outputs.add(DirX);
		outputs.add(DirY);
		outputs.add(Velocity);
		
		//Start building Contract
		addAssumption(TRUE);
		addGuarantee(new Relation(Velocity,GREATER_EQUAL,ZERO));
		addGuarantee(new Equivalence(new Relation(Velocity, EQUAL, ZERO), new Conjunction(new Relation(DirX, EQUAL, ZERO), new Relation(DirY, EQUAL, ZERO))));
		
		for(String oppName : opponents) {
			Variable DiffXOpp = new Variable("R","Diff"+oppName+"X");
			Variable DiffYOpp = new Variable("R","Diff"+oppName+"Y");
			Variable DirXOpp = new Variable("R","Dir"+oppName+"X");
			Variable DirYOpp = new Variable("R","Dir"+oppName+"Y");
			inputs.add(DiffXOpp);
			inputs.add(DiffYOpp);
			inputs.add(DirXOpp);
			inputs.add(DirYOpp);
			
			//Constants
			Constant VMAX = new Constant("R", "VMAX"+oppName);
			Constant THRESHOLD = new Constant("R", "THRESHOLD"+oppName);
			constants.add(VMAX);
			constants.add(THRESHOLD);
			addSafeRLGuaranteeForOpp(DiffXOpp, DiffYOpp, Velocity, VMAX, THRESHOLD);
		}
		inputs.add(Reward);
		inputs.add(IsDone);
	}
	

	/**Safe RL contract: 
      ?(
        (true)
        ->((
            Velocity >= 0.0) &
          (
            
              Velocity = 0.0
            <->(
                DirX = 0.0) &
              (
                DirY = 0.0)) &
          ((
              ((((DiffXOpp^2.0)+(DiffYOpp^2.0))^0.5)-(Velocity*STEPTSRL)-(VMAXOpp*STEPTSRL)) > THRESHOLDOpp) |
            (
              Velocity = 0.0)))) ... ;
	 */
	private void addSafeRLGuaranteeForOpp(Term DiffX, Term DiffY, Term Velocity, Term VelocityOpp, Term ThresholdOpp) {
		Term distance = new ExponentTerm((new AdditionTerm(new ExponentTerm(DiffX,new RealTerm(2)),new ExponentTerm(DiffY,new RealTerm(2)))), new RealTerm(0.5));
		
		AdditionTerm newDistance = new AdditionTerm(distance);
		Constant samplingTime = new Constant("R", "STEP"+this.getSamplingTime());
		constants.add(samplingTime);
		newDistance.subtract(new MultiplicationTerm(Velocity, samplingTime));
		newDistance.subtract(new MultiplicationTerm(VelocityOpp, samplingTime));
		
		Relation safeVelocity = new Relation(newDistance, GREATER_THAN, ThresholdOpp);
		Relation stop = new Relation(Velocity, EQUAL, ZERO);
		
		Disjunction safeBehavior = new Disjunction(safeVelocity,stop);
		
		addGuarantee(safeBehavior);
	}
}
