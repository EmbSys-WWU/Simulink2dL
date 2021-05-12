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
package org.conqat.lib.commons.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.conqat.lib.commons.filesystem.CanonicalFile;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for test cases that access test data files. This class provides a
 * simple mechanism for accessing test data files in a specified directory and
 * provides statistics on test file usage and non-usage.
 * <p>
 * The test files a test case accesses must reside in the following location:
 * 
 * <pre>
 *        test-data/&lt;Name of the package the test case resides in&gt;
 * </pre>
 * 
 * For example if a test case is defined in package <code>demo.test</code> the
 * test files it accesses must be located in directory
 * <code>test-data/demo.test</code>.
 */
public abstract class CCSMTestCaseBase {

	/** Tmp directory. */
	private final static File TEST_TMP_ROOT_DIRECTORY = new File("test-tmp");

	/** Test data manager for this test case. */
	private final TestDataManager testDataManager = TestDataManager.getInstance(getClass());

	/** Tmp directory. */
	private final File tmpDirectory = new File(TEST_TMP_ROOT_DIRECTORY, getClass().getPackage().getName());

	/**
	 * The key for the "user working directory" system property. (usually it's just
	 * called "working directory").
	 * 
	 * Not 'protected' by design, since changing the working directory in tests is
	 * an exceptional case.
	 */
	private static final String USER_WORKING_DIR_PROPERTY_KEY = "user.dir";

	/** The working-directory path before the test starts. */
	private String workingDirBeforeTest;

	/** Default constructor */
	public CCSMTestCaseBase() {
	}

	/**
	 * Store the working directory, so we can check for modification after the test.
	 */
	@Before
	public void cacheWorkingDir() throws Exception {
		workingDirBeforeTest = System.getProperty(USER_WORKING_DIR_PROPERTY_KEY);
	}

	/**
	 * Assert that the test does not modify the working directory, or at least
	 * resets it. Modifying the working directory would affect following tests.
	 */
	@After
	public void checkWorkingDirUnmodified() throws Exception {
		if (workingDirBeforeTest != null) {
			assertTrue("Working directory was changed and not reset during tests.",
					System.getProperty(USER_WORKING_DIR_PROPERTY_KEY).equals(workingDirBeforeTest));
		}
	}

	/** Get a test file. (Useful for JUnit 4 test cases.) */
	public static File useTestFile(Class<?> cls, String fileName) {
		return TestDataManager.getInstance(cls).getTestFile(fileName);
	}

	/** Get a test file from a specific bundle. */
	public static File useTestFile(String bundleName, Class<?> cls, String fileName) {
		return TestDataManager.getInstance(bundleName, cls).getTestFile(fileName);
	}

	/** Returns the tmp-directory for the given test class. */
	public static File getTmpDirectory(Class<?> cls) {
		return TestDataManager.getInstance(cls).getTmpDirectory();
	}

	/**
	 * Use test file. This method does not actually access the file, so no IO
	 * exception can be raised. This method uses a {@link TestDataManager} to log
	 * access to test data files.
	 * 
	 * @param filename
	 *            Name of the file
	 * @return the file.
	 */
	protected File useTestFile(String filename) {
		return testDataManager.getTestFile(filename, getClass());
	}

	/** Returns the test data directory for this test case. */
	protected File getTestDataDirectory() {
		return useTestFile(".");
	}

	/**
	 * Create a temporary file in a subdirectory of the test temp directory.
	 * Directories are created as needed.
	 * 
	 * @param filename
	 *            name of the file
	 * @param content
	 *            content
	 * @return the file
	 * @throws IOException
	 *             if an IO exception occurrs
	 */
	protected synchronized File createTmpFile(String filename, String content) throws IOException {
		File file = new File(tmpDirectory, filename);
		FileSystemUtils.writeFile(file, content);
		return file;
	}

	/** Get temporary directory. */
	protected File getTmpDirectory() {
		return tmpDirectory;
	}

	/** Delete temporary directory. */
	protected synchronized void deleteTmpDirectory() {
		if (tmpDirectory.isDirectory()) {
			FileSystemUtils.deleteRecursively(tmpDirectory);
		}
	}

	/**
	 * Same as {@link #useTestFile(String)} but returns a {@link CanonicalFile}. If
	 * canonization fails, this makes the current test fail.
	 */
	protected CanonicalFile useCanonicalTestFile(String filename) {
		try {
			CanonicalFile testFile = new CanonicalFile(useTestFile(filename));
			if (!testFile.exists()) {
				throw new IOException("Test file does not exist: " + filename);
			}
			return testFile;
		} catch (IOException e) {
			fail("Problem canonizing file: " + filename + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Same as {@link #createTmpFile(String, String)} but returns a
	 * {@link CanonicalFile}. If canonization fails, this makes the current test
	 * fail.
	 */
	protected CanonicalFile createCanonicalTmpFile(String filename, String content) throws IOException {
		return canonize(createTmpFile(filename, content));
	}

	/**
	 * Canonize file. If canonization fails, this makes the current test fail.
	 */
	protected CanonicalFile canonize(File file) {
		try {
			return new CanonicalFile(file);
		} catch (IOException e) {
			fail("Problem canonizing file: " + file + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Asserts that the given collections equal, except for their order. Since we
	 * use a {@link HashSet} for this purpose, the items inside the collection must
	 * properly implement {@link #hashCode()} and {@link #equals(Object)}.
	 */
	public static <T> void assertEqualsExceptOrder(String message, Collection<T> expected, Collection<T> actual) {
		assertEquals(message, new HashSet<>(expected), new HashSet<>(actual));
	}

	/** Asserts that the delegate throws an exception of the given type. */
	public static <T extends Throwable> void assertThrows(Class<T> exceptionClass, IExceptionAction action) {
		try {
			action.perform();
			fail("Expected expection of type '" + exceptionClass.getName() + "'.");
		} catch (Throwable e) {
			assertTrue("Expected expection of type '" + exceptionClass.getName() + "', but caught '"
					+ e.getClass().getName() + "'.", exceptionClass.isInstance(e));
		}
	}

	/**
	 * Returns the resolved server name in a build environment and "localhost"
	 * otherwise.
	 */
	public static String resolveDockerHostname(String serverName) {
		// CI environment is determined by the CI variable (set by, e.g. gitlab)
		if (System.getenv("CI") != null) {
			String runnerTags = System.getenv("CI_RUNNER_TAGS");

			// Additionally, when using kubernetes, the logical name is the localhost IP
			// address
			if (runnerTags != null && runnerTags.toLowerCase().contains("k8s")) {
				return "127.0.0.1";
			}

			return serverName;
		}
		return "localhost";
	}

	/** Describes a delegate method that is able to throw an exception. */
	@FunctionalInterface
	public static interface IExceptionAction {
		/** Performs the action. */
		public void perform() throws Exception;
	}
}