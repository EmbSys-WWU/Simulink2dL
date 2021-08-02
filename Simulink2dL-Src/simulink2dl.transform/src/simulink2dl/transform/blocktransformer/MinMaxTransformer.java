package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.term.MinMaxTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;

public class MinMaxTransformer  extends BlockTransformer {

	public MinMaxTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
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
		String type = "MinMax";
		checkBlock(type, block);
		
		String function = block.getParameter("Function");

		// get input string
		SimulinkOutPort in1 = environment.getConnectedOuputPort(block, 1);
		String in1Id = environment.getPortID(in1);
		SimulinkOutPort in2 = environment.getConnectedOuputPort(block, 2);
		String in2Id = environment.getPortID(in2);
		
		List<Macro> macros = new ArrayList<>();
		macros.add(new SimpleMacro(environment.getToReplace(block), new MinMaxTerm(new PortIdentifier(in1Id), new PortIdentifier(in2Id), function)));
		
		return macros;
	}

}