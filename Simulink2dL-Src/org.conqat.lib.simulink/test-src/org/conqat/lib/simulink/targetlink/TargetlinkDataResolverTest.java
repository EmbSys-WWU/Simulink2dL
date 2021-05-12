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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;
import java.util.Map;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

import java_cup.runtime.Symbol;

/**
 * Test for Targetlink support facility.
 * <p>
 * The test specified here are far from complete but meant as basis to
 * conveniently add more tests.
 */
public class TargetlinkDataResolverTest extends SimulinkTestBase {

	/** Test parser. */
	@Test
	public void testParser() throws Exception {
		SimulinkModel model = loadModel("tl_blocklib.mdl");
		SimulinkBlock block = model.getBlock("tl_blocklib/Abs");
		String data = block.getParameter("data");

		TargetlinkDataScanner scanner = new TargetlinkDataScanner(new StringReader(data));
		TargetlinkDataParser parser = new TargetlinkDataParser(scanner);
		Symbol sym = parser.parse();
		TargetlinkStruct struct = (TargetlinkStruct) sym.value;

		Map<String, String> values = struct.getParameters();
		assertEquals(32, values.size());
	}

	/** Tests resolution of "old style" Targetlink data. */
	@Test
	public void testOldStyleDataResolution() throws Exception {
		SimulinkModel model = loadModel("tl_blocklib.mdl");
		SimulinkBlock block = model.getBlock("tl_blocklib/Abs");
		assertNull(block.getParameter("data/comment/placement"));

		new TargetLinkDataResolver().visit(block);
		assertEquals("1", block.getParameter("data/comment/placement"));
	}

	/** Tests resolution of "new style" Targetlink data. */
	@Test
	public void testNewStyleDataResolution() throws Exception {
		SimulinkModel model = loadModel("tl_newstyle_data.mdl");
		SimulinkBlock block = model.getBlock("new_tl_style/Abs");
		assertNull(block.getParameter("data/output/variable"));

		new TargetLinkDataResolver().visit(block);
		assertEquals("test", block.getParameter("data/output/variable"));
	}
}