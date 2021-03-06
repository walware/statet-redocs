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

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.IHandler2;
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingEditorAddon;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.sourceediting.IDocEditor;
import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.ast.TexAstNode;
import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.ui.TexUI;
import de.walware.docmlet.tex.ui.editors.LtxDefaultFoldingProvider;
import de.walware.docmlet.tex.ui.editors.TexMarkOccurrencesLocator;
import de.walware.docmlet.tex.ui.sourceediting.TexEditingSettings;

import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.IRSourceEditor;
import de.walware.statet.r.ui.editors.RCorrectIndentHandler;
import de.walware.statet.r.ui.editors.RDefaultFoldingProvider;
import de.walware.statet.r.ui.editors.RMarkOccurrencesLocator;
import de.walware.statet.r.ui.sourceediting.InsertAssignmentHandler;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;
import de.walware.statet.redocs.r.ui.RedocsRUI;
import de.walware.statet.redocs.r.ui.sourceediting.actions.RweaveToggleCommentHandler;
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
	
	private static class DocContentSectionIndentHandler extends RCorrectIndentHandler {
		
		public DocContentSectionIndentHandler(final IRSourceEditor editor) {
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
		
		this.combinedConfig= new LtxRweaveSourceViewerConfigurator(
				TexCore.WORKBENCH_ACCESS, RCore.WORKBENCH_ACCESS,
				new LtxRweaveSourceViewerConfiguration(this, null, null,null) );
		return this.combinedConfig;
	}
	
	@Override
	protected Collection<String> getContextIds() {
		return CONTEXT_IDS;
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
	public IRCoreAccess getRCoreAccess() {
		return this.combinedConfig.getRCoreAccess();
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
				(su != null) ? su.getTexCoreAccess() : TexCore.WORKBENCH_ACCESS,
				(su != null) ? su.getRCoreAccess() : RCore.WORKBENCH_ACCESS );
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
	}
	
	@Override
	protected IHandler2 createToggleCommentHandler() {
		final IHandler2 handler= new RweaveToggleCommentHandler(this) {
			@Override
			protected void doPrefixPrimary(final AbstractDocument document, final IRegion block)
					throws BadLocationException, BadPartitioningException {
				doPrefix(document, block, "%"); //$NON-NLS-1$
			}
		};
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
