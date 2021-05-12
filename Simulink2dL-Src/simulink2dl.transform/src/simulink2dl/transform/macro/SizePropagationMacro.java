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

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.transform.dlmodel.term.ExpandedTerm;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;
/**
 * Propagates the size of a linkedPort to a Term.
 * Size is applied, when a VectorTerm is applied to the SizePropagationMacro
 *
 */
public class SizePropagationMacro extends Macro{

	private ReplaceableTerm linkedPort;
	private ReplaceableTerm linkedTerm;
	
	public SizePropagationMacro(ReplaceableTerm term, ReplaceableTerm replaceableTerm) {
		linkedPort = term;
		linkedTerm = replaceableTerm;
	}
	
	@Override
	public ReplaceableTerm getToReplace() {
		PluginLogger.error("getToReplace() should not be called for SizePropagationMacro");
		return new PortIdentifier("none");
	}

	@Override
	public Term getReplaceWith() {
		return linkedTerm;
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		
	}

	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection behavior) {
		
	}

	@Override
	public boolean containsTerm(Term toReplace) {
		return linkedPort.equals(toReplace);
	}

	@Override
	public Macro createDeepCopy() {
		return new SizePropagationMacro(linkedPort.createDeepCopy(), linkedTerm.createDeepCopy());
	}
	
	@Override 
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();
		return resultList;
		
	}
	
	@Override
	protected List<Macro> applyVectorMacro(VectorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();
		VectorTerm expandedTerm = new ExpandedTerm();
		int newSize = other.getReplaceWithVector().size();

		if (linkedTerm instanceof Variable) {
			Variable linkedVariable = (Variable) linkedTerm;
			linkedVariable.setSize(newSize);
			//getVector can not return ExpandedTerm directly
			//as including ExpandedTerm in simulink2dl.model leads to broken project
			expandedTerm = new ExpandedTerm(linkedVariable.getVector());
		} else {
			for (int i = 0; i<newSize; i++) {
				expandedTerm.add(linkedTerm.createDeepCopy());
			}
		}
		resultList.add(new VectorMacro(linkedTerm, expandedTerm));
		return resultList;
	}
	
	public String toString() {
		return linkedTerm + " resizeWith " + linkedPort;
	}

	@Override
	public void applyToInitialConditions(Conjunction initialConditions) {
		
	}
	
	@Override
	protected List<Macro> applyConditionalMacro(ConditionalMacro other) {
	List<Macro> resultList = new LinkedList<Macro>();

	for (MacroContainer outerContainer : other.getMacroContainers()) {
		Macro toApply = outerContainer.getMacro();
		if (!(toApply instanceof VectorMacro)) {
			continue;
		}
		resultList = this.applyVectorMacro((VectorMacro) toApply);
		break;
		//TODO check for same sizes in multiple VectorMacros
	}
	return resultList;
	}
}
