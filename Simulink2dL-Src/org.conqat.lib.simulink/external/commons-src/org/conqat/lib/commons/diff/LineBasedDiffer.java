/*-----------------------------------------------------------------------+
 | com.teamscale.ui
 |                                                                       |
   $Id$            
 |                                                                       |
 | Copyright (c)  2009-2013 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.commons.diff;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.string.StringUtils;

/**
 * Class for calculating a line-based diff.
 */
public class LineBasedDiffer extends DifferBase<String> {

	/** Whether to ignore whitespace in each line. */
	private final boolean ignoreWhitespace;

	/** Constructor. */
	public LineBasedDiffer(boolean ignoreWhitespace) {
		this.ignoreWhitespace = ignoreWhitespace;
	}

	/** {@inheritDoc} */
	@Override
	protected String getElementText(String element) {
		return element;
	}

	/** {@inheritDoc} */
	@Override
	protected String getDiffName() {
		String diffName = "line-based";
		if (ignoreWhitespace) {
			diffName += " (ignore whitespace)";
		}
		return diffName;
	}

	/** {@inheritDoc} */
	@Override
	protected List<TextChunk> getChunks(String elementText, boolean isLeft) {
		List<String> lines = StringUtils.splitLinesAsList(elementText);

		List<TextChunk> result = new ArrayList<TextChunk>();
		int offset = 0;
		int lineNumber = 1;
		for (String line : lines) {
			result.add(createChunkForLine(offset, lineNumber, line));
			offset += line.length() + 1;
			lineNumber += 1;
		}
		return result;
	}

	/** Creates a chunk for a single line of the element. */
	private TextChunk createChunkForLine(int offset, int lineNumber, String line) {
		int startOffset = offset;
		int endOffset = startOffset + line.length();
		String comparisonText = line;

		if (ignoreWhitespace) {
			comparisonText = line.replaceFirst("^\\s+", StringUtils.EMPTY_STRING);
			int removed = endOffset - startOffset - comparisonText.length();

			startOffset += removed;

			comparisonText = line.replaceFirst("\\s+$", StringUtils.EMPTY_STRING);
			removed = endOffset - startOffset - comparisonText.length();
			endOffset -= removed;

			comparisonText = comparisonText.replaceAll("\\s+", StringUtils.EMPTY_STRING);
		}

		return new TextChunk(startOffset, endOffset, lineNumber, lineNumber + 1, comparisonText);
	}
}
