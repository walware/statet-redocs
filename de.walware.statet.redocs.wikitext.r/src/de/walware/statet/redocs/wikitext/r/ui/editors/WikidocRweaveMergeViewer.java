/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.ui.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ltk.ui.compare.CompareMergeTextViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;

import de.walware.docmlet.wikitext.core.WikitextCore;

import de.walware.statet.r.core.RCore;

import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfigurator;


public class WikidocRweaveMergeViewer extends CompareMergeTextViewer {
	
	
	private final WikidocRweaveDocumentSetupParticipant documentSetup;
	
	
	public WikidocRweaveMergeViewer(final WikidocRweaveDocumentSetupParticipant documentSetup,
			final Composite parent, final CompareConfiguration configuration) {
		super(parent, configuration);
		this.documentSetup= documentSetup;
	}
	
	
	@Override
	protected IDocumentSetupParticipant createDocumentSetupParticipant() {
		return this.documentSetup;
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfigurator(final SourceViewer sourceViewer) {
		return new WikidocRweaveSourceViewerConfigurator(this.documentSetup,
				WikitextCore.getWorkbenchAccess(), RCore.getWorkbenchAccess(),
				new WikidocRweaveSourceViewerConfiguration() );
	}
	
}
