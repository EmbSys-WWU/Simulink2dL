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
import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringToTerm;

public class StructureTransformer extends BlockTransformer {

	String oldLeftSide;
	TransformerFactory factory;
	boolean isLoop;

	public StructureTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
		isLoop = false;
		try {
			factory = new TransformerFactory();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		for (Macro macro : macros) {
			if (!(macro instanceof ConditionalMacro)) {
				if (isLoop) {
					String macroString = macro.toString();
					try {
						long before = System.currentTimeMillis();
						String result = sendQuerry(macroString, "WolframAlpha");
						long after = System.currentTimeMillis();
						long delta = after - before;
						PluginLogger.info("[EVALUATION] The WA querry took " + delta + " milliseconds");
						String toReplaceString = result.split(":")[0];
						String replaceWithString = result.split(":")[1];
						Term term = StringToTerm.parseString(replaceWithString);
						ReplaceableTerm newToReplace = new PortIdentifier(toReplaceString);
						macro = new SimpleMacro(newToReplace, term);
					} catch (Exception e) {
						PluginLogger
								.error("The macro " + macroString + " could not be solved by the external tool.");
					}
				}
			}
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO: implement BlockStructure Block in simulink model
		PluginLogger.error("Blockstructures are not implemented!");
		List<Macro> macros = new ArrayList<>();
//		BlockStructure struct = (BlockStructure) block;
//		BlockTransformer transformer = null;
//
//		// getting all port IDs of the outPorts of the structure
//		List<String> portIDs = new ArrayList<>();
//		for (SimulinkOutPort port : struct.getOutPorts()) {
//			portIDs.add(environment.getPortID(port));
//		}
//
//		for (SimulinkBlock innerBlock : struct.getBlockList()) {
//			transformer = factory.getBlockTransformer(innerBlock, simulinkModel, dlModel, environment);
//
//			if (transformer != null) {
//				macros.addAll(transformer.createMacro(innerBlock));
//			} else {
//				String type = innerBlock.getType();
//
//				Logger.error("No transformer for blocktype " + type + " implemented.");
//			}
//
//		}
//		try {
//			macros = mergeMacros(macros, portIDs);
//		} catch (Exception e) {
//			Logger.error("The loop to transform was too komplex to handle.");
//		}
		return macros;
	}

	/**
	 * Merging the final macro out of the list of macros of the inner blocks of the
	 * structure
	 * 
	 * @param macros The list of macros to be merged into one
	 * @return The merged macro for this structure
	 */
	private List<Macro> mergeMacros(List<Macro> macros, List<String> portIDs) {
		// getting all macros that are at an outPort
		List<Macro> macrosToKeep = new ArrayList<>();
		for (int i = 0; i < macros.size(); i++) {
			Macro check = macros.get(i);
			String macroPort = check.getToReplace().toString();
			for (String outPort : portIDs) {
				if (macroPort.equals(outPort)) {
					macrosToKeep.add(check);
				}
			}
		}

		// applying all macros to all other except the ones to keep
		macros.removeAll(macrosToKeep);
		for (int outerIndex = 0; outerIndex < macros.size(); outerIndex++) {
			Macro outerMacro = macros.get(outerIndex);
			for (int innerIndex = 0; innerIndex < macros.size(); innerIndex++) {
				Macro innerMacro = macros.get(innerIndex);
				if (outerMacro.equals(innerMacro)) {
					continue;
				}

				// check whether the outer macro influences the inner macro
				if (innerMacro.containsTerm(outerMacro.getToReplace())) {
					// remove old macro
					macros.remove(innerIndex);

					// create and add new macros
					List<Macro> newMacros = innerMacro.applyOtherMacro(outerMacro);
					macros.addAll(innerIndex, newMacros);

					// increase inner index, skip new macros
					innerIndex += newMacros.size() - 1;
				}
			}
		}

		// applying all other macros to the ones to keep
		List<Macro> copyList = new ArrayList<>();
		for (Macro keep : macrosToKeep) {
			for (Macro apply : macros) {
				if (!keep.equals(apply)) {
					keep.applyOtherMacro(apply);
				}
			}
			copyList.add(keep.createDeepCopy());
		}
		if (copyList.size() > 1 && isLoop) {
			for (Macro keep : macrosToKeep) {
				for (Macro apply : copyList) {
					if (!keep.equals(apply)) {
						keep.applyOtherMacro(apply);
					}
				}
			}
		}
		return macrosToKeep;
	}

	public void setIsLoop(boolean value) {
		this.isLoop = value;
	}

	private String sendQuerry(String query, String identifier) throws Exception {
		String value = "";
		switch (identifier) {
		case "WolframAlpha":
			value = sendWolframAlphaQuery(query);
		}
		return value;
	}

	private String sendWolframAlphaQuery(String query) throws Exception {
		String value = "";
		String appID = "LTVUGY-VH4JT22YW3";

		// TODO: Enable Wolfram Alpha queries
		PluginLogger.error("Wolfram Alpha queries are currently disabled!");

//		WAEngine engine = new WAEngine();
//		engine.setAppID(appID);
//		engine.addFormat("plaintext");
//
//		WAQuery waquery = engine.createQuery();
//		// make query WA readable
//		query = convertToWAQuery(query);
//		waquery.setInput(query);
//
//		WAQueryResult result = engine.performQuery(waquery);
//		WAPod[] pods = result.getPods();
//		for (WAPod pod : pods) {
//			if ("Result".equals(pod.getTitle())) {
//				WASubpod[] subPods = pod.getSubpods();
//				for (WASubpod subPod : subPods) {
//					Visitable visitable = subPod.getContents()[0];
//					String resultText = ((WAPlainText) visitable).getText();
//					value = convertFromWAQuery(resultText);
//					break;
//				}
//			}
//		}
		return value;
	}

	private String convertToWAQuery(String query) {

		oldLeftSide = query.split(":")[0];
		oldLeftSide = oldLeftSide.trim();
		query = query.replaceAll(oldLeftSide, "a");
		query = query.replaceAll("#out", "x_");
		query = query + " for a";
		query = "solve " + query;
		query = query.replaceAll(":", "=");

		return query;
	}

	private String convertFromWAQuery(String query) {

		query = query.replaceAll("x_", "#out");
		query = query.replaceAll("=", ":");
		query = query.replaceAll("a", oldLeftSide);
		query = query.replaceAll("\\s+", "");

		int length = query.length();
		for (int i = 1; i < length; i++) {
			char character = query.charAt(i);
			if (character == '#' || character == '(') {
				char check = query.charAt(i - 1);
				if (check != '*' && check != '(' && check != '-' && check != '+' && check != '/') {
					query = query.substring(0, i) + "*" + query.substring(i, length);
					length++;
				}
			}
			if (character == ')') {
				if (i + 1 < length) {
					char check = query.charAt(i + 1);
					if (check != '*' && check != ')' && check != '-' && check != '+' && check != '/') {
						query = query.substring(0, i) + "*" + query.substring(i, length);
						length++;
					}
				}
			}
		}

		return query;
	}

}
