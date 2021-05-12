/*-----------------------------------------------------------------------+
 | com.teamscale.index
 |                                                                       |
   $Id: codetemplates.xml 18709 2009-03-06 13:31:16Z hummelb $            
 |                                                                       |
 | Copyright (c)  2009-2016 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.simulink.ui;

import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.TransitionLayoutData;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

/**
 * UI transport class for stateflow transitions.
 * 
 * @ConQAT.Rating YELLOW Hash: 58AE9120A4383243978CF8CF356FB996
 */
public class StateflowTransitionUiData {

	/** The layout data. */
	private final TransitionLayoutData layoutData;

	/** The label data. */
	private LabelLayoutData labelData;

	/** Constructor. */
	public StateflowTransitionUiData(StateflowTransition transition) {
		this.labelData = transition.obtainLabelData();
		this.layoutData = transition.obtainLayoutData();
	}

	/** @see #layoutData */
	public TransitionLayoutData getLayoutData() {
		return layoutData;
	}

	/** @see #labelData */
	public LabelLayoutData getLabelData() {
		return labelData;
	}
}
