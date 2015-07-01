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

package de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting;

import de.walware.ecommons.ltk.core.model.IEmbeddedForeignElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;

import de.walware.docmlet.wikitext.core.model.IWikitextSourceElement;
import de.walware.docmlet.wikitext.core.model.WikitextModel;

import de.walware.statet.redocs.internal.wikitext.r.core.model.WikitextRChunkElement;


public class DocROutlineContentProvider extends OutlineContentProvider {
	
	
	public DocROutlineContentProvider(final IOutlineContent content) {
		super(content);
	}
	
	
	@Override
	public Object getParent(final Object element) {
		final Object parent= super.getParent(element);
		if (parent instanceof WikitextRChunkElement) {
			return ((ISourceStructElement) element).getSourceParent();
		}
		return parent;
	}
	
	@Override
	public boolean hasChildren(final Object element) {
		final ISourceStructElement e= (ISourceStructElement) element;
		if (e.getModelTypeId() == WikitextModel.WIKIDOC_TYPE_ID
				&& e.getElementType() == IWikitextSourceElement.C1_EMBEDDED) {
			final ISourceStructElement foreignElement= ((IEmbeddedForeignElement) e).getForeignElement();
			return (foreignElement != null
					&& foreignElement.hasSourceChildren(getContent().getContentFilter()) );
		}
		return super.hasChildren(element);
	}
	
	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof ISourceStructElement) {
			final ISourceStructElement e= (ISourceStructElement) parentElement;
			if (e.getModelTypeId() == WikitextModel.WIKIDOC_TYPE_ID
					&& e.getElementType() == IWikitextSourceElement.C1_EMBEDDED) {
				final ISourceStructElement foreignElement= ((IEmbeddedForeignElement) e).getForeignElement();
				return foreignElement.getSourceChildren(getContent().getContentFilter()).toArray();
			}
		}
		return super.getChildren(parentElement);
	}
	
}