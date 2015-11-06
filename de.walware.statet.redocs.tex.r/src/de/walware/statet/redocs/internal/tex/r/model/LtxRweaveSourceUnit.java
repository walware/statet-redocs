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

package de.walware.statet.redocs.internal.tex.r.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.impl.GenericResourceSourceUnit2;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.text.core.sections.IDocContentSections;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.model.ITexWorkspaceSourceUnit;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;

import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;
import de.walware.statet.redocs.tex.r.core.model.TexRweaveModel;
import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;


public class LtxRweaveSourceUnit extends GenericResourceSourceUnit2<LtxRweaveSuModelContainer> 
		implements ILtxRweaveSourceUnit, ITexWorkspaceSourceUnit, IRWorkspaceSourceUnit {
	
	
	public LtxRweaveSourceUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	@Override
	protected LtxRweaveSuModelContainer createModelContainer() {
		return new LtxRweaveSuModelContainer(this);
	}
	
	
	@Override
	public String getModelTypeId() {
		return TexRweaveModel.LTX_R_MODEL_TYPE_ID;
	}
	
	@Override
	public IDocContentSections getDocumentContentInfo() {
		return LtxRweaveDocumentContentInfo.INSTANCE;
	}
	
	
	@Override
	public ITexCoreAccess getTexCoreAccess() {
		return TexCore.WORKBENCH_ACCESS;
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		final IRProject rProject= RProjects.getRProject(getResource().getProject());
		return (rProject != null) ? rProject : RCore.WORKBENCH_ACCESS;
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
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
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(IRCoreAccess.class)) {
			return getTexCoreAccess();
		}
		return super.getAdapter(required);
	}
	
}
