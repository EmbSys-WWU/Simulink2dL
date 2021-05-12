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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.zip.ZipException;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/**
 * Specific tests for the {@link ModelDataHandler} and subclasses.
 */
public class ModelDataHandlerTest extends SimulinkTestBase {

	/**
	 * Tests reading of HTML annotations between MDL and SLX to ensure same
	 * return values.
	 */
	@Test
	public void testHtmlAnnotations() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel mdlModel = loadModel("annotations.mdl");
		SimulinkModel slxModel = loadModel("annotations.slx");

		SimulinkAnnotation mdlHtmlAnnotation = findHtmlAnnotation(mdlModel);
		SimulinkAnnotation slxHtmlAnnotation = findHtmlAnnotation(slxModel);

		assertNotNull(mdlHtmlAnnotation);
		assertNotNull(slxHtmlAnnotation);

		assertEquals(StringUtils.normalizeLineSeparatorsPlatformSpecific(mdlHtmlAnnotation.obtainLabelData().getText()),
				StringUtils.normalizeLineSeparatorsPlatformSpecific(slxHtmlAnnotation.obtainLabelData().getText()));
	}

	/** Returns the first HTML annotation or null. */
	private static SimulinkAnnotation findHtmlAnnotation(SimulinkModel model) {
		for (SimulinkAnnotation annotation : model.getAnnotations()) {
			LabelLayoutData labelData = annotation.obtainLabelData();
			if (labelData != null && labelData.getText().contains("DOCTYPE")) {
				return annotation;
			}
		}
		return null;
	}

}
