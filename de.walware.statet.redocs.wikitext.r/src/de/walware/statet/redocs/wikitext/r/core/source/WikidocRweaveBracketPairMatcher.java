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

package de.walware.statet.redocs.wikitext.r.core.source;

import de.walware.ecommons.ltk.core.util.MultiContentSectionCharPairMatcher;
import de.walware.ecommons.text.ICharPairMatcher;

import de.walware.docmlet.wikitext.core.source.MarkupBracketPairMatcher;
import de.walware.docmlet.wikitext.core.source.WikitextHeuristicTokenScanner;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class WikidocRweaveBracketPairMatcher extends MultiContentSectionCharPairMatcher {
	
	
	public static RBracketPairMatcher createRChunkPairMatcher(final RHeuristicTokenScanner scanner) {
		return new RBracketPairMatcher(scanner,
				new String[] {
					IRDocumentConstants.R_DEFAULT_CONTENT_TYPE,
					IWikitextRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE
				} );
	}
	
	
	public WikidocRweaveBracketPairMatcher() {
		super(WikidocRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	protected ICharPairMatcher createHandler(final String sectionType) {
		switch (sectionType) {
		case WikidocRweaveDocumentContentInfo.WIKITEXT:
			return new MarkupBracketPairMatcher(
					WikitextHeuristicTokenScanner.create(getSections()) );
		case WikidocRweaveDocumentContentInfo.R:
		case WikidocRweaveDocumentContentInfo.R_CHUNK_CONTROL:
			return createRChunkPairMatcher(
					RHeuristicTokenScanner.create(getSections()) );
		default:
			return null;
		}
	}
	
}
