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

package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.docmlet.wikitext.ui.config.IMarkupConfigUIAdapter;


public class ConfigUIAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class[] {
		IMarkupConfigUIAdapter.class,
	};
	
	
	private IMarkupConfigUIAdapter markupConfigUI;
	
	
	public ConfigUIAdapterFactory() {
	}
	
	@Override
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == IMarkupConfigUIAdapter.class) {
			synchronized (this) {
				if (this.markupConfigUI == null) {
					this.markupConfigUI= new RCommonmarkConfigUI();
				}
				return this.markupConfigUI;
			}
		}
		return null;
	}
	
}
