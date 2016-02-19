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

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.BUILDTEX_TYPE_DISABLED;

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
import de.walware.statet.redocs.tex.r.core.TexRweaveCore;


public class TexRweaveConfigsMenuContribution extends RunConfigsMenuContribution {
	
	
	private static final String PROCESS_TEX_COMMAND_ID= "de.walware.docmlet.tex.commands.ProcessTexDefault"; //$NON-NLS-1$
	
	
	private class ThisConfigContribution extends ConfigContribution {
		
		
		public ThisConfigContribution(final Image image, final String label,
				final ILaunchConfiguration configuration) {
			super(image, label, configuration);
		}
		
		
		@Override
		protected void addLaunchItems(final Menu menu) {
			boolean weaveDisabled= false;
			boolean texDisabled= false;
			String processDetail= null;
			String weaveDetail= null;
			String texDetail= null;
			
			try {
				final ILaunchConfiguration config= getConfiguration();
				final String attribute= config.getAttribute(RweaveTab.ATTR_SWEAVE_ID, (String) null);
				if (attribute != null && !attribute.isEmpty()) {
					weaveDetail= "  (Rnw > TeX)"; //$NON-NLS-1$
				}
				else {
					weaveDisabled= true;
				}
				
				if (config.getAttribute(TexTab.ATTR_BUILDTEX_TYPE, -2) > BUILDTEX_TYPE_DISABLED) {
					final String format= config.getAttribute(TexTab.ATTR_BUILDTEX_FORMAT, (String) null);
					if (format != null) {
						texDetail= "  (TeX > " + format + ")"; //$NON-NLS-1$ //$NON-NLS-2$
						processDetail= "  (Rnw > " + format + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else {
					texDisabled= true;
				}
			} catch (final CoreException e) {
			}
			
			addLaunchItem(menu, CommonFlags.PROCESS_AND_PREVIEW, null, true,
					DocProcessingUI.PROCESS_AND_PREVIEW_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.PROCESS, processDetail, true,
					DocProcessingUI.PROCESS_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_PROCESS_HELP_CONTEXT_ID );
			
			new MenuItem(menu, SWT.SEPARATOR);
			
			addLaunchItem(menu, CommonFlags.WEAVE, weaveDetail, !weaveDisabled,
					RedocsRUI.PROCESS_WEAVE_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_STEP_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.PRODUCE_OUTPUT, texDetail, !texDisabled,
					PROCESS_TEX_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_STEP_HELP_CONTEXT_ID );
			addLaunchItem(menu, CommonFlags.OPEN_OUTPUT, null, true,
					DocProcessingUI.PREVIEW_DOC_DEFAULT_COMMAND_ID,
					DocProcessingUI.ACTIONS_RUN_CONFIG_PREVIEW_HELP_CONTEXT_ID );
			
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
	}
	
	
	public TexRweaveConfigsMenuContribution() {
		super(TexRweaveCore.LTX_R_CONTENT_TYPE);
	}
	
	
	@Override
	protected ConfigContribution createConfigContribution(final Image icon, final StringBuilder label,
			final ILaunchConfiguration configuration) {
		return new ThisConfigContribution(icon, label.toString(), configuration);
	}
	
}
