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
import java.awt.Point;
import java.util.Map;

/**
 * Encapsulates all information required for layouting a label.
 */
public class LabelLayoutData {

	/** The text of the label. */
	private final String text;

	/** Whether the label is visible. */
	private final boolean visible;

	/** The position of the top left corner of the label. */
	private final Point position;

	/** The font used. */
	private final FontData font;

	/** The color of the label. */
	private final Color color;

	/** The file name of the label icon. */
	private String iconFileName;

	/** The opacity value of the label. */
	private final double opacity;

	/**
	 * Maps placeholder strings to their actual substitute. This map may be null
	 * if no placeholder replacement was performed.
	 */
	private final Map<String, String> placeholderSubstitutes;

	/** Constructor. */
	public LabelLayoutData(String text, boolean visible, FontData font, Point position, Color color, double opacity) {
		this(text, visible, font, position, color, opacity, null);
	}

	/** Constructor. */
	public LabelLayoutData(String text, boolean visible, FontData font, Point position, Color color, double opacity,
			Map<String, String> placeholderSubstitutes) {
		this.text = text;
		this.visible = visible;
		this.font = font;
		this.position = position;
		this.color = color;
		this.placeholderSubstitutes = placeholderSubstitutes;
		this.opacity = opacity;
	}

	/** Constructor. */
	public LabelLayoutData(String text, String iconFileName, FontData font, Point position, Color color, double opacity,
			boolean visible) {
		this(text, visible, font, position, color, opacity, iconFileName, null);
	}

	/** Constructor. */
	public LabelLayoutData(String text, boolean visible, FontData font, Point position, Color color, double opacity,
			String iconFileName, Map<String, String> placeholderSubstitutes) {
		this(text, visible, font, position, color, opacity, placeholderSubstitutes);
		this.iconFileName = iconFileName;
	}

	/** Returns {@link #text}. */
	public String getText() {
		return text;
	}

	/** Returns {@link #visible}. */
	public boolean isVisible() {
		return visible;
	}

	/** Returns {@link #font}. */
	public FontData getFont() {
		return font;
	}

	/** Returns the position of the top left corner of the label. */
	public Point getPosition() {
		return position;
	}

	/** Returns {@link #color}. */
	public Color getColor() {
		return color;
	}

	/** @see #placeholderSubstitutes */
	public Map<String, String> getPlaceholderSubstitutes() {
		return placeholderSubstitutes;
	}

	/** Returns {@link #iconFileName} */
	public String getIconFileName() {
		return this.iconFileName;
	}

	/** Returns {@link #opacity}. */
	public double getOpacity() {
		return opacity;
	}
}
