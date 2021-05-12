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
package simulink2dl.transform.blocktransfomer.RL;


import java.util.LinkedList;
import java.util.List;


import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.contracts.helper.RL.ArrivedCheckerContract;
import simulink2dl.dlmodel.contracts.helper.RL.AvoidOvershootingContract;
import simulink2dl.dlmodel.contracts.helper.RL.CrashDetectorContract;
import simulink2dl.dlmodel.contracts.helper.RL.EuclideanDistanceCalculatorContract;
import simulink2dl.dlmodel.contracts.helper.RL.EvasiveMoveChooserContract;
import simulink2dl.dlmodel.contracts.helper.RL.GoalTrackerContract;
import simulink2dl.dlmodel.contracts.helper.RL.NoiseContract;
import simulink2dl.dlmodel.contracts.helper.RL.InvalidMoveCheckerContract;
import simulink2dl.dlmodel.contracts.helper.RL.JobSchedulerContract;
import simulink2dl.dlmodel.contracts.helper.RL.MotorControlContractMoveEqVelo;
import simulink2dl.dlmodel.contracts.helper.RL.MotorControlContractMoveZero;
import simulink2dl.dlmodel.contracts.helper.RL.OpponentControllerContract;
import simulink2dl.dlmodel.contracts.helper.RL.OpponentEvasionContract;
import simulink2dl.dlmodel.contracts.helper.RL.OpponentVelocityContract;
import simulink2dl.dlmodel.contracts.helper.RL.RLInfoContract;
import simulink2dl.dlmodel.contracts.helper.RL.RLRobotContract;
import simulink2dl.dlmodel.contracts.helper.RL.RewardContract;
import simulink2dl.dlmodel.contracts.helper.RL.RobotRLAgentContract;
import simulink2dl.dlmodel.contracts.helper.RL.RobotSensorContractCreator;
import simulink2dl.dlmodel.contracts.helper.RL.VelocityAdjustorContract1;
import simulink2dl.transform.Environment;
import simulink2dl.transform.blocktransformer.ServiceTransformer;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.util.PluginLogger;
/** Service transformer for the SimulinkRL2dL project.
 * TODO: Rework contract implementation.
 * - Use parser instead of hard-coded contracts.
 * - Delete Contract interface, which only exists to support deprecated SimpleContract.
*/
public class ServiceTransformerRL extends ServiceTransformer {
	
	public ServiceTransformerRL(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}
	@Override
	protected List<HybridContract> createContracts(DLModel model, String serviceName) {
		List<HybridContract> contracts = new LinkedList<HybridContract>();
		if (serviceName.endsWith("EuclideanDistanceCalculator")) {
			contracts.add(new EuclideanDistanceCalculatorContract(serviceName));
		} else if (serviceName.endsWith("OpponentController")) {
			contracts.add(new OpponentControllerContract(serviceName));
		} else if (serviceName.endsWith("Reward")) {
			contracts.add(new RewardContract(serviceName));
		} else if (serviceName.endsWith("GoalTracker")) {
			contracts.add(new GoalTrackerContract(serviceName));
		} else if (serviceName.endsWith("CrashDetector")) {
			contracts.add(new CrashDetectorContract(serviceName));
		} else if (serviceName.endsWith("RLAgent")) {
			contracts.add(new RobotRLAgentContract(serviceName));
		} else if (serviceName.endsWith("RLRobot")) {
			contracts.add(new RLRobotContract(serviceName));
		} else if (serviceName.contains("Opponent")) {
			contracts.add(new OpponentEvasionContract(serviceName));
			contracts.add(new OpponentVelocityContract(serviceName));
		} else if (serviceName.endsWith("JobScheduler")) {
			contracts.add(new JobSchedulerContract(serviceName));
		} else if (serviceName.endsWith("RobotSensorSubsystem")) {
			contracts.addAll(RobotSensorContractCreator.getContractsForSensor(model, serviceName, false));
		} else if (serviceName.endsWith("RobotSensor")) {
			contracts.addAll(RobotSensorContractCreator.getContractsForSensor(model, serviceName, false));
		} else if (serviceName.endsWith("RobotSensorSubsystemWithDMAXAndNoise")) {
			contracts.add(RobotSensorContractCreator.getContractForSubSensor(model, serviceName, true));
		} else if (serviceName.endsWith("RobotSensorWithDMAXAndNoise")) {
			contracts.add(RobotSensorContractCreator.getContractForSubSensor(model, serviceName, true));
		} else if (serviceName.endsWith("Noise")) {
			contracts.add(new NoiseContract(serviceName));
			//Note: All other contracts for the disturbed RL Robot sensor were not implemented here but manually inserted in KeYmaera X.
		} else if (serviceName.endsWith("InvalidMoveChecker")) {
			contracts.add(new InvalidMoveCheckerContract(serviceName));
		} else if (serviceName.endsWith("ArrivedChecker")) {
			contracts.add(new ArrivedCheckerContract(serviceName));	
		} else if (serviceName.endsWith("AvoidOvershooting")) {
			contracts.add(new AvoidOvershootingContract(serviceName));
		} else if (serviceName.endsWith("AdjustVelocity")) {
			contracts.add(new VelocityAdjustorContract1(serviceName));
		} else if (serviceName.endsWith("MotorControl")) {
			contracts.add(new MotorControlContractMoveEqVelo(serviceName));
			contracts.add(new MotorControlContractMoveZero(serviceName));
		} else if (serviceName.endsWith("EvasiveMoveChooser")) {
			contracts.add(new EvasiveMoveChooserContract(serviceName));
		} else if (serviceName.endsWith("RLInfo")) {
			contracts.add(new RLInfoContract(serviceName));
		} else {
			PluginLogger.error("No contract creation given for service type: " + serviceName);
			return null;
		}
		

		return contracts;
	}

}
