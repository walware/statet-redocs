/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.textile.ui;

import de.walware.statet.redocs.r.ui.debug.RweaveSubmitContentHandler;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentContentInfo;


public class TextileRweaveSubmitContentHandler extends RweaveSubmitContentHandler {
	
	
	public TextileRweaveSubmitContentHandler() {
		super(new TextileRweaveDocumentSetupParticipant(), WikidocRweaveDocumentContentInfo.INSTANCE);
	}
	
}
