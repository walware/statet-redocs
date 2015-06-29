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

package de.walware.statet.redocs.r.core.source;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;


public interface IDocContentSectionsRweaveExtension extends IDocContentSections {
	
	
	ITreePartitionNode getRChunkRegion(IDocument document, int offset)
			throws BadLocationException;
	
	List<ITreePartitionNode> getRChunkRegions(IDocument document, int offset, int length)
			throws BadLocationException;
	
	IRegion getRChunkContentRegion(IDocument document, int offset)
			throws BadLocationException;
	
	List<ITreePartitionNode> getRChunkCodeRegions(IDocument document, int offset, int length)
			throws BadLocationException;
	
	ITreePartitionNode getRCodeRegion(IDocument document, int offset)
			throws BadLocationException;
	
}
