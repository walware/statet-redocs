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

package de.walware.statet.redocs.internal.wikitext.r.markdown;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.redocs.internal.wikitext.r.markdown.core.RMarkdownLanguage;
import de.walware.statet.redocs.internal.wikitext.r.markdown.ui.NewDocTemplateCategoryConfiguration;


public class MarkdownRweavePreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public MarkdownRweavePreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		
		{	final IEclipsePreferences node= scope.getNode(MarkdownRweavePlugin.PLUGIN_ID + "/markup/" + RMarkdownLanguage.WEAVE_MARKUP_LANGUAGE_NAME); //$NON-NLS-1$
			node.put("MarkupConfig.Workbench.config", //$NON-NLS-1$
					"Markdown:yaml_metadata_block;" + //$NON-NLS-1$
					"tex_math_dollars;" + //$NON-NLS-1$
					"tex_math_single_backslash" ); //$NON-NLS-1$
		}
		{	final IEclipsePreferences node= scope.getNode(MarkdownRweavePlugin.TEMPLATES_QUALIFIER);
			node.put(NewDocTemplateCategoryConfiguration.NEWDOC_DEFAULT_NAME_KEY,
					MarkdownRweavePlugin.NEWDOC_TEMPLATE_CATEGORY_ID + ':' + "Article" ); //$NON-NLS-1$
		}
	}
	
}
