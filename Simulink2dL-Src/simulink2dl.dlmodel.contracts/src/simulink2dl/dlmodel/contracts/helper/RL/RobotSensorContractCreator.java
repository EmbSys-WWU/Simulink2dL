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

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.HybridContract;

public abstract class RobotSensorContractCreator {
	
	public static List<HybridContract> getContractsForSensor(DLModel model, String serviceName, boolean cutoff) {
		String[] subsensors;
		serviceName=serviceName.replace("RL", "");
		serviceName=serviceName.replace("Service", "");
		serviceName=serviceName.replace("RobotSensor", "");
		try {
			int numOpps = Integer.parseInt(serviceName);
			if(numOpps==6) {
				String[] opps = {"A","B","C","D","E","F"};
				subsensors = opps;
			}
			else {
				String[] opps = {"A","B"};
				subsensors = opps;
			}
		} catch(Exception e) {
			String[] opps = {"A","B"};
			subsensors = opps;
		}
		List<HybridContract> contracts = new LinkedList<HybridContract>();
		for(String sub : subsensors) {
			contracts.add(new RobotSensorContract(model, cutoff, sub, subsensors));
		}
		return contracts;
	}
	
	public static HybridContract getContractForSubSensor(DLModel model, String serviceName, boolean cutoff) {
		serviceName=serviceName.replace("RL", "");
		serviceName=serviceName.replace("Service", "");
		serviceName=serviceName.replace("RobotSensorSubsystem", "");
		String[] serviceNameArray = {serviceName};
		
		return new RobotSensorContract(model, cutoff, serviceName, serviceNameArray);
	}
}
