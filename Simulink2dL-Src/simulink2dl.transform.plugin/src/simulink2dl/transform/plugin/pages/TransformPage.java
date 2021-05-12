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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.eclipse.core.resources.IFile;
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

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.transform.Transformer;
import simulink2dl.transform.optimizer.ConditionalChoiceOptimizer;
import simulink2dl.transform.optimizer.ContradictionOptimizer;
import simulink2dl.transform.optimizer.FormulaOptimizer;
import simulink2dl.transform.plugin.TransformWizard;
import simulink2dl.util.PluginLogger;

/**
 * This page contains the interface to transform a Simulink model into a
 * Differential Dynamic Logic formula.
 * 
 * It consists of a list to show the blocks in the system and a button to start
 * the transformation.
 * 
 * @author Timm Liebrenz
 *
 */
public class TransformPage extends WizardPage {

	// the project that is selected
	private IProject currentProject;

	// the model in JavaIR
	private SimulinkModel model;

	// model transformer to create a dL representation
	private Transformer transformer;

	// thread to create the model in its JavaIR representation
	private Thread modelMaker;

	// blocks that are selected for transformation
	private Set<SimulinkBlock> selectedBlocks;

	// blocks that are selected for transformation
	private Set<String> selectedOptimizer;

	// blockstructure handler that are selected for transformation
	private Set<String> selectedHandler;

	private Table resultsTable;

	public TransformPage(String pageName, IProject project, IFile selectedFile) {
		// dummy page name
		super(pageName);

		// starting threaded work as early as possible
		this.modelMaker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					model = makeModel(project, selectedFile);
				} catch (CoreException ce) {
					PluginLogger.exception("Model couldn't be loaded", ce);
				} catch (IOException ioe) {
					PluginLogger.exception("Model couldn't be loaded", ioe);
				}
			}
		});
		this.modelMaker.start();

		// set and initialize the member variables
		this.currentProject = project;
		this.selectedBlocks = new HashSet<SimulinkBlock>();
		this.selectedOptimizer = new HashSet<String>();
		this.selectedHandler = new HashSet<String>();
		this.setMessage("Transformation of the model (" + project.getName() + ").");
		this.setPageComplete(true);
		this.setErrorMessage(null);
	}

	private SimulinkModel makeModel(IProject project, IFile selectedFile) throws CoreException, IOException {
		// TODO Currently the model file must be selected from the project tree
		PluginLogger.info("Parsing model from file...");

		// get file
		String pathToFile = selectedFile.getLocation().toOSString();

		// JOptionPane.showInputDialog(null, "Path to model", "Enter path to Simulink
		// model",
		// JOptionPane.PLAIN_MESSAGE);

		if (pathToFile == null) {
			this.setMessage("Please select a Simulink model from the project tree.");
			return null;
		}

		File simulinkFile = new File(pathToFile);
		if (simulinkFile == null || !simulinkFile.exists()) {
			this.setMessage("Please select a Simulink model from the project tree.");
			return null;
		}

		// SimpleLogger sLogger = new SimpleLogger();
		SimulinkModelBuilder modelBuilder = new SimulinkModelBuilder(simulinkFile, null);

		SimulinkModel model = null;

		try {
			model = modelBuilder.buildModel();
		} catch (SimulinkModelBuildingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		modelBuilder.close();

		return model;
	}

	@Override
	public void createControl(Composite parent) {
		// disable next button
		this.setPageComplete(false);

		Composite composite = new Composite(parent, SWT.NONE);
		this.setControl(composite);

		// assign a layout to this page that contains three columns
		GridLayout layout = new GridLayout(5, false);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setLayout(layout);

		// =======================================
		// list of blocks
		// =======================================
		final Group blocksGroup = new Group(composite, SWT.SHADOW_IN);
		blocksGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		blocksGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		blocksGroup.setLayout(new GridLayout(1, false));
		blocksGroup.setText("Select blocks");

		Table blockSelectorTable = new Table(blocksGroup,
				SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK);
		blockSelectorTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		blockSelectorTable.addListener(SWT.SetData, event -> {
			TableItem item = (TableItem) event.item;
			int index = blockSelectorTable.indexOf(item);
			item.setText("Item " + index);
			System.out.println(item.getText());
		});
		// when block get (un-)checked (remove them from) add them to the
		// member variable this.checkedBlocks
		blockSelectorTable.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				// there's a difference between checking and selecting an item
				TableItem fhtagn = ((TableItem) event.item);
//				if (fhtagn.getChecked()) {
//					this.selectedBlocks.add(this.model.getBlockById((int) fhtagn.getData()));
//				} else {
//					this.selectedBlocks.remove(this.model.getBlockById((int) fhtagn.getData()));
//				}
			}
		});

		if (this.model == null) { // Blocks not ready!
			PluginLogger.info("Blocks not ready!");
			try {
				this.modelMaker.join();
			} catch (InterruptedException ie) {
				PluginLogger.error("Aborted waiting!");
				ie.printStackTrace();
			}
			PluginLogger.info("Blocks arrived. Continuing as usual.");
		}

//		this.putSimulinkItemsInTable((this.model == null) ? null : model.getBlocks(), blockSelectorTable, "");

		// =======================================
		// list of selectable blockstructure handler
		// =======================================
		// TODO currently deactivated due to conflict with block structure and simulink
		// parser

//		final Group structureGroup = new Group(composite, SWT.SHADOW_IN);
//		structureGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
//		structureGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
//		structureGroup.setLayout(new GridLayout(1, false));
//		structureGroup.setText("Select blockstructure handler");
//
//		Table structureSelectorTable = new Table(structureGroup,
//				SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK);
//		structureSelectorTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
//		structureSelectorTable.addListener(SWT.SetData, event -> {
//			TableItem item = (TableItem) event.item;
//			int index = structureSelectorTable.indexOf(item);
//			item.setText("Item " + index);
//			System.out.println(item.getText());
//		});
//		// when structure handlers get (un-)checked (remove them from) add them to the
//		// member variable this.checkedStructures
//		structureSelectorTable.addListener(SWT.Selection, event -> {
//			if (event.detail == SWT.CHECK) {
//				// there's a difference between checking and selecting an item
//				TableItem fhtagn = ((TableItem) event.item);
//				if (fhtagn.getChecked()) {
//					this.selectedHandler.add((String) fhtagn.getData());
//				} else {
//					this.selectedHandler.remove((String) fhtagn.getData());
//				}
//			}
//		});
//		Simulink2dLPlugin.out.println("[INFO] starting handler table filling");
//		this.putAvailableHandlersInTable(structureSelectorTable, "");

		// =======================================
		// list of selectable optimizer
		// =======================================
		final Group optimizerGroup = new Group(composite, SWT.SHADOW_IN);
		optimizerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		optimizerGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		optimizerGroup.setLayout(new GridLayout(1, false));
		optimizerGroup.setText("Select optimizers");

		Table optimizerSelectorTable = new Table(optimizerGroup,
				SWT.VIRTUAL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.CHECK);
		optimizerSelectorTable.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		optimizerSelectorTable.addListener(SWT.SetData, event -> {
			TableItem item = (TableItem) event.item;
			int index = optimizerSelectorTable.indexOf(item);
			item.setText("Item " + index);
			System.out.println(item.getText());
		});
		// when optimizers get (un-)checked (remove them from) add them to the
		// member variable this.checkedOptimizer
		optimizerSelectorTable.addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				// there's a difference between checking and selecting an item
				TableItem fhtagn = ((TableItem) event.item);
				if (fhtagn.getChecked()) {
					this.selectedOptimizer.add((String) fhtagn.getData());
				} else {
					this.selectedOptimizer.remove((String) fhtagn.getData());
				}
			}
		});

		this.putAvailableOptimizersInTable(optimizerSelectorTable, "");

		// =======================================
		// transformation button
		// =======================================
		Button startBtn = new Button(composite, SWT.PUSH);
		startBtn.setText("Transform");
		startBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PluginLogger.info("transform pressed");
				resultsTable.removeAll();

				// get contract files
				List<File> contractFolders = ((TransformWizard) getWizard()).getContractFiles();

				try {

					// start time
					long time1 = System.currentTimeMillis();

					transformer = new Transformer(model, lookupContractsInFolders(contractFolders));

					// after initialization
					long time2 = System.currentTimeMillis();

					transformer.transform(selectedHandler);
					transformer.finalizeTransform();
					PluginLogger.info("Transformer finished");

					// after transformation
					long time3 = System.currentTimeMillis();

					transformer.optimize(selectedOptimizer);
					PluginLogger.info("Transformer finished");

					// after optimizations
					long time4 = System.currentTimeMillis();
					transformer.writeOut(getOutputFilePath(currentProject));
					refreshOutput(currentProject);

					// after write out
					long time5 = System.currentTimeMillis();

					PluginLogger.info("output finished");
					PluginLogger.info("[Time] Initialization time: " + (time2 - time1));
					PluginLogger.info("[Time] Transformation time: " + (time3 - time2));
					PluginLogger.info("[Time] Optimization time:   " + (time4 - time3));
					PluginLogger.info("[Time] Write Out time:      " + (time5 - time4));
					PluginLogger.info("----------------------------------------------");
					PluginLogger.info("[Time] Total time:          " + (time5 - time1));

					// enable next button
					setPageComplete(true);
				} catch (FileNotFoundException fnfException) {
					PluginLogger.exception("", fnfException);
				} catch (IOException ioException) {
					PluginLogger.exception("", ioException);
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

			private String getOutputFilePath(IProject project) {
				IFolder output = project.getFolder("output" + File.separator + "dL");
				if (!output.exists()) {
					try {
						output.create(IResource.NONE, true, null);
					} catch (CoreException e) {
						PluginLogger.exception("", e);
					}
				}
				String nameString = model.getOriginId();
				return output.getLocation().toOSString() + File.separator + nameString;
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

		PluginLogger.info("createControl done");
	}

	/**
	 * Adds the provided <code>SimulinkItem</code>s to the specified
	 * <code>Table</code>. The names of the elements are also written to the
	 * console, with the optional prefix. A <code>Table</code> should probably be
	 * cleared, before adding items.
	 * 
	 * @param items  These <code>SimulinkItem</code>s will be added.
	 * @param tbl    The elements are added to this <code>Table</code>.
	 * @param prefix Optional prefix displayed in the console.
	 */
	private void putSimulinkItemsInTable(Set<? extends SimulinkElementBase> items, Table tbl, String prefix) {
		if (items == null || items.size() == 0) {
			PluginLogger.info("no items to add.");
			TableItem tblItem = new TableItem(tbl, SWT.NONE);
			tblItem.setText(prefix + "none");
			tblItem.setData(0);
		} else {
			PluginLogger.info("|items|: " + items.size() + " prefix: " + prefix);
			for (SimulinkElementBase slItem : items) {
				TableItem tblItem = new TableItem(tbl, SWT.NONE);
				tblItem.setText(prefix + slItem.toString());
				tblItem.setData(slItem.getId());
			}
		}
	}

	private void putAvailableOptimizersInTable(Table tbl, String prefix) {

		putSingleOptimizerInTable(tbl, prefix + ConditionalChoiceOptimizer.class.getSimpleName(), "ConditionalChoice");

		putSingleOptimizerInTable(tbl, prefix + FormulaOptimizer.class.getSimpleName(), "Formula");

		putSingleOptimizerInTable(tbl, prefix + ContradictionOptimizer.class.getSimpleName(), "Contradiction");
	}

	private void putSingleOptimizerInTable(Table tbl, String text, String data) {
		TableItem tblItem = new TableItem(tbl, SWT.NONE);
		tblItem.setText(text);
		tblItem.setData(data);
		tblItem.setChecked(true);
		this.selectedOptimizer.add((String) tblItem.getData());

	}

	private Set<HybridContract> lookupContractsInFolders(List<File> contractFiles) {
		// TODO
		// existence of given folders is already ensured
		// folders are given as absolute paths
		PluginLogger.debug("method 'lookupContractsInFolders' in class 'TransformPage' is not yet implemented");
		PluginLogger.debug("method 'lookupContractsInFolders' in class 'TransformPage' is not yet implemented");

		return new HashSet<HybridContract>();
	}

	private void putAvailableHandlersInTable(Table tbl, String prefix) {

		putSingleHandlerInTable(tbl, prefix + "Arithmetic structure", "Arithmetic");

		putSingleHandlerInTable(tbl, prefix + "Algebraic loop", "Algebraic");

		putSingleHandlerInTable(tbl, prefix + "Controlflow structure", "Controlflow");
	}

	private void putSingleHandlerInTable(Table tbl, String text, String data) {
		TableItem tblItem = new TableItem(tbl, SWT.NONE);
		tblItem.setText(text);
		tblItem.setData(data);
		tblItem.setChecked(true);
		this.selectedHandler.add((String) tblItem.getData());
	}

	public SimulinkModel getModel() {
		return this.model;
	}

	public Transformer getTransformer() {
		return this.transformer;
	}

}
