/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.commands.TexCommand;
import de.walware.docmlet.tex.core.commands.TexCommandSet;
import de.walware.docmlet.tex.core.util.TexCoreAccessWrapper;

import de.walware.statet.redocs.internal.tex.r.core.ISweaveLtxCommands;


/**
 * Enriched TexCoreAccess for TexRweave
 */
public class TexRweaveCoreAccessWrapper extends TexCoreAccessWrapper {
	
	
	private static final ImList<TexCommand> LTX_SWEAVE_COMMAND_LIST= ImCollections.newList( // ASorted
			ISweaveLtxCommands.SWEAVE_Sexpr_COMMANDS,
			ISweaveLtxCommands.SWEAVE_SweaveOpts_COMMANDS );
	
	private static final WeakHashMap<TexCommandSet, TexCommandSet> COMMAND_CACHE= new WeakHashMap<>();
	
	
	private TexCommandSet texCommandSetOrg;
	private volatile TexCommandSet texCommandSet;
	
	
	public TexRweaveCoreAccessWrapper(final ITexCoreAccess texCoreAccess) {
		super(texCoreAccess);
	}
	
	
	@Override
	public TexCommandSet getTexCommandSet() {
		if (this.texCommandSetOrg != super.getTexCommandSet()) {
			updateTexCommandSet();
		}
		return this.texCommandSet;
	}
	
	private void updateTexCommandSet() {
		synchronized (COMMAND_CACHE) { 
			final TexCommandSet org= super.getTexCommandSet();
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
	
}
