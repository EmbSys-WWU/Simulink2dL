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
package simulink2dl.transform.macro;

import java.util.List;

import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public abstract class Macro {

	public abstract ReplaceableTerm getToReplace();

	public abstract Term getReplaceWith();

	public List<Macro> applyOtherMacro(Macro other) {
		if (other instanceof SimpleMacro) {
			return this.applySimpleMacro((SimpleMacro) other);
		} else if (other instanceof ConditionalMacro) {
			return this.applyConditionalMacro((ConditionalMacro) other);
		} else if (other instanceof VectorMacro) {
			return this.applyVectorMacro((VectorMacro) other);
		} else if (other instanceof BusCreatorMacro) {
			return this.applyBusCreatorMacro((BusCreatorMacro) other);
		} else {
			PluginLogger
					.error("Handling of macro of type \"" + other.getClass().getSimpleName() + "\" not implemented.");
			return null;
		}
	}
	protected List<Macro> applySizePropagationMacro(SizePropagationMacro other){
		PluginLogger.error("Application of " + other.getClass().getSimpleName() + " to "
				+ this.getClass().getSimpleName() + " not implemented.");
		return null;
	}

	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		PluginLogger.error("Application of " + other.getClass().getSimpleName() + " to "
				+ this.getClass().getSimpleName() + " not implemented.");
		return null;
	}

	protected List<Macro> applyConditionalMacro(ConditionalMacro other) {
		PluginLogger.error("Application of " + other.getClass().getSimpleName() + " to "
				+ this.getClass().getSimpleName() + " not implemented.");
		return null;
	}

	protected List<Macro> applyVectorMacro(VectorMacro other) {
		PluginLogger.error("Application of " + other.getClass().getSimpleName() + " to "
				+ this.getClass().getSimpleName() + " not implemented.");
		return null;
	}

	protected List<Macro> applyBusCreatorMacro(BusCreatorMacro other) {
		PluginLogger.error("Application of " + other.getClass().getSimpleName() + " to "
				+ this.getClass().getSimpleName() + " not implemented.");
		return null;
	}

	public abstract void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior);

	public abstract void applyToHybridProgramCollection(HybridProgramCollection behavior);

	public abstract boolean containsTerm(Term toReplace);

	public abstract Macro createDeepCopy();
	
	// For ReplaceableTerms in initialConditions (e.g. for outPorts)
	public abstract void applyToInitialConditions(Conjunction initialConditions);


}
