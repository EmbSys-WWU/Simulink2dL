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

import java.awt.BasicStroke;

/**
 * The possible stroke styles used in Simulink/StateFlow.
 */
public enum ESimulinkStrokeStyle {

	/** Standard stroke. */
	DEFAULT(new BasicStroke()),

	/**
	 * Dashed stroke modelled after the outline stroke of AND-type states in
	 * stateflow. Except for the dashing, it is identical to DEFAULT.
	 */
	DASHED(new BasicStroke(DEFAULT.getStroke().getLineWidth(), DEFAULT.getStroke().getEndCap(), DEFAULT.getStroke().getLineJoin(), DEFAULT.getStroke().getMiterLimit(), new float[] { 2.0f, 3.0f }, 0.0f)),

	/** The stroke used for unconnected lines. */
	UNCONNECTED_LINE_STROKE(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 3 }, 0)),

	/** Stroke for the outline of a subchart. (Stroke is wider than default) */
	SUBCHART_OUTLINE_STROKE(new BasicStroke(DEFAULT.getStroke().getLineWidth() * 2));

	/** The actual stroke style. */
	private final BasicStroke stroke;

	/** private constructor */
	private ESimulinkStrokeStyle(BasicStroke stroke) {
		this.stroke = stroke;
	}

	/** Returns {@link #stroke}. */
	public BasicStroke getStroke() {
		return stroke;
	}
}
