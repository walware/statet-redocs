/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.r.ui.sourceediting.actions;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;

import de.walware.ecommons.ltk.ui.EditorUtil;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.actions.ToggleCommentHandler;

import de.walware.statet.redocs.r.ui.sourceediting.IRweaveEditor;


public abstract class RweaveToggleCommentHandler extends ToggleCommentHandler {
	
	
	public RweaveToggleCommentHandler(final SourceEditor1 editor) {
		super(editor);
		assert (editor instanceof IRweaveEditor);
	}
	
	
	@Override
	protected void run(final AbstractDocument document, final ITextSelection selection,
			final int operationCode) {
		try {
			if (isMixed(document, selection)) {
				final IRegion block= EditorUtil.getTextBlockFromSelection(document,
						selection.getOffset(), selection.getLength() );
				switch (operationCode) {
				case ITextOperationTarget.PREFIX:
					doPrefixPrimary(document, block);
					return;
				case ITextOperationTarget.STRIP_PREFIX:
					doStripPrefix(document, block);
					return;
				default:
					throw new IllegalArgumentException("operationCode= " + operationCode); //$NON-NLS-1$
				}
			}
		}
		catch (final BadLocationException | BadPartitioningException e) {
			log(e);
		}
		
		doRunOperation(operationCode);
	}
	
	protected boolean isMixed(final IDocument document, final ITextSelection selection)
			throws BadLocationException, BadPartitioningException {
		final IRegion block= EditorUtil.getTextBlockFromSelection(document,
				selection.getOffset(), selection.getLength() );
		final IRegion rContent= ((IRweaveEditor) getEditor()).getDocumentContentInfo()
				.getRChunkContentRegion(document, block.getOffset());
		return (rContent == null || block.getOffset() < rContent.getOffset()
				|| block.getOffset() + block.getLength() > rContent.getOffset() + rContent.getLength() );
	}
	
	
	protected abstract void doPrefixPrimary(AbstractDocument document, IRegion block)
			throws BadLocationException, BadPartitioningException;
	
}
