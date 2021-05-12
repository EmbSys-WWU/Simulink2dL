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
package simulink2dl.dlmodel.hybridprogram;

import java.util.HashSet;
import java.util.List;

import simulink2dl.dlmodel.term.Term;

/**
 * This class only exists for debugging purposes. It implements the interface
 * HybridProgram and just contains a string. It can be created to check whether
 * the toString method of HybridPrograms at a higher level work as intended.
 * 
 * @author Timm Liebrenz
 *
 */
public class DebugString implements HybridProgram {

	private String content;

	public DebugString(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return content;
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
	public DebugString createDeepCopy() {
		return new DebugString(content);
	}

	@Override
	public HybridProgram expand() {
		// TODO Auto-generated method stub
		return this;
	}


}
