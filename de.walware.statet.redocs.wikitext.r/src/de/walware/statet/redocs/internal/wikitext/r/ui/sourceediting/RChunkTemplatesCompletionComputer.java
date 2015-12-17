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

package de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting;


import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.TemplateProposal;
import de.walware.ecommons.ltk.ui.sourceediting.assist.TemplatesCompletionComputer;

import de.walware.docmlet.wikitext.core.markup.IMarkupLanguage;
import de.walware.docmlet.wikitext.core.source.MarkupLanguageDocumentSetupParticipant;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.r.ui.RedocsRUIResources;
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;
import de.walware.statet.redocs.wikitext.r.core.source.WikitextRweaveTemplatesContextType;


public class RChunkTemplatesCompletionComputer extends TemplatesCompletionComputer {
	
	
	private IRweaveMarkupLanguage markupLanguage;
	
	
	public RChunkTemplatesCompletionComputer() {
		super(	RedocsWikitextRPlugin.getInstance().getCodegenTemplateStore(),
				RedocsWikitextRPlugin.getInstance().getCodegenTemplateContextTypeRegistry() );
	}
	
	
	// sessionStarted not called automatically, because computer is not registered in registry
	@Override
	public void sessionStarted(final ISourceEditor editor, final ContentAssist assist) {
		final IMarkupLanguage markupLanguage= MarkupLanguageDocumentSetupParticipant.getMarkupLanguage(
				editor.getViewer().getDocument(), editor.getDocumentContentInfo().getPartitioning() );
		if (markupLanguage instanceof IRweaveMarkupLanguage) {
			this.markupLanguage= (IRweaveMarkupLanguage) markupLanguage;
		}
	}
	
	@Override
	public void sessionEnded() {
		this.markupLanguage= null;
	}
	
	
	@Override
	public IStatus computeCompletionProposals(final AssistInvocationContext context, int mode,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		// Set to specific mode to force to include templates in default mode
		if (mode == COMBINED_MODE) {
			mode= SPECIFIC_MODE;
		}
		
		sessionStarted(context.getEditor(), null);
		try {
			if (this.markupLanguage == null) {
				return null;
			}
			return super.computeCompletionProposals(context, mode, proposals, monitor);
		}
		finally {
			sessionEnded();
		}
	}
	
	@Override
	protected boolean include(final Template template, final String prefix) {
		final String pattern= template.getPattern();
		final int varIdx= pattern.indexOf("${"); //$NON-NLS-1$
		return ((varIdx < 0 || varIdx >= prefix.length()) && pattern.startsWith(prefix));
	}
	
	@Override
	protected String extractPrefix(final AssistInvocationContext context) {
		final IDocument document= context.getSourceViewer().getDocument();
		final int offset= context.getOffset();
		try {
			final int lineOffset= document.getLineOffset(document.getLineOfOffset(offset));
			String prefix= document.get(lineOffset, offset - lineOffset);
			final List<String> indentPrefixes= this.markupLanguage.getIndentPrefixes();
			ITER_PREFIX: while (!prefix.isEmpty()) {
				for (final String indentPrefix : indentPrefixes) {
					if (prefix.startsWith(indentPrefix)) {
						prefix= prefix.substring(indentPrefix.length());
						continue ITER_PREFIX;
					}
				}
				return prefix;
			}
			return ""; //$NON-NLS-1$
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	@Override
	protected TemplateContextType getContextType(final AssistInvocationContext context, final IRegion region) {
		return (this.markupLanguage != null) ?
				getTypeRegistry().getContextType(this.markupLanguage.getName() +
						WikitextRweaveTemplatesContextType.WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX) :
				null;
	}
	
	@Override
	protected Image getImage(final Template template) {
		return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.OBJ_RCHUNK_IMAGE_ID);
	}
	
	@Override
	protected TemplateProposal createProposal(final Template template, final DocumentTemplateContext context,
			final String prefix, final IRegion region, int relevance) {
		relevance= (!prefix.isEmpty() && template.getPattern().startsWith(prefix)) ?
				95 : 0;
		return super.createProposal(template, context, prefix, region, relevance);
	}
	
}
