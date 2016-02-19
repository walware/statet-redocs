/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.walware.docmlet.base.ui.processing.DocProcessingUI;
import de.walware.docmlet.base.ui.processing.DocProcessingUI.CommonFlags;
import de.walware.docmlet.base.ui.processing.actions.RunConfigsMenuContribution;

import de.walware.statet.redocs.r.ui.RedocsRUI;


public class WikitextRweaveConfigsMenuContribution extends RunConfigsMenuContribution {
	
	
	private static final String PROCESS_TEX_COMMAND_ID= "de.walware.docmlet.tex.commands.ProcessTexDefault"; //$NON-NLS-1$
	
	
	private class ThisConfigContribution extends ConfigContribution {
		
		
		public ThisConfigContribution(final Image icon, final String label,
				final ILaunchConfiguration configuration) {
			super(icon, label, configuration);
		}
		
		
		@Override
		protected void addLaunchItems(final Menu menu) {
			String buildDetail= null;
			String weaveDetail= null;
			String produceDetail= null;
			try {
				final ILaunchConfiguration config= getConfiguration();
				final IFile file= getFile();
				final String sourceInputExt= resolveFormatExt(WikitextRweaveConfig.SOURCE_FORMAT,
						file.getFileExtension() );
				final boolean weaveEnabled= config.getAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, false);
				final String weaveInputExt= sourceInputExt;
				final String weaveOutputExt= resolveFormatExt(WikitextRweaveConfig.getFormat(
							WikitextRweaveConfig.WEAVE_OUTPUT_FORMATS, null,
							config.getAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME, (String) null) ),
						weaveInputExt );
				final boolean produceEnabled= config.getAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, false);
				final String produceInputExt= (weaveEnabled) ? weaveOutputExt : sourceInputExt;
				final String produceOutputExt= resolveFormatExt(WikitextRweaveConfig.getFormat(
							WikitextRweaveConfig.PRODUCE_OUTPUT_FORMATS, null,
							config.getAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME, (String) null) ),
						produceInputExt );
				
				if (weaveEnabled | produceEnabled) {
					buildDetail= createDetail(
							(weaveEnabled) ? weaveInputExt : produceInputExt,
							(produceEnabled) ? produceOutputExt : weaveOutputExt );
				}
				weaveDetail= createDetail(weaveInputExt, weaveOutputExt);
				produceDetail= createDetail(produceInputExt, produceOutputExt);
			} catch (final CoreException e) {
			}
			
			addLaunchItem(menu, CommonFlags.PROCESS_AND_PREVIEW, null, true,
					DocProcessingUI.PROCESS_AND_PREVIEW_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.PROCESS, buildDetail, (buildDetail != null),
					DocProcessingUI.PROCESS_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_PROCESS_HELP_CONTEXT_ID );
			
			new MenuItem(menu, SWT.SEPARATOR);
			
			addLaunchItem(menu, CommonFlags.WEAVE, weaveDetail, (weaveDetail != null),
					RedocsRUI.PROCESS_WEAVE_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_STEP_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.PRODUCE_OUTPUT, produceDetail, (produceDetail != null),
					PROCESS_TEX_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_STEP_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.OPEN_OUTPUT, null, true,
					DocProcessingUI.PREVIEW_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_PREVIEW_HELP_CONTEXT_ID );
			
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
	}
	
	
	public WikitextRweaveConfigsMenuContribution() {
		super(); // use dynamically content type of editor
	}
	
	
	@Override
	protected ConfigContribution createConfigContribution(final Image icon, final StringBuilder label,
			final ILaunchConfiguration configuration) {
		return new ThisConfigContribution(icon, label.toString(), configuration);
	}
	
}
