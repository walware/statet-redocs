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

package de.walware.statet.redocs.internal.wikitext.r.textile;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.redocs.internal.wikitext.r.textile.core.RTextileLanguage;
import de.walware.statet.redocs.internal.wikitext.r.textile.ui.NewDocTemplateCategoryConfiguration;


public class TextileRweavePreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public TextileRweavePreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		
		{	final IEclipsePreferences node= scope.getNode(TextileRweavePlugin.PLUGIN_ID + "/markup/" + RTextileLanguage.WEAVE_MARKUP_LANGUAGE_NAME); //$NON-NLS-1$
			node.put("MarkupConfig.Workbench.config", "Textile:"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		{	final IEclipsePreferences node= scope.getNode(TextileRweavePlugin.TEMPLATES_QUALIFIER);
			node.put(NewDocTemplateCategoryConfiguration.NEWDOC_DEFAULT_NAME_KEY,
					TextileRweavePlugin.NEWDOC_TEMPLATE_CATEGORY_ID + ':' + "Article" ); //$NON-NLS-1$
		}
	}
	
}
