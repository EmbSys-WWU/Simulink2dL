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
package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.ConcurrentContract;
import simulink2dl.dlmodel.contracts.GhostVariable;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.contracts.RLAgentContract;
import simulink2dl.dlmodel.contracts.helper.AbstandsrechnerCorrectGainContract;
import simulink2dl.dlmodel.contracts.helper.CounterContract;
import simulink2dl.dlmodel.contracts.helper.DiscreteDifferenceContract;
import simulink2dl.dlmodel.contracts.helper.TemperatureControlContract;
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
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.blocktransformer.BlockTransformer;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.dlmodel.hybridprogram.ServiceAsHybridProgram;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.model.ConcurrentContractBehavior;
import simulink2dl.transform.model.DiscreteContractBehavior;
import simulink2dl.util.PluginLogger;
/** Service transformer for the SimulinkRL2dL project.
 * TODO: Rework contract implementation.
 * - Use parser instead of hard-coded contracts.
 * - Delete Contract interface, which only exists to support deprecated SimpleContract.
*/
public class ServiceTransformer extends BlockTransformer {
	protected List<Variable> inputVariables;
	protected List<Variable> outputVariables;
	
	public ServiceTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
		
		this.inputVariables = new LinkedList<Variable>();
		this.outputVariables = new LinkedList<Variable>();
	}
	
	public void transformBlock(SimulinkBlock block) {
		List<HybridContract> contracts = createContracts(dlModel, block.getName());
		transformContracts(contracts, block);
	}
	/**
	 * add input behavior
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void handleInputs(HybridContract contract, SimulinkBlock block, List<Macro> contractMacros) {
		LinkedList<Variable> inputs = contract.getInputs();
		Iterator<Variable> inputIterator = inputs.iterator();
		List<SimulinkInPort> inPorts= sortWithIndexIn(block.getInPorts());
		Iterator<SimulinkInPort> inPortIterator = inPorts.iterator();
		
		while(inputIterator.hasNext() && inPortIterator.hasNext()) {
			SimulinkOutPort connectedPort = environment.getConnectedOuputPort(inPortIterator.next());
			String connectedPortID = environment.getPortID(connectedPort);
			ReplaceableTerm inputTerm = new PortIdentifier(connectedPortID);
			ReplaceableTerm contractTermToReplace = inputIterator.next();
			contractMacros.add(new SimpleMacro(contractTermToReplace,inputTerm));
		}
	}
	
	/**
	 * The port collections returned by the block.getInPorts() and block.getOutPort() methods are not sorted.
	 * TODO: Revisit
	 * @param ports
	 * @return sorted List of inPorts
	 */
	private List<SimulinkInPort> sortWithIndexIn(Collection<SimulinkInPort> ports) {
		SimulinkInPort[] sortedCollection = new SimulinkInPort[ports.size()];
		Iterator<SimulinkInPort> portIterator = ports.iterator();
		while(portIterator.hasNext()) {
			SimulinkInPort curPort = portIterator.next();
			int index = Integer.parseInt(curPort.getIndex());
			sortedCollection[index-1]=curPort;
		}
		return Arrays.asList(sortedCollection);
	}
	
	/**
	 * The port collections returned by the block.getInPorts() and block.getOutPort() methods are not sorted.
	 * TODO: Revisit
	 * @param ports
	 * @return sorted List of outPorts
	 */
	private List<SimulinkOutPort> sortWithIndexOut(Collection<SimulinkOutPort> ports) {
		SimulinkOutPort[] sortedCollection = new SimulinkOutPort[ports.size()];
		Iterator<SimulinkOutPort> portIterator = ports.iterator();
		while(portIterator.hasNext()) {
			SimulinkOutPort curPort = portIterator.next();
			int index = Integer.parseInt(curPort.getIndex());
			sortedCollection[index-1]=curPort;
		}
		return Arrays.asList(sortedCollection);
	}
	
	/**
	 * add ghost variables and assignments
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void handleGhosts(HybridContract contract, SimulinkBlock block, HybridProgramCollection contractHP) {
		if (contract instanceof ConcurrentContract) {
			for (GhostVariable variable : contract.getGhostVariable()) {
				if(dlModel.getVariableByName(variable.getName())==null) {
					contractHP.addElement(new DiscreteAssignment(variable, variable.getAssignedTerm()));
					dlModel.addVariable(variable);
				}
			}
		} else {
			for (GhostVariable variable : contract.getGhostVariable()) {
				contractHP.addElement(new DiscreteAssignment(variable, variable.getAssignedTerm()));
				dlModel.addVariable(variable);
			}
		}
	}
	/**
	 * add output behavior
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void handleOutputs(HybridContract contract, SimulinkBlock block, HybridProgramCollection contractHP) {
		LinkedList<Variable> outputs = contract.getOutputVariables();
		Iterator<Variable> outputIterator = outputs.iterator();
		List<SimulinkOutPort> outPorts = sortWithIndexOut(block.getOutPorts());
		Iterator<SimulinkOutPort> outPortIterator = outPorts.iterator();
		
		while(outputIterator.hasNext() && outPortIterator.hasNext()) {
			Variable variable = outputIterator.next();
			SimulinkOutPort outPort = outPortIterator.next();
			if(!this.outputVariables.contains(variable)) {
				dlModel.addVariable(variable);
				this.outputVariables.add(variable);
				
				contractHP.addElement(new NondeterministicAssignment(variable));
				
			}
			Term replaceWith = variable;
			dlModel.addMacro(new SimpleMacro(environment.getToReplace(outPort), replaceWith));
		}
	}
	
	/**
	 * add constants introduced by the contracts
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void addConstants(HybridContract contract, DLModel model) {
		for (Constant constant : contract.getConstants()) {
			dlModel.addConstant(constant);
		}
	}
	
	/**
	 * add contracts to initial conditions
	 * TODO should this always be done for all contracts?
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void addToInitConditions(DLModel model, List<HybridContract> contracts, List<Macro> contractMacros) {
	
		List<Formula> initConditions = new LinkedList<Formula>();
		for(HybridContract contract: contracts) {
			initConditions.add(contract.asFormula());
		}
	
		for(Formula condition: initConditions) {
			/* Replace internal names of the contract inputs with port identifiers */
			for(Macro macro: contractMacros) {
				if (condition.containsTerm(macro.getToReplace())) {
					condition.replaceTermRecursive(macro.getToReplace(), macro.getReplaceWith());
				}
			}
			dlModel.addInitialCondition(condition);
		}
	}

	/**
	 * Transform contract to HP
	 * @param contract
	 * @param block
	 * @param contractHP
	 */
	private void transformContracts(List<HybridContract> contracts, SimulinkBlock block) {

		List<Macro> contractMacros = new LinkedList<Macro>();
		HybridProgramCollection ghostAssignments = new HybridProgramCollection();
		HybridProgramCollection outputAssignments = new HybridProgramCollection();
		
		for(HybridContract contract : contracts) {
				handleGhosts(contract, block, ghostAssignments);
				handleInputs(contract, block, contractMacros);
				handleOutputs(contract, block, outputAssignments);
				addConstants(contract, dlModel);
		}
		addToDlModel(dlModel, contracts, ghostAssignments, outputAssignments, contractMacros);
		addToInitConditions(dlModel, contracts, contractMacros);
	}
	/**
	 * add contracts to the dL model.
	 * @param dlModel
	 * @param contract
	 * @param contractHP
	 * @param contractMacros
	 */
	private void addToDlModel(DLModelSimulink dlModel, List<HybridContract> contracts, HybridProgramCollection ghostAssignments, HybridProgramCollection outputAssignments, List<Macro> contractMacros) {
		// add contract information to the system behavior
		
		//TODO: contracts for the same service should all be of the same type.
		
		ServiceAsHybridProgram contractHP = new ServiceAsHybridProgram();
		if (contracts != null) {
			dlModel.addContracts(contracts);
			
			for(HybridContract contract : contracts) {
				contractHP.addElement(contract.asHybridProgram());
			}
			
			for(Macro macro : contractMacros) {
				macro.applyToHybridProgramCollection(contractHP);
				macro.applyToHybridProgramCollection(ghostAssignments);
				macro.applyToHybridProgramCollection(outputAssignments);
			}

			if(contracts.get(0) instanceof RLAgentContract) {
				/* create contract HP that acts in sampling intervals used for the RL agent */
				//TODO all RL agent contracts should refer to the same sampling time.
				DiscreteContractBehavior discreteBehavior = new DiscreteContractBehavior(((RLAgentContract)contracts.get(0)).getSamplingTime()); 
				if(!ghostAssignments.isEmpty())
					discreteBehavior.addBehavior(ghostAssignments);
				if(!outputAssignments.isEmpty())
					discreteBehavior.addBehavior(outputAssignments);
				if(!contractHP.isEmpty())
					discreteBehavior.addBehavior(contractHP);
				discreteBehavior.addDiscreteContract(dlModel);
				dlModel.addRLContractsToModel(contracts);
			} else if (contracts.get(0) instanceof ConcurrentContract){
				/* create contract capturing behavior of services that show continuous/concurrent behavior */
				ConcurrentContractBehavior concurrentContractHP = dlModel.getConcurrentContractBehavior();
				if(!ghostAssignments.isEmpty())
					concurrentContractHP.addGhostAssignments(ghostAssignments);
				if(!outputAssignments.isEmpty())
					concurrentContractHP.addOutputAssignments(outputAssignments);
				if(!contractHP.isEmpty())
					concurrentContractHP.addHP(contractHP);
			} else {
				/* create contract HP for 'normal' services */
				if(!outputAssignments.isEmpty())
					contractHP.addElementFront(outputAssignments);
				if(!ghostAssignments.isEmpty())
					contractHP.addElementFront(ghostAssignments);
				dlModel.addBehavior(contractHP);
			}
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

	// TODO temporary solution, fixed contracts
	// TODO later on: provide contracts in file
	protected List<HybridContract> createContracts(DLModel model, String serviceName) {
		List<HybridContract> contracts = new LinkedList<HybridContract>();
		//contract for non RL
		if (serviceName.endsWith("TemperatureControl")) {
			Constant smallstep = dlModel.getConstantByName("SMALLSTEPSIZE");
			contracts.add(new TemperatureControlContract(serviceName, smallstep));
//		} else if (serviceName.endsWith("CounterNeg9to0")) {
//			contracts.add(new CounterContract(serviceName));
//		} else if (serviceName.endsWith("DiscreteDifference")) {
//			contracts.add(new DiscreteDifferenceContract(serviceName));
//		} else if (serviceName.endsWith("Abstandsrechner1") || serviceName.endsWith("Abstandsrechner2")) {
//			contract = new AbstandsrechnerNoOverflowContract(dlModel, serviceName);
//			contracts.add(new AbstandsrechnerCorrectGainContract(serviceName));
		} else {
			PluginLogger.error("No contract creation given for service type: " + serviceName);
			return null;
		}
		return contracts;
	}

}
