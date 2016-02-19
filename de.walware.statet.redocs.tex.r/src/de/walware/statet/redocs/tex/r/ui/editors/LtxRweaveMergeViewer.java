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

package de.walware.statet.redocs.tex.r.ui.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ltk.ui.compare.CompareMergeTextViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.r.core.RCore;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfigurator;


public class LtxRweaveMergeViewer extends CompareMergeTextViewer {
	
	
	public LtxRweaveMergeViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, configuration);
	}
	
	@Override
	protected IDocumentSetupParticipant createDocumentSetupParticipant() {
		return new LtxRweaveDocumentSetupParticipant();
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfigurator(final SourceViewer sourceViewer) {
		return new LtxRweaveSourceViewerConfigurator(
				TexCore.WORKBENCH_ACCESS, RCore.WORKBENCH_ACCESS,
				new LtxRweaveSourceViewerConfiguration() );
	}
	
}
