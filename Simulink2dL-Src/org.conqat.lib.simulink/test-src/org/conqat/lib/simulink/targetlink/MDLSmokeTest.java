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
package org.conqat.lib.simulink.targetlink;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.util.SimulinkUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Smoke testlet for Simulink/Stateflow. This builds a Simulink file and runs
 * the {@link TargetLinkDataResolver} on all blocks.
 */
@RunWith(Parameterized.class)
public class MDLSmokeTest extends CCSMTestCaseBase {

	/** Name of file to read. */
	private final String filename;

	/** Create new testlet. */
	public MDLSmokeTest(String filename) {
		this.filename = filename;
	}

	/**
	 * Builds a Simulink file and run the {@link TargetLinkDataResolver} on all
	 * blocks.
	 */
	@Test
	public void test() throws Exception {
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(useTestFile(filename), new SimpleLogger())) {
			SimulinkModel model = builder.buildModel();
			SimulinkUtils.visitDepthFirst(model, new TargetLinkDataResolver());
		}
	}

	/** Creates the parameters. */
	@Parameters(name = "{0}")
	public static Collection<Object[]> createParameters() {

		File dir = new File("test-data/" + MDLSmokeTest.class.getPackage().getName());

		List<File> mdlFiles = FileSystemUtils.listFilesRecursively(dir, new FileExtensionFilter("mdl"));

		ArrayList<Object[]> suite = new ArrayList<>();
		for (File file : mdlFiles) {
			suite.add(new Object[] { file.getName() });
		}
		return suite;
	}
}