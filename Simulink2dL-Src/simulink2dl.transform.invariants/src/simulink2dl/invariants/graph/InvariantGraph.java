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
package simulink2dl.invariants.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;

import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.StringTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.information.DataInformation;
import simulink2dl.transform.Transformer;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;

public class InvariantGraph {

	private List<InvariantNode> invariantNodes;
	private List<InvariantEdge> invariantEdges;

	private List<DataInformation> allDataInformation;
	private List<Term> securityProperties;

	private int MAX_INT = Integer.MAX_VALUE;
	private int MIN_INT = Integer.MIN_VALUE;

	private Transformer transformer;

	// Constants
	public static final int NO_EDGE_FOUND = -1;

	public InvariantGraph(Transformer transformer) {
		this.transformer = transformer;

		invariantNodes = new LinkedList<InvariantNode>();
		invariantEdges = new LinkedList<InvariantEdge>();
		allDataInformation = new LinkedList<DataInformation>();
		securityProperties = new LinkedList<Term>();
	}

	public List<Macro> finalizeInformation() {
		List<Macro> macros = new LinkedList<Macro>();

		for (InvariantEdge edge : invariantEdges) {
			macros.addAll(edge.finalizeInformation(transformer));
		}
		for (InvariantNode node : invariantNodes) {
			macros.addAll(node.finalizeInformation(transformer));
		}

		for (DataInformation info : allDataInformation) {
			Macro informationMacro = new SimpleMacro(new PortIdentifier(""), (Term) info);
			macros.add(informationMacro);
		}

		for (Macro macro : macros) {
			transformer.getDLModel().addMacro(macro);
		}

		for (Term security : securityProperties) {
			Macro macro = new SimpleMacro(new PortIdentifier(""), security);
			transformer.getDLModel().addMacro(macro);
		}

		return macros;
	}

	// Graph building
	public void insertNode(SimulinkBlock block) {
		if (!containsNode(block)) {
			HashMap<Integer, ReplaceableTerm> portIdentifier = new HashMap<>();
			for (SimulinkInPort p : block.getInPorts()) {
				portIdentifier.put(Integer.parseInt(p.getIndex()), transformer.getEnvironment().getToReplace(p));
			}

			invariantNodes.add(new InvariantNode(this, block));
		}
	}

	public void insertEdge(SimulinkLine signal, SimulinkBlock src, SimulinkBlock dst) {
		if (containsEdge(signal) == NO_EDGE_FOUND) {
			if (this.containsNode(src) && this.containsNode(dst)) {
				invariantEdges.add(new InvariantEdge(this, signal, this.getNode(src), this.getNode(dst)));
			}
		}
	}

	public void insertEdge(SimulinkLine signal, InvariantNode src, InvariantNode dst) {
		if (containsEdge(signal) == NO_EDGE_FOUND) {
			invariantEdges.add(new InvariantEdge(this, signal, src, dst));
		}
	}

	public int containsEdge(SimulinkLine signal) {
		for (int i = 0; i < invariantEdges.size(); i++) {
			if (invariantEdges.get(i).contains(signal))
				return i;
		}
		return NO_EDGE_FOUND;
	}

	public boolean containsNode(SimulinkBlock block) {
		for (InvariantNode node : invariantNodes) {
			if (node.contains(block)) {
				return true;
			}
		}
		return false;
	}

	public InvariantEdge getEdge(int srcID, int dstID) {
		for (InvariantEdge edge : invariantEdges) {
			if (edge.contains(srcID, dstID))
				return edge;
		}
		return null;
	}

	public InvariantEdge getEdge(SimulinkLine signal) {
		int index = containsEdge(signal);
		if (index != NO_EDGE_FOUND)
			return invariantEdges.get(index);
		return null;
	}

	public InvariantNode getNode(String id) {
		for (InvariantNode node : invariantNodes) {
			if (node.contains(id)) {
				return node;
			}
		}
		return null;
	}

	public InvariantNode getNode(SimulinkBlock block) {
		for (InvariantNode node : invariantNodes) {
			if (node.contains(block)) {
				return node;
			}
		}
		return null;
	}

	public List<InvariantNode> getAllNodes() {
		return invariantNodes;
	}

	public List<InvariantEdge> getAllEdges() {
		return invariantEdges;
	}

	// Informations
	public void addDataInformation(DataInformation information) {
		boolean isIn = false;
		for (DataInformation info : allDataInformation) {
			if (information.equals(info)) {
				isIn = true;
				break;
			}
		}
		if (!isIn) {
			allDataInformation.add((DataInformation) information.clone());
		}
	}

	public void addSecurityProperty(Term property) {
		boolean isIn = false;
		for (Term info : securityProperties) {
			if (property.equals(info)) {
				isIn = true;
				break;
			}
		}
		if (!isIn) {
			securityProperties.add(property.createDeepCopy());
		}
	}

	public List<DataInformation> getAllDataInformation() {
		return allDataInformation;
	}

	public List<Term> getAllSecurityProperties() {
		return securityProperties;
	}

	// Util
	public HashMap<Integer, List<InvariantNode>> getOutBlocksWithSameSignalLine(SimulinkBlock block) {
		HashMap<Integer, List<InvariantNode>> allBlocks = new HashMap<Integer, List<InvariantNode>>();

		for (SimulinkLine signal : block.getOutLines()) {
			InvariantEdge edge = getEdge(signal);

			List<InvariantNode> hashmapList = null;
			if ((hashmapList = allBlocks.get(edge.getDstID())) == null) {
				hashmapList = new LinkedList<InvariantNode>();
			}
			hashmapList.add(getNode(signal.getDstPort().getBlock()));

			allBlocks.remove(edge.getDstID());
			allBlocks.put(edge.getDstID(), hashmapList);
		}

		return allBlocks;
	}

	public Term getReplaceableTermOf(String blockName) {
		Term term = new StringTerm(blockName);

		InvariantNode relBlock = null;
		blockName = blockName.replace(" ", "");
		blockName = blockName.replace("\n", "");
		blockName = blockName.replace("\\n", "");
		blockName = blockName.replace("-", "");
		blockName = blockName.replace("_", "");

		for (InvariantNode node : getAllNodes()) {
			if (node.getBlock().getName().equals(blockName)) {
				relBlock = node;
			}
		}

		if (relBlock == null)
			term = transformer.getEnvironment().getToReplace(relBlock.getBlock());

		return term;
	}

	// Setters
	public void addMaxInt(int max) {
		this.MAX_INT = max;
	}

	public void addMinInt(int min) {
		this.MIN_INT = min;
	}

	// Getters
	public List<InvariantEdge> getAllIncomingEdges(SimulinkBlock block) {
		List<InvariantEdge> edges = new LinkedList<>();

		InvariantNode node = this.getNode(block);

		for (InvariantEdge e : invariantEdges) {
			if (e.getDstNode() == node)
				edges.add(e);
		}

		return edges;
	}

	public List<InvariantEdge> getAllIncomingEdges(InvariantNode block) {
		List<InvariantEdge> edges = new LinkedList<>();

		for (InvariantEdge e : invariantEdges) {
			if (e.getDstNode() == block)
				edges.add(e);
		}

		return edges;
	}

	public int getMaxInt() {
		return this.MAX_INT;
	}

	public int getMinInt() {
		return this.MIN_INT;
	}

	@Override
	public String toString() {
		String result = "\n";

		result += "Nodes: " + invariantNodes.toString() + "\n";
		result += "Edges: " + invariantEdges.toString();

		return result;
	}

	public Transformer getTransformer() {
		return this.transformer;
	}

}
