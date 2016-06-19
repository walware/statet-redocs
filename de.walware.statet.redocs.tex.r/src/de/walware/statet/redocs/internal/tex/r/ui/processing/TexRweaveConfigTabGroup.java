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

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.walware.ecommons.debug.ui.util.CheckedCommonTab;


/**
 * Tab group for Sweave (LaTeX+R) output creation toolchain.
 */
public class TexRweaveConfigTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public TexRweaveConfigTabGroup() {
	}
	
	@Override
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final ILaunchConfigurationTab[] tabs= new ILaunchConfigurationTab[] {
				new RweaveTab(),
				new TexTab(),
				new PreviewTab(),
				new CheckedCommonTab()
			};
			setTabs(tabs);
	}
	
}
