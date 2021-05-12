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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.concurrent.InterruptTimerTask;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.system.SystemUtils;

/**
 * Executes a system process. Takes care of reading stdout and stderr of the
 * process in separate threads to avoid blocking.
 */
public class ProcessUtils {

	/** Character set used for process I/O. Important on Windows. */
	public static Charset CONSOLE_CHARSET = StandardCharsets.UTF_8;

	static {
		if (SystemUtils.isWindows()) {
			try {
				ExecutionResult result = execute(new String[] { "chcp.com" });
				Matcher matcher = Pattern.compile("\\d+").matcher(result.getStdout());
				if (matcher.find()) {
					CONSOLE_CHARSET = Charset.forName("Cp" + matcher.group());
				}
			} catch (IOException | IllegalArgumentException e) {
				// Keep default.
			}
		}
	}

	/**
	 * Creates a {@link ProcessBuilder} for executing a .net assembly. If the
	 * current OS is not Windows mono will be used for executing the assembly.
	 */
	public static ProcessBuilder createDotNetProcessBuilder(String... arguments) {
		ProcessBuilder builder = new ProcessBuilder();

		if (!SystemUtils.isWindows()) {
			builder.command().add("mono");
		}

		builder.command().addAll(Arrays.asList(arguments));

		return builder;
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param completeArguments
	 *            Array of command line arguments to start the process
	 *
	 * @return result of the execution
	 */
	public static ExecutionResult execute(String[] completeArguments) throws IOException {
		return execute(completeArguments, null);
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param completeArguments
	 *            Array of command line arguments to start the process
	 * @param input
	 *            String that gets written to stdin
	 *
	 * @return result of the execution
	 */
	public static ExecutionResult execute(String[] completeArguments, String input) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(completeArguments);
		return execute(builder, input);
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param builder
	 *            builder that gets executed
	 * @return result of the execution
	 */
	public static ExecutionResult execute(ProcessBuilder builder) throws IOException {
		return execute(builder, null);
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param builder
	 *            builder that gets executed
	 * @param input
	 *            String that gets written to stdin
	 * @return result of the execution
	 */
	public static ExecutionResult execute(ProcessBuilder builder, String input) throws IOException {
		return execute(builder, input, -1);
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param builder
	 *            builder that gets executed
	 * @param input
	 *            String that gets written to stdin (may be null).
	 * @param timeOut
	 *            the number to seconds to wait for the process. If this runs
	 *            longer, the process is killed. Passing a value of 0 or less
	 *            makes the method wait forever (until the process finishes
	 *            normally). To find out whether the process was killed, query
	 *            {@link ExecutionResult#isNormalTermination()}.
	 *
	 * @return result of the execution
	 */
	public static ExecutionResult execute(ProcessBuilder builder, String input, int timeOut) throws IOException {
		return execute(builder, input, timeOut, true);
	}

	/**
	 * Executes a process in a thread-safe way.
	 *
	 * @param builder
	 *            builder that gets executed
	 * @param input
	 *            String that gets written to stdin (may be null).
	 * @param timeOut
	 *            the number to seconds to wait for the process. If this runs
	 *            longer, the process is killed. Passing a value of 0 or less
	 *            makes the method wait forever (until the process finishes
	 *            normally). To find out whether the process was killed, query
	 *            {@link ExecutionResult#isNormalTermination()}.
	 * @param collectOutputStreamContent
	 *            boolean that indicates if the content from the stderr and
	 *            stdout shall be collected. False is useful if the content is
	 *            not needed and / or if the content would cause an
	 *            OutOfMemoryException.
	 *
	 * @return result of the execution
	 */
	public static ExecutionResult execute(ProcessBuilder builder, String input, int timeOut,
			boolean collectOutputStreamContent) throws IOException {
		// start process
		Process process = builder.start();

		// read error for later use
		StreamReaderThread stderrReader = new StreamReaderThread(process.getErrorStream(), CONSOLE_CHARSET,
				collectOutputStreamContent);
		StreamReaderThread stdoutReader = new StreamReaderThread(process.getInputStream(), CONSOLE_CHARSET,
				collectOutputStreamContent);

		// write input to process
		if (input != null) {
			Writer stdIn = new OutputStreamWriter(process.getOutputStream());
			stdIn.write(input);
			stdIn.close();
		}

		// wait for process
		boolean normalTermination = waitForProcess(process, timeOut);
		int exitValue = -1;
		if (normalTermination) {
			exitValue = process.exitValue();
		}

		try {
			// It is important to wait for the threads, so the output is
			// completely stored.
			stderrReader.join();
			stdoutReader.join();
		} catch (InterruptedException e) {
			// ignore this one
		}

		return new ExecutionResult(stdoutReader.getContent(), stderrReader.getContent(), exitValue, normalTermination);
	}

	/**
	 * Waits for the process to end or terminates it if it hits the timeout. The
	 * return value indicated whether the process terminated (true) or was
	 * killed by the timeout (false).
	 *
	 * @param maxRuntimeSeconds
	 *            is this is non-positive, this method waits until the process
	 *            terminates (without timeout).
	 */
	private static boolean waitForProcess(Process process, int maxRuntimeSeconds) {
		// Stopping a running process after a given time is not well supported
		// by the Java API. See the following links for the gory details:
		// * http://kylecartmell.com/?p=9
		// * http://www.kylecartmell.com/public_files/ProcessTimeoutExample.java
		Timer timer = new Timer(true);

		if (maxRuntimeSeconds > 0) {
			timer.schedule(new InterruptTimerTask(Thread.currentThread()), maxRuntimeSeconds * 1000);
		}
		try {
			process.waitFor();
			return true;
		} catch (InterruptedException e) {
			process.destroy();
			return false;
		} finally {
			// stop timer if still running (relevant if process terminates "in
			// time")
			timer.cancel();
			// clear the interrupt flag (see the links above for details)
			Thread.interrupted();
		}
	}

	/**
	 * Parameter object that encapsulates the result of a process execution.
	 * This object is immutable.
	 */
	public static class ExecutionResult {

		/** Output on stdout of the process */
		private final String stdout;

		/** Output on stderr of the process */
		private final String stderr;

		/** Return code of the process */
		private final int returnCode;

		/** Whether termination was normal (not timeout). */
		private final boolean normalTermination;

		/** Constructor */
		private ExecutionResult(String stdout, String stderr, int returnCode, boolean normalTermination) {
			this.stdout = stdout;
			this.stderr = stderr;
			this.returnCode = returnCode;
			this.normalTermination = normalTermination;
		}

		/** Returns stdout. */
		public String getStdout() {
			return stdout;
		}

		/** Returns stderr. */
		public String getStderr() {
			return stderr;
		}

		/** Returns returnCode. */
		public int getReturnCode() {
			return returnCode;
		}

		/** Returns whether this was a normal termination (not a timeout). */
		public boolean isNormalTermination() {
			return normalTermination;
		}
	}

	/**
	 * Runs the given process builder with the given input. If creating the
	 * process fails, the process times out or the process exits with a non-zero
	 * exit code, an exception is thrown
	 *
	 * @param builder
	 *            The process builder to execute.
	 * @param input
	 *            The input to the program.
	 * @param timeout
	 *            How many seconds to wait before terminating the process. If
	 *            this is less than 1, no timeout is applied.
	 * @param exceptionConstructor
	 *            Constructs the exception that is thrown in case something goes
	 *            wrong. The first argument is a descriptive message, the second
	 *            is either <code>null</code> (in case of a timeout or non-zero
	 *            exit code) or the exception that caused the failure. This
	 *            parameter is compatible with most exception constructors, e.g.
	 *            <code>ConQATException::new</code>.
	 * @return the execution result of the successfully run process.
	 * @throws T
	 *             An exception constructed with the given constructor. Thrown
	 *             if the process fails to run and return a zero exit code.
	 */
	public static <T extends Exception> ExecutionResult executeOrThrow(ProcessBuilder builder, String input,
			int timeout, BiFunction<String, Throwable, T> exceptionConstructor) throws T {
		String commandString = StringUtils.concat(builder.command(), StringUtils.SPACE);
		try {
			ExecutionResult result = ProcessUtils.execute(builder, input, timeout);
			if (!result.isNormalTermination()) {
				throw exceptionConstructor.apply("Process " + commandString + " timed out.", null);
			}
			if (result.getReturnCode() != 0) {
				throw exceptionConstructor.apply("Process " + commandString + " failed with non-zero exit code "
						+ result.getReturnCode() + ". Error output: " + result.getStderr(), null);
			}
			return result;
		} catch (IOException e) {
			throw exceptionConstructor.apply("Failed to execute " + commandString, e);
		}
	}
}