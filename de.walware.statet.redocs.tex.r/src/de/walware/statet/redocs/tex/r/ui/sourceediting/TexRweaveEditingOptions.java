/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.tex.r.ui.sourceediting;

import de.walware.ecommons.preferences.core.Preference.BooleanPref;

import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


public class TexRweaveEditingOptions {
	
	
	public static final String LTX_EDITOR_NODE= TexRweaveUI.PLUGIN_ID + "/editor/Ltx"; //$NON-NLS-1$
	
	
	public static final String LTX_SPELLCHECK_ENABLED_PREF_KEY= "SpellCheck.enabled"; //$NON-NLS-1$
	public static final BooleanPref LTX_SPELLCHECK_ENABLED_PREF= new BooleanPref(
			LTX_EDITOR_NODE, LTX_SPELLCHECK_ENABLED_PREF_KEY);
	
}
