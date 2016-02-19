/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.core.model;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.SourceContent;

import de.walware.docmlet.wikitext.core.ast.Embedded;
import de.walware.docmlet.wikitext.core.ast.WikitextAstInfo;
import de.walware.docmlet.wikitext.core.markup.IMarkupLanguage;
import de.walware.docmlet.wikitext.core.model.EmbeddingReconcileItem;
import de.walware.docmlet.wikitext.core.model.IWikidocModelInfo;
import de.walware.docmlet.wikitext.core.model.IWikidocSuModelContainerEmbeddedExtension;
import de.walware.docmlet.wikitext.core.model.WikidocSuModelContainer;
import de.walware.docmlet.wikitext.core.model.WikitextModel;

import de.walware.statet.r.core.model.RModel;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;
import de.walware.statet.redocs.wikitext.r.core.model.WikitextRweaveModel;
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;


public class WikidocRweaveSuModelContainer extends WikidocSuModelContainer<IWikidocRweaveSourceUnit>
		implements IWikidocSuModelContainerEmbeddedExtension {
	
	
	public WikidocRweaveSuModelContainer(final IWikidocRweaveSourceUnit su) {
		super(su);
	}
	
	
	@Override
	public boolean isContainerFor(final String modelTypeId) {
		return (modelTypeId == WikitextModel.WIKIDOC_TYPE_ID || modelTypeId == WikitextRweaveModel.WIKIDOC_R_MODEL_TYPE_ID);
	}
	
	public String getNowebType() {
		return RModel.R_TYPE_ID;
	}
	
	
	@Override
	public void reconcileEmbeddedAst(final SourceContent content, final List<Embedded> list,
			final IMarkupLanguage markupLanguage, final int level,
			final IProgressMonitor monitor) {
		if (markupLanguage instanceof IRweaveMarkupLanguage) {
			WikidocRChunkReconciler.getInstance((IRweaveMarkupLanguage) markupLanguage)
					.reconcileAst(this, content, list, monitor);
		}
	}
	
	@Override
	public void reconcileEmbeddedModel(final SourceContent content, final IWikidocModelInfo wikitextModel,
			final List<EmbeddingReconcileItem> list,
			final int level, final IProgressMonitor monitor) {
		final WikitextAstInfo astInfo= wikitextModel.getAst();
		final IMarkupLanguage markupLanguage= astInfo.getMarkupLanguage();
		if (markupLanguage instanceof IRweaveMarkupLanguage) {
			WikidocRChunkReconciler.getInstance((IRweaveMarkupLanguage) markupLanguage)
					.reconcileModel(this, content, wikitextModel, list, level, monitor);
		}
	}
	
	@Override
	public void reportEmbeddedProblems(final SourceContent content, final IWikidocModelInfo wikitextModel,
			final IProblemRequestor problemRequestor, final int level,
			final IProgressMonitor monitor) {
		final WikitextAstInfo astInfo= wikitextModel.getAst();
		final IMarkupLanguage markupLanguage= astInfo.getMarkupLanguage();
		if (markupLanguage instanceof IRweaveMarkupLanguage) {
			WikidocRChunkReconciler.getInstance((IRweaveMarkupLanguage) markupLanguage)
					.reportEmbeddedProblems(this, content, wikitextModel, problemRequestor, level, monitor);
		}
	}
	
	@Override
	public IProblemRequestor createProblemRequestor() {
		if (getMode() == LTK.EDITOR_CONTEXT) {
			return RedocsWikitextRPlugin.getInstance().getDocRDocumentProvider().createProblemRequestor(
					getSourceUnit() );
		}
		return null;
	}
	
}
