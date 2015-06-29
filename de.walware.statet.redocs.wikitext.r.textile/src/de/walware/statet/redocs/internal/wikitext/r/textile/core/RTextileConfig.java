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

package de.walware.statet.redocs.internal.wikitext.r.textile.core;

import de.walware.docmlet.wikitext.core.source.extdoc.AbstractMarkupConfig;

import de.walware.statet.redocs.wikitext.r.textile.core.IRTextileConfig;


public class RTextileConfig extends AbstractMarkupConfig<RTextileConfig> implements IRTextileConfig {
	
	
	public RTextileConfig() {
	}
	
	public RTextileConfig(final RTextileConfig config) {
		load(config);
	}
	
	
	@Override
	public String getConfigType() {
		return "Textile"; //$NON-NLS-1$
	}
	
	@Override
	public RTextileConfig clone() {
		return new RTextileConfig(this);
	}
	
}
