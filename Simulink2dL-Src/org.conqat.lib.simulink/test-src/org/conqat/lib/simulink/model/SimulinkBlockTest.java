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
package org.conqat.lib.simulink.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.conqat.lib.simulink.util.SimulinkUtils;
import org.junit.Test;

/**
 * Tests the {@link SimulinkBlock}.
 */
public class SimulinkBlockTest extends SimulinkTestBase {

	/** Tests lookup of referenced blocks. */
	@Test
	public void testReferenceLookup() throws ZipException, SimulinkModelBuildingException, IOException {
		loadModelAndCheckReferences(false,
				"child_model.slx: child_model -> not found\n" + "child_model2.slx: child_model2 -> not found\n"
						+ "sub_library: sub_library/Subsystem -> not found\n"
						+ "sub_library: sub_library/Subsystem -> not found\n"
						+ "test_library: test_library/coolerSum -> not found\n"
						+ "test_library: test_library/crazy -> not found\n"
						+ "test_library: test_library/crazy/gain -> not found\n"
						+ "test_library: test_library/timesTen -> not found\n"
						+ "test_library: test_library/timesTen -> not found");

		loadModelAndCheckReferences(true,
				"child_model.slx: child_model -> child_model.slx\n" + "child_model2.slx: child_model2 -> not found\n"
						+ "sub_library: sub_library/Subsystem -> sub_library.mdl\n"
						+ "sub_library: sub_library/Subsystem -> sub_library.mdl\n"
						+ "test_library: test_library/coolerSum -> test_library.slx\n"
						+ "test_library: test_library/crazy -> test_library.slx\n"
						+ "test_library: test_library/crazy/gain -> test_library.slx\n"
						+ "test_library: test_library/timesTen -> test_library.slx\n"
						+ "test_library: test_library/timesTen -> test_library.slx");
	}

	/** Loads the test models and checks the references. */
	private void loadModelAndCheckReferences(boolean includeSub, String expected)
			throws SimulinkModelBuildingException, ZipException, IOException {
		ModelBuildingParameters parameters = new ModelBuildingParameters();
		if (includeSub) {
			parameters.addReferencePath(useTestFile("sub"));
		}

		SimulinkModel model = loadModel("parent_model.mdl", parameters);

		List<String> foundReferences = new ArrayList<>();
		for (SimulinkBlock block : SimulinkUtils.listBlocksDepthFirst(model)) {
			ReferencedBlockInfo referenceInfo = block.getReferencedBlockInfo();
			if (referenceInfo == null) {
				continue;
			}
			String fileName = "not found";
			if (referenceInfo.isModelFileFound()) {
				fileName = referenceInfo.getModelFile().getName();
			}
			foundReferences.add(referenceInfo.getModelName() + ": " + referenceInfo.getBlockName() + " -> " + fileName);
		}

		assertEquals(expected, StringUtils.concat(CollectionUtils.sort(foundReferences), "\n"));
	}

	/** Tests the {@link SimulinkModel#isLibrary()} method. */
	@Test
	public void testIsLibrary() throws Exception {
		assertEquals(false, loadModel("libs/lib_user.mdl").isLibrary());
		assertEquals(false, loadModel("libs/lib_user.slx").isLibrary());
		assertEquals(true, loadModel("libs/simple_library.mdl").isLibrary());
		assertEquals(true, loadModel("libs/simple_library.slx").isLibrary());
	}

	/** Tests block type resolution. */
	@Test
	public void testResolvedBlockType() throws Exception {
		SimulinkBlock block1 = CollectionUtils.getAny(loadModel("manual_switch_8.mdl").getSubBlocks());
		assertEquals("ManualSwitch", block1.getResolvedType());

		SimulinkBlock block2 = CollectionUtils.getAny(loadModel("manual_switch_7.mdl").getSubBlocks());
		assertEquals("ManualSwitch", block2.getResolvedType());

		SimulinkBlock block3 = CollectionUtils.getAny(loadModel("chirpSignal.mdl").getSubBlocks());
		assertEquals("ChirpSignal", block3.getResolvedType());
	}

	/** Tests the resolved Id. */
	@Test
	public void testResolvedId() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("libs/simple_library.mdl",
				new ModelBuildingParameters().setParentBlockId("some/other/block"));

		SimulinkBlock block = model.getBlock("simple_library/Reusable2");
		assertNotNull(block);
		assertEquals("simple_library/Reusable2", block.getId());
		assertEquals("some/other/block/Reusable2", block.getResolvedId());

		block = model.getBlock("simple_library/Reusable2/In1");
		assertNotNull(block);
		assertEquals("simple_library/Reusable2/In1", block.getId());
		assertEquals("some/other/block/Reusable2/In1", block.getResolvedId());
	}

	/** Tests the resolved Id. */
	@Test
	public void testResolvedIdWithRename() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("libs/simple_library.mdl",
				new ModelBuildingParameters().setParentBlockId("some/other/ref-block", "simple_library/Reusable2"));

		SimulinkBlock block = model.getBlock("simple_library/Reusable2");
		assertNotNull(block);
		assertEquals("simple_library/Reusable2", block.getId());
		assertEquals("some/other/ref-block", block.getResolvedId());

		block = model.getBlock("simple_library/Reusable2/In1");
		assertNotNull(block);
		assertEquals("simple_library/Reusable2/In1", block.getId());
		assertEquals("some/other/ref-block/In1", block.getResolvedId());
	}

	/** Tests the model name. */
	@Test
	public void testModelName() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("libs/simple_library.mdl");
		assertEquals("simple_library", model.getId());

		model = loadModel("libs/simple_library.slx");
		assertEquals("simple_library", model.getId());
	}
}
