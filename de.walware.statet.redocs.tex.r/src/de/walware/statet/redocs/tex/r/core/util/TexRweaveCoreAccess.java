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

package de.walware.statet.redocs.tex.r.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCodeStyleSettings;
import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.commands.TexCommand;
import de.walware.docmlet.tex.core.commands.TexCommandSet;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;

import de.walware.statet.redocs.internal.tex.r.core.ISweaveLtxCommands;
import de.walware.statet.redocs.tex.r.core.ITexRweaveCoreAccess;


public class TexRweaveCoreAccess implements ITexRweaveCoreAccess {
	
	
	private static ITexRweaveCoreAccess WORKSPACE_ACCESS= new TexRweaveCoreAccess(
			TexCore.getWorkbenchAccess(), RCore.getWorkbenchAccess() );
	
	public static ITexRweaveCoreAccess combine(final ITexCoreAccess tex, final IRCoreAccess r) {
		if (tex == null) {
			return WORKSPACE_ACCESS;
		}
		if (tex instanceof ITexRweaveCoreAccess) {
			return (ITexRweaveCoreAccess) tex;
		}
		return new TexRweaveCoreAccess(tex, (r != null) ? r : RCore.getWorkbenchAccess());
	}
	
	
	private static final ImList<TexCommand> LTX_SWEAVE_COMMAND_LIST= ImCollections.newList( // ASorted
			ISweaveLtxCommands.SWEAVE_Sexpr_COMMANDS,
			ISweaveLtxCommands.SWEAVE_SweaveOpts_COMMANDS );
	
	private static final WeakHashMap<TexCommandSet, TexCommandSet> COMMAND_CACHE= new WeakHashMap<>();
	
	
	private final ITexCoreAccess texAccess;
	private final IRCoreAccess rAccess;
	
	private TexCommandSet texCommandSetOrg;
	private volatile TexCommandSet texCommandSet;
	
	
	public TexRweaveCoreAccess(final ITexCoreAccess tex, final IRCoreAccess r) {
		if (tex == null) {
			throw new NullPointerException("tex"); //$NON-NLS-1$
		}
		if (r == null) {
			throw new NullPointerException("r"); //$NON-NLS-1$
		}
		this.texAccess= tex;
		this.rAccess= r;
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.rAccess.getPrefs(); // TODO
	}
	
	@Override
	public TexCommandSet getTexCommandSet() {
		if (this.texCommandSetOrg != this.texAccess.getTexCommandSet()) {
			updateTexCommandSet();
		}
		return this.texCommandSet;
	}
	
	private void updateTexCommandSet() {
		synchronized (COMMAND_CACHE) { 
			final TexCommandSet org= this.texAccess.getTexCommandSet();
			if (this.texCommandSetOrg == org) {
				return;
			}
			TexCommandSet set= COMMAND_CACHE.get(org);
			if (set == null) {
				set= new TexCommandSet(addSweaveList(org.getAllLtxCommands()), org.getAllLtxEnvs(),
						org.getLtxTextEnvMap(), org.getLtxTextEnvsASorted(),
						addSweaveMap(org.getLtxTextCommandsASorted()), addSweaveListASorted(org.getLtxTextCommandsASorted()),
						org.getLtxPreambleCommandMap(), org.getLtxPreambleCommandsASorted(),
						org.getLtxMathEnvMap(), org.getLtxMathEnvsASorted(),
						addSweaveMap(org.getLtxMathCommandsASorted()), addSweaveListASorted(org.getLtxMathCommandsASorted()),
						org.getLtxInternEnvMap() );
				COMMAND_CACHE.put(org, set);
			}
			this.texCommandSet= set;
			this.texCommandSetOrg= org;
		}
	}
	
	private Map<String, TexCommand> addSweaveMap(final List<TexCommand> org) {
		final Map<String, TexCommand> map= new IdentityHashMap<>(org.size() + 2);
		for (final TexCommand command : org) {
			map.put(command.getControlWord(), command);
		}
		for (final TexCommand command : LTX_SWEAVE_COMMAND_LIST) {
			map.put(command.getControlWord(), command);
		}
		return Collections.unmodifiableMap(map);
	}
	
	private List<TexCommand> addSweaveList(final List<TexCommand> org) {
		return ImCollections.concatList(org, LTX_SWEAVE_COMMAND_LIST);
	}
	
	private List<TexCommand> addSweaveListASorted(final List<TexCommand> org) {
		final ImList<TexCommand> list= ImCollections.concatList(org, LTX_SWEAVE_COMMAND_LIST,
				(Comparator<TexCommand>) null );
		return list;
	}
	
	
	@Override
	public TexCodeStyleSettings getTexCodeStyle() {
		return this.texAccess.getTexCodeStyle();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rAccess.getRCodeStyle();
	}
	
}
