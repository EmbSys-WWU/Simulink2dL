package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;

public class UniformRandomNumberTransformer extends BlockTransformer {

	public UniformRandomNumberTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "UniformRandomNumber";
		checkBlock(type, block);

		String name = block.getName();
		name = name.replace("Uniform", "");
		name = name.replace("Random", "");
		name = name.replace("Number", "");
		name = name.replace("\n", "");
		name = name.replace(" ", "");
		name = "rand" + name;
		// add variable
		Variable variable = new Variable("R", name);
		dlModel.addVariable(variable);

		// add macro
		Term replaceWith = variable;
		dlModel.addMacro(new SimpleMacro(environment.getToReplace(block), replaceWith));

		// add upper and lower limits
		Constant upperLimit = new Constant("R", name + "Max");
		Constant lowerLimit = new Constant("R", name + "Min");
		dlModel.addConstant(upperLimit);
		dlModel.addConstant(lowerLimit);

		Conjunction limitFormula = new Conjunction();

		limitFormula.addElement(new Relation(variable, Relation.RelationType.LESS_EQUAL, upperLimit));
		limitFormula.addElement(new Relation(variable, Relation.RelationType.GREATER_EQUAL, lowerLimit));
		dlModel.addInitialCondition(limitFormula);
		dlModel.addBehaviorFront(new TestFormula(limitFormula));

		// add nondeterministic assignment
		dlModel.addBehaviorFront(new NondeterministicAssignment(variable));
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}
	
}
