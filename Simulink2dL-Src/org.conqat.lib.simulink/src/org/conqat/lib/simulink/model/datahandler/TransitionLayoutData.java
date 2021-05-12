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

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all information required for layouting a transition (relative to
 * its parent's canvas).
 * 
 * The transition's shape is described by points (usually 5), which are the
 * support points of the curve. The points are fixed and interpolation points
 * mixed alternatively. Interpolation should be quadratic (e.g. using Java's
 * {@link GeneralPath#quadTo(double, double, double, double)} method).
 * 
 * For example, for a path with 5 points, the first segment should be drawn from
 * points[0] to points[2] using points[1] as interpolation/support point, the
 * second segment would be from points[2] to points[4] using points[3] as
 * support.
 */
public class TransitionLayoutData {

	/**
	 * The points that define the segments of the transition. This includes both
	 * the start and end point. All positions are global and absolute. The order
	 * of the points is from start to end.
	 */
	private final List<Point> points;

	/**
	 * Size of arrowhead at destination of this transition.
	 */
	private final int arrowheadSize;

	/** Constructor. */
	public TransitionLayoutData(List<Point> points, int arrowheadSize) {
		this.points = new ArrayList<>(points);
		this.arrowheadSize = arrowheadSize;
	}

	/** @see #arrowheadSize */
	public int getArrowheadSize() {
		return arrowheadSize;
	}

	/** @see #points */
	public List<Point> getPoints() {
		return points;
	}
}
