/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.docmlet.base.ui.processing.DocProcessingConfigIOStepTab;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigMainTab;
import de.walware.docmlet.base.ui.processing.operations.DocProcessingConfigOpenFileSetting;

import de.walware.statet.redocs.r.ui.RedocsRUIResources;
import de.walware.statet.redocs.r.ui.processing.RWeaveDocProcessingConfig;
import de.walware.statet.redocs.r.ui.processing.RunRCmdToolOperationSettings;
import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperation;
import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperationSettings;


public class WeaveTab extends DocProcessingConfigIOStepTab {
	
	
	private final DocProcessingConfigOpenFileSetting openResult;
	
	
	public WeaveTab(final DocProcessingConfigMainTab mainTab) {
		super(mainTab, RWeaveDocProcessingConfig.WEAVE_ATTR_QUALIFIER);
		
		setInput(WikitextRweaveConfig.SOURCE_FORMAT, null);
		setAvailableOutputFormats(WikitextRweaveConfig.WEAVE_OUTPUT_FORMATS,
				WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
		
		setAvailableOperations(ImCollections.newList(
				new RunRConsoleSnippetOperationSettings(),
				new RunRCmdToolOperationSettings() ));
		
		final Realm realm= getRealm();
		this.openResult= new DocProcessingConfigOpenFileSetting(
				RWeaveDocProcessingConfig.WEAVE_OPEN_RESULT_ATTR_NAME, realm );
	}
	
	
	@Override
	public Image getImage() {
		return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_RWEAVE_IMAGE_ID);
	}
	
	@Override
	public String getName() {
		return createName(Messages.WeaveTab_name);
	}
	
	@Override
	public String getLabel() {
		return Messages.Weave_label;
	}
	
	
	private void updateInput() {
		setInput(WikitextRweaveConfig.SOURCE_FORMAT, getMainTab().getSourceFile());
	}
	
	
	@Override
	protected void addControls(final Composite parent) {
		updateInput();
		
		super.addControls(parent);
		
		{	final Composite group= createPostGroup(parent);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}
	
	@Override
	protected Composite createPostGroup(final Composite parent) {
		final Composite group= super.createPostGroup(parent);
		group.setLayout(LayoutUtil.createGroupGrid(2));
		
		{	final ComboViewer viewer= this.openResult.createControls(group, Messages.WeaveTab_OpenResult_label);
			viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc) {
		super.addBindings(dbc);
		
		this.openResult.addBindings(dbc);
	}
	
	
	@Override
	protected String getDefaultOperationId() {
		return RunRConsoleSnippetOperation.ID;
	}
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		super.doInitialize(configuration);
		
		try {
			this.openResult.load(configuration);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		super.doSave(configuration);
		
		this.openResult.save(configuration);
	}
	
}
