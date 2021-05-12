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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkLine;

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

public class InvariantNode {

	private InvariantGraph invGraph;
	private SimulinkBlock block;
	private List<InvariantInformation> information;

	public InvariantNode(InvariantGraph invGraph, SimulinkBlock block) {
		this.block = block;
		this.invGraph = invGraph;

		initializeVariables();
	}

	private void initializeVariables() {
		information = new LinkedList<>();
	}

	public void addInformation(InvariantInformation t) {
		// only add if information is not in
		InvariantInformation cloned = (InvariantInformation) t.clone();

		boolean isIn = false;
		for (InvariantInformation info : information) {
			if (info.equals(cloned))
				isIn = true;
		}
		if (!isIn)
			information.add(cloned);
	}

	public List<Macro> finalizeInformation(Transformer trForm) {
		List<Macro> macros = new LinkedList<Macro>();
		Environment env = trForm.getEnvironment();

		Variable v = AnalyzerUtil.getOutVariableOfBlock(block, invGraph);

		if (v != null) {
			for (InvariantInformation info : this.information) {
				switch (block.getType().toLowerCase()) {
				case "outport":
					info.finalizeInformation(v);

					Macro informationMacro = new SimpleMacro(new PortIdentifier(getMacroIdentifier()), (Term) info);
					macros.add(informationMacro);
					break;
				}
			}
		}

		return macros;
	}

	public String getMacroIdentifier() {
		return "#" + block.getName() + "_inv";
	}

	public void deleteAllInformation() {
		information.clear();
	}

	public SimulinkBlock getBlock() {
		return this.block;
	}

	@Override
	public String toString() {
		return block.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InvariantNode) {
			return this.block.getId().equals(((InvariantNode) obj).getBlock().getId());
		}
		return false;
	}

	public boolean contains(SimulinkBlock block) {
		return this.block.getId().equals(block.getId());
	}

	public boolean contains(String id) {
		return this.block.getId().equals(id);
	}

	public boolean contains(int id) {
		return this.block.getId().equals(id);
	}

	public Collection<SimulinkLine> getSignalLines() {
		return block.getOutLines();
	}
}
