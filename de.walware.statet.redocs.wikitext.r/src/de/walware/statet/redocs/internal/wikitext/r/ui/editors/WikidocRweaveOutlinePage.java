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

package de.walware.statet.redocs.internal.wikitext.r.ui.editors;

import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.core.model.IEmbeddedForeignElement;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.IModelElement.Filter;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor2OutlinePage;
import de.walware.ecommons.ltk.ui.util.ViewerDragSupport;
import de.walware.ecommons.ltk.ui.util.ViewerDropSupport;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.ViewerUtil;

import de.walware.docmlet.base.ui.DocBaseUIResources;
import de.walware.docmlet.wikitext.core.model.IWikidocModelInfo;
import de.walware.docmlet.wikitext.core.model.IWikitextSourceElement;
import de.walware.docmlet.wikitext.core.model.WikitextModel;

import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.launching.RCodeLaunching;

import de.walware.statet.redocs.internal.wikitext.r.RedocsWikitextRPlugin;
import de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting.DocROutlineContentProvider;
import de.walware.statet.redocs.r.ui.RedocsRUIResources;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveLabelProvider;


public class WikidocRweaveOutlinePage extends SourceEditor2OutlinePage {
	
	
	private static final String EXPAND_ELEMENTS_COMMAND_ID= "de.walware.ecommons.base.commands.ExpandElements"; //$NON-NLS-1$
	
	
	private static boolean isRChunk(final IModelElement element) {
		if (element instanceof IEmbeddedForeignElement) {
			final ISourceStructElement foreignElement= ((IEmbeddedForeignElement) element).getForeignElement();
			if (foreignElement != null && foreignElement.getModelTypeId() == RModel.R_TYPE_ID) {
				return true;
			}
		}
		return false;
	}
	
	
	private static Map<String, String> H2_PARAMETERS= Collections.singletonMap("type", "h2"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> H3_PARAMETERS= Collections.singletonMap("type", "h3"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> H4_PARAMETERS= Collections.singletonMap("type", "h4"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> RCHUNK_PARAMETERS= Collections.singletonMap("type", "rchunks"); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private class FilterRChunks extends AbstractToggleHandler {
		
		public FilterRChunks() {
			super("filter.r_chunks.enabled", false, null, 0); //$NON-NLS-1$
		}
		
		@Override
		protected void apply(final boolean on) {
			final TreeViewer viewer= getViewer();
			WikidocRweaveOutlinePage.this.filter.hideRChunks= on;
			if (UIAccess.isOkToUse(viewer)) {
				viewer.refresh(false);
			}
		}
		
	}
	
	public class ExpandElementsContributionItem extends HandlerContributionItem {
		
		private final IHandler2 expandElementsHandler;
		
		public ExpandElementsContributionItem(
				final IServiceLocator serviceLocator, final HandlerCollection handlers) {
			super(new CommandContributionItemParameter(serviceLocator,
					".ExpandElements", NAVIGATE_EXPAND_ALL, null, //$NON-NLS-1$
					null, null, null,
					"Expand All", "E", null,
					HandlerContributionItem.STYLE_PULLDOWN, null, false ),
					handlers.get(NAVIGATE_EXPAND_ALL) );
			this.expandElementsHandler= handlers.get(EXPAND_ELEMENTS_COMMAND_ID);
		}
		
		
		@Override
		protected void initDropDownMenu(final MenuManager menuManager) {
			menuManager.addMenuListener(new IMenuListener2() {
				@Override
				public void menuAboutToShow(final IMenuManager manager) {
					final TreeViewer viewer= getViewer();
					if (viewer == null) {
						return;
					}
					
					final IWikidocModelInfo modelInfo= getCurrentInputModel();
					final DocBaseUIResources baseResources= DocBaseUIResources.INSTANCE;
					final RedocsRUIResources redocsResources= RedocsRUIResources.INSTANCE;
					if (modelInfo.getMinSectionLevel() > 0) {
						if (modelInfo.getMinSectionLevel() < 2
								&& modelInfo.getMaxSectionLevel() >= 2) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, H2_PARAMETERS,
									baseResources.getImageDescriptor(DocBaseUIResources.OBJ_HEADING2_IMAGE_ID), null, null,
									"Show all H2 sections", "C", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									ExpandElementsContributionItem.this.expandElementsHandler ));
						}
						if (modelInfo.getMinSectionLevel() < 3
								&& modelInfo.getMaxSectionLevel() >= 3) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, H3_PARAMETERS,
									baseResources.getImageDescriptor(DocBaseUIResources.OBJ_HEADING3_IMAGE_ID), null, null,
									"Show all H3 sections", "S", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									ExpandElementsContributionItem.this.expandElementsHandler ));
						}
						if (modelInfo.getMinSectionLevel() < 4
								&& modelInfo.getMaxSectionLevel() >= 4) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, H4_PARAMETERS,
									baseResources.getImageDescriptor(DocBaseUIResources.OBJ_HEADING4_IMAGE_ID), null, null,
									"Show all H4 sections", "u", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									ExpandElementsContributionItem.this.expandElementsHandler ));
							manager.add(new Separator());
						}
					}
					if (!WikidocRweaveOutlinePage.this.filter.hideRChunks) {
						manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
								getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, RCHUNK_PARAMETERS,
								redocsResources.getImageDescriptor(RedocsRUIResources.OBJ_RCHUNK_IMAGE_ID), null, null,
								"Show all R Chunks", "R", null,
								HandlerContributionItem.STYLE_PUSH, null, false ),
								ExpandElementsContributionItem.this.expandElementsHandler ));
					}
				}
				@Override
				public void menuAboutToHide(final IMenuManager manager) {
					ExpandElementsContributionItem.this.display.asyncExec(new Runnable() {
						@Override
						public void run() {
							menuManager.dispose();
						}
					});
				}
			});
		}
		
	}
	
	public class ExpandElementsHandler extends AbstractHandler {
		
		public ExpandElementsHandler() {
		}
		
		@Override
		public Object execute(final ExecutionEvent event) {
			final TreeViewer viewer= getViewer();
			final String type= event.getParameter("type");
			if (UIAccess.isOkToUse(viewer) && type != null) {
				final ISourceUnitModelInfo modelInfo= getModelInfo(viewer.getInput());
				if (modelInfo == null) {
					return null;
				}
				final Filter contentFilter= getContentFilter();
				final Filter expandFilter;
				if (type.equals("rchunks")) {
					expandFilter= new Filter() {
						@Override
						public boolean include(final IModelElement element) {
							if (contentFilter.include(element)) {
								if ((element.getElementType() & IModelElement.MASK_C1) == IWikitextSourceElement.C1_EMBEDDED
										&& isRChunk(element)) {
									ViewerUtil.expandToLevel(viewer, element, 0);
									return false;
								}
								((ISourceStructElement) element).hasSourceChildren(this);
								return false;
							}
							return false;
						}
					};
				}
				else {
					final int sectionLevel;
					if (type.equals("h2")) { //$NON-NLS-1$
						sectionLevel= 2;
					}
					else if (type.equals("h3")) { //$NON-NLS-1$
						sectionLevel= 3;
					}
					else if (type.equals("h4")) { //$NON-NLS-1$
						sectionLevel= 4;
					}
					else if (type.equals("h5")) { //$NON-NLS-1$
						sectionLevel= 5;
					}
					else if (type.equals("h6")) { //$NON-NLS-1$
						sectionLevel= 6;
					}
					else {
						sectionLevel= 0;
					}
					if (sectionLevel < 1 || sectionLevel > 6) {
						return null;
					}
					expandFilter= new Filter() {
						private boolean childExpand;
						@Override
						public boolean include(final IModelElement element) {
							if (contentFilter.include(element)
									&& (element.getElementType() & IModelElement.MASK_C2) == IWikitextSourceElement.C2_SECTIONING) {
								final int currentLevel= (element.getElementType() & 0xf);
								if (currentLevel < 1 || currentLevel > sectionLevel) {
									return false; // nothing to do
								}
								if (currentLevel < sectionLevel) {
									this.childExpand= false;
									((ISourceStructElement) element).hasSourceChildren(this);
									if (this.childExpand) {
										return false; // done
									}
								}
								// expand
								ViewerUtil.expandToLevel(viewer, element, 0);
								this.childExpand= true;
								return false;
							}
							return false;
						}
					};
				}
				modelInfo.getSourceElement().hasSourceChildren(expandFilter);
			}
			return null;
		}
	}
	
	
	private class ContentFilter implements IModelElement.Filter {
		
		private boolean hideRChunks;
		
		@Override
		public boolean include(final IModelElement element) {
			switch ((element.getElementType() & IModelElement.MASK_C1)) {
			case IWikitextSourceElement.C1_EMBEDDED:
				if (isRChunk(element)) {
					return !this.hideRChunks;
				}
			}
			return true;
		};
		
	}
	
	
	private final ContentFilter filter= new ContentFilter();
	
	
	public WikidocRweaveOutlinePage(final SourceEditor1 editor) {
		super(editor, WikitextModel.WIKIDOC_TYPE_ID, WikidocRweaveRefactoringFactory.getInstance(),
				"de.walware.docmlet.wikitext.menus.WikidocOutlineViewContextMenu"); //$NON-NLS-1$
	}
	
	
	@Override
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RedocsWikitextRPlugin.getInstance(), "WikidocRweaveOutlineView"); //$NON-NLS-1$
	}
	
	@Override
	protected OutlineContentProvider createContentProvider() {
		return new DocROutlineContentProvider(new OutlineContent());
	}
	
	@Override
	protected IModelElement.Filter getContentFilter() {
		return this.filter;
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new WikitextRweaveLabelProvider(0));
		
		final ViewerDropSupport drop= new ViewerDropSupport(viewer, this,
				getRefactoringFactory() );
		drop.init();
		final ViewerDragSupport drag= new ViewerDragSupport(viewer);
		drag.init();
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator,
			final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		
		final IHandlerService handlerService= (IHandlerService) serviceLocator.getService(IHandlerService.class);
		
		handlers.add(".FilterRChunks", new FilterRChunks()); //$NON-NLS-1$
		
		{	final IHandler2 handler= new ExpandElementsHandler();
			handlers.add(EXPAND_ELEMENTS_COMMAND_ID, handler);
//			handlerService.activateHandler(EXPAND_ELEMENTS_COMMAND_ID, handler); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		super.contributeToActionBars(serviceLocator, actionBars, handlers);
		
		final IToolBarManager toolBarManager= actionBars.getToolBarManager();
		final RedocsRUIResources redocsResources= RedocsRUIResources.INSTANCE;
		
		toolBarManager.appendToGroup(SharedUIResources.VIEW_EXPAND_MENU_ID,
				new ExpandElementsContributionItem(serviceLocator, handlers));
		
//		toolBarManager.appendToGroup(ECommonsUI.VIEW_SORT_MENU_ID,
//				new AlphaSortAction());
		toolBarManager.appendToGroup(SharedUIResources.VIEW_FILTER_MENU_ID,
				new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, HandlerContributionItem.NO_COMMAND_ID, null,
						redocsResources.getImageDescriptor(RedocsRUIResources.LOCTOOL_FILTERCHUNKS_IMAGE_ID), null, null,
						"Hide R Chunks", "R", null,
						HandlerContributionItem.STYLE_CHECK, null, false ),
						handlers.get(".FilterRChunks") ) ); //$NON-NLS-1$
//		toolBarManager.appendToGroup(ECommonsUI.VIEW_FILTER_MENU_ID,
//				new FilterLocalDefinitions());
	}
	
	@Override
	protected void contextMenuAboutToShow(final IMenuManager m) {
		super.contextMenuAboutToShow(m);
		final IPageSite site= getSite();
		
		if (m.find(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID) == null) {
			m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID,
					new Separator(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID) );
		}
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, 
				new CommandContributionItem(new CommandContributionItemParameter(
						site, null, RCodeLaunching.SUBMIT_SELECTION_COMMAND_ID, null,
						null, null, null,
						null, "R", null, //$NON-NLS-1$
						CommandContributionItem.STYLE_PUSH, null, false) ));
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, 
				new CommandContributionItem(new CommandContributionItemParameter(
						site, null, RCodeLaunching.SUBMIT_UPTO_SELECTION_COMMAND_ID, null,
						null, null, null,
						null, "U", null, //$NON-NLS-1$
						CommandContributionItem.STYLE_PUSH, null, false) ));
		
		m.add(new Separator(IStatetUIMenuIds.GROUP_ADD_MORE_ID));
	}
	
	protected IWikidocModelInfo getCurrentInputModel() {
		final TreeViewer viewer= getViewer();
		if (viewer == null) {
			return null;
		}
		return (IWikidocModelInfo) getModelInfo(viewer.getInput());
	}
	
}
