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

package de.walware.statet.redocs.internal.tex.r.ui.sourceediting;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.tex.core.ITexCoreAccess;

import de.walware.statet.r.core.IRCoreAccess;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfigurator;


public class LtxRweaveTemplateViewerConfigurator extends LtxRweaveSourceViewerConfigurator {
	
	
	private static class LtxRweaveTemplatesSourceViewerConfiguration extends LtxRweaveSourceViewerConfiguration {
		
		
		private final TemplateVariableProcessor fProcessor;
		
		
		public LtxRweaveTemplatesSourceViewerConfiguration(final TemplateVariableProcessor processor) {
			super(null, null, null);
			this.fProcessor= processor;
		}
		
		@Override
		protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
			return createTemplateVariableContentAssistant(sourceViewer, this.fProcessor);
		}
		
		@Override
		public int[] getConfiguredTextHoverStateMasks(final ISourceViewer sourceViewer, final String contentType) {
			return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
		}
		
		@Override
		public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
			return new TemplateVariableTextHover(this.fProcessor);
		}
		
	}
	
	
	public LtxRweaveTemplateViewerConfigurator(final ITexCoreAccess texCore, final IRCoreAccess rCore,
			final TemplateVariableProcessor processor) {
		super(texCore, rCore, new LtxRweaveTemplatesSourceViewerConfiguration(processor));
	}
	
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new LtxRweaveDocumentSetupParticipant(true);
	}
	
}
