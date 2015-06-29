/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.collections.ImCollections;

import de.walware.docmlet.base.ui.processing.DocProcessingConfig.Format;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigIOStepTab;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigMainTab;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigStepTab;
import de.walware.docmlet.base.ui.processing.operations.RunExternalProgramOperationSettings;

import de.walware.statet.redocs.r.ui.RedocsRUIResources;
import de.walware.statet.redocs.r.ui.processing.RunRCmdToolOperationSettings;
import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperation;
import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperationSettings;


public class ProduceTab extends DocProcessingConfigIOStepTab
		implements DocProcessingConfigStepTab.Listener {
	
	
	private final DocProcessingConfigIOStepTab weaveTab;
	
	
	public ProduceTab(final DocProcessingConfigMainTab mainTab,
			final DocProcessingConfigIOStepTab weaveTab) {
		super(mainTab, WikitextRweaveConfig.PRODUCE_ATTR_QUALIFIER);
		
		this.weaveTab= weaveTab;
		this.weaveTab.addListener(this);
		
		setInput(WikitextRweaveConfig.SOURCE_FORMAT, null);
		setAvailableOutputFormats(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMATS,
				WikitextRweaveConfig.AUTO_YAML_FORMAT_KEY );
		
		setAvailableOperations(ImCollections.newList(
				new RunRConsoleSnippetOperationSettings(),
				new RunRCmdToolOperationSettings(),
				new RunExternalProgramOperationSettings() ));
		
		this.weaveTab.getOutputFileValue().addValueChangeListener(this);
	}
	
	
	@Override
	public Image getImage() {
		return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_BUILDTEX_IMAGE_ID);
	}
	
	@Override
	public String getName() {
		return createName(Messages.ProduceTab_name);
	}
	
	@Override
	public String getLabel() {
		return Messages.Produce_label;
	}
	
	@Override
	public void changed(final DocProcessingConfigStepTab source) {
		if (source == this.weaveTab) {
			updateInput();
		}
	}
	
	@Override
	public void handleValueChange(final ValueChangeEvent event) {
		if (event.getObservable() == this.weaveTab.getOutputFileValue()
				&& this.weaveTab.isEnabled() ) {
			updateInput();
			return;
		}
		super.handleValueChange(event);
	}
	
	private void updateInput() {
		if (this.weaveTab.isEnabled()) {
			final Format format= this.weaveTab.getOutputFormat();
			setInput((format != null) ?
							WikitextRweaveConfig.createWeaveOutputFormat(format) :
							null,
					this.weaveTab.getOutputFile() );
		}
		else {
			setInput(WikitextRweaveConfig.SOURCE_FORMAT,
					getMainTab().getSourceFile() );
		}
	}
	
	@Override
	protected IFile getInputFile(final Format format) throws CoreException {
		if (this.weaveTab.isEnabled()) {
			return this.weaveTab.getOutputFile();
		}
		return null;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc) {
		super.addBindings(dbc);
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
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		super.doSave(configuration);
	}
	
}
