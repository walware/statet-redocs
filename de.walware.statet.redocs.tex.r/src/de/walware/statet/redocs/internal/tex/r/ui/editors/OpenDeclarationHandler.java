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

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import org.eclipse.core.commands.IHandler2;

import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionHandler;

import de.walware.docmlet.tex.ui.sourceediting.LtxOpenDeclarationHandler;

import de.walware.statet.r.ui.sourceediting.ROpenDeclarationHandler;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;


public class OpenDeclarationHandler extends MultiContentSectionHandler {
	
	
	public OpenDeclarationHandler() {
		super(LtxRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	protected IHandler2 createHandler(final String sectionType) {
		switch (sectionType) {
		case LtxRweaveDocumentContentInfo.LTX:
			return new LtxOpenDeclarationHandler();
		case LtxRweaveDocumentContentInfo.R:
			return new ROpenDeclarationHandler();
		default:
			return null;
		}
	}
	
}
