/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.tex.r.core.source;

import de.walware.ecommons.ltk.core.util.MultiContentSectionCharPairMatcher;
import de.walware.ecommons.text.ICharPairMatcher;

import de.walware.docmlet.tex.core.source.LtxBracketPairMatcher;
import de.walware.docmlet.tex.core.source.LtxHeuristicTokenScanner;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class LtxRweaveBracketPairMatcher extends MultiContentSectionCharPairMatcher {
	
	
	public static RBracketPairMatcher createRChunkPairMatcher(final RHeuristicTokenScanner scanner) {
		return new RBracketPairMatcher(scanner,
				new String[] {
					IRDocumentConstants.R_DEFAULT_CONTENT_TYPE,
					ITexRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE
				} );
	}
	
	
	public LtxRweaveBracketPairMatcher() {
		super(LtxRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	protected ICharPairMatcher createHandler(final String sectionType) {
		switch (sectionType) {
		case LtxRweaveDocumentContentInfo.LTX:
			return new LtxBracketPairMatcher(
					LtxHeuristicTokenScanner.create(getSections()) );
		case LtxRweaveDocumentContentInfo.R:
		case LtxRweaveDocumentContentInfo.R_CHUNK_CONTROL:
			return createRChunkPairMatcher(
					RHeuristicTokenScanner.create(getSections()) );
		default:
			return null;
		}
	}
	
}
