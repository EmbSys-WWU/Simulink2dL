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
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.SingleEvolution;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelSimulink;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.macro.SizePropagationMacro;
import simulink2dl.transform.model.container.ContinuousEvolutionContainer;
import simulink2dl.util.PluginLogger;
import simulink2dl.util.parser.StringParser;

public class IntegratorTransformer extends BlockTransformer {

	public IntegratorTransformer(SimulinkModel simulinkModel, DLModelSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "Integrator";
		checkBlock(type, block);

		// add variable
		Variable variable = new Variable("R", block.getName());
		dlModel.addVariable(variable);

		// add initial condition
		String initCondition = block.getParameter("InitialCondition");
		RealTerm initialValue = new RealTerm(initCondition);
		dlModel.addInitialCondition(new Relation(variable, RelationType.EQUAL, initialValue));

		// get connected port
		SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, 1);
		String connectedPortID = environment.getPortID(connectedPort);

		// get saturation limits
		String upperLimit = block.getParameter("UpperSaturationLimit");
		String lowerLimit = block.getParameter("LowerSaturationLimit");

		// add macro
		Term replaceWith = variable;
		dlModel.addMacro(new SizePropagationMacro(environment.getToReplace(block),variable));
		dlModel.addMacro(new SimpleMacro(environment.getToReplace(block), replaceWith));

		// check for external reset
		String externalReset = block.getParameter("ExternalReset");

		switch (externalReset) {
		case "none":
			createEvolutionsWithoutReset(upperLimit, lowerLimit, variable, connectedPortID);
			break;
		case "rising":
			PluginLogger.error("External Reset of type \"" + externalReset + "\" not implemented for Integrator.");
			break;
		case "falling":
			PluginLogger.error("External Reset of type \"" + externalReset + "\" not implemented for Integrator.");
			break;
		case "either":
			PluginLogger.error("External Reset of type \"" + externalReset + "\" not implemented for Integrator.");
			break;
		case "level":
			createEvolutionsLevelReset(block, upperLimit, lowerLimit, variable, initialValue, connectedPortID);
			break;
		case "level hold":
			PluginLogger.error("External Reset of type \"" + externalReset + "\" not implemented for Integrator.");
			break;
		default:
			PluginLogger.error("External Reset of type \"" + externalReset + "\" not implemented for Integrator.");
			break;
		}
	}

	private void createEvolutionsWithoutReset(String upperLimit, String lowerLimit, Variable variable,
			String connectedPortID) {
		// check for saturation limits
		boolean enableSaturation = false;
		if (upperLimit != null && !upperLimit.equals("inf")) {
			PluginLogger.info("upper Limit: " + upperLimit);
			enableSaturation = true;
		}
		if (lowerLimit != null && !lowerLimit.equals("-inf")) {
			PluginLogger.info("lower Limit: " + lowerLimit);
			enableSaturation = true;
		}

		// add continuous evolution
		if (!enableSaturation) {
			dlModel.addContinuousEvolution(variable, new PortIdentifier(connectedPortID));
		} else {
			List<ContinuousEvolutionContainer> evolutions = new LinkedList<ContinuousEvolutionContainer>();

			Conjunction betweenLimitsFormula = new Conjunction();

			Relation GainLeZero = new Relation(new PortIdentifier(connectedPortID), RelationType.LESS_EQUAL, new RealTerm(0.0));
			Relation GainGeZero = new Relation(new PortIdentifier(connectedPortID), RelationType.GREATER_EQUAL, new RealTerm(0.0));
			
			if (upperLimit != null && !upperLimit.equals("inf")) {
				// handle upper limit
				double upperLimitInt = StringParser.parseScalar(upperLimit);
				Formula greaterEqualUpperLimit = new Relation(variable, RelationType.GREATER_EQUAL,new RealTerm(upperLimitInt));
				Formula lessEqualUpperLimit = new Relation(variable, RelationType.LESS_EQUAL,new RealTerm(upperLimitInt));

				ContinuousEvolution upperEvolution = new ContinuousEvolution(new SingleEvolution(variable, new RealTerm(0.0)));
				// set upper evolution domain
				// evolution should not get above saturation limit and
				// gain should be greater or equal to zero
				Formula upperEvolutionDomain = new Conjunction(greaterEqualUpperLimit, GainGeZero);
				upperEvolution.setEvolutionDomain(upperEvolutionDomain);
				// add between limits formula (negation of upper limit but limit relations are not exclusive).
				betweenLimitsFormula.addElement(new Disjunction(lessEqualUpperLimit, GainGeZero.createNegation()));

				// add upper limit evolution
				ContinuousEvolutionContainer upperLimitContainer = new ContinuousEvolutionContainer(upperEvolution, upperEvolutionDomain);
				evolutions.add(upperLimitContainer);
			}
			if (lowerLimit != null && !lowerLimit.equals("-inf")) {
				// handle lower limit
				double lowerLimitInt = StringParser.parseScalar(lowerLimit);
				Formula lessEqualLowerLimit = new Relation(variable, RelationType.LESS_EQUAL,new RealTerm(lowerLimitInt));
				Formula greaterEqualLowerLimit = new Relation(variable, RelationType.GREATER_EQUAL,new RealTerm(lowerLimitInt));

				ContinuousEvolution lowerEvolution = new ContinuousEvolution(new SingleEvolution(variable, new RealTerm(0.0)));
				
				// set lower evolution domain
				// evolution should not get below saturation limit and
				// gain should be less or equal to zero
				Formula lowerEvolutionDomain = new Conjunction(lessEqualLowerLimit, GainLeZero);
				lowerEvolution.setEvolutionDomain(lowerEvolutionDomain);
				// add between limits formula (negation of lower limit but limit relations are not exclusive).
				betweenLimitsFormula.addElement(new Disjunction(greaterEqualLowerLimit, GainLeZero.createNegation()));

				// add lower limit evolution
				ContinuousEvolutionContainer lowerLimitContainer = new ContinuousEvolutionContainer(lowerEvolution, lowerEvolutionDomain);
				evolutions.add(lowerLimitContainer);
			}
				// handle evolution between saturation limits
				ContinuousEvolution betweenEvolution = new ContinuousEvolution(
						new SingleEvolution(variable, new PortIdentifier(connectedPortID)));

				// set between evolution domain
				betweenEvolution.setEvolutionDomain(betweenLimitsFormula.createDeepCopy());

				// add between limit evolution
				ContinuousEvolutionContainer betweenLimitContainer = new ContinuousEvolutionContainer(betweenEvolution,
						betweenLimitsFormula);
				evolutions.add(0, betweenLimitContainer);
				dlModel.addNewEvolutionAlternatives(evolutions);
		}

	}

	/**
	 * Creates the evolution for an integrator with an external reset. The "level"
	 * reset sets the integrator to its initial value, if the reset input is not
	 * equal to zero.
	 * 
	 * @param block
	 * @param upperLimit
	 * @param lowerLimit
	 * @param variable
	 * @param connectedPortID
	 */
	private void createEvolutionsLevelReset(SimulinkBlock block, String upperLimit, String lowerLimit,
			Variable variable, RealTerm initialValue, String connectedPortID) {

		// get reset signal
		SimulinkOutPort resetConnectedPort = environment.getConnectedOuputPort(block, 2);
		String resetConnectedPortID = environment.getPortID(resetConnectedPort);

		Formula resetCondition = new Relation(new PortIdentifier(resetConnectedPortID), RelationType.NOT_EQUAL,
				new RealTerm(0.0));
		Formula noResetCondition = resetCondition.createNegation();

		// if the reset signal is not equal to zero
		// set the integrator state variable to its input value
		// as long as the state variable is equal to zero, do not evolve the integrator
		// variable
		List<ContinuousEvolutionContainer> evolutions = new LinkedList<ContinuousEvolutionContainer>();

		// TODO: add discrete assignment to dL model
		// reset integrator variable to initial value
		DiscreteAssignment resetAssignment = new DiscreteAssignment(variable, initialValue);

		ConditionalHybridProgram resetChoice = new ConditionalHybridProgram(resetCondition, resetAssignment);
		ConditionalHybridProgram noResetChoice = new ConditionalHybridProgram(noResetCondition,
				new HybridProgramCollection());
		ConditionalChoice resetChoiceProgram = new ConditionalChoice(resetChoice, noResetChoice);

		dlModel.addBehavior(resetChoiceProgram);

		// evolution during reset
		ContinuousEvolution resetEvolution = new ContinuousEvolution(new SingleEvolution(variable, new RealTerm(0.0)));
		resetEvolution.setEvolutionDomain(resetCondition);

		// add reset evolution
		ContinuousEvolutionContainer resetContainer = new ContinuousEvolutionContainer(resetEvolution, resetCondition);
		evolutions.add(resetContainer);

		// evolution without reset
		ContinuousEvolution defaultEvolution = new ContinuousEvolution(
				new SingleEvolution(variable, new PortIdentifier(connectedPortID)));
		defaultEvolution.setEvolutionDomain(noResetCondition);

		// default evolution
		ContinuousEvolutionContainer defaultContainer = new ContinuousEvolutionContainer(defaultEvolution,
				noResetCondition);
		evolutions.add(defaultContainer);

		dlModel.addNewEvolutionAlternatives(evolutions);

		// TODO add saturation limits for reset handling
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

}
