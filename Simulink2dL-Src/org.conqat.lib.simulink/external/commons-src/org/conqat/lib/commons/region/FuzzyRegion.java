/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2017 The ConQAT Project                               |
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
|                                                                          |
+-------------------------------------------------------------------------*/
package org.conqat.lib.commons.region;

/**
 * FuzzyRegions represent intervals with variable size and start index. A fuzzy
 * region has a start index somewhere between A and B and an end index somewhere
 * between C and D. FuzzyRegions can, for example, represent the expected
 * location of a finding after a file-content change.
 * <p>
 * The intervals for the start and end indices are represented as
 * {@link Region}s.
 * <p>
 * A {@link FuzzyRegion} is empty if the maximum index in its
 * {@link FuzzyRegion#endIndexRegion} is smaller than the minimal index in its
 * {@link FuzzyRegion#startIndexRegion}.
 * <p>
 * This class is immutable.
 */
public class FuzzyRegion {
	/** Region for the startIndex */
	private Region startIndexRegion;

	/** Region for the endIndex */
	private Region endIndexRegion;

	/** Constructor. */
	public FuzzyRegion(Region startIndexRegion, Region endIndexRegion) {
		this.startIndexRegion = startIndexRegion;
		this.endIndexRegion = endIndexRegion;
	}

	/** Constructor. */
	public FuzzyRegion(int minNewStartIndex, int maxNewStartIndex, int minNewEndIndex, int maxNewEndIndex) {
		this(new Region(minNewStartIndex, maxNewStartIndex), new Region(minNewEndIndex, maxNewEndIndex));
	}

	/** @see #startIndexRegion */
	public Region getStartIndexRegion() {
		return startIndexRegion;
	}

	/** @see #endIndexRegion */
	public Region getEndIndexRegion() {
		return endIndexRegion;
	}
}
