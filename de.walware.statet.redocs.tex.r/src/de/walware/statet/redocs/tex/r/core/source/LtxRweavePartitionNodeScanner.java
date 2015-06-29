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

package de.walware.statet.redocs.tex.r.core.source;

import org.eclipse.jface.text.rules.ICharacterScanner;

import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNodeScan;
import de.walware.ecommons.text.core.treepartitioner.WrappedPartitionScan;

import de.walware.docmlet.tex.core.source.LtxPartitionNodeScanner;
import de.walware.docmlet.tex.core.source.LtxPartitionNodeType;
import de.walware.docmlet.tex.core.source.LtxPartitionNodeType.VerbatimInline;

import de.walware.statet.r.core.source.RPartitionNodeType;


/**
 * Paritition scanner for LaTeX with chunks.
 * 
 * Stops if find '&lt;&lt;' at column 0 and handles 'Sexpr' control word.
 */
public class LtxRweavePartitionNodeScanner extends LtxPartitionNodeScanner {
	
	
	protected static final int S_RVERB= S_EXT_LTX + 1;
	protected static final int S_RCHUNK= S_EXT_LTX + 2;
	
	private static final VerbatimInline SEXPR_LTX_TYPE= new VerbatimInline('}') {
		
		@Override
		public byte getScannerState() {
			return S_RVERB;
		}
		
	};
	
	private static final LtxPartitionNodeType RCHUNK_LTX_TYPE= new LtxPartitionNodeType() {
		
		@Override
		public String getPartitionType() {
			return ITexRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE;
		}
		
		@Override
		public byte getScannerState() {
			return S_RCHUNK;
		}
	};
	
	private static final char[] SEQ_Sexpr= "Sexpr".toCharArray(); //$NON-NLS-1$
	
	
	private final RChunkPartitionNodeScanner rScanner= new RChunkPartitionNodeScanner();
	
	private WrappedPartitionScan rScan;
	
	private int rBeginOffset;
	private ITreePartitionNode rBeginNode;
	
	
	public LtxRweavePartitionNodeScanner() {
		super();
	}
	
	public LtxRweavePartitionNodeScanner(final boolean templateMode) {
		super(templateMode);
	}
	
	
	@Override
	public void execute(final ITreePartitionNodeScan scan) {
		this.rScan= new WrappedPartitionScan(scan);
		
		super.execute(scan);
		
		this.rScan= null;
	}
	
	
	@Override
	protected void init() {
		ITreePartitionNode beginNode= getScan().getBeginNode();
		if (beginNode.getType() instanceof RPartitionNodeType) {
			this.rBeginOffset= getScan().getBeginOffset();
			this.rBeginNode= beginNode;
			while (beginNode.getParent().getType() instanceof RPartitionNodeType) {
				beginNode= beginNode.getParent();
			}
			// !(beginNode.getParent().getType() instanceof RPartitionNodeType)
			final RPartitionNodeType rType= (RPartitionNodeType) beginNode.getType();
			if (rType == RChunkPartitionNodeScanner.R_CHUNK_BASE_TYPE) {
				initNode(beginNode, RCHUNK_LTX_TYPE);
			}
			else {
				initNode(beginNode, SEXPR_LTX_TYPE);
			}
			return;
		}
		this.rBeginNode= null;
		super.init();
	}
	
	
	@Override
	protected void handleNewLine(final LtxPartitionNodeType type) {
		if (this.reader.readTemp('<', '<')) {
			if (type == RCHUNK_LTX_TYPE) { // setup by #init
				assert (this.rBeginNode != null);
				return;
			}
			assert (this.rBeginNode == null);
			this.rBeginOffset= this.reader.getOffset();
			addNode(RChunkPartitionNodeScanner.R_CHUNK_BASE_TYPE, RCHUNK_LTX_TYPE,
					this.rBeginOffset );
			this.rBeginNode= getNode();
		}
	}
	
	@Override
	protected boolean searchExtCommand(final int c) {
		if (c == 'S' && this.reader.readConsuming2(SEQ_Sexpr)) {
			this.reader.readConsumingWhitespace();
			if (this.reader.read('{')) {
				assert (this.rBeginNode == null);
				this.rBeginOffset= this.reader.getOffset();
				addNode(this.rScanner.getRootType(), SEXPR_LTX_TYPE,
						this.rBeginOffset );
				this.rBeginNode= getNode();
				processExt(SEXPR_LTX_TYPE);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void processExt(final LtxPartitionNodeType type) {
		final ITreePartitionNode node;
		switch (type.getScannerState()) {
		
		case S_RVERB:
			node= this.rBeginNode;
			this.rBeginNode= null;
			
			processInline(); // includes exitNode
			
			if (node.getLength() > 0) {
				this.rScan.init(this.rBeginOffset, node.getOffset() + node.getLength(), node);
				this.rScanner.execute(this.rScan);
				this.rScan.exit();
			}
			return;
			
		case S_RCHUNK:
			node= this.rBeginNode;
			this.rBeginNode= null;
			
			this.rScan.init(this.rBeginOffset, this.rScan.getDocument().getLength(), node);
			this.rScanner.execute(this.rScan);
			this.rScan.exit();
			
			final ITreePartitionNode chunkNode= getNode();
			exitNode();
			setRange(chunkNode.getOffset() + chunkNode.getLength(),
					getScan().getDocument().getLength() );
			return;
		}
		
		super.processExt(type);
	}
	
	protected void processInline() {
		int expandVar= 0;
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				exitNode(this.reader.getOffset()); // required for rweave
				this.last= LAST_EOF;
				return;
			case '\r':
				exitNode(this.reader.getOffset() - 1);
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				exitNode(this.reader.getOffset() - 1);
				this.last= LAST_NEWLINE;
				return;
			case '{':
				if (this.reader.read('{')) {
					expandVar++;
					continue LOOP;
				}
				continue LOOP;
			case '}':
				if (expandVar > 0 && this.reader.read('}')) {
					expandVar--;
					continue LOOP;
				}
				exitNode(this.reader.getOffset() - 1);
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
}
