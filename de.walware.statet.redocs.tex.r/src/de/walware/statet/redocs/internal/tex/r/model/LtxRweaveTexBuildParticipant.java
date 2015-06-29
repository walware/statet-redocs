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

package de.walware.statet.redocs.internal.tex.r.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;

import de.walware.docmlet.tex.core.TexBuildParticipant;
import de.walware.docmlet.tex.core.model.ITexWorkspaceSourceUnit;
import de.walware.docmlet.tex.core.model.LtxSuModelContainer;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RModelIndexUpdate;

import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;
import de.walware.statet.redocs.tex.r.core.model.TexRweaveModel;


public class LtxRweaveTexBuildParticipant extends TexBuildParticipant {
	
	
	private static final ConstList<String> TEX_R_MODEL_TYPES= new ConstArrayList<>(
			TexRweaveModel.LTX_R_MODEL_TYPE_ID );
	
	
	private RModelIndexUpdate rIndexUpdate;
	
	
	public LtxRweaveTexBuildParticipant() {
	}
	
	
	@Override
	public void init() {
		super.init();
		
		final IRProject rProject= RProjects.getRProject(getTexProject().getProject());
		if (rProject != null) {
			setEnabled(true);
			this.rIndexUpdate= new RModelIndexUpdate(rProject, TEX_R_MODEL_TYPES,
					(getBuildType() == IncrementalProjectBuilder.FULL_BUILD) );
		}
	}
	
	
	@Override
	public void clear(final IFile file) throws CoreException {
		file.deleteMarkers("de.walware.statet.r.markers.Tasks", false, IResource.DEPTH_INFINITE); //$NON-NLS-1$
	}
	
	@Override
	public void ltxUnitUpdated(final ITexWorkspaceSourceUnit sourceUnit,
			final IProgressMonitor monitor) throws CoreException {
		if (sourceUnit instanceof ILtxRweaveSourceUnit) {
			final ILtxRweaveSourceUnit unit= (ILtxRweaveSourceUnit) sourceUnit;
			final LtxRweaveSuModelContainer modelContainer= (LtxRweaveSuModelContainer) unit.getAdapter(LtxSuModelContainer.class);
			if (modelContainer != null) {
				this.rIndexUpdate.update(unit,
						RModel.getRModelInfo(modelContainer.getCurrentModel()) );
			}
			else {
				this.rIndexUpdate.remove(unit);
			}
		}
	}
	
	@Override
	public void ltxUnitRemoved(final IFile file,
			final IProgressMonitor monitor) throws CoreException {
		this.rIndexUpdate.remove(file);
	}
	
	@Override
	public void ltxFinished(final IProgressMonitor monitor) throws CoreException {
		this.rIndexUpdate.submit(monitor);
	}
	
}
