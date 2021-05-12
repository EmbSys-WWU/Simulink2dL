/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Default implementation of {@link IRegexReplacement}.
 */
public class RegexReplacement implements IRegexReplacement {

	/** The pattern. */
	private final Pattern pattern;

	/** The replacement. */
	private final String replacement;

	/**
	 * Create a new regex replacement. Syntax for patterns, flags and
	 * replacements is specified in the API documentation of
	 * {@link java.util.regex.Pattern} and {@link java.util.regex.Matcher}.
	 * 
	 * @throws PatternSyntaxException
	 *             if the pattern has a syntax error
	 */
	public RegexReplacement(String regex, String replacement, int flags) throws PatternSyntaxException {
		pattern = Pattern.compile(regex, flags);
		this.replacement = replacement;
	}

	/**
	 * Create a new regex replacement. Syntax for patterns and replacements is
	 * specified in the API documentation of {@link java.util.regex.Pattern} and
	 * {@link java.util.regex.Matcher}.
	 * 
	 * @throws PatternSyntaxException
	 *             if the pattern has a syntax error
	 */
	public RegexReplacement(String regex, String replacement) throws PatternSyntaxException {
		this(regex, replacement, 0);
	}

	/**
	 * Create a new regex replacement that does not replace the pattern matches
	 * by another string but deletes them. Syntax for patterns and flags is
	 * specified in the API documentation of {@link java.util.regex.Pattern}.
	 * 
	 * @throws PatternSyntaxException
	 *             if the pattern has a syntax error
	 */
	public RegexReplacement(String regex, int flags) throws PatternSyntaxException {
		this(regex, StringUtils.EMPTY_STRING, flags);
	}

	/**
	 * Create a new regex replacement that does not replace the pattern matches
	 * by another string but deletes them. Syntax for patterns is specified in
	 * the API documentation of {@link java.util.regex.Pattern}.
	 * 
	 * @throws PatternSyntaxException
	 *             if the pattern has a syntax error
	 */
	public RegexReplacement(String regex) throws PatternSyntaxException {
		this(regex, 0);
	}

	/** {@inheritDoc} */
	@Override
	public Pattern getPattern() {
		return pattern;
	}

	/** {@inheritDoc} */
	@Override
	public String getReplacement() {
		return replacement;
	}

}