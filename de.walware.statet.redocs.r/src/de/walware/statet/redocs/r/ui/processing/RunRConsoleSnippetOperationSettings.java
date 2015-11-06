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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.walware.ecommons.databinding.core.util.UpdateableErrorValidator;
import de.walware.ecommons.debug.core.variables.ResourceVariables;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.variables.core.VariableText2;
import de.walware.ecommons.variables.core.VariableTextValidator;

import de.walware.docmlet.base.ui.processing.DocProcessingConfig;
import de.walware.docmlet.base.ui.processing.DocProcessingConfigStepTab;
import de.walware.docmlet.base.ui.processing.DocProcessingOperationSettings;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;

import de.walware.statet.redocs.internal.r.Messages;


public class RunRConsoleSnippetOperationSettings extends DocProcessingOperationSettings {
	
	
	private WritableValue snippetValue;
	
	private SnippetEditor snippetEditor;
	private VariableText2 snippetVariableResolver;
	
	
	public RunRConsoleSnippetOperationSettings() {
	}
	
	
	@Override
	public String getId() {
		return RunRConsoleSnippetOperation.ID;
	}
	
	@Override
	public String getLabel() {
		return Messages.ProcessingOperation_RunRConsoleSnippet_label;
	}
	
	@Override
	public String getInfo() {
		final String label= getLabel();
		final String code= (String) this.snippetValue.getValue();
		return label + ":  " + ((code != null) ? limitInfo(code) : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	@Override
	protected void init(final DocProcessingConfigStepTab tab) {
		super.init(tab);
		
		final Realm realm= getRealm();
		this.snippetValue= new WritableValue(realm);
	}
	
	@Override
	protected Composite createControl(final Composite parent) {
		final Composite composite= super.createControl(parent);
		composite.setLayout(LayoutUtil.createCompositeGrid(1));
		
		this.snippetVariableResolver= new VariableText2(getTab().getStepVariables());
		
		{	final Label label= new Label(composite, SWT.NONE);
			label.setText(Messages.ProcessingOperation_RunRConsoleSnippetSettings_RCode_label);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		{	final TemplateVariableProcessor templateVariableProcessor= new TemplateVariableProcessor();
			final RSourceViewerConfigurator configurator= new RTemplateSourceViewerConfigurator(
					RCore.WORKBENCH_ACCESS,
					templateVariableProcessor );
			final SnippetEditor editor= new SnippetEditor(configurator, null, null, true) {
				@Override
				protected void fillToolMenu(final Menu menu) {
					{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
						item.setText(SharedMessages.InsertVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								final CustomizableVariableSelectionDialog dialog= new CustomizableVariableSelectionDialog(getTextControl().getShell());
								dialog.addVariableFilter(DialogUtil.EXCLUDE_JAVA_FILTER);
								dialog.setAdditionals(RunRConsoleSnippetOperationSettings
										.this.snippetVariableResolver.getExtraVariables().values() );
								if (dialog.open() != Dialog.OK) {
									return;
								}
								final String variable= dialog.getVariableExpression();
								if (variable == null) {
									return;
								}
								getTextControl().insert(variable);
								getTextControl().setFocus();
							}
						});
					}
					{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.ProcessingOperation_Insert_InFileLocVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert(
										"${" + ResourceVariables.RESOURCE_LOC_VAR_NAME + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
								getTextControl().setFocus();
							}
						});
					}
					{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.ProcessingOperation_Insert_OutFileLocVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert(
										"${" + ResourceVariables.RESOURCE_LOC_VAR_NAME + ":" + //$NON-NLS-1$ //$NON-NLS-2$
												"${" + DocProcessingConfig.OUT_FILE_PATH_VAR_NAME + "}" + //$NON-NLS-1$ //$NON-NLS-2$
										"}" ); //$NON-NLS-1$
								getTextControl().setFocus();
							}
						});
					}
				}
			};
			editor.create(composite, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
			
			final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint= LayoutUtil.hintHeight(editor.getSourceViewer().getTextWidget(), 5);
			editor.getControl().setLayoutData(gd);
			this.snippetEditor= editor;
		}
		return composite;
	}
	
	
	@Override
	protected void addBindings(final DataBindingContext dbc) {
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(
						this.snippetEditor.getTextControl() ),
				this.snippetValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new UpdateableErrorValidator(new VariableTextValidator(
								this.snippetVariableResolver,
								Messages.ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecInvalid_message ))),
				null );
	}
	
	
	@Override
	protected void load(final Map<String, String> config) {
		final String code= config.get(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME);
		this.snippetValue.setValue((code != null) ? code : ""); //$NON-NLS-1$
	}
	
	@Override
	protected void save(final Map<String, String> config) {
		final String code= (String) this.snippetValue.getValue();
		config.put(RunRConsoleSnippetOperation.R_SNIPPET_CODE_ATTR_NAME, code);
	}
	
}
