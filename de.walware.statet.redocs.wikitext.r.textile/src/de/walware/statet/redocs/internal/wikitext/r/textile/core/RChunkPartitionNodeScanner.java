/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.textile.core;

import org.eclipse.jface.text.rules.ICharacterScanner;

import de.walware.statet.r.core.source.RPartitionNodeType;

import de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner;


/**
 * Partition scanner for R chunks (stops if find '@' at column 0).
 */
class RChunkPartitionNodeScanner extends AbstractRChunkPartitionNodeScanner {
	
	
	private static final char[] CHUNK_START_KEYWORD= "begin.rcode".toCharArray(); //$NON-NLS-1$
	private static final char[] CHUNK_STOP_KEYWORD= "end.rcode".toCharArray(); //$NON-NLS-1$
	
	
	public RChunkPartitionNodeScanner() {
	}
	
	
	@Override
	protected void handleNewLine(final RPartitionNodeType type) {
		if (getChunkNode() != null) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				return;
			case '#':
				if (this.reader.read('#', '#', '.')) {
					int n= 4;
					n+= this.reader.readConsumingWhitespace();
					if (this.reader.readConsuming(CHUNK_START_KEYWORD)) {
						n+= CHUNK_START_KEYWORD.length;
						exitToChunkBase(START_LINE, this.reader.getOffset() - n);
						addNode(R_CHUNK_CONTROL_TYPE, this.reader.getOffset());
						this.last= LAST_OTHER;
						return;
					}
					if (this.reader.readConsuming(CHUNK_STOP_KEYWORD)) {
						n+= CHUNK_STOP_KEYWORD.length;
						exitToChunkBase(STOP_LINE, this.reader.getOffset() - n);
						addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
						this.last= LAST_OTHER;
						return;
					}
					this.reader.unread(n);
					return;
				}
				break;
			case '<':
				if (this.reader.read('<')) {
					exitToChunkBase(REF_LINE, this.reader.getOffset() - 2);
					addNode(R_CHUNK_CONTROL_TYPE, this.reader.getOffset());
					this.last= LAST_OTHER;
					return;
				}
				break;
			}
			this.reader.unread();
		}
	}
	
	@Override
	protected void processChunkControlOpen(final byte chunkLink) {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				//$FALL-THROUGH$
			case '\n':
				exitToChunkBase(chunkLink, this.reader.getOffset());
				addNode(getRootType(), this.reader.getOffset());
				this.last= LAST_NEWLINE;
				return;
			case '>':
				if (chunkLink == REF_LINE && this.reader.read('>')) {
					exitToChunkBase(REF_LINE, this.reader.getOffset() - 2);
					addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
					return;
				}
				continue LOOP;
			default:
				continue LOOP;
			}
		}
	}
	
}
