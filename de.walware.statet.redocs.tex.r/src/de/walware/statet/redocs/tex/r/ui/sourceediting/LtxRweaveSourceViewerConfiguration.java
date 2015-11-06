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

package de.walware.statet.redocs.tex.r.ui.sourceediting;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.ui.LTKUIPreferences;
import de.walware.ecommons.ltk.ui.sourceediting.EcoReconciler2;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.MultiContentSectionSourceViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewer;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistCategory;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IContentAssistComputer;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.TreePartition;
import de.walware.ecommons.text.core.treepartitioner.TreePartitionUtil;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.source.ITexDocumentConstants;
import de.walware.docmlet.tex.ui.sourceediting.LtxSourceViewerConfiguration;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.core.source.RPartitionNodeType;
import de.walware.statet.r.ui.sourceediting.RAutoEditStrategy;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.internal.tex.r.ui.sourceediting.DocRQuickOutlineInformationProvider;
import de.walware.statet.redocs.internal.tex.r.ui.sourceediting.RChunkTemplatesCompletionComputer;
import de.walware.statet.redocs.tex.r.core.source.ITexRweaveDocumentConstants;
import de.walware.statet.redocs.tex.r.core.source.LtxRweaveBracketPairMatcher;
import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;


/**
 * Default Configuration for SourceViewer of Sweave (LaTeX+R) code.
 */
public class LtxRweaveSourceViewerConfiguration extends MultiContentSectionSourceViewerConfiguration {
	
	
	private static final String[] CONTENT_TYPES= ITexRweaveDocumentConstants.LTX_R_CONTENT_TYPES.toArray(
			new String[ITexRweaveDocumentConstants.LTX_R_CONTENT_TYPES.size()]);
	
	
	private static class LtxConfiguration extends LtxSourceViewerConfiguration {
		
		public LtxConfiguration(final IDocContentSections documentContentInfo,
				final ISourceEditor editor,
				final ITexCoreAccess texCoreAccess,
				final IPreferenceStore preferenceStore) {
			super(documentContentInfo, editor, texCoreAccess, preferenceStore, null);
		}
		
		@Override
		protected void setCoreAccess(final ITexCoreAccess access) {
			super.setCoreAccess(access);
		}
		
	}
	
	
	private static class RChunkAutoEditStrategy extends RAutoEditStrategy {
		
		public RChunkAutoEditStrategy(final IRCoreAccess coreAccess, final ISourceEditor sourceEditor) {
			super(coreAccess, sourceEditor);
		}
		
		@Override
		protected IRegion getValidRange(final int offset, final TreePartition partition, final int c) {
			switch (LtxRweaveDocumentContentInfo.INSTANCE.getTypeByPartition(partition.getType())) {
			case LtxRweaveDocumentContentInfo.R:
				return TreePartitionUtil.searchNodeUp(partition.getTreeNode(),
						RPartitionNodeType.DEFAULT_ROOT );
			case LtxRweaveDocumentContentInfo.R_CHUNK_CONTROL:
				switch (c) {
				case '(':
				case '[':
				case '{':
				case '%':
				case '\"':
				case '\'':
					return TreePartitionUtil.searchPartitionRegion(partition,
							ITexRweaveDocumentConstants.RCHUNK_PARTITION_CONSTRAINT );
				}
				return null;
			default:
				return null;
			}
		}
		
	}
	
	private static class RChunkConfiguration extends RSourceViewerConfiguration {
		
		public RChunkConfiguration(final IDocContentSections documentContentInfo,
				final ISourceEditor sourceEditor,
				final IRCoreAccess coreAccess,
				final IPreferenceStore preferenceStore) {
			super(documentContentInfo, sourceEditor, coreAccess, preferenceStore, null);
		}
		
		@Override
		protected void setCoreAccess(final IRCoreAccess access) {
			super.setCoreAccess(access);
		}
		
		@Override
		protected TextStyleManager getTextStyles() {
			return super.getTextStyles();
		}
		
		@Override
		protected ITokenScanner getScanner(final String contentType) {
			return super.getScanner(contentType);
		}
		
		@Override
		protected RAutoEditStrategy createRAutoEditStrategy() {
			return new RChunkAutoEditStrategy(getRCoreAccess(), getSourceEditor());
		}
		
	}
	
	
	private final LtxConfiguration docConfig;
	private final RChunkConfiguration rConfig;
	
	private ITextDoubleClickStrategy rDoubleClickStrategy;
	
	
	public LtxRweaveSourceViewerConfiguration() {
		this(null, null, null, null);
	}
	
	public LtxRweaveSourceViewerConfiguration(final ISourceEditor sourceEditor,
			final ITexCoreAccess texCoreAccess, final IRCoreAccess rCoreAccess,
			final IPreferenceStore preferenceStore) {
		super(LtxRweaveDocumentContentInfo.INSTANCE, sourceEditor);
		
		this.docConfig= new LtxConfiguration(LtxRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				texCoreAccess, preferenceStore );
		this.rConfig= new RChunkConfiguration(LtxRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				rCoreAccess, preferenceStore );
		
		registerConfig(LtxRweaveDocumentContentInfo.LTX, this.docConfig);
		registerConfig(LtxRweaveDocumentContentInfo.R, this.rConfig);
		
		setup((preferenceStore != null) ? preferenceStore : RedocsTexRPlugin.getInstance().getEditorPreferenceStore(),
				LTKUIPreferences.getEditorDecorationPreferences(),
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
	}
	
	
	@Override
	protected void initScanners() {
		final TextStyleManager textStyles= this.rConfig.getTextStyles();
		
		addScanner(ITexRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				new SingleTokenScanner(textStyles, IRTextTokens.UNDEFINED_KEY ));
	}
	
	@Override
	protected ITokenScanner getScanner(final String contentType) {
		if (contentType == ITexRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE) {
			return this.rConfig.getScanner(IRDocumentConstants.R_DEFAULT_CONTENT_TYPE);
		}
		if (contentType == ITexRweaveDocumentConstants.RCHUNK_COMMENT_CONTENT_TYPE) {
			return this.rConfig.getScanner(IRDocumentConstants.R_COMMENT_CONTENT_TYPE);
		}
		return super.getScanner(contentType);
	}
	
	protected void setCoreAccess(final ITexCoreAccess texCoreAccess, final IRCoreAccess rCoreAccess) {
		this.docConfig.setCoreAccess(texCoreAccess);
		this.rConfig.setCoreAccess(rCoreAccess);
	}
	
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return CONTENT_TYPES;
	}
	
	@Override
	protected void initPresentationReconciler(final PresentationReconciler reconciler) {
		super.initPresentationReconciler(reconciler);
		
		for (final String contentType : ITexRweaveDocumentConstants.RCHUNK_CONTENT_TYPES) {
			final ITokenScanner scanner= getScanner(contentType);
			if (scanner != null) {
				final DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);
				reconciler.setDamager(dr, contentType);
				reconciler.setRepairer(dr, contentType);
			}
		}
	}
	
	
	@Override
	public ICharPairMatcher createPairMatcher() {
		return new LtxRweaveBracketPairMatcher();
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		switch (LtxRweaveDocumentContentInfo.INSTANCE.getTypeByPartition(contentType)) {
		case LtxRweaveDocumentContentInfo.LTX:
			return this.docConfig.getDoubleClickStrategy(sourceViewer, contentType);
		case LtxRweaveDocumentContentInfo.R_CHUNK_CONTROL:
		case LtxRweaveDocumentContentInfo.R:
			if (this.rDoubleClickStrategy == null) {
				final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(getDocumentContentInfo());
				this.rDoubleClickStrategy= new RDoubleClickStrategy(scanner,
						LtxRweaveBracketPairMatcher.createRChunkPairMatcher(scanner) );
			}
			return this.rDoubleClickStrategy;
		default:
			return null;
		}
	}
	
	
	protected IReconcilingStrategy getSpellingStrategy(final ISourceViewer sourceViewer) {
		if (!(this.rConfig.getRCoreAccess().getPrefs().getPreferenceValue(TexRweaveEditingOptions.LTX_SPELLCHECK_ENABLED_PREF)
				&& this.fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) ) {
			return null;
		}
		final SpellingService spellingService= EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(this.fPreferenceStore) == null) {
			return null;
		}
		return new SpellingReconcileStrategy(sourceViewer, spellingService);
	}
	
	
	@Override
	protected void initContentAssist(final ContentAssist assistant) {
		super.initContentAssist(assistant);
		
		final IContentAssistComputer chunkComputer= new RChunkTemplatesCompletionComputer();
		
		final ContentAssistProcessor texProcessor= (ContentAssistProcessor) assistant.getContentAssistProcessor(
				ITexDocumentConstants.LTX_DEFAULT_CONTENT_TYPE );
		texProcessor.addCategory(new ContentAssistCategory(
				ITexDocumentConstants.LTX_DEFAULT_CONTENT_TYPE,
				ImCollections.newList(chunkComputer) ));
		texProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '\\', '<' });
		
		final ContentAssistProcessor mathProcessor= (ContentAssistProcessor) assistant.getContentAssistProcessor(
				ITexDocumentConstants.LTX_MATH_CONTENT_TYPE );
		mathProcessor.addCategory(new ContentAssistCategory(
				ITexDocumentConstants.LTX_MATH_CONTENT_TYPE,
				ImCollections.newList(chunkComputer) ));
		mathProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '\\', '<' });
		
		final ContentAssistProcessor controlProcessor= new ContentAssistProcessor(assistant,
				ITexRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				RedocsTexRPlugin.getInstance().getLtxRweaveEditorContentAssistRegistry(),
				getSourceEditor() );
		controlProcessor.addCategory(new ContentAssistCategory(
				ITexRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				ImCollections.newList(chunkComputer) ));
		assistant.setContentAssistProcessor(controlProcessor, ITexRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE);
		assistant.setContentAssistProcessor(controlProcessor, ITexRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE);
	}
	
	
	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		final ISourceEditor editor= getSourceEditor();
		if (!(editor instanceof SourceEditor1)) {
			return null;
		}
		final EcoReconciler2 reconciler= (EcoReconciler2) this.docConfig.getReconciler(sourceViewer);
		if (reconciler != null) {
			final IReconcilingStrategy spellingStrategy= getSpellingStrategy(sourceViewer);
			if (spellingStrategy != null) {
				reconciler.addReconcilingStrategy(spellingStrategy);
			}
		}
		return reconciler;
	}
	
	
	@Override
	protected IInformationProvider getQuickInformationProvider(final ISourceViewer sourceViewer,
			final int operation) {
		final ISourceEditor editor= getSourceEditor();
		if (editor == null) {
			return null;
		}
		switch (operation) {
		case SourceEditorViewer.SHOW_SOURCE_OUTLINE:
			return new DocRQuickOutlineInformationProvider(editor, operation);
		default:
			return null;
		}
	}
	
}
