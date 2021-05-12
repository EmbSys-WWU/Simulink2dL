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
package simulink2dl.transform.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import simulink2dl.util.PluginLogger;

/**
 * This singleton class contains all mappings of block names to transformer
 * classes.
 * 
 * @author Timm Liebrenz
 *
 */
public class TransformerMapping {

	private static TransformerMapping instance;

	private List<Mapping> mappings;

	private boolean initialized;

	private static final String empty = "simulink2dl.transform.blocktransformer.EmptyTransformer";

	public class Mapping {
		private String blockType;
		private String className;

		public Mapping(String blockType, String className) {
			this.blockType = blockType;
			this.className = className;
		}

		public String getBlockType() {
			return blockType;
		}

		public String getClassName() {
			return className;
		}
	}

	private TransformerMapping() {
		mappings = new LinkedList<Mapping>();
		initialized = false;
	}

	public static TransformerMapping getInstance() {
		if (instance == null) {
			instance = new TransformerMapping();
		}
		return instance;
	}

	public void addMapping(String blockType, String transformerName) {
		String mapping = getMapping(blockType);
		if (mapping != null && !mapping.isEmpty() && !mapping.equals(empty)) {
			PluginLogger.warning("Transformer mapping for block type \"" + blockType
					+ "\" already exists. Overwriting existing mapping.");
		}
		mappings.add(new Mapping(blockType, transformerName));
	}

	public String getMapping(String blockType) {
		for (Mapping inMapping : mappings) {
			if (inMapping.getBlockType().equals(blockType)) {
				return inMapping.getClassName();
			}
		}
		return empty;
	}

	public void initialize(Bundle bundle) {
		try {
			URL configURL = bundle.getEntry("/config/transformer.cfg");
			File config = new File(FileLocator.toFileURL(configURL).getPath());

			FileReader fr = new FileReader(config);
			BufferedReader br = new BufferedReader(fr);
			StringBuffer sb = new StringBuffer();
			String blockType;
			String transformer;

			// currently the configuration file needs to have the following form:
			// lines with odd numbers contain block types
			// the following line with even number contains the associated block transformer
			while ((blockType = br.readLine()) != null && (transformer = br.readLine()) != null) {
				if (!blockType.isBlank() && !transformer.isBlank()) {
					addMapping(blockType, transformer);
				}
			}
			initialized = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean initialized() {
		return initialized;
	}
}
