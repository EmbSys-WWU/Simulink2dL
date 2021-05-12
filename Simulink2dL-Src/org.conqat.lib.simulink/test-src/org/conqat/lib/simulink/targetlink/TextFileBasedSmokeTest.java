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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Smoke testlet for Targetlink parser. This reads a Targetlink struct
 * definition from a text file and parses it.
 */
@RunWith(Parameterized.class)
public class TextFileBasedSmokeTest extends CCSMTestCaseBase {

	/** Name of file to read. */
	private final String filename;

	/** Create new testlet. */
	public TextFileBasedSmokeTest(String filename) {
		this.filename = filename;
	}

	/** Read Targetlink struct definition from a text file and parse it. */
	@Test
	public void test() throws Exception {
		TargetlinkDataScanner scanner = new TargetlinkDataScanner(
				new InputStreamReader(new FileInputStream(useTestFile(filename)), FileSystemUtils.UTF8_CHARSET));
		TargetlinkDataParser parser = new TargetlinkDataParser(scanner);
		TargetlinkStruct struct = (TargetlinkStruct) parser.parse().value;

		String actual = struct.toString();
		FileSystemUtils.writeFileUTF8(new File(getTmpDirectory(), filename + ".actual"), actual);

		File expectedFile = new File(getTestDataDirectory(), filename + ".expected");
		assertTrue("Expected file " + expectedFile + " does not exist or is not readable!", expectedFile.canRead());
		String expected = FileSystemUtils.readFileUTF8(expectedFile);

		assertEquals(StringUtils.normalizeLineSeparatorsPlatformSpecific(expected),
				StringUtils.normalizeLineSeparatorsPlatformSpecific(actual));
	}

	/** Creates the parameters. */
	@Parameters(name = "{0}")
	public static Collection<Object[]> createParameters() {

		File dir = new File("test-data/" + TextFileBasedSmokeTest.class.getPackage().getName());

		List<File> txtFiles = FileSystemUtils.listFilesRecursively(dir, new FileExtensionFilter("txt"));

		ArrayList<Object[]> suite = new ArrayList<>();
		for (File file : txtFiles) {
			suite.add(new Object[] { file.getName() });
		}
		return suite;
	}
}