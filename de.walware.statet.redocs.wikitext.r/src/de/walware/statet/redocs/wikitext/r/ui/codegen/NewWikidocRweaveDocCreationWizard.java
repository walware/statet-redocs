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

package de.walware.statet.redocs.wikitext.r.ui.codegen;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.NewDocTemplateGenerateWizardPage;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil.EvaluatedTemplate;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.ext.ui.wizards.NewElementWizard;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.internal.wikitext.r.ui.Messages;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveUI;


public abstract class NewWikidocRweaveDocCreationWizard extends NewElementWizard {
	
	
	private static class NewRweaveFileCreator extends NewFileCreator {
		
		private final Template template;
		
		public NewRweaveFileCreator(final IPath containerPath, final String resourceName,
				final IContentType contentType, final Template template) {
			super(containerPath, resourceName, contentType);
			this.template= template;
		}
		
		@Override
		protected String getInitialFileContent(final IFile newFileHandle, final SubMonitor m) {
			final String lineDelimiter= TextUtil.getLineDelimiter(newFileHandle.getProject());
			final ISourceUnit su= LTK.getSourceUnitManager().getSourceUnit(
					LTK.PERSISTENCE_CONTEXT, newFileHandle, getContentType(newFileHandle),
					true, m );
			try {
				final EvaluatedTemplate data= CodeGeneration.getNewDocContent(su, lineDelimiter,
						this.template );
				if (data != null) {
					this.fInitialSelection= data.getRegionToSelect();
					return data.getContent();
				}
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID, 0,
						Messages.NewDocWizard_error_ApplyTemplate_message,
						e ));
			}
			finally {
				if (su != null) {
					su.disconnect(m);
				}
			}
			return null;
		}
		
	}
	
	
	private final IContentType contentType;
	
	private NewWikidocRweaveDocCreationWizardPage firstPage;
	private NewFileCreator newDocFile;
	private NewDocTemplateGenerateWizardPage templatePage;
	
	
	public NewWikidocRweaveDocCreationWizard(final IContentType contentType) {
		this.contentType= contentType;
		setDialogSettings(DialogUtil.getDialogSettings(RedocsWikitextRPlugin.getInstance(), "NewElementWizard")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(RedocsWikitextRPlugin.getInstance().getImageRegistry().getDescriptor(
				RedocsWikitextRPlugin.WIZBAN_NEW_WIKIDOCRWEAVE_FILE_IMAGE_ID ));
	}
	
	
	@Override
	public void addPages() {
		super.addPages();
		this.firstPage= createFirstPage(getSelection());
		addPage(this.firstPage);
		this.templatePage= createTemplatePage();
		addPage(this.templatePage);
	}
	
	protected abstract NewWikidocRweaveDocCreationWizardPage createFirstPage(IStructuredSelection selection);
	
	protected abstract NewDocTemplateGenerateWizardPage createTemplatePage();
	
	@Override // for lazy loading
	public void createPageControls(final Composite pageContainer) {
		this.firstPage.createControl(pageContainer);
	}
	
	@Override
	protected ISchedulingRule getSchedulingRule() {
		final ISchedulingRule rule= createRule(this.newDocFile.getFileHandle());
		if (rule != null) {
			return rule;
		}
		
		return super.getSchedulingRule();
	}
	
	@Override
	public boolean performFinish() {
		// befor super, so it can be used in getSchedulingRule
		this.newDocFile= new NewRweaveFileCreator(
				this.firstPage.resourceGroup.getContainerFullPath(),
				this.firstPage.resourceGroup.getResourceName(),
				this.contentType,
				this.templatePage.getTemplate() );
		
		final boolean result= super.performFinish();
		
		final IFile newFile= this.newDocFile.getFileHandle();
		if (result && newFile != null) {
			// select and open file
			selectAndReveal(newFile);
			openResource(this.newDocFile);
		}
		
		return result;
	}
	
	@Override
	protected void doFinish(final IProgressMonitor monitor) throws InterruptedException, CoreException, InvocationTargetException {
		final SubMonitor m= SubMonitor.convert(monitor, Messages.NewDocWizard_Task_label, 100 + 10);
		try {
			this.newDocFile.createFile(m.newChild(100));
			
			this.firstPage.saveSettings();
			m.worked(10);
		}
		finally {
			m.done();
		}
	}
	
}
