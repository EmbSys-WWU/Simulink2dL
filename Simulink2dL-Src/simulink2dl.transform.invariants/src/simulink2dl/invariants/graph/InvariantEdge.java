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

import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.invariants.information.InvariantInformation;
import simulink2dl.invariants.util.AnalyzerUtil;
import simulink2dl.transform.Environment;
import simulink2dl.transform.Transformer;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;

public class InvariantEdge {

	private InvariantGraph invGraph;

	private SimulinkLine signal;
	private InvariantNode srcNode;
	private InvariantNode dstNode;
	private SimulinkInPort dstPort;
	private SimulinkOutPort srcPort;

	private List<InvariantInformation> information;

	public InvariantEdge(InvariantGraph invGraph, SimulinkLine signal, InvariantNode srcNode, InvariantNode dstNode) {
		this.invGraph = invGraph;
		this.signal = signal;
		this.srcNode = srcNode;
		this.dstNode = dstNode;

		this.dstPort = signal.getDstPort();
		this.srcPort = signal.getSrcPort();

		initializeVariables();
	}

	private void initializeVariables() {
		information = new LinkedList<InvariantInformation>();
	}

	public void addInformation(InvariantInformation t) {
		// only add if information is not in
		InvariantInformation cloned = (InvariantInformation) t.clone();
		cloned.changeReplaceableTerm(invGraph.getTransformer().getEnvironment().getToReplace(signal.getSrcPort()));

		boolean isIn = false;
		for (InvariantInformation info : information) {
			if (info.equals(cloned))
				isIn = true;
		}
		if (!isIn)
			information.add(cloned);
	}

	public void addInformation(List<InvariantInformation> infos) {
		for (InvariantInformation info : infos) {
			addInformation(info);
		}
	}

	public List<Macro> finalizeInformation(Transformer trForm) {
		List<Macro> macros = new LinkedList<Macro>();
		Environment env = trForm.getEnvironment();

		Variable v = AnalyzerUtil.getOutVariableOfBlock(signal.getSrcPort().getBlock(), invGraph);

		if (v != null) {
			for (InvariantInformation info : this.information) {
				((InvariantInformation) info).finalizeInformation(v);

				Macro informationMacro = new SimpleMacro(new PortIdentifier(getMacroIdentifier()), (Term) info);
				macros.add(informationMacro);
			}
		}

		return macros;
	}

	public String getMacroIdentifier() {
		return "#" + signal.getSrcPort().getBlock().getName() + "_inv";
	}

	public List<InvariantInformation> getAllInformation() {
		return information;
	}

	public void deleteAllInformation() {
		information.clear();
	}

	@Override
	public String toString() {
		return srcNode.toString() + " -> " + dstNode.toString() + "_" + dstPort;
	}

	public SimulinkLine getSignal() {
		return signal;
	}

	public InvariantNode getSrcNode() {
		return srcNode;
	}

	public InvariantNode getDstNode() {
		return dstNode;
	}

	public int getDstID() {
		return Integer.parseInt(dstPort.getIndex());
	}

	public int getSrcID() {
		return Integer.parseInt(srcPort.getIndex());
	}

	public boolean contains(int srcID, int dstID) {
		return srcNode.equals(srcID) && dstNode.equals(dstID);
	}

	public boolean contains(SimulinkLine otherSignal) {
		if (this.signal.equals(otherSignal)) {
			return true;
		}
		if (this.dstPort.equals(otherSignal.getDstPort()) && this.srcPort.equals(otherSignal.getSrcPort())) {
			return true;
		}

		return false;
	}

}
