/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.statet.redocs.tex.r.ui.editors.LtxRweaveMergeViewer;


public class LtxRweaveMergeViewerCreator implements IViewerCreator {
	
	
	public LtxRweaveMergeViewerCreator() {
	}
	
	
	@Override
	public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
		return new LtxRweaveMergeViewer(parent, config);
	}
	
}
