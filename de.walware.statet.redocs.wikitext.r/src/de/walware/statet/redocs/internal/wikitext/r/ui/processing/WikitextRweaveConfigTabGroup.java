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

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.walware.ecommons.debug.ui.CheckedCommonTab;

import de.walware.docmlet.base.ui.processing.DocProcessingConfigMainTab;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigPresets;
import de.walware.docmlet.base.ui.processing.PreviewTab;

import de.walware.statet.redocs.r.ui.processing.RunRConsoleSnippetOperation;


/**
 * Tab group for Sweave (LaTeX+R) output creation toolchain.
 */
public class WikitextRweaveConfigTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	private static final DocProcessingConfigPresets PRESETS;
	static {
		final DocProcessingConfigPresets presets= new DocProcessingConfigPresets(
				WikitextRweaveConfig.TYPE_ID );
		
		{	final ILaunchConfigurationWorkingCopy config= presets.add("PDF using knitr + pandoc", 1, 2);
			config.setAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"knitr::knit(" +
									"input= \"${resource_loc}\", " +
									"output= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
			config.setAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.EXT_PDF_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"knitr::pandoc(" +
									"input= \"${resource_loc}\", " +
									"format= \"${out_file_ext}\", " +
									"encoding= \"${resource_enc:${source_file_path}\")" ));
		}
		{	final ILaunchConfigurationWorkingCopy config= presets.add("PDF using RMarkdown, two-step", 1, 2);
			config.setAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"md_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
			config.setAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.EXT_PDF_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"pdf_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc:${source_file_path}}\")" ));
		}
		{	final ILaunchConfigurationWorkingCopy config= presets.add("PDF using RMarkdown, single-step", 1, 2);
			config.setAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, false);
			config.setAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"md_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
			config.setAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.EXT_PDF_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"pdf_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
		}
		{	final ILaunchConfigurationWorkingCopy config= presets.add("Auto (YAML) using RMarkdown, two-step", 1, 2);
			config.setAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"md_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
			config.setAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_YAML_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_dir= \"${container_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc:${source_file_path}}\")" ));
		}
		{	final ILaunchConfigurationWorkingCopy config= presets.add("Auto (YAML) using RMarkdown, single-step", 1, 2);
			config.setAttribute(WikitextRweaveConfig.WEAVE_ENABLED_ATTR_NAME, false);
			config.setAttribute(WikitextRweaveConfig.WEAVE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.WEAVE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_format= \"md_document\", " +
									"output_file= \"${resource_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
			config.setAttribute(WikitextRweaveConfig.PRODUCE_ENABLED_ATTR_NAME, true);
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OUTPUT_FORMAT_ATTR_NAME,
					WikitextRweaveConfig.AUTO_YAML_FORMAT_KEY );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_ID_ATTR_NAME,
					RunRConsoleSnippetOperation.ID );
			config.setAttribute(WikitextRweaveConfig.PRODUCE_OPERATION_SETTINGS_ATTR_NAME,
					Collections.singletonMap(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME,
							"rmarkdown::render(" +
									"input= \"${resource_loc}\", " +
									"output_dir= \"${container_loc:${out_file_path}}\", " +
									"encoding= \"${resource_enc}\")" ));
		}
		
		PRESETS= presets;
	}
	
	
	public WikitextRweaveConfigTabGroup() {
	}
	
	@Override
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final DocProcessingConfigMainTab mainTab= new DocProcessingConfigMainTab(PRESETS);
		final WeaveTab weaveTab= new WeaveTab(mainTab);
		final ProduceTab produceTab= new ProduceTab(mainTab, weaveTab);
		final PreviewTab previewTab= new PreviewTab(mainTab, produceTab);
		
		final ILaunchConfigurationTab[] tabs= new ILaunchConfigurationTab[] {
				mainTab,
				weaveTab,
				produceTab,
				previewTab,
				new CheckedCommonTab()
		};
		
		setTabs(tabs);
	}
	
}
