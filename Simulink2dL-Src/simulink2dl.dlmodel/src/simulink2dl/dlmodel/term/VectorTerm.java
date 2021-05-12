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

package simulink2dl.dlmodel.term;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
/**
 * VectorTerm for vector macro applications
 *
 */
public class VectorTerm extends LinkedList<Term> implements Term {
	
	public VectorTerm(List<Term> toReplaceList) {
		for(Term term : toReplaceList) {
			this.add(term);
		}
	}
	
	public VectorTerm(VectorTerm vectorTerm) {
		for(Term var : vectorTerm) {
			this.add(var);
		}
	}

	public VectorTerm() {
		super();
	}
	
	public void set(VectorTerm vectorTerm) {
		this.clear();
		for(Term var : vectorTerm) {
			this.add(var);
		}
	}

	@Override
	public String toString() {
		String toString = "";
        for (int i = 0; i < this.size(); i++) {
        	toString += this.get(i).toString()+ " ";
        }
		return toString;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		for (int i = 0; i < this.size(); i++) {
			Term oldTerm = this.get(i);
			if (oldTerm.equals(toReplace)) {
				this.set(i, replaceWith);
			} else if (oldTerm.containsTerm(toReplace)) {
				oldTerm.replaceTermRecursive(toReplace, replaceWith);
			}
		}
	}
	
	/* special treatment for other VectorTerms */
	public void replaceTermRecursive(Term toReplace, VectorTerm replaceWithVector) {
		VectorTerm newReplaceWithVector = new VectorTerm();
		
		for (Term oldTerm : this) {
			if (oldTerm.equals(toReplace)) {
				for (Term replacement : replaceWithVector) {
					newReplaceWithVector.add(replacement.createDeepCopy());
				}
			} else if (oldTerm.containsTerm(toReplace)) {
				for (Term replacement : replaceWithVector) {
					Term newReplaceWith = oldTerm.createDeepCopy();
					newReplaceWith.replaceTermRecursive(toReplace, replacement);
					newReplaceWithVector.add(newReplaceWith);
				}
			} else {
				newReplaceWithVector.add(oldTerm.createDeepCopy());
			}
		}
		
		this.set(newReplaceWithVector);
		return;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj != null && this.getClass() == obj.getClass()) {
			VectorTerm other = (VectorTerm) obj;
			if (this.size()  == other.size()) {
				for(int i = 0; i<this.size(); i++) {
					if (!(this.get(i).equals(other.get(i)))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsTerm(Term term) {
		return false;
	}

	@Override
	public VectorTerm createDeepCopy() {
		VectorTerm copy = new VectorTerm();
		for(Term term : this) {
			copy.add(term.createDeepCopy());
		}
		return copy;
	}
}