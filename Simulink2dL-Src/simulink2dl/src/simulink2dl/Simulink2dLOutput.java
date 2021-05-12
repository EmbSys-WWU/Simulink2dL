/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of Münster
 * SESE Software and Embedded Systems Engineering, TU Berlin
 * 
 * Authors:
 * 	Paula Herber
 * 	Sabine Glesner
 * 	Timm Liebrenz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package simulink2dl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

/**
 * This class handles the standard output to the console.
 * 
 * @author Timm Liebrenz
 *
 */
public class Simulink2dLOutput {

	boolean debugThreadActive = false;
	static IOConsole myConsole = findConsole();
	static IOConsoleOutputStream console = myConsole.newOutputStream();
	public final static String CONSOLE_NAME = "Test";

	private static Simulink2dLOutput instance = new Simulink2dLOutput();

	public static void init() {
		instance.createOuputStreams();
		Simulink2dLPlugin.setSimulink2dLLog(instance);
		console.setActivateOnWrite(true);
	}

	private void printErr(final String msg) {
		System.err.print(msg);
	}

	private void createOuputStreams() {

		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				console.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				console.write(b, off, len);
			}

			@Override
			public void write(byte[] b) throws IOException {
				console.write(b, 0, b.length);
			}
		};

		OutputStream err = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				String str = String.valueOf((char) b);
				printErr(str);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				String str = new String(b, off, len);
				printErr(str);
			}

			@Override
			public void write(byte[] b) throws IOException {
				console.write(b, 0, b.length);
			}
		};

		Simulink2dLPlugin.setOut(new PrintStream(out, true));
		Simulink2dLPlugin.setErr(new PrintStream(err, true));
	}

	public void logException(ILog log, String PLUGIN_ID, String msg, Exception e) {
		log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));
		e.printStackTrace(System.err);
	}

	public static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole element : existing) {
			if (CONSOLE_NAME.equals(element.getName())) {
				return (MessageConsole) element;
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(CONSOLE_NAME, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
