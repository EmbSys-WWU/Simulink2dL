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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.transform.Environment;
import simulink2dl.transform.blocktransfomer.RL.ServiceTransformerRL;
import simulink2dl.transform.config.TransformerMapping;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.util.PluginLogger;

/**
 * This factory provides transformation classes that handle the transformation
 * of Simulink blocks. The mapping of block types to transformation classes is
 * given in a configuration file.
 * 
 * @author Timm Liebrenz
 *
 */
public class TransformerFactory {

	/**
	 * Map that stores the Simulink block type to Transformer class mappings.
	 */
	private Map<String, BlockTransformer> blockTransformers;

	private TransformerMapping mapping;

	public TransformerFactory() throws FileNotFoundException, IOException {
		blockTransformers = new HashMap<String, BlockTransformer>();
		mapping = TransformerMapping.getInstance();
	}

	public BlockTransformer getBlockTransformer(SimulinkBlock block, SimulinkModel simulinkModel,
			DLModelSimulink dlModel, Environment environment) {
		if (block == null) {
			PluginLogger.error("Invalid block.");
			return null;
		}
		String blockType = block.getType();
		if (blockType == null | blockType.isEmpty()) {
			PluginLogger.error("Invalid block type.");
			return null;
		}
		PluginLogger.info("transform block of type " + blockType);

		if (block.getName().startsWith("Service")) {
			return new ServiceTransformer(simulinkModel, dlModel, environment);
		} else if (block.getName().startsWith("RLService")) {
			return new ServiceTransformerRL(simulinkModel, dlModel, environment);
		}

		switch (blockType) {
		// special handling for Subsystems
		case "SubSystem":
			return createSubSystemTransformer(block, simulinkModel, dlModel, environment);
		case "Structure":
			// return new StructureTransformer(simulinkModel, dlModel, environment);
			PluginLogger.error("Structure transformer not yet implemented!");
			return new EmptyTransformer(simulinkModel, dlModel, environment);
		case "Loop":
			// StructureTransformer transformer = new StructureTransformer(simulinkModel,
			PluginLogger.error("Structure transformer not yet implemented!");
			return new EmptyTransformer(simulinkModel, dlModel, environment);

		default:
			// create the block transformer according to the transformer mappings
			// the configuration file is part in the simulink2dl project
			// simulink2dl/config/transformer.cfg
			String transformerClassName = mapping.getMapping(blockType);
			if (transformerClassName != null && !transformerClassName.isEmpty()) {
				Class<? extends BlockTransformer> transformerClass;

				Class<? extends SimulinkModel> smClass = simulinkModel.getClass();
				Class<? extends DLModelSimulink> dlmClass = dlModel.getClass();
				Class<? extends Environment> envClass = environment.getClass();
				try {
					transformerClass = (Class<? extends BlockTransformer>) Class.forName(transformerClassName);
					return transformerClass.getDeclaredConstructor(smClass, dlmClass, envClass)
							.newInstance(simulinkModel, dlModel, environment);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return new EmptyTransformer(simulinkModel, dlModel, environment);
			} else {
				PluginLogger.error("Invalid transformer mapping for block type \"" + blockType + "\"");
				return new EmptyTransformer(simulinkModel, dlModel, environment);
			}
		}
	}

	private BlockTransformer createSubSystemTransformer(SimulinkBlock block, SimulinkModel simulinkModel,
			DLModelSimulink dlModel, Environment environment) {

		return new ServiceTransformer(simulinkModel, dlModel, environment);

	}

	public BlockTransformer getBlockTransformerAlternative(String blockName, SimulinkModel simulinkModel,
			DLModelSimulink dlModel, Environment environment) {
		BlockTransformer t = blockTransformers.get(blockName);
		if (t == null) {
			t = createBlockTransformer(blockName, simulinkModel, dlModel, environment);
		}
		return t;
	}

	private BlockTransformer createBlockTransformer(String blockType, SimulinkModel simulinkModel,
			DLModelSimulink dlModel, Environment environment) {

		String className = mapping.getMapping(blockType);
		if (className == null || className == "") {
			PluginLogger.error("No transformer specified for block type \"" + blockType + "\"");
			return new EmptyTransformer(simulinkModel, dlModel, environment);
		}

		try {
			URL[] urls = { TransformerFactory.class.getResource("") };
			URLClassLoader cl = new URLClassLoader(urls);

			BlockTransformer t = (BlockTransformer) cl.loadClass(className).newInstance();
			blockTransformers.put(blockType, t);

			cl.close();

			return t;
		} catch (InstantiationException e) {
			PluginLogger.error("Transformer for block type " + blockType + " can not be instantiated. Class file: \""
					+ className + "\"");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			PluginLogger.error("Transformer for block type " + blockType
					+ " can not be created. The class file is not accessible. Class file: \"" + className + "\"");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			PluginLogger.error("Transformer for block type " + blockType
					+ " can not be created. Class file cannot be found. Class file: \"" + className + "\"");
			e.printStackTrace();
		} catch (IOException e) {
			PluginLogger.error("Could not close URLClassLoader.");
			e.printStackTrace();
		}

		return new EmptyTransformer(simulinkModel, dlModel, environment);
	}

}
