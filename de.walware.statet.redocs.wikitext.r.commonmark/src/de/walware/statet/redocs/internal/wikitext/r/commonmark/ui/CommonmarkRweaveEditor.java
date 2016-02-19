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

package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import de.walware.statet.redocs.internal.wikitext.r.commonmark.CommonmarkRweavePlugin;
import de.walware.statet.redocs.wikitext.r.ui.editors.WikidocRweaveEditor;


public class CommonmarkRweaveEditor extends WikidocRweaveEditor {
	
	
	public CommonmarkRweaveEditor() {
		super(CommonmarkRweavePlugin.DOC_CONTENT_TYPE, new CommonmarkRweaveDocumentSetupParticipant());
	}
	
	
}
