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

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import static de.walware.docmlet.base.ui.processing.DocProcessingToolConfig.StepConfig.RUN_DEFAULT;
import static de.walware.docmlet.base.ui.processing.DocProcessingToolConfig.StepConfig.RUN_EXPLICITE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.BUILDTEX_TYPE_RCONSOLE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.SWEAVE_TYPE_RCMD;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.SWEAVE_TYPE_RCONSOLE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.VARNAME_LATEX_FILE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.VARNAME_OUTPUT_FILE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.VARNAME_SWEAVE_FILE;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.variables.core.VariableText;
import de.walware.ecommons.variables.core.VariableText.LocationProcessor;

import de.walware.docmlet.base.ui.processing.DocProcessingToolProcess;
import de.walware.docmlet.base.ui.processing.operations.OpenUsingEclipseOperation;
import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.AbstractBuilder;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.TexlipseBuilder;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.core.RUtil;

import de.walware.rj.services.RServiceControlExtension;

import de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.Config;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


class TexRweaveProcessToolProcess extends DocProcessingToolProcess {
	
	
	private static final int TICKS_PREPARER= 5;
	private static final int TICKS_RWEAVE= 30;
	private static final int TICKS_TEX= 30;
	private static final int TICKS_OPEN_TEX= 5;
	private static final int TICKS_OPEN_OUTPUT= 20;
	private static final int TICKS_REST= 10;
	
	
	public static final List<String> SWEAVE_FOLDER_VARNAMES= ImCollections.newList(
			VARNAME_SWEAVE_FILE );
	public static final List<String> SWEAVE_COMMAND_VARNAMES= ImCollections.newList(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	public static final List<String> OUTPUT_DIR_VARNAMES= ImCollections.newList(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE );
	public static final List<String> TEX_COMMAND_VARNAMES= ImCollections.newList(
			VARNAME_SWEAVE_FILE, VARNAME_LATEX_FILE, VARNAME_OUTPUT_FILE );
	
	
	private class R implements IToolRunnable {
		
		
		public static final int TASK_FINISHED= 1;
		public static final int TASK_PREPARE_TEX= 2;
		
		private int task= 0;
		
		
		R() {
		}
		
		@Override
		public String getTypeId() {
			return "r/sweave/commands"; //$NON-NLS-1$
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
		}
		
		@Override
		public String getLabel() {
			return NLS.bind(Messages.RweaveTexProcessing_Sweave_Task_label, getConfig().getSourceFile().getName());
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case BEING_ABANDONED:
				getStatus().add(new Status(IStatus.CANCEL, TexRweaveUI.PLUGIN_ID, -1,
						Messages.RweaveTexProcessing_Sweave_Task_info_Canceled_message, null));
				continueAfterR();
				break;
			// finishing handled in run
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IRBasicAdapter r= (IRBasicAdapter) service;
			TexRweaveProcessToolProcess.this.fProgress2= monitor;
			Callable<Boolean> cancel= null;
			if (r instanceof RServiceControlExtension) {
				cancel= new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						terminate();
						return Boolean.FALSE;
					}
				};
				((RServiceControlExtension) r).addCancelHandler(cancel);
			}
			try {
				if (checkExit(0)) {
					return;
				}
				
				final ToolWorkspace workspace= r.getWorkspaceData();
				if (TexRweaveProcessToolProcess.this.fWorkingFolder == null) {
					r.refreshWorkspaceData(0, monitor);
					updatePathInformations(r.getWorkspaceData());
				}
				else {
					String path= workspace.toToolPath(TexRweaveProcessToolProcess.this.fWorkingFolder);
					path= RUtil.escapeBackslash(path);
					r.submitToConsole("setwd(\""+path+"\")", monitor); //$NON-NLS-1$ //$NON-NLS-2$
					r.refreshWorkspaceData(0, monitor);
					if (!TexRweaveProcessToolProcess.this.fWorkingFolder.equals(workspace.getWorkspaceDir())) {
						getStatus().add(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
								"Failed to set the R working directory." ));
					}
				}
				
				if (checkExit(0)) {
					return;
				}
				
				final LocationProcessor processor= new LocationProcessor() {
					@Override
					public String process(String path) throws CoreException {
						final IFileStore store= FileUtil.getFileStore(path);
						path= workspace.toToolPath(store);
						path= RUtil.escapeBackslash(path);
						return path;
					}
				};
				
				if (getConfig().weave.isRun() && TexRweaveProcessToolProcess.this.fSweaveType == SWEAVE_TYPE_RCONSOLE) {
					monitor.subTask("Sweave"); //$NON-NLS-1$
					final SubMonitor progress= TexRweaveProcessToolProcess.this.fProgress.newChild(TICKS_RWEAVE);
					progress.beginTask(Messages.RweaveTexProcessing_Sweave_InConsole_label, 100);
					
					try {
						TexRweaveProcessToolProcess.this.fSweaveRCommands.set(VARNAME_SWEAVE_FILE, getConfig().getSourceFile().getFullPath().toString());
						TexRweaveProcessToolProcess.this.fSweaveRCommands.set(VARNAME_LATEX_FILE, TexRweaveProcessToolProcess.this.fTexFile.getFullPath().toString());
						TexRweaveProcessToolProcess.this.fSweaveRCommands.set(VARNAME_OUTPUT_FILE, TexRweaveProcessToolProcess.this.fTexPathConfig.getOutputFile().getFullPath().toString());
						TexRweaveProcessToolProcess.this.fSweaveRCommands.performFinalStringSubstitution(processor);
					}
					catch (final NullPointerException e) {
						throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
								Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message));
					}
					catch (final CoreException e) {
						throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
								Messages.RweaveTexProcessing_Sweave_error_ResourceVariable_message + ' ' + e.getLocalizedMessage()));
					}
					final String[] commands= RUtil.LINE_SEPARATOR_PATTERN.split(TexRweaveProcessToolProcess.this.fSweaveRCommands.getText());
					for (int i= 0; i < commands.length; i++) {
						r.submitToConsole(commands[i], monitor);
					}
					if (r instanceof IRequireSynch) {
						((IRequireSynch) r).synch(monitor);
					}
				}
				
				if (getConfig().produce.isRun() && TexRweaveProcessToolProcess.this.fTexType == BUILDTEX_TYPE_RCONSOLE) {
					monitor.subTask("LaTeX"); //$NON-NLS-1$
					if (checkExit(0)) {
						return;
					}
					
					waitTask(TASK_PREPARE_TEX);
					if (checkExit(0) || this.task < 0) {
						return;
					}
					
					final SubMonitor progress= TexRweaveProcessToolProcess.this.fProgress.newChild(TICKS_TEX);
					progress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
					
					try {
						TexRweaveProcessToolProcess.this.fTexRCommands.set(VARNAME_SWEAVE_FILE, getConfig().getSourceFile().getFullPath().toString());
						TexRweaveProcessToolProcess.this.fTexRCommands.set(VARNAME_LATEX_FILE, TexRweaveProcessToolProcess.this.fTexFile.getFullPath().toString());
						TexRweaveProcessToolProcess.this.fTexRCommands.set(VARNAME_OUTPUT_FILE, TexRweaveProcessToolProcess.this.fTexPathConfig.getOutputFile().getFullPath().toString());
						TexRweaveProcessToolProcess.this.fTexRCommands.performFinalStringSubstitution(processor);
						progress.setWorkRemaining(90);
					}
					catch (final NullPointerException e) {
						throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
								Messages.RweaveTexProcessing_Tex_error_ResourceVariable_message));
					}
					catch (final CoreException e) {
						throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
								Messages.RweaveTexProcessing_Tex_error_ResourceVariable_message + ' ' + e.getLocalizedMessage()));
					}
					
					Texlipse.getViewerManager().closeDocInViewer(TexRweaveProcessToolProcess.this.fTexPathConfig);
					
					final String[] commands= RUtil.LINE_SEPARATOR_PATTERN.split(TexRweaveProcessToolProcess.this.fTexRCommands.getText());
					for (int i= 0; i < commands.length; i++) {
						r.submitToConsole(commands[i], monitor);
						progress.setWorkRemaining(90-80/commands.length*(i+1));
					}
					if (r instanceof IRequireSynch) {
						((IRequireSynch) r).synch(monitor);
					}
				}
			}
			catch (final CoreException e) {
				getStatus().add(e.getStatus());
				throw e;
			}
			finally {
				if (cancel != null) {
					((RServiceControlExtension) r).removeCancelHandler(cancel);
					cancel= null;
				}
				continueAfterR();
				TexRweaveProcessToolProcess.this.fProgress2= null;
			}
			
		}
		
		private void updatePathInformations(final ToolWorkspace workspace) {
			final IFileStore wd= workspace.getWorkspaceDir();
			final IStatus status= setWorkingDir(wd, null, true);
			if (status.getSeverity() > IStatus.OK) {
				getStatus().add(status);
			}
		}
		
		private synchronized void waitTask(final int task) {
			this.task= task;
			while (this.task == task) {
				notifyAll();
				try {
					this.wait();
				}
				catch (final InterruptedException e) {
				}
			}
		}
		
		private synchronized void continueAfterR() {
			this.task= TASK_FINISHED;
			notifyAll();
		}
		
	}
	
	private SubMonitor fProgress;
	private IProgressMonitor fProgress2;
	
	private IContainer fWorkingFolderInWorkspace;
	private IFileStore fWorkingFolder;
	private String fBaseFileName;
	private String fTexFileExtension;
	
	private int fSweaveType;
	private VariableText fSweaveRCommands;
	private ILaunchConfiguration fSweaveConfig;
	
	private String fOutputFormat;
	private VariableText fOutputDir;
	private boolean fOutputInitialized;
	
	private IFile fTexFile;
	int fTexOpenEditor= 0;
	private int fTexType;
	private Builder fTexBuilder;
	private VariableText fTexRCommands;
	private TexPathConfig fTexPathConfig;
	
	ViewerConfiguration fPreviewConfig;
	
	
	public TexRweaveProcessToolProcess(final ILaunch launch, final TexRweaveLaunchDelegate.Config config) {
		super(launch, config);
	}
	
	
	@Override
	public TexRweaveLaunchDelegate.Config getConfig() {
		return (TexRweaveLaunchDelegate.Config) super.getConfig();
	}
	
	public void setWorkingDir(final VariableText wd) throws CoreException {
		wd.performInitialStringSubstitution(true);
		wd.set(VARNAME_SWEAVE_FILE, getConfig().getSourceFile().getFullPath().toString());
		wd.performFinalStringSubstitution(null);
		
		final FileValidator validator= new FileValidator(false);
		validator.setResourceLabel("Sweave Working / Output Folder");
		validator.setOnFile(IStatus.ERROR);
		validator.setOnExisting(IStatus.OK);
		validator.setOnNotExisting(IStatus.ERROR);
		validator.setRequireWorkspace(true, true);
		{	final IStatus status= validator.validate(wd.getText());
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
		{	final IStatus status= setWorkingDir(null, (IContainer) validator.getWorkspaceResource(), true);
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setSweave(final VariableText rCommands) throws CoreException {
		if (this.fSweaveType > 0 || rCommands == null) {
			throw new IllegalArgumentException();
		}
		this.fSweaveType= SWEAVE_TYPE_RCONSOLE;
		this.fSweaveRCommands= rCommands;
		this.fSweaveRCommands.performInitialStringSubstitution(true);
	}
	
	public void setSweave(final ILaunchConfiguration rCmd) {
		if (this.fSweaveType > 0 || rCmd == null) {
			throw new IllegalArgumentException();
		}
		this.fSweaveType= SWEAVE_TYPE_RCMD;
		this.fSweaveConfig= rCmd;
	}
	
	public void setOutput(final VariableText directory, final String format) throws CoreException {
		this.fOutputDir= directory;
		this.fOutputDir.performInitialStringSubstitution(true);
		this.fOutputFormat= format;
		
		if (this.fWorkingFolder != null && !this.fOutputInitialized) {
			final IStatus status= initOutputDir();
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
	
	public void setBuildTex(final VariableText commands) throws CoreException {
		if (this.fTexType > 0 || commands == null) {
			throw new IllegalArgumentException();
		}
		this.fTexType= BUILDTEX_TYPE_RCONSOLE;
		this.fTexRCommands= commands;
		this.fTexRCommands.performInitialStringSubstitution(true);
	}
	
	public void setBuildTex(final Builder texBuilder) {
		if (this.fTexType > 0 || texBuilder == null) {
			throw new IllegalArgumentException();
		}
		this.fTexType= BUILDTEX_TYPE_ECLIPSE;
		this.fTexBuilder= texBuilder;
	}
	
	
	public IStatus setWorkingDir(final IFileStore efsFolder, final IContainer workspaceFolder, final boolean synch) {
		this.fWorkingFolder= efsFolder;
		this.fWorkingFolderInWorkspace= workspaceFolder;
		if (synch) {
			if (this.fWorkingFolder == null && this.fWorkingFolderInWorkspace != null) {
				this.fWorkingFolder= EFS.getLocalFileSystem().getStore(this.fWorkingFolderInWorkspace.getLocation());
			}
			else if (this.fWorkingFolder != null && this.fWorkingFolderInWorkspace == null) {
				final IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
				final IContainer[] found= root.findContainersForLocationURI(this.fWorkingFolder.toURI());
				for (int i= 0; i < found.length; i++) {
					if (found[i].getType() == IResource.PROJECT || found[i].getType() == IResource.FOLDER) {
						this.fWorkingFolderInWorkspace= found[i];
						break;
					}
				}
			}
		}
		if (this.fWorkingFolderInWorkspace == null) {
			doSetExitValue(11);
			return new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_MustBeInWorkspace_message, null);
		}
		
		if (this.fBaseFileName == null) {
			this.fBaseFileName= getConfig().getSourceFile().getName();
			final int idx= this.fBaseFileName.lastIndexOf('.');
			if (idx >= 0) {
				this.fBaseFileName= this.fBaseFileName.substring(0, idx);
			}
		}
			
		if (this.fTexFileExtension == null) {
			this.fTexFileExtension= "tex";  //$NON-NLS-1$
		}
		this.fTexFile= this.fWorkingFolderInWorkspace.getFile(new Path(this.fBaseFileName + '.' + this.fTexFileExtension));
		
		if (this.fOutputDir != null && !this.fOutputInitialized) {
			return initOutputDir();
		}
		return Status.OK_STATUS;
	}
	
	private IStatus initOutputDir() {
		this.fOutputInitialized= true;
		final String texFilePath= this.fTexFile.getFullPath().toString();
		this.fOutputDir.set(VARNAME_SWEAVE_FILE, getConfig().getSourceFile().getFullPath().toString());
		this.fOutputDir.set(VARNAME_LATEX_FILE, texFilePath);
		
		// 21x
		if (this.fOutputFormat == null) {
			return new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_BuilderNotConfigured_message, null );
		}
		final IContainer outputDir;
		try {
			outputDir= TexPathConfig.resolveDirectory(this.fOutputDir.getText(), this.fTexFile, getConfig().getSourceFile());
		}
		catch (final CoreException e) {
			return new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_Tex_error_OutputDir_message, e );
		}
		this.fTexPathConfig= new TexPathConfig(this.fTexFile, outputDir, this.fOutputFormat);
		
		return Status.OK_STATUS;
	}
	
	public IFileStore getWorkingDirectory() {
		return this.fWorkingFolder;
	}
	
	
	@Override
	protected void runProcessing(final SubMonitor progress) {
		this.fProgress= progress;
		
		if (checkExit(0)) {
			return;
		}
		
		doWeave();
		if (checkExit(0)) {
			return;
		}
		
		if (!this.fOutputInitialized) {
			final IStatus status= initOutputDir();
			if (!status.isOK()) {
				getStatus().add(status);
			}
		}
		if (checkExit(0)) {
			return;
		}
		
		if (getConfig().produce.isRun() && this.fTexType == BUILDTEX_TYPE_RCONSOLE) {
			finallyTex(this.fProgress.newChild(1));
		}
		else {
			doPrepareTex();
			if (checkExit(0)) {
				return;
			}
			
			doProcessTex();
			if (checkExit(0)) {
				return;
			}
		}
		
		doOpenOutput();
		
		this.fProgress.done();
	}
	
	@Override
	protected int calculateTicks() {
		final Config config= getConfig();
		int sum= 0;
		if (config.weave.isRun()) {
			sum += TICKS_RWEAVE;
		}
		else {
			sum += TICKS_RWEAVE/10;
		}
		if (this.fTexOpenEditor >= TexTab.OPEN_ALWAYS) {
			sum += TICKS_OPEN_TEX;
		}
		if (config.produce.isRun()) {
			sum += TICKS_TEX;
		}
		if (config.preview.isRun()) {
			sum += TICKS_OPEN_OUTPUT;
		}
		sum += TICKS_REST;
		
		return sum;
	}
	
	
	@Override
	protected void runFinished() {
		super.runFinished();
		
		this.fProgress= null;
		
		if (getStatus().getSeverity() > IStatus.OK) {
			if (getStatus().getSeverity() == IStatus.ERROR) {
				StatusManager.getManager().handle(getStatus(), StatusManager.LOG | StatusManager.SHOW);
				return;
			}
			StatusManager.getManager().handle(getStatus(), StatusManager.LOG);
		}
	}
	
	
	private void doWeave() { // 1xx
		final Config config= getConfig();
		if (this.fSweaveType == SWEAVE_TYPE_RCONSOLE || this.fTexType == BUILDTEX_TYPE_RCONSOLE) { // 11x
			if (!(config.weave.isRun() || config.produce.isRun()) && this.fWorkingFolder != null) {
				return;
			}
			try {
	//			RCodeLaunchRegistry.runRCodeDirect(RUtil.LINE_SEPARATOR_PATTERN.split(fSweaveCommands), false);
				final ToolProcess rProcess= NicoUI.getToolRegistry().getActiveToolSession(
						getConfig().getWorkbenchPage() ).getProcess();
				if (rProcess == null) {
					NicoUITools.accessTool(RConsoleTool.TYPE, rProcess); // throws CoreException
				}
				
				final R rTask= new R();
				if (config.weave.isRun() || config.produce.isRun()) {
					this.fProgress.worked(TICKS_PREPARER);
					
					final IStatus submitStatus= rProcess.getQueue().add(rTask);
					if (submitStatus.getSeverity() > IStatus.OK) {
						getStatus().add(submitStatus);
						if (checkExit(112)) {
							return;
						}
					}
					RTASK: while (true) {
						synchronized (rTask) {
							boolean ok= false;
							try {
								rTask.notifyAll();
								if (rTask.task != R.TASK_FINISHED && checkExit(0)) {
									rTask.task= -1;
									// removing runnable sets the cancel status
									rProcess.getQueue().remove(rTask);
								}
								switch (rTask.task) {
								case R.TASK_FINISHED:
									ok= true;
									break RTASK;
								case R.TASK_PREPARE_TEX:
									doPrepareTex();
									ok= true;
									rTask.task= 0;
									break;
								default:
									ok= true;
								}
								rTask.wait(100);
							}
							catch (final InterruptedException e) {
								// continue loop, monitor is checked
							}
							finally {
								if (!ok) {
									rTask.task= -1;
								}
							}
						}
					}
					if (checkExit(113)) {
						return;
					}
				}
				else if (this.fWorkingFolder == null) { // we need the working directory
					final SubMonitor progress= this.fProgress.newChild(TICKS_RWEAVE/10);
					rTask.updatePathInformations(rProcess.getWorkspaceData());
					progress.done();
				}
			}
			catch (final CoreException e) {
				abort(e, 110);
				return;
			}
		}
		else if (this.fSweaveConfig != null) { // 12x
			if (!config.weave.isRun() && this.fWorkingFolder != null) {
				return;
			}
			try {
				if (config.weave.isRun()) {
					final SubMonitor monitor= this.fProgress.newChild(TICKS_RWEAVE);
					monitor.beginTask(Messages.RweaveTexProcessing_Sweave_RCmd_label, 100);
					final ILaunchConfigurationDelegate delegate= LaunchUtils.getLaunchConfigurationDelegate(
							this.fSweaveConfig, ILaunchManager.RUN_MODE, getStatus() );
					delegate.launch(this.fSweaveConfig, ILaunchManager.RUN_MODE, getLaunch(), monitor.newChild(75));
					final IProcess[] processes= getLaunch().getProcesses();
					if (processes.length == 0) {
						throw new IllegalStateException();
					}
					final IProcess sweaveProcess= processes[processes.length-1];
					if (!sweaveProcess.isTerminated()) {
						throw new IllegalStateException();
					}
					final int exitValue= sweaveProcess.getExitValue();
					if (exitValue != 0) {
						abort(IStatus.CANCEL, NLS.bind(Messages.RweaveTexProcessing_Sweave_RCmd_error_Found_message, exitValue), null,
								121);
						return;
					}
					monitor.done();
				}
			}
			catch (final CoreException e) {
				abort(e, 120);
				return;
			}
		}
	}
	
	private void doPrepareTex() {
		final Config config= getConfig();
		final ISchedulingRule rule= beginSchedulingRule(this.fTexFile.getParent(), this.fProgress.newChild(1));
		try {
			if ((config.weave.isRun() || config.produce.isRun()) && this.fTexFile.exists() && this.fTexFile.getType() == IResource.FILE) {
				try {
					this.fTexFile.deleteMarkers(TexlipseBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
					this.fTexFile.deleteMarkers(TexlipseBuilder.LAYOUT_WARNING_TYPE, true, IResource.DEPTH_INFINITE);
				}
				catch (final CoreException e) {}
			}
			this.fProgress.worked(1);
			refreshDir(this.fTexFile, this.fProgress.newChild(1));
			if (checkExit(195)) {
				return;
			}
			
			final boolean exists= this.fTexFile.exists() && this.fTexFile.getType() == IResource.FILE;
			if (config.produce.isRun() && !exists) {
				doSetExitValue(199);
				getStatus().add(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, -1,
						NLS.bind(Messages.RweaveTexProcessing_Tex_error_NotFound_message, this.fTexFile.getFullPath().toString()), null));
				return;
			}
			
			if ((config.weave.isRun() || getConfig().produce.isRun()) && exists && this.fTexOpenEditor == TexTab.OPEN_ALWAYS) {
				final OpenUsingEclipseOperation operation= new OpenUsingEclipseOperation(this.fTexFile);
				try {
					operation.init(config.produce, Collections.EMPTY_MAP, this.fProgress.newChild(1));
					operation.setFailSeverity(IStatus.WARNING);
					operation.run(this, this.fProgress.newChild(TICKS_OPEN_TEX - 1));
				}
				catch (final CoreException e) {}
			}
		}
		finally {
			endSchedulingRule(rule);
		}
	}
	
	private void doProcessTex() { // 2xx
		if (getConfig().produce.isRun() && this.fTexType == TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE) {
			final SubMonitor progress= this.fProgress.newChild(TICKS_TEX);
			this.fProgress.beginTask(Messages.RweaveTexProcessing_Tex_label, 100);
			Texlipse.getViewerManager().closeDocInViewer(this.fTexPathConfig);
			try {
				this.fTexBuilder.reset(progress.newChild(60, SubMonitor.SUPPRESS_SUBTASK));
				this.fTexBuilder.build(this.fTexPathConfig);
				AbstractBuilder.checkOutput(this.fTexPathConfig, new SubProgressMonitor(progress, 10));
			}
			catch (final OperationCanceledException e) {
				abort(IStatus.CANCEL, Messages.RweaveTexProcessing_info_Canceled_message, e,
						211);
				return;
			}
			catch (final CoreException e) {
				abort(e, 210);
				return;
			}
			finally {
				finallyTex(progress);
			}
			progress.done();
		}
		
		if (getStatus().getSeverity() < IStatus.ERROR) {
			try { // 28x
				if (getConfig().produce.isRun() && this.fTexOpenEditor > TexTab.OPEN_ALWAYS && this.fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= this.fTexOpenEditor) {
					final OpenUsingEclipseOperation operation= new OpenUsingEclipseOperation(this.fTexFile);
					try {
						operation.init(getConfig().produce, Collections.EMPTY_MAP, this.fProgress.newChild(1));
						operation.setFailSeverity(IStatus.WARNING);
						operation.run(this, this.fProgress.newChild(TICKS_OPEN_TEX - 1));
					}
					catch (final CoreException e) {}
				}
			}
			catch (final CoreException e) {
				abort(e, 280);
				return;
			}
		}
	}
	
	private void finallyTex(final SubMonitor progress) {
		refreshDir(this.fTexPathConfig.getOutputFile(), progress.isCanceled() ? null : progress.newChild(5));
		if (!this.fWorkingFolderInWorkspace.equals(this.fTexPathConfig.getOutputFile().getParent())) {
			final Job job= new Job("Refresh after TeX build") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					return refreshDir(TexRweaveProcessToolProcess.this.fWorkingFolderInWorkspace, progress.isCanceled() ? null : progress.newChild(5));
				}
			};
			job.setSystem(true);
			final IResourceRuleFactory ruleFactory= this.fWorkingFolderInWorkspace.getWorkspace().getRuleFactory();
			job.setRule(ruleFactory.refreshRule(this.fWorkingFolderInWorkspace));
		}
	}
	
	private void doOpenOutput() { // 3xx
		final Config config= getConfig();
		if (config.preview.isRun()) {
			final SubMonitor progress= this.fProgress.newChild(TICKS_OPEN_OUTPUT);
			progress.setWorkRemaining(100);
			if (!this.fTexPathConfig.getOutputFile().exists()) {
				abort((config.preview.getRun() == RUN_EXPLICITE) ? IStatus.ERROR : IStatus.INFO,
						NLS.bind(Messages.RweaveTexProcessing_Output_error_NotFound_message, this.fTexPathConfig.getOutputFile().getFullPath().toString()), null,
						301);
				return;
			}
			try {
				if (config.preview.getRun() == RUN_DEFAULT && this.fTexFile.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ZERO) >= IMarker.SEVERITY_ERROR) {
					abort(IStatus.CANCEL, Messages.RweaveTexProcessing_Output_info_SkipBecauseTex_message, null,
							302);
					return;
				}
			}
			catch (final CoreException e) {
				abort(e, 303);
				return;
			}
			progress.worked(10);
			if (this.fPreviewConfig != null) {
				Texlipse.getViewerManager().openDocInViewer(this.fTexPathConfig, this.fPreviewConfig);
			}
			else {
				final OpenUsingEclipseOperation operation= new OpenUsingEclipseOperation(
						this.fTexPathConfig.getOutputFile() );
				try {
					operation.init(getConfig().preview, Collections.EMPTY_MAP, progress);
					operation.run(this, progress);
				}
				catch (final CoreException e) {
					abort(e, 304);
				}
			}
			progress.done();
//			final ILaunchConfigurationDelegate delegate= getRunDelegate(fPreviewConfig);
//			delegate.launch(fPreviewConfig, ILaunchManager.RUN_MODE, fLaunch, new SubProgressMonitor(fMonitor, 10));
		}
	}
	
	private boolean checkExit(final int code) {
		if (getStatus().getSeverity() >= IStatus.ERROR) {
			if (code != 0 && doGetExitValue() == 0) {
				doSetExitValue(code);
			}
			return true;
		}
		if (this.fProgress.isCanceled()) {
			final IProgressMonitor p2= this.fProgress2;
			if (p2 != null && !p2.isCanceled()) {
				p2.setCanceled(true);
			}
			if (getStatus().getSeverity() < IStatus.CANCEL) { 
				getStatus().add(new Status(IStatus.CANCEL, TexRweaveUI.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
			}
			return true;
		}
		else {
			final IProgressMonitor p2= this.fProgress2;
			if (p2 != null && p2.isCanceled()) {
				this.fProgress.setCanceled(true);
				if (getStatus().getSeverity() < IStatus.CANCEL) { 
					getStatus().add(new Status(IStatus.CANCEL, TexRweaveUI.PLUGIN_ID, Messages.RweaveTexProcessing_info_Canceled_message));
				}
				return true;
			}
		}
		return false;
	}
	
	private void abort(final CoreException e, final int exitCode) {
		final IStatus status= e.getStatus();
		if (status.getSeverity() == IStatus.CANCEL) {
			getStatus().add(status);
		}
		else {
			abort(status.getSeverity(), status.getMessage(), e, exitCode);
		}
	}
	
	private void abort(final int severity, final String message, final Throwable cause, final int exitValue) {
		getStatus().add(new Status(severity, TexRweaveUI.PLUGIN_ID, -1, message, cause));
		doSetExitValue(exitValue);
	}
	
	
	private IStatus refreshDir(final IResource resource, final IProgressMonitor monitor) {
		try {
			resource.refreshLocal(IResource.DEPTH_ONE, monitor);
			return Status.OK_STATUS;
		}
		catch (final OperationCanceledException e) {
			return new Status(IStatus.CANCEL, TexRweaveUI.PLUGIN_ID, -1,
					Messages.RweaveTexProcessing_info_Canceled_message, e);
		}
		catch (final CoreException e) {
			return e.getStatus();
		}
	}
	
	
	@Override
	public void terminate() throws DebugException {
		{	final IProgressMonitor monitor= this.fProgress2;
			if (monitor != null) {
				monitor.setCanceled(true);
			}
		}
		
		super.terminate();
	}
	
}
