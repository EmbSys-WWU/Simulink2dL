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

import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.macro.BusCreatorMacro.BusSignal;
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public class BusSelectorMacro extends Macro {

	private class BusOutputSignal {
		private String identifier;
		private ReplaceableTerm toReplace;

		public BusOutputSignal(String identifier, ReplaceableTerm toReplace) {
			this.identifier = identifier;
			this.toReplace = toReplace;
		}

		public ReplaceableTerm getToReplaceTerm() {
			return toReplace;
		}

		/**
		 * For debug purpose.
		 */
		@Override
		public String toString() {
			return toReplace.toString() + ": " + identifier;
		}

		public String getIdentifier() {
			return identifier;
		}
	}

	private ReplaceableTerm replaceWith;

	private List<BusOutputSignal> busSelectionList;

	public BusSelectorMacro(ReplaceableTerm replaceWith) {
		this.replaceWith = replaceWith;

		busSelectionList = new LinkedList<BusOutputSignal>();
	}

	public void addNewSelection(ReplaceableTerm toReplace, String identifier) {
		this.busSelectionList.add(new BusOutputSignal(identifier, toReplace));
	}

	@Override
	public ReplaceableTerm getToReplace() {
		PluginLogger.error("getToReplace() should not be called for bus selector macros");
		return busSelectionList.get(0).getToReplaceTerm();
	}

	@Override
	public ReplaceableTerm getReplaceWith() {
		return replaceWith;
	}

	protected List<Macro> applyBusCreatorMacro(BusCreatorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		// for each output, create a new macro that represents its connected
		// input signal
		for (BusOutputSignal busOutputSignal : busSelectionList) {
			// find the connected input signal
			Term inputTerm = other.getTermWithIdentifier(busOutputSignal.getIdentifier());

			if (inputTerm != null) {
				// create new macro
				SimpleMacro newMacro = new SimpleMacro(busOutputSignal.getToReplaceTerm(), inputTerm);

				resultList.add(newMacro);
				continue;
			}

			// the output could be a bus that consists of multiple signals
			List<BusSignal> inputList = other.getTermsWithPrefix(busOutputSignal.getIdentifier() + ".");

			if (!inputList.isEmpty()) {
				// create new BusCreatorMacro
				BusCreatorMacro newMacro = new BusCreatorMacro(busOutputSignal.getToReplaceTerm());

				for (BusSignal newBusSignal : inputList) {
					newMacro.addElement(newBusSignal);
				}

				resultList.add(newMacro);
				continue;
			}
		}

		return resultList;
	}

	@Override
	public void applyToContinuousBehavior(ContinuousEvolutionBehavior continuousBehavior) {
		// do nothing
	}

	@Override
	public void applyToHybridProgramCollection(HybridProgramCollection behavior) {
		// do nothing
	}
	
	@Override
	public void applyToInitialConditions(Conjunction initialConditions) {
		// do nothing	
	}

	@Override
	public boolean containsTerm(Term toCompare) {
		return replaceWith.equals(toCompare);
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {
		String result = "";
		for (BusOutputSignal busOutputSignal : busSelectionList) {
			result += "  " + busOutputSignal.toString() + ", ";
		}
		return result + ": {" + replaceWith.toString() + "}";
	}

	@Override
	public BusSelectorMacro createDeepCopy() {
		BusSelectorMacro result = new BusSelectorMacro(replaceWith.createDeepCopy());

		for (BusOutputSignal oldBusSelection : busSelectionList) {
			result.addNewSelection(oldBusSelection.getToReplaceTerm().createDeepCopy(),
					oldBusSelection.getIdentifier());
		}

		return result;
	}

}
