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
import java.awt.Stroke;

/**
 * Encapsulates all information required for layouting a rectangular element.
 */
public class RectangleLayoutData {

	/** Foreground color of the element (usually the border). */
	private final Color foregroundColor;

	/** Background color of the element (usually the fill color). */
	private final Color backgroundColor;

	/**
	 * The position of the element (which in Simulink terms also includes size
	 * information).
	 */
	private final Rectangle position;

	/**
	 * The strokeStyle that should be used to draw the outline of this
	 * rectangle.
	 */
	private final ESimulinkStrokeStyle strokeStyle;

	/** The opacity value of the element. */
	private final double opacity;

	/** Constructor. */
	protected RectangleLayoutData(Rectangle position, Color foregroundColor, Color backgroundColor, double opacity) {
		this.position = position;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
		this.strokeStyle = ESimulinkStrokeStyle.DEFAULT;
		this.opacity = opacity;
	}

	/** Constructor with stroke style (e.g., ESimulinkStrokeStyle.DASHED). */
	protected RectangleLayoutData(Rectangle position, Color foregroundColor, Color backgroundColor,
			ESimulinkStrokeStyle stroke, double opacity) {
		this.position = position;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
		this.strokeStyle = stroke;
		this.opacity = opacity;
	}

	/** @see #strokeStyle */
	public ESimulinkStrokeStyle getStrokeStyle() {
		return strokeStyle;
	}

	/**
	 * Returns the AWT {@link Stroke} that should be used to draw this
	 * Rectangle.
	 */
	public Stroke getStroke() {
		return strokeStyle.getStroke();
	}

	/** Returns {@link #foregroundColor}. */
	public Color getForegroundColor() {
		return foregroundColor;
	}

	/** Returns {@link #backgroundColor}. */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/** Returns {@link #position}. */
	public Rectangle getPosition() {
		return position;
	}

	/** Returns {@link #opacity}. */
	public double getOpacity() {
		return opacity;
	}
}
