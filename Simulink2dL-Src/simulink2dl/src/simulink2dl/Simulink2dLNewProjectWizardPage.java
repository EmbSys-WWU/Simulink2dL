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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import simulink2dl.preferences.Simulink2dLPreferenceConstants;

public class Simulink2dLNewProjectWizardPage extends WizardNewProjectCreationPage {

	Text sLModelText = null;
	Composite importGroup;

	public Simulink2dLNewProjectWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite newComposite = new Composite((Composite) getControl(), SWT.None);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newComposite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		newComposite.setLayout(new GridLayout());
		newComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createImportGroup(newComposite);

		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(newComposite);
	}

	private final void createImportGroup(Composite parent) {
		importGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		importGroup.setLayout(layout);
		importGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createSimulinkModelChooserPart(importGroup);
	}

	private void createSimulinkModelChooserPart(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Simulink Model:");

		sLModelText = new Text(parent, SWT.BORDER);
		sLModelText.setText("<specify model>");
		sLModelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button browseModelButton = new Button(parent, SWT.NONE);
		browseModelButton.setText("Browse...");
		browseModelButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell(), SWT.OPEN);
				fd.setText("Open");

				Simulink2dLPlugin plugin = Simulink2dLPlugin.getDefault();
				IPreferenceStore iPreferenceStore = plugin.getPreferenceStore();
				String lastModelFilePath = iPreferenceStore
						.getString(Simulink2dLPreferenceConstants.LAST_MODEL_FILE_PATH);

				fd.setFilterPath(lastModelFilePath);

				// setting the filter to allow multiple extensions only works in
				// Windows,
				// for Linux, we do not use a filter
				if (System.getProperty("os.name").startsWith("Windows")) {
					// http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fswt%2Fwidgets%2FFileDialog.html
					// allow .mdl, .slx (and for now .xml) file types
					String[] filterExt = { "*.mdl; *.slx; *.xml" };
					fd.setFilterExtensions(filterExt);
				}
				String selected = fd.open();
				if (selected != null) {
					sLModelText.setText(selected);
					Simulink2dLPlugin.getDefault().getPreferenceStore()
							.setValue(Simulink2dLPreferenceConstants.LAST_MODEL_FILE_PATH, selected);
				}
				setPageComplete(validatePage());
			}

		});

	}

	@Override
	protected boolean validatePage() {
		boolean result = super.validatePage();
		if (sLModelText == null) {
			return false;
		}
		File f = new File(sLModelText.getText());
		return result && f.exists();

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		importGroup.setVisible(visible);
	}

	public String getModelLocation() {
		return sLModelText.getText();
	}

}
