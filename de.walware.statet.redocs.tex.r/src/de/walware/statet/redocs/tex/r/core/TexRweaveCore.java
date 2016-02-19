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

package de.walware.statet.redocs.tex.r.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;


public class TexRweaveCore {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.redocs.tex.r"; //$NON-NLS-1$
	
	
	/**
	 * Content type id for Sweave (LaTeX+R) document sources
	 */
	public static final String LTX_R_CONTENT_ID= "de.walware.statet.redocs.contentTypes.LtxRweave"; //$NON-NLS-1$
	
	public static final IContentType LTX_R_CONTENT_TYPE;
	
	static {
		final IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
		LTX_R_CONTENT_TYPE= contentTypeManager.getContentType(LTX_R_CONTENT_ID);
	}
	
}
