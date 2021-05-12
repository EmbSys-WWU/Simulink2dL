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
package simulink2dl.transform.model;

import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.transform.dlmodel.DLModelSimulink;

public class ConcurrentContractBehavior {
	
	private HybridProgramCollection concurrentContractGhostAssignments;
	private HybridProgramCollection concurrentContractOutputAssignments;
	private HybridProgramCollection concurrentContractsAsHPs;

	public ConcurrentContractBehavior() {
		
	}
	
	public HybridProgramCollection getGhostAssignments() {
		if(concurrentContractGhostAssignments == null) {
			concurrentContractGhostAssignments = new HybridProgramCollection();
		}
		return concurrentContractGhostAssignments;
	}
	
	public HybridProgramCollection getOutputAssignments() {
		if(concurrentContractOutputAssignments == null) {
			concurrentContractOutputAssignments = new HybridProgramCollection();
		}
		return concurrentContractOutputAssignments;
	}
	
	public HybridProgramCollection getContractsAsHPs() {
		if(concurrentContractsAsHPs == null) {
			concurrentContractsAsHPs = new HybridProgramCollection();
		}
		return concurrentContractsAsHPs;
	}
	
	
	public void addToModel(DLModelSimulink dlModel) {
		if(concurrentContractGhostAssignments!=null) {
			dlModel.addBehavior(concurrentContractGhostAssignments);
		}
		if(concurrentContractOutputAssignments!=null) {
			dlModel.addBehavior(concurrentContractOutputAssignments);
		}
		if(concurrentContractsAsHPs!=null) {
			dlModel.addBehavior(concurrentContractsAsHPs);
		}
	}
	
	/**
	 * Adds the given element to the collection.
	 * 
	 * @param element
	 */
	public void addGhostAssignments(HybridProgramCollection hpc) {
		if(concurrentContractGhostAssignments == null) {
			concurrentContractGhostAssignments = new HybridProgramCollection();
		}
		concurrentContractGhostAssignments.addElement(hpc);
		
	}
	
	/**
	 * Adds the given element to the collection.
	 * 
	 * @param element
	 */
	public void addOutputAssignments(HybridProgramCollection hpc) {
		if(concurrentContractOutputAssignments == null) {
			concurrentContractOutputAssignments = new HybridProgramCollection();
		}
		concurrentContractOutputAssignments.addElement(hpc);
	}
	
	/**
	 * Adds the given element to the collection.
	 * 
	 * @param element
	 */
	public void addHP(HybridProgramCollection hpc) {
		if(concurrentContractsAsHPs == null) {
			concurrentContractsAsHPs = new HybridProgramCollection();
		}
		concurrentContractsAsHPs.addElement(hpc);
	}
	
}
