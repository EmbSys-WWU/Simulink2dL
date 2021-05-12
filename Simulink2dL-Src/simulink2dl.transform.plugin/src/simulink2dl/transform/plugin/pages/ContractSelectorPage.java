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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

public class ContractSelectorPage extends WizardPage {

	private List<File> contracts;
	private ListViewer contractListViewer;
	private ArrayList<String> listContent = new ArrayList<String>();

	public ContractSelectorPage(String pageName) {
		super(pageName);

		this.contracts = new LinkedList<File>();
		setMessage(
				"Choose contracts, which will be applied during optimization. This is optional.\nContracts can be removed by selecting them and pressing [DEL].");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.SCROLL_LINE);

		setControl(composite);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.heightHint = 200;
		contractListViewer = new ListViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		contractListViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// NOP
			}

			@Override
			public void dispose() {
				// NOP
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});

		// files in the fileList can be removed with [DEL]
		contractListViewer.getControl().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.DEL) {
					IStructuredSelection selection = (IStructuredSelection) contractListViewer.getSelection();
					for (Object file : selection.toArray()) {
						listContent.remove(file);
					}
					contractListViewer.setInput(listContent.toArray());
					setPageComplete(validatePage());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		contractListViewer.setInput(listContent.toArray());
		contractListViewer.getList().setLayoutData(data);

		// "Browse..." button
		Button browseModelButton = new Button(composite, SWT.NONE);
		browseModelButton.setText("Browse...");
		browseModelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// FileDialog dialog = new FileDialog(getShell(), SWT.MULTI); // uncomment to
				// select *.slx files instead of dirs
				// String[] filterExt = {"*.slx", "*.slx"};
				// dialog.setFilterExtensions(filterExt);

				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText("Select a contract folder");
				dialog.setFilterPath(System.getProperty("user.dir"));
				String absolutePath = dialog.open();

				// for (String file : fd.getFileNames()) {
				// listContent.add(fd.getFilterPath() + File.separator + file);
				// }

				listContent.add(absolutePath);
				contractListViewer.setInput(listContent.toArray());

				setPageComplete(validatePage());
			}
		});
		data = new GridData();
		data.widthHint = 100;
		data.verticalAlignment = SWT.BOTTOM;
		browseModelButton.setLayoutData(data);
		setVisible(true);
		setPageComplete(validatePage());
	}

	/**
	 * Checks if all given paths are correct.
	 *
	 * @return true if everything is fine
	 */
	private boolean validatePage() {
		Object[] files = (Object[]) contractListViewer.getInput();
		this.contracts.clear();
		for (Object file : files) {
			File f = new File((String) file);
			if (!f.exists()) {
				setErrorMessage("\"" + (String) file + "\" is not a valid File");
				return false;
			}
			this.contracts.add(f);
		}

		setErrorMessage(null);
		return true;
	}

	/**
	 * @return the contracts
	 */
	public List<File> getContracts() {
		return contracts;
	}
}
