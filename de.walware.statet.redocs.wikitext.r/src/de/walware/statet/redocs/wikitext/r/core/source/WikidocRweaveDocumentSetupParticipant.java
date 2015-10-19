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

import de.walware.docmlet.wikitext.core.markup.IMarkupLanguage;
import de.walware.docmlet.wikitext.core.source.IMarkupLanguagePartitioner;
import de.walware.docmlet.wikitext.core.source.MarkupLanguageDocumentSetupParticipant1;
import de.walware.docmlet.wikitext.core.source.WikitextPartitioner;
import de.walware.docmlet.wikitext.core.source.extdoc.IExtdocMarkupLanguage;


/**
 * The document setup participant for Wikitext-R documents.
 */
public class WikidocRweaveDocumentSetupParticipant extends MarkupLanguageDocumentSetupParticipant1 {
	
	
	public WikidocRweaveDocumentSetupParticipant(final IExtdocMarkupLanguage markupLanguage) {
		this(markupLanguage, false);
	}
	
	public WikidocRweaveDocumentSetupParticipant(final IExtdocMarkupLanguage markupLanguage,
			final boolean templateMode) {
		super(markupLanguage, (templateMode) ? IMarkupLanguage.TEMPLATE_MODE : 0);
	}
	
	
	@Override
	public String getPartitioningId() {
		return IWikitextRweaveDocumentConstants.WIKIDOC_R_PARTITIONING;
	}
	
	@Override
	protected IMarkupLanguagePartitioner createDocumentPartitioner(final IMarkupLanguage markupLanguage) {
		return new WikitextPartitioner(getPartitioningId(),
				new WikidocRweavePartitionNodeScanner(markupLanguage, getMarkupLanguageMode()),
				IWikitextRweaveDocumentConstants.WIKIDOC_R_CONTENT_TYPES );
	}
	
}
