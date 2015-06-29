/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.model;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.SourceContent;

import de.walware.docmlet.tex.core.ast.Embedded;
import de.walware.docmlet.tex.core.model.EmbeddingReconcileItem;
import de.walware.docmlet.tex.core.model.ILtxModelInfo;
import de.walware.docmlet.tex.core.model.ILtxSuModelContainerEmbeddedExtension;
import de.walware.docmlet.tex.core.model.LtxSuModelContainer;
import de.walware.docmlet.tex.core.model.TexModel;

import de.walware.statet.r.core.model.RModel;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;
import de.walware.statet.redocs.tex.r.core.model.TexRweaveModel;


public class LtxRweaveSuModelContainer extends LtxSuModelContainer<ILtxRweaveSourceUnit>
		implements ILtxSuModelContainerEmbeddedExtension {
	
	
	public LtxRweaveSuModelContainer(final ILtxRweaveSourceUnit su) {
		super(su);
	}
	
	
	@Override
	public boolean isContainerFor(final String modelTypeId) {
		return (modelTypeId == TexModel.LTX_TYPE_ID || modelTypeId == TexRweaveModel.LTX_R_MODEL_TYPE_ID);
	}
	
	@Override
	public String getNowebType() {
		return RModel.R_TYPE_ID;
	}
	
	
	@Override
	public void reconcileEmbeddedAst(final SourceContent content, final List<Embedded> list,
			final int level, final IProgressMonitor monitor) {
		LtxRChunkReconciler.getInstance()
				.reconcileAst(this, content, list, monitor);
	}
	
	@Override
	public void reconcileEmbeddedModel(final SourceContent content, final ILtxModelInfo texModel,
			final List<EmbeddingReconcileItem> list,
			final int level, final IProgressMonitor monitor) {
		LtxRChunkReconciler.getInstance()
				.reconcileModel(this, content, texModel, list, level, monitor);
	}
	
	@Override
	public void reportEmbeddedProblems(final SourceContent content, final ILtxModelInfo texModel,
			final IProblemRequestor problemRequestor,
			final int level, final IProgressMonitor monitor) {
		LtxRChunkReconciler.getInstance()
				.reportEmbeddedProblems(this, content, texModel, problemRequestor, level, monitor);
	}
	
	@Override
	public IProblemRequestor createProblemRequestor() {
		if (getMode() == LTK.EDITOR_CONTEXT) {
			return RedocsTexRPlugin.getInstance().getDocRDocumentProvider().createProblemRequestor(
					getSourceUnit() );
		}
		return null;
	}
	
}
