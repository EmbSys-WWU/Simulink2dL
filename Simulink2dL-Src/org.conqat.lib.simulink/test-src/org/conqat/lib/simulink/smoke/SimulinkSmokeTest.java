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
package org.conqat.lib.simulink.smoke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.error.NeverThrownRuntimeException;
import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.conqat.lib.commons.test.DeepCloneTestUtils;
import org.conqat.lib.commons.visitor.IVisitor;
import org.conqat.lib.commons.visitor.VisitorUtils;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.constraints.SimulinkConstraints;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.util.SimulinkIdProvider;
import org.conqat.lib.simulink.util.SimulinkModelWalker;
import org.conqat.lib.simulink.util.SimulinkUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Smoke testlet for Simulink/Stateflow. This parses a MDL file and checks if
 * deep cloning is implemented correctly.
 */
@RunWith(Parameterized.class)
public class SimulinkSmokeTest extends CCSMTestCaseBase {

	/** Name of file to read. */
	private final File mdlFile;

	/** Create new testlet. */
	public SimulinkSmokeTest(File mdlFile) {
		this.mdlFile = mdlFile;
	}

	/**
	 * Parse MDL file and check if deep cloning is implemented correctly.
	 */
	@Test
	public void test() throws Exception {
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(mdlFile, new SimpleLogger(), null)) {
			SimulinkModel model = builder.buildModel();
			SimulinkConstraints.checkAllConstraints(model);
			SimulinkModel clone = model.deepClone();
			checkModelWalker(model);
			checkCloning(model, clone);
			checkModelDump(model);
		}
	}

	/**
	 * Performs a regression test that ensures that the dump of the model is
	 * unchanged.
	 */
	private void checkModelDump(SimulinkModel model) throws IOException {
		String actual = buildModelDump(model);
		FileSystemUtils.writeFileUTF8(new File(getTmpDirectory(), mdlFile.getName() + ".actual"), actual);

		File expectedFile = new File(mdlFile.getParentFile(), mdlFile.getName() + ".dump");
		assertTrue("Dump file " + expectedFile + " does not exist or is not readable!", expectedFile.canRead());
		String expected = FileSystemUtils.readFileUTF8(expectedFile);

		assertEquals(StringUtils.normalizeLineSeparatorsPlatformSpecific(expected),
				StringUtils.normalizeLineSeparatorsPlatformSpecific(actual));
	}

	/** Builds a stable string representation of the model. */
	private static String buildModelDump(SimulinkModel model) {
		StringBuilder actualBuilder = new StringBuilder();
		List<SimulinkBlock> blocks = SimulinkUtils.sortById(SimulinkUtils.listBlocksDepthFirst(model));
		for (SimulinkBlock block : blocks) {
			actualBuilder.append(block.getId() + StringUtils.LINE_SEPARATOR);
			for (String parameter : CollectionUtils.sort(block.getDeclaredParameterNames())) {
				actualBuilder.append("  " + parameter + " = " + block.getDeclaredParameter(parameter) + StringUtils.LINE_SEPARATOR);
			}
		}

		return actualBuilder.toString();
	}

	/** Test for {@link SimulinkModelWalker} */
	private void checkModelWalker(SimulinkModel model) {
		ModelElementsCollector collector = new ModelElementsCollector();

		VisitorUtils.visitAllDepthFirst(model, new SimulinkModelWalker(), collector);

		IdentityHashSet<Object> elements = DeepCloneTestUtils.getAllReferencedObjects(model,
				SimulinkModel.class.getPackage().getName());

		assertEquals(elements, collector.elements);
	}

	/** Checks if deep cloning is implemented correctly. */
	private static int checkCloning(SimulinkModel orig, SimulinkModel clone) {
		IdentityHashMap<Object, Object> map = DeepCloneTestUtils.testDeepCloning(orig, clone, new SimulinkIdProvider(),
				"org.conqat.lib.simulink");

		for (Object origObject : map.keySet()) {
			Object cloneObject = map.get(origObject);

			checkParameters(origObject, cloneObject);
		}

		return map.size();
	}

	/**
	 * If <code>origObject</code> is a {@link ParameterizedElement} this checks
	 * if parameters are cloned correctly.
	 */
	private static void checkParameters(Object origObject, Object cloneObject) {
		if (!(origObject instanceof ParameterizedElement)) {
			return;
		}

		ParameterizedElement orig = (ParameterizedElement) origObject;
		ParameterizedElement clone = (ParameterizedElement) cloneObject;

		assertEquals("Orig: " + orig + " (" + orig.getClass() + "), Clone: " + clone + " (" + clone.getClass() + ")",
				CollectionUtils.sort(orig.getParameterNames()), CollectionUtils.sort(clone.getParameterNames()));

		for (String name : orig.getParameterNames()) {
			assertEquals(orig.getParameter(name), clone.getParameter(name));
		}

	}

	/** Simple visitor that collects all model elements. */
	private class ModelElementsCollector implements IVisitor<Object, NeverThrownRuntimeException> {

		/** Collected elements. */
		private final IdentityHashSet<Object> elements = new IdentityHashSet<>();

		/** Add object to collection */
		@Override
		public void visit(Object element) {
			elements.add(element);
		}

	}

	/** Create a smoke test suite. */
	@Parameters(name = "{0}")
	public static Collection<Object[]> createSuite() throws UnsupportedEncodingException, IOException {

		File dir = new File("test-data/" + SimulinkSmokeTest.class.getPackage().getName());

		List<File> files = listMdlAndSlxFiles(dir);

		ArrayList<Object[]> suite = new ArrayList<>();
		for (File file : files) {
			suite.add(new Object[] { file });
		}

		return suite;
	}

	/**
	 * List MDL and SLX files from test-data. Additionally, load files from
	 * paths in extra file.
	 */
	public static List<File> listMdlAndSlxFiles(File dir) throws UnsupportedEncodingException, IOException {
		FileExtensionFilter filter = new FileExtensionFilter("mdl", "slx");
		List<File> files = FileSystemUtils.listFilesRecursively(dir, filter);

		File additionalPathsFile = new File(dir, "additional-paths.txt");
		if (additionalPathsFile.canRead()) {
			for (String line : FileSystemUtils.readLinesUTF8(additionalPathsFile)) {
				if (StringUtils.isEmpty(line) || line.startsWith("#")) {
					continue;
				}

				files.addAll(FileSystemUtils.listFilesRecursively(new File(line), filter));
			}
		}
		return files;
	}

}