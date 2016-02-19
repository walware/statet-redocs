/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui;

import org.eclipse.core.filesystem.IFileStore;

import de.walware.ecommons.ltk.core.impl.AbstractEditorSourceUnitFactory;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;

import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;

import de.walware.statet.redocs.internal.wikitext.r.core.model.WikidocRweaveEditorWorkingCopy;


public final class WikidocRweaveEditorUnitFactory extends AbstractEditorSourceUnitFactory {
	
	
	public WikidocRweaveEditorUnitFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IWorkspaceSourceUnit su) {
		return new WikidocRweaveEditorWorkingCopy((IRWorkspaceSourceUnit) su);
	}
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFileStore file) {
		return null;
	}
	
}
