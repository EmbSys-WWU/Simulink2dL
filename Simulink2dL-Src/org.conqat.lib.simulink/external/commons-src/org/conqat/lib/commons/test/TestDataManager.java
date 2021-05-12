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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.conqat.lib.commons.collections.SetMap;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;

/**
 * Support class for identifying unused test data files. This class provides a
 * method to access test data files and logs which test cases access which test
 * files. On every access to a file, access statistic for used and unused files
 * are written to {@value #REPORT_DIRECTORY_NAME}.
 */
public class TestDataManager {

	/** Test data directory. */
	private final static File TEST_DATA_ROOT_DIRECTORY = new File("test-data");

	/** Name of the directory to write reports to. */
	public static final String REPORT_DIRECTORY_NAME = "test-tmp";

	/** Map of all instances (which is indexed by managed directory). */
	private static Map<File, TestDataManager> instances = new HashMap<>();

	/** Returns the instance of the test data manager for the given class. */
	public static TestDataManager getInstance(String bundleName, Class<?> clazz) {
		File directory = new File("../" + bundleName + "/" + TEST_DATA_ROOT_DIRECTORY, clazz.getPackage().getName());
		return getInstance(clazz, directory);
	}

	/** Returns the instance of the test data manager for the given class. */
	public static TestDataManager getInstance(Class<?> clazz) {
		File directory = new File(TEST_DATA_ROOT_DIRECTORY, clazz.getPackage().getName());
		return getInstance(clazz, directory);
	}

	private static TestDataManager getInstance(Class<?> clazz, File directory) {
		if (!instances.containsKey(directory)) {
			instances.put(directory, new TestDataManager(directory, clazz));
		}
		return instances.get(directory);
	}

	/** The set of unused files. */
	private final HashSet<String> unusedFiles = new HashSet<>();

	/** Storage for all test files used so far. */
	private final SetMap<Class<?>, String> usedFiles = new SetMap<>();

	/** The directory this manager works in. */
	private final File directory;

	/** The class this manager was instantiated for. */
	private Class<?> testCaseClass;

	/** Private constructor. */
	private TestDataManager(File directory, Class<?> testClass) {
		this.directory = directory;
		this.testCaseClass = testClass;

		if (!directory.exists() || !directory.isDirectory()) {
			return;
		}

		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				unusedFiles.add(file.getName());
			}
		}
	}

	/** Returns a file pointing to the tmp directory. */
	public File getTmpDirectory() {
		return new File(REPORT_DIRECTORY_NAME, getClass().getPackage().getName());
	}

	/**
	 * Marks the given file as used and returns the complete file (with directory).
	 */
	public File getTestFile(String fileName) {
		return getTestFile(fileName, testCaseClass);
	}

	/**
	 * Marks the given file as used and returns the complete file (with directory).
	 */
	public File getTestFile(String fileName, Class<?> testCaseClass) {
		usedFiles.add(testCaseClass, fileName);
		unusedFiles.remove(fileName);
		updateUsageReports();

		return new File(directory, fileName);
	}

	/**
	 * Print a summary on used and unused test data files into a directory specific
	 * log file.
	 */
	private void updateUsageReports() {
		try {
			File baseDir = new File(REPORT_DIRECTORY_NAME);
			FileSystemUtils.ensureDirectoryExists(baseDir);
			String fname = directory.toString().replaceAll("[\\\\/]", "_");

			PrintWriter pw = new PrintWriter(new FileWriter(new File(baseDir, fname + "_usage.txt")));
			printUsedFiles(pw);
			pw.close();

			pw = new PrintWriter(new FileWriter(new File(baseDir, fname + "_unusage.txt")));
			printUnusedFiles(pw);
			pw.close();
		} catch (IOException e) {
			// This is the best we can do (as we are in testing)
			e.printStackTrace();
		}
	}

	/** Print a report on all files not used. */
	public void printUnusedFiles(PrintWriter pw) {
		pw.println("Unused files for directory " + directory + ": " + unusedFiles.size());
		ArrayList<String> fileList = new ArrayList<>(unusedFiles);
		Collections.sort(fileList);
		for (String fileName : fileList) {
			pw.print("  ");
			pw.println(fileName);
		}

		pw.flush();
	}

	/** Print a report on all files used. */
	public void printUsedFiles(PrintWriter pw) {
		pw.println("Used files for directory " + directory);
		for (Class<?> key : usedFiles.getKeys()) {
			pw.print("  ");
			pw.println(key);
			for (String fileName : usedFiles.getCollection(key)) {
				pw.print("      ");
				pw.println(fileName);
			}
		}
		pw.println();
		pw.flush();
	}

	/** Read content of file from test-data folder as a string. */
	public String readTestDataFile(String fileName) throws IOException {
		String fileContent = FileSystemUtils.readFile(getTestFile(fileName));
		return StringUtils.replaceLineBreaks(fileContent, "\n");
	}

}