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

import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.BusCreatorMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.util.PluginLogger;

public class BusCreatorTransformer extends BlockTransformer {

	public BusCreatorTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "BusCreator";
		checkBlock(type, block);

		// add vector macro
		BusCreatorMacro busCreatorMacro = new BusCreatorMacro(environment.getToReplace(block));

		// get input signal names
		String inputSignalNames = block.getParameter("InputSignalNames");

		// these replacements are necessary to obtain the correct name string
		inputSignalNames = inputSignalNames.replace("[ ", "");
		inputSignalNames = inputSignalNames.replace(" ]", "");

		// handle empty name list
		inputSignalNames = inputSignalNames.replace("[]", " ");

		// the splitting creates an array where each element represents an input
		// signal
		// empty elements mean that no name is specified in the model and the
		// name id defaulted to "signalX", where "X" is the number of the input
		// port
		String inputSignals[] = inputSignalNames.split(" ");

		// no given input names mean that all names are defaulted to "signalX"
		if (inputSignals.length == 0) {
			// get number of input signals
			String numberOfInputsString = block.getParameter("Inputs");
			int numberOfInputs = Integer.parseInt(numberOfInputsString);

			// use names "signal1", "signal2", ...
			for (int n = 1; n <= numberOfInputs; n++) {
				// get connected port
				SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, n);
				String connectedPortID = environment.getPortID(connectedPort);

				busCreatorMacro.addElement("signal" + n, new PortIdentifier(connectedPortID));
			}
		} else {
			int i = 1;
			for (String signalName : inputSignals) {
				if (signalName.isEmpty()) {
					signalName = "signal" + i;
				}

				// get connected port
				SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, i);
				i++;
				String connectedPortID = environment.getPortID(connectedPort);

				busCreatorMacro.addElement(signalName, new PortIdentifier(connectedPortID));
			}
		}
		dlModel.addMacro(busCreatorMacro);
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

}
