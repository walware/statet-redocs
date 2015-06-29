/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.core.util;

import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;
import de.walware.docmlet.wikitext.core.WikitextCodeStyleSettings;
import de.walware.docmlet.wikitext.core.WikitextCore;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;

import de.walware.statet.redocs.wikitext.r.core.IWikitextRweaveCoreAccess;


public class WikitextRweaveCoreAccess implements IWikitextRweaveCoreAccess {
	
	
	private static IWikitextRweaveCoreAccess WORKSPACE_ACCESS= new WikitextRweaveCoreAccess(
			WikitextCore.getWorkbenchAccess(), RCore.getWorkbenchAccess() );
	
	public static IWikitextRweaveCoreAccess combine(final IWikitextCoreAccess wikitext, final IRCoreAccess r) {
		if (wikitext == null) {
			return WORKSPACE_ACCESS;
		}
		if (wikitext instanceof IWikitextRweaveCoreAccess) {
			return (IWikitextRweaveCoreAccess) wikitext;
		}
		return new WikitextRweaveCoreAccess(wikitext, (r != null) ? r : RCore.getWorkbenchAccess());
	}
	
	
	private final IWikitextCoreAccess wikitextAccess;
	private final IRCoreAccess rAccess;
	
	
	public WikitextRweaveCoreAccess(final IWikitextCoreAccess wikitext, final IRCoreAccess r) {
		if (wikitext == null) {
			throw new NullPointerException("wikitext"); //$NON-NLS-1$
		}
		if (r == null) {
			throw new NullPointerException("r"); //$NON-NLS-1$
		}
		this.wikitextAccess= wikitext;
		this.rAccess= r;
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.rAccess.getPrefs(); // TODO
	}
	
	@Override
	public WikitextCodeStyleSettings getWikitextCodeStyle() {
		return this.wikitextAccess.getWikitextCodeStyle();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rAccess.getRCodeStyle();
	}
	
}
