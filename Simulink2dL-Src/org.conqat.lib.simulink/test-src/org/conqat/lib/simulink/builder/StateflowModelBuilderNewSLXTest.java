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

/**
 * New slx files store the stateflow machine information in a separate xml file
 * archived in the slx file. This test tests whether the machine is build
 * correctly from the xml file.
 */
public class StateflowModelBuilderNewSLXTest extends StateflowModelBuilderSLXTestBase {

	/** {@inheritDoc} */
	@Override
	protected String getTestModelName() {
		return "model02_2015b.slx";
	}
}
