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

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionHandler;

import de.walware.statet.r.ui.editors.RElementSearchHandler;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;


public class ElementSearchHandler extends MultiContentSectionHandler implements IExecutableExtension {
	
	
	private String commandId;
	
	
	public ElementSearchHandler() {
		super(LtxRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
		if (this.commandId == null) {
			final String s= config.getAttribute("commandId"); //$NON-NLS-1$
			if (s != null && !s.isEmpty()) {
				this.commandId= s.intern();
			}
		}
	}
	
	@Override
	protected IHandler2 createHandler(final String sectionType) {
		switch (sectionType) {
		case LtxRweaveDocumentContentInfo.R:
			return new RElementSearchHandler(this.commandId);
		default:
			return null;
		}
	}
	
}
