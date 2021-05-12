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
package org.conqat.lib.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A thread to drain an input stream. Storing the content is optional.
 *
 * If an exception occurs during draining, the exceptions message is appended to
 * the content and the exception is also made available via
 * {@link #getException()}. So if the caller wants to ensure that the content is
 * really complete, he not only has to wait for the end of the thread via join,
 * but also check this method.
 */
public class StreamReaderThread extends Thread {

	/** Stream the reader reads from. */
	private final InputStream input;

	/** Content read from the stream. */
	private final StringBuilder content = new StringBuilder();

	/** Whether to store content or not. */
	private final boolean storeContent;

	/** Charset to convert streams to strings. */
	private final Charset charset;

	/** Exception that occurred (or null). */
	private IOException exception;

	/**
	 * Create a new reader that reads the content of this stream in its own
	 * thread. => This call is non-blocking.
	 * <p>
	 * This constructor causes the content to be stored.
	 *
	 * @param input
	 *            Stream to read from. This stream is not automatically closed,
	 *            but must be closed by the caller (if this is intended).
	 *
	 */
	public StreamReaderThread(InputStream input) {
		this(input, StandardCharsets.UTF_8, true);
	}

	/**
	 * Create a new reader that reads the content of this stream in its own
	 * thread. => This call is non-blocking.
	 * <p>
	 * This constructor causes the content to be stored.
	 *
	 * @param input
	 *            Stream to read from. This stream is not automatically closed,
	 *            but must be closed by the caller (if this is intended).
	 * @param charset
	 *            Character set to be used to convert the stream into a string.
	 */
	public StreamReaderThread(InputStream input, Charset charset) {
		this(input, charset, true);
	}

	/**
	 * Create a new reader that reads the content of this stream in its own
	 * thread. => This call is non-blocking
	 *
	 * @param input
	 *            Stream to read from. This stream is not automatically closed,
	 *            but must be closed by the caller (if this is intended).
	 *
	 */
	public StreamReaderThread(InputStream input, Charset charset, boolean storeContent) {
		this.input = input;
		this.charset = charset;
		this.storeContent = storeContent;
		start();
	}

	/**
	 * Reads content from the stream as long as the stream is not empty.
	 */
	@Override
	public synchronized void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		char[] buffer = new char[1024];

		try {
			int read = 0;
			while ((read = reader.read(buffer)) != -1) {
				if (storeContent) {
					content.append(buffer, 0, read);
				}
			}
		} catch (IOException e) {
			exception = e;
			content.append(e);
		}
	}

	/** Returns the content read from the stream. */
	public synchronized String getContent() {
		return content.toString();
	}

	/**
	 * If everything went ok during reading from the stream, this returns null.
	 * Otherwise the exception can be found here.
	 */
	public IOException getException() {
		return exception;
	}
}