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

package de.walware.statet.redocs.r.core.source;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;
import de.walware.ecommons.text.core.treepartitioner.TreePartitionUtil;

import de.walware.statet.r.core.source.IRDocumentConstants;


public class RweaveDocSectionTreePartImpl {
	
	
	private final IDocContentSections sections;
	
	
	public RweaveDocSectionTreePartImpl(final IDocContentSections sections) {
		this.sections= sections;
	}
	
	
	public IDocContentSections getSections() {
		return this.sections;
	}
	
	public ITreePartitionNode getRChunkRegion(final IDocument document, final int offset)
			throws BadLocationException {
		return TreePartitionUtil.searchNode(document, this.sections.getPartitioning(), offset, false,
				IRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE );
	}
	
	public List<ITreePartitionNode> getRChunkRegions(final IDocument document,
			final int offset, final int length) throws BadLocationException {
		final List<ITreePartitionNode> nodes= new ArrayList<>();
		final ITreePartitionNode root= TreePartitionUtil.getRootNode(document, this.sections.getPartitioning());
		
		addRChunkRegions(root, offset, offset + length, nodes);
		
		return nodes;
	}
	
	private void addRChunkRegions(final ITreePartitionNode node, final int beginOffset, final int endOffset,
			final List<ITreePartitionNode> nodes) {
		final int childCount= node.getChildCount();
		int childIdx= 0;
		for (; childIdx < childCount; childIdx++) {
			final ITreePartitionNode child= node.getChild(childIdx);
			if (child.getOffset() + child.getLength() > beginOffset) {
				break;
			}
		}
		for (; childIdx < childCount; childIdx++) {
			final ITreePartitionNode child= node.getChild(childIdx);
			if (child.getOffset() >= endOffset) {
				break;
			}
			if (child.getType().getPartitionType() == IRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE) {
				nodes.add(child);
			}
			else {
				addRChunkRegions(child, beginOffset, endOffset, nodes);
			}
		}
	}
	
	public IRegion getRChunkContentRegion(final IDocument document, final int offset)
			throws BadLocationException {
		final ITreePartitionNode rChunk= getRChunkRegion(document, offset);
		if (rChunk == null || rChunk.getLength() <= 2) {
			return null;
		}
		final int beginLine= document.getLineOfOffset(rChunk.getOffset()) + 1;
		final int beginLineOffset= document.getLineOffset(beginLine);
		int endLine= document.getLineOfOffset(rChunk.getOffset() + rChunk.getLength());
		if (beginLine >= endLine) {
			return null;
		}
		int endLineOffset= document.getLineOffset(endLine);
		if (endLineOffset == rChunk.getOffset() + rChunk.getLength()) {
			endLine--;
			if (beginLine >= endLine) {
				return null;
			}
			endLineOffset= document.getLineOffset(endLine);
		}
		
		if (document.getChar(endLineOffset) == '@') { // closed
			return new Region(beginLineOffset, endLineOffset - beginLineOffset);
		}
		else { // node closed
			return new Region(beginLineOffset,
					rChunk.getOffset() + rChunk.getLength() - beginLineOffset );
		}
	}
	
	public List<ITreePartitionNode> getRChunkCodeRegions(final IDocument document,
			final int offset, final int length) throws BadLocationException {
		final List<ITreePartitionNode> rChunks= getRChunkRegions(document, offset, length);
		final List<ITreePartitionNode> nodes= new ArrayList<>(rChunks.size());
		
		final int endOffset= offset + length;
		for (int i= 0; i < rChunks.size(); i++) {
			final ITreePartitionNode node= rChunks.get(i);
			final int childCount= node.getChildCount();
			int childIdx= 0;
			for (; childIdx < childCount; childIdx++) {
				final ITreePartitionNode child= node.getChild(childIdx);
				if (child.getOffset() + child.getLength() > offset) {
					break;
				}
			}
			for (; childIdx < childCount; childIdx++) {
				final ITreePartitionNode child= node.getChild(childIdx);
				if (child.getOffset() >= endOffset) {
					break;
				}
				if (child.getType().getPartitionType() == IRDocumentConstants.R_DEFAULT_CONTENT_TYPE) {
					nodes.add(child);
				}
			}
		}
		
		return nodes;
	}
	
	public ITreePartitionNode getRCodeRegion(final IDocument document, final int offset)
			throws BadLocationException {
		return TreePartitionUtil.searchNode(document, this.sections.getPartitioning(), offset, true,
				IRDocumentConstants.R_DEFAULT_CONTENT_TYPE );
	}
	
}
