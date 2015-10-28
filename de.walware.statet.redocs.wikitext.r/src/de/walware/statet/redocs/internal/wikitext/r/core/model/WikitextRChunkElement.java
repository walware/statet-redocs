/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.core.model;

import org.eclipse.jface.text.IRegion;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.core.model.ISourceStructElement;

import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.SourceComponent;

import de.walware.statet.redocs.r.core.model.RChunkNode;


public class WikitextRChunkElement extends RChunkElement {
	
	
	public WikitextRChunkElement(final ISourceStructElement parent, final RChunkNode astNode,
			final RElementName name, final IRegion nameRegion) {
		super(parent, astNode, name, nameRegion);
	}
	
	
	@Override
	protected RChunkNode getNode() {
		return (RChunkNode) super.getNode();
	}
	
	@Override
	protected ImList<SourceComponent> getSourceComponents() {
		return getNode().getRCodeChildren();
	}
	
}
