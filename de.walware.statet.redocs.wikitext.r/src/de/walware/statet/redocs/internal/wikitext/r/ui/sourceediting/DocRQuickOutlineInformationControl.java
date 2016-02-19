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

package de.walware.statet.redocs.internal.wikitext.r.ui.sourceediting;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.IModelElement.Filter;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.QuickOutlineInformationControl;
import de.walware.ecommons.ltk.ui.sourceediting.actions.OpenDeclaration;
import de.walware.ecommons.ui.content.ITextElementFilter;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RModel;

import de.walware.statet.redocs.internal.wikitext.r.ui.util.WikitextRNameElementFilter;
import de.walware.statet.redocs.wikitext.r.core.model.WikitextRweaveModel;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveLabelProvider;


class DocRQuickOutlineInformationControl extends QuickOutlineInformationControl {
	
	
	private class ContentFilter implements IModelElement.Filter {
		
		@Override
		public boolean include(final IModelElement element) {
			if (element.getModelTypeId() == RModel.R_TYPE_ID) {
				switch (element.getElementType()) {
				case IRElement.R_ARGUMENT:
					return false;
				default:
					return true;
				}
			}
			return true;
		};
		
	}
	
	
	private final ContentFilter contentFilter= new ContentFilter();
	
	
	public DocRQuickOutlineInformationControl(final Shell parent, final String modelType,
			final String commandId) {
		super(parent, commandId, 2, new OpenDeclaration());
	}
	
	
	@Override
	public String getModelTypeId() {
		if (getIterationPosition() == 1) {
			return RModel.R_TYPE_ID;
		}
		return WikitextRweaveModel.WIKIDOC_R_MODEL_TYPE_ID;
	}
	
	@Override
	protected int getInitialIterationPage(final ISourceElement element) {
//		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
//			return 1;
//		}
		return 0;
	}
	
	@Override
	protected String getDescription(final int iterationPage) {
		if (iterationPage == 1) {
			return "R Outline";
		}
		return super.getDescription(iterationPage);
	}
	
	@Override
	protected OutlineContentProvider createContentProvider() {
		return new DocROutlineContentProvider(new OutlineContent());
	}
	
	@Override
	protected Filter getContentFilter() {
		return this.contentFilter;
	}
	
	@Override
	protected ITextElementFilter createNameFilter() {
		return new WikitextRNameElementFilter();
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new WikitextRweaveLabelProvider(0));
	}
	
}
