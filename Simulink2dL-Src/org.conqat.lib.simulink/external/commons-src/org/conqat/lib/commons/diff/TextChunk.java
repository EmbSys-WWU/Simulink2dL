/*-----------------------------------------------------------------------+
 | com.teamscale.ui
 |                                                                       |
   $Id$            
 |                                                                       |
 | Copyright (c)  2009-2013 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.commons.diff;

/**
 * Class describing a substring of the diffed text as used for the diff
 * algorithms.
 */
public class TextChunk {

	/** Start offset of the text (inclusive, 0-based). */
	/* package */final int startOffset;

	/** End offset of the text (exclusive, 0-based). */
	/* package */final int endOffset;

	/** Start offset of the text (inclusive, 1-based). */
	/* package */final int startLine;

	/** End offset of the text (exclusive, 1-based). */
	/* package */final int endLine;

	/** Text used for comparison of the chunk. */
	private final String comparisonText;

	/** Constructor. */
	public TextChunk(int startOffset, int endOffset, int startLine, int endLine, String comparisonText) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.startLine = startLine;
		this.endLine = endLine;
		this.comparisonText = comparisonText;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return comparisonText.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object other) {
		return ((TextChunk) other).comparisonText.equals(comparisonText);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "\"" + comparisonText + "\" (lines: " + startLine + "-" + endLine + ", offsets: " + startOffset + "-"
				+ endOffset + ")";
	}
}
