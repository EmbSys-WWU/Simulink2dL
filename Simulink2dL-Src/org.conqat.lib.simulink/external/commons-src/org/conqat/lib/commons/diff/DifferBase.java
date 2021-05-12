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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.algo.Diff;
import org.conqat.lib.commons.algo.Diff.Delta;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.Pair;

/**
 * Base class for the code that produces a {@link DiffDescription} for two
 * elements.
 */
public abstract class DifferBase<T> {

	/**
	 * Maximal size of the delta computed during diff calculation. If there are
	 * more than this number of changes, a partial diff will be displayed (i.e.
	 * only changes in the top region). The reason to limit this, is that the
	 * used algorithm uses O(D^2) memory for a diff of size D.
	 */
	private static final int MAX_DIFF_SIZE = 10000;

	/**
	 * Pattern for extracting fragments. This matches consecutive word
	 * characters, consecutive digits, consecutive whitespace, or a single
	 * character.
	 */
	private static final Pattern FRAGMENT_MATCH_PATTERN = Pattern.compile("\\w+|\\d+|\\s+|.");

	/** Text of the left element. */
	private String leftText;

	/** Text of the right element. */
	private String rightText;

	/** Chunks for the left element. */
	private List<TextChunk> leftChunks;

	/** Chunks for the right element. */
	private List<TextChunk> rightChunks;

	/** Performs the diff and returns a {@link DiffDescription}. */
	public DiffDescription performDiff(T leftElement, T rightElement) {
		leftText = getElementText(leftElement);
		rightText = getElementText(rightElement);

		leftChunks = getChunks(leftElement, true);
		rightChunks = getChunks(rightElement, false);
		Delta<TextChunk> delta = Diff.computeDelta(leftChunks, rightChunks, MAX_DIFF_SIZE);

		DiffDescription diffDescription = new DiffDescription(getDiffName());
		
		convertDiffToDiffDescription(delta, diffDescription);

		return postProcessDiffDescription(diffDescription);
	}

	/**
	 * Post processing of the diffDescription.
	 */
	protected DiffDescription postProcessDiffDescription(DiffDescription diffDescription) {
		// default does no post processing
		return diffDescription;
	}

	/** Get the text of the element. */
	protected abstract String getElementText(T element);

	/**
	 * Processes the given diff and inserts the changed regions into the diff
	 * description.
	 */
	private void convertDiffToDiffDescription(Delta<TextChunk> delta, DiffDescription diffDescription) {
		boolean[] chunkRemoved = new boolean[leftChunks.size()];
		boolean[] chunkAdded = new boolean[rightChunks.size()];

		for (int i = 0; i < delta.getSize(); i++) {
			int position = delta.getPosition(i);
			if (position < 0) { // chunk was removed
				chunkRemoved[-position - 1] = true;
			} else { // chunk was added
				chunkAdded[position - 1] = true;
			}
		}

		int leftChunkPosition = 0;
		int rightChunkPosition = 0;

		while (leftChunkPosition < chunkRemoved.length || rightChunkPosition < chunkAdded.length) {
			int removedChunksRegionStart = leftChunkPosition;
			int addedChunksRegionStart = rightChunkPosition;

			// Moving leftChunkPosition to the end of the removed chunks
			while (leftChunkPosition < chunkRemoved.length && chunkRemoved[leftChunkPosition]) {
				leftChunkPosition++;
			}

			// Moving rightChunkPosition to the end of the added chunks
			while (rightChunkPosition < chunkAdded.length && chunkAdded[rightChunkPosition]) {
				rightChunkPosition++;
			}

			// Check if there where actual changes
			if (removedChunksRegionStart != leftChunkPosition || addedChunksRegionStart != rightChunkPosition) {
				insertDiffRegion(diffDescription, removedChunksRegionStart, leftChunkPosition, addedChunksRegionStart,
						rightChunkPosition);
			} else {
				leftChunkPosition++;
				rightChunkPosition++;
			}
		}
	}

	/**
	 * Inserts a diff region based on unit indexes. Start indexes are inclusive,
	 * end indexes are exclusive.
	 */
	private void insertDiffRegion(DiffDescription diffDescription, int currentLeftStart, int currentLeftEnd,
			int currentRightStart, int currentRightEnd) {

		Pair<Integer, Integer> leftLines = convertToFirstLastLine(currentLeftStart, currentLeftEnd, leftChunks);
		Pair<Integer, Integer> rightLines = convertToFirstLastLine(currentRightStart, currentRightEnd, rightChunks);
		diffDescription.addLineRegion(leftLines.getFirst(), leftLines.getSecond(), rightLines.getFirst(),
				rightLines.getSecond());

		List<TextChunk> leftFragments = extractFragments(currentLeftStart, currentLeftEnd, leftText, leftChunks);
		List<TextChunk> rightFragments = extractFragments(currentRightStart, currentRightEnd, rightText, rightChunks);
		Delta<TextChunk> delta = Diff.computeDelta(leftFragments, rightFragments, MAX_DIFF_SIZE);
		for (int i = 0; i < delta.getSize(); ++i) {
			int position = delta.getPosition(i);
			if (position > 0) {
				TextChunk fragment = rightFragments.get(position - 1);
				diffDescription.addRightChange(fragment.startOffset, fragment.endOffset);
			} else {
				TextChunk fragment = leftFragments.get(-position - 1);
				diffDescription.addLeftChange(fragment.startOffset, fragment.endOffset);
			}
		}
	}

	/**
	 * Converts start/end indexes to start/end lines (start inclusive, end
	 * exclusive). Line indices are starting from 1, indices for units from 0. If
	 * units is empty or the endIndex is 0, the method returns new Pair(1,1).
	 */
	private static Pair<Integer, Integer> convertToFirstLastLine(int startIndex, int endIndex,
			List<TextChunk> units) {
		if (units.isEmpty() || endIndex == 0) {
			return new Pair<>(1, 1);
		}

		int firstLine;
		if (startIndex >= units.size()) {
			firstLine = units.get(startIndex - 1).endLine;
		} else {
			firstLine = units.get(startIndex).startLine;
		}
		int lastLine = Math.max(firstLine, units.get(endIndex - 1).endLine);
		return new Pair<Integer, Integer>(firstLine, lastLine);
	}

	/**
	 * Extracts fragments for the diff region denoted by start/end indexes in
	 * the chunks. Fragments are used for "diff in diff" to highlight the
	 * actually changed parts.
	 * 
	 * @param startIndex
	 *            The zero-based index of the chunk in the given units which
	 *            marks the start of the region
	 * @param endIndex
	 *            The zero-based index of the chunk in the given units which
	 *            marks the end of the region (exclusive)
	 * @param text
	 *            The text to extract the region from
	 * @param units
	 *            The chunks of the text. Used to find out, the substring of the
	 *            region inside the given text
	 * 
	 * @return The region split into chunks of fragments denoted by the
	 *         {@link #FRAGMENT_MATCH_PATTERN}
	 */
	private static List<TextChunk> extractFragments(int startIndex, int endIndex, String text, List<TextChunk> units) {
		if (startIndex >= units.size() || startIndex >= endIndex) {
			return CollectionUtils.emptyList();
		}

		List<TextChunk> fragments = new ArrayList<TextChunk>();
		int startOffset = units.get(startIndex).startOffset;
		int endOffset = Math.min(units.get(endIndex - 1).endOffset, text.length());

		Matcher matcher = FRAGMENT_MATCH_PATTERN.matcher(text.substring(startOffset, endOffset));
		while (matcher.find()) {
			int fragmentStart = startOffset + matcher.start();
			String fragmentText = matcher.group();

			// ignore fragments with new lines, as they are only filler
			// white-space
			if (fragmentText.contains("\n")) {
				continue;
			}

			// lines are not used and thus initialized with dummy values
			fragments.add(new TextChunk(fragmentStart, fragmentStart + fragmentText.length(), 0, 0, fragmentText));
		}

		return fragments;
	}

	/**
	 * Returns the chunks for an element.
	 * 
	 * @param isLeft
	 *            indicates whether this method is called for the left or right
	 *            element. The implementer then may behave differently for both
	 *            elements (possibly depending on additional information).
	 */
	protected abstract List<TextChunk> getChunks(T element, boolean isLeft);

	/** Returns the name for this diff. */
	protected abstract String getDiffName();

}
