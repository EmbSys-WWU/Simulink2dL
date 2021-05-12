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

import java.io.PrintStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import simulink2dl.preferences.Simulink2dLPreferenceConstants;
import simulink2dl.transform.config.TransformerMapping;

/**
 * The activator class controls the plug-in life cycle
 */
public class Simulink2dLPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "simulink2dl";

	public enum LogLevel {
		info, warning, error, success, attention
	}

	public static PrintStream out = System.out;
	public static PrintStream err = System.err;
	private static Simulink2dLOutput simulink2dLLog;
	private static IProject currentProject;

	private static Simulink2dLPlugin plugin;

	/**
	 * The constructor
	 */
	public Simulink2dLPlugin() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Simulink2dLOutput.init();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.addSelectionListener(new ISelectionListener() {

					@Override
					public void selectionChanged(IWorkbenchPart part, ISelection selection) {
						if (selection instanceof IStructuredSelection) {
							Object first = ((IStructuredSelection) selection).getFirstElement();
							if (first instanceof IResource) {
								IResource res = (IResource) first;
								IProject project = res.getProject();
								Simulink2dLPlugin.setCurrentProject(project);
							}
						}

					}
				});

		// initialize transformer mappings
		TransformerMapping tm = TransformerMapping.getInstance();
		if (!tm.initialized()) {
			tm.initialize(context.getBundle());
		}

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// plugin = null; // Don't set to null. Race conditions!
		// Can cause nullpointer exceptions if stop()
		// in other plugins access getDefault().
		super.stop(context);
	}

	public static Simulink2dLPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void setOut(PrintStream out) {
		Simulink2dLPlugin.out = out;
	}

	public static void setErr(PrintStream err) {
		Simulink2dLPlugin.err = err;
	}

	public static void setSimulink2dLLog(Simulink2dLOutput outputLog) {
		Simulink2dLPlugin.simulink2dLLog = outputLog;
	}

	public static void logException(String msg, Exception e) {
		if (simulink2dLLog != null) {
			simulink2dLLog.logException(getDefault().getLog(), PLUGIN_ID, msg, e);
		} else if (getDefault() != null) {
			getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));
		}

		if (getDefault() != null && Simulink2dLPlugin.getDefault().getPreferenceStore()
				.getBoolean(Simulink2dLPreferenceConstants.DEBUG_MODE)) {
			e.printStackTrace(err);
		}
	}

	public static IProject getCurrentProject() {
		return currentProject;
	}

	public static void setCurrentProject(IProject currentProject) {
		Simulink2dLPlugin.currentProject = currentProject;
	}

	public static void warn(String message) {
		Simulink2dLPlugin.out.println("[WARNING:]   " + message);

	}

	public static void log(Object obj, LogLevel level) {
		switch (level) {
		case info:
		default:
			Simulink2dLPlugin.out.println(obj.toString());
		}
	}

}
