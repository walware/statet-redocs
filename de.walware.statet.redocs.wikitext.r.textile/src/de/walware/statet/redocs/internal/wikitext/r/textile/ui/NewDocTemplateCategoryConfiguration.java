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

package de.walware.statet.redocs.internal.wikitext.r.textile.ui;

import static de.walware.statet.redocs.internal.wikitext.r.textile.core.RTextileLanguage.WEAVE_MARKUP_LANGUAGE_NAME;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateCategoryConfiguration;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateContribution;
import de.walware.ecommons.ltk.ui.templates.config.TemplateStoreContribution;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringPref;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.wikitext.core.WikitextCore;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;

import de.walware.statet.redocs.internal.wikitext.r.textile.TextileRweavePlugin;
import de.walware.statet.redocs.wikitext.r.core.source.WikitextRweaveTemplatesContextType;
import de.walware.statet.redocs.wikitext.r.ui.codegen.CodeGeneration;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveTemplateViewerConfigurator;


public class NewDocTemplateCategoryConfiguration
		implements ITemplateCategoryConfiguration {
	
	
	public static final String NEWDOC_DEFAULT_NAME_KEY= "NewDoc.Default.name"; //$NON-NLS-1$
	public static final Preference<String> NEWDOC_DEFAULT_NAME_PREF= new StringPref(
			TextileRweavePlugin.TEMPLATES_QUALIFIER, NEWDOC_DEFAULT_NAME_KEY );
	
	public static final String TEMPLATES_NEWDOC_CONTEXTTYPE= WEAVE_MARKUP_LANGUAGE_NAME +
			WikitextRweaveTemplatesContextType.NEWDOC_CONTEXTTYPE_SUFFIX;
	
	
	public NewDocTemplateCategoryConfiguration() {
	}
	
	
	@Override
	public ITemplateContribution getTemplates() {
		return new TemplateStoreContribution(CodeGeneration.getDocTemplateStore());
	}
	
	@Override
	public Preference<String> getDefaultPref() {
		return NEWDOC_DEFAULT_NAME_PREF;
	}
	
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		return CodeGeneration.getDocContextTypeRegistry();
	}
	
	@Override
	public String getDefaultContextTypeId() {
		return NewDocTemplateCategoryConfiguration.TEMPLATES_NEWDOC_CONTEXTTYPE;
	}
	
	@Override
	public String getViewerConfigId(final TemplatePersistenceData data) {
		return NewDocTemplateCategoryConfiguration.TEMPLATES_NEWDOC_CONTEXTTYPE;
	}
	
	@Override
	public SourceEditorViewerConfigurator createViewerConfiguator(final String viewerConfigId,
			final TemplatePersistenceData data, final TemplateVariableProcessor templateProcessor,
			final IProject project) {
		final IRProject rProject= RProjects.getRProject(project);
		return new WikidocRweaveTemplateViewerConfigurator(
				new TextileRweaveDocumentSetupParticipant(true),
				WikitextCore.getWorkbenchAccess(), (rProject != null) ? rProject : RCore.getWorkbenchAccess(),
				templateProcessor );
	}
	
}
