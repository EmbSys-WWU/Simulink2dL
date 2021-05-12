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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;

import simulink2dl.blockanalyzer.BlockAnalyzer;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.invariants.graph.InvariantGraph;
import simulink2dl.invariants.information.ControlInformation;
import simulink2dl.invariants.information.InvariantInformation;

public class AnalyzerUtil {

	public static String[] controlBlockStrings = new String[] { "logic", "relationaloperator", "relay", "switch" };
	public static String[] discreteBlockStrings = new String[] { "unitdelay", "zeroorderhold", "delay" };
	public static String[] feedtroughBlockStrings = new String[] { "sum", "product", "gain", "add" };
	public static HashSet<String> controlBlocks = new HashSet<>(Arrays.asList(controlBlockStrings));
	public static HashSet<String> discreteBlocks = new HashSet<>(Arrays.asList(discreteBlockStrings));
	public static HashSet<String> feedtroughBlocks = new HashSet<>(Arrays.asList(feedtroughBlockStrings));

	public static String[] stateBlockString = new String[] { "integrator", "discreteintegrator", "unitdelay",
			"zeroorderhold", "delay" };
	public static HashSet<String> stateBlocks = new HashSet<String>(Arrays.asList(stateBlockString));
	public static String[] nonStateBlockStrings = new String[] { "constant", "gain", "logical", "product", "relational",
			"sum", "switch" };
	public static HashSet<String> nonStateBlocks = new HashSet<String>(Arrays.asList(nonStateBlockStrings));

	private AnalyzerUtil() {
	}

	public static Variable getOutVariableOfBlock(SimulinkBlock block, InvariantGraph invGraph) {
		String name = block.getName();
		if (discreteBlocks.contains(block.getType().toLowerCase())) {
			name += "Out";
		}
		Variable v = invGraph.getTransformer().getDLModel().getVariableByName(name);
		if (v == null)
			v = new Variable("R", name);

		return v;
	}

	public static int applyControlInformation(BlockAnalyzer analyzer, ControlInformation information,
			InvariantGraph invGraph) {
		List<InvariantInformation> allInfos = analyzer.generateInformation();
		ControlInformation newInfo = (ControlInformation) information.clone();
		for (Implication imp : newInfo.getImplications()) {
			imp.setAntecedent(new Disjunction());
		}

		for (int i = 0; i < allInfos.size(); i++) {
			InvariantInformation info = allInfos.get(i);
			if (info instanceof ControlInformation) {
				ControlInformation control = (ControlInformation) info;
				for (Implication op : control.getImplications()) {
					Operator condA = op.getAntecedent();
					int k = 0;
					for (Implication op2 : information.getImplications()) {
						Operator condB = op2.getAntecedent();

						Implication newInfoImplication = newInfo.getImplications().get(k);

						Conjunction con = new Conjunction(condA.createDeepCopy(), condB.createDeepCopy());
						Disjunction dis;
						if (((Disjunction) newInfoImplication.getAntecedent()).getElements().size() == 0) {
							dis = new Disjunction(con);
						} else {
							dis = new Disjunction(con, newInfoImplication.getAntecedent());
						}

						newInfoImplication.setAntecedent(dis);
						newInfoImplication.setConsequent(op2.getConsequent().createDeepCopy());

						k++;
					}
				}
			}
		}
		information.setElements(newInfo.getElements());
		return BlockAnalyzer.VALID_INFORMATION;
		// TODO uncomment and set antecedent correct
		// return BlockAnalyzer.END_OF_INFORMATION;
	}

	public static String replacePIWithNumber(String pi) {
		String replacement = pi;
		if (replacement.contains("pi")) {
			replacement.replace("pi", "3,14159265");
		}
		return replacement;
	}

}
