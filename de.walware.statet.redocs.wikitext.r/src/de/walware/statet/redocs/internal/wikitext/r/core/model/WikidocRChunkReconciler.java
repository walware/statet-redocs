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

package de.walware.statet.redocs.internal.wikitext.r.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.walware.docmlet.wikitext.core.model.IWikidocModelInfo;

import de.walware.statet.redocs.r.core.model.RChunkReconciler;
import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;


class WikidocRChunkReconciler extends RChunkReconciler
		<IWikidocRweaveSourceUnit, IWikidocModelInfo, WikidocRweaveSuModelContainer> {
	
	
	private static final Map<String, WikidocRChunkReconciler> INSTANCES= new HashMap<>();
	
	static final WikidocRChunkReconciler getInstance(final IRweaveMarkupLanguage markupLanguage) {
		synchronized(WikidocRChunkReconciler.class) {
			WikidocRChunkReconciler reconciler= INSTANCES.get(markupLanguage.getName());
			if (reconciler == null) {
				reconciler= new WikidocRChunkReconciler(markupLanguage.getName(),
						markupLanguage.getRChunkStartLinePattern(),
						markupLanguage.getRChunkRefLinePattern(),
						markupLanguage.getRChunkEndLinePattern() );
				INSTANCES.put(markupLanguage.getName(), reconciler);
			}
			return reconciler;
		}
	}
	
	
	public WikidocRChunkReconciler(final String name, final Pattern chunkStartLinePattern,
			final Pattern chunkRefLinePattern, final Pattern chunkEndLinePattern) {
		super(name, chunkStartLinePattern, chunkRefLinePattern, chunkEndLinePattern);
	}
	
	
}
