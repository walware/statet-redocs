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

package de.walware.statet.redocs.tex.r.core.source;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.text.core.sections.AbstractDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;

import de.walware.docmlet.tex.core.source.ITexDocumentConstants;

import de.walware.statet.r.core.source.IRDocumentConstants;

import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;
import de.walware.statet.redocs.r.core.source.RweaveDocSectionTreePartImpl;


public class LtxRweaveDocumentContentInfo extends AbstractDocContentSections
		implements IDocContentSectionsRweaveExtension {
	
	
	public static final String LTX=                         ITexDocumentConstants.LTX_PARTITIONING;
	public static final String R_CHUNK_CONTROL=             "LtxR-ChunkControl_walware"; //$NON-NLS-1$
	public static final String R=                           IRDocumentConstants.R_PARTITIONING;
	
	
	public static final IDocContentSectionsRweaveExtension INSTANCE= new LtxRweaveDocumentContentInfo();
	
	
	private final RweaveDocSectionTreePartImpl rweaveImpl;
	
	
	public LtxRweaveDocumentContentInfo() {
		super(ITexRweaveDocumentConstants.LTX_R_PARTITIONING, LTX,
				ImCollections.newList(LTX, R_CHUNK_CONTROL, R) );
		
		this.rweaveImpl= new RweaveDocSectionTreePartImpl(this);
	}
	
	
	@Override
	public final String getTypeByPartition(final String contentType) {
		if (IRDocumentConstants.R_ANY_CONTENT_CONSTRAINT.matches(contentType)) {
			return R;
		}
		if (ITexRweaveDocumentConstants.RCHUNK_PARTITION_CONSTRAINT.matches(contentType)) {
			return R_CHUNK_CONTROL;
		}
		return LTX;
	}
	
	
	@Override
	public ITreePartitionNode getRChunkRegion(final IDocument document, final int offset)
			throws BadLocationException {
		return this.rweaveImpl.getRChunkRegion(document, offset);
	}
	
	@Override
	public List<ITreePartitionNode> getRChunkRegions(final IDocument document,
			final int offset, final int length)
			throws BadLocationException {
		return this.rweaveImpl.getRChunkCodeRegions(document, offset, length);
	}
	
	@Override
	public IRegion getRChunkContentRegion(final IDocument document, final int offset)
			throws BadLocationException {
		return this.rweaveImpl.getRChunkContentRegion(document, offset);
	}
	
	@Override
	public ITreePartitionNode getRCodeRegion(final IDocument document, final int offset)
			throws BadLocationException {
		return this.rweaveImpl.getRCodeRegion(document, offset);
	}
	
	@Override
	public List<ITreePartitionNode> getRChunkCodeRegions(final IDocument document,
			final int offset, final int length)
			throws BadLocationException {
		return this.rweaveImpl.getRChunkCodeRegions(document, offset, length);
	}
	
}
