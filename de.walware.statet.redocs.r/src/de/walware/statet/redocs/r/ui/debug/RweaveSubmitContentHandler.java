/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.r.ui.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.launching.ICodeSubmitContentHandler;

import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;


public class RweaveSubmitContentHandler implements ICodeSubmitContentHandler {
	
	
	private final IDocumentSetupParticipant docSetup;
	
	private final IDocContentSectionsRweaveExtension docContentSections;
	
	
	public RweaveSubmitContentHandler(final IDocumentSetupParticipant docSetup,
			final IDocContentSectionsRweaveExtension docContentSections) {
		this.docSetup= docSetup;
		this.docContentSections= docContentSections;
	}
	
	
	@Override
	public void setup(final IDocument document) {
		this.docSetup.setup(document);
	}
	
	@Override
	public List<String> getCodeLines(final IDocument document) throws BadLocationException, CoreException {
		final ArrayList<String> lines= new ArrayList<>(document.getNumberOfLines() / 2);
		
		final List<? extends IRegion> rCodeRegions= this.docContentSections.getRChunkCodeRegions(
				document, 0, document.getLength() );
		for (final IRegion region : rCodeRegions) {
			TextUtil.addLines(document, region.getOffset(), region.getLength(), lines);
		}
		
		return lines;
	}
	
	@Override
	public List<String> getCodeLines(final IDocument document, final int offset, final int length)
			throws CoreException, BadLocationException {
		final ArrayList<String> lines= new ArrayList<>(Math.min(
				document.getNumberOfLines(0, length) + 1, 64) );
		
		final List<? extends IRegion> rCodeRegions= this.docContentSections.getRChunkCodeRegions(
				document, offset, length );
		for (final IRegion region : rCodeRegions) {
			TextUtil.addLines(document, region.getOffset(), region.getLength(), lines);
		}
		
		return lines;
	}
	
}
