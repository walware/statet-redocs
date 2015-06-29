/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionElementSearchContributionItem;
import de.walware.ecommons.ui.actions.ListContributionItem;

import de.walware.statet.r.ui.editors.RElementSearchContributionItem;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentContentInfo;


public class ElementSearchContributionItem extends MultiContentSectionElementSearchContributionItem {
	
	
	public ElementSearchContributionItem() {
		super(LtxRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	protected ListContributionItem createItem(final String sectionType) {
		switch (sectionType) {
		case LtxRweaveDocumentContentInfo.R:
			return new RElementSearchContributionItem(getCommandId());
		default:
			return null;
		}
	}
	
}
