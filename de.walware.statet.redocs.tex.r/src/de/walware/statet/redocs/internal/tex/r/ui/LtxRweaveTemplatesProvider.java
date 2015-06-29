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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.internal.tex.r.ui.sourceediting.LtxRweaveTemplateViewerConfigurator;


/**
 * Integrates the R templates into the common StatET template
 * preference page.
 */
public class LtxRweaveTemplatesProvider implements ICodeGenerationTemplatesCategory {
	
	
	public LtxRweaveTemplatesProvider() {
	}
	
	
	@Override
	public String getProjectNatureId() {
		return RProjects.R_NATURE_ID;
	}
	
	@Override
	public TemplateStore getTemplateStore() {
		return RedocsTexRPlugin.getInstance().getCodegenTemplateStore();
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return RedocsTexRPlugin.getInstance().getCodegenTemplateContextTypeRegistry();
	}
	
	@Override
	public SourceEditorViewerConfigurator getEditTemplateDialogConfiguator(final TemplateVariableProcessor processor, final IProject project) {
		final IRProject rProject= RProjects.getRProject(project);
		return new LtxRweaveTemplateViewerConfigurator(TexCore.getWorkbenchAccess(),
				(rProject != null) ? rProject : RCore.getWorkbenchAccess(),
				processor );
	}
	
}
