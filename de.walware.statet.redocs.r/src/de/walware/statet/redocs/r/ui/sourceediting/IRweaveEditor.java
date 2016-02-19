/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.r.ui.sourceediting;

import de.walware.statet.r.ui.editors.IRSourceEditor;

import de.walware.statet.redocs.r.core.source.IDocContentSectionsRweaveExtension;


public interface IRweaveEditor extends IRSourceEditor {
	
	
	@Override
	IDocContentSectionsRweaveExtension getDocumentContentInfo();
	
}
