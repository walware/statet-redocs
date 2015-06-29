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

package de.walware.statet.redocs.internal.tex.r;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.ui.LTKUIPreferences;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.templates.WaContributionContextTypeRegistry;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.docmlet.tex.ui.sourceediting.TexEditingSettings;

import de.walware.statet.r.internal.ui.RUIPlugin;

import de.walware.statet.redocs.internal.tex.r.core.LtxRweaveTemplatesContextType;
import de.walware.statet.redocs.internal.tex.r.ui.LtxRweaveTemplates;
import de.walware.statet.redocs.internal.tex.r.ui.editors.LtxRweaveDocumentProvider;
import de.walware.statet.redocs.tex.r.core.TexRweaveCore;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


/**
 * The activator class controls the plug-in life cycle
 */
public class RedocsTexRPlugin extends AbstractUIPlugin {
	
	
/*- Images --------------------------------------------------------------------*/
	
	private static final String NS= "de.walware.statet.redocs.tex.r"; //$NON-NLS-1$
	
	public static final String OBJ_LTXRWEAVE_IMAGE_ID= NS + "/image/obj/LtxRweave"; //$NON-NLS-1$
	
	public static final String TOOL_NEW_LTXRWEAVE_IMAGE_ID= NS + "/image/tool/New-LtxRweave"; //$NON-NLS-1$
	
	public static final String WIZBAN_NEW_LTXRWEAVE_FILE_IMAGE_ID= NS + "/image/wizban/New-LtxRweaveFile"; //$NON-NLS-1$
	
/*- Prefs ---------------------------------------------------------------------*/
	
	public static final String TEX_RWEAVE_EDITOR_NODE= TexRweaveUI.PLUGIN_ID + "/rweavetex.editor/options"; //$NON-NLS-1$
	
	
	// The shared instance
	private static RedocsTexRPlugin instance;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static RedocsTexRPlugin getInstance() {
		return instance;
	}
	
	public static void log(final IStatus status) {
		final Plugin plugin= getInstance();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	@Deprecated
	public static void logError(final int code, final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, code, message, e));
	}
	
	
	private boolean started;
	
	private List<IDisposable> disposables;
	
	private LtxRweaveDocumentProvider docRDocumentProvider;
	
	private IPreferenceStore editorPreferenceStore;
	
	private ContextTypeRegistry codegenTemplateContextTypeRegistry;
	private TemplateStore codegenTemplatesStore;
	
	private ContextTypeRegistry docTemplateContextTypeRegistry;
	private TemplateStore docTemplatesStore;
	
	private ContentAssistComputerRegistry ltxRweaveEditorContentAssistRegistry;
	
	
	/**
	 * The constructor
	 */
	public RedocsTexRPlugin() {
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
				this.ltxRweaveEditorContentAssistRegistry= null;
			}
			
			for (final IDisposable listener : this.disposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
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
		
		util.register(OBJ_LTXRWEAVE_IMAGE_ID, ImageRegistryUtil.T_OBJ, "ltx_rweave-file.png"); //$NON-NLS-1$
		
		util.register(TOOL_NEW_LTXRWEAVE_IMAGE_ID, ImageRegistryUtil.T_TOOL, "new-ltx_rweave-file.png"); //$NON-NLS-1$
		
		util.register(WIZBAN_NEW_LTXRWEAVE_FILE_IMAGE_ID, ImageRegistryUtil.T_WIZBAN, "new-ltx_rweave-file.png"); //$NON-NLS-1$
	}
	
	
	public synchronized LtxRweaveDocumentProvider getDocRDocumentProvider() {
		if (this.docRDocumentProvider == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.docRDocumentProvider= new LtxRweaveDocumentProvider();
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
					"de.walware.statet.redocs.templates.TexRweaveCodegen" ); //$NON-NLS-1$
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
				log(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, 0,
						"An error occured when loading 'TeX+R code generation' template store.",
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
					"de.walware.statet.redocs.templates.TexRweaveDoc" ); //$NON-NLS-1$
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
			
			final ScopedPreferenceStore prefStore= new ScopedPreferenceStore(
					InstanceScope.INSTANCE, LtxRweaveTemplates.PREF_QUALIFIER );
			this.docTemplatesStore= new ContributionTemplateStore(
					getDocTemplateContextTypeRegistry(), prefStore,
					LtxRweaveTemplates.DOC_TEMPLATES_STORE_KEY ); //$NON-NLS-1$
			try {
				this.docTemplatesStore.load();
			}
			catch (final IOException e) {
				log(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, 0,
						"An error occured when loading 'Tex+R document' template store.",
						e )); 
			}
			
			if (!prefStore.contains(LtxRweaveTemplates.DOC_TEMPLATES_STORE_KEY)) {
				migrateDocTemplateStore();
			}
		}
		return this.docTemplatesStore;
	}
	
	private void migrateDocTemplateStore() {
		final ScopedPreferenceStore oldPrefStore= new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "de.walware.statet.r.sweave" ); //$NON-NLS-1$
		if (!oldPrefStore.isDefault("de.walware.statet.r.sweave.templates.sweavedoc")) { //$NON-NLS-1$
			try {
				final TemplateStore oldTemplateStore= new TemplateStore(oldPrefStore,
						"de.walware.statet.r.sweave.templates.sweavedoc" ); //$NON-NLS-1$
				oldTemplateStore.load();
				final TemplatePersistenceData[] templateDatas= oldTemplateStore.getTemplateData(false);
				for (final TemplatePersistenceData templateData : templateDatas) {
					if (templateData.isEnabled()) {
						final Template template= templateData.getTemplate();
						if (template.getContextTypeId().equals("ltx-rweave_NewSweaveDoc")) { //$NON-NLS-1$
							if (template.getName().startsWith("ltx-rweave.NewDoc:") ) { //$NON-NLS-1$
								final String templateName= template.getName().substring(18);
								final Template newTemplate= new Template(
										LtxRweaveTemplates.NEWDOC_TEMPLATE_CATEGORY_ID + ':' + templateName,
										template.getDescription(),
										LtxRweaveTemplatesContextType.NEWDOC_CONTEXTTYPE,
										template.getPattern(), false );
								final TemplatePersistenceData defaultData= this.docTemplatesStore.getTemplateData(
										"de.walware.statet.redocs.templates.LtxRweave_" + templateName + "Doc" ); //$NON-NLS-1$ //$NON-NLS-2$
								if (defaultData != null) {
									defaultData.setTemplate(newTemplate);
								}
								else {
									this.docTemplatesStore.add(
											new TemplatePersistenceData(newTemplate, true) );
								}
							}
						}
					}
				}
				this.docTemplatesStore.save();
			}
			catch (final Exception e) {
				log(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, 0,
						"An error occurred while migrating old 'Tex+R document' template store.",
						e ));
			}
		}
	}
	
	
	public synchronized IPreferenceStore getEditorPreferenceStore() {
		if (this.editorPreferenceStore == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.editorPreferenceStore= CombinedPreferenceStore.createStore(
					getPreferenceStore(),
					TexEditingSettings.getPreferenceStore(),
					RUIPlugin.getDefault().getPreferenceStore(),
					LTKUIPreferences.getPreferenceStore(),
					EditorsUI.getPreferenceStore() );
		}
		return this.editorPreferenceStore;
	}
	
	/** Only used for chunk */
	public synchronized ContentAssistComputerRegistry getLtxRweaveEditorContentAssistRegistry() {
		if (this.ltxRweaveEditorContentAssistRegistry == null) {
			if (!this.started) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			this.ltxRweaveEditorContentAssistRegistry= new ContentAssistComputerRegistry(
					TexRweaveCore.LTX_R_CONTENT_ID,
					TEX_RWEAVE_EDITOR_NODE );
			this.disposables.add(this.ltxRweaveEditorContentAssistRegistry);
		}
		return this.ltxRweaveEditorContentAssistRegistry;
	}
	
}
