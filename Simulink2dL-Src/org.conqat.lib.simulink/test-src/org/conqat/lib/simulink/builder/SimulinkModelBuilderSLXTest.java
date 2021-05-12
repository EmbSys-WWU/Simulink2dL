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
package org.conqat.lib.simulink.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.simulink.model.SimulinkModel;
import org.junit.Test;

/**
 * Performs model building checks for the <b>.slx format</b>. See
 * {@link SimulinkModelBuilderTestBase}.
 */
public class SimulinkModelBuilderSLXTest extends SimulinkModelBuilderTestBase {

	/** Slx file with the default settings in a separate bddefaults.xml file. */
	private static final String SLX_FILE_WITH_BDDEFAULTS_XML = "bddefaults_2018a.slx";

	/** {@inheritDoc} */
	@Override
	protected String resolveModelName(String basename) {
		return basename + "_2013a" + SimulinkModelBuilder.SLX_FILE_EXTENSION;
	}

	/**
	 * Tests the extraction of default settings from the bddefaults.xml file in
	 * newer slx format and that they are added to the model.
	 */
	@Test
	public void testDefaultSettingsExtraction() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel(SLX_FILE_WITH_BDDEFAULTS_XML);
		assertEquals("Helvetica", model.getBlockDefaultParameter("FontName"));
		assertEquals("1", model.getTypeBlockDefaultParameter("Inport", "Port"));
	}
}