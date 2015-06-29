/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.r.ui.debug;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;

import de.walware.statet.redocs.internal.r.Messages;
import de.walware.statet.redocs.r.RedocsRweave;
import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;


/**
 * Launch shortcut, which submits the chunks (touched by selection).
 * 
 * Supports only text editors.
 */
public class SubmitRChunkDirectLaunchShortcut implements ILaunchShortcut {
	
	
	public static class AndGotoConsole extends SubmitRChunkDirectLaunchShortcut {
		
		
		public AndGotoConsole() {
			super(true);
		}
		
	}
	
	
	private static class Data {
		
		private AbstractDocument document;
		
		private IDocContentSectionsRweaveExtension docContentSections;
		
		private ITextSelection selection;
		
		
		public boolean isComplete() {
			return (this.document != null && this.docContentSections != null && this.selection != null);
		}
		
	}
	
	
	private final boolean gotoConsole;
	
	
	public SubmitRChunkDirectLaunchShortcut() {
		this(false);
	}
	
	protected SubmitRChunkDirectLaunchShortcut(final boolean gotoConsole) {
		this.gotoConsole= gotoConsole;
	}
	
	
	@Override
	public void launch(final ISelection selection, final String mode) {
		// not supported
	}
	
	@Override
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		final Data data= new Data();
		try {
			final ISourceEditor sourceEditor= (ISourceEditor) editor.getAdapter(ISourceEditor.class);
			if (sourceEditor != null) {
				UIAccess.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						final SourceViewer viewer= sourceEditor.getViewer();
						if (UIAccess.isOkToUse(viewer)) {
							data.document= (AbstractDocument) viewer.getDocument();
							final IDocContentSections docContentInfo= sourceEditor.getDocumentContentInfo();
							if (docContentInfo instanceof IDocContentSectionsRweaveExtension) {
								data.docContentSections= (IDocContentSectionsRweaveExtension) docContentInfo;
							}
							data.selection= (ITextSelection) viewer.getSelection();
						}
					}
				});
			}
			
			if (data.isComplete()) {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException {
						try {
							doLaunch(data, monitor);
						}
						catch (final CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			}
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING, Messages.RChunkLaunch_error_message, e.getTargetException()));
		}
		catch (final InterruptedException e) {
		}
	}
	
	private void doLaunch(final Data data, final IProgressMonitor monitor) throws CoreException {
		try {
			final List<? extends IRegion> rCodeRegions= data.docContentSections.getRChunkCodeRegions(
					data.document, data.selection.getOffset(), data.selection.getLength() );
			final ArrayList<String> lines= new ArrayList<>();
			for (final IRegion region : rCodeRegions) {
				TextUtil.addLines(data.document, region.getOffset(), region.getLength(), lines);
			}
			
			if (monitor.isCanceled()) {
				return;
			}
			
			if (lines.size() == 0 || monitor.isCanceled()) {
				return;
			}
			
			RCodeLaunching.runRCodeDirect(lines, this.gotoConsole, monitor);
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RedocsRweave.PLUGIN_ID, -1,
					"An error occurred when picking code lines.", e ));
		}
	}
	
}
