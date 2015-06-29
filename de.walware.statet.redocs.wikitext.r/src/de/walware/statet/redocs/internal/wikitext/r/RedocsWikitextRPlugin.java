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

package de.walware.statet.redocs.internal.wikitext.r;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.ui.LTKUIPreferences;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.templates.WaContributionContextTypeRegistry;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.docmlet.wikitext.ui.sourceediting.WikitextEditingSettings;

import de.walware.statet.r.internal.ui.RUIPlugin;

import de.walware.statet.redocs.internal.wikitext.r.ui.editors.WikidocRweaveDocumentProvider;
import de.walware.statet.redocs.wikitext.r.core.WikitextRweaveCore;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveUI;


/**
 * The activator class controls the plug-in life cycle
 */
public class RedocsWikitextRPlugin extends AbstractUIPlugin {
	
	
/*- Images --------------------------------------------------------------------*/
	
	private static final String NS= "de.walware.statet.redocs.wikitext.r"; //$NON-NLS-1$
	
	public static final String OBJ_WIKIDOCRWEAVE_IMAGE_ID= NS + "/image/obj/WikidocRweave"; //$NON-NLS-1$
	
	public static final String TOOL_NEW_WIKIDOCRWEAVE_IMAGE_ID= NS + "/image/tool/New-WikidocRweave"; //$NON-NLS-1$
	
	public static final String WIZBAN_NEW_WIKIDOCRWEAVE_FILE_IMAGE_ID= NS + "/image/wizban/New-WikidocRweaveFile"; //$NON-NLS-1$
	
/*- Prefs ---------------------------------------------------------------------*/
	
	public static final String WIKITEXT_RWEAVE_EDITOR_NODE= WikitextRweaveUI.PLUGIN_ID + "/rweavetex.editor/options"; //$NON-NLS-1$
	
	
	// The shared instance
	private static RedocsWikitextRPlugin instance;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static RedocsWikitextRPlugin getInstance() {
		return instance;
	}
	
	public static void log(final IStatus status) {
		final Plugin plugin= getInstance();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean started;
	
	private List<IDisposable> disposables;
	
	private WikidocRweaveDocumentProvider docRDocumentProvider;
	
	private IPreferenceStore editorPreferenceStore;
	
	private ContextTypeRegistry codegenTemplateContextTypeRegistry;
	private TemplateStore codegenTemplatesStore;
	
	private ContextTypeRegistry docTemplateContextTypeRegistry;
	private TemplateStore docTemplatesStore;
	
	private ContentAssistComputerRegistry wikidocRweaveEditorContentAssistRegistry;
	
	
	/**
	 * The constructor
	 */
	public RedocsWikitextRPlugin() {
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance= this;
		
		this.disposables= new ArrayList<>();
		
		this.started= true;
	}
	
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			if (this.codegenTemplatesStore != null) {
				this.codegenTemplatesStore.stopListeningForPreferenceChanges();
			}
			
			synchronized (this) {
				this.started= false;
				
				this.editorPreferenceStore= null;
				this.codegenTemplateContextTypeRegistry= null;
				this.codegenTemplatesStore= null;
				this.docTemplateContextTypeRegistry= null;
				this.docTemplatesStore= null;
				this.wikidocRweaveEditorContentAssistRegistry= null;
			}
			
			for (final IDisposable listener : this.disposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
							"Error occured while disposing a module.", e )); 
				}
			}
			this.disposables= null;
		}
		finally {
			instance= null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util= new ImageRegistryUtil(this);
		
		util.register(OBJ_WIKIDOCRWEAVE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "wikidoc_rweave-file.png"); //$NON-NLS-1$
		
		util.register(TOOL_NEW_WIKIDOCRWEAVE_IMAGE_ID, ImageRegistryUtil.T_TOOL, "new-wikidoc_rweave-file.png"); //$NON-NLS-1$
		
		util.register(WIZBAN_NEW_WIKIDOCRWEAVE_FILE_IMAGE_ID, ImageRegistryUtil.T_WIZBAN, "new-wikidoc_rweave-file.png"); //$NON-NLS-1$
	}
	
	
	public synchronized WikidocRweaveDocumentProvider getDocRDocumentProvider() {
		if (this.docRDocumentProvider == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.docRDocumentProvider= new WikidocRweaveDocumentProvider();
			this.disposables.add(this.docRDocumentProvider);
		}
		return this.docRDocumentProvider;
	}
	
	
	/**
	 * Returns the template context type registry for the code generation templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getCodegenTemplateContextTypeRegistry() {
		if (this.codegenTemplateContextTypeRegistry == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.codegenTemplateContextTypeRegistry= new WaContributionContextTypeRegistry(
					"de.walware.statet.redocs.templates.WikitextRweaveCodegen" ); //$NON-NLS-1$
		}
		return this.codegenTemplateContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getCodegenTemplateStore() {
		if (this.codegenTemplatesStore == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.codegenTemplatesStore= new ContributionTemplateStore(
					getCodegenTemplateContextTypeRegistry(), getPreferenceStore(),
					"codegen/CodeTemplates_store" ); //$NON-NLS-1$
			try {
				this.codegenTemplatesStore.load();
			}
			catch (final IOException e) {
				log(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID,
						"An error occured when loading 'Wikitext+R code generation' template store.",
						e ));
			}
		}
		return this.codegenTemplatesStore;
	}
	
	/**
	 * Returns the template context type registry for the new documents templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getDocTemplateContextTypeRegistry() {
		if (this.docTemplateContextTypeRegistry == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.docTemplateContextTypeRegistry= new WaContributionContextTypeRegistry(
					"de.walware.statet.redocs.templates.WikitextRweaveDoc" ); //$NON-NLS-1$
		}
		return this.docTemplateContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the new documents templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getDocTemplateStore() {
		if (this.docTemplatesStore == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.docTemplatesStore= new ContributionTemplateStore(
					getDocTemplateContextTypeRegistry(), getPreferenceStore(),
					"codegen/DocTemplates_store" ); //$NON-NLS-1$
			try {
				this.docTemplatesStore.load();
			}
			catch (final IOException e) {
				log(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID, 0,
						"An error occured when loading 'Wikitext+R document' template store.",
						e )); 
			}
		}
		return this.docTemplatesStore;
	}
	
	
	public synchronized IPreferenceStore getEditorPreferenceStore() {
		if (this.editorPreferenceStore == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.editorPreferenceStore= CombinedPreferenceStore.createStore(
					getPreferenceStore(),
					WikitextEditingSettings.getPreferenceStore(),
					RUIPlugin.getDefault().getPreferenceStore(),
					LTKUIPreferences.getPreferenceStore(),
					EditorsUI.getPreferenceStore() );
		}
		return this.editorPreferenceStore;
	}
	
	/** Only used for chunk */
	public synchronized ContentAssistComputerRegistry getWikidocRweaveEditorContentAssistRegistry() {
		if (this.wikidocRweaveEditorContentAssistRegistry == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.wikidocRweaveEditorContentAssistRegistry= new ContentAssistComputerRegistry(
					WikitextRweaveCore.WIKIDOC_R_CONTENT_ID,
					WIKITEXT_RWEAVE_EDITOR_NODE ); 
			this.disposables.add(this.wikidocRweaveEditorContentAssistRegistry);
		}
		return this.wikidocRweaveEditorContentAssistRegistry;
	}
	
}
