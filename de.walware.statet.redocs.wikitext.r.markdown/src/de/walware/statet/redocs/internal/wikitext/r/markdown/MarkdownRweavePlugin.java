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

package de.walware.statet.redocs.internal.wikitext.r.markdown;

import static de.walware.statet.redocs.internal.wikitext.r.markdown.core.RMarkdownLanguage.BASE_MARKUP_LANGUAGE_NAME;
import static de.walware.statet.redocs.internal.wikitext.r.markdown.core.RMarkdownLanguage.WEAVE_MARKUP_LANGUAGE_NAME;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.ui.WikiText;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.docmlet.wikitext.core.WikitextCore;

import de.walware.statet.redocs.internal.wikitext.r.markdown.core.RMarkdownLanguage;
import de.walware.statet.redocs.wikitext.r.core.source.WikitextRweaveTemplatesContextType;


public class MarkdownRweavePlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.redocs.wikitext.r.markdown"; //$NON-NLS-1$
	
	
	public static final String DOC_CONTENT_ID= "de.walware.statet.redocs.contentTypes.MarkdownRweave"; //$NON-NLS-1$
	
	public static final IContentType DOC_CONTENT_TYPE;
	
	static {
		final IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
		DOC_CONTENT_TYPE= contentTypeManager.getContentType(DOC_CONTENT_ID);
	}
	
	
	public static final String TEMPLATES_WEAVE_DOCDEFAULT_CONTEXTTYPE= WEAVE_MARKUP_LANGUAGE_NAME +
			WikitextRweaveTemplatesContextType.WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX;
	
	public static final String TEMPLATES_QUALIFIER= PLUGIN_ID + "/codegen"; //$NON-NLS-1$
	
	public static final String NEWDOC_TEMPLATE_CATEGORY_ID= "MarkdownRweave.NewDoc"; //$NON-NLS-1$
	
	
	private static MarkdownRweavePlugin instance;
	
	
	public static MarkdownRweavePlugin getInstance() {
		return MarkdownRweavePlugin.instance;
	}
	
	public static void log(final IStatus status) {
		final Plugin plugin= getInstance();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean started;
	
	private RMarkdownLanguage markupLanguage;
	
	
	public MarkdownRweavePlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance= this;
		
		final MarkupLanguage baseLanguage= WikiText.getMarkupLanguage(BASE_MARKUP_LANGUAGE_NAME);
		if (baseLanguage == null) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					NLS.bind("Failed to load markup language ''{0}''.", BASE_MARKUP_LANGUAGE_NAME) ));
		}
		this.markupLanguage= (RMarkdownLanguage) WikitextCore.getMarkupLanguageManager()
				.getLanguage(WEAVE_MARKUP_LANGUAGE_NAME);
		
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
	
	
	public RMarkdownLanguage getMarkupLanguage() {
		return this.markupLanguage;
	}
	
}
