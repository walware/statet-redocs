/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan WaOhlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.debug.ui.actions.RToggleBreakpointAdapter;

import de.walware.statet.redocs.tex.r.ui.editors.ILtxRweaveEditor;


public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	
	private final RToggleBreakpointAdapter rAdapter;
	
	
	public ToggleBreakpointAdapter() {
		this.rAdapter= new RToggleBreakpointAdapter();
	}
	
	
	@Override
	public boolean canToggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection)
			throws CoreException {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		if (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection) {
			if (isRChunk(editor, (ITextSelection) selection) ) {
				this.rAdapter.toggleLineBreakpoints(part, selection);
			}
			else {
				this.rAdapter.removeBreakpoints(part, selection, new NullProgressMonitor());
			}
		}
	}
	
	@Override
	public boolean canToggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		if (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection) {
			if (isRChunk(editor, (ITextSelection) selection) ) {
				this.rAdapter.toggleMethodBreakpoints(part, selection);
			}
			else {
				this.rAdapter.removeBreakpoints(part, selection, new NullProgressMonitor());
			}
		}
	}
	
	@Override
	public boolean canToggleWatchpoints(final IWorkbenchPart part, final ISelection selection) {
		return false;
	}
	
	@Override
	public void toggleWatchpoints(final IWorkbenchPart part, final ISelection selection)
			throws CoreException {
	}
	
	@Override
	public boolean canToggleBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleBreakpoints(final IWorkbenchPart part, final ISelection selection)
			throws CoreException {
		final ILtxRweaveEditor editor= getREditor(part, selection);
		if (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection) {
			if (isRChunk(editor, (ITextSelection) selection) ) {
				this.rAdapter.toggleBreakpoints(part, selection);
			}
			else {
				this.rAdapter.removeBreakpoints(part, selection, new NullProgressMonitor());
			}
		}
	}
	
	private ILtxRweaveEditor getREditor(final IWorkbenchPart part, final ISelection selection) {
		if (part instanceof ILtxRweaveEditor) {
			return (ILtxRweaveEditor) part;
		}
		final Object adapter= part.getAdapter(ISourceEditor.class);
		if (adapter instanceof ILtxRweaveEditor) {
			return (ILtxRweaveEditor) adapter;
		}
		return null;
	}
	
	private boolean isRChunk(final ILtxRweaveEditor editor, final ITextSelection selection) {
		final SourceViewer viewer= editor.getViewer();
		if (viewer != null) {
			try {
				final ITypedRegion partition= ((IDocumentExtension3) viewer.getDocument()).getPartition(
						editor.getDocumentContentInfo().getPartitioning(),
						selection.getOffset(), false );
				return IRDocumentConstants.R_ANY_CONTENT_CONSTRAINT.matches(partition.getType());
			}
			catch (final BadLocationException e) {}
			catch (final BadPartitioningException e) {}
		}
		return false;
	}
	
}
