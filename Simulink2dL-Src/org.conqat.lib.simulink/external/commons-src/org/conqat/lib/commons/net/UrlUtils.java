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
package org.conqat.lib.commons.net;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utils for URL handling.
 */
public class UrlUtils {
	/** "file://" protocol prefix in URLs */
	public static final String FILE_PROTOCOL = "file://";

	/**
	 * Converts the given url to a {@link URI}. Has special handling for "file://"
	 * paths since {@link URI#URI(String)} does not handle windows paths correctly.
	 */
	public static URI convertUriFromUrl(String url) throws URISyntaxException {
		if (url.startsWith(FILE_PROTOCOL)) {
			return new File(url.substring(FILE_PROTOCOL.length())).toURI();
		}
		return new URI(url);
	}
}
