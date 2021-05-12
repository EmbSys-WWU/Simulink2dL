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
package simulink2dl.transform;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.transform.blocktransformer.BlockTransformer;
import simulink2dl.transform.blocktransformer.TransformerFactory;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.optimizer.ConditionalChoiceOptimizer;
import simulink2dl.transform.optimizer.ContradictionOptimizer;
import simulink2dl.transform.optimizer.EvolutionDomainOptimizer;
import simulink2dl.transform.optimizer.FormulaOptimizer;
import simulink2dl.transform.optimizer.Optimizer;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.order.BlockOrder;

/**
 * This class contains all functions to transform Simulink systems and blocks in
 * an equivalent representation in dL.
 * 
 * @author Timm Liebrenz
 */
public class Transformer {

	private SimulinkModel simulinkModel;

	private DLModelSimulink dlModel;

	private Environment environment;

	private Set<String> lastBlocks;

	private Set<HybridContract> transformationContracts;

	public Transformer(SimulinkModel model, Set<HybridContract> contracts) {
		this.simulinkModel = model;
		this.dlModel = new DLModelSimulink();

		this.environment = new Environment(dlModel, model, contracts);

		lastBlocks = new HashSet<String>();

		lastBlocks.add("FromWorkspace");
		lastBlocks.add("S-Function");
		lastBlocks.add("DiscreteIntegrator");
		lastBlocks.add("Integrator");
		lastBlocks.add("ZeroOrderHold");
		lastBlocks.add("DiscreteTransferFcn");
		lastBlocks.add("UnitDelay");

		transformationContracts = contracts;
	}

	public SimulinkModel getSimulinkModel() {
		return this.simulinkModel;
	}

	public DLModelSimulink getDLModel() {
		return this.dlModel;
	}

	public Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * Prepares the Simulink model for the transformation. Block names are changed
	 * to remove white spaces and line breaks.
	 */
	private void prepareModel() {
		PluginLogger.warning("Block names will not be updated!");
		for (SimulinkBlock block : simulinkModel.getSubBlocks()) {
			String newName = block.getName();
			newName = newName.replace(" ", "");
			newName = newName.replace("\n", "");
			newName = newName.replace("\\n", "");
			newName = newName.replace("-", "");
			newName = newName.replace("_", "");
			// TODO change block name
			// block.setName(newName);
		}

		List<String> conIds = new LinkedList<String>();
		for (HybridContract cntrct : this.transformationContracts) {
			conIds.add(cntrct.getId());
		}

		PluginLogger.warning("Subsystems will not be removed!");
		// TODO remove subsystems
		// SubsystemRemover2 sr = new SubsystemRemover2(simulinkModel, conIds);
		// sr.initialize();
		// sr.eliminateOrdinarySubsystems();
		// sr.removeBlocksInsideServiceSystem();

		// TODO handle contract substitutions here

		environment.prepareModel();

		environment.checkModelIntegrity();
	}

	public List<SimulinkBlock> generateBlockOrder(UnmodifiableCollection<SimulinkBlock> simulinkBlocks,
			Set<SimulinkBlock> unsortedBlocks) {
		BlockOrder blockOrder = new BlockOrder();

		return blockOrder.generateBlockOrder(simulinkBlocks, unsortedBlocks);
	}

	/**
	 * Transforms the internal model in a dL representation.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void transform(Set<String> selectedHandler) throws FileNotFoundException, IOException {
		prepareModel();

		TransformerFactory transformerFactory = new TransformerFactory();

		// create transformation order for all blocks
		// added block structure handling by Philipp Wonschik
		Set<SimulinkBlock> unsortedBlocks = new HashSet<SimulinkBlock>();
		List<SimulinkBlock> blockList = generateBlockOrder(simulinkModel.getSubBlocks(), unsortedBlocks);

		// structure handling
		// TODO add structure handling
//		Logger.warning("No structure handling!");
//		Logger.info("[EVALUATION] " + blockList.size() + " blocks before structurehandling.");
//		long analyseStart = System.currentTimeMillis();
//		StructureHandler structureHandler = new StructureHandler(blockList, unsortedBlocks, selectedHandler);
//		structureHandler.handleStructures();
//		Set<Block> blockSet = new HashSet<>(blockList);
//		blockList = blockOrder.generateBlockOrder(blockSet, unsortedBlocks);
//		long analyseEnd = System.currentTimeMillis();
//		Logger.info("[EVALUATION] " + (analyseEnd - analyseStart) + " ms for structure analysis.");
//		Logger.info("[EVALUATION] " + blockList.size() + " blocks after structurehandling.");

		// transform model
		for (SimulinkBlock block : blockList) {
			BlockTransformer blockTransformer = transformerFactory.getBlockTransformer(block, simulinkModel, dlModel,
					environment);
			if (selectedHandler.contains("Controlflow")) {
				blockTransformer.setHandleControlFlow(true);
			}
			blockTransformer.transformBlock(block);
		}

	}

	public void finalizeTransform() {
		dlModel.finalizeModel(environment);
	}

	public void optimize(Set<String> selectedOptimizer) {
		if (selectedOptimizer.contains("ConditionalChoice")) {
			Optimizer condChoiceOptimizer = new ConditionalChoiceOptimizer();
			condChoiceOptimizer.run(dlModel);
		}

		if (selectedOptimizer.contains("Formula")) {
			Optimizer formulaOptimizer = new FormulaOptimizer();
			formulaOptimizer.run(dlModel);
		}

		if (selectedOptimizer.contains("Contradiction")) {
			Optimizer contradictionOptimizer = new ContradictionOptimizer();
			contradictionOptimizer.run(dlModel);
		}

		if (selectedOptimizer.contains("EvolutionDomain")) {
			Optimizer evolutionDomainOptimizer = new EvolutionDomainOptimizer();
			evolutionDomainOptimizer.run(dlModel);
		}
	}

	/**
	 * Creates files containing the model in a dL representation.
	 */
	public void writeOut(String pathToFile) {
		// check whether a transformed model is present
		if (dlModel == null) {
			PluginLogger.error("Tried to write model when no model is created");
			return;
		}
		// TODO: maybe use a default transformation instead?

		try {
			// create a writer and output file
			Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(pathToFile + ".kyx"), "utf-8"));

			PluginLogger.info("path to file: " + pathToFile);

			// write the transformed model into the file
			writer.write(dlModel.createOutputString(Constants.writeMultiLineTestFormulas,
					Constants.writeMultiLineEvolutionDomains));

			// close the writer
			writer.close();

		} catch (IOException ioe) {
			PluginLogger.exception("Could not write output file.", ioe);
		}
		
		writeOutContracts(pathToFile+"AgentContract");
	}
	
	/**
	 * Writes out the RL agent contract to separate dL model
	 * @param pathToFile
	 */
	public void writeOutContracts(String pathToFile) {
		List<HybridContract> rLContracts = dlModel.getRLContracts();
		for(int i = 0; i<rLContracts.size(); i++) {
			Writer writer;
			try {
				HybridContract rLContract = rLContracts.get(i);
				DLModel rLContractModel = rLContract.todLModel();
				writer = new BufferedWriter(
						 new OutputStreamWriter(new FileOutputStream(pathToFile +i+ ".kyx"), "utf-8"));
	
				PluginLogger.info("path to file: " + pathToFile);
	
				// write the transformed model into the file
				writer.write(rLContractModel.createOutputString(Constants.writeMultiLineTestFormulas,
						Constants.writeMultiLineEvolutionDomains));
	
				// close the writer
				writer.close();
			} catch (IOException ioe) {
				PluginLogger.exception("Could not write contract output file.", ioe);
			}
			
		}
	}

}
