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
package org.conqat.lib.simulink.builder;

import org.conqat.lib.simulink.model.SimulinkConstants;

/**
 * SAX handler for building {@link MutableMDLSection} from an .slx file.
 */
public class SLXModelHandler extends SLXDefaultHandlerBase {

	/** Constructor. */
	public SLXModelHandler() {
		this.rootSectionName = SimulinkConstants.SECTION_MODEL_INFORMATION;
	}

	/** Constructor. */
	public SLXModelHandler(String rootSectionName) {
		this.rootSectionName = rootSectionName;
	}

	/**
	 * Returns the root of the model (called "model information" in the XML).
	 */
	public MutableMDLSection getRootModelSection() {
		return rootSection;
	}
}
