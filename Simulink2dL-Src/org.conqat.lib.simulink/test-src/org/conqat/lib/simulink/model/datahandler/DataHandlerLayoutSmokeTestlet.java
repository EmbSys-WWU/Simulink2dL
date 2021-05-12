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
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipException;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.smoke.SimulinkSmokeTest;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;
import org.conqat.lib.simulink.util.SimulinkUtils;
import org.conqat.lib.simulink.util.StateflowChartRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Smoke Test for data handler layouts.
 */
@RunWith(Parameterized.class)
public class DataHandlerLayoutSmokeTestlet extends CCSMTestCaseBase {

	/** Name of the fixed font used for rendering. */
	private static final String FIXED_FONT_NAME = "font/DejaVuSansCondensed.ttf.gz";

	/** Name of file to read. */
	private final File simulinkFile;

	/** Name of the subsystem to layout. May be null to indicate top-level. */
	private final String subsystemName;

	/** Just for suite generation. */
	private DataHandlerLayoutSmokeTestlet() {
		simulinkFile = null;
		subsystemName = null;
	}

	/** Create new testlet. */
	public DataHandlerLayoutSmokeTestlet(File simulinkFile, String subsystemName,
			@SuppressWarnings("unused") String testName) {
		this.simulinkFile = simulinkFile;
		this.subsystemName = subsystemName;
	}

	/** Executes the test. */
	@Test
	public void test() throws Exception {
		try (InputStream in = FileSystemUtils.autoDecompressStream(new FileInputStream(useTestFile(FIXED_FONT_NAME)))) {
			FontData.overrideFont = Font.createFont(Font.TRUETYPE_FONT, in);
		}

		SimulinkBlock block;
		try (SimulinkModelBuilder simulinkModelBuilder = new SimulinkModelBuilder(simulinkFile, new SimpleLogger())) {
			block = simulinkModelBuilder.buildModel(new ModelBuildingParameters().setPreserveUnconnectedLines(true));
		}

		if (subsystemName != null) {
			block = block.getSubBlock(subsystemName);
		}

		StateflowChart chart = findStateFlowChart(block);
		BufferedImage actualImage;
		if (chart != null) {
			actualImage = StateflowChartRenderer.renderChart(chart);
		} else {
			actualImage = SimulinkBlockRenderer.renderBlock(block);
		}

		FileSystemUtils.ensureDirectoryExists(getTmpDirectory());
		ImageIO.write(actualImage, "PNG", new File(getTmpDirectory(), getActualFilename()));

		String baseName = StringUtils.stripSuffix(simulinkFile.getAbsolutePath(),
				SimulinkModelBuilder.MDL_FILE_EXTENSION);
		baseName = StringUtils.stripSuffix(baseName, SimulinkModelBuilder.SLX_FILE_EXTENSION);
		File expectedFile = getExpectedFile(baseName);
		assertTrue("Expected image file " + expectedFile + " missing or not readable!", expectedFile.canRead());
		BufferedImage expectedImage = ImageIO.read(expectedFile);

		assertImagesEqual(expectedImage, actualImage);
	}

	/** Returns the first stateflow chart child. */
	private static StateflowChart findStateFlowChart(SimulinkBlock block) {
		for (SimulinkBlock subBlock : block.getSubBlocks()) {
			if (SimulinkUtils.isStateflowChart(subBlock)) {
				return block.getModel().getStateflowMachine().getChart(subBlock.getId());
			}
		}

		return null;
	}

	/** Returns the name of the expected image file. */
	private File getExpectedFile(String baseName) {
		return new File(baseName + getNormalizedSubsystemName(subsystemName) + "_expected.png");
	}

	/** Returns the name of the actual image file. */
	private String getActualFilename() {
		return simulinkFile.getName() + getNormalizedSubsystemName(subsystemName) + ".png";
	}

	/** Returns the normalized subsystem name. */
	private static String getNormalizedSubsystemName(String subsystemName) {
		if (subsystemName == null) {
			return StringUtils.EMPTY_STRING;
		}
		return "_" + subsystemName.replace(StringUtils.SPACE, "empty").toLowerCase();
	}

	/** Asserts that two images are equal. */
	private static void assertImagesEqual(BufferedImage expectedImage, BufferedImage actualImage) {
		assertEquals(expectedImage.getWidth(), actualImage.getWidth());
		assertEquals(expectedImage.getHeight(), actualImage.getHeight());
		for (int y = 0; y < expectedImage.getHeight(); ++y) {
			for (int x = 0; x < expectedImage.getWidth(); ++x) {
				assertEquals(expectedImage.getRGB(x, y), actualImage.getRGB(x, y));
			}
		}
	}

	/** Create a smoke test suite. */
	@Parameters(name = "{2}")
	public static Collection<Object[]> suite() throws ZipException, IOException, SimulinkModelBuildingException {

		File dir = new DataHandlerLayoutSmokeTestlet().getTestDataDirectory();
		List<File> files = SimulinkSmokeTest.listMdlAndSlxFiles(dir);

		ArrayList<Object[]> suite = new ArrayList<>();
		for (File file : files) {
			String withoutExtension = StringUtils.removeLastPart(file.getName(), '.');
			if (withoutExtension.endsWith(".multi")) {
				addMultiTest(suite, file);
			} else {
				suite.add(new Object[] { file, null, file.getName() + getNormalizedSubsystemName(null) });
			}
		}
		return suite;
	}

	/**
	 * Adds a multi test, i.e. each subsystem is treated as a separate test
	 * case.
	 */
	private static void addMultiTest(ArrayList<Object[]> suite, File file)
			throws SimulinkModelBuildingException, ZipException, IOException {
		try (SimulinkModelBuilder simulinkModelBuilder = new SimulinkModelBuilder(file, new SimpleLogger())) {
			SimulinkModel model = simulinkModelBuilder
					.buildModel(new ModelBuildingParameters().setPreserveUnconnectedLines(true));
			for (SimulinkBlock block : model.getSubBlocks()) {
				if (SimulinkConstants.SUBSYSTEM.equals(block.getType())) {
					suite.add(new Object[] { file, block.getName(),
							file.getName() + getNormalizedSubsystemName(block.getName()) });
				}
			}
		}
	}
}
