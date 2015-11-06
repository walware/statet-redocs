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

package de.walware.statet.redocs.internal.wikitext.r.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy2;

import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;
import de.walware.docmlet.wikitext.core.model.IWikidocWorkspaceSourceUnit;
import de.walware.docmlet.wikitext.core.model.IWikitextSourceUnit;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;

import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;


public class WikidocRweaveEditorWorkingCopy
		extends GenericEditorWorkspaceSourceUnitWorkingCopy2<WikidocRweaveSuModelContainer>
		implements IWikidocRweaveSourceUnit, IWikidocWorkspaceSourceUnit, IRWorkspaceSourceUnit {
	
	
	public WikidocRweaveEditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	@Override
	protected WikidocRweaveSuModelContainer createModelContainer() {
		return new WikidocRweaveSuModelContainer(this);
	}
	
	
	@Override
	public IWikitextCoreAccess getWikitextCoreAccess() {
		return ((IWikitextSourceUnit) getUnderlyingUnit()).getWikitextCoreAccess();
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return ((IWikidocRweaveSourceUnit) getUnderlyingUnit()).getRCoreAccess();
	}
	
	
	@Override
	protected void register() {
		super.register();
		final IModelManager rManager= RModel.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
	}
	
	@Override
	protected void unregister() {
		final IModelManager rManager= RModel.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
		super.unregister();
	}
	
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int flags, final IProgressMonitor monitor) {
		if (type == RModel.R_TYPE_ID) {
			return RModel.getRModelInfo(getModelContainer().getModelInfo(flags, monitor));
		}
		return super.getModelInfo(type, flags, monitor);
	}
	
}
