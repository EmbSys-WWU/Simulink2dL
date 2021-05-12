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
package simulink2dl.util.order;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.simulink.model.SimulinkBlock;

import simulink2dl.util.PluginLogger;

public class BlockOrder {

	private Set<String> lastBlocks;

	public BlockOrder() {

		lastBlocks = new HashSet<String>();

		lastBlocks.add("FromWorkspace");
		lastBlocks.add("S-Function");
		lastBlocks.add("DiscreteIntegrator");
		lastBlocks.add("Integrator");
		lastBlocks.add("ZeroOrderHold");
		lastBlocks.add("DiscreteTransferFcn");
		lastBlocks.add("UnitDelay");
	}

	/**
	 * Creates a list that defines the order of the transformation of blocks.
	 * 
	 * @param unmodifiableCollection
	 * @param Array                  to return the unsorted blocks
	 * @return
	 */
	public List<SimulinkBlock> generateBlockOrder(UnmodifiableCollection<SimulinkBlock> unmodifiableCollection,
			Set<SimulinkBlock> unsortedBlocks) {
		Set<SimulinkBlock> unsorted = new HashSet<SimulinkBlock>();
		Set<SimulinkBlock> unsortedRev = new HashSet<SimulinkBlock>();
		List<SimulinkBlock> sorted = new LinkedList<SimulinkBlock>();

		// start with source blocks without input
		for (SimulinkBlock block : unmodifiableCollection) {
			if (block.getInPorts().isEmpty()) {
				sorted.add(block);
			} else {
				unsorted.add(block);
			}
		}

		int sizeBefore = 0;
		int sizeAfter = 0;
		do {
			sizeBefore = unsorted.size();
			for (SimulinkBlock myBlock : unsorted) {
				unsortedRev.add(myBlock);
			}
			unsorted.clear();
			for (SimulinkBlock block : unsortedRev) {
				boolean allPredeccesorsAvailable = specialBlockChecking(block, sorted);
				if (allPredeccesorsAvailable) {
					sorted.add(block);
				} else {
					unsorted.add(block);
				}
			}
			sizeAfter = unsorted.size();
			unsortedRev.clear();
		} while ((!(unsorted.isEmpty())) && (!(sizeBefore == sizeAfter)));

		for (SimulinkBlock block : unsorted) {
			// TODO sort the last unsorted blocks
			PluginLogger
					.info("Currently unsorted after sorting : " + block.getName() + " of type " + block.getType());
			sorted.add(block);
			unsortedBlocks.add(block);
		}
		return sorted;
	}

	/**
	 * Checks whether all stateful predecessors of this block are already handled
	 * 
	 * @param block
	 * @param processedBlocks
	 * @return
	 */
	private boolean specialBlockChecking(SimulinkBlock block, List<SimulinkBlock> processedBlocks) {

		boolean allPredeccesorsAvailable = true;
		// every predecessor has to be checked
		for (int i = 0; i < block.getInPorts().size(); i++) {
			SimulinkBlock srcBlock = block.getInLines().get(i).getSrcPort().getBlock();
			// if srcblock is not in sorted AND is no "last Block"
			if (!(processedBlocks.contains(srcBlock))) {
				if (!lastBlocks.contains(srcBlock.getType())) {
					allPredeccesorsAvailable = false;
				}
			}
		}

		return allPredeccesorsAvailable;
	}

}
