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

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
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
import de.walware.ecommons.ltk.ui.EditorUtil;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionHandler;
import de.walware.ecommons.ltk.ui.sourceediting.actions.SpecificContentAssistHandler;
import de.walware.ecommons.ltk.ui.sourceediting.actions.ToggleCommentHandler;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingEditorAddon;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.sourceediting.IDocEditor;
import de.walware.docmlet.tex.core.ast.TexAstNode;
import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.ui.TexUI;
import de.walware.docmlet.tex.ui.editors.LtxDefaultFoldingProvider;
import de.walware.docmlet.tex.ui.editors.TexMarkOccurrencesLocator;
import de.walware.docmlet.tex.ui.sourceediting.TexEditingSettings;

import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.r.core.model.RModel;
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

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;
import de.walware.statet.redocs.r.ui.RedocsRUI;
import de.walware.statet.redocs.tex.r.core.TexRweaveCore;
import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;
import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;
import de.walware.statet.redocs.tex.r.ui.editors.ILtxRweaveEditor;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfiguration;
import de.walware.statet.redocs.tex.r.ui.sourceediting.LtxRweaveSourceViewerConfigurator;


/**
 * Editor for Sweave (LaTeX+R) code.
 */
public class LtxRweaveEditor extends SourceEditor1 implements ILtxRweaveEditor, IDocEditor {
	
	
	private static final ImList<String> KEY_CONTEXTS= ImCollections.newIdentityList(
			TexUI.EDITOR_CONTEXT_ID,
			DocBaseUI.DOC_EDITOR_CONTEXT_ID,
			RedocsRUI.RWEAVE_EDITOR_CONTEXT_ID );
			
	private static final ImList<String> CONTEXT_IDS= ImCollections.concatList(
			ACTION_SET_CONTEXT_IDS, KEY_CONTEXTS );
	
	
	private static class ThisMarkOccurrencesProvider extends AbstractMarkOccurrencesProvider {
		
		
		private final TexMarkOccurrencesLocator docLocator= new TexMarkOccurrencesLocator();
		private final RMarkOccurrencesLocator rLocator= new RMarkOccurrencesLocator();
		
		
		public ThisMarkOccurrencesProvider(final SourceEditor1 editor) {
			super(editor, IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT);
		}
		
		@Override
		protected void doUpdate(final RunData run, final ISourceUnitModelInfo info,
				final AstSelection astSelection, final ITextSelection orgSelection)
				throws BadLocationException, BadPartitioningException, UnsupportedOperationException {
			if (astSelection.getCovering() instanceof TexAstNode) {
				this.docLocator.run(run, info, astSelection, orgSelection);
			}
			else if (astSelection.getCovering() instanceof RAstNode) {
				this.rLocator.run(run, info, astSelection, orgSelection);
			}
		}
		
	}
	
	
	private static class ThisToggleCommentHandler extends ToggleCommentHandler {
		
		ThisToggleCommentHandler(final LtxRweaveEditor editor) {
			super(editor);
		}
		
		
		@Override
		protected void run(final IDocument document, final ITextSelection selection,
				final int operationCode) {
			try {
				if (operationCode == ITextOperationTarget.PREFIX && isMixed(document, selection)) {
					final IRegion block= EditorUtil.getTextBlockFromSelection(document,
							selection.getOffset(), selection.getLength() );
					addPrefix(document, block, "%"); //$NON-NLS-1$
					return;
				}
			}
			catch (final BadLocationException e) {
				log(e);
			}
			super.run(document, selection, operationCode);
		}
		
		protected boolean isMixed(final IDocument document, final ITextSelection selection) throws BadLocationException {
			final IRegion block= EditorUtil.getTextBlockFromSelection(document,
					selection.getOffset(), selection.getLength() );
			final IRegion rContent= LtxRweaveDocumentContentInfo.INSTANCE.getRChunkContentRegion(
					document, block.getOffset() );
			return (rContent == null || block.getOffset() < rContent.getOffset()
					|| block.getOffset() + block.getLength() > rContent.getOffset() + rContent.getLength() );
		}
		
	}
	
	private static class DocContentSectionIndentHandler extends RCorrectIndentHandler {
		
		public DocContentSectionIndentHandler(final IREditor editor) {
			super(editor);
		}
		
		@Override
		protected List<? extends IRegion> getCodeRanges(final AbstractDocument document,
				final ITextSelection selection) throws BadLocationException {
			return LtxRweaveDocumentContentInfo.INSTANCE.getRChunkCodeRegions(document,
					selection.getOffset(), selection.getLength() );
		}
	}
	
	
	private LtxRweaveSourceViewerConfigurator combinedConfig;
	
	
	public LtxRweaveEditor() {
		super(TexRweaveCore.LTX_R_CONTENT_TYPE);
	}
	
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setEditorContextMenuId("de.walware.statet.redocs.menus.LtxRweaveEditorContextMenu"); //$NON-NLS-1$
		setRulerContextMenuId("de.walware.statet.redocs.menus.LtxRweaveEditorRulerMenu"); //$NON-NLS-1$
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		setDocumentProvider(RedocsTexRPlugin.getInstance().getDocRDocumentProvider());
		
		enableStructuralFeatures(TexModel.getLtxModelManager(),
				TexEditingSettings.FOLDING_ENABLED_PREF,
				TexEditingSettings.MARKOCCURRENCES_ENABLED_PREF );
		
		this.combinedConfig= new LtxRweaveSourceViewerConfigurator(null, null,
				new LtxRweaveSourceViewerConfiguration(this, null, null) );
		return this.combinedConfig;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setContexts(CONTEXT_IDS);
	}
	
	@Override
	protected ISourceEditorAddon createCodeFoldingProvider() {
		return new FoldingEditorAddon(new LtxDefaultFoldingProvider(Collections.singletonMap(
				RModel.R_TYPE_ID, new RDefaultFoldingProvider() )));
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
	public ILtxRweaveSourceUnit getSourceUnit() {
		return (ILtxRweaveSourceUnit) super.getSourceUnit();
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		
		final ILtxRweaveSourceUnit su= getSourceUnit();
		this.combinedConfig.setSource(
				(su != null) ? su.getTexCoreAccess() : null,
				(su != null) ? su.getRCoreAccess() : null );
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
		pageIds.add(TexRweaveUI.EDITOR_PREF_PAGE_ID);
		pageIds.add(TexUI.EDITOR_PREF_PAGE_ID);
		pageIds.add("de.walware.docmlet.tex.preferencePages.LtxTextStyles"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.LtxEditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.TexCodeStyle"); //$NON-NLS-1$
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
		{	final IHandler2 handler= new MultiContentSectionHandler(LtxRweaveDocumentContentInfo.INSTANCE,
					LtxRweaveDocumentContentInfo.R, new SpecificContentAssistHandler(this,
							RUIPlugin.getDefault().getREditorContentAssistRegistry() )
					);
			handlerService.activateHandler(ISourceEditorCommandIds.SPECIFIC_CONTENT_ASSIST_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected IHandler2 createToggleCommentHandler() {
		final IHandler2 handler= new ThisToggleCommentHandler(this);
		markAsStateDependentHandler(handler, true);
		return handler;
	}
	
	@Override
	protected IHandler2 createCorrectIndentHandler() {
		final IHandler2 handler= new DocContentSectionIndentHandler(this);
		markAsStateDependentHandler(handler, true);
		return handler;
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager m) {
		super.editorContextMenuAboutToShow(m);
		final ILtxRweaveSourceUnit su= getSourceUnit();
		
		m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID));
		final IContributionItem additions= m.find(SharedUIResources.ADDITIONS_MENU_ID);
		if (additions != null) {
			additions.setVisible(false);
		}
		
		m.remove(ITextEditorActionConstants.SHIFT_RIGHT);
		m.remove(ITextEditorActionConstants.SHIFT_LEFT);
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.SUBMIT_SELECTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.SUBMIT_UPTO_SELECTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
	}
	
	
	@Override
	protected SourceEditor1OutlinePage createOutlinePage() {
		return new LtxRweaveOutlinePage(this);
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new LtxRweaveEditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, RUI.R_HELP_VIEW_ID };
	}
	
}
