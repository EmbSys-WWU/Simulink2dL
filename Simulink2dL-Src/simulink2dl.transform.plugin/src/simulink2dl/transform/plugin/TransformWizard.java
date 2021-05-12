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

import java.io.File;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

import simulink2dl.transform.Transformer;
import simulink2dl.transform.plugin.pages.ContractSelectorPage;
import simulink2dl.transform.plugin.pages.InvariantPage;
import simulink2dl.transform.plugin.pages.TransformPage;

public class TransformWizard extends Wizard {

	// the project that is selected
	private IProject currentProject;

	// the file that is selected
	private IFile currentFile;

	// Pages
	private ContractSelectorPage contractSelectorPage;
	private TransformPage transformPage;
	private InvariantPage invariantPage;

	/**
	 * Generates a wizard for the transformation
	 * 
	 * @param iproj
	 * @param selectedFile
	 */
	public TransformWizard(IProject iproj, IFile currentFile) {
		this.setWindowTitle("Transform Simulink model");

		this.currentProject = iproj;
		this.currentFile = currentFile;

		this.contractSelectorPage = new ContractSelectorPage("Select Contracts");
		this.addPage(this.contractSelectorPage);

		this.transformPage = new TransformPage("Transform Model", iproj, currentFile);
		this.addPage(this.transformPage);

		this.invariantPage = new InvariantPage("Generate Invariants");
		this.addPage(invariantPage);
	}

	/**
	 * Exit the wizard
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

	/**
	 * @return the contractFiles
	 */
	public List<File> getContractFiles() {
		return this.contractSelectorPage.getContracts();
	}

	public SimulinkModel getModel() {
		return this.transformPage.getModel();
	}

	public Transformer getTransformer() {
		return this.transformPage.getTransformer();
	}

	public IProject getProject() {
		return this.currentProject;
	}

}
