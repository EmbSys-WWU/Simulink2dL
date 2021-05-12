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
package org.conqat.lib.commons.string;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.region.LineBasedRegion;
import org.conqat.lib.commons.region.OffsetBasedRegion;

/**
 * Utilities to convert character-based regions to line-based regions
 */
public class LineOffsetUtils {

	/**
	 * Converts the given regions from offset-based to line-based using the
	 * given text.
	 */
	public static List<LineBasedRegion> convertToLineRegions(List<OffsetBasedRegion> regions, String text) {
		LineOffsetConverter lineOffsetConverter = new LineOffsetConverter(text);
		List<LineBasedRegion> result = new ArrayList<>();
		for (OffsetBasedRegion region : regions) {
			result.add(convertToLineRegion(region, lineOffsetConverter));
		}
		return result;
	}

	/** Creates a new line-based region from a offset-based region. */
	public static LineBasedRegion convertToLineRegion(OffsetBasedRegion region,
			LineOffsetConverter lineOffsetConverter) {
		int startLine = lineOffsetConverter.getLine(region.getStart());
		int endLine = lineOffsetConverter.getLine(region.getEnd());
		return new LineBasedRegion(startLine, endLine);
	}

}
