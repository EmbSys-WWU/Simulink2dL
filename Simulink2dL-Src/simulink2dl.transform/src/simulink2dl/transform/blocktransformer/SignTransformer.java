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

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.MacroContainer;
import simulink2dl.transform.macro.SimpleMacro;

public class SignTransformer extends BlockTransformer {

	public SignTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		// add alternative to model
		for (Macro macro : macros) {
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		String type = "Signum";
		checkBlock(type, block);

		// add variable
		Variable variable = new Variable("R", block.getName());
		dlModel.addVariable(variable);

		// get connected ports
		RealTerm pos = new RealTerm(1);
		RealTerm zero = new RealTerm(0);
		RealTerm neg = new RealTerm(-1);
		
		SimulinkOutPort conditionPort = environment.getConnectedOuputPort(block, 1);
		String conditionPortID = environment.getPortID(conditionPort);
		ReplaceableTerm conditionTerm = new PortIdentifier(conditionPortID);
		
		// get condition
		Relation testPos = new Relation (conditionTerm, RelationType.GREATER_THAN, zero);
		Relation testZero = new Relation (conditionTerm, RelationType.EQUAL, zero);
		Relation testNeg = new Relation (conditionTerm, RelationType.LESS_THAN, zero);

		// case pos
		SimpleMacro macroPos = new SimpleMacro(environment.getToReplace(block), pos);
		MacroContainer macroContainerPos = new MacroContainer(macroPos, testPos, null);
		// case zero
		SimpleMacro macroZero = new SimpleMacro(environment.getToReplace(block), zero);
		MacroContainer macroContainerZero = new MacroContainer(macroZero, testZero, null);
		// case neg
		SimpleMacro macroNeg = new SimpleMacro(environment.getToReplace(block), neg);
		MacroContainer macroContainerNeg = new MacroContainer(macroNeg, testNeg, null);

		macros.add(new ConditionalMacro(environment.getToReplace(block), macroContainerPos, macroContainerZero, macroContainerNeg));
		return macros;
	}
}
