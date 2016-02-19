/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.ui.sourceediting;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;

import de.walware.statet.r.core.IRCoreAccess;

import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentSetupParticipant;


public class WikidocRweaveTemplateViewerConfigurator extends WikidocRweaveSourceViewerConfigurator {
	
	
	private static class WikidocRweaveTemplatesSourceViewerConfiguration extends WikidocRweaveSourceViewerConfiguration {
		
		
		private final TemplateVariableProcessor fProcessor;
		
		
		public WikidocRweaveTemplatesSourceViewerConfiguration(final TemplateVariableProcessor processor) {
			super(null, null, null, null, 0);
			this.fProcessor= processor;
		}
		
		
		@Override
		protected ContentAssist createContentAssistant(final ISourceViewer sourceViewer) {
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
	
	
	public WikidocRweaveTemplateViewerConfigurator(
			final WikidocRweaveDocumentSetupParticipant documentSetup,
			final IWikitextCoreAccess wikitextCoreAccess, final IRCoreAccess rCoreAccess,
			final TemplateVariableProcessor processor) {
		super(documentSetup, wikitextCoreAccess, rCoreAccess,
				new WikidocRweaveTemplatesSourceViewerConfiguration(processor) );
	}
	
}
