/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.editors;

import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;
import de.walware.ecommons.ltk.core.refactoring.MultiRefactoringFactory;

import de.walware.docmlet.wikitext.core.model.WikitextModel;
import de.walware.docmlet.wikitext.core.refactoring.WikitextRefactoring;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;


public class WikidocRweaveRefactoringFactory extends MultiRefactoringFactory {
	
	
	private static final WikidocRweaveRefactoringFactory INSTANCE= new WikidocRweaveRefactoringFactory();
	
	public static WikidocRweaveRefactoringFactory getInstance() {
		return INSTANCE;
	}
	
	
	public WikidocRweaveRefactoringFactory() {
		super(WikitextModel.WIKIDOC_TYPE_ID, WikitextRefactoring.getWikidocFactory());
	}
	
	
	@Override
	protected CommonRefactoringFactory createFactory(final String modelTypeId) {
		switch (modelTypeId) {
		case RModel.R_TYPE_ID:
			return RRefactoring.getFactory();
		default:
			return null;
		}
	}
	
}
