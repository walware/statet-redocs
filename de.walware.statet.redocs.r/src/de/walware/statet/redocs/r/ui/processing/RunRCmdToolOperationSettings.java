/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.r.ui.processing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import de.walware.docmlet.base.ui.processing.operations.AbstractLaunchConfigOperationSettings;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;

import de.walware.statet.redocs.internal.r.Messages;


public class RunRCmdToolOperationSettings extends AbstractLaunchConfigOperationSettings {
	
	
	public RunRCmdToolOperationSettings() {
		super(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID);
	}
	
	
	@Override
	public String getId() {
		return RunRCmdToolOperation.ID;
	}
	
	@Override
	public String getLabel() {
		return Messages.ProcessingOperation_RunRCmdTool_label;
	}
	
	
	@Override
	protected boolean includeLaunchConfig(final ILaunchConfiguration config) {
		try {
			final String cmd= config.getAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, ""); //$NON-NLS-1$
			return (cmd.equals("CMD Sweave")); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			return false;
		}
	}
	
	@Override
	protected String getNewLaunchConfigName() {
		return "R CMD Sweave"; //$NON-NLS-1$
	}
	
	@Override
	protected void initializeNewLaunchConfig(final ILaunchConfigurationWorkingCopy config) {
		RCmdLaunching.initializeRCmdConfig(config, "CMD Sweave"); //$NON-NLS-1$
	}
	
}
