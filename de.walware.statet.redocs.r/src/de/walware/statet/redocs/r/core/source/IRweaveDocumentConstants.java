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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.text.core.IPartitionConstraint;


public interface IRweaveDocumentConstants {
	
	
	String RCHUNK_BASE_CONTENT_TYPE= "RweaveChunk.Base"; //$NON-NLS-1$
	String RCHUNK_CONTROL_CONTENT_TYPE= "RweaveChunk.Control"; //$NON-NLS-1$
	String RCHUNK_COMMENT_CONTENT_TYPE= "RweaveChunk.Comment"; //$NON-NLS-1$
	
	
	ImList<String> RCHUNK_CONTENT_TYPES= ImCollections.newList(
			RCHUNK_BASE_CONTENT_TYPE,
			RCHUNK_CONTROL_CONTENT_TYPE,
			RCHUNK_COMMENT_CONTENT_TYPE );
	
	
	IPartitionConstraint RCHUNK_PARTITION_CONSTRAINT= new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType == RCHUNK_BASE_CONTENT_TYPE
					|| partitionType == RCHUNK_CONTROL_CONTENT_TYPE
					|| partitionType == RCHUNK_COMMENT_CONTENT_TYPE );
		}
	};
	
	
}
