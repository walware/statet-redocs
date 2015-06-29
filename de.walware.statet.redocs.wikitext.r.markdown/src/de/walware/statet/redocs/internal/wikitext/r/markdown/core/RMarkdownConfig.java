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

package de.walware.statet.redocs.internal.wikitext.r.markdown.core;

import de.walware.docmlet.wikitext.core.source.extdoc.AbstractMarkupConfig;

import de.walware.statet.redocs.wikitext.r.markdown.core.IRMarkdownConfig;


public class RMarkdownConfig extends AbstractMarkupConfig<RMarkdownConfig> implements IRMarkdownConfig {
	
	
	public RMarkdownConfig() {
	}
	
	public RMarkdownConfig(final RMarkdownConfig config) {
		load(config);
	}
	
	
	@Override
	protected String getConfigType() {
		return "Markdown"; //$NON-NLS-1$
	}
	
	@Override
	public RMarkdownConfig clone() {
		return new RMarkdownConfig(this);
	}
	
}
