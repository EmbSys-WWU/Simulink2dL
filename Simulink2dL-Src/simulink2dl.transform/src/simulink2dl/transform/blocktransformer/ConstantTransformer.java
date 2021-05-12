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

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.macro.VectorMacro;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringParser;

public class ConstantTransformer extends BlockTransformer {

	public ConstantTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
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
		List<Macro> macros = new ArrayList<>();
		String type = "Constant";
		checkBlock(type, block);

		// get constant string
		String valueString = block.getParameter("Value");

		if (StringParser.isScalar(valueString)) {
			// add macro
			Term replaceWith;
			if(StringParser.isNumber(valueString)){
				replaceWith = new RealTerm(StringParser.parseScalar(valueString));
			} else {
				Constant newConstant = new Constant("R",valueString);
				dlModel.addConstant(newConstant);
				replaceWith = newConstant;//TODO check if constant name is valid
			}
			macros.add(new SimpleMacro(environment.getToReplace(block), replaceWith));
		} else if (StringParser.isVector(valueString)) {
			// add vector macro
			VectorMacro vectorMacro = new VectorMacro(environment.getToReplace(block));

			List<Double> vectorElements = StringParser.parseVector(valueString);
			for (Double element : vectorElements) {
				vectorMacro.addElement(new RealTerm(element));
			}

			macros.add(vectorMacro);
		} else {
			PluginLogger.error("Constant of the following form is not handled: " + valueString);
		}

		return macros;
	}

}
