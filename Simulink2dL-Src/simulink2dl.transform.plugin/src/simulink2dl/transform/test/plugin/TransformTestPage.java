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
package simulink2dl.transform.test.plugin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.conqat.lib.simulink.model.SimulinkModel;
import org.eclipse.core.resources.IProject;
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

import simulink2dl.transform.Transformer;
import simulink2dl.transform.test.DefaultParametersChecker;
import simulink2dl.transform.test.ModelConsistencyChecker;
import simulink2dl.util.PluginLogger;

/**
 * This page lets the user choose which tests to run to verify the
 * transformation.
 * 
 * @author Nick Bremer
 *
 */
public class TransformTestPage extends WizardPage {

	// the project that is selected
	private IProject currentProject;

	// the model in JavaIR
	private SimulinkModel model;

	// model transformer to create a dL representation
	private Transformer transformer;

	// thread to create the model in its JavaIR representation
	private Thread modelMaker;

	// tests that are selected for transformation
	private List<String> selectedTests = new LinkedList<>();

	private Table resultsTable;

	protected TransformTestPage(IProject project) {
		// dummy page name
		super("PageName");

		// starting threaded work as early as possible
		this.modelMaker = new Thread(new Runnable() {
			@Override
			public void run() {
				model = null;
			}
		});
		this.modelMaker.start();

		// set and initialize the member variables
		this.currentProject = project;
		this.setMessage("Test the transformation of the model (" + project.getName() + ").");
		this.setPageComplete(true);
		this.setErrorMessage(null);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		this.setControl(composite);

		// assign a layout to this page that contains three columns
		GridLayout layout = new GridLayout(4, false);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(layout);

		// =======================================
		// list of tests
		// =======================================
		final Group testsGroup = new Group(composite, SWT.SHADOW_IN);
		testsGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		testsGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		testsGroup.setLayout(new GridLayout(1, false));
		testsGroup.setText("Select Tests");

		Table testsSelectorTable = new Table(testsGroup,
				SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK);
		testsSelectorTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		testsSelectorTable.addListener(SWT.SetData, event -> {
			TableItem item = (TableItem) event.item;
			int index = testsSelectorTable.indexOf(item);
			item.setText("Item " + index);
			System.out.println(item.getText());
		});
		// when blocks get (un-)checked (remove them from) add them to the
		// member variable this.selectedTests
		testsSelectorTable.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				// there's a difference between checking and selecting an item
				TableItem fhtagn = ((TableItem) event.item);
				if (fhtagn.getChecked()) {
					this.selectedTests.add(fhtagn.getText());
				} else {
					this.selectedTests.remove(fhtagn.getText());
				}
			}
		});

		if (this.model == null) { // Blocks not ready!
			PluginLogger.info("[INFO] Blocks not ready!");
			try {
				this.modelMaker.join();
			} catch (InterruptedException ie) {
				PluginLogger.error("Aborted waiting!");
				ie.printStackTrace();
			}
			PluginLogger.info("Blocks arrived. Continuing as usual.");
		}
		// this.putSimulinkItemsInTable((this.model == null) ? null : model.getBlocks(),
		// testsSelectorTable, "");

		TableItem tblItemModelCon = new TableItem(testsSelectorTable, SWT.NONE);
		tblItemModelCon.setText("Model Consistency");

		TableItem tblItemDefPara = new TableItem(testsSelectorTable, SWT.NONE);
		tblItemDefPara.setText("Default Parameters");

		// =======================================
		// test button
		// =======================================
		Button startBtn = new Button(composite, SWT.PUSH);
		startBtn.setText("Test");
		startBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PluginLogger.info("[INFO] Started testing");
				resultsTable.removeAll();
				long tick;
				long tock;

				for (String test : selectedTests) {
					boolean res;
					/**
					 * Test cases can be added here by copying and modifying existing tests. You
					 * will want to add TableItems above so that the tests can be selected in the
					 * GUI.
					 */
					switch (test) {
					case "Model Consistency":
						tick = System.nanoTime();
						res = ModelConsistencyChecker.modelConsistent(model);
						tock = System.nanoTime();
						putTestResultItemsInTable(test, resultsTable, res, tock - tick);
						break;

					case "Default Parameters":
						tick = System.nanoTime();
						res = DefaultParametersChecker.someBlocksHaveDefParams(model);
						tock = System.nanoTime();
						putTestResultItemsInTable("some " + test, resultsTable, res, tock - tick);

						tick = System.nanoTime();
						res = DefaultParametersChecker.allBlocksHaveDefParams(model);
						tock = System.nanoTime();
						putTestResultItemsInTable("all " + test, resultsTable, res, tock - tick);
						break;

					default:
						PluginLogger.info("Test '" + test + "' was not run.");
						res = false;
						break;
					}
				}

				if (selectedTests.isEmpty()) {
					putTestResultItemsInTable(null, resultsTable, false, 0L);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// empty
			}
		});

		// =======================================
		// results pane
		// =======================================
		final Group resultsGrp = new Group(composite, SWT.SHADOW_IN);
		resultsGrp.setLayout(new GridLayout(1, false));
		GridData resultsGrpLayoutData = new GridData(GridData.FILL_VERTICAL);
		resultsGrpLayoutData.widthHint = 400;
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
		PluginLogger.info("createControl done");
	}

	/**
	 * // TODO Adds the provided <code>SimulinkItem</code>s to the specified
	 * <code>Table</code>. The names of the elements are also written to the
	 * console, with the optional prefix. A <code>Table</code> should probably be
	 * cleared, before adding items.
	 * 
	 * @param items  These <code>SimulinkItem</code>s will be added.
	 * @param tbl    The elements are added to this <code>Table</code>.
	 * @param prefix Optional prefix displayed in the console.
	 */
	private void putTestResultItemsInTable(String test, Table tbl, boolean res, long elapsedTime) {
		TableItem tblItem = new TableItem(tbl, SWT.NONE);
		if (test == null) {
			tblItem.setText("<none>");
		} else {
			double time = elapsedTime / Math.pow(10, 6);
			tblItem.setText(test + ": " + res + " (" + time + " ms)");
		}
	}
}
