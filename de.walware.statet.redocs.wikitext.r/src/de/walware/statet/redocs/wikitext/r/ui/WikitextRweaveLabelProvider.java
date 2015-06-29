/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.wikitext.r.ui;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.core.model.IEmbeddedForeignElement;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;

import de.walware.docmlet.wikitext.core.model.IWikitextSourceElement;
import de.walware.docmlet.wikitext.ui.WikitextLabelProvider;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.RLabelProvider;

import de.walware.statet.redocs.r.ui.RedocsRUIResources;


public class WikitextRweaveLabelProvider extends WikitextLabelProvider {
	
	
	private final RLabelProvider fRProvider;
	
	
	public WikitextRweaveLabelProvider(final int rStyle) {
		this.fRProvider= new RLabelProvider(rStyle);
	}
	
	
	@Override
	public Image getImage(final IModelElement element) {
		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
			return this.fRProvider.getImage(element);
		}
		return super.getImage(element);
	}
	
	@Override
	protected Image getEmbeddedForeignImage(final IModelElement element) {
		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
			return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.OBJ_RCHUNK_IMAGE_ID);
		}
		return super.getEmbeddedForeignImage(element);
	}
	
	@Override
	public String getText(final IModelElement element) {
		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
			return this.fRProvider.getText(element);
		}
		if (element.getElementType() == IWikitextSourceElement.C1_EMBEDDED) {
			final ISourceStructElement rElement= ((IEmbeddedForeignElement) element).getForeignElement();
			if (rElement != null) {
				return this.fRProvider.getText(rElement);
			}
		}
		return super.getText(element);
	}
	
	@Override
	public StyledString getStyledText(final IModelElement element) {
		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
			return this.fRProvider.getStyledText(element);
		}
		if (element.getElementType() == IWikitextSourceElement.C1_EMBEDDED) {
			final ISourceStructElement rElement= ((IEmbeddedForeignElement) element).getForeignElement();
			if (rElement != null) {
				return this.fRProvider.getStyledText(rElement);
			}
		}
		return super.getStyledText(element);
	}
	
}
