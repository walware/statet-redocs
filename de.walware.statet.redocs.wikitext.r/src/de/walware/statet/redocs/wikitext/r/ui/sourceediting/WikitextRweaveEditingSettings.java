/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.ui.sourceediting;

import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveUI;


public class WikitextRweaveEditingSettings {
	
	
	public static final String WIKIDOC_EDITOR_NODE= WikitextRweaveUI.PLUGIN_ID + "/editor/Doc"; //$NON-NLS-1$
	
	
	public static final String SPELLCHECK_ENABLED_PREF_KEY= "SpellCheck.enabled"; //$NON-NLS-1$
	public static final BooleanPref WIKIDOC_SPELLCHECK_ENABLED_PREF= new BooleanPref(
			WIKIDOC_EDITOR_NODE, SPELLCHECK_ENABLED_PREF_KEY); 
	
}
