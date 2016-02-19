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

package de.walware.statet.redocs.internal.wikitext.r.ui.config;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikitextRweaveEditingSettings;


public class WikitextRweaveUIPreferenceInitializer extends AbstractPreferenceInitializer {
	
	
	public WikitextRweaveUIPreferenceInitializer() {
	}
	
	
	@Override
	public void initializeDefaultPreferences() {
		final IScopeContext scope= DefaultScope.INSTANCE;
		
		{	final IEclipsePreferences node= scope.getNode(WikitextRweaveEditingSettings.WIKIDOC_EDITOR_NODE);
			node.putBoolean(WikitextRweaveEditingSettings.SPELLCHECK_ENABLED_PREF_KEY, false);
		}
	}
	
}
