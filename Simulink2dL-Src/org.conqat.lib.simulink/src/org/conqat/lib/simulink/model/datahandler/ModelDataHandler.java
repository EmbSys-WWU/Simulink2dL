/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 the ConQAT Project                                   |
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
+-------------------------------------------------------------------------*/
package org.conqat.lib.simulink.model.datahandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableList;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.simulink.SimulinkLayoutHandler;
import org.conqat.lib.simulink.model.datahandler.stateflow.StateflowLayoutHandler;

/**
 * Data handlers for the Simulink/StateFlow model. The purpose of the data
 * handler is to interpret the data stored in Simulink's key/value properties
 * and provide a meaningful interpretation. Most of the interpretation is layout
 * oriented and delegated to the corresponding layout handlers.
 */
public class ModelDataHandler {

	/** Logger used for reporting any problems during model data extraction. */
	protected final ILogger logger;

	/** The directories to search for referenced models (MDL/SLX) in. */
	private final List<File> referenceDirectories;

	/**
	 * This is the ID of a parent block that is used to build the resolved ID of
	 * included library blocks. This may be null, if there is no parent block.
	 */
	private final String parentBlockId;

	/**
	 * The prefix to discard from a library block during creation of resolved
	 * IDs. May be null to indicate default (top-level) behavior.
	 */
	private String discardedPrefix;

	/** Layout helper for Simulink. */
	private final SimulinkLayoutHandler simulinkLayoutHandler;

	/** Layout helper for StateFlow. */
	private final StateflowLayoutHandler stateflowLayoutHandler;

	/** Constructor. */
	/* package */ ModelDataHandler(ModelBuildingParameters parameters, SimulinkLayoutHandler simulinkLayoutHandler,
			StateflowLayoutHandler stateflowLayoutHandler) {

		this.simulinkLayoutHandler = simulinkLayoutHandler;
		this.stateflowLayoutHandler = stateflowLayoutHandler;

		logger = parameters.getLogger();
		this.parentBlockId = parameters.getParentBlockId();
		this.discardedPrefix = parameters.getDiscardedPrefix();
		this.referenceDirectories = new ArrayList<>(parameters.getReferencePaths());
	}

	/**
	 * Returns the directories that should be searched when looking for
	 * references.
	 */
	public UnmodifiableList<File> getReferenceDirectories() {
		return CollectionUtils.asUnmodifiable(referenceDirectories);
	}

	/** @see #parentBlockId */
	public String getParentBlockId() {
		return parentBlockId;
	}

	/** @see #discardedPrefix */
	public String getDiscardedPrefix() {
		return discardedPrefix;
	}

	/** @see #simulinkLayoutHandler */
	public SimulinkLayoutHandler getSimulinkLayoutHandler() {
		return simulinkLayoutHandler;
	}

	/** @see #stateflowLayoutHandler */
	public StateflowLayoutHandler getStateflowLayoutHandler() {
		return stateflowLayoutHandler;
	}
	
	/** Factory method for creating a {@link GotoFromResolver}. */
	public GotoFromResolver createGotoFromResolver(SimulinkModel model) {
		return new GotoFromResolver(model, logger);
	}
}
