/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateCategoryConfiguration;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateContribution;
import de.walware.ecommons.ltk.ui.templates.config.TemplateStoreContribution;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.internal.tex.r.core.LtxRweaveTemplatesContextType;
import de.walware.statet.redocs.internal.tex.r.ui.sourceediting.LtxRweaveTemplateViewerConfigurator;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


public class NewDocTemplateCategoryConfiguration implements ITemplateCategoryConfiguration {
	
	
	public static final String PREF_QUALIFIER= TexRweaveUI.PLUGIN_ID + "/codegen"; //$NON-NLS-1$
	
	public static final String NEWDOC_DEFAULT_NAME_KEY= "NewDoc.Default.name"; //$NON-NLS-1$
	public static final Preference<String> NEWDOC_DEFAULT_NAME_PREF= new Preference.StringPref(
			PREF_QUALIFIER, NEWDOC_DEFAULT_NAME_KEY);
	
	
	public NewDocTemplateCategoryConfiguration() {
	}
	
	
	@Override
	public ITemplateContribution getTemplates() {
		return new TemplateStoreContribution(RedocsTexRPlugin.getInstance().getDocTemplateStore());
	}
	
	@Override
	public Preference<String> getDefaultPref() {
		return NEWDOC_DEFAULT_NAME_PREF;
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return RedocsTexRPlugin.getInstance().getDocTemplateContextTypeRegistry();
	}
	
	@Override
	public String getDefaultContextTypeId() {
		return LtxRweaveTemplatesContextType.NEWDOC_CONTEXTTYPE;
	}
	
	@Override
	public String getViewerConfigId(final TemplatePersistenceData data) {
		return LtxRweaveTemplatesContextType.NEWDOC_CONTEXTTYPE;
	}
	
	@Override
	public SourceEditorViewerConfigurator createViewerConfiguator(final String viewerConfigId,
			final TemplatePersistenceData data,
			final TemplateVariableProcessor templateProcessor, final IProject project) {
		final IRProject rProject= RProjects.getRProject(project);
		return new LtxRweaveTemplateViewerConfigurator(TexCore.getWorkbenchAccess(),
				(rProject != null) ? rProject : RCore.getWorkbenchAccess(),
				templateProcessor );
	}
	
}
