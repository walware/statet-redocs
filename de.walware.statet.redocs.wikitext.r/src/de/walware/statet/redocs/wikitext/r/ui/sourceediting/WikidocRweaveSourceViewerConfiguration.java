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

package de.walware.statet.redocs.wikitext.r.ui.sourceediting;

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

import de.walware.docmlet.tex.ui.sourceediting.LtxSourceViewerConfiguration;
import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;
import de.walware.docmlet.wikitext.core.source.IWikitextDocumentConstants;
import de.walware.docmlet.wikitext.ui.sourceediting.WikidocSourceViewerConfiguration;
import de.walware.eutils.yaml.ui.sourceediting.YamlSourceViewerConfiguration;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.core.source.RPartitionNodeType;
import de.walware.statet.r.ui.sourceediting.RAutoEditStrategy;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting.DocRQuickOutlineInformationProvider;
import de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting.RChunkTemplatesCompletionComputer;
import de.walware.statet.redocs.wikitext.r.core.source.IWikitextRweaveDocumentConstants;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveBracketPairMatcher;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentContentInfo;


/**
 * Default Configuration for source viewer of Wikitext-R documents.
 */
public class WikidocRweaveSourceViewerConfiguration extends MultiContentSectionSourceViewerConfiguration {
	
	
	private static final String[] CONTENT_TYPES= IWikitextRweaveDocumentConstants.WIKIDOC_R_CONTENT_TYPES.toArray(
			new String[IWikitextRweaveDocumentConstants.WIKIDOC_R_CONTENT_TYPES.size()]);
	
	
	private static class WikidocConfiguration extends WikidocSourceViewerConfiguration {
		
		public WikidocConfiguration(final IDocContentSections documentContentInfo,
				final ISourceEditor editor,
				final IWikitextCoreAccess wikitextCoreAccess,
				final IPreferenceStore preferenceStore,
				final int styleFlags) {
			super(documentContentInfo, editor, wikitextCoreAccess, preferenceStore, styleFlags);
		}
		
		@Override
		protected void setCoreAccess(final IWikitextCoreAccess access) {
			super.setCoreAccess(access);
		}
		
	}
	
	
	private static class RChunkAutoEditStrategy extends RAutoEditStrategy {
		
		public RChunkAutoEditStrategy(final IRCoreAccess coreAccess, final ISourceEditor sourceEditor) {
			super(coreAccess, sourceEditor);
		}
		
		@Override
		protected IRegion getValidRange(final int offset, final TreePartition partition, final int c) {
			switch (WikidocRweaveDocumentContentInfo.INSTANCE.getTypeByPartition(partition.getType())) {
			case WikidocRweaveDocumentContentInfo.R:
				return TreePartitionUtil.searchNodeUp(partition.getTreeNode(),
						RPartitionNodeType.DEFAULT_ROOT );
			case WikidocRweaveDocumentContentInfo.R_CHUNK_CONTROL:
				switch (c) {
				case '(':
				case '[':
				case '{':
				case '%':
				case '\"':
				case '\'':
					return TreePartitionUtil.searchPartitionRegion(partition,
							IWikitextRweaveDocumentConstants.RCHUNK_PARTITION_CONSTRAINT );
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
				final IRCoreAccess coreAccess, final IPreferenceStore preferenceStore) {
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
	
	
	private final WikidocConfiguration docConfig;
	private final YamlSourceViewerConfiguration yamlConfig;
	private final LtxSourceViewerConfiguration ltxConfig;
	private final RChunkConfiguration rConfig;
	
	private ITextDoubleClickStrategy rDoubleClickStrategy;
	
	
	public WikidocRweaveSourceViewerConfiguration() {
		this(null, null, null, null, WikidocSourceViewerConfiguration.FIXED_LINE_HEIGHT_STYLE);
	}
	
	public WikidocRweaveSourceViewerConfiguration(final ISourceEditor sourceEditor,
			final IWikitextCoreAccess wikitextCoreAccess, final IRCoreAccess rCoreAccess,
			final IPreferenceStore preferenceStore,
			final int styleFlags) {
		super(WikidocRweaveDocumentContentInfo.INSTANCE, sourceEditor);
		
		this.docConfig= new WikidocConfiguration(WikidocRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				wikitextCoreAccess, preferenceStore, styleFlags );
		this.yamlConfig= new YamlSourceViewerConfiguration(WikidocRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				null, null, null );
		this.ltxConfig= new LtxSourceViewerConfiguration(WikidocRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				null, null, null );
		this.rConfig= new RChunkConfiguration(WikidocRweaveDocumentContentInfo.INSTANCE, sourceEditor,
				rCoreAccess, preferenceStore );
		
		registerConfig(WikidocRweaveDocumentContentInfo.WIKITEXT, this.docConfig);
		registerConfig(WikidocRweaveDocumentContentInfo.YAML, this.yamlConfig);
		registerConfig(WikidocRweaveDocumentContentInfo.LTX, this.ltxConfig);
		registerConfig(WikidocRweaveDocumentContentInfo.R, this.rConfig);
		
		setup((preferenceStore != null) ? preferenceStore : RedocsWikitextRPlugin.getInstance().getEditorPreferenceStore(),
				LTKUIPreferences.getEditorDecorationPreferences(),
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
	}
	
	
	@Override
	protected void initScanners() {
		final TextStyleManager textStyles= this.rConfig.getTextStyles();
		
		addScanner(IWikitextRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				new SingleTokenScanner(textStyles, IRTextTokens.UNDEFINED_KEY ));
	}
	
	@Override
	protected ITokenScanner getScanner(final String contentType) {
		if (contentType == IWikitextRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE) {
			return this.rConfig.getScanner(IRDocumentConstants.R_DEFAULT_CONTENT_TYPE);
		}
		if (contentType == IWikitextRweaveDocumentConstants.RCHUNK_COMMENT_CONTENT_TYPE) {
			return this.rConfig.getScanner(IRDocumentConstants.R_COMMENT_CONTENT_TYPE);
		}
		return super.getScanner(contentType);
	}
	
	protected void setCoreAccess(final IWikitextCoreAccess wikitextCoreAccess, final IRCoreAccess rCoreAccess) {
		this.docConfig.setCoreAccess(wikitextCoreAccess);
		this.rConfig.setCoreAccess(rCoreAccess);
	}
	
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return CONTENT_TYPES;
	}
	
	@Override
	protected void initPresentationReconciler(final PresentationReconciler reconciler) {
		super.initPresentationReconciler(reconciler);
		
		for (final String contentType : IWikitextRweaveDocumentConstants.RCHUNK_CONTENT_TYPES) {
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
		return new WikidocRweaveBracketPairMatcher();
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		switch (WikidocRweaveDocumentContentInfo.INSTANCE.getTypeByPartition(contentType)) {
		case WikidocRweaveDocumentContentInfo.WIKITEXT:
			return this.docConfig.getDoubleClickStrategy(sourceViewer, contentType);
		case WikidocRweaveDocumentContentInfo.R_CHUNK_CONTROL:
		case WikidocRweaveDocumentContentInfo.R:
			if (this.rDoubleClickStrategy == null) {
				final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(getDocumentContentInfo());
				this.rDoubleClickStrategy= new RDoubleClickStrategy(scanner,
						WikidocRweaveBracketPairMatcher.createRChunkPairMatcher(scanner) );
			}
			return this.rDoubleClickStrategy;
		default:
			return null;
		}
	}
	
	
	protected IReconcilingStrategy getSpellingStrategy(final ISourceViewer sourceViewer) {
		if (!(this.rConfig.getRCoreAccess().getPrefs().getPreferenceValue(WikitextRweaveEditingSettings.WIKIDOC_SPELLCHECK_ENABLED_PREF)
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
		
		final ContentAssistProcessor wikitextProcessor= (ContentAssistProcessor) assistant.getContentAssistProcessor(
				IWikitextDocumentConstants.WIKIDOC_DEFAULT_CONTENT_TYPE );
		wikitextProcessor.addCategory(new ContentAssistCategory(
				IWikitextDocumentConstants.WIKIDOC_DEFAULT_CONTENT_TYPE,
				ImCollections.newList(chunkComputer) ));
//		wikitextProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '\\', '<' });
		
		final ContentAssistProcessor controlProcessor= new ContentAssistProcessor(assistant,
				IWikitextRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				RedocsWikitextRPlugin.getInstance().getWikidocRweaveEditorContentAssistRegistry(),
				getSourceEditor() );
		controlProcessor.addCategory(new ContentAssistCategory(IWikitextRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE,
				ImCollections.newList(chunkComputer) ));
		assistant.setContentAssistProcessor(controlProcessor, IWikitextRweaveDocumentConstants.RCHUNK_BASE_CONTENT_TYPE);
		assistant.setContentAssistProcessor(controlProcessor, IWikitextRweaveDocumentConstants.RCHUNK_CONTROL_CONTENT_TYPE);
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
