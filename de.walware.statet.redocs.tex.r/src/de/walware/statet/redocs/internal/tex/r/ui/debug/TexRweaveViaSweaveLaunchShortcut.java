/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.debug;

import de.walware.statet.r.launching.SubmitFileViaCommandLaunchShortcut;


public class TexRweaveViaSweaveLaunchShortcut extends SubmitFileViaCommandLaunchShortcut {
	
	
	public TexRweaveViaSweaveLaunchShortcut() {
		super("de.walware.statet.r.rFileCommand.SweaveRweaveTexDoc", false); //$NON-NLS-1$
	}
	
}
