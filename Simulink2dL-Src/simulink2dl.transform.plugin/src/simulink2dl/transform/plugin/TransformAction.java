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
package simulink2dl.transform.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import simulink2dl.util.PluginLogger;

/**
 * Entry Point for the GUI Button
 * 
 * @author Timm Liebrenz
 */
public class TransformAction implements IWorkbenchWindowActionDelegate {

	// the currently selected project
	private IProject iproj;

	private IFile selectedFile;

	@Override
	public void run(IAction arg0) {
		// check whether a project is selected
		if (this.iproj == null) {
			// no projects are selected
			PluginLogger.info("Select a project.");
			return;
		}
		if (this.selectedFile == null) {
			// no projects are selected
			PluginLogger.info("Please select a Simulink model from the project tree.");
			return;
		}

		// open a wizard dialog
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog wizard = new WizardDialog(shell, new TransformWizard(this.iproj, this.selectedFile));
		wizard.open();
	}

	/**
	 * Called if someone selects a project in the Project Navigator
	 */
	@Override
	public void selectionChanged(IAction arg0, ISelection selection) {
		if (selection.isEmpty()) {
			return;
		}

		// check whether a project was selected
		if (selection instanceof IStructuredSelection) {
			Object[] sel = ((IStructuredSelection) selection).toArray();

			// check whether only one project is selected
			if (sel.length != 1) {
				PluginLogger.info("Select a single project.");
				this.iproj = null;
				return;
			}

			// update the currently selected program
			Object first = sel[0];
			if ((first instanceof IResource)) {
				IResource res = (IResource) first;
				this.iproj = res.getProject();
				PluginLogger.info("Selected: " + this.iproj.getName());
			}

			if (first instanceof IFile) {
				selectedFile = (IFile) first;
			}
		}
	}

	@Override
	public void dispose() {
		// empty
	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		// empty
	}

}
