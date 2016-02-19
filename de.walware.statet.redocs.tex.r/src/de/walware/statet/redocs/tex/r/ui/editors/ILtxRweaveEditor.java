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

package de.walware.statet.redocs.tex.r.ui.editors;

import de.walware.docmlet.tex.ui.editors.ILtxEditor;

import de.walware.statet.redocs.r.ui.sourceediting.IRweaveEditor;
import de.walware.statet.redocs.tex.r.core.model.ILtxRweaveSourceUnit;


public interface ILtxRweaveEditor extends IRweaveEditor, ILtxEditor {
	
	
	@Override
	public ILtxRweaveSourceUnit getSourceUnit();
	
}
