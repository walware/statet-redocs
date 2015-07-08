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

package de.walware.statet.redocs.wikitext.r.ui.editors;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ForwardSourceDocumentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionHandler;
import de.walware.ecommons.ltk.ui.sourceediting.actions.SpecificContentAssistHandler;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingEditorAddon;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.markuphelp.IMarkupHelpContextProvider;
import de.walware.docmlet.base.ui.processing.actions.RunDocProcessingOnSaveExtension;
import de.walware.docmlet.base.ui.sourceediting.IDocEditor;
import de.walware.docmlet.wikitext.core.model.WikitextModel;
import de.walware.docmlet.wikitext.ui.WikitextUI;
import de.walware.docmlet.wikitext.ui.editors.WikidocDefaultFoldingProvider;
import de.walware.docmlet.wikitext.ui.sourceediting.WikitextEditingSettings;

import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.IREditor;
import de.walware.statet.r.ui.editors.RCorrectIndentHandler;
import de.walware.statet.r.ui.editors.RDefaultFoldingProvider;
import de.walware.statet.r.ui.editors.RMarkOccurrencesLocator;
import de.walware.statet.r.ui.sourceediting.InsertAssignmentHandler;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.internal.wikitext.r.ui.editors.WikidocRweaveEditorTemplatesPage;
import de.walware.statet.redocs.internal.wikitext.r.ui.editors.WikidocRweaveOutlinePage;
import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;
import de.walware.statet.redocs.r.ui.RedocsRUI;
import de.walware.statet.redocs.r.ui.sourceediting.actions.RweaveToggleCommentHandler;
import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentContentInfo;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveUI;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikidocRweaveSourceViewerConfigurator;


/**
 * Editor for Wikitext-R documents.
 */
public abstract class WikidocRweaveEditor extends SourceEditor1 implements IWikidocRweaveEditor,
		IDocEditor, IMarkupHelpContextProvider {
	
	
	private static final ImList<String> KEY_CONTEXTS= ImCollections.newIdentityList(
			WikitextUI.EDITOR_CONTEXT_ID,
			DocBaseUI.DOC_EDITOR_CONTEXT_ID,
			RedocsRUI.RWEAVE_EDITOR_CONTEXT_ID );
	
	private static final ImList<String> CONTEXT_IDS= ImCollections.concatList(
			ACTION_SET_CONTEXT_IDS, KEY_CONTEXTS );
	
	
	private static class ThisMarkOccurrencesProvider extends AbstractMarkOccurrencesProvider {
		
		
//		private final WikitextMarkOccurrencesLocator docLocator= new WikitextMarkOccurrencesLocator();
		private final RMarkOccurrencesLocator rLocator= new RMarkOccurrencesLocator();
		
		
		public ThisMarkOccurrencesProvider(final SourceEditor1 editor) {
			super(editor, IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT);
		}
		
		@Override
		protected void doUpdate(final RunData run, final ISourceUnitModelInfo info,
				final AstSelection astSelection, final ITextSelection orgSelection)
				throws BadLocationException, BadPartitioningException, UnsupportedOperationException {
//			if (astSelection.getCovering() instanceof WikitextAstNode) {
//				this.docLocator.run(run, info, astSelection, orgSelection);
//			}
//			else 
			if (astSelection.getCovering() instanceof RAstNode) {
				this.rLocator.run(run, info, astSelection, orgSelection);
			}
		}
		
	}
	
	private static class ThisCorrectIndentHandler extends RCorrectIndentHandler {
		
		public ThisCorrectIndentHandler(final IREditor editor) {
			super(editor);
		}
		
		@Override
		protected List<? extends IRegion> getCodeRanges(final AbstractDocument document,
				final ITextSelection selection) throws BadLocationException {
			return WikidocRweaveDocumentContentInfo.INSTANCE.getRChunkCodeRegions(document,
					selection.getOffset(), selection.getLength() );
		}
		
	}
	
	
	private final WikidocRweaveDocumentSetupParticipant documentSetup;
	
	private WikidocRweaveSourceViewerConfigurator combinedConfig;
	
	private final RunDocProcessingOnSaveExtension autoDocProcessing;
	
	
	public WikidocRweaveEditor(final IContentType contentType,
			final WikidocRweaveDocumentSetupParticipant documentSetup) {
		super(contentType);
		if (documentSetup == null) {
			throw new NullPointerException("documentSetup"); //$NON-NLS-1$
		}
		this.documentSetup= documentSetup;
		
		this.autoDocProcessing= new RunDocProcessingOnSaveExtension(this);
		
		initializeEditor();
	}
	
	
	@Override
	protected void initializeEditor() {
		if (this.documentSetup == null) {
			return;
		}
		
		super.initializeEditor();
		
		setEditorContextMenuId("de.walware.statet.redocs.menus.WikidocRweaveEditorContextMenu"); //$NON-NLS-1$
		setRulerContextMenuId("de.walware.statet.redocs.menus.WikidocRweaveEditorRulerMenu"); //$NON-NLS-1$
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		setDocumentProvider(new ForwardSourceDocumentProvider(
				RedocsWikitextRPlugin.getInstance().getDocRDocumentProvider(),
				this.documentSetup ));
		
		enableStructuralFeatures(WikitextModel.getWikidocModelManager(),
				WikitextEditingSettings.FOLDING_ENABLED_PREF,
				WikitextEditingSettings.MARKOCCURRENCES_ENABLED_PREF );
		
		this.combinedConfig= new WikidocRweaveSourceViewerConfigurator(
				this.documentSetup, null, null,
				new WikidocRweaveSourceViewerConfiguration(this, null, null, 0) );
		return this.combinedConfig;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setContexts(CONTEXT_IDS);
	}
	
	@Override
	protected ISourceEditorAddon createCodeFoldingProvider() {
		return new FoldingEditorAddon(new WikidocDefaultFoldingProvider(Collections.singletonMap(
				IRweaveMarkupLanguage.EMBEDDED_R, new RDefaultFoldingProvider() )));
	}
	
	@Override
	protected ISourceEditorAddon createMarkOccurrencesProvider() {
		return new ThisMarkOccurrencesProvider(this);
	}
	
	
	@Override
	public IDocContentSectionsRweaveExtension getDocumentContentInfo() {
		return (IDocContentSectionsRweaveExtension) super.getDocumentContentInfo();
	}
	
	@Override
	public IWikidocRweaveSourceUnit getSourceUnit() {
		return (IWikidocRweaveSourceUnit) super.getSourceUnit();
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		
		final IWikidocRweaveSourceUnit su= getSourceUnit();
		this.combinedConfig.setSource(
				(su != null) ? su.getWikitextCoreAccess() : null,
				(su != null) ? su.getRCoreAccess() : null );
		
		this.autoDocProcessing.setAutoRunEnabled(false);
	}
	
	
	@Override
	protected void handlePreferenceStoreChanged(final org.eclipse.jface.util.PropertyChangeEvent event) {
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(event.getProperty())
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(event.getProperty())) {
			return;
		}
		super.handlePreferenceStoreChanged(event);
	}
	
	
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return false;
	}
	
	public void updateSettings(final boolean indentChanged) {
		if (indentChanged) {
			updateIndentPrefixes();
		}
	}
	
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add(WikitextRweaveUI.EDITOR_PREF_PAGE_ID);
		pageIds.add(WikitextUI.EDITOR_PREF_PAGE_ID);
		pageIds.add("org.eclipse.mylyn.internal.wikitext.ui.editor.preferences.WikiTextTemplatePreferencePage"); //$NON-NLS-1$
//		pageIds.add("de.walware.docmlet.wikitext.preferencePages.WikitextCodeStyle"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStyles"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		final IHandlerService handlerService= (IHandlerService) getServiceLocator()
				.getService(IHandlerService.class);
		
		{	final IHandler2 handler= new InsertAssignmentHandler(this);
			handlerService.activateHandler(LTKUI.INSERT_ASSIGNMENT_COMMAND_ID, handler);
			markAsStateDependentHandler(handler, true);
		}
		{	final IHandler2 handler= new MultiContentSectionHandler(WikidocRweaveDocumentContentInfo.INSTANCE,
					WikidocRweaveDocumentContentInfo.R, new SpecificContentAssistHandler(this,
							RUIPlugin.getDefault().getREditorContentAssistRegistry() )
					);
			handlerService.activateHandler(ISourceEditorCommandIds.SPECIFIC_CONTENT_ASSIST_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected IHandler2 createToggleCommentHandler() {
		final IHandler2 handler= new RweaveToggleCommentHandler(this) {
			@Override
			protected Pattern getPrefixPattern(final String contentType, final String prefix) {
				if (prefix.equals("<!--")) { //$NON-NLS-1$
					return HTML_SPACE_PREFIX_PATTERN;
				}
				return super.getPrefixPattern(contentType, prefix);
			}
			@Override
			protected Pattern getPostfixPattern(final String contentType, final String prefix) {
				if (prefix.equals("<!--")) { //$NON-NLS-1$
					return HTML_SPACE_POSTFIX_PATTERN;
				}
				return super.getPostfixPattern(contentType, prefix);
			}
			@Override
			protected void doPrefixPrimary(final AbstractDocument document, final IRegion block)
					throws BadLocationException, BadPartitioningException {
				doPrefix(document, block, "<!-- ", " -->"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		markAsStateDependentHandler(handler, true);
		return handler;
	}
	
	@Override
	protected IHandler2 createCorrectIndentHandler() {
		final IHandler2 handler= new ThisCorrectIndentHandler(this);
		markAsStateDependentHandler(handler, true);
		return handler;
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager m) {
		super.editorContextMenuAboutToShow(m);
		final IWikidocRweaveSourceUnit su= getSourceUnit();
		
		m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID));
		final IContributionItem additions= m.find(SharedUIResources.ADDITIONS_MENU_ID);
		if (additions != null) {
			additions.setVisible(false);
		}
		
		m.remove(ITextEditorActionConstants.SHIFT_RIGHT);
		m.remove(ITextEditorActionConstants.SHIFT_LEFT);
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(
				new CommandContributionItemParameter(getSite(), null,
						RCodeLaunching.SUBMIT_SELECTION_COMMAND_ID,
						CommandContributionItem.STYLE_PUSH )));
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(
				new CommandContributionItemParameter(getSite(), null,
						RCodeLaunching.SUBMIT_UPTO_SELECTION_COMMAND_ID,
						CommandContributionItem.STYLE_PUSH )));
		
		m.appendToGroup(ITextEditorActionConstants.GROUP_SETTINGS, new CommandContributionItem(
				new CommandContributionItemParameter(getSite(), null,
						DocBaseUI.CONFIGURE_MARKUP_COMMAND_ID, null,
						null, null, null,
						"Configure Markup...", "M", null,
						CommandContributionItem.STYLE_PUSH, null, false )));
	}
	
	
	@Override
	protected SourceEditor1OutlinePage createOutlinePage() {
		return new WikidocRweaveOutlinePage(this);
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new WikidocRweaveEditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, RUI.R_HELP_VIEW_ID };
	}
	
	@Override
	public String getHelpContentId() {
		return WikitextUI.getMarkupHelpContentIdFor(this.documentSetup.getMarkupLanguage());
	}
	
	
	@Override
	protected void editorSaved() {
		super.editorSaved();
		
		this.autoDocProcessing.onEditorSaved();
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required == IMarkupHelpContextProvider.class) {
			return this;
		}
		if (required == RunDocProcessingOnSaveExtension.class) {
			return this.autoDocProcessing;
		}
		return super.getAdapter(required);
	}
	
}
