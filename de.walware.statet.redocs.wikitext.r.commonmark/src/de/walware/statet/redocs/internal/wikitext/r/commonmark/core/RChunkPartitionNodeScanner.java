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

package de.walware.statet.redocs.internal.wikitext.r.commonmark.core;

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
			case ' ':
			case '\t':
				{	int n= 1;
					n+= this.reader.readConsumingWhitespace();
					if (this.reader.read('`', '`', '`')) {
						n+= 3;
						if (checkStart(n)) {
							return;
						}
						exitToChunkBase(STOP_LINE, this.reader.getOffset() - n);
						addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
						this.last= LAST_OTHER;
						return;
					}
					if (this.reader.read('<', '<')) {
						n+= 2;
						exitToChunkBase(REF_LINE, this.reader.getOffset() - n);
						addNode(R_CHUNK_CONTROL_TYPE, this.reader.getOffset());
						this.last= LAST_OTHER;
						return;
					}
					this.reader.unread(n);
					return;
				}
//				break;
			case '`':
				if (this.reader.read('`', '`')) {
					int num= 3;
					if (checkStart(num)) {
						return;
					}
					exitToChunkBase(STOP_LINE, this.reader.getOffset() - num);
					addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
					this.last= LAST_OTHER;
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
	
	private boolean checkStart(final int num) {
		int numTmp= this.reader.readConsumingWhitespace();
		if (this.reader.read('{')) {
			numTmp++;
			if (this.reader.read('.')) { // optional
				numTmp++;
			}
			if (this.reader.read('r')) {
				numTmp++;
				exitToChunkBase(START_LINE, this.reader.getOffset() - (num + numTmp));
				addNode(R_CHUNK_CONTROL_TYPE, this.reader.getOffset());
				this.last= LAST_OTHER;
				return true;
			}
		}
		this.reader.unread(numTmp);
		return false;
	}
	
	@Override
	protected void processChunkControlOpen(final byte chunkLink) {
		int bracketCount= 0;
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
			case '{':
				bracketCount++;
				continue LOOP;
			case '}':
				if (bracketCount == 0 && chunkLink == START_LINE) {
					exitToChunkBase(START_LINE, this.reader.getOffset() - 1);
					addNode(R_CHUNK_COMMENT_TYPE, this.reader.getOffset());
					this.last= LAST_OTHER;
					return;
				}
				bracketCount--;
				continue LOOP;
			default:
				continue LOOP;
			}
		}
	}
	
}
