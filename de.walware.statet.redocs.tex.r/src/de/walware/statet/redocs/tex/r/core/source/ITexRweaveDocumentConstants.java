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

import java.util.List;

import de.walware.ecommons.collections.ImCollections;

import de.walware.docmlet.tex.core.source.ITexDocumentConstants;

import de.walware.statet.r.core.source.IRDocumentConstants;

import de.walware.statet.redocs.r.core.source.IRweaveDocumentConstants;


public interface ITexRweaveDocumentConstants extends IRweaveDocumentConstants {
	
	
	/**
	 * The id of partitioning of Sweave (LaTeX+R) documents.
	 */
	String LTX_R_PARTITIONING= "LtxRweave_walware"; //$NON-NLS-1$
	
	
	/**
	 * List with all partition content types of Sweave (LaTeX+R) documents.
	 */
	List<String> LTX_R_CONTENT_TYPES= ImCollections.concatList(
			ITexDocumentConstants.LTX_CONTENT_TYPES,
			RCHUNK_CONTENT_TYPES,
			IRDocumentConstants.R_CONTENT_TYPES );
	
}
