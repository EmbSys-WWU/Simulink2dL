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

import simulink2dl.util.PluginLogger;

/**
 * A ReplaceableTerm that has a size and can be split into a VectorTerm
 * Primarily used for resizable Variables
 * 
 * @author Julius Adelt
 *
 */
public abstract class ResizableTerm implements Term{
	
	private int size = 0; //non-vectors have size 0
	
	private VectorTerm termAsVector;
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if(this.size>0)
			PluginLogger.error("ResizableTerm " + this.getClass().toString() + " should not be resized more than once");
		this.size = size;
	}
	
	public boolean isVector() {
		return getSize()>0;
	}
	
	public VectorTerm getVector() {
		if(termAsVector == null) {
			termAsVector = new VectorTerm();
			for(int i = 0; i<this.getSize(); i++) {
				termAsVector.add(this.createVectorEntry(i));
			}
		}
		return termAsVector;	
	}
	
	protected abstract ResizableTerm createVectorEntry(int i);

}