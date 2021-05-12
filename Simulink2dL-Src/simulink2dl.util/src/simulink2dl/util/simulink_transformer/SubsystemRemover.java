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
package simulink2dl.util.simulink_transformer;

import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.util.PluginLogger;

/**
 * This class removes the subsystem blocks of a Java-IR simulink model. The
 * inner blocks are still present and connected to the outer blocks.
 * 
 * @author Nick Bremer, Timm Liebrenz
 *
 */
public class SubsystemRemover {

	private SimulinkModel simulinkModel;

	private List<String> serviceSubsystemPrefixes;

	private List<SimulinkBlock> ordinarySubsystems;
	private List<SimulinkBlock> serviceSubsystems;

	private List<String> contractIds;

	public SubsystemRemover(SimulinkModel simulinkModel, List<String> contractIds) {
		this.simulinkModel = simulinkModel;

		serviceSubsystemPrefixes = new LinkedList<String>();

		ordinarySubsystems = new LinkedList<SimulinkBlock>();
		serviceSubsystems = new LinkedList<SimulinkBlock>();

		serviceSubsystemPrefixes = new LinkedList<String>();

		this.contractIds = contractIds;
	}

	private void initializeServiceSubsystemNames() {
		serviceSubsystemPrefixes.add("Service");
	}

	public void initialize() {
		initializeServiceSubsystemNames();

//		for (SimulinkBlock block : simulinkModel.getSubBlocks()) {
//			if (isOrdinarySubsystem(block)) {
//				if (isServiceSubsystem(block)) {
//					serviceSubsystems.add(block);
//				} else {
//					ordinarySubsystems.add(block);
//				}
//			}
//		}
	}

	public void eliminateOrdinarySubsystems() {
		// TODO: implement this
		PluginLogger.error("Elimination of Subsystems not implemented!");
//		List<SimulinkBlock> inAndOutports = new LinkedList<SimulinkBlock>();
//
//		// collect the subsystem blocks and their input and output ports
//		for (SimulinkBlock subsystemBlock : ordinarySubsystems) {
//			for (SimulinkLine inputLine : subsystemBlock.getInLines()) {
//				if (inputLine.getDstPort().getBlock().getType().equals("Inport")) {
//					inAndOutports.add(inputLine.getDstPort().getBlock());
//				}
//			}
//			for (SimulinkLine outputLine : subsystemBlock.getOutLines()) {
//				if (outputLine.getSrcPort().getBlock().getType().equals("Outport")) {
//					inAndOutports.add(outputLine.getSrcPort().getBlock());
//				}
//			}
//		}
//
//		// remove all subsystem input and output ports
//		for (SimulinkBlock port : inAndOutports) {
//			eliminateOneIOPort(port);
//		}
//
//		// update the parent reference of inner blocks
//		// and update the name of the block
//		for (SimulinkBlock ordinarySubsystem : ordinarySubsystems) {
//			for (SimulinkBlock child : simulinkModel.getBlocks()) {
//				if (child.getParent() != null && child.getParent().equals(ordinarySubsystem)) {
//					// update name
//					child.setName(ordinarySubsystem.getName() + child.getName());
//					// update parent and level
//					child.setParent(ordinarySubsystem.getParent());
//					child.setLevel(child.getLevel() - 1);
//				}
//			}
//		}
//
//		// SignalLines need not be level-changed
//
//		// remove the subsystem blocks from the system
//		Set<SimulinkBlock> newBlocks = simulinkModel.getBlocks();
//		newBlocks.removeAll(ordinarySubsystems);
//		simulinkModel.setBlocks(newBlocks);
	}

//	/**
//	 * Checks whether the given subsystem block is a service.
//	 * 
//	 * @param block
//	 * @return
//	 */
//	private boolean isServiceSubsystem(SimulinkBlock block) {
//		// TODO this is only a temporary solution
//		// currently subsystems with a name that begins with a specified prefix
//		// are handled as service subsystems
//		for (String prefix : serviceSubsystemPrefixes) {
//			if (block.getName() != null & block.getName().startsWith(prefix)) {
//				return true;
//			}
//		}
//
//		return this.contractIds.contains(block.getName());
//	}

	/**
	 * Remove all inner blocks of a subsystem.
	 */
	public void removeBlocksInsideServiceSystem() {
		// TODO: implement this
		PluginLogger.error("Elimination of Subsystems not implemented!");

//		for (SimulinkBlock serviceSubsystem : serviceSubsystems) {
//			List<SimulinkBlock> childList = getInnerBlocks(serviceSubsystem);
//
//			for (SimulinkBlock child : childList) {
//				// if the block is an input or output of the subsystem, redirect
//				// the incoming or outgoing signal line
//				if (child.getType().equals("Inport") && child.getParent().equals(serviceSubsystem)) {
//					// get subsystem input port
//					String portString = child.getParameter("Port");
//					Integer portNumber;
//					portNumber = Integer.parseInt(portString);
//
//					HashMap<Integer, Port> inPortMap = serviceSubsystem.getInPortsMap();
//					Port inPort = inPortMap.get(portNumber);
//
//					SimulinkLine inputLine = child.getInLines().iterator().next();
//
//					// update input line
//					inputLine.setDstBlock(serviceSubsystem);
//					inputLine.setDstPort(inPort);
//
//					// update the name of the service input
//					inPort.setName(child.getName());
//
//					// update the block
//					child.setInSignals(new HashSet<SignalLine>());
//				} else if (child.getType().equals("Outport") && child.getParent().equals(serviceSubsystem)) {
//					// get subsystem output port
//					String portString = child.getParameter("Port");
//					Integer portNumber;
//					if (portString != null) {
//						portNumber = Integer.parseInt(portString);
//					} else {
//						portNumber = 1;
//					}
//					HashMap<Integer, Port> outPortMap = serviceSubsystem.getOutPortsMap();
//					Port outPort = outPortMap.get(portNumber);
//
//					// update all outgoing signal lines
//					for (SignalLine outputLine : child.getOutSignals()) {
//						// update output line
//						outputLine.setSrcBlock(serviceSubsystem);
//						outputLine.setSrcPort(outPort);
//					}
//					// update the name of the service ouput
//					outPort.setName(child.getName());
//
//					// update the block
//					child.setOutSignals(new HashSet<SignalLine>());
//				}
//				removeBlock(child);
//			}
//		}
	}

//	private List<Block> getInnerBlocks(Block outerBlock) {
//		List<Block> innerBlocks = new LinkedList<Block>();
//
//		for (Block block : simulinkModel.getBlocks()) {
//			if (!(block.getParent() != null && block.getParent().equals(outerBlock))) {
//				continue;
//			}
//
//			// add the block
//			innerBlocks.add(block);
//
//			// if the block is a subsystem, also add all of its inner blocks
//			if (block.getType().equals("SubSystem")) {
//				innerBlocks.addAll(getInnerBlocks(block));
//			}
//		}
//
//		return innerBlocks;
//	}
//
//	/**
//	 * Checks whether the <code>Block</code> is a SubSystem without trigger, enable,
//	 * IfAction or state ports.
//	 * 
//	 * @param block
//	 * @return
//	 */
//	private boolean isOrdinarySubsystem(SimulinkBlock block) {
//		return block.getType().equals("SubSystem") && block.getEnablePort() == null && block.getTriggerPort() == null
//				&& block.getIfactionPort() == null && block.getStatePort() == null;
//	}
//
//	private void eliminateOneIOPort(Block toRemove) {
//		// change pointers to represent new structure
//		Set<SignalLine> newOutSignals = new HashSet<SignalLine>();
//
//		// wire outgoing signal lines back to source of this port
//		// get source block
//		SignalLine inLine = toRemove.getInSignals().iterator().next();
//		Block newSource = inLine.getSrcBlock();
//		Port srcOutPort = inLine.getSrcPort();
//		// get outgoing lines
//		for (SignalLine line : toRemove.getOutSignals()) {
//			// update source block and port
//			line.setSrcBlock(newSource);
//			line.setSrcPort(srcOutPort);
//			newOutSignals.add(line);
//		}
//		// update outgoing signals for the source block
//		for (SignalLine sourceOutSignals : newSource.getOutSignals()) {
//			// add all remaining output signal lines
//			if (!sourceOutSignals.getDstBlock().equals(toRemove)) {
//				newOutSignals.add(sourceOutSignals);
//			}
//		}
//		newSource.setOutSignals(newOutSignals);
//
//		// housekeeping
//		removeSingleBlock(toRemove);
//		removeLine(inLine);
//	}
//
//	private void removeBlock(Block toRemove) {
//		for (SignalLine inSignal : toRemove.getInSignals()) {
//			removeLine(inSignal);
//		}
//		for (SignalLine outSignal : toRemove.getOutSignals()) {
//			removeLine(outSignal);
//		}
//		removeSingleBlock(toRemove);
//	}
//
//	private void removeSingleBlock(Block toRemove) {
//		Set<Block> blocks = simulinkModel.getBlocks();
//		blocks.remove(toRemove);
//		simulinkModel.setBlocks(blocks);
//	}
//
//	private void removeLine(SignalLine line) {
//		Set<SignalLine> lines = simulinkModel.getSignalLines();
//		lines.remove(line);
//		simulinkModel.setSignalLines(lines);
//	}

}
