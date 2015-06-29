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

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ts.ui.IToolRunnableDecorator;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.processing.DocProcessingToolOperationContext;
import de.walware.docmlet.base.ui.processing.DocProcessingToolProcess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RProcess;

import de.walware.rj.services.RServiceControlExtension;

import de.walware.statet.redocs.internal.r.Messages;


public class RConsoleOperationContext extends DocProcessingToolOperationContext {
	
	
	private class RRunnable implements IToolRunnable, IToolRunnableDecorator {
		
		
		public RRunnable() {
		}
		
		
		@Override
		public String getTypeId() {
			return "r/redocs/RConsoleOperation"; //$NON-NLS-1$
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
		}
		
		@Override
		public Image getImage() {
			return RConsoleOperationContext.this.toolProcess.getImage();
		}
		
		@Override
		public String getLabel() {
			return NLS.bind(Messages.ProcessingOperationContext_RConsole_RTask_label,
					RConsoleOperationContext.this.toolProcess.getLabel() );
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case BEING_ABANDONED:
				RConsoleOperationContext.this.toolProcess.log(new Status(IStatus.CANCEL, DocBaseUI.PLUGIN_ID,
						Messages.ProcessingOperationContext_RConsole_RTask_Canceled_label) );
				cleanR();
				cancel();
				return true;
			default:
				return true;
			}
		}
		
		@Override
		public void run(final IToolService service, final IProgressMonitor monitor) throws CoreException {
			initR((IRBasicAdapter) service, monitor);
			Callable<Boolean> cancel= null;
			if (service instanceof RServiceControlExtension) {
				cancel= new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						RConsoleOperationContext.this.toolProcess.terminate();
						return Boolean.FALSE;
					}
				};
				((RServiceControlExtension) service).addCancelHandler(cancel);
			}
			try {
				runInContext();
			}
			finally {
				if (cancel != null) {
					((RServiceControlExtension) service).removeCancelHandler(cancel);
					cancel= null;
				}
				cleanR();
			}
		}
		
	}
	
	
	public static final String ID= "RConsole"; //$NON-NLS-1$
	
	
	private DocProcessingToolProcess toolProcess;
	
	private RProcess rProcess;
	private RRunnable rRunnable;
	
	private IRBasicAdapter rService;
	private IProgressMonitor rMonitor;
	
	
	public RConsoleOperationContext() {
	}
	
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getLabel() {
		return Messages.ProcessingOperationContext_RConsole_label;
	}
	
	
	@Override
	protected void start(final DocProcessingToolProcess toolProcess,
			final SubMonitor m) throws CoreException {
		m.beginTask("Starting doc operation(s) in R console...", 10);
		
		if (this.toolProcess == null) {
			this.toolProcess= toolProcess;
			this.rProcess= getRProcess();
		}
		
		m.setTaskName("Waiting for R console...");
		this.rRunnable= new RRunnable();
		final IStatus submitStatus= this.rProcess.getQueue().add(this.rRunnable);
		this.toolProcess.check(submitStatus);
	}
	
	private RProcess getRProcess() throws CoreException {
		final ToolProcess process= NicoUI.getToolRegistry().getActiveToolSession(
				this.toolProcess.getConfig().getWorkbenchPage() ).getProcess();
		NicoUITools.accessTool(RConsoleTool.TYPE, process);
		return (RProcess) process;
	}
	
	@Override
	public void canceling(final boolean running) {
		if (this.rProcess == null) {
			return;
		}
		
		if (!running) {
			final RRunnable runnable= this.rRunnable;
			if (runnable != null) {
				this.rProcess.getQueue().remove(runnable);
			}
		}
		final IProgressMonitor monitor= this.rMonitor;
		if (monitor != null) {
			monitor.setCanceled(true);
		}
	}
	
	private void initR(final IRBasicAdapter service, final IProgressMonitor monitor) {
		this.rService= service;
		this.rMonitor= monitor;
	}
	
	private void cleanR() {
		this.rRunnable= null;
		this.rService= null;
		this.rMonitor= null;
	}
	
	
	public IRBasicAdapter getRService() {
		return this.rService;
	}
	
	public IProgressMonitor getRMonitor() {
		return this.rMonitor;
	}
	
}
