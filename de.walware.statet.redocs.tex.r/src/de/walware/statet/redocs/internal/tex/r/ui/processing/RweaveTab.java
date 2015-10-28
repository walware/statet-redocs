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

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.config.LaunchConfigTabWithDbc;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.r.ui.RedocsRUIResources;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


public class RweaveTab extends LaunchConfigTabWithDbc {
	
	
	public static final String NS= "de.walware.statet.r.debug/Rweave/"; //$NON-NLS-1$
	public static final String ATTR_SWEAVE_FOLDER= NS + "sweave.folder"; //$NON-NLS-1$
	public static final String ATTR_SWEAVE_ID= NS + "SweaveProcessing"; //$NON-NLS-1$
	
	
	private class SelectionObservable extends AbstractObservableValue implements SelectionListener, ISelectionChangedListener, IDocumentListener, IValidator {
		
		private String fEncodedValue;
		private IStatus fCurrentStatus;
		
		
		public SelectionObservable() {
			this.fCurrentStatus= ValidationStatus.ok();
		}
		
		
		@Override
		public Object getValueType() {
			return String.class;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof String) {
				final String s= (String) value;
				this.fEncodedValue= s;
				if (s.startsWith(TexRweaveLaunchDelegate.SWEAVE_CONSOLE)) {
					updateEnablement(TexRweaveLaunchDelegate.SWEAVE_CONSOLE);
					
					final String[] split= s.split(":", 2); //$NON-NLS-1$
					final String command= (split.length == 2 && split[1].length() > 0) ? split[1] : TexRweaveLaunchDelegate.DEFAULT_SWEAVE_R_COMMANDS;
					if (!command.equals(RweaveTab.this.fConsoleCommandEditor.getDocument().get())) {
						RweaveTab.this.fConsoleCommandEditor.getDocument().set(command);
					}
					
					this.fCurrentStatus= ValidationStatus.ok();
					return;
				}
				else if (s.startsWith(TexRweaveLaunchDelegate.SWEAVE_LAUNCH)) {
					updateEnablement(TexRweaveLaunchDelegate.SWEAVE_LAUNCH);
					
					final String[] split= s.split(":", 2); //$NON-NLS-1$
					if (split.length == 2 && split[1].length() > 0) {
						final ILaunchConfiguration[] configs= RweaveTab.this.fAvailableConfigs;
						for (final ILaunchConfiguration config : configs) {
							if (config.getName().equals(split[1])) {
								RweaveTab.this.fCmdLaunchTable.setSelection(new StructuredSelection(config));
								this.fCurrentStatus= ValidationStatus.ok();
								return;
							}
						}
					}
					RweaveTab.this.fCmdLaunchTable.setSelection(new StructuredSelection());
					this.fCurrentStatus= ValidationStatus.warning(Messages.RweaveTab_RCmd_error_NoConfigSelected_message);
					return;
				}
			}
			
			this.fCurrentStatus= ValidationStatus.ok();
			updateEnablement(null);
		}
		
		@Override
		protected Object doGetValue() {
			return this.fEncodedValue;
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		@Override
		public void documentAboutToBeChanged(final DocumentEvent event) {
		}
		
		@Override
		public void documentChanged(final DocumentEvent event) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		private void updateValue() {
			String value;
			if (RweaveTab.this.fConsoleSelectControl.getSelection()) {
				value= TexRweaveLaunchDelegate.SWEAVE_CONSOLE + ':' + RweaveTab.this.fConsoleCommandEditor.getDocument().get();
				this.fCurrentStatus= ValidationStatus.ok();
				updateEnablement(TexRweaveLaunchDelegate.SWEAVE_CONSOLE);
			}
			else if (RweaveTab.this.fCmdLaunchSelectControl.getSelection()) {
				final Object selectedLaunch= ((StructuredSelection) RweaveTab.this.fCmdLaunchTable.getSelection()).getFirstElement();
				value= TexRweaveLaunchDelegate.SWEAVE_LAUNCH;
				if (selectedLaunch instanceof ILaunchConfiguration) {
					value += ':'+((ILaunchConfiguration) selectedLaunch).getName();
					this.fCurrentStatus= ValidationStatus.ok();
				}
				else {
					this.fCurrentStatus= ValidationStatus.warning(Messages.RweaveTab_RCmd_error_NoConfigSelected_message);
				}
				updateEnablement(TexRweaveLaunchDelegate.SWEAVE_LAUNCH);
			}
			else {
				value= ""; //$NON-NLS-1$
				this.fCurrentStatus= ValidationStatus.ok();
				updateEnablement(null);
			}
			if (!value.equals(this.fEncodedValue)) {
				final String oldValue= this.fEncodedValue;
				this.fEncodedValue= value;
				fireValueChange(Diffs.createValueDiff(oldValue, value));
			}
		}
		
		@Override
		public IStatus validate(final Object value) {
			return this.fCurrentStatus;
		}
		
		public void updateEnablement(final String selection) {
			RweaveTab.this.fSkipSelectControl.setSelection(selection == null);
			RweaveTab.this.fConsoleSelectControl.setSelection(selection == TexRweaveLaunchDelegate.SWEAVE_CONSOLE);
			RweaveTab.this.fCmdLaunchSelectControl.setSelection(selection == TexRweaveLaunchDelegate.SWEAVE_LAUNCH);
			
			RweaveTab.this.fConsoleCommandEditor.getTextControl().setEnabled(selection == TexRweaveLaunchDelegate.SWEAVE_CONSOLE);
			RweaveTab.this.fConsoleCommandInsertButton.setEnabled(selection == TexRweaveLaunchDelegate.SWEAVE_CONSOLE);
			RweaveTab.this.fCmdLaunchTable.getControl().setEnabled(selection == TexRweaveLaunchDelegate.SWEAVE_LAUNCH);
			RweaveTab.this.fCmdLaunchNewButton.setEnabled(selection == TexRweaveLaunchDelegate.SWEAVE_LAUNCH);
		}
		
	}
	
	
	private ResourceInputComposite fDirControl;
	private WritableValue fDirValue;
	
	private ILaunchConfiguration[] fAvailableConfigs;
	private WritableValue fSelectionValue;
	
	private ILaunchConfigurationListener fLaunchConfigurationListener;
	private Button fSkipSelectControl;
	private Button fConsoleSelectControl;
	private SnippetEditor fConsoleCommandEditor;
	
	private Button fConsoleCommandInsertButton;
	private Button fCmdLaunchSelectControl;
	private TableViewer fCmdLaunchTable;
	private Button fCmdLaunchNewButton;
	
	
	@Override
	public String getName() {
		return Messages.Processing_SweaveTab_label;
	}
	
	@Override
	public Image getImage() {
		return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_RWEAVE_IMAGE_ID);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite= new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		{	final Label label= new Label(mainComposite, SWT.NONE);
			label.setText(Messages.RweaveTab_label);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		{	final Composite composite= createDirectoryGroup(mainComposite);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		{	final Composite composite= createSweaveCommandGroup(mainComposite);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		this.fLaunchConfigurationListener= new ILaunchConfigurationListener() {
			@Override
			public void launchConfigurationAdded(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
			@Override
			public void launchConfigurationChanged(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
			@Override
			public void launchConfigurationRemoved(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
		};
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this.fLaunchConfigurationListener);
		updateAvailableConfigs();
		
		initBindings();
	}
	
	private Composite createDirectoryGroup(final Composite parent) {
		final Group group= new Group(parent, SWT.NONE);
		group.setText("Working &Folder (path in workspace):");
		group.setLayout(LayoutUtil.createGroupGrid(1));
		
		this.fDirControl= new ResourceInputComposite(group, ResourceInputComposite.STYLE_TEXT,
				ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN | ResourceInputComposite.MODE_WS_ONLY,
				"Working Directory");
		this.fDirControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.fDirControl.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS,
				ImCollections.newList(TexRweaveLaunchDelegate.VARIABLE_SWEAVE_FILE) );
		
		return group;
	}
	
	private Composite createSweaveCommandGroup(final Composite parent) {
		final Composite group= new Composite(parent, SWT.NONE);
		group.setLayout(LayoutUtil.createCompositeGrid(2));
		GridData gd;
		
		this.fSkipSelectControl= new Button(group, SWT.RADIO);
		this.fSkipSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.fSkipSelectControl.setText(Messages.RweaveTab_Skip_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		this.fConsoleSelectControl= new Button(group, SWT.RADIO);
		this.fConsoleSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.fConsoleSelectControl.setText(Messages.RweaveTab_InConsole_label);
		
		final TemplateVariableProcessor templateVariableProcessor= new TemplateVariableProcessor();
		final RSourceViewerConfigurator configurator= new RTemplateSourceViewerConfigurator(
				null, templateVariableProcessor );
		this.fConsoleCommandEditor= new SnippetEditor(configurator);
		this.fConsoleCommandEditor.create(group, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
		gd= new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.heightHint= LayoutUtil.hintHeight(this.fConsoleCommandEditor.getSourceViewer().getTextWidget(), 5);
		gd.horizontalIndent= LayoutUtil.defaultIndent();
		this.fConsoleCommandEditor.getControl().setLayoutData(gd);
		
		this.fConsoleCommandInsertButton= new Button(group, SWT.PUSH);
		this.fConsoleCommandInsertButton.setText(Messages.RweaveTab_InConsole_InserVar_label);
		this.fConsoleCommandInsertButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		this.fConsoleCommandInsertButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final CustomizableVariableSelectionDialog dialog= new CustomizableVariableSelectionDialog(getShell());
				dialog.setFilters(DialogUtil.DEFAULT_INTERACTIVE_FILTERS);
				dialog.addAdditional(TexRweaveLaunchDelegate.VARIABLE_SWEAVE_FILE);
				dialog.addAdditional(TexRweaveLaunchDelegate.VARIABLE_LATEX_FILE);
				if (dialog.open() != Dialog.OK) {
					return;
				}
				final String variable= dialog.getVariableExpression();
				if (variable == null) {
					return;
				}
				RweaveTab.this.fConsoleCommandEditor.getSourceViewer().getTextWidget().insert(variable);
				RweaveTab.this.fConsoleCommandEditor.getControl().setFocus();
			}
		});
		
		LayoutUtil.addSmallFiller(group, false);
		
		this.fCmdLaunchSelectControl= new Button(group, SWT.RADIO);
		this.fCmdLaunchSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.fCmdLaunchSelectControl.setText(Messages.RweaveTab_RCmd_label);
		
		this.fCmdLaunchTable= new TableViewer(group, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		gd= new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.horizontalIndent= LayoutUtil.defaultIndent();
		gd.heightHint= LayoutUtil.hintHeight(this.fCmdLaunchTable.getTable(), 5);
		this.fCmdLaunchTable.getControl().setLayoutData(gd);
		this.fCmdLaunchTable.setLabelProvider(DebugUITools.newDebugModelPresentation());
		this.fCmdLaunchTable.setContentProvider(new ArrayContentProvider());
		this.fCmdLaunchTable.setInput(new Object());
		
		this.fCmdLaunchNewButton= new Button(group, SWT.PUSH);
		this.fCmdLaunchNewButton.setText(Messages.RweaveTab_RCmd_NewConfig_label);
		gd= new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.widthHint= LayoutUtil.hintWidth(this.fCmdLaunchNewButton);
		this.fCmdLaunchNewButton.setLayoutData(gd);
		this.fCmdLaunchNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				createNewRCmdSweaveLaunchConfig();
			}
			
		});
		return group;
	}
	
	
	private void updateAvailableConfigs() {
		try {
			final ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfigurationType type= launchManager.getLaunchConfigurationType(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID);
			final ILaunchConfiguration[] allConfigs= launchManager.getLaunchConfigurations(type);
			final ArrayList<Object> filteredConfigs= new ArrayList<>(allConfigs.length+1);
			for (final ILaunchConfiguration config : allConfigs) {
				if (config.getAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, "").equals("CMD Sweave")) { //$NON-NLS-1$ //$NON-NLS-2$
					filteredConfigs.add(config);
				}
			}
			this.fAvailableConfigs= filteredConfigs.toArray(new ILaunchConfiguration[filteredConfigs.size()]);
			if (UIAccess.isOkToUse(this.fCmdLaunchTable)) {
				this.fCmdLaunchTable.setInput(this.fAvailableConfigs);
			}
		}
		catch (final CoreException e) {
			RedocsTexRPlugin.logError(ICommonStatusConstants.LAUNCHCONFIG_ERROR, "An error occurred while updating R CMD list.", e);
		}
	}
	
	private void createNewRCmdSweaveLaunchConfig() {
		try {
			final String name= getLaunchConfigurationDialog().generateName(Messages.RweaveTab_RCmd_NewConfig_seed);
			final ILaunchConfigurationWorkingCopy config= RCmdLaunching.createNewRCmdConfig(name, "CMD Sweave"); //$NON-NLS-1$
			
			this.fSelectionValue.setValue(TexRweaveLaunchDelegate.SWEAVE_LAUNCH+':'+name);
			setDirty(true);
			
			config.doSave();
		} catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.RweaveTab_RCmd_NewConfig_error_Creating_message, e), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		this.fDirValue= new WritableValue(realm, String.class);
		this.fSelectionValue= new WritableValue(realm, String.class);
		
		final FileValidator validator= this.fDirControl.getValidator();
		validator.setOnEmpty(IStatus.OK);
		dbc.bindValue(this.fDirControl.getObservable(), this.fDirValue,
				new UpdateValueStrategy().setAfterGetValidator(validator),
				null );
		
		final SelectionObservable obs= new SelectionObservable();
		this.fSkipSelectControl.addSelectionListener(obs);
		this.fConsoleSelectControl.addSelectionListener(obs);
		this.fCmdLaunchSelectControl.addSelectionListener(obs);
		this.fCmdLaunchTable.addSelectionChangedListener(obs);
		this.fConsoleCommandEditor.getDocument().addDocumentListener(obs);
		
		this.fSelectionValue.setValue("init"); //$NON-NLS-1$
		
		dbc.bindValue(obs, this.fSelectionValue,
				new UpdateValueStrategy().setAfterGetValidator(obs),
				null );
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_SWEAVE_FOLDER, "${container_path:${source_file_path}}"); //$NON-NLS-1$
		configuration.setAttribute(ATTR_SWEAVE_ID, TexRweaveLaunchDelegate.SWEAVE_CONSOLE+':');
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		{	String dir= ""; //$NON-NLS-1$
			try {
				dir= configuration.getAttribute(ATTR_SWEAVE_FOLDER, ""); //$NON-NLS-1$
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			this.fDirValue.setValue(dir);
		}
		
		this.fConsoleCommandEditor.getDocument().set(TexRweaveLaunchDelegate.DEFAULT_SWEAVE_R_COMMANDS);
		final Object firstConfig= this.fCmdLaunchTable.getElementAt(0);
		this.fCmdLaunchTable.setSelection((firstConfig != null) ? new StructuredSelection(firstConfig) : new StructuredSelection());
		{	String value= ""; //$NON-NLS-1$
			try {
				value= configuration.getAttribute(ATTR_SWEAVE_ID, ""); //$NON-NLS-1$
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			this.fSelectionValue.setValue(value);
		}
		this.fConsoleCommandEditor.reset();
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_SWEAVE_ID, (String) this.fSelectionValue.getValue());
		configuration.setAttribute(ATTR_SWEAVE_FOLDER, (String) this.fDirValue.getValue());
	}
	
	@Override
	public void dispose() {
		if (this.fLaunchConfigurationListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this.fLaunchConfigurationListener);
			this.fLaunchConfigurationListener= null;
		}
		super.dispose();
	}
	
}
