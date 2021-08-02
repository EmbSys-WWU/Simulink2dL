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

public class MinMaxTerm implements Term {
	
	public MinMaxTerm(Term first, Term second, String minOrMax) {
		if(minOrMax.toLowerCase().startsWith("min"))
			this.minOrMax = "min";
		else if(minOrMax.toLowerCase().startsWith("max"))
			this.minOrMax = "max";
		else {
			throw new IllegalArgumentException("MinMax Block with neither 'min' or 'max'");
		}
		
		this.first = first;
		this.second = second;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinMaxTerm other = (MinMaxTerm) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else 	if (second == null) {
			if (other.second != null)
				return false;
		} else if (minOrMax != other.minOrMax) {
			return false;
		}
		return true;
	}

	private Term first;
	private Term second;
	private String minOrMax;
	
	@Override
	public boolean isAtomic() {
		return false;
	}


	public String toString() {
		return minOrMax+"("+first+","+second+")";
	}

	@Override
	public void replaceTermRecursive(Term toReplace, Term replaceWith) {
		if(first.equals(toReplace)) {
			first = replaceWith;
		} else {
			first.replaceTermRecursive(toReplace, replaceWith);
		}
		
		if(second.equals(toReplace)) {
			second = replaceWith;
		} else {
			second.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	@Override
	public boolean containsTerm(Term term) {	
		if(first.equals(term) || second.equals(term)) {
			return true;
		} else if (first.containsTerm(term) || second.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public MinMaxTerm createDeepCopy() {
		return new MinMaxTerm(first.createDeepCopy(), second.createDeepCopy(), minOrMax);
	}

}
