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
import simulink2dl.transform.model.ContinuousEvolutionBehavior;
import simulink2dl.util.PluginLogger;

public class BusCreatorMacro extends Macro {

	protected class BusSignal {
		private String identifier;
		private Term term;

		public BusSignal(String identifier, Term term) {
			this.identifier = identifier;
			this.term = term;
		}

		public boolean isIdentifier(String toCompare) {
			return identifier.equals(toCompare);
		}

		public Term getTerm() {
			return term;
		}

		/**
		 * For debug purpose.
		 */
		@Override
		public String toString() {
			return identifier + ": " + term.toString();
		}

		public boolean contains(ReplaceableTerm toCompare) {
			if (term.equals(toCompare)) {
				return true;
			}
			if (term.containsTerm(toCompare)) {
				return true;
			}
			return false;
		}

		public void replace(ReplaceableTerm otherToReplace, Term otherReplaceWith) {
			if (term.equals(otherToReplace)) {
				term = otherReplaceWith;
			} else {
				term.replaceTermRecursive(otherToReplace, otherReplaceWith);
			}
		}

		public String getIdentifier() {
			return identifier;
		}

		public boolean hasPrefix(String prefix) {
			return identifier.startsWith(prefix);
		}
	}

	private ReplaceableTerm toReplace;

	private List<BusSignal> busSignalList;

	public BusCreatorMacro(ReplaceableTerm toReplace) {
		this.toReplace = toReplace;

		busSignalList = new LinkedList<BusSignal>();
	}

	public void addElement(String identifier, Term term) {
		this.addElement(new BusSignal(identifier, term));
	}

	public void addElement(BusSignal newBusSignal) {
		this.busSignalList.add(newBusSignal);
	}

	@Override
	public ReplaceableTerm getToReplace() {
		return toReplace;
	}

	@Override
	public ReplaceableTerm getReplaceWith() {
		PluginLogger.error("getReplaceWith() should not be called for BusCreatorMacro.");
		return null;
	}

	@Override
	protected List<Macro> applySimpleMacro(SimpleMacro other) {
		ReplaceableTerm otherToReplace = other.getToReplace();
		Term otherReplaceWith = other.getReplaceWith();
		for (BusSignal busSignal : busSignalList) {
			if (busSignal.contains(otherToReplace)) {
				busSignal.replace(otherToReplace, otherReplaceWith);
			}
		}
		List<Macro> resultList = new LinkedList<Macro>();
		resultList.add(this);
		return resultList;
	}

//	@Override
//	protected List<Macro> applyConditionalMacro(ConditionalMacro other) {
//		List<Macro> resultList = new LinkedList<Macro>();
//		ConditionalMacro result = new ConditionalMacro(toReplace);
//
//		List<MacroContainer> oldMacroContainerList = other.getMacroContainers();
//		for (MacroContainer oldMacroContainer : oldMacroContainerList) {
//			MacroContainer newMacroContainer = new MacroContainer(null, null);
//		}
//
//		resultList.add(this);
//		return resultList;
//	}

	@Override
	protected List<Macro> applyBusCreatorMacro(BusCreatorMacro other) {
		List<Macro> resultList = new LinkedList<Macro>();

		ReplaceableTerm otherToReplace = other.getToReplace();

		BusCreatorMacro result = new BusCreatorMacro(toReplace.createDeepCopy());

		for (BusSignal busSignal : busSignalList) {
			if (busSignal.contains(otherToReplace)) {
				// connect the signal names according to: "thisName"."otherName"
				for (BusSignal otherBusSignal : other.busSignalList) {
					Term newTerm;
					if (busSignal.getTerm().equals(other.getToReplace())) {
						newTerm = otherBusSignal.getTerm().createDeepCopy();
					} else {
						newTerm = busSignal.getTerm().createDeepCopy();
						newTerm.replaceTermRecursive(otherToReplace, otherBusSignal.getTerm());
					}

					String newIdentifier = busSignal.getIdentifier() + "." + otherBusSignal.getIdentifier();
					result.addElement(newIdentifier, newTerm);
				}
			} else {
				// just add the existing busSignal to the result
				result.addElement(busSignal.getIdentifier(), busSignal.getTerm().createDeepCopy());
			}
		}

		resultList.add(result);
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
		for (BusSignal busSignal : busSignalList) {
			Term term = busSignal.getTerm();
			if (term.equals(toCompare)) {
				return true;
			}
			if (term.containsTerm(toCompare)) {
				return true;
			}
		}
		return false;
	}

	public Term getTermWithIdentifier(String identifier) {
		for (BusSignal busSignal : busSignalList) {
			if (busSignal.isIdentifier(identifier)) {
				return busSignal.getTerm();
			}
		}
		return null;
	}

	/**
	 * For debug purpose.
	 */
	@Override
	public String toString() {
		String result = "\"" + toReplace.toString() + "\":\n  {";
		for (BusSignal busSignal : busSignalList) {
			result += "  " + busSignal.toString() + ", ";
		}
		return result + "}";
	}

	/**
	 * Returns a list of bus signals that begin with the given prefix. The
	 * identifiers of the resulting bus signals have the prefix removed.
	 * 
	 * @param prefix
	 * @return
	 */
	public List<BusSignal> getTermsWithPrefix(String prefix) {
		List<BusSignal> resultList = new LinkedList<BusSignal>();

		for (BusSignal busSignal : busSignalList) {
			if (!busSignal.hasPrefix(prefix)) {
				continue;
			}
			String newIdentifier = busSignal.getIdentifier().substring(prefix.length());
			Term newTerm = busSignal.getTerm().createDeepCopy();
			BusSignal newBusSignal = new BusSignal(newIdentifier, newTerm);

			resultList.add(newBusSignal);
		}

		return resultList;
	}

	@Override
	public BusCreatorMacro createDeepCopy() {
		BusCreatorMacro result = new BusCreatorMacro(toReplace.createDeepCopy());

		for (BusSignal oldBusSignal : busSignalList) {
			BusSignal newBusSignal = new BusSignal(oldBusSignal.getIdentifier(),
					oldBusSignal.getTerm().createDeepCopy());

			result.addElement(newBusSignal);
		}

		return result;
	}



}
