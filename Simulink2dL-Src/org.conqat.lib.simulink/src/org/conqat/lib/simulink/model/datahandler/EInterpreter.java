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

import org.conqat.lib.commons.enums.EnumUtils;
import org.conqat.lib.commons.string.StringUtils;

/**
 * Possible interpreters supported by simulink annotations.
 */
public enum EInterpreter {

	/** No special interpreter (default). */
	NONE,

	/** TeX/LaTeX markup. */
	TEX,

	/** Rich text/HTML markup. */
	RICH;

	/**
	 * Parses a string value and returns the matching enum literal. If nothing
	 * matches, {@link #NONE} is returned.
	 */
	public static EInterpreter getFromValue(String value) {
		if (StringUtils.isEmpty(value)) {
			return NONE;
		}

		EInterpreter result = EnumUtils.valueOfIgnoreCase(EInterpreter.class, value);
		if (result != null) {
			return result;
		}

		return NONE;
	}
}
