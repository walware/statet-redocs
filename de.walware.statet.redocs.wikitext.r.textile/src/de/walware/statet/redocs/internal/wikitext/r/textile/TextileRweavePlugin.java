/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.textile;

import static de.walware.statet.redocs.internal.wikitext.r.textile.core.RTextileLanguage.TEXTILE_LANGUAGE_NAME;
import static de.walware.statet.redocs.internal.wikitext.r.textile.core.RTextileLanguage.TEXTILE_RWEAVE_LANGUAGE_NAME;

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

import de.walware.statet.redocs.internal.wikitext.r.textile.core.RTextileLanguage;
import de.walware.statet.redocs.wikitext.r.core.source.WikitextRweaveTemplatesContextType;


public class TextileRweavePlugin extends AbstractUIPlugin {
	
	
	public static final String PLUGIN_ID= "de.walware.statet.redocs.wikitext.r.textile"; //$NON-NLS-1$
	
	
	public static final String DOC_CONTENT_ID= "de.walware.statet.redocs.contentTypes.TextileRweave"; //$NON-NLS-1$
	
	public static final IContentType DOC_CONTENT_TYPE;
	
	static {
		final IContentTypeManager contentTypeManager= Platform.getContentTypeManager();
		DOC_CONTENT_TYPE= contentTypeManager.getContentType(DOC_CONTENT_ID);
	}
	
	
	public static final String TEMPLATES_WEAVE_DOCDEFAULT_CONTEXTTYPE= TEXTILE_RWEAVE_LANGUAGE_NAME +
			WikitextRweaveTemplatesContextType.WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX;
	
	public static final String TEMPLATES_QUALIFIER= PLUGIN_ID + "/codegen"; //$NON-NLS-1$
	
	public static final String NEWDOC_TEMPLATE_CATEGORY_ID= "TextileRweave.NewDoc"; //$NON-NLS-1$
	
	
	private static TextileRweavePlugin instance;
	
	
	public static TextileRweavePlugin getInstance() {
		return TextileRweavePlugin.instance;
	}
	
	public static void log(final IStatus status) {
		final Plugin plugin= getInstance();
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}
	
	
	private boolean started;
	
	private RTextileLanguage markupLanguage;
	
	
	public TextileRweavePlugin() {
	}
	
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance= this;
		
		final MarkupLanguage baseLanguage= WikiText.getMarkupLanguage(TEXTILE_LANGUAGE_NAME);
		if (baseLanguage == null) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					NLS.bind("Failed to load markup language ''{0}''.", TEXTILE_LANGUAGE_NAME) ));
		}
		this.markupLanguage= (RTextileLanguage) WikitextCore.getMarkupLanguageManager()
				.getLanguage(TEXTILE_RWEAVE_LANGUAGE_NAME);
		
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
	
	
	public RTextileLanguage getMarkupLanguage() {
		return this.markupLanguage;
	}
	
}
