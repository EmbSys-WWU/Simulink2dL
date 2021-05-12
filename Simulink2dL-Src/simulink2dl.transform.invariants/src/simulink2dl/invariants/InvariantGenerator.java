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
package simulink2dl.invariants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.blockanalyzer.BlockAnalyzer;
import simulink2dl.blockanalyzer.BlockAnalyzerFactory;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.analyzer.DataFlowAnalysis;
import simulink2dl.invariants.analyzer.SafetyAnalysis;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.ControlInformation;
import simulink2dl.invariants.information.DataInformation;
import simulink2dl.invariants.information.DiscreteSignalInformation;
import simulink2dl.invariants.information.IntervalInformation;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.information.SignalboundaryInformation;
import simulink2dl.invariants.util.AnalyzerUtil;
import simulink2dl.invariants.util.ModelInformation;
import simulink2dl.transform.Transformer;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;

public class InvariantGenerator {

	private Transformer transformer;
	private SimulinkModel simulinkModel;
	private String file;
	private String tempOutput;

	private InvariantGraph invariantGraph;

	// Constants
	public static final boolean INPUT = true;
	public static final boolean DEBUG = true;

	public InvariantGenerator(Transformer transformer, SimulinkModel simulinkModel, String inputsFile,
			String outputsFile) {
		this.transformer = transformer;
		this.simulinkModel = simulinkModel;
		this.file = inputsFile;
		this.tempOutput = outputsFile;

		initalizeVariables();
	}

	private void initalizeVariables() {
		invariantGraph = new InvariantGraph(this.transformer);
	}

	public void generateInvariants() {
		// Get block order from transformer
		Set<SimulinkBlock> unsortedBlocks = new HashSet<SimulinkBlock>();
		List<SimulinkBlock> blockOrder = transformer.generateBlockOrder(simulinkModel.getSubBlocks(), unsortedBlocks);

		// build graph
		buildGraph(blockOrder);

		PluginLogger.info("Graph: ");
		PluginLogger.info(invariantGraph.toString());

		// Some tests
		tests();

		loadInputData();

		// analyze graph and fill with information
		PluginLogger.info(" ---- Generate Information ---- ");
		for (SimulinkBlock block : blockOrder) {
			BlockAnalyzer blockAnalyzer = BlockAnalyzerFactory.build(block, invariantGraph);
			List<InvariantInformation> information = blockAnalyzer.generateInformation();

			if (information.size() > 0) {
				PluginLogger.info("Information for '" + block.getName() + "': " + information.toString());
			}

			for (SimulinkLine signal : block.getOutLines()) {
				invariantGraph.getEdge(signal).addInformation(information);
			}
			DataFlowAnalysis.dataflowAnalysis(transformer, invariantGraph, block);

			// add a macro for each block that is not creating a variable
			if (AnalyzerUtil.nonStateBlocks.contains(block.getType().toLowerCase())) {
				Macro macro = new SimpleMacro(transformer.getEnvironment().getToReplace(block),
						new Variable("R", block.getName()));
				transformer.getDLModel().addMacro(macro);
			}
		}
		PluginLogger.info(" ---- Generate Information END ---- ");
	}

	private void buildGraph(List<SimulinkBlock> blockOrder) {
		for (SimulinkBlock currentBlock : blockOrder) {
			if (this.DEBUG)
				PluginLogger.info("Block: " + currentBlock.getName() + " with Type: " + currentBlock.getType()
						+ " and ID: " + currentBlock.getId());
			for (SimulinkInPort inPort : currentBlock.getInPorts()) {
				SimulinkLine signalLine = inPort.getLine();
				SimulinkBlock srcBlock = signalLine.getSrcPort().getBlock();
				SimulinkBlock dstBlock = signalLine.getDstPort().getBlock();

				invariantGraph.insertNode(srcBlock);
				invariantGraph.insertNode(dstBlock);

				invariantGraph.insertEdge(signalLine, srcBlock, dstBlock);
			}
		}

		if (!(invariantGraph.getAllNodes().size() == simulinkModel.getSubBlocks().size())) {
			PluginLogger.error("Simulink node size: " + simulinkModel.getSubBlocks().size() + " : Graph node size: "
					+ invariantGraph.getAllNodes().size());
		}

		// TODO Check this. Maybe use getContainedLinesRecursively()
		if (!(invariantGraph.getAllEdges().size() == simulinkModel.getContainedLines().size())) {
			PluginLogger.error("Simulink edge size: " + simulinkModel.getContainedLines().size()
					+ " : Graph edge size: " + invariantGraph.getAllEdges().size());
		}
	}

	private void tests() {
		PluginLogger.info(" ---- Tests ---- ");

		PluginLogger.info(" ---- Tests End ---- ");
	}

	private void loadInputData() {
		if (INPUT) {
			ModelInformation.getStartInformationFromModel(transformer, invariantGraph, file);
		}
	}

	public void finalizeInformation() {
		// Finalize invariant graph
		PluginLogger.info(" ---- Finalize Information ---- ");

		applyInformation();

		SafetyAnalysis.checkDivisions(invariantGraph);
		SafetyAnalysis.checkOverflow(invariantGraph);

		List<Macro> allInformation = invariantGraph.finalizeInformation();
		transformer.finalizeTransform();

		printOutAllInvariants(allInformation);

		PluginLogger.info(" ---- Finalize Information END ---- ");
	}

	public void applyInformation() {
		// Create Block order
		PluginLogger.info("Block order: ");
		LinkedList<InvariantNode> blocksNoOutComingEdges = new LinkedList<>();
		for (SimulinkBlock b : transformer.getSimulinkModel().getSubBlocks()) {
			if (b.getOutLines().size() == 0)
				blocksNoOutComingEdges.push(invariantGraph.getNode(b));
		}
		// if model has no blocks with no outcoming edges, add all blocks of block order
		if (blocksNoOutComingEdges.size() == 0) {
			Set<SimulinkBlock> unsortedBlocks = new HashSet<SimulinkBlock>();
			List<SimulinkBlock> blockOrder = transformer
					.generateBlockOrder(transformer.getSimulinkModel().getSubBlocks(), unsortedBlocks);
			for (SimulinkBlock block : blockOrder) {
				blocksNoOutComingEdges.add(invariantGraph.getNode(block));
			}
		}
		PluginLogger.info(blocksNoOutComingEdges.toString());

		// Apply information recursively
		HashMap<InvariantNode, Boolean> isDone = new HashMap<InvariantNode, Boolean>();
		for (InvariantNode block : blocksNoOutComingEdges) {
			applyInformationOnBlock(block, isDone, new LinkedList<SimulinkBlock>());
		}
	}

	public void applyInformationOnBlock(InvariantNode blockNode, HashMap<InvariantNode, Boolean> isDone,
			LinkedList<SimulinkBlock> previousBlocks) {
		if (isDone.get(blockNode) != null) {
			return;
		}

		if (previousBlocks.contains(blockNode.getBlock())) { // check if feedback loop
			handleFeedbackLoop(blockNode, isDone, previousBlocks);
			isDone.put(blockNode, true);
			return;
		}

		previousBlocks.add(blockNode.getBlock());

		// all previous blocks have to be marked as done
		for (SimulinkLine edge : blockNode.getBlock().getInLines()) {
			InvariantNode prev = invariantGraph.getNode(edge.getSrcPort().getBlock());
			if (isDone.get(prev) == null)
				applyInformationOnBlock(prev, isDone, previousBlocks);
		}

		if (isDone.get(blockNode) != null) {
			return;
		}

		// save here, all previous blocks done
		PluginLogger.info("Handle block: " + blockNode.getBlock().getName());
		BlockAnalyzer analyzer = BlockAnalyzerFactory.build(blockNode.getBlock(), invariantGraph);

		LinkedList<InvariantInformation> allInfos = new LinkedList<InvariantInformation>(); // get all information
		PluginLogger.info("All information from incoming edges: " + allInfos.toString());
		PluginLogger.info("All information count: " + String.valueOf(allInfos.size()));
		for (SimulinkLine edge : blockNode.getBlock().getInLines()) {
			for (InvariantInformation information : invariantGraph.getEdge(edge).getAllInformation()) {
				boolean isIn = false;
				for (InvariantInformation info : allInfos) { // is info already in allInfos?
					if (info.equals(information))
						isIn = true; // add, if not
				}
				if (!isIn) { // apply block on information if not already in allInfos
					allInfos.add(information);

					if (information.isDone())
						continue;
					InvariantInformation copyInformation = (InvariantInformation) information.clone();

					int endOfInformation = analyzer.applyInformation(copyInformation, invariantGraph.getEdge(edge));

					if (!copyInformation.isValid()) {
						continue;
					}
					PluginLogger.info("Created new information: " + copyInformation);

					if (endOfInformation != BlockAnalyzer.END_OF_INFORMATION
							&& endOfInformation != BlockAnalyzer.END_OF_INFORMATION_IN_NODE) {
						PluginLogger.info("Put information on all edges");
						for (SimulinkLine nextSignalLine : blockNode.getBlock().getOutLines()) {
							InvariantEdge nextEdge = invariantGraph.getEdge(nextSignalLine);
							nextEdge.addInformation(copyInformation);
						}
					} else if (endOfInformation == BlockAnalyzer.END_OF_INFORMATION_IN_NODE) {
						PluginLogger.info("Put information into node");
						blockNode.addInformation(copyInformation);
					}
					information.done();
				}
			}
		}

		// check if there is no information of a specific type on any outgoing edge
		for (SimulinkLine signalLine : blockNode.getBlock().getOutLines()) {
			InvariantEdge accordingEdge = invariantGraph.getEdge(signalLine);

			boolean signal = false, control = false, data = false;
			for (InvariantInformation info : accordingEdge.getAllInformation()) {
				if (info instanceof SignalboundaryInformation || info instanceof DiscreteSignalInformation)
					signal = true;
				if (info instanceof ControlInformation)
					control = true;
				if (info instanceof DataInformation)
					data = true;
			}
			if (!signal) {
				List<InvariantInformation> newInfos = analyzer.noInformationApplied(accordingEdge,
						SignalboundaryInformation.class.toString());
				accordingEdge.addInformation(newInfos);
			}
			if (!control) {
				List<InvariantInformation> newInfos = analyzer.noInformationApplied(accordingEdge,
						ControlInformation.class.toString());
				accordingEdge.addInformation(newInfos);
			}
			if (!data) {
				List<InvariantInformation> newInfos = analyzer.noInformationApplied(accordingEdge,
						DataInformation.class.toString());
				accordingEdge.addInformation(newInfos);
			}
		}

		isDone.put(blockNode, true);
	}

	public void handleFeedbackLoop(InvariantNode block, HashMap<InvariantNode, Boolean> isDone,
			LinkedList<SimulinkBlock> previousBlocks) {
		PluginLogger.info("");
		PluginLogger.info("Feedback-Loop on block: " + block.getBlock().getName());
		double start = System.currentTimeMillis();

		if (isDone.get(block) != null) {
			return;
		}

		double applyInfo = 0, writeInfos = 0;
		if (AnalyzerUtil.feedtroughBlocks.contains(block.getBlock().getType().toLowerCase())
				|| AnalyzerUtil.discreteBlocks.contains(block.getBlock().getType().toLowerCase())) {
			List<InvariantNode> loop = new LinkedList<InvariantNode>();
			if (LoopAnalysis(block, loop)) { // LoopAnalysis returns loop
				loop.add(loop.get(0));
				loop.remove(0);
				PluginLogger.info("Loop is : " + loop.toString());

				HashMap<String, List<InvariantInformation>> preSimulationInformation = new HashMap<String, List<InvariantInformation>>();
				List<InvariantInformation> outGoingInformation = new LinkedList<>();
				boolean fixpointFound = false;
				try {
					// iterate each block in loop and be sure that previous blocks are done
					boolean isStateBlock = false;
					for (InvariantNode loopBlock : loop) {
						if (AnalyzerUtil.stateBlocks.contains(loopBlock.getBlock().getType().toLowerCase()))
							isStateBlock = true;
						for (SimulinkLine inSignal : loopBlock.getBlock().getInLines()) {
							InvariantNode srcNode = invariantGraph.getNode(inSignal.getSrcPort().getBlock());
							if (!loop.contains(srcNode) && isDone.get(srcNode) == null) { // block is not done yet
								applyInformationOnBlock(srcNode, isDone, previousBlocks);
							}
						}
						// add information to preSimulationInformation
						for (SimulinkLine outSignal : loopBlock.getBlock().getOutLines()) {
							InvariantEdge outEdge = invariantGraph.getEdge(outSignal);
							preSimulationInformation.put(outSignal.toLineString(),
									new LinkedList<>(outEdge.getAllInformation()));
						}
					}

					if (!isStateBlock)
						return;

					// all previous information are applied
					// generate start information for loop
					HashMap<InvariantEdge, List<InvariantInformation>> startInformations = new HashMap<>();
					for (InvariantNode loopBlockNode : loop) {
						BlockAnalyzer loopBlockAnalyzer = BlockAnalyzerFactory.build(loopBlockNode.getBlock(),
								invariantGraph);
						for (SimulinkLine signal : loopBlockNode.getBlock().getOutLines()) {
							InvariantEdge edge = invariantGraph.getEdge(signal);
							List<InvariantInformation> startInformation = loopBlockAnalyzer
									.generateInformationForFeedbackSimulation();
							for (InvariantInformation in : startInformation) {
								Macro macro = new SimpleMacro(new PortIdentifier(""), (Term) in);
								transformer.getDLModel().finalizeSingleMacro(macro);
							}
							startInformations.put(edge, startInformation);
						}
					}
					PluginLogger.info("Start information: " + startInformations.toString());

					// Simulation START
					List<HashMap<InvariantEdge, List<InvariantInformation>>> allSimulationInformation = new LinkedList<>();
					allSimulationInformation.add(0, startInformations);
					for (int i = 0; i < 100 && !fixpointFound; i++) {
						HashMap<InvariantEdge, List<InvariantInformation>> currentLoopInformation = new HashMap<InvariantEdge, List<InvariantInformation>>();

						// update all information on each block in the loop
						HashMap<InvariantEdge, List<InvariantInformation>> previousLoopInformation = allSimulationInformation
								.get(i);
						for (InvariantNode loopBlock : loop) {
							for (SimulinkLine line : loopBlock.getBlock().getOutLines()) {
								InvariantEdge accordingEdge = invariantGraph.getEdge(line);
								accordingEdge.deleteAllInformation();
								if (previousLoopInformation.get(accordingEdge) != null)
									accordingEdge.addInformation(previousLoopInformation.get(accordingEdge));
							}
						}

						// do all of calculations
						for (InvariantNode loopBlock : loop) {
							BlockAnalyzer loopBlockAnalyzer = BlockAnalyzerFactory.build(loopBlock.getBlock(),
									invariantGraph);
							double start2 = System.currentTimeMillis();
							boolean changed = false;
							for (SimulinkLine signal : loopBlock.getBlock().getInLines()) {
								InvariantEdge accordingInEdge = invariantGraph.getEdge(signal);
								for (InvariantInformation inf : accordingInEdge.getAllInformation()) {
									InvariantInformation copyInformation = (InvariantInformation) inf.clone();
									if (inf.isDone())
										continue;
									if (AnalyzerUtil.controlBlocks
											.contains(loopBlock.getBlock().getType().toLowerCase())) {
										if (inf instanceof SignalboundaryInformation
												|| inf instanceof DiscreteSignalInformation) {
											List<InvariantInformation> newInformation = loopBlockAnalyzer
													.evaluateConditionForFeedbackSimulation();

											for (SimulinkLine nextSignalLine : loopBlock.getBlock().getOutLines()) {
												InvariantEdge nextEdge = invariantGraph.getEdge(nextSignalLine);
												nextEdge.addInformation(newInformation);
											}
											changed = true;
											break;
										}
									} else {
										int endOfInformation = loopBlockAnalyzer.applyInformation(copyInformation,
												accordingInEdge);

										if (!copyInformation.isValid()) {
											continue;
										}

										if (endOfInformation != BlockAnalyzer.END_OF_INFORMATION
												&& endOfInformation != BlockAnalyzer.END_OF_INFORMATION_IN_NODE) {
											for (SimulinkLine nextSignalLine : loopBlock.getBlock().getOutLines()) {
												InvariantEdge nextEdge = invariantGraph.getEdge(nextSignalLine);
												nextEdge.addInformation(copyInformation);
											}
											changed = true;
										}
									}
									inf.done();
								}
							}
							applyInfo += (System.currentTimeMillis() - start2);

							start2 = System.currentTimeMillis();
							for (SimulinkLine signal : loopBlock.getBlock().getOutLines()) {
								InvariantEdge accordingOutEdge = invariantGraph.getEdge(signal);
								// replace all replaceableterms
								for (InvariantInformation inf : accordingOutEdge.getAllInformation()) {
									Macro replacer = new SimpleMacro(new PortIdentifier(""), (Term) inf);
									transformer.getDLModel().finalizeSingleMacro(replacer);
								}
								LinkedList<InvariantInformation> newInformation = (LinkedList<InvariantInformation>) accordingOutEdge
										.getAllInformation();
								if (changed && previousLoopInformation.get(accordingOutEdge) != null) {
									for (int j = 0; j < previousLoopInformation.get(accordingOutEdge).size(); j++) {
										newInformation.removeFirst();
									}
								}
								currentLoopInformation.put(accordingOutEdge,
										new LinkedList<InvariantInformation>(newInformation));
							}
							writeInfos += (System.currentTimeMillis() - start2);
						} // end of for of loop

						boolean outSignalContainsInformation = false;
						for (SimulinkLine line : block.getBlock().getOutLines()) {
							InvariantEdge accordingEdge3 = invariantGraph.getEdge(line);
							if (currentLoopInformation.get(accordingEdge3) != null
									&& currentLoopInformation.get(accordingEdge3).size() > 0) {
								if (!outSignalContainsInformation) {
									for (InvariantInformation info : outGoingInformation) {
										for (InvariantInformation infoNew : currentLoopInformation
												.get(accordingEdge3)) {
											if (info.equals(infoNew)) {
												fixpointFound = true;
											}
										}
									}
									outGoingInformation.addAll(currentLoopInformation.get(accordingEdge3));
								}
								outSignalContainsInformation = true;
							}
						}
						if (!outSignalContainsInformation)
							throw new Exception("No information for outgoing edge");

						allSimulationInformation.add((i + 1), currentLoopInformation);
					}
					// Simulation END

					for (InvariantNode loopBlock : loop) {
						for (SimulinkLine line : loopBlock.getBlock().getOutLines()) {
							InvariantEdge accordingEdge2 = invariantGraph.getEdge(line);
							accordingEdge2.deleteAllInformation();
						}
					}

					int matchCount = 0;
					for (int i = 0; i < outGoingInformation.size(); i++) {
						for (int j = i + 1; j < outGoingInformation.size(); j++) {
							if (outGoingInformation.get(i).equals(outGoingInformation.get(j))) {
								outGoingInformation.remove(j);
								j--;
								matchCount++;
								fixpointFound = true;
							}
						}
					}

				} catch (Exception e) {

				} finally {
					DiscreteSignalInformation discrete = new DiscreteSignalInformation();
					for (InvariantInformation inf : outGoingInformation) {
						if (inf instanceof SignalboundaryInformation || inf instanceof DiscreteSignalInformation) {
							discrete.addElement(inf);
						}
					}
					discrete.finalizeInformation(AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invariantGraph));

					SignalboundaryInformation otherFixpoint = new SignalboundaryInformation(invariantGraph);
					boolean otherFixpointFound = false;
					// try to sum information
					if (!fixpointFound) {
						// fixpoint not found
						boolean leq = discrete.crossesValue(RelationType.LESS_EQUAL, "0");
						boolean less = discrete.crossesValue(RelationType.LESS_THAN, "0");
						boolean geq = discrete.crossesValue(RelationType.GREATER_EQUAL, "0");
						boolean greater = discrete.crossesValue(RelationType.GREATER_THAN, "0");

						if (leq || geq) {
							Relation lowgreZero = new Relation(
									AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invariantGraph),
									leq ? RelationType.LESS_EQUAL : RelationType.GREATER_EQUAL, new RealTerm(0));
							Relation lowgreZeroInf = new Relation(
									AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invariantGraph),
									leq ? RelationType.GREATER_THAN : RelationType.LESS_THAN,
									leq ? new Variable("R", "-inf") : new Variable("R", "inf"));
							if (leq) {
								otherFixpoint.getDisjunction()
										.addElement(new IntervalInformation(invariantGraph, lowgreZeroInf, lowgreZero));
							} else {
								otherFixpoint.getDisjunction()
										.addElement(new IntervalInformation(invariantGraph, lowgreZero, lowgreZeroInf));
							}
							otherFixpointFound = true;
						} else {
							if (less || greater) {
								Relation lowgreZero = new Relation(
										AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invariantGraph),
										less ? RelationType.LESS_THAN : RelationType.GREATER_THAN, new RealTerm(0));
								Relation lowgreZeroInf = new Relation(
										AnalyzerUtil.getOutVariableOfBlock(block.getBlock(), invariantGraph),
										less ? RelationType.GREATER_THAN : RelationType.LESS_THAN,
										less ? new Variable("R", "-inf") : new Variable("R", "inf"));
								if (less) {
									otherFixpoint.getDisjunction().addElement(
											new IntervalInformation(invariantGraph, lowgreZeroInf, lowgreZero));
								} else {
									otherFixpoint.getDisjunction().addElement(
											new IntervalInformation(invariantGraph, lowgreZero, lowgreZeroInf));
								}
								otherFixpointFound = true;
							}
						}
					}

					PluginLogger.info("Outgoing Invariant: ");
					PluginLogger.info(discrete.toString());
					PluginLogger.info(otherFixpoint.toString());

					// reload startInformation
					for (InvariantNode loopBlock : loop) {
						for (SimulinkLine outSignal : loopBlock.getBlock().getOutLines()) {
							InvariantEdge outEdge = invariantGraph.getEdge(outSignal);
							if (preSimulationInformation.get(outSignal.toLineString()) != null
									&& preSimulationInformation.get(outSignal.toLineString()).size() > 0) {
								outEdge.addInformation(preSimulationInformation.get(outSignal.toLineString()));
							}
							if (loopBlock.getBlock().getId() == block.getBlock().getId()) {
								if (fixpointFound) {
									outEdge.addInformation(discrete);
								} else if (otherFixpointFound) {
									outEdge.addInformation(otherFixpoint);
								}
							}
						}
						isDone.put(loopBlock, true);
					}
				}
			} // end of LoopAnalysis
		} // end of AnalyzerUtil
		PluginLogger.info("Applying took: " + applyInfo + " ms");
		PluginLogger.info("Write Info took: " + writeInfos + " ms");
		PluginLogger.info("Feedback-Loop on block: " + block.getBlock().getName() + " END\nTook: "
				+ (System.currentTimeMillis() - start) + " ms\n");
	}

	// Copied and changed: see
	// simulink2dl.transform.block_stucture/StructureHandler.java at
	// branch philipp.wonschik
	private boolean LoopAnalysis(InvariantNode blockNode, List<InvariantNode> potentialLoop) {
		boolean returnValue = false;
		// if the block to analyze is already part of the potential loop, then a loop is
		// found
		if (potentialLoop.contains(blockNode)) {
			return true;
		}
		potentialLoop.add(blockNode);

		// getting all outgoing signals for this block
		Collection<SimulinkLine> outSignals = blockNode.getBlock().getOutLines();

		// iterating over all signals to get the successor of this block and continuing
		// loop analysis
		for (SimulinkLine signal : outSignals) {
			InvariantNode successorBlock = invariantGraph.getNode(signal.getDstPort().getBlock());
			boolean stillALoop = LoopAnalysis(successorBlock, potentialLoop);
			// if an invocation of the method returned false, the last inserted block can't
			// be part of the loop
			if (!stillALoop) {
				potentialLoop.remove(potentialLoop.size() - 1);
			}
			if (stillALoop) {
				returnValue = true;
			}
		}
		return returnValue;
	}

	public void printOutAllInvariants(List<Macro> allInformation) {
		try (FileWriter writer = new FileWriter(tempOutput); BufferedWriter bw = new BufferedWriter(writer)) {

			List<Term> done = new LinkedList<Term>();
			for (Macro m : allInformation) {
				if (!done.contains(m.getReplaceWith()))
					bw.write(m.getReplaceWith().toString() + "\r\n");
				done.add(m.getReplaceWith());
			}

			if (invariantGraph.getAllSecurityProperties().size() > 0) {
				bw.write("\r\nZu zeigen:\r\n");
				for (Term t : invariantGraph.getAllSecurityProperties()) {
					bw.write(t.toString() + "\r\n");
				}
			}
		} catch (IOException e) {
			PluginLogger.error("Unable to write to file: " + tempOutput + " : " + e);
		}

	}

	public String getInputsFile() {
		return file;
	}

}
