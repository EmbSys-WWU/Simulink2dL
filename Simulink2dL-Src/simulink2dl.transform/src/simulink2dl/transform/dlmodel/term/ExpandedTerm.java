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
package simulink2dl.transform.dlmodel.term;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.util.PluginLogger;

/**
 * Special case of a VectorTerm that counters unwanted Term duplication when applying VectorMacros to each other
 * Example:
 * (#out1 = #out2) is expanded by VectorMacro #out1 <- (a,b), which
 * leads to the Term (a = #out2, b = #out2).
 * 
 * If (a = #out2, b = #out2) is a VectorTerm, 
 * the application of another Macro #out2 <- (c,d) 
 * leads to the Term
 * (a = (c,d), b = (c,d)). 
 * 
 * If (a = #out2, b=#out2) is an ExpandedTerm, this leads to
 * (a = c, b = d).
 *
 */ 
@SuppressWarnings("serial")
public class ExpandedTerm extends VectorTerm {
	
	public ExpandedTerm(LinkedList<Variable> vectorVariables) {
		for(Variable var : vectorVariables) {
			this.add(var);
		}
	}
	
	public ExpandedTerm() {
		super();
	}

	public ExpandedTerm(List<ReplaceableTerm> toReplaceList) {
		for(ReplaceableTerm replc : toReplaceList) {
			this.add(replc);
		}
	}
	
	public ExpandedTerm(VectorTerm toReplaceList) {
		super(toReplaceList);
	}

	public void replaceTermRecursive(Term toReplace, VectorTerm replaceWithList) {
		VectorTerm newReplaceWithList = new VectorTerm();
		if(this.size() != replaceWithList.size()) {
			PluginLogger.error(this.getClass().toString()+" failed to combine VectorTerms of different size");
		} else {
			for(int i = 0; i<this.size();i++) {
				Term oldTerm = this.get(i);
				Term newTerm = replaceWithList.get(i);
				Term newReplaceWith = oldTerm.createDeepCopy();
				if (oldTerm.equals(toReplace)) {
					newReplaceWithList.add(newTerm.createDeepCopy());
				} else {
					newReplaceWith.replaceTermRecursive(toReplace, replaceWithList.get(i));
					newReplaceWithList.add(newReplaceWith);
				}
			}
		}
		this.set(newReplaceWithList);
		return;
	}

}