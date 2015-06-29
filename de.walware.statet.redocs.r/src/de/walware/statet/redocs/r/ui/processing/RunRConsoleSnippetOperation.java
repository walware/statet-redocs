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

import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.variables.core.VariableText2;
import de.walware.ecommons.variables.core.VariableText2.Severities;
import de.walware.ecommons.variables.core.VariableUtils;

import de.walware.docmlet.base.ui.processing.DocProcessingConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingOperation;
import de.walware.docmlet.base.ui.processing.DocProcessingToolConfig.StepConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingToolOperationContext;
import de.walware.docmlet.base.ui.processing.DocProcessingToolProcess;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.RUtil;

import de.walware.statet.redocs.internal.r.Messages;
import de.walware.statet.redocs.r.RedocsRweave;


public class RunRConsoleSnippetOperation extends DocProcessingOperation {
	
	
	public static final String ID= "de.walware.statet.redocs.docProcessing.RunRConsoleSnippetOperation"; //$NON-NLS-1$
	
	public static final String R_SNIPPET_CODE_ATTR_NAME= ID + '/' + "RSnippet.code"; //$NON-NLS-1$
	
	
	private String rCodeSnippet;
	
	
	public RunRConsoleSnippetOperation() {
	}
	
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getLabel() {
		return Messages.ProcessingOperation_RunRConsoleSnippet_label;
	}
	
	
	@Override
	public void init(final StepConfig stepConfig, final Map<String, String> settings,
			final SubMonitor m) throws CoreException {
		super.init(stepConfig, settings, m);
		
		final String code= settings.get(R_SNIPPET_CODE_ATTR_NAME);
		if (code == null) {
			throw new CoreException(new Status(IStatus.ERROR, RedocsRweave.PLUGIN_ID,
					Messages.ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecMissing_message ));
		}
		try {
			getStepConfig().getVariableResolver().validate(code, Severities.CHECK_SYNTAX, null);
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RedocsRweave.PLUGIN_ID,
					NLS.bind(Messages.ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecInvalid_message,
							e.getMessage() )));
		}
		this.rCodeSnippet= code;
	}
	
	
	@Override
	public String getContextId() {
		return RConsoleOperationContext.ID;
	}
	
	@Override
	public DocProcessingToolOperationContext createContext() {
		return new RConsoleOperationContext();
	}
	
	
	@Override
	public IStatus run(final DocProcessingToolProcess toolProcess,
			final SubMonitor m) throws CoreException {
		final RConsoleOperationContext context= (RConsoleOperationContext) toolProcess.getCurrentOperationContext();
		final IRBasicAdapter r= context.getRService();
		final IProgressMonitor rMonitor= context.getRMonitor();
		
		m.beginTask(NLS.bind(Messages.ProcessingOperation_RunRConsoleSnippet_label,
						r.getTool().getLabel() ),
				10 );
		
		final RWorkspace rWorkspace= r.getWorkspaceData();
		final VariableText2 variableResolver= new VariableText2(
				getStepConfig().getVariableResolver().getExtraVariables() ) {
			@Override
			protected String checkValue(final IStringVariable variable, String value) throws CoreException {
				if (variable.getName().endsWith("_loc")) { //$NON-NLS-1$
					if (rWorkspace.isRemote()) {
						final IFileStore store= FileUtil.getFileStore(value);
						value= rWorkspace.toToolPath(store);
					}
					return RUtil.escapeBackslash(value);
				}
				return value;
			}
		};
		
		{	// Set wd
			final IFileStore dir= FileUtil.getFileStore(VariableUtils.getValue(
					variableResolver.getVariable(DocProcessingConfig.WD_LOC_VAR_NAME)) );
			IFileStore currentDir= rWorkspace.getWorkspaceDir();
			if (!dir.equals(currentDir)) {
				final String command= variableResolver.performStringSubstitution("setwd(\"${wd_loc}\")", null); //$NON-NLS-1$
				r.submitToConsole(command, rMonitor);
				r.refreshWorkspaceData(0, rMonitor);
				currentDir= rWorkspace.getWorkspaceDir();
				if (!dir.equals(currentDir)) {
					toolProcess.check(new Status(IStatus.ERROR, RedocsRweave.PLUGIN_ID,
							NLS.bind(Messages.ProcessingOperation_RunRConsoleSnippet_error_SetWdFailed_message,
									dir.toString(),
									(currentDir != null) ? currentDir.toString() : "<missing>" ) )); //$NON-NLS-1$
				}
			}
		}
		
		{	final String code;
			try {
				code= variableResolver.performStringSubstitution(this.rCodeSnippet, null);
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RedocsRweave.PLUGIN_ID,
						NLS.bind(Messages.ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecInvalid_message,
								e.getMessage() )));
			}
			final String[] codeLines= RUtil.LINE_SEPARATOR_PATTERN.split(code);
			for (int i= 0; i < codeLines.length; i++) {
				r.submitToConsole(codeLines[i], rMonitor);
			}
			if (r instanceof IRequireSynch) {
				((IRequireSynch) r).synch(rMonitor);
			}
		}
		
		return Status.OK_STATUS;
	}
	
}
