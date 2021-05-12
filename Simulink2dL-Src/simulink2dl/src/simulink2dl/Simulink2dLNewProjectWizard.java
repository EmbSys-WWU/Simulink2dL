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

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class Simulink2dLNewProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private Simulink2dLNewProjectWizardPage newProjectPage;

	private IConfigurationElement configurationElement;

	public Simulink2dLNewProjectWizard() {
		setWindowTitle("Simulink2dL Project");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// NOP
	}

	@Override
	public boolean performFinish() {
		String name = newProjectPage.getProjectName();
		URI location = null;
		File slModel = new File(newProjectPage.getModelLocation());
		if (!newProjectPage.useDefaults()) {
			location = newProjectPage.getLocationURI();
		}

		Simulink2dLNewProjectSupport.createProject(name, location, slModel);

		BasicNewProjectResourceWizard.updatePerspective(configurationElement);

		return true;
	}

	@Override
	public void addPages() {
		super.addPages();

		newProjectPage = new Simulink2dLNewProjectWizardPage("New Simulink2dL Project");
		newProjectPage.setTitle("New Simulink2dL Project");
		newProjectPage.setDescription("Create a new Simulink2dL Project");

		addPage(newProjectPage);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

		configurationElement = config;

	}
}
