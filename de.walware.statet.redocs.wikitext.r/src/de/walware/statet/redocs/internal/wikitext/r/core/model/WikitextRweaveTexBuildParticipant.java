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

package de.walware.statet.redocs.internal.wikitext.r.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.docmlet.wikitext.core.WikitextBuildParticipant;
import de.walware.docmlet.wikitext.core.model.IWikidocWorkspaceSourceUnit;
import de.walware.docmlet.wikitext.core.model.WikidocSuModelContainer;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RModelIndexUpdate;

import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;
import de.walware.statet.redocs.wikitext.r.core.model.WikitextRweaveModel;


public class WikitextRweaveTexBuildParticipant extends WikitextBuildParticipant {
	
	
	private static final ImList<String> WIKITEXT_R_MODEL_TYPES= ImCollections.newList(
			WikitextRweaveModel.WIKIDOC_R_MODEL_TYPE_ID );
	
	
	private RModelIndexUpdate rIndexUpdate;
	
	
	public WikitextRweaveTexBuildParticipant() {
	}
	
	
	@Override
	public void init() {
		super.init();
		
		final IRProject rProject= RProjects.getRProject(getWikitextProject().getProject());
		if (rProject != null) {
			setEnabled(true);
			this.rIndexUpdate= new RModelIndexUpdate(rProject, WIKITEXT_R_MODEL_TYPES,
					(getBuildType() == IncrementalProjectBuilder.FULL_BUILD) );
		}
	}
	
	
	@Override
	public void clear(final IFile file) throws CoreException {
		file.deleteMarkers("de.walware.statet.r.markers.Tasks", false, IResource.DEPTH_INFINITE); //$NON-NLS-1$
	}
	
	@Override
	public void docUnitUpdated(final IWikidocWorkspaceSourceUnit sourceUnit,
			final IProgressMonitor monitor) throws CoreException {
		if (sourceUnit instanceof IWikidocRweaveSourceUnit) {
			final IWikidocRweaveSourceUnit unit= (IWikidocRweaveSourceUnit) sourceUnit;
			final WikidocRweaveSuModelContainer modelContainer= (WikidocRweaveSuModelContainer) unit.getAdapter(WikidocSuModelContainer.class);
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
	public void docUnitRemoved(final IFile file,
			final IProgressMonitor monitor) throws CoreException {
		this.rIndexUpdate.remove(file);
	}
	
	@Override
	public void docFinished(final IProgressMonitor monitor) throws CoreException {
		this.rIndexUpdate.submit(monitor);
	}
	
}
