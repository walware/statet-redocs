/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.commonmark;

import static de.walware.statet.redocs.internal.wikitext.r.commonmark.core.RCommonmarkLanguage.COMMONMARK_RWEAVE_LANGUAGE_NAME;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.docmlet.wikitext.core.WikitextCore;
import de.walware.docmlet.wikitext.core.markup.IMarkupLanguageManager1;

import de.walware.statet.redocs.internal.wikitext.r.commonmark.core.RCommonmarkLanguage;
import de.walware.statet.redocs.wikitext.r.core.source.WikitextRweaveTemplatesContextType;


public class CommonmarkRweavePlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.redocs.wikitext.r.commonmark"; //$NON-NLS-1$
	
	
	public static final String DOC_CONTENT_ID= "de.walware.statet.redocs.contentTypes.CommonmarkRweave"; //$NON-NLS-1$
	
	public static final IContentType DOC_CONTENT_TYPE;
	
	static {
		final IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
		DOC_CONTENT_TYPE= contentTypeManager.getContentType(DOC_CONTENT_ID);
	}
	
	
	public static final String TEMPLATES_WEAVE_DOCDEFAULT_CONTEXTTYPE= COMMONMARK_RWEAVE_LANGUAGE_NAME +
			WikitextRweaveTemplatesContextType.WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX;
	
	public static final String TEMPLATES_QUALIFIER= PLUGIN_ID + "/codegen"; //$NON-NLS-1$
	
	public static final String NEWDOC_TEMPLATE_CATEGORY_ID= "CommonmarkRweave.NewDoc"; //$NON-NLS-1$
	
	
	private static CommonmarkRweavePlugin instance;
	
	
	public static CommonmarkRweavePlugin getInstance() {
		return CommonmarkRweavePlugin.instance;
	}
	
	public static void log(final IStatus status) {
		final Plugin plugin= getInstance();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean started;
	
	private RCommonmarkLanguage markupLanguage;
	
	
	public CommonmarkRweavePlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance= this;
		
		final IMarkupLanguageManager1 markupLanguageManager= WikitextCore.getMarkupLanguageManager();
		
		this.markupLanguage= (RCommonmarkLanguage) markupLanguageManager
				.getLanguage(COMMONMARK_RWEAVE_LANGUAGE_NAME);
		
		this.started= true;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				this.started= false;
			}
		}
		finally {
			instance= null;
			super.stop(context);
		}
	}
	
	
	public RCommonmarkLanguage getMarkupLanguage() {
		return this.markupLanguage;
	}
	
}
