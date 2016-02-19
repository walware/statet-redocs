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

package de.walware.statet.redocs.r.core.source;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;

import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;

import de.walware.statet.r.core.source.RPartitionNodeScanner;
import de.walware.statet.r.core.source.RPartitionNodeType;


/**
 * Abstract partition scanner for R chunks e.g. in Sweave files.
 */
public abstract class AbstractRChunkPartitionNodeScanner extends RPartitionNodeScanner {
	
	
	private static final int S_CHUNKCONTROL= 8;
	private static final int S_CHUNKCOMMENT= 9;
	
	public static final RPartitionNodeType R_CHUNK_BASE_TYPE= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return S_CHUNKCONTROL;
		}
		
	};
	
	public static final RPartitionNodeType R_CHUNK_CONTROL_TYPE= new RPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return IRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return S_CHUNKCONTROL;
		}
		
		@Override
		public boolean prefereAtBegin(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
		@Override
		public boolean prefereAtEnd(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
	};
	
	public static final RPartitionNodeType R_CHUNK_COMMENT_TYPE= new RPartitionNodeType.Comment() {
		
		@Override
		public String getPartitionType() {
			return IRweaveDocumentConstants.RCHUNK_COMMENT_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return S_CHUNKCOMMENT;
		}
		
		@Override
		public boolean prefereAtBegin(final ITreePartitionNode node, final IDocument document) {
			return true;
		}
		
	};
	
	
	protected static final byte NONE= 0;
	protected static final byte START_LINE= 1;
	protected static final byte REF_LINE= 2;
	protected static final byte STOP_LINE= 3;
	
	
	private ITreePartitionNode chunkNode;
	
	private byte chunkLine;
	
	
	public AbstractRChunkPartitionNodeScanner() {
	}
	
	
	@Override
	public RPartitionNodeType getRootType() {
		return RPartitionNodeType.DEFAULT_ROOT;
	}
	
	@Override
	protected void init() {
		ITreePartitionNode node= getScan().getBeginNode();
		while (node.getType() instanceof RPartitionNodeType) {
			if (node.getType().getPartitionType() == IRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE) {
				this.chunkNode= node;
				break;
			}
			node= node.getParent();
		}
		this.chunkLine= NONE;
		
		super.init();
	}
	
	protected final ITreePartitionNode getChunkNode() {
		return this.chunkNode;
	}
	
	protected final byte getChunkLine() {
		return this.chunkLine;
	}
	
	protected final void exitToChunkBase(final byte chunkLine, final int offset) {
		ITreePartitionNode node= getNode();
		while (node != this.chunkNode) {
			exitNode(offset);
			node= node.getParent();
		}
		this.chunkLine= chunkLine;
	}
	
	@Override
	protected abstract void handleNewLine(final RPartitionNodeType type);
	
	@Override
	protected void processExt(final RPartitionNodeType type) {
		if (this.chunkNode != null) {
			switch (type.getScannerState()) {
			case S_CHUNKCONTROL:
				processChunkControlOpen(this.chunkLine);
				return;
			case S_CHUNKCOMMENT:
				processChunkComment();
				return;
			default:
				break;
			}
		}
		super.processExt(type);
	}
	
	protected abstract void processChunkControlOpen(byte chunkLink);
	
	protected void processChunkComment() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				//$FALL-THROUGH$
			case '\n':
				exitNode(this.reader.getOffset());
				if (this.chunkLine == STOP_LINE) {
					this.last= LAST_EOF;
					return;
				}
				else {
					addNode(getRootType(), this.reader.getOffset());
					this.last= LAST_NEWLINE;
					return;
				}
			default:
				continue LOOP;
			}
		}
	}
	
}
