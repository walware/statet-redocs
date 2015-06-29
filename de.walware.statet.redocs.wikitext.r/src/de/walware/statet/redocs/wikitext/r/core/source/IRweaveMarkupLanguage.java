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

import java.util.List;
import java.util.regex.Pattern;

import de.walware.ecommons.ltk.ast.IEmbeddingAstNode;

import de.walware.docmlet.wikitext.core.markup.IMarkupLanguage;

import de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner;


public interface IRweaveMarkupLanguage extends IMarkupLanguage {
	
	
	String EMBEDDED_R= "R"; // RModel.R_TYPE_ID //$NON-NLS-1$
	
	byte EMBEDDED_R_CHUNK_DESCR= IEmbeddingAstNode.EMBED_CHUNK;
	byte EMBEDDED_R_INLINE_DESCR= IEmbeddingAstNode.EMBED_INLINE;
	
	
	List<String> getIndentPrefixes();
	
	Pattern getRChunkStartLinePattern();
	Pattern getRChunkRefLinePattern();
	Pattern getRChunkEndLinePattern();
	
	
	AbstractRChunkPartitionNodeScanner getRChunkPartitionScanner();
	
}
