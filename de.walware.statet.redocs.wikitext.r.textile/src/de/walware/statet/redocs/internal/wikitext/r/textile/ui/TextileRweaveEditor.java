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

package de.walware.statet.redocs.internal.wikitext.r.textile.ui;

import de.walware.statet.redocs.internal.wikitext.r.textile.TextileRweavePlugin;
import de.walware.statet.redocs.wikitext.r.ui.editors.WikidocRweaveEditor;


public class TextileRweaveEditor extends WikidocRweaveEditor {
	
	
	public TextileRweaveEditor() {
		super(TextileRweavePlugin.DOC_CONTENT_TYPE, new TextileRweaveDocumentSetupParticipant());
	}
	
	
}
