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

package de.walware.statet.redocs.internal.wikitext.r.textile.ui;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ltk.ui.compare.CompareTextViewer;

import de.walware.docmlet.wikitext.core.WikitextCore;

import de.walware.statet.r.core.RCore;

import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfigurator;


public class TextileRweaveContentViewerCreator implements IViewerCreator {
	
	
	public TextileRweaveContentViewerCreator() {
	}
	
	
	@Override
	public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
		final WikidocRweaveSourceViewerConfigurator viewerConfigurator=
				new WikidocRweaveSourceViewerConfigurator(
						new TextileRweaveDocumentSetupParticipant(),
						WikitextCore.getWorkbenchAccess(), RCore.getWorkbenchAccess(),
						new WikidocRweaveSourceViewerConfiguration() );
		return new CompareTextViewer(parent, config, viewerConfigurator);
	}
	
}
