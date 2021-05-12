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
package org.conqat.lib.simulink.ui;

import java.util.ArrayList;
import java.util.List;
import org.conqat.lib.simulink.model.ReferencedBlockInfo;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.ESimulinkBlockType;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;

/**
 * Represents all data needed to render a Simulink block and its subBlocks.
 */
public class SimulinkBlockUIData extends SimulinkUiDataBase {

	/**
	 * The block type, needed to identify the graphical representation of the
	 * block
	 */
	private final ESimulinkBlockType type;

	/**
	 * The block SourceType. It is <code>null</code> if it's not a Reference
	 * block.
	 */
	private final String sourceType;

	/** The inner label data. This is often <code>null</code>. */
	private final LabelLayoutData innerLabelData;

	/** The label data. */
	private final LabelLayoutData labelData;

	/** The layout data. */
	private final BlockLayoutData layoutData;

	/** The sub label data. */
	private final LabelLayoutData subLabelData;

	/** The subBlocks of this block. */
	private final List<SimulinkBlockUIData> subBlocks = new ArrayList<SimulinkBlockUIData>();

	/** Inports of this block. */
	private final List<SimulinkPortUIData> inPorts = new ArrayList<SimulinkPortUIData>();

	/** Outports of this block. */
	private final List<SimulinkPortUIData> outPorts = new ArrayList<SimulinkPortUIData>();

	/** Data corresponding to annotations of this block. */
	private final List<SimulinkAnnotationUIData> annotations = new ArrayList<SimulinkAnnotationUIData>();

	/** The label on the block. */
	private final SimulinkLabelOnBlockData labelOnBlock;

	/**
	 * The data corresponding to lines directly contained in this block (e.g.
	 * connecting child blocks).
	 */
	private final List<SimulinkLineUIData> containedLines = new ArrayList<SimulinkLineUIData>();

	/**
	 * If this is not null, this provides information about a referenced block.
	 */
	private ReferenceUiData reference = null;

	/**
	 * Constructor that generates UI data for the specified block and its direct
	 * subBlocks
	 */
	public SimulinkBlockUIData(SimulinkBlock block, IModelResolver modelResolver) {
		this(block, true, modelResolver);
	}

	/**
	 * Constructor that generates UI data.
	 * 
	 * @param provideSubBlocks
	 *            If <code>true</code> the list of subBlocks will be filled with
	 *            data of the direct subBlocks. Else the list will always be
	 *            empty.
	 */
	public SimulinkBlockUIData(SimulinkBlock block, boolean provideSubBlocks, IModelResolver modelResolver) {
		super(block.getName().replaceAll("\\\\\\\\", "\\"), block.getId().replaceAll("\\\\\\\\", "\\"));
		type = determineBlockType(block);
		sourceType = block.getSourceType();
		innerLabelData = block.obtainInnerLabelData();
		labelData = block.obtainLabelData();
		layoutData = block.obtainLayoutData();
		subLabelData = block.obtainSubLabelData();
		labelOnBlock = new SimulinkLabelOnBlockData(block, type);

		addContainedLines(block);
		addAnnotations(block);
		addSubBlocks(block, provideSubBlocks, modelResolver);
		addPorts(block);
		addReferenceInfo(block, modelResolver);
	}

	/**
	 * Adds the block's reference information to the block's UI data.
	 * 
	 * @param block
	 *            the block whose UI data is being generated.
	 * @param modelResolver
	 *            interface which resolves model references.
	 */
	private void addReferenceInfo(SimulinkBlock block, IModelResolver modelResolver) {
		ReferencedBlockInfo referenceInfo = block.getReferencedBlockInfo();
		if (referenceInfo != null) {
			String modelPath = modelResolver.resolveModelPath(referenceInfo.getModelName(),
					referenceInfo.getBlockName());
			this.reference = new ReferenceUiData(modelPath, referenceInfo.getBlockName());
		}
	}

	/**
	 * Adds the block's ports (both InPorts and OutPorts) to the block's UI
	 * data.
	 * 
	 * @param block
	 *            the block whose UI data is being generated.
	 */
	private void addPorts(SimulinkBlock block) {
		for (SimulinkInPort inPort : block.getInPorts()) {
			this.inPorts.add(new SimulinkPortUIData(inPort));
		}
		for (SimulinkOutPort outPort : block.getOutPorts()) {
			this.outPorts.add(new SimulinkPortUIData(outPort));
		}
	}

	/**
	 * Adds the block's subBlocks to the block's UI data.
	 * 
	 * @param block
	 *            the block whose UI data is being generated.
	 * @param provideSubBlocks
	 *            flag indicating whether the block has subBlocks or not.
	 * @param modelResolver
	 *            interface which resolves model references.
	 */
	private void addSubBlocks(SimulinkBlock block, boolean provideSubBlocks, IModelResolver modelResolver) {
		if (provideSubBlocks) {
			for (SimulinkBlock subBlock : block.getSubBlocks()) {
				this.subBlocks.add(new SimulinkBlockUIData(subBlock, false, modelResolver));
			}
		}
	}

	/**
	 * Adds the block's contained lines to the block's UI data.
	 * 
	 * @param block
	 *            the block whose UI data is being generated
	 */
	private void addContainedLines(SimulinkBlock block) {
		for (SimulinkLine line : block.getContainedLines()) {
			this.containedLines.add(new SimulinkLineUIData(line));
		}
	}

	/**
	 * Adds the block's annotations to the block's UI data.
	 * 
	 * @param block
	 *            the block whose UI data is being generated
	 */
	private void addAnnotations(SimulinkBlock block) {
		for (SimulinkAnnotation annotation : block.getAnnotations()) {
			this.annotations.add(new SimulinkAnnotationUIData(annotation));
		}
	}

	/**
	 * Determines the block type corresponding to the
	 * {@link ESimulinkBlockType}.
	 * 
	 * @param block
	 *            the SimulinkBlock for which this object represents the UI
	 *            data.
	 */
	public static ESimulinkBlockType determineBlockType(SimulinkBlock block) {
		if (block.getParent() == null) {
			// Model itself is of type SUB_SYSTEM for the UI
			return ESimulinkBlockType.SUB_SYSTEM;
		}

		String blockType = block.getType();
		if (SimulinkConstants.TYPE_SUM.equals(blockType) && block.getParameter(SimulinkConstants.PARAM_ICON_SHAPE)
				.contains(SimulinkConstants.VALUE_SHAPE_ROUND)) {
			return ESimulinkBlockType.SUM_ROUND;
		}
		if (SimulinkConstants.TYPE_REFERENCE.equals(block.getType())) {
			return ESimulinkBlockType.getReferenceBlockEnumType(block.getSourceType(),
					block.getParameter(SimulinkConstants.PARAM_SOURCE_BLOCK));
		}
		try {
			// Convert block type to enum form: BlockType -> BLOCK_TYPE
			String pattern = "([A-Z])";
			String blockTypeInEnumForm = blockType.replaceAll(pattern, "_$1").substring(1).toUpperCase();
			return ESimulinkBlockType.valueOf(blockTypeInEnumForm);
		} catch (IllegalArgumentException e) {
			return ESimulinkBlockType.DEFAULT;
		}
	}

	/**
	 * @see #type
	 */
	public ESimulinkBlockType getType() {
		return type;
	}

	/**
	 * @see #innerLabelData
	 */
	public LabelLayoutData getBlockInnerLabelData() {
		return innerLabelData;
	}

	/**
	 * @see #labelData
	 */
	public LabelLayoutData getBlockLabelData() {
		return labelData;
	}

	/**
	 * @see #layoutData
	 */
	public BlockLayoutData getBlockLayoutData() {
		return layoutData;
	}

	/**
	 * @see #subLabelData
	 */
	public LabelLayoutData getBlockSubLabelData() {
		return subLabelData;
	}

	/**
	 * @see #subBlocks
	 */
	public List<SimulinkBlockUIData> getSubBlocks() {
		return subBlocks;
	}

	/**
	 * @see #inPorts
	 */
	public List<SimulinkPortUIData> getInPorts() {
		return inPorts;
	}

	/**
	 * @see #outPorts
	 */
	public List<SimulinkPortUIData> getOutPorts() {
		return outPorts;
	}

	/**
	 * @see #annotations
	 */
	public List<SimulinkAnnotationUIData> getAnnotations() {
		return annotations;
	}

	/**
	 * @see #containedLines
	 */
	public List<SimulinkLineUIData> getContainedLines() {
		return containedLines;
	}

	/**
	 * @see #labelOnBlock
	 */
	public SimulinkLabelOnBlockData getLabelOnBlock() {
		return labelOnBlock;
	}

	/** @see #sourceType */
	public String getSourceType() {
		return sourceType;
	}

	/** @see #reference */
	public ReferenceUiData getReference() {
		return reference;
	}

	/** Data for reference links. Only used for transport to JavaScript UI. */
	@SuppressWarnings("unused")
	private static class ReferenceUiData {

		/**
		 * Path of the referenced block. This may be null to indicate broken
		 * links.
		 */
		private final String path;

		/** Id of the referenced block. */
		private final String id;

		/** Constructor. */
		public ReferenceUiData(String path, String id) {
			this.path = path;
			this.id = id;
		}
	}

	/** Resolves model references. */
	@FunctionalInterface
	public static interface IModelResolver {

		/**
		 * Returns the path to the given model or null.
		 * 
		 * @param containedBlock
		 *            id of a referenced block within the model. This can be
		 *            used to check whether the right model was found.
		 */
		String resolveModelPath(String modelName, String containedBlock);
	}
}
