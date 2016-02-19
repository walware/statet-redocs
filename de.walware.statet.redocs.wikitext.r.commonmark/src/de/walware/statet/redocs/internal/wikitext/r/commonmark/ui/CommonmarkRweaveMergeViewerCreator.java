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

package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.statet.redocs.wikitext.r.ui.editors.WikidocRweaveMergeViewer;


public class CommonmarkRweaveMergeViewerCreator implements IViewerCreator {
	
	
	public CommonmarkRweaveMergeViewerCreator() {
	}
	
	
	@Override
	public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
		return new WikidocRweaveMergeViewer(new CommonmarkRweaveDocumentSetupParticipant(),
				parent, config );
	}
	
}
