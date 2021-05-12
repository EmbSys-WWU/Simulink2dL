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
package org.conqat.lib.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.conqat.lib.commons.filesystem.FileSystemUtils;

/**
 * {@link AutoCloseable} wrapper around
 * {@link Files#createTempDirectory(String, java.nio.file.attribute.FileAttribute...)}
 * that deletes the temp directory when closed.
 */
public class SelfDeletingTempDirectory implements AutoCloseable {

	/**
	 * The temp directory.
	 */
	private final File directory;

	/** @see #directory */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Creates a new temp directory using the given prefix to determine its
	 * name.
	 */
	public SelfDeletingTempDirectory(String prefix) throws IOException {
		directory = Files.createTempDirectory(prefix).toFile();
	}

	/**
	 * Recursively deletes the temp directory. In case that fails (e.g. if some
	 * files are still being used), registers the directory for deletion on JVM
	 * shutdown.
	 */
	@Override
	public void close() {
		FileSystemUtils.deleteRecursively(directory);
		if (directory.exists()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> FileSystemUtils.deleteRecursively(directory)));
		}
	}
}
