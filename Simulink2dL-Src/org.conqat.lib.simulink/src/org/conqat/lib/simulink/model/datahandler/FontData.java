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

import java.awt.Font;

/**
 * Encapsulates all information required for font selection.
 */
public class FontData {

	/**
	 * This can be used during testing to override the font used in the model
	 * with a fixed font to ensure the same results on different operating
	 * systems during regression testing.
	 */
	/* package */static Font overrideFont = null;

	/** The name of the font. */
	private final String name;

	/** The size of the font in pixels. */
	private final int size;

	/** Whether the font should be bold. */
	private final boolean bold;

	/** Whether the font should be italic. */
	private final boolean italic;

	/** Constructor. */
	public FontData(String name, int size, boolean bold, boolean italic) {
		this.name = name;
		this.size = size;
		this.bold = bold;
		this.italic = italic;
	}

	/** Returns {@link #name}. */
	public String getName() {
		return name;
	}

	/** Returns {@link #size}. */
	public int getSize() {
		return size;
	}

	/** Returns {@link #bold}. */
	public boolean isBold() {
		return bold;
	}

	/** Returns {@link #italic}. */
	public boolean isItalic() {
		return italic;
	}

	/** Returns the AWT font object that matches this font most closely. */
	public Font getAwtFont() {
		if (overrideFont != null) {
			return overrideFont.deriveFont(getAwtFontStyle(), size);
		}
		return new Font(name, getAwtFontStyle(), size);
	}

	/** Returns AWT font style value. */
	public int getAwtFontStyle() {
		int style = Font.PLAIN;
		if (bold) {
			style |= Font.BOLD;
		}
		if (italic) {
			style |= Font.ITALIC;
		}
		return style;
	}
}
