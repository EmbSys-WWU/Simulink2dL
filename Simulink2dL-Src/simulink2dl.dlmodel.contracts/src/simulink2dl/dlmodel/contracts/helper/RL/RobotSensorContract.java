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

import java.util.Arrays;
import java.util.LinkedList;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.Term;

public class RobotSensorContract extends HybridContract {
	
	//in and outputs for current contract
	Variable XOpp;
	Variable YOpp;
	Variable DiffX;
	Variable DiffY;
	Variable DirX;
	Variable DirY;
	
	public RobotSensorContract(DLModel model, boolean cutoff, String oppName, String[] allOpps) {
		super();
		Variable X = new Variable("R","XRL");
		Variable Y = new Variable("R","YRL");
		inputs.addAll(Arrays.asList(X,Y));

		//all in and outputs of the service
		for(String opp: allOpps) {
			Variable additionalXOpp = new Variable("R","X"+opp);
			Variable additionalYOpp = new Variable("R","Y"+opp);
			inputs.addAll(Arrays.asList(additionalXOpp,additionalYOpp));
			Variable additionalDiffX = new Variable("R","DiffX"+opp);
			Variable additionalDiffY = new Variable("R","DiffY"+opp);
			Variable additionalDirX = new Variable("R","DirX"+opp);
			Variable additionalDirY = new Variable("R","DirY"+opp);
			if(opp.equals(oppName)) {
				XOpp = additionalXOpp;
				YOpp = additionalYOpp;
				DiffX = additionalDiffX;
				DiffY = additionalDiffY;
				DirX = additionalDirX;
				DirY = additionalDirY;
			}
			outputs.addAll(Arrays.asList(additionalDiffX,additionalDiffY,additionalDirX,additionalDirY));
		}

		
		if(cutoff) {
			Constant cutoffConst = new Constant("R", "CUTOFF");
			model.addConstant(cutoffConst);
			createSubSensorContractWithCutoffAndNoise(X,Y,oppName,constants, inputs, intermediateVariables, outputs, cutoffConst);
		} else {
			createSubSensorContract(X,Y,oppName);
		}
	}
	/** Sensor Contract
	 * ?(
     *     (true)
     *     ->((
     *         SensorDiffXA = (XA-X)) &
     *       (
     *         SensorDiffYA = (YA-Y))));
     *   ?(
     *     (true)
     *     ->((
     *         SensorDiffXB = (XB-X)) &
     *       (
     *         SensorDiffYB = (YB-Y))));
	 */
	private void createSubSensorContract(Variable X, Variable Y, String serviceName) {
		
		AdditionTerm DiffXCalc = new AdditionTerm(XOpp); DiffXCalc.subtract(X);
		AdditionTerm DiffYCalc = new AdditionTerm(YOpp); DiffYCalc.subtract(Y);

		Conjunction distCalcGuarantee = new Conjunction(new Relation(DiffX, EQUAL, DiffXCalc),new Relation(DiffY, EQUAL, DiffYCalc));

		setAssumptionGuaranteePair(TRUE, distCalcGuarantee);
	}
	/** Sensor Contract with cutoff and noise
	 *   (
      (
          CUTOFF >= 0.0) &
        (
          noiseMIN >= 0.0) &
        (
          noiseMAX >= noiseMIN)
      ->((
          noiseB >= noiseMIN) &
        (
          noiseB <= noiseMAX) &
        (
          
            (((((XB-X)^2.0)+((YB-Y)^2.0))^0.5)*noiseB) <= CUTOFF
          ->(
            (((DiffXB^2.0)+(DiffYB^2.0))^0.5) = (((((XB-X)^2.0)+((YB-Y)^2.0))^0.5)*noiseB))) &
        (
          
            (((((XB-X)^2.0)+((YB-Y)^2.0))^0.5)*noiseB) > CUTOFF
          ->(
            (((DiffXB^2.0)+(DiffYB^2.0))^0.5) = CUTOFF))))
    */
	private void createSubSensorContractWithCutoffAndNoise(Variable X, Variable Y, String serviceName, LinkedList<Constant> constants, LinkedList<Variable> inputs, LinkedList<Variable> intermediates, LinkedList<Variable> outputs, Constant cutoff) {
		serviceName=serviceName.replace("Andnoise", "");
		Variable noise = new Variable("R", "noise"+serviceName);
		intermediates.add(noise);
		
		Term EuclDist = RLContractUtil.getEuclideanDistance(XOpp,YOpp,X,Y);
		MultiplicationTerm DistInter= new MultiplicationTerm(EuclDist, noise);
		
		AdditionTerm DiffXCalc = new AdditionTerm(XOpp); DiffXCalc.subtract(X);
		AdditionTerm DiffYCalc = new AdditionTerm(YOpp); DiffYCalc.subtract(Y);
		
		Constant noiseMinValue = new Constant("R", "noiseMIN");
		Constant noiseMaxValue = new Constant("R", "noiseMAX");
		
		constants.add(noiseMinValue);
		constants.add(noiseMaxValue);
		
		Relation noiseMin = new Relation(noise, GREATER_EQUAL, noiseMinValue);
		Relation noiseMax = new Relation(noise, LESS_EQUAL, noiseMaxValue);
		
		Conjunction noiseRange = new Conjunction(noiseMin,noiseMax);
		
		Relation DistLECutoff = new Relation(DistInter, LESS_EQUAL, cutoff);
		Implication caseNotCutoff = new Implication(DistLECutoff,new Relation(RLContractUtil.getEuclideanDistance(DiffX,DiffY),EQUAL,DistInter));
	
		Relation DistGCutoff = new Relation(DistInter, GREATER_THAN, cutoff);
		Implication caseCutoff = new Implication(DistGCutoff,new Relation(RLContractUtil.getEuclideanDistance(DiffX,DiffY),EQUAL,cutoff));
		
		setAssumptionGuaranteePair(new Conjunction(new Relation(cutoff, GREATER_EQUAL, ZERO), new Relation(noiseMinValue, GREATER_EQUAL, ZERO), new Relation(noiseMaxValue, GREATER_EQUAL, noiseMinValue)), new Conjunction(noiseRange,caseNotCutoff,caseCutoff));
	}
	
//	/**
//	 * Sensor contract contains the sensor subsystem contract
//	 * 
//	 */
//	public static class RobotSensorSubsystemContract extends Simulink2dLContract {
//		public RobotSensorSubsystemContract(DLModel model, String serviceName, boolean cutoff) {
//			super();
//			String oppName = serviceName;
//			oppName = oppName.replace("RLService", "");
//			oppName = oppName.replace("RobotSensor", "");
//			oppName = oppName.replace("system", "");
//			oppName = oppName.replace("WithCutoff", "");
//			Variable X = new Variable("R","X");
//			Variable Y = new Variable("R","Y");
//			inputs.addAll(Arrays.asList(X,Y));
//			AssumptionGuaranteePair contract;
//			/* create contract with cutoff and noise or usual sensor contract*/
//			if(cutoff) {
//				Constant cutoffConst = new Constant("R","CUTOFF");
//				model.addConstant(cutoffConst);
//				contract = getSubSensorContractWithCutoffAndnoise(X,Y,oppName,constants, inputs, intermediateVariables, outputs, cutoffConst);
//			} else {
//				contract = getSubSensorContract(X,Y,oppName, inputs, outputs);
//			}
//			setAssumptionGuaranteePair(contract);
//		}
//	
//	}
}