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

package de.walware.statet.redocs.internal.tex.r.ui.config;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.redocs.internal.tex.r.ui.LtxRweaveTemplates;
import de.walware.statet.redocs.internal.tex.r.ui.NewDocTemplateCategoryConfiguration;
import de.walware.statet.redocs.tex.r.ui.sourceediting.TexRweaveEditingOptions;


public class TexRweaveUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public TexRweaveUIPreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		
		{	final IEclipsePreferences node= scope.getNode(TexRweaveEditingOptions.LTX_EDITOR_NODE);
			node.putBoolean(TexRweaveEditingOptions.LTX_SPELLCHECK_ENABLED_PREF_KEY, false);
		}
		{	final IEclipsePreferences node= scope.getNode(NewDocTemplateCategoryConfiguration.PREF_QUALIFIER);
			node.put(NewDocTemplateCategoryConfiguration.NEWDOC_DEFAULT_NAME_KEY,
					LtxRweaveTemplates.NEWDOC_TEMPLATE_CATEGORY_ID + ':' + "Article" ); //$NON-NLS-1$
		}
	}
	
}
