/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2018 The ConQAT Project                               |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
|                                                                          |
+-------------------------------------------------------------------------*/
package org.conqat.lib.simulink.model.datahandler;

import java.util.Collections;
import java.util.List;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.ListMap;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.util.SimulinkUtils;

/** Helper class for resolving Goto and From blocks and their targets. */
public class GotoFromResolver {

	/** All Goto blocks grouped by their tag. */
	private final ListMap<String, SimulinkBlock> gotoBlocksByTag = new ListMap<>();

	private final ILogger logger;

	/** All From blocks grouped by their tag. */
	private final ListMap<String, SimulinkBlock> fromBlocksByTag = new ListMap<>();

	/** Constructor. */
	/* package */ GotoFromResolver(SimulinkModel model, ILogger logger) {
		this.logger = logger;

		for (SimulinkBlock block : SimulinkUtils.listBlocksDepthFirst(model)) {
			if (SimulinkConstants.TYPE_GOTO.equals(block.getType())) {
				insertGotoOrFromBlock(block, gotoBlocksByTag);
			} else if (SimulinkConstants.TYPE_FROM.equals(block.getType())) {
				insertGotoOrFromBlock(block, fromBlocksByTag);
			}
		}
	}

	private void insertGotoOrFromBlock(SimulinkBlock block, ListMap<String, SimulinkBlock> blocksByTag) {
		String gotoTag = extractGotoTag(block);
		if (gotoTag != null) {
			blocksByTag.add(gotoTag, block);
		}
	}

	/** Returns the GotoTag of a block (may return null). */
	public String extractGotoTag(SimulinkBlock block) {
		String tag = block.getParameter(SimulinkConstants.PARAM_GOTO_TAG);
		if (tag == null) {
			logger.warn("Missing GotoTag in " + block.getId());
		}
		return tag;
	}

	/** Returns the connected From blocks for a Goto block. */
	public List<SimulinkBlock> getConnectedFromBlocks (SimulinkBlock gotoBlock) {
		CCSMAssert.isTrue(SimulinkConstants.TYPE_GOTO.equals(gotoBlock.getType()), "May only be used for goto blocks!");
		String tag = extractGotoTag(gotoBlock);
		if (tag == null) {
			return Collections.emptyList();
		}
		return fromBlocksByTag.getCollectionOrEmpty(tag);
	}

	/** Returns the connected Goto blocks for a From block. */
	public List<SimulinkBlock> getConnectedGotoBlocks (SimulinkBlock fromBlock) {
		CCSMAssert.isTrue(SimulinkConstants.TYPE_FROM.equals(fromBlock.getType()), "May only be used for from blocks!");
		String tag = extractGotoTag(fromBlock);
		if (tag == null) {
			return Collections.emptyList();
		}
		return gotoBlocksByTag.getCollectionOrEmpty(tag);
	}

}

