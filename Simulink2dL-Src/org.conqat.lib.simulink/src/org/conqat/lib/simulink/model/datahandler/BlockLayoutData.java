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

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Encapsulates all information required for layouting a block (relative to its
 * parent's canvas).
 */
public class BlockLayoutData extends RectangleLayoutData {

	/** The orientation of the block. */
	private final EOrientation orientation;

	/** Constructor. */
	public BlockLayoutData(Rectangle position, EOrientation orientation, Color foregroundColor, Color backgroundColor,
			double opacity) {
		super(position, foregroundColor, backgroundColor, opacity);
		this.orientation = orientation;
	}

	/** Returns {@link #orientation}. */
	public EOrientation getOrientation() {
		return orientation;
	}
}
