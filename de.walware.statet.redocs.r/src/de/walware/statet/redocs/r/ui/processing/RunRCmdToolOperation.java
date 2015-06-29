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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.debug.core.util.OverlayLaunchConfiguration;
import de.walware.ecommons.variables.core.VariableText2;
import de.walware.ecommons.variables.core.VariableUtils;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.processing.DocProcessingConfig;
import de.walware.docmlet.base.ui.processing.operations.AbstractLaunchConfigOperation;
import de.walware.docmlet.base.ui.processing.operations.RunExternalProgramOperation.IExternalProgramLaunchConfig;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;

import de.walware.statet.redocs.internal.r.Messages;


public class RunRCmdToolOperation extends AbstractLaunchConfigOperation {
	
	
	public static final String ID= "de.walware.statet.redocs.docProcessing.RunRCmdToolOperation"; //$NON-NLS-1$
	
	public static final String LAUNCH_CONFIG_NAME_ATTR_NAME= ID + '/' + LAUNCH_CONFIG_NAME_ATTR_KEY;
	
	
	public RunRCmdToolOperation() {
		super(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID);
	}
	
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getLabel() {
		return Messages.ProcessingOperation_RunRCmdTool_label;
	}
	
	
	@Override
	protected ILaunchConfiguration preprocessConfig(final ILaunchConfiguration config) throws CoreException {
		final Map<String, Object> additionalAttributes= new HashMap<>();
		final VariableText2 variableResolver= createVariableResolver();
		
		additionalAttributes.put(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		
		try {
			String value= config.getAttribute(RCmdLaunching.WORKING_DIRECTORY_ATTR_NAME, ""); //$NON-NLS-1$
			if (value.isEmpty()) {
				value= VariableUtils.getValue(getStepConfig().getToolConfig().getVariables()
						.get(DocProcessingConfig.WD_LOC_VAR_NAME) );
				additionalAttributes.put(IExternalProgramLaunchConfig.WORKING_DIRECTORY_ATTR_NAME, value);
			}
			else {
				value= variableResolver.performStringSubstitution(value, null);
				additionalAttributes.put(IExternalProgramLaunchConfig.ARGUMENTS_ATTR_NAME, value);
			}
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
					NLS.bind(Messages.ProcessingOperation_RunRCmdTool_Wd_error_SpecInvalid_message,
							e.getMessage() )));
		}
		
		try {
			String value= config.getAttribute(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, ""); //$NON-NLS-1$
			if (!value.isEmpty()) {
				value= variableResolver.performStringSubstitution(value, null);
				additionalAttributes.put(IExternalProgramLaunchConfig.ARGUMENTS_ATTR_NAME, value);
			}
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
					NLS.bind(Messages.ProcessingOperation_RunRCmdTool_RCmdResource_error_SpecInvalid_message,
							e.getMessage() )));
		}
		
		try {
			String value= config.getAttribute(RCmdLaunching.R_CMD_OPTIONS_ATTR_NAME, ""); //$NON-NLS-1$
			if (!value.isEmpty()) {
				value= variableResolver.performStringSubstitution(value, null);
				additionalAttributes.put(IExternalProgramLaunchConfig.ARGUMENTS_ATTR_NAME, value);
			}
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
					NLS.bind(Messages.ProcessingOperation_RunRCmdTool_RCmdOptions_error_SpecInvalid_message,
							e.getMessage() )));
		}
		
		return new OverlayLaunchConfiguration(config, additionalAttributes);
	}
	
}
