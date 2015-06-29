/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui;

import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.ecommons.ui.dialogs.groups.Layouter;

import de.walware.statet.ext.ui.wizards.NewElementWizardPage;


/**
 * The "New" wizard page allows setting the container for
 * the new file as well as the file name. The page
 * will only accept file name without the extension or
 * with the extension that matches the expected one (r).
 */
public class NewLtxRweaveDocCreationWizardPage extends NewElementWizardPage {
	
	
	private static final String fgDefaultExtension= ".Rnw"; //$NON-NLS-1$
	
	
	ResourceGroup resourceGroup;
	
	
	public NewLtxRweaveDocCreationWizardPage(final IStructuredSelection selection) {
		super("NewLtxRweaveDocCreationWizardPage", selection); //$NON-NLS-1$
		
		setTitle(Messages.NewDocWizardPage_title);
		setDescription(Messages.NewDocWizardPage_description);
		
		this.resourceGroup= new ResourceGroup(fgDefaultExtension);
	}
	
	@Override
	protected void createContents(final Layouter layouter) {
		this.resourceGroup.createGroup(layouter);
	}
	
	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible) {
			this.resourceGroup.setFocus();
		}
	}
	
	public void saveSettings() {
		this.resourceGroup.saveSettings();
	}
	
	@Override
	protected void validatePage() {
		updateStatus(this.resourceGroup.validate());
	}
	
}
