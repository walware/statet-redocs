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

package de.walware.statet.redocs.wikitext.r.core.source;

import java.util.List;

import de.walware.ecommons.collections.ImCollections;

import de.walware.docmlet.wikitext.core.source.IWikitextDocumentConstants;

import de.walware.statet.r.core.source.IRDocumentConstants;

import de.walware.statet.redocs.r.core.source.IRweaveDocumentConstants;


public interface IWikitextRweaveDocumentConstants extends IRweaveDocumentConstants {
	
	
	/**
	 * The id of partitioning of Wikitext+R documents.
	 */
	String WIKIDOC_R_PARTITIONING= "WikidocRweave_walware"; //$NON-NLS-1$
	
	
	/**
	 * List with all partition content types of Wikitext+R documents.
	 */
	List<String> WIKIDOC_R_CONTENT_TYPES= ImCollections.concatList(
			IWikitextDocumentConstants.WIKIDOC_EXT_CONTENT_TYPES,
			RCHUNK_CONTENT_TYPES,
			IRDocumentConstants.R_CONTENT_TYPES );
	
}
