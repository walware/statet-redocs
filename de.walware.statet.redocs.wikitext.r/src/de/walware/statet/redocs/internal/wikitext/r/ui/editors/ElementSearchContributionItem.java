/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.editors;

import de.walware.ecommons.ltk.ui.sourceediting.actions.MultiContentSectionElementSearchContributionItem;
import de.walware.ecommons.ui.actions.ListContributionItem;

import de.walware.statet.r.ui.editors.RElementSearchContributionItem;

import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentContentInfo;


public class ElementSearchContributionItem extends MultiContentSectionElementSearchContributionItem {
	
	
	public ElementSearchContributionItem() {
		super(WikidocRweaveDocumentContentInfo.INSTANCE);
	}
	
	
	@Override
	protected ListContributionItem createItem(final String sectionType) {
		switch (sectionType) {
		case WikidocRweaveDocumentContentInfo.R:
			return new RElementSearchContributionItem(getCommandId());
		default:
			return null;
		}
	}
	
}
