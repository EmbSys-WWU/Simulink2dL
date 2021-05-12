/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2018 The ConQAT Project                               |
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
package org.conqat.lib.simulink.model.datahandler;

import java.io.IOException;
import java.util.zip.ZipException;

import org.assertj.core.api.Assertions;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.junit.Test;

/** Tests the {@link GotoFromResolver}. */
public class GotoFromResolverTest extends SimulinkTestBase {

	/** Tests the resolver using a very simple model. */
	@Test
	public void testSimpleModel() throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel model = loadModel("SmallExampleGOTOFROM.slx");
		GotoFromResolver gotoFromResolver = model.getGotoFromResolver();

		SimulinkBlock gotoBlock = model.getSubBlock("Goto");
		SimulinkBlock fromBlock = model.getSubBlock("From");

		Assertions.assertThat(gotoFromResolver.extractGotoTag(gotoBlock)).isEqualTo("s1");
		Assertions.assertThat(gotoFromResolver.extractGotoTag(fromBlock)).isEqualTo("s1");

		Assertions.assertThat(gotoFromResolver.getConnectedFromBlocks(gotoBlock)).containsExactly(fromBlock);
		Assertions.assertThat(gotoFromResolver.getConnectedGotoBlocks(fromBlock)).containsExactly(gotoBlock);
	}
}
