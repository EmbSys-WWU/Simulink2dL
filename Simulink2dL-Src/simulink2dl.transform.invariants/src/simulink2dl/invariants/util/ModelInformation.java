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
package simulink2dl.invariants.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.graph.InvariantEdge;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.invariants.information.ControlInformation;
import simulink2dl.invariants.information.DiscreteSignalInformation;
import simulink2dl.invariants.information.EqualityInformation;
import simulink2dl.invariants.information.IntervalInformation;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.information.SignalboundaryInformation;
import simulink2dl.transform.Environment;
import simulink2dl.transform.Transformer;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringToTerm;

public class ModelInformation {

	public static void getStartInformationFromModel(Transformer transformer, InvariantGraph invGraph, String folder) {
		try {
			File fXmlFile = new File(folder);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList allBlocks = doc.getElementsByTagName("block");

			for (int i = 0; i < allBlocks.getLength(); i++) {
				parseBlock(transformer, invGraph, allBlocks.item(i));
			}

		} catch (Exception e) {
		}
	}

	public static void parseBlock(Transformer transformer, InvariantGraph invGraph, Node blockItem) {
		try {
			Element block = (Element) blockItem;

			String blockId = "-1";
			String blockName = "";
			boolean nameUsed = true;
			if (block.getElementsByTagName("block_id").item(0) != null) {
				blockId = block.getElementsByTagName("block_id").item(0).getTextContent();
				nameUsed = false;
			} else {
				blockName = String.valueOf(block.getElementsByTagName("block_name").item(0).getTextContent());
				nameUsed = true;
			}

			// get max and min int
			if (hasChildNode(block, "max_int")) {
				int maxInt = Integer.parseInt(block.getElementsByTagName("max_int").item(0).getTextContent());
				invGraph.addMaxInt(maxInt);
			}
			if (hasChildNode(block, "min_int")) {
				int minInt = Integer.parseInt(block.getElementsByTagName("min_int").item(0).getTextContent());
				invGraph.addMinInt(minInt);
			}

			NodeList ports = block.getElementsByTagName("port");
			for (int i = 0; i < ports.getLength(); i++) {
				Element port = (Element) ports.item(i);

				int edgeNo = Integer.parseInt(port.getElementsByTagName("port_no").item(0).getTextContent());

				NodeList allInformation = port.getElementsByTagName("information");

				// Find correct edge
				InvariantNode relBlock = null;
				if (nameUsed) {
					blockName = blockName.replace(" ", "");
					blockName = blockName.replace("\n", "");
					blockName = blockName.replace("\\n", "");
					blockName = blockName.replace("-", "");
					blockName = blockName.replace("_", "");

					for (InvariantNode node : invGraph.getAllNodes()) {
						if (node.getBlock().getName().equals(blockName)) {
							relBlock = node;
						}
					}
				} else {
					relBlock = invGraph.getNode(blockId);
				}
				LinkedList<InvariantEdge> relEdge = new LinkedList<>();
				for (InvariantEdge e : invGraph.getAllEdges()) {
					if (e.getSrcID() == edgeNo && e.getSrcNode() == relBlock) {
						relEdge.add(e);
					}
				}

				if (relEdge == null)
					return;

				for (int j = 0; j < allInformation.getLength(); j++) {
					Element information = (Element) allInformation.item(j);

					String type = information.getElementsByTagName("type").item(0).getTextContent();

					if (type.matches("signal")) {
						parseSignal(transformer, invGraph, information, relBlock, relEdge);
					} else if (type.matches("control")) {
						parseControl(transformer, invGraph, information, relBlock, relEdge);
					}
				}
			}
		} catch (Exception e) {
			PluginLogger.info("Error: Wrong input in inputs_info.xml : " + e.toString());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				PluginLogger.info("Trace " + e.getStackTrace()[i].getFileName() + " : "
						+ e.getStackTrace()[i].getMethodName() + "(" + e.getStackTrace()[i].getLineNumber() + ")");
			}
		}
	}

	public static void parseSignal(Transformer transformer, InvariantGraph invGraph, Element information,
			InvariantNode relBlock, List<InvariantEdge> relEdge) {
		if (hasChildNode(information, "disjunction")) {
			Element conInformation = (Element) information.getElementsByTagName("disjunction").item(0);
			DiscreteSignalInformation newDisjunction = parseDisjunction(transformer, invGraph, conInformation,
					relBlock);

			for (InvariantEdge e : relEdge) {
				e.addInformation(newDisjunction);
			}
		} else if (hasChildNode(information, "conjunction")) {
			Element disInformation = (Element) information.getElementsByTagName("conjunction").item(0);
			SignalboundaryInformation newConjunction = parseConjunction(transformer, invGraph, disInformation,
					relBlock);

			for (InvariantEdge e : relEdge) {
				e.addInformation(newConjunction);
			}
		}
	}

	public static DiscreteSignalInformation parseDisjunction(Transformer transformer, InvariantGraph invGraph,
			Element information, InvariantNode relBlock) {
		DiscreteSignalInformation newInfo = new DiscreteSignalInformation();

		if (hasChildNode(information, "conjunction")) {
			NodeList conInformations = information.getElementsByTagName("conjunction");
			for (int i = 0; i < conInformations.getLength(); i++) {
				Element con = (Element) conInformations.item(i);
				SignalboundaryInformation newSignal = parseConjunction(transformer, invGraph, con, relBlock);

				newInfo.addLiterals(newSignal);
			}
		}

		return newInfo;
	}

	public static SignalboundaryInformation parseConjunction(Transformer transformer, InvariantGraph invGraph,
			Element information, InvariantNode relBlock) {
		SignalboundaryInformation newInfo = new SignalboundaryInformation(invGraph);

		if (hasChildNode(information, "interval")) {
			NodeList allIntervals = information.getElementsByTagName("interval");
			for (int i = 0; i < allIntervals.getLength(); i++) {
				Element interval = (Element) allIntervals.item(i);
				IntervalInformation newInterval = parseInterval(transformer, invGraph, interval, relBlock);

				newInfo.getDisjunction().addElement(newInterval);
			}
		} else if (hasChildNode(information, "equality")) {
			Element equality = (Element) information.getElementsByTagName("equality").item(0);
			EqualityInformation newEq = parseEquality(transformer, invGraph, equality, relBlock);

			newInfo.getDisjunction().addElement(newEq);
		}

		return newInfo;
	}

	public static IntervalInformation parseInterval(Transformer transformer, InvariantGraph invGraph,
			Element information, InvariantNode relBlock) {
		Environment env = transformer.getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(relBlock.getBlock());

		Relation lowerBound = new Relation(toReplace, RelationType.GREATER_THAN, new StringTerm("-inf"));
		if (hasChildNode(information, "lower_equal")) {
			Element lowerInfo = (Element) information.getElementsByTagName("lower_equal").item(0);
			lowerBound.setType(RelationType.GREATER_EQUAL);

			Term rightSide = null;
			try {
				rightSide = new RealTerm(lowerInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(lowerInfo.getTextContent());
			}
			lowerBound.setRightSide(rightSide);
		} else if (hasChildNode(information, "lower")) {
			Element lowerInfo = (Element) information.getElementsByTagName("lower").item(0);

			Term rightSide = null;
			try {
				rightSide = new RealTerm(lowerInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(lowerInfo.getTextContent());
			}
			lowerBound.setRightSide(rightSide);
		}

		Relation upperBound = new Relation(toReplace, RelationType.LESS_THAN, new StringTerm("inf"));
		if (hasChildNode(information, "upper_equal")) {
			Element upperInfo = (Element) information.getElementsByTagName("upper_equal").item(0);
			upperBound.setType(RelationType.LESS_EQUAL);

			Term rightSide = null;
			try {
				rightSide = new RealTerm(upperInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(upperInfo.getTextContent());
			}
			upperBound.setRightSide(rightSide);
		} else if (hasChildNode(information, "upper")) {
			Element upperInfo = (Element) information.getElementsByTagName("upper").item(0);

			Term rightSide = null;
			try {
				rightSide = new RealTerm(upperInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(upperInfo.getTextContent());
			}
			upperBound.setRightSide(rightSide);
		}

		IntervalInformation newInfo = new IntervalInformation(invGraph, lowerBound, upperBound);
		return newInfo;
	}

	public static EqualityInformation parseEquality(Transformer transformer, InvariantGraph invGraph,
			Element information, InvariantNode relBlock) {
		Environment env = transformer.getEnvironment();
		ReplaceableTerm toReplace = env.getToReplace(relBlock.getBlock());

		Relation relation = null;
		if (hasChildNode(information, "eq")) {
			Element eqInfo = (Element) information.getElementsByTagName("eq").item(0);
			Term rightSide = null;
			try {
				rightSide = new RealTerm(eqInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(eqInfo.getTextContent());
			}
			relation = new Relation(toReplace, RelationType.EQUAL, rightSide);
		} else if (hasChildNode(information, "neq")) {
			Element neqInfo = (Element) information.getElementsByTagName("neq").item(0);
			Term rightSide = null;
			try {
				rightSide = new RealTerm(neqInfo.getTextContent());
			} catch (NumberFormatException e) {
				rightSide = invGraph.getReplaceableTermOf(neqInfo.getTextContent());
			}
			relation = new Relation(toReplace, RelationType.NOT_EQUAL, rightSide);
		}

		if (relation != null) {
			EqualityInformation newInfo = new EqualityInformation(invGraph, relation);
			return newInfo;
		}
		return null;
	}

	public static void parseControl(Transformer transformer, InvariantGraph invGraph, Element information,
			InvariantNode relBlock, List<InvariantEdge> relEdge) {
		if (hasChildNode(information, "antecedent") && hasChildNode(information, "consequent")) {
			Element antecedentInfo = (Element) information.getElementsByTagName("antecedent").item(0);
			Element consequentInfo = (Element) information.getElementsByTagName("consequent").item(0);

			Operator antecedent = parseAntecedent(transformer, invGraph, antecedentInfo, relBlock);
			InvariantInformation consequent = parseConsequent(transformer, invGraph, consequentInfo, relBlock);

			ControlInformation newInfo = new ControlInformation(invGraph);
			newInfo.addImplication(antecedent, consequent);
		}
	}

	public static Operator parseAntecedent(Transformer transformer, InvariantGraph invGraph, Element information,
			InvariantNode relBlock) {
		Operator newInfo = null;

		if (hasChildNode(information, "disjunction")) {
			Element con = (Element) information.getElementsByTagName("disjunction").item(0);
			DiscreteSignalInformation newSignal = parseDisjunction(transformer, invGraph, con, relBlock);

			newInfo = newSignal;
		} else if (hasChildNode(information, "conjunction")) {
			Element con = (Element) information.getElementsByTagName("conjunction").item(0);
			SignalboundaryInformation newSignal = parseConjunction(transformer, invGraph, con, relBlock);

			newInfo = newSignal;
		} else if (hasChildNode(information, "string")) {
			Element string = (Element) information.getElementsByTagName("string").item(0);
			Term parsed = StringToTerm.parseString(string.getTextContent());

			newInfo = (Operator) parsed;
		}

		return newInfo;
	}

	public static InvariantInformation parseConsequent(Transformer transformer, InvariantGraph invGraph,
			Element information, InvariantNode relBlock) {
		InvariantInformation newInfo = null;

		if (hasChildNode(information, "disjunction")) {
			Element dis = (Element) information.getElementsByTagName("disjunction").item(0);
			DiscreteSignalInformation newSignal = parseDisjunction(transformer, invGraph, dis, relBlock);

			newInfo = newSignal;
		} else if (hasChildNode(information, "conjunction")) {
			Element con = (Element) information.getElementsByTagName("conjunction").item(0);
			SignalboundaryInformation newSignal = parseConjunction(transformer, invGraph, con, relBlock);

			newInfo = newSignal;
		}

		return newInfo;
	}

	public static boolean hasChildNode(Element elem, String name) {
		NodeList elems = elem.getElementsByTagName(name);
		return elems.getLength() == 0 ? false : true;
	}
}
