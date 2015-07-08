/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.core.source;

import static de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner.R_CHUNK_BASE_TYPE;

import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;

import de.walware.docmlet.wikitext.core.markup.IMarkupLanguage;
import de.walware.docmlet.wikitext.core.source.EmbeddingAttributes;
import de.walware.docmlet.wikitext.core.source.WikitextPartitionNodeType;
import de.walware.docmlet.wikitext.core.source.extdoc.WikidocPartitionNodeScanner;

import de.walware.statet.r.core.source.RPartitionNodeType;

import de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner;


public class WikidocRweavePartitionNodeScanner extends WikidocPartitionNodeScanner {
	
	
	private static final WikitextPartitionNodeType R_CHUNK_WIKITEXT_TYPE= new WikitextPartitionNodeType();
	
	private static final WikitextPartitionNodeType R_INLINE_WIKITEXT_TYPE= new WikitextPartitionNodeType();
	
	
	private AbstractRChunkPartitionNodeScanner rScanner;
	
	
	public WikidocRweavePartitionNodeScanner(final IMarkupLanguage markupLanguage) {
		super(markupLanguage);
	}
	
	public WikidocRweavePartitionNodeScanner(final IMarkupLanguage markupLanguage,
			final int markupLanguageMode) {
		super(markupLanguage, markupLanguageMode);
	}
	
	
	@Override
	protected void init() {
		{	final IMarkupLanguage markupLanguage= getMarkupLanguage();
			if (markupLanguage instanceof IRweaveMarkupLanguage) {
				final IRweaveMarkupLanguage rweaveLanguage= (IRweaveMarkupLanguage) markupLanguage;
				
				this.rScanner= rweaveLanguage.getRChunkPartitionScanner();
			}
			else {
				throw new IllegalArgumentException("markupLanguage"); //$NON-NLS-1$
			}
		}
		
		final ITreePartitionNode beginNode= getScan().getBeginNode();
		if (beginNode.getType() instanceof RPartitionNodeType) {
			assert (false);
//			this.rBeginNode= beginNode;
//			while (beginNode.getParent().getType() instanceof RPartitionNodeType) {
//				beginNode= beginNode.getParent();
//			}
//			// !(beginNode.getParent().getType() instanceof RPartitionNodeType)
//			final RPartitionNodeType rType= (RPartitionNodeType) beginNode.getType();
//			if (rType == WikitextRChunkPartitionNodeScanner.R_CHUNK_CONTROL_TYPE) {
//				initNode(beginNode, RCHUNK_WIKITEXT_TYPE);
//			}
//			else {
//				initNode(beginNode, RINLINE_WIKITEXT_TYPE);
//			}
//			return;
		}
		super.init();
	}
	
	
	@Override
	protected void beginEmbeddingBlock(final BlockType type,
			final EmbeddingAttributes attributes) {
		if (type == BlockType.CODE
				&& attributes.getForeignType() == IRweaveMarkupLanguage.EMBEDDED_R) {
			addNode(R_CHUNK_BASE_TYPE, R_CHUNK_WIKITEXT_TYPE, getEventBeginOffset());
			setEmbedded(getNode(), attributes);
			return;
		}
		super.beginEmbeddingBlock(type, attributes);
	}
	
	@Override
	protected void endEmbeddingBlock(final WikitextPartitionNodeType type,
			final EmbeddingAttributes attributes) {
		if (type == R_CHUNK_WIKITEXT_TYPE) {
//			this.embeddedContentEndOffset= getScan().getDocument().getLength();
			executeForeignScanner(this.rScanner);
			exitNode(getEventEndOffset());
			return;
		}
		super.endEmbeddingBlock(type, attributes);
	}
	
	@Override
	protected void beginEmbeddingSpan(final SpanType type, final EmbeddingAttributes attributes) {
		if (type == SpanType.CODE
				&& attributes.getForeignType() == IRweaveMarkupLanguage.EMBEDDED_R) {
			addNode(this.rScanner.getRootType(), R_INLINE_WIKITEXT_TYPE,
					getBeginOffset() + attributes.getContentBeginOffset() );
			setEmbedded(getNode(), attributes);
			return;
		}
		super.beginEmbeddingSpan(type, attributes);
	}
	
	@Override
	protected void endEmbeddingSpan(final WikitextPartitionNodeType type, final EmbeddingAttributes attributes) {
		if (type == R_INLINE_WIKITEXT_TYPE) {
			if (this.embeddedContentEndOffset < 0) {
				this.embeddedContentEndOffset= getEventEndOffset() - 1;
			}
			
			exitNode(this.embeddedContentEndOffset);
			
			if (this.embeddingNode.getLength() > 0) {
				executeForeignScanner(this.rScanner);
			}
			return;
		}
		super.endEmbeddingSpan(type, attributes);
	}
	
}
