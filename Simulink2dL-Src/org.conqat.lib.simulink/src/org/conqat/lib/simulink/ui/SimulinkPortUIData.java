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

import java.awt.Point;

import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.PortLayoutData;

/**
 * Encapsulates all necessary data to render a port
 */
public class SimulinkPortUIData {

	/**
	 * Size of the port arrow, if rendered: Assume (0,0) is the tip of the
	 * arrow, then the other two vertices are (-PORT_ARROW_SIZE,
	 * PORT_ARROW_SIZE) and (-PORT_ARROW_SIZE, -PORT_ARROW_SIZE)
	 */
	private final int portArrowSize = 4;

	/** The block this port belongs to */
	private final String block;

	/** The layout data. */
	private final PortLayoutData layoutData;

	/** The label data. */
	private final LabelLayoutData labelData;

	/**
	 * Flag indicating whether the port is connected (no rendering of ports when
	 * they are connected)
	 */
	private final boolean isConnected;

	/** The tip of the arrow port, if rendered */
	private final Point arrowTip;

	/** Constructor. */
	public SimulinkPortUIData(SimulinkPortBase port) {
		block = port.getBlock().getName();
		layoutData = port.obtainLayoutData();
		labelData = port.obtainLabelData();
		isConnected = port.isConnected();

		Point position = layoutData.getPosition();
		if (port instanceof SimulinkOutPort) {
			switch ((int) layoutData.getDirection()) {
			case 270:
				arrowTip = new Point(position.x, position.y + portArrowSize + 1);
				break;
			case 180:
				arrowTip = new Point(position.x - (portArrowSize + 1), position.y);
				break;
			case 90:
				arrowTip = new Point(position.x, position.y - (portArrowSize + 1));
				break;
			default:
				arrowTip = new Point(position.x + portArrowSize + 1, position.y);
			}

		} else {
			arrowTip = new Point(position);
		}
	}

	/**
	 * @see #block
	 */
	public String getBlock() {
		return block;
	}

	/**
	 * @see #layoutData
	 */
	public PortLayoutData getLayoutData() {
		return layoutData;
	}

	/**
	 * @see #labelData
	 */
	public LabelLayoutData getLabelData() {
		return labelData;
	}

	/**
	 * @see #isConnected
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * @see #arrowTip
	 */
	public Point getArrowTip() {
		return arrowTip;
	}
}
