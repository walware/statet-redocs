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

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;
import de.walware.ecommons.ltk.core.refactoring.MultiRefactoringFactory;

import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.core.refactoring.TexRefactoring;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;


public class LtxRweaveRefactoringFactory extends MultiRefactoringFactory {
	
	
	private static final LtxRweaveRefactoringFactory INSTANCE= new LtxRweaveRefactoringFactory();
	
	public static LtxRweaveRefactoringFactory getInstance() {
		return INSTANCE;
	}
	
	
	public LtxRweaveRefactoringFactory() {
		super(TexModel.LTX_TYPE_ID, TexRefactoring.getLtxFactory());
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
