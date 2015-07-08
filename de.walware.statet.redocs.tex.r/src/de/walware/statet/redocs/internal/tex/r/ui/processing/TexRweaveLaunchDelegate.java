/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.util.OverlayLaunchConfiguration;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.variables.core.StringVariable;
import de.walware.ecommons.variables.core.VariableText;

import de.walware.docmlet.base.ui.processing.DocProcessingOperation;
import de.walware.docmlet.base.ui.processing.DocProcessingToolConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingToolConfig.StepConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingToolJob;
import de.walware.docmlet.base.ui.processing.DocProcessingToolLaunchDelegate;
import de.walware.docmlet.base.ui.processing.DocProcessingUI;
import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderRegistry;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;

import de.walware.statet.redocs.r.ui.processing.RWeaveDocProcessingConfig;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


public class TexRweaveLaunchDelegate extends DocProcessingToolLaunchDelegate {
	
	
	public static final String VARNAME_SWEAVE_FILE= "source_file_path"; //$NON-NLS-1$
	public static final String VARNAME_LATEX_FILE= "latex_file_path"; //$NON-NLS-1$
	public static final String VARNAME_OUTPUT_FILE= "output_file_path"; //$NON-NLS-1$
	
	public static final IStringVariable VARIABLE_SWEAVE_FILE= new StringVariable(VARNAME_SWEAVE_FILE, "Returns the workspace relative path of the Sweave file.");
	public static final IStringVariable VARIABLE_LATEX_FILE= new StringVariable(VARNAME_LATEX_FILE, "Returns the workspace relative path of the LaTeX file.");
	public static final IStringVariable VARIABLE_OUTPUT_FILE= new StringVariable(VARNAME_OUTPUT_FILE, "Returns the workspace relative path of the output file.");
	
	
	public static final String SWEAVE_CONSOLE= "console"; //$NON-NLS-1$
	public static final String SWEAVE_LAUNCH= "cmdlaunch"; //$NON-NLS-1$
	public static final String DEFAULT_SWEAVE_R_COMMANDS= "Sweave(file= \"${resource_loc:${"+VARNAME_SWEAVE_FILE+"}}\")"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int SWEAVE_TYPE_DISABLED= 0;
	public static final int SWEAVE_TYPE_RCMD= 1;
	public static final int SWEAVE_TYPE_RCONSOLE= 2;
	
	public static final int BUILDTEX_TYPE_DISABLED= 0;
	public static final int BUILDTEX_TYPE_ECLIPSE= 1;
	public static final int BUILDTEX_TYPE_RCONSOLE= 2;
	public static final int DEFAULT_BUILDTEX_TYPE= BUILDTEX_TYPE_ECLIPSE;
	
	public static final String DEFAULT_BUILDTEX_R_COMMANDS= "require(tools)\ntexi2dvi(file= \"${resource_loc:${"+VARNAME_LATEX_FILE+"}}\", pdf= TRUE)"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String DEFAULT_BUILDTEX_FORMAT= "pdf"; //$NON-NLS-1$
	
	public static final String PREVIEW_IDE= "ide"; //$NON-NLS-1$
	public static final String PREVIEW_SPECIAL= "tex"; //$NON-NLS-1$
	
	
	static class Config extends DocProcessingToolConfig {
		
		
//		protected static class WeaveStepConfig extends StepConfig {
//			
//			
//			public WeaveStepConfig(final DocProcessingToolConfig config) {
//				super(config, TexRweaveConfig.WEAVE_ATTR_QUALIFIER, Messages.WeaveTab_label);
//			}
//			
//			
//			@Override
//			public void initPost(final ILaunchConfiguration configuration,
//					final SubMonitor m) throws CoreException {
//				if (isPostOpen(configuration.getAttribute(
//						TexRweaveConfig.WEAVE_OPEN_RESULT_ATTR_NAME, "" ))) { //$NON-NLS-1$
////					addPost();
//				}
//			}
//			
//		}
		
		
		public final StepConfig weave= new StepConfig(this,
				RWeaveDocProcessingConfig.WEAVE_ATTR_QUALIFIER, Messages.Weave_label );
		public final StepConfig produce= new StepConfig(this,
				RWeaveDocProcessingConfig.PRODUCE_ATTR_QUALIFIER, Messages.Produce_label );
		public final StepConfig preview= new PreviewStepConfig(this);
		
		
		protected Config() {
			setSteps(this.weave, this.produce, this.preview);
		}
		
		
		protected boolean initRun(final ILaunchConfiguration configuration) throws CoreException {
			byte runWeave= 0;
			byte runProduce= 0;
			byte runPreview= 0;
			
			final Set<String> steps= configuration.getAttribute(
					DocProcessingUI.RUN_STEPS_ATTR_NAME, Collections.EMPTY_SET );
			if (steps.isEmpty()) {
				runWeave= StepConfig.RUN_DEFAULT;
				runProduce= StepConfig.RUN_DEFAULT;
				runPreview= StepConfig.RUN_DEFAULT;
			}
			else if (steps.contains(DocProcessingUI.PROCESSING_STEPS_FLAG)) {
				runWeave= StepConfig.RUN_DEFAULT;
				runProduce= StepConfig.RUN_DEFAULT;
			}
			else {
				if (steps.contains(DocProcessingUI.WEAVE_STEP)) {
					runWeave= StepConfig.RUN_EXPLICITE;
				}
				if (steps.contains(DocProcessingUI.PRODUCE_OUTPUT_STEP)) {
					runProduce= StepConfig.RUN_EXPLICITE;
				}
				if (steps.contains(DocProcessingUI.PREVIEW_OUTPUT_STEP)) {
					runPreview= StepConfig.RUN_EXPLICITE;
				}
				
				if (!(runWeave != 0 || runProduce != 0 || runPreview != 0)) {
					throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
							"Unsupported steps configurations: " + steps.toString() ));
				}
			}
			
			this.weave.initRun(runWeave, configuration);
			this.produce.initRun(runProduce, configuration);
			this.preview.initRun(runPreview, configuration);
			
			return (this.weave.isRun() || this.produce.isRun() || this.preview.isRun());
		}
		
		
		@Override
		protected DocProcessingOperation createStepOperation(final String id) {
			return null;
		}
		
	}
	
	
	public TexRweaveLaunchDelegate() {
	}
	
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final Config config= new Config();
		if (!config.initRun(configuration)) {
			return; // nothing to do
		}
		
		final SubMonitor m= SubMonitor.convert(monitor, "Configuring Document Processing...", 20 +
				((config.preview.getRun() == StepConfig.RUN_EXPLICITE) ? 30 : 0) );
		try {
			{	final SubMonitor mInit= m.newChild(10);
				config.initSourceFile(configuration, mInit);
			}
			
			final TexRweaveProcessToolProcess toolProcess= new TexRweaveProcessToolProcess(launch, config);
			
			// Tex config (for output format, before sweave)
			{	toolProcess.fTexOpenEditor= configuration.getAttribute(TexTab.ATTR_OPENTEX_ENABLED, TexTab.OPEN_OFF);
				
				final VariableText outputDir= new VariableText(
						replaceOldVariables(configuration.getAttribute(TexTab.ATTR_BUILDTEX_OUTPUTDIR, "")), //$NON-NLS-1$
						TexRweaveProcessToolProcess.OUTPUT_DIR_VARNAMES);
				final String outputFormat;
				
				int texType= configuration.getAttribute(TexTab.ATTR_BUILDTEX_TYPE, -2);
				if (texType == -2) {
					texType= configuration.getAttribute(TexTab.ATTR_BUILDTEX_ENABLED, false) ? BUILDTEX_TYPE_ECLIPSE : BUILDTEX_TYPE_DISABLED;
				}
				switch (texType) {
				case BUILDTEX_TYPE_ECLIPSE:
					final Builder builder= BuilderRegistry.get(configuration.getAttribute(TexTab.ATTR_BUILDTEX_ECLIPSE_BUILDERID, -1));
					toolProcess.setBuildTex(builder);
					outputFormat= (builder != null) ? builder.getOutputFormat() : null;
					break;
				case BUILDTEX_TYPE_RCONSOLE:
					toolProcess.setBuildTex(new VariableText(
							configuration.getAttribute(TexTab.ATTR_BUILDTEX_R_COMMANDS, ""), //$NON-NLS-1$
							TexRweaveProcessToolProcess.TEX_COMMAND_VARNAMES) );
					//$FALL-THROUGH$
				default:
					outputFormat= configuration.getAttribute(TexTab.ATTR_BUILDTEX_FORMAT, ""); //$NON-NLS-1$
				}
				toolProcess.setOutput(outputDir, outputFormat);
			}
			m.newChild(10);
			// Sweave config
			{	final String sweaveFolderRaw= configuration.getAttribute(RweaveTab.ATTR_SWEAVE_FOLDER, ""); //$NON-NLS-1$
				if (!sweaveFolderRaw.isEmpty()) {
					toolProcess.setWorkingDir(new VariableText(sweaveFolderRaw,
							TexRweaveProcessToolProcess.SWEAVE_FOLDER_VARNAMES ));
				}
				
				final String sweaveProcessing= configuration.getAttribute(RweaveTab.ATTR_SWEAVE_ID, ""); //$NON-NLS-1$
				if (sweaveProcessing.startsWith(TexRweaveLaunchDelegate.SWEAVE_LAUNCH)) {
					final String[] split= sweaveProcessing.split(":", 2); //$NON-NLS-1$
					final String sweaveConfigName= (split.length == 2) ? split[1] : ""; //$NON-NLS-1$
					
					final Map<String, Object> attributes= new HashMap<>();
					attributes.put(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, config.getSourceFile().getLocation().toOSString());
					attributes.put(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
					
					final ILaunchConfiguration sweaveConfig= getRCmdSweaveConfig(sweaveConfigName, attributes);
					if (sweaveConfig == null && config.weave.isRun()) {
						throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
								NLS.bind(Messages.ProcessingConfig_error_MissingRCmdConfig_message, sweaveConfigName), null));
					}
					toolProcess.setSweave(sweaveConfig);
					
					IFileStore wd= toolProcess.getWorkingDirectory();
					if (wd == null) {
						final FileValidator workingDirectory= REnvTab.getWorkingDirectoryValidator(sweaveConfig, true);
						final IStatus status= toolProcess.setWorkingDir(workingDirectory.getFileStore(), (IContainer) workingDirectory.getWorkspaceResource(), false);
						if (status.getSeverity() >= IStatus.ERROR && config.weave.isRun()) {
							throw new CoreException(status);
						}
						wd= workingDirectory.getFileStore();
					}
					attributes.put(RLaunching.ATTR_WORKING_DIRECTORY, wd.toURI().toString());
				}
				else if (sweaveProcessing.startsWith(TexRweaveLaunchDelegate.SWEAVE_CONSOLE)) {
					final String[] split= sweaveProcessing.split(":", 2); //$NON-NLS-1$
					toolProcess.setSweave(new VariableText(
							(split.length == 2 && split[1].length() > 0) ? replaceOldVariables(split[1]) : DEFAULT_SWEAVE_R_COMMANDS,
							TexRweaveProcessToolProcess.SWEAVE_COMMAND_VARNAMES) );
				}
				else if (toolProcess.getWorkingDirectory() == null) {
					toolProcess.setWorkingDir(null, config.getSourceFile().getParent(), true);
				}
			}
			
			// Preview config
			{	final String preview= configuration.getAttribute(PreviewTab.ATTR_VIEWER_CODE, ""); //$NON-NLS-1$
				if (config.weave.isRun()) {
					if (preview.startsWith(PREVIEW_SPECIAL)) {
						final String previewConfigName= preview.split(":", -1)[1]; //$NON-NLS-1$
						toolProcess.fPreviewConfig= Texlipse.getViewerManager().getConfiguration(previewConfigName);
						if (toolProcess.fPreviewConfig == null) {
							throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
									NLS.bind(Messages.ProcessingConfig_error_MissingViewerConfig_message, previewConfigName), null));
						}
					}
				}
			}
			
			if (config.preview.getRun() == StepConfig.RUN_EXPLICITE) {
				final IStatus status= toolProcess.run(m.newChild(30, SubMonitor.SUPPRESS_NONE));
				if (status.getSeverity() == IStatus.ERROR) {
					throw new CoreException(status);
				}
			}
			else {
				new DocProcessingToolJob(toolProcess).schedule();
			}
		}
		finally {
			m.done();
		}
	}
	
	
	private OverlayLaunchConfiguration getRCmdSweaveConfig(final String name, final Map<String, Object> attributes) throws CoreException {
		final ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType launchType= launchManager.getLaunchConfigurationType(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID); //$NON-NLS-1
		final ILaunchConfiguration[] launchConfigurations= launchManager.getLaunchConfigurations(launchType);
		
		if (name != null && name.length() > 0) {
			for (final ILaunchConfiguration config : launchConfigurations) {
				if (config.getName().equals(name)) {
					return new OverlayLaunchConfiguration(config, attributes);
				}
			}
		}
		return null;
	}
	
	private String replaceOldVariables(String text) {
		text= text.replace(TexPathConfig.SOURCEFILE_LOC_VARIABLE, "${resource_loc:${"+VARNAME_SWEAVE_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
		text= text.replace(TexPathConfig.SOURCEFILE_PATH_VARIABLE, "${"+VARNAME_SWEAVE_FILE+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		text= text.replace(TexPathConfig.TEXFILE_LOC_VARIABLE, "${resource_loc:${"+VARNAME_LATEX_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
		text= text.replace(TexPathConfig.TEXFILE_PATH_VARIABLE, "${"+VARNAME_LATEX_FILE+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		return text;
	}
	
	
}
