/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright (c) 2005-2017 The ConQAT Project                               |
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
package org.conqat.lib.commons.filesystem;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A wrapper for a {@link FileInputStream} to support marking. This is
 * preferable over using a {@link BufferedInputStream}, which tries to keep the
 * entire file in memory (and will fail for large files).
 * 
 * This solution is based on code from
 * <a href="https://stackoverflow.com/a/18665678/1237576">here</a>.
 */
public class MarkableFileInputStream extends FilterInputStream {

	/** The underlying channel. */
	private final FileChannel channel;

	/** The current mark position. */
	private long mark = -1;

	/** Constructor. */
	public MarkableFileInputStream(FileInputStream fis) {
		super(fis);
		channel = fis.getChannel();
	}

	/** {@inheritDoc} */
	@Override
	public boolean markSupported() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void mark(int readlimit) {
		try {
			mark = channel.position();
		} catch (IOException ex) {
			mark = -1;
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void reset() throws IOException {
		if (mark == -1) {
			throw new IOException("Must call mark() before reset()!");
		}
		channel.position(mark);
	}
}