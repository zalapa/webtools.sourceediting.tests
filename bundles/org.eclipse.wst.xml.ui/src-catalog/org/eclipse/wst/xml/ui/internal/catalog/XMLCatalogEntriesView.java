/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.ui.internal.catalog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.xml.core.internal.catalog.Catalog;
import org.eclipse.wst.xml.core.internal.catalog.CatalogElement;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogElement;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;



public class XMLCatalogEntriesView extends Composite {
	protected Button newButton;
	protected Button editButton;
	protected Button deleteButton;
	protected XMLCatalogTreeViewer tableViewer;
	protected ICatalog workingUserCatalog;
	protected ICatalog systemCatalog;

	// protected boolean isPageEnabled = true;

	public XMLCatalogEntriesView(Composite parent, ICatalog workingUserCatalog, ICatalog systemCatalog) {
		super(parent, SWT.NONE);
		this.workingUserCatalog = workingUserCatalog;
		this.systemCatalog = systemCatalog;

		GridLayout gridLayout = new GridLayout();
		this.setLayout(gridLayout);

		tableViewer = createTableViewer(this);
		tableViewer.setInput("dummy"); //$NON-NLS-1$
		
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202692
		// specifically set size of tree before expanding it
		Point initialSize = tableViewer.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = initialSize.x;
		gridData.heightHint = initialSize.y;
		tableViewer.getControl().setLayoutData(gridData);
		
		tableViewer.expandToLevel(2);
		tableViewer.reveal(XMLCatalogTreeViewer.USER_SPECIFIED_ENTRIES_OBJECT);
		
		ISelectionChangedListener listener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateWidgetEnabledState();
			}
		};
		tableViewer.addSelectionChangedListener(listener);

		createButtons(this);
	}

	public static String removeLeadingSlash(String uri) {
		// remove leading slash from the value to avoid the whole leading
		// slash ambiguity problem
		//       
		if (uri != null) {
			while (uri.startsWith("/") || uri.startsWith("\\")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				uri = uri.substring(1);
			}
		}
		return uri;
	}

	protected XMLCatalogTreeViewer createTableViewer(Composite parent) {
		String headings[] = new String[2];
		headings[0] = XMLCatalogMessages.UI_LABEL_KEY;
		headings[1] = XMLCatalogMessages.UI_LABEL_URI;

		XMLCatalogTreeViewer theTableViewer = new XMLCatalogTreeViewer(parent, workingUserCatalog, systemCatalog);
		return theTableViewer;
	}

	protected void createButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		gridLayout.makeColumnsEqualWidth = true;
		composite.setLayout(gridLayout);

		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 4;

		Button hiddenButton = new Button(composite, SWT.NONE);
		hiddenButton.setLayoutData(gd);
		hiddenButton.setVisible(false);
		hiddenButton.setEnabled(false);

		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == newButton) {
					performNew();
				}
				else if (e.widget == editButton) {
					performEdit();
				}
				else if (e.widget == deleteButton) {
					performDelete();
				}
			}
		};

		// add the "New..." button
		//
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;

		newButton = new Button(composite, SWT.NONE);
		newButton.setText(XMLCatalogMessages.UI_BUTTON_NEW);
		// WorkbenchHelp.setHelp(newButton,
		// XMLBuilderContextIds.XMLP_MAPPING_NEW);
		newButton.setLayoutData(gd);
		newButton.addSelectionListener(selectionListener);

		// add the "Edit..." button
		//
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		editButton = new Button(composite, SWT.NONE);
		editButton.setText(XMLCatalogMessages.UI_BUTTON_EDIT);
		// WorkbenchHelp.setHelp(editButton,
		// XMLBuilderContextIds.XMLP_MAPPING_EDIT);
		editButton.setLayoutData(gd);
		editButton.addSelectionListener(selectionListener);

		// add the "Delete" button
		//
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		deleteButton = new Button(composite, SWT.NONE);
		deleteButton.setText(XMLCatalogMessages.UI_BUTTON_DELETE);
		// WorkbenchHelp.setHelp(deleteButton,
		// XMLBuilderContextIds.XMLP_MAPPING_DELETE);
		deleteButton.setLayoutData(gd);
		deleteButton.addSelectionListener(selectionListener);

		// a cruddy hack so that the PreferenceDialog doesn't close every time
		// we press 'enter'
		//
		getShell().setDefaultButton(hiddenButton);
		updateWidgetEnabledState();
	}

	public void refresh() {
		tableViewer.refresh();// XMLCatalogTreeViewer.USER_SPECIFIED_ENTRIES_OBJECT);
	}

	protected EditCatalogEntryDialog invokeDialog(String title, ICatalogElement entry) {
		Shell shell = XMLUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
		EditCatalogEntryDialog dialog = new EditCatalogEntryDialog(shell, entry);
		dialog.create();
		dialog.getShell().setText(title);
		dialog.setBlockOnOpen(true);
		dialog.open();
		return dialog;
	}

	protected EditCatalogEntryDialog invokeDialog(String title, ICatalog catalog) {
		Shell shell = XMLUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
		EditCatalogEntryDialog dialog = new EditCatalogEntryDialog(shell, catalog);
		dialog.create();
		dialog.getShell().setText(title);
		dialog.setBlockOnOpen(true);
		dialog.open();
		return dialog;
	}


	protected void performNew() {

		// ICatalogEntry newEntry =
		// (ICatalogEntry)workingUserCatalog.createCatalogElement(ICatalogElement.TYPE_ENTRY);
		EditCatalogEntryDialog dialog = invokeDialog(XMLCatalogMessages.UI_LABEL_NEW_DIALOG_TITLE, workingUserCatalog);
		ICatalogElement element = dialog.getCatalogElement();
		if (dialog.getReturnCode() == Window.OK) {
			workingUserCatalog.addCatalogElement(element);
			tableViewer.setSelection(new StructuredSelection(element), true);
			tableViewer.refresh();
		}
	}

	protected void performEdit() {
		ISelection selection = tableViewer.getSelection();
		Object selectedObject = (selection instanceof IStructuredSelection) ? ((IStructuredSelection) selection).getFirstElement() : null;

		if (selectedObject instanceof ICatalogElement) {
			ICatalogElement oldEntry = (ICatalogElement) selectedObject;
			ICatalogElement newEntry = (ICatalogElement) ((CatalogElement) oldEntry).clone();

			EditCatalogEntryDialog dialog = invokeDialog(XMLCatalogMessages.UI_LABEL_EDIT_DIALOG_TITLE, newEntry);
			if (dialog.getReturnCode() == Window.OK) {
				// delete the old value if the 'mapFrom' has changed
				//
				workingUserCatalog.removeCatalogElement(oldEntry);

				// update the new mapping
				//
				workingUserCatalog.addCatalogElement(newEntry);
				tableViewer.setSelection(new StructuredSelection(newEntry));
			}
		}
	}

	protected void performDelete() {
		ISelection selection = tableViewer.getSelection();
		Object selectedObject = (selection instanceof IStructuredSelection) ? ((IStructuredSelection) selection).getFirstElement() : null;

		if (selectedObject instanceof ICatalogElement) {
			ICatalogElement catalogElement = (ICatalogElement) selectedObject;
			workingUserCatalog.removeCatalogElement(catalogElement);
		}
	}

	protected void updateWidgetEnabledState() {
		boolean isEditable = false;
		ISelection selection = tableViewer.getSelection();
		Object selectedObject = (selection instanceof IStructuredSelection) ? ((IStructuredSelection) selection).getFirstElement() : null;

		if (selectedObject instanceof ICatalogElement) {
			ICatalogElement[] elements = ((Catalog) workingUserCatalog).getCatalogElements();
			// dw List entriesList = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				ICatalogElement element = elements[i];
				isEditable = selectedObject.equals(element);
				if (isEditable) {
					break;
				}
			}
		}

		// if (isPageEnabled)
		{
			editButton.setEnabled(isEditable);
			deleteButton.setEnabled(isEditable);
		}
	}

	/*
	 * public void setPageEnabled(boolean enabled) { isPageEnabled = enabled;
	 * 
	 * tableViewer.getControl().setEnabled(isPageEnabled);
	 * 
	 * newButton.setEnabled(isPageEnabled);
	 * editButton.setEnabled(isPageEnabled);
	 * deleteButton.setEnabled(isPageEnabled); }
	 */
	public void updatePage() {
		refresh();
		updateWidgetEnabledState();
	}

	public Viewer getViewer() {
		return tableViewer;
	}


}