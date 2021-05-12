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
package simulink2dl.dlmodel.contracts;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.ReplaceableTerm;

/**
 * @author nick
 *
 */
@SuppressWarnings("serial")
public class ContractWrapper implements Serializable {

	private int numberInPorts;
	private int numberOutPorts;

	private Map<SimulinkInPort, ReplaceableTerm> inPortToReplaceableTerm = new HashMap<>();
	private Map<SimulinkOutPort, ReplaceableTerm> outPortToReplaceableTerm = new HashMap<>();

	private Set<ReplaceableTerm> symbols = new HashSet<>();

	private Set<Formula> assumptions = new HashSet<>();
	private Set<Formula> guarantees = new HashSet<>();

	/**
	 * ContractWrappers provide a way to associate parsed Simulink models with
	 * contracts, proven by KeYmaeraX.
	 * 
	 * @param symbols        A Collection of dlmodel.elements.Variables and
	 *                       dlmodel.elements.Constants.
	 * @param assumptions    Collection of assumptions.
	 * @param guarantees     Collection of guarantees.
	 * @param numberInPorts  The number of inPorts the feature has.
	 * @param numberOutPorts The number of outPorts the feature has.
	 */
	public ContractWrapper(Collection<ReplaceableTerm> symbols, Collection<Formula> assumptions,
			Collection<Formula> guarantees, int numberInPorts, int numberOutPorts) {
		this.symbols.addAll(symbols);
		this.assumptions.addAll(assumptions);
		this.guarantees.addAll(guarantees);
		this.numberInPorts = numberInPorts;
		this.numberOutPorts = numberOutPorts;
	}

	/**
	 * In order to assure users don't accidentally overwrite the symbol map, they
	 * are required to reset the map manually beforehand.
	 * 
	 * @param inPorts
	 */
	public void resetPortToSymbolMap(boolean inPorts) {
		if (inPorts) {
			this.inPortToReplaceableTerm.clear();
		} else {
			this.outPortToReplaceableTerm.clear();
		}
	}

	/**
	 * A valid map must provide a mapping for each port to a symbol in the set of
	 * symbols of the instance.
	 * 
	 * @param mapping
	 * @return
	 */
	public boolean setInPortToSymbolMap(Map<SimulinkInPort, ReplaceableTerm> mapping) {
		boolean abort = false;
		if (this.inPortToReplaceableTerm.isEmpty()) {
			// PluginLogger.info("ContractWrapper: A mapping exists already! Reset it,
			// before setting a new one.");
			abort = true;
		}

		if (this.numberInPorts != mapping.keySet().size()) {
			// PluginLogger.error("ContractWrapper: Every Port has to be mapped to a symbol.
			// " + mapping.keySet());
			abort = true;
		}

		if (!this.symbols.containsAll(mapping.values())) {
			// PluginLogger.error("ContractWrapper: Mapping contains an unknown symbol. " +
			// mapping.values());
			abort = true;
		}

		if (abort) {
			return false;
		}

		this.inPortToReplaceableTerm = mapping;
		return true;
	}

	/**
	 * A valid map must provide a mapping for each port to a symbol in the set of
	 * symbols of the instance.
	 * 
	 * @param mapping
	 * @return
	 */
	public boolean setOutPortToSymbolMap(Map<SimulinkOutPort, ReplaceableTerm> mapping) {
		boolean abort = false;
		if (this.outPortToReplaceableTerm.isEmpty()) {
			// PluginLogger.info("ContractWrapper: A mapping exists already! Reset it,
			// before setting a new one.");
			abort = true;
		}

		if (this.numberOutPorts != mapping.keySet().size()) {
			// PluginLogger.error("ContractWrapper: Every Port has to be mapped to a symbol.
			// " + mapping.keySet());
			abort = true;
		}

		if (!this.symbols.containsAll(mapping.values())) {
			// PluginLogger.error("ContractWrapper: Mapping contains an unknown symbol. " +
			// mapping.values());
			abort = true;
		}

		if (abort) {
			return false;
		}

		this.outPortToReplaceableTerm = mapping;
		return true;
	}

	/**
	 * @param inPort
	 * @return
	 */
	public Set<Formula> getAssumptionsByInPort(SimulinkInPort inPort) {
		ReplaceableTerm internalName = this.inPortToReplaceableTerm.get(inPort);
		return this.getAssumptionsByReplaceableTerm(internalName);
	}

	/**
	 * @param term
	 * @return
	 */
	public Set<Formula> getAssumptionsByReplaceableTerm(ReplaceableTerm term) {
		Set<Formula> returnee = new HashSet<>();

		for (Formula form : this.assumptions) {
			if (form.containsTerm(term)) {
				returnee.add(form);
			}
		}

		return returnee;
	}

	/**
	 * @param outPort
	 * @return
	 */
	public Set<Formula> getGuaranteesByOutPort(SimulinkOutPort outPort) {
		ReplaceableTerm internalName = this.outPortToReplaceableTerm.get(outPort);
		return this.getGuaranteesByReplaceableTerm(internalName);
	}

	/**
	 * @param term
	 * @return
	 */
	public Set<Formula> getGuaranteesByReplaceableTerm(ReplaceableTerm term) {
		Set<Formula> returnee = new HashSet<>();

		for (Formula form : this.guarantees) {
			if (form.containsTerm(term)) {
				returnee.add(form);
			}
		}

		return returnee;
	}

	/**
	 * @return the numberInPorts
	 */
	public int getNumberInPorts() {
		return numberInPorts;
	}

	/**
	 * @return the numberOutPorts
	 */
	public int getNumberOutPorts() {
		return numberOutPorts;
	}

	/**
	 * @return the assumptions
	 */
	public Set<Formula> getAssumptions() {
		return assumptions;
	}

	/**
	 * @return the guarantees
	 */
	public Set<Formula> getGuarantees() {
		return guarantees;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
