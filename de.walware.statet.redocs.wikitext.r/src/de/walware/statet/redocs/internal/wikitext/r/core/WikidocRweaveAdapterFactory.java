/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.core;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.docmlet.wikitext.core.WikitextBuildParticipant;

import de.walware.statet.redocs.internal.wikitext.r.core.model.WikitextRweaveTexBuildParticipant;


public class WikidocRweaveAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class<?>[] {
		WikitextBuildParticipant.class,
	};
	
	
	public WikidocRweaveAdapterFactory() {
	}
	
	
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (WikitextBuildParticipant.class.equals(adapterType)) {
			return new WikitextRweaveTexBuildParticipant();
		}
		return null;
	}
	
}
