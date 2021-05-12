/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id: codetemplates.xml 18709 2009-03-06 13:31:16Z hummelb $            
 |                                                                       |
 | Copyright (c)  2009-2016 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.ui;

import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.RectangleLayoutData;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;

/**
 * UI transport class for stateflow nodes.
 * 
 * @ConQAT.Rating YELLOW Hash: 1D18F428AC36DD904231DE66215AB121
 */
public class StateflowNodeUiData extends SimulinkUiDataBase {

	/** Layout data. */
	private final RectangleLayoutData layoutData;

	/** Label data. */
	private final LabelLayoutData labelData;

	/** Subnodes (or null). */
	private final List<StateflowNodeUiData> nodes;

	/** The type */
	private final ENodeType type;

	/** The id of the subchart (if any). */
	private String subchartId = null;

	/** Constructor. */
	public StateflowNodeUiData(StateflowNodeBase node) {
		super(node.getId(), extractQualifiedName(node));
		this.layoutData = node.obtainLayoutData();

		if (node instanceof StateflowState) {
			this.labelData = ((StateflowState) node).obtainLabelData();
			this.nodes = CollectionUtils.map(((StateflowState) node).getNodes(), StateflowNodeUiData::new);

			StateflowState state = ((StateflowState) node);
			if (state.isNoteBox()) {
				type = ENodeType.NOTE_BOX;
			} else if (state.isGroupState()) {
				type = ENodeType.GROUP_STATE;
			} else if (state.isFunctionState()) {
				type = ENodeType.FUNCTION_STATE;
			} else {
				type = ENodeType.PLAIN;
			}

			if (state.isSubChart()) {
				subchartId = state.getSubViewer().getResolvedId();
			}
		} else {
			this.labelData = null;
			this.nodes = null;
			this.type = ENodeType.JUNCTION;
		}
	}

	/** Returns the qualified name to use. */
	private static String extractQualifiedName(StateflowNodeBase node) {
		if (node instanceof StateflowState) {
			StateflowState state = (StateflowState) node;
			return state.getParentChart().getResolvedId() + "/" + extractId(state.getId());
		}
		return node.getId();
	}

	/** @see #labelData */
	public LabelLayoutData getLabelData() {
		return labelData;
	}

	/** @see #layoutData */
	public RectangleLayoutData getLayoutData() {
		return layoutData;
	}

	/** @see #nodes */
	public List<StateflowNodeUiData> getNodes() {
		return nodes;
	}

	/** @see #type */
	public ENodeType getType() {
		return type;
	}

	/** @see #subchartId */
	public String getSubchartId() {
		return subchartId;
	}

	/**
	 * Extracts the actual ID/SSID. We need to split at the colon, as our SLX to
	 * MDL transformator adds stuff before the ":" to make IDs unique.
	 */
	public static String extractId(String stateflowId) {
		return StringUtils.getLastPart(stateflowId, ':');
	}

	/** The different node types. */
	public static enum ENodeType {

		/** A junction. */
		JUNCTION,

		/** A note box. */
		NOTE_BOX,

		/** A group state. */
		GROUP_STATE,

		/** A function state. */
		FUNCTION_STATE,

		/** A plain state. */
		PLAIN
	}
}
