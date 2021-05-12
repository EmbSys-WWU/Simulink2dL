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
package simulink2dl.dlmodel.elements;

import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.ResizableTerm;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents a variable in dL.
 * 
 * Implementing the ReplaceableTerm interface allows replacing inner Variables defined in contracts.
 * A variable is also resizable and can be split into a VectorTerm.
 * 
 * @author Timm Liebrenz
 *
 */
public class Variable extends ResizableTerm implements ReplaceableTerm{

	private String name;

	private String type; 

	/**
	 * Constructor for a variable
	 * 
	 * @param type
	 * @param name
	 * @param sourceBlock
	 */
	public Variable(String type, String name) {
		// set member variables
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the name of this variable.
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	/**
	 * Creates a String that represent the definition of this variable
	 * 
	 * @return
	 */
	public String toDefString() {
		return type + " " + name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		// do nothing
	}

	@Override
	public boolean containsTerm(Term term) {
		return false;
	}

	@Override
	public Variable createDeepCopy() {
		// do not create copies for variables
		return this;
	}

	public String getType() {
		return type;
	}

	@Override
	protected Variable createVectorEntry(int i) {
		return new Variable(this.type, this.name+i);
	}

	@Override
	public String getIdentifier() {
		return type+"_"+name;
	}

}
