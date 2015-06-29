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

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;

import de.walware.docmlet.base.ui.processing.DocProcessingOperation;
import de.walware.docmlet.base.ui.processing.DocProcessingToolConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingToolConfig.StepConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingToolJob;
import de.walware.docmlet.base.ui.processing.DocProcessingToolLaunchDelegate;
import de.walware.docmlet.base.ui.processing.DocProcessingToolProcess;
import de.walware.docmlet.base.ui.processing.DocProcessingUI;
import de.walware.docmlet.base.ui.processing.operations.OpenUsingEclipseOperation;
import de.walware.docmlet.base.ui.processing.operations.RunExternalProgramOperation;

import de.walware.statet.redocs.r.ui.processing.RunRCmdToolOperation;
import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperation;
import de.walware.statet.redocs.wikitext.r.core.model.WikitextRweaveModel;
import de.walware.statet.redocs.wikitext.r.ui.WikitextRweaveUI;


public class WikitextRweaveLaunchDelegate extends DocProcessingToolLaunchDelegate {
	
	
	static class Config extends DocProcessingToolConfig {
		
		
		public final StepConfig weave= new ProcessingStepConfig(this,
				WikitextRweaveConfig.WEAVE_ATTR_QUALIFIER, Messages.Weave_label );
		public final StepConfig produce= new ProcessingStepConfig(this,
				WikitextRweaveConfig.PRODUCE_ATTR_QUALIFIER, Messages.Produce_label );
		public final StepConfig preview= new PreviewStepConfig(this);
		
		
		protected Config() {
			setSteps(this.weave, this.produce, this.preview);
		}
		
		
		protected boolean initRun(final ILaunchConfiguration configuration) throws CoreException {
			byte runWeave= 0;
			byte runProduce= 0;
			byte runPreview= 0;
			
			final Set<String> steps= configuration.getAttribute(
					DocProcessingUI.RUN_STEPS_ATTR_NAME, Collections.<String>emptySet() );
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
					throw new CoreException(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID,
							"Unsupported steps configurations: " + steps.toString() ));
				}
			}
			
			this.weave.initRun(runWeave, configuration);
			this.produce.initRun(runProduce, configuration);
			this.preview.initRun(runPreview, configuration);
			
			return (this.weave.isRun() || this.produce.isRun() || this.preview.isRun());
		}
		
		
		@Override
		protected String getOutputExt(final StepConfig stepConfig, final String formatKey,
				final SubMonitor m) throws CoreException {
			if (formatKey.equals(WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY)) {
				final IFile inputFile= stepConfig.getInputFile();
				final String ext= WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT.getExt(
						inputFile.getFileExtension() );
				if (ext == null) {
					throw new CoreException(new Status(IStatus.ERROR, WikitextRweaveUI.PLUGIN_ID,
							NLS.bind("Failed to resolve Wikitext filename extension for ''{0}''.",
									inputFile.getName() )));
				}
				return ext;
			}
			if (formatKey.equals(WikitextRweaveConfig.AUTO_YAML_FORMAT_KEY)) {
				final YamlFormatDetector detector= new YamlFormatDetector(WikitextRweaveModel.WIKIDOC_R_MODEL_TYPE_ID);
				return detector.detect(getSourceFile(), m);
			}
			return super.getOutputExt(stepConfig, formatKey, m);
		}
		
		@Override
		protected DocProcessingOperation createStepOperation(final String id) {
			if (id.equals(RunRConsoleSnippetOperation.ID)) {
				return new RunRConsoleSnippetOperation();
			}
			if (id.equals(RunRCmdToolOperation.ID)) {
				return new RunRCmdToolOperation();
			}
			if (id.equals(RunExternalProgramOperation.ID)) {
				return new RunExternalProgramOperation();
			}
			if (id.equals(OpenUsingEclipseOperation.ID)) {
				return new OpenUsingEclipseOperation();
			}
			return null;
		}
		
	}
	
	
	public WikitextRweaveLaunchDelegate() {
	}
	
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		final Config config= new Config();
		if (!config.initRun(configuration)) {
			return; // nothing to do
		}
		
		final SubMonitor m= SubMonitor.convert(monitor, "Configuring Document Processing...", 20 +
				((config.preview.getRun() == StepConfig.RUN_EXPLICITE) ? 30 : 0) );
		
		{	final SubMonitor mInit= m.newChild(10);
			final boolean weaveRequired= (config.weave.isRun() || config.weave.isEnabled());
			final boolean produceRequired= (config.produce.isRun() || config.preview.isRun());
			final boolean previewRequired= (config.preview.isRun());
			mInit.setWorkRemaining(2 + 1 +
					((weaveRequired) ? 2 : 0) +
					((produceRequired) ? 2 : 0) +
					((previewRequired) ? 1 : 0) );
			
			config.initSourceFile(configuration, mInit.newChild(2));
			config.initWorkingDirectory(configuration, mInit.newChild(1));
			
			if (weaveRequired) {
				final IFile inputFile= config.getSourceFile();
				config.weave.initIOFiles(inputFile, configuration, mInit.newChild(2));
			}
			if (produceRequired) {
				final IFile inputFile= (config.weave.isEnabled()) ?
						config.weave.getOutputFile() : config.getSourceFile();
				config.produce.initIOFiles(inputFile, configuration, mInit.newChild(2));
			}
			if (previewRequired) {
				final IFile inputFile= config.produce.getOutputFile();
				config.preview.initIOFiles(inputFile, configuration, mInit.newChild(1));
			}
		}
		
		{	final SubMonitor mOps= m.newChild(10);
			mOps.setWorkRemaining(
					((config.weave.isRun()) ? 3 : 0) +
					((config.produce.isRun()) ? 3 : 0) +
					((config.preview.isRun()) ? 2 : 0) );
			
			if (config.weave.isRun()) {
				config.weave.initOperation(configuration, mOps.newChild(2));
				config.weave.initPost(configuration, mOps.newChild(1));
			}
			if (config.produce.isRun()) {
				config.produce.initOperation(configuration, mOps.newChild(2));
				config.produce.initPost(configuration, mOps.newChild(1));
			}
			if (config.preview.isRun()) {
				config.preview.initOperation(configuration, mOps.newChild(2));
			}
		}
		
		final DocProcessingToolProcess toolProcess= new DocProcessingToolProcess(launch, config);
		
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
	
}
