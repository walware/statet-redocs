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

import org.eclipse.jface.text.rules.ICharacterScanner;

import de.walware.statet.r.core.source.RPartitionNodeType;

import de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner;


/**
 * Partition scanner for R chunks (stops if find '@' at column 0).
 */
class RChunkPartitionNodeScanner extends AbstractRChunkPartitionNodeScanner {
	
	
	public RChunkPartitionNodeScanner() {
	}
	
	
	@Override
	protected void handleNewLine(final RPartitionNodeType type) {
		if (getChunkNode() != null) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				return;
			case '<':
				if (this.reader.read('<')) {
					exitToChunkBase(START_LINE, this.reader.getOffset() - 2);
					addNode(R_CHUNK_CONTROL_TYPE, this.reader.getOffset());
					this.last= LAST_OTHER;
					return;
				}
				break;
			case '@':
				exitToChunkBase(STOP_LINE, this.reader.getOffset() - 1);
				addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
				this.last= LAST_OTHER;
				return;
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
				exitToChunkBase(getChunkLine(), this.reader.getOffset());
				addNode(getRootType(), this.reader.getOffset());
				this.last= LAST_NEWLINE;
				return;
			case '>':
				if (this.reader.read('>')) {
					exitToChunkBase(chunkLink, this.reader.getOffset() - 2);
					this.reader.read('=');
					addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
					this.last= LAST_OTHER;
					return;
				}
				continue LOOP;
			default:
				continue LOOP;
			}
		}
	}
	
}
