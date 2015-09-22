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

package de.walware.statet.redocs.internal.wikitext.r.commonmark.core;

import de.walware.docmlet.wikitext.commonmark.core.AbstractCommonmarkConfig;

import de.walware.statet.redocs.wikitext.r.commonmark.core.IRCommonmarkConfig;


public class RCommonmarkConfig extends AbstractCommonmarkConfig<RCommonmarkConfig>
		implements IRCommonmarkConfig {
	
	
	public RCommonmarkConfig() {
	}
	
	public RCommonmarkConfig(final RCommonmarkConfig config) {
		load(config);
	}
	
	
	@Override
	public RCommonmarkConfig clone() {
		return new RCommonmarkConfig(this);
	}
	
}
