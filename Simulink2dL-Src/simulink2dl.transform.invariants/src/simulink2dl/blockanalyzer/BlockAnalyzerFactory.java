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
package simulink2dl.blockanalyzer;

import org.conqat.lib.simulink.model.SimulinkBlock;

import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.graph.InvariantNode;
import simulink2dl.util.PluginLogger;

public class BlockAnalyzerFactory {

	private BlockAnalyzerFactory() {
	}

	public static BlockAnalyzer build(SimulinkBlock block, InvariantGraph invGraph) {
		InvariantNode node = invGraph.getNode(block);
		BlockAnalyzer analyzer = new EmptyAnalyzer(node, invGraph);

		switch (block.getType().toLowerCase()) {
		case "outport":
			return new OutportAnalyzer(node, invGraph);
		case "inport":
			return new InportAnalyzer(node, invGraph);
		case "constant":
			return new ConstantAnalyzer(node, invGraph);
		case "gain":
			return new GainAnalyzer(node, invGraph);
		case "sum":
			return new SumAnalyzer(node, invGraph);
		case "product":
			return new ProductAnalyzer(node, invGraph);
		case "integrator":
			return new IntegratorAnalyzer(node, invGraph);
		case "unitdelay":
			return new UnitdelayAnalyzer(node, invGraph);
		case "switch":
			return new SwitchAnalyzer(node, invGraph);
		case "relay":
			return new RelayAnalyzer(node, invGraph);
		case "logic":
			return new LogicalAnalyzer(node, invGraph);
		case "relationaloperator":
			return new RelationalAnalyzer(node, invGraph);
		case "zeroorderhold":
			return new ZeroOrderAnalyzer(node, invGraph);
		// TODO delay
		default:
			PluginLogger.error("No transformation rules implemented for block of type \"" + block.getType() + "\"");
		}
		return analyzer;
	}

}
