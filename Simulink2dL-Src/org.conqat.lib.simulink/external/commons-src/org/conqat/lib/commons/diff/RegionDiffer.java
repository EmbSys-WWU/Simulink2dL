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

import org.conqat.lib.commons.region.Region;
import org.conqat.lib.commons.string.LineOffsetConverter;

/**
 * Class for calculating diff based on given regions.
 */
public class RegionDiffer extends DifferBase<String> {

	/**
	 * Pattern used to parse region. See {@link #RegionDiffer(String)} for
	 * details.
	 */
	private static final Pattern REGION_PATTERN = Pattern.compile("(\\d+)-(\\d+):(\\d+)-(\\d+)");

	/** The region in the left element. */
	private final Region leftRegion;

	/** The region in the right element. */
	private final Region rightRegion;

	/**
	 * Constructor.
	 * 
	 * @param regionDescription
	 *            description of the regions as a string, formatted as
	 *            "leftStart-leftEnd:rightStart-rightEnd", where all are
	 *            one-based inclusive line numbers.
	 * @throws IllegalArgumentException
	 *             if the regionDescription does not follow the required format.
	 */
	public RegionDiffer(String regionDescription) throws IllegalArgumentException {
		Matcher matcher = REGION_PATTERN.matcher(regionDescription);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid region description: " + regionDescription);
		}

		try {
			leftRegion = new Region(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
			rightRegion = new Region(Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid region description: " + regionDescription);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String getElementText(String element) {
		return element;
	}

	/** {@inheritDoc} */
	@Override
	protected String getDiffName() {
		return "region-based";
	}

	/**
	 * This method return only three chunks. One for the text before the given
	 * region, the given region, and the text after. The comparison text is
	 * chosen such that the diff will be in the given region.
	 */
	@Override
	protected List<TextChunk> getChunks(String elementText, boolean isLeft) {

		Region region = leftRegion;
		if (!isLeft) {
			region = rightRegion;
		}

		LineOffsetConverter converter = new LineOffsetConverter(elementText);

		List<TextChunk> result = new ArrayList<TextChunk>();

		// both pre-regions get the same comparison text, hence are mapped to
		// each other
		result.add(new TextChunk(0, converter.getOffset(region.getStart()), 1, region.getStart(), "pre"));

		// both main regions get different comparison text, hence are detected
		// as change
		result.add(new TextChunk(converter.getOffset(region.getStart()), converter.getOffset(region.getEnd() + 1),
				region.getStart(), region.getEnd() + 1, "content" + isLeft));

		// both post-regions get the same comparison text, hence are mapped to
		// each other
		result.add(new TextChunk(converter.getOffset(region.getEnd() + 1), elementText.length(), region.getEnd() + 1,
				converter.getLine(elementText.length()) + 1, "post"));
		return result;
	}
}
