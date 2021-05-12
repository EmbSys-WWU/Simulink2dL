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
package simulink2dl.transform.test;

import java.util.HashSet;
import java.util.Set;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.util.PluginLogger;

/**
 * @author nick
 *
 */
public class ModelConsistencyChecker {

	// no dangling blocks

	/**
	 * Asserts that the following statements are true of the Model: -for each
	 * SignalLine the source and destination Ports are connected to the src and dst
	 * Block of that SignalLine -for each SignalLine the src and dst Block contain
	 * that line as OutSignal and InSignal respectively -for each SignalLine the src
	 * and dst Block are in model.getBlocks
	 * 
	 * -for each Block each Port connected to it has at least one SignalLine
	 * attached to it -for each Block all InSignals and OutSignals are in
	 * model.getSignalLines
	 * 
	 * @param model
	 * @return
	 */
	public static boolean modelConsistent(SimulinkModel model) {
		System.out.println("[INFO] Checking model " + model.getName() + " for consistency");

		boolean linesConsistent = checkSignalLines(model);
		if (!linesConsistent) {
			System.out.println("[ERR] SignalLines inconsistent.");
		}
		boolean blocksConsistent = checkBlocks(model);
		if (!blocksConsistent) {
			System.out.println("[ERR] Blocks inconsistent.");
		}
		return linesConsistent && blocksConsistent;
	}

	private static boolean checkSignalLines(SimulinkModel model) {
		boolean modelConsistent = true;

		Set<SimulinkBlock> blocksReachableByLine = new HashSet<SimulinkBlock>();

		for (SimulinkLine line : model.getContainedLinesRecursively()) {
			SimulinkOutPort srcPort = line.getSrcPort();
			SimulinkBlock srcBlock = srcPort.getBlock();
			if (srcBlock == null || srcPort == null) {
				modelConsistent = false;
				PluginLogger
						.error("[ERR] SignalLine: " + line + " src Block: " + srcBlock + " src Port: " + srcPort);
			} else {
				blocksReachableByLine.add(srcBlock);

				// line and srcBlock point to the same Port
				if (!srcBlock.getOutPorts().contains(srcPort)) {
					modelConsistent = false;
					PluginLogger.error("[ERR] SignalLine: " + line + " points to " + srcPort
							+ " which is not connected to source Block (" + srcBlock + ")");
				}

				// line is in srcBlock.getOutSignals
				if (!srcBlock.getOutLines().contains(line)) {
					modelConsistent = false;
					PluginLogger.error("[ERR] SignalLine: " + line + " not in OutSignals of src Block: " + srcBlock);
				}
			}

			SimulinkInPort dstPort = line.getDstPort();
			SimulinkBlock dstBlock = dstPort.getBlock();
			if (dstBlock == null || dstPort == null) {
				modelConsistent = false;
				PluginLogger
						.error("[ERR] SignalLine: " + line + " dst Block: " + dstBlock + " dst Port: " + dstPort);
			} else {
				blocksReachableByLine.add(dstBlock);

				// line and dstBlock point to the same Port
				if (!dstBlock.getInPorts().contains(dstPort)) {
					modelConsistent = false;
					PluginLogger.error("[ERR] SignalLine: " + line + " points to " + srcPort
							+ " which is not connected to destination Block (" + dstBlock + ")");
				}

				// line is in dstBlock.getInSignals
				if (!dstBlock.getInLines().contains(line)) {
					modelConsistent = false; // TODO comment back in
					PluginLogger.error("[ERR] SignalLine: " + line + " not in InSignals of dst Block: " + dstBlock);
				}
			}
		}

		if (!blocksReachableByLine.containsAll(model.getSubBlocks())) {
			// some Blocks in the model are not reachable by a SignalLine
			// Examples might be: removeAll Block_Support_Table, DocBlock,
			// Model_Info, Timed-Base_Linearization, Enable_Port, Trigger_Port,
			// Data_Propagation_Example, Data_Store_Memory, Goto_Tag_Visibility,
			// Floating_Scope, S-Function_Examples, ...
			// This is not an error. The user will be notified anyway.
			Set<SimulinkBlock> modelBlocks = new HashSet<>(model.getSubBlocks());
			modelBlocks.removeAll(blocksReachableByLine);
			PluginLogger.info(
					"[INFO] Block(s) neither src nor dst of a SignalLine, but in Blocks of model: " + modelBlocks);
		}

		if (!model.getSubBlocks().containsAll(blocksReachableByLine)) {
			// There is Block, which is not in model.getBlocks, but can be
			// reached by a SignalLine.
			modelConsistent = false;
			blocksReachableByLine.removeAll(model.getSubBlocks());
			PluginLogger.error("[ERR] Block(s) either a src or a dst of a SignalLine, but not in Blocks of model: "
					+ blocksReachableByLine);
		}

		return modelConsistent;
	}

	private static boolean checkBlocks(SimulinkModel model) {
		boolean modelConsistent = true;

		Set<SimulinkLine> connectedSignalLines = new HashSet<>();

		for (SimulinkBlock block : model.getSubBlocks()) {
			connectedSignalLines.addAll(block.getInLines());
			connectedSignalLines.addAll(block.getOutLines());

			// each inPort has at least on inSignal
			Set<SimulinkLine> inLines = new HashSet<>(block.getInLines());
			for (SimulinkInPort inPort : block.getInPorts()) {
				SimulinkLine connectedLine = null;
				for (SimulinkLine inLine : inLines) {
					if (inPort.getLine() != null && inPort.getLine().equals(inLine)) {
						connectedLine = inLine;
					}
				}
				if (connectedLine != null) {
					modelConsistent = false;
					PluginLogger
							.error("[ERR] Port " + inPort + " of Block " + block + " has no SignalLine attached.");
				} else {
					inLines.remove(connectedLine);
				}
			}

			// each outPort has at least one outSignal
			Set<SimulinkLine> outLines = new HashSet<>(block.getOutLines());
			for (SimulinkOutPort outPort : block.getOutPorts()) {
				SimulinkLine connectedLine = null;
				for (SimulinkLine outLine : outLines) {
					if (outPort.getLines().contains(outLine)) {
						connectedLine = outLine;
					}
				}
				if (connectedLine != null) {
					modelConsistent = false;
					PluginLogger
							.error("[ERR] Port " + outPort + " of Block " + block + " has no SignalLine attached.");
				} else {
					// TODO remove all outgoing lines?
					outLines.remove(connectedLine);
				}
			}
		}

		if (!model.getContainedLinesRecursively().containsAll(connectedSignalLines)) {
			modelConsistent = false;
			connectedSignalLines.removeAll(model.getContainedLinesRecursively());
			PluginLogger.error("[ERR] SignalLine(s) not in model.getSignalLines: " + connectedSignalLines);
		}

		return modelConsistent;
	}
}
