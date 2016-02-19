/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.model;

import java.util.regex.Pattern;

import de.walware.docmlet.tex.core.model.ILtxModelInfo;

import de.walware.statet.redocs.r.core.model.RChunkReconciler;
import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;


class LtxRChunkReconciler extends RChunkReconciler
		<ILtxRweaveSourceUnit, ILtxModelInfo, LtxRweaveSuModelContainer> {
	
	
	private static LtxRChunkReconciler INSTANCE;
	
	static final LtxRChunkReconciler getInstance() {
		synchronized(LtxRChunkReconciler.class) {
			if (INSTANCE == null) {
				INSTANCE= new LtxRChunkReconciler();
			}
			return INSTANCE;
		}
	}
	
	
//	private static final Pattern CHUNK_START_LINE_PATTERN= Pattern.compile("\\A<<(.*?)(?:>>\\p{all}*)?+\\z"); //$NON-NLS-1$
	/**
	 * Regex: <code>\A<<(.*?)(?:>>\p{all}*)?+\s*\z</code>
	 **/
	private static final Pattern CHUNK_START_LINE_PATTERN= Pattern.compile("\\A<<(.*?)(?:>>\\p{all}*)?+\\s*\\z"); //$NON-NLS-1$
	private static final Pattern CHUNK_REF_LINE_PATTERN= CHUNK_START_LINE_PATTERN;
	private static final Pattern CHUNK_END_LINE_PATTERN= Pattern.compile("\\A@\\p{all}*\\z"); //$NON-NLS-1$
	
	
	public LtxRChunkReconciler() {
		super("Ltx", CHUNK_START_LINE_PATTERN, CHUNK_REF_LINE_PATTERN, CHUNK_END_LINE_PATTERN); //$NON-NLS-1$
	}
	
	
}
