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

package de.walware.statet.redocs.internal.wikitext.r.commonmark;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.redocs.internal.wikitext.r.commonmark.core.RCommonmarkLanguage;
import de.walware.statet.redocs.internal.wikitext.r.commonmark.ui.NewDocTemplateCategoryConfiguration;


public class CommonmarkRweavePreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public CommonmarkRweavePreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		
		{	final IEclipsePreferences node= scope.getNode(CommonmarkRweavePlugin.PLUGIN_ID + "/markup/" + RCommonmarkLanguage.COMMONMARK_RWEAVE_LANGUAGE_NAME); //$NON-NLS-1$
			node.put("MarkupConfig.Workbench.config", //$NON-NLS-1$
					"Commonmark:yaml_metadata_block;" + //$NON-NLS-1$
					"tex_math_dollars;" + //$NON-NLS-1$
					"tex_math_single_backslash;" + //$NON-NLS-1$
					"Paragraph+Header=Blank;" + //$NON-NLS-1$
					"Paragraph+Blockquote=Blank;" + //$NON-NLS-1$
					"Superscript=^;Subscript=~" ); //$NON-NLS-1$
		}
		{	final IEclipsePreferences node= scope.getNode(CommonmarkRweavePlugin.TEMPLATES_QUALIFIER);
			node.put(NewDocTemplateCategoryConfiguration.NEWDOC_DEFAULT_NAME_KEY,
					CommonmarkRweavePlugin.NEWDOC_TEMPLATE_CATEGORY_ID + ':' + "Article" ); //$NON-NLS-1$
		}
	}
	
}
