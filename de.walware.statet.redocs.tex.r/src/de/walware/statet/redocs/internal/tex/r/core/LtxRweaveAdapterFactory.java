/*=============================================================================#
 # Copyright (c) 2011-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.core;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.docmlet.tex.core.TexBuildParticipant;

import de.walware.statet.redocs.internal.tex.r.model.LtxRweaveTexBuildParticipant;


public class LtxRweaveAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class<?>[] {
		TexBuildParticipant.class,
	};
	
	
	public LtxRweaveAdapterFactory() {
	}
	
	
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (TexBuildParticipant.class.equals(adapterType)) {
			return new LtxRweaveTexBuildParticipant();
		}
		return null;
	}
	
}
