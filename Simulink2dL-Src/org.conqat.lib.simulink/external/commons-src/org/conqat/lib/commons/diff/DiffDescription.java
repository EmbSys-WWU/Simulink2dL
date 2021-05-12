/*-----------------------------------------------------------------------+
 | com.teamscale.ui
 |                                                                       |
   $Id$            
 |                                                                       |
 | Copyright (c)  2009-2013 CQSE GmbH                                 |
 +-----------------------------------------------------------------------*/
package org.conqat.lib.commons.diff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableList;

/**
 * A class describing a diff.
 */
public class DiffDescription implements Serializable {

	/** Serial version UID. */
	private static final long serialVersionUID = 1;

	/** The name of this description. */
	private final String name;

	/**
	 * Line changes for the left lines. The integers are organized in pairs,
	 * giving the start (inclusive) and end (exclusive) lines of a region.
	 */
	private final List<Integer> leftChangeLines = new ArrayList<Integer>();

	/**
	 * Line changes for the right lines. The integers are organized in pairs,
	 * giving the start (inclusive) and end (exclusive) lines of a region.
	 */
	private final List<Integer> rightChangeLines = new ArrayList<Integer>();

	/**
	 * Change tokens for the left text. These are used to highlight the exact
	 * change within changed lines. The integers are organized in pairs, giving
	 * the start (inclusive) and end (exclusive) offsets of a region.
	 */
	private final List<Integer> leftChangeRegions = new ArrayList<Integer>();

	/**
	 * Change tokens for the left text. These are used to highlight the exact
	 * change within changed lines. The integers are organized in pairs, giving
	 * the start (inclusive) and end (exclusive) offsets of a region.
	 */
	private final List<Integer> rightChangeRegions = new ArrayList<Integer>();

	/** Constructor. */
	public DiffDescription(String name) {
		this.name = name;
	}

	/** Returns the name. */
	public String getName() {
		return name;
	}

	/**
	 * Adds a line region that is matched between the left and right element.
	 * This denotes a part where these regions differ. First lines are
	 * inclusive, last lines are exclusive. All lines are 1-based. Adding
	 * regions must be performed in ascending order.
	 */
	public void addLineRegion(int leftFirstLine, int leftEndLine, int rightFirstLine, int rightEndLine) {
		addLineRegion(leftFirstLine, leftEndLine, leftChangeLines);
		addLineRegion(rightFirstLine, rightEndLine, rightChangeLines);
	}

	/**
	 * Inserts a line region into either {@link #leftChangeLines} or
	 * {@link #rightChangeLines}.
	 */
	private static void addLineRegion(int firstLine, int endLine, List<Integer> changeLines) {
		CCSMAssert.isTrue(firstLine >= 0, "May only insert positive lines!");
		CCSMAssert.isTrue(firstLine <= endLine, "End must not be before start!");
		CCSMAssert.isTrue(changeLines.isEmpty() || CollectionUtils.getLast(changeLines) - 1 <= firstLine,
				"Must insert in ascending order!");
		changeLines.add(firstLine);
		changeLines.add(endLine);
	}

	/**
	 * Adds a change within a line region (that is highlighted) for the left
	 * side. Start is inclusive, end is exclusive.
	 */
	public void addLeftChange(int startOffset, int endOffset) {
		addChange(startOffset, endOffset, leftChangeRegions);
	}

	/**
	 * Adds a change within a line region (that is highlighted) for the right
	 * side. Start is inclusive, end is exclusive.
	 */
	public void addRightChange(int startOffset, int endOffset) {
		addChange(startOffset, endOffset, rightChangeRegions);
	}

	/**
	 * Adds a change within a line region. Start is inclusive, end is exclusive.
	 */
	private static void addChange(int startOffset, int endOffset, List<Integer> changeTokens) {
		if (!changeTokens.isEmpty() && startOffset <= CollectionUtils.getLast(changeTokens)) {
			changeTokens.set(changeTokens.size() - 1, endOffset);
		} else {
			changeTokens.add(startOffset);
			changeTokens.add(endOffset);
		}
	}
	

	/**
	 * Returns the change regions for the left lines. The integers are organized
	 * in pairs, giving the start (inclusive) and end (exclusive) index of a
	 * region.
	 */
	public UnmodifiableList<Integer> getLeftChangeRegions() {
		return CollectionUtils.asUnmodifiable(leftChangeRegions);
	}

	/**
	 * Returns the change regions for the right lines. The integers are organized
	 * in pairs, giving the start (inclusive) and end (exclusive) index of a
	 * region.
	 */
	public UnmodifiableList<Integer> getRightChangeRegions() {
		return CollectionUtils.asUnmodifiable(rightChangeRegions);
	}
	
	/**
	 * Adds the given list to the leftChangeRegions
	 */
	public void addLeftChangeRegions(UnmodifiableList<Integer> leftChangeRegions) {
		this.leftChangeRegions.addAll(leftChangeRegions);
	}

	/**
	 * Adds the given list to the rightChangeRegions
	 */
	public void addRightChangeRegions(UnmodifiableList<Integer> rightChangeRegions) {
		this.rightChangeRegions.addAll(rightChangeRegions);
	}

	/**
	 * Returns the line changes for the left lines. The integers are organized
	 * in pairs, giving the start (inclusive) and end (exclusive) lines of a
	 * region.
	 */
	public UnmodifiableList<Integer> getLeftChangeLines() {
		return CollectionUtils.asUnmodifiable(leftChangeLines);
	}

	/**
	 * Returns the line changes for the right lines. The integers are organized
	 * in pairs, giving the start (inclusive) and end (exclusive) lines of a
	 * region.
	 */
	public UnmodifiableList<Integer> getRightChangeLines() {
		return CollectionUtils.asUnmodifiable(rightChangeLines);
	}
}
