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

import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringParser;

public class ProductTransformer extends BlockTransformer {

	public ProductTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		for (Macro macro : macros) {
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		String type = "Product";
		checkBlock(type, block);

		// get input string
		String inputs = block.getParameter("Inputs");

		if (!StringParser.containsNumber(inputs)) {
			// no digits, this means we have a sign list
			if (inputs.length() > 1) {
				return handleSignList(inputs, block);
			} else {
				return handleProductOfVectorInput(inputs.equals("*"), block);
			}
		} else {
			int numberOfInputs = Integer.parseInt(inputs);
			if (numberOfInputs != 1) {
				return handleInputNumber(numberOfInputs, block);
			} else {
				return handleProductOfVectorInput(true, block);
			}
		}
	}

	private List<Macro> handleSignList(String signs, SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		// generate product
		MultiplicationTerm product = new MultiplicationTerm();
		for (int signNumber = 0; signNumber < signs.length(); signNumber++) {
			char sign = signs.charAt(signNumber);
			// get connected port
			SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, signNumber + 1);
			String connectedPortID = environment.getPortID(connectedPort);
			ReplaceableTerm term = new PortIdentifier(connectedPortID);

			if (sign == '*') {
				product.multiplyBy(term);
			} else {
				product.dividedBy(term);
			}
		}

		// add macro
		Term replaceWith = product;
		macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));
		return macros;
	}

	private List<Macro> handleInputNumber(int numberOfInputs, SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		// generate product
		MultiplicationTerm product = new MultiplicationTerm();
		for (int i = 0; i < numberOfInputs; i++) {
			// get connected port
			SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, i + 1);
			String connectedPortID = environment.getPortID(connectedPort);
			ReplaceableTerm term = new PortIdentifier(connectedPortID);

			product.multiplyBy(term);
		}

		// add macro
		Term replaceWith = product;
		macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));
		return macros;
	}

	private List<Macro> handleProductOfVectorInput(boolean positiveSum, SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		// get connected port
		// Port connectedPort = getConnectedOuputPort(block, 1);
		// String connectedPortID = environment.getPortID(connectedPort);
		// ReplaceableTerm term = new ReplaceableTerm(connectedPortID);

		// add product macro
		// TODO: implement product of a vector signal macro
		PluginLogger.error("Product of a vector signal not implemented");
		// dlModel.addMacro(new VectorProductMacro(getToReplace(block), term,
		// positiveSum));
		return macros;
	}

}
