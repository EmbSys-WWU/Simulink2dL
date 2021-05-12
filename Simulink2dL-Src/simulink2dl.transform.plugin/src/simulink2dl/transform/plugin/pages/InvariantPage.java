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
package simulink2dl.transform.plugin.pages;

import java.io.File;
import java.util.HashSet;

import org.conqat.lib.simulink.model.SimulinkModel;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import simulink2dl.invariants.InvariantGenerator;
import simulink2dl.transform.plugin.TransformWizard;
import simulink2dl.util.PluginLogger;

public class InvariantPage extends WizardPage {

	private TransformWizard wizard;

	private Table analyzerSelection;
	private Table resultsTable;
	private HashSet<String> selectedAnalyzer;

	public InvariantPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		initialize();

		Composite composite = new Composite(parent, SWT.NONE);
		createLayout(composite);

		PluginLogger.info("InvariantPage loaded");
	}

	private void initialize() {
		this.wizard = (TransformWizard) getWizard();
		selectedAnalyzer = new HashSet<String>();

		this.setErrorMessage(null);
	}

	private void createLayout(Composite composite) {
		this.setControl(composite);

		// assign a layout to this page that contains three columns
		GridLayout layout = new GridLayout(3, false);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(layout);

		// =======================================
		// selectable list of analyzer
		// =======================================
		final Group analyzerGroup = new Group(composite, SWT.SHADOW_IN);
		analyzerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		analyzerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		analyzerGroup.setLayout(new GridLayout(1, false));
		analyzerGroup.setText("Select Analyzer");

		Table analyzerSelectorTable = new Table(analyzerGroup,
				SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK);
		analyzerSelectorTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		analyzerSelectorTable.addListener(SWT.SetData, event -> {
			TableItem item = (TableItem) event.item;
			int index = analyzerSelectorTable.indexOf(item);
			item.setText("Item " + index);
			System.out.println(item.getText());
		});

		analyzerSelectorTable.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				// there's a difference between checking and selecting an item
				TableItem fhtagn = ((TableItem) event.item);
				if (fhtagn.getChecked()) {
					this.selectedAnalyzer.add((String) fhtagn.getData());
				} else {
					this.selectedAnalyzer.remove((String) fhtagn.getData());
				}
			}
		});

		this.putAvailableAnalyzerInTable(analyzerSelectorTable);

		// =======================================
		// transformation button
		// =======================================
		Button startBtn = new Button(composite, SWT.PUSH);
		startBtn.setText("Invariant Generation");
		startBtn.addSelectionListener(this.invariantButtonClicked());

		// =======================================
		// results pane
		// =======================================
		final Group resultsGrp = new Group(composite, SWT.SHADOW_IN);
		resultsGrp.setLayout(new GridLayout(1, false));
		GridData resultsGrpLayoutData = new GridData(GridData.FILL_VERTICAL);
		resultsGrpLayoutData.widthHint = 200;
		resultsGrp.setLayoutData(resultsGrpLayoutData);
		resultsGrp.setText("Results");

		this.resultsTable = new Table(resultsGrp, SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		this.resultsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.resultsTable.addListener(SWT.SetData, event -> {
			TableItem item = (TableItem) event.item;
			int index = this.resultsTable.indexOf(item);
			item.setText("Itemized " + index);
			System.out.println(item.getText());
		});
	}

	private SelectionListener invariantButtonClicked() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PluginLogger.info("Invariant pressed");
				resultsTable.removeAll();

				try {
					// start time
					long time1 = System.currentTimeMillis();

					PluginLogger.info(
							"Invariant Generation for Model: " + wizard.getTransformer().getSimulinkModel().getName());

					SimulinkModel model = wizard.getModel();
					InvariantGenerator invGen = new InvariantGenerator(wizard.getTransformer(), wizard.getModel(),
							getInputsFilePath(), getTempOutpuFilePath());

					invGen.generateInvariants();
					invGen.finalizeInformation();
					refreshOutput(wizard.getProject());

					PluginLogger.info("Invariant Generation Took: " + (System.currentTimeMillis() - time1) + " ms");

					// If finish is pressed, performFinish() in TransformWizard is called.
					setPageComplete(true);
				} catch (Exception exception) {
					PluginLogger.exception("", exception);
				}
			}

			private void refreshOutput(IProject project) {
				IFolder output = project.getFolder("output" + File.separator + "dL");
				try {
					output.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		};
	}

	private String getInputsFilePath() {
		IFolder output = wizard.getProject().getFolder("input" + File.separator + "inputs_info.xml");
		return output.getLocation().toOSString();
	}

	private String getTempOutpuFilePath() {
		IFolder output = wizard.getProject().getFolder("output" + File.separator + "dL");
		if (!output.exists()) {
			try {
				output.create(IResource.NONE, true, null);
			} catch (CoreException e) {
				PluginLogger.exception("", e);
			}
		}
		return output.getLocation().toOSString() + File.separator + "invariants.txt";
	}

	private void putAvailableAnalyzerInTable(Table analyzerTable) {
		// TODO ADD ANALYZER
	}

	private void putSingleOptimizerInTable(Table tbl, String text, String data) {
		TableItem tblItem = new TableItem(tbl, SWT.NONE);
		tblItem.setText(text);
		tblItem.setData(data);
		tblItem.setChecked(true);
		this.selectedAnalyzer.add((String) tblItem.getData());
	}
}
