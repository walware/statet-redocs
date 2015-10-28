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

import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE;
import static de.walware.statet.redocs.internal.tex.r.ui.processing.TexRweaveLaunchDelegate.BUILDTEX_TYPE_RCONSOLE;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.databinding.jface.RadioGroupObservable;
import de.walware.ecommons.databinding.jface.SWTMultiEnabledObservable;
import de.walware.ecommons.debug.ui.config.LaunchConfigTabWithDbc;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderChooser;
import net.sourceforge.texlipse.builder.BuilderRegistry;

import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;

import de.walware.statet.redocs.r.ui.RedocsRUIResources;


public class TexTab extends LaunchConfigTabWithDbc {
	
	
	private static class BuildChooserObservable extends AbstractObservableValue implements SelectionListener {
		
		private final BuilderChooser fControl;
		private Integer fCurrentBuilder;
		
		public BuildChooserObservable(final BuilderChooser control) {
			this.fControl= control;
			this.fCurrentBuilder= this.fControl.getSelectedBuilder();
			this.fControl.addSelectionListener(this);
		}
		
		@Override
		public Object getValueType() {
			return Integer.class;
		}
		
		@Override
		protected Object doGetValue() {
			return this.fCurrentBuilder;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof Integer) {
				this.fCurrentBuilder= (Integer) value;
				this.fControl.setSelectedBuilder(this.fCurrentBuilder);
				return;
			}
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final int oldValue= this.fCurrentBuilder;
			this.fCurrentBuilder= this.fControl.getSelectedBuilder();
			fireValueChange(Diffs.createValueDiff(oldValue, this.fCurrentBuilder));
		}
		
	}
	
	
	public static final String NS= "de.walware.statet.r.debug/Tex/"; //$NON-NLS-1$
	
	public static final String ATTR_OPENTEX_ENABLED= NS + "OpenTex.enabled"; //$NON-NLS-1$
	/** @Deprecated replaced by {@link #ATTR_BUILDTEX_TYPE} */
	public static final String ATTR_BUILDTEX_ENABLED= NS + "BuildTex.enabled"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_TYPE= NS + "BuildTex.type"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_ECLIPSE_BUILDERID= NS + "BuildTex.builderId"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_R_COMMANDS= NS + "BuildTex.rCommands"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_FORMAT= NS + "BuildTex.format"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_OUTPUTDIR= NS + "BuildTex.outputDir"; //$NON-NLS-1$
	
	public static final int OPEN_OFF= -1;
	public static final int OPEN_ALWAYS= 0;
	
	private Button fOpenTexFileControl;
	private Button fOpenTexFileOnErrorsControl;
	
	private ResourceInputComposite fOutputDirControl;
	
	private Button fBuildTexFileDisabledControl;
	private Button fBuildTexFileEclipseControl;
	private BuilderChooser fBuildTexTypeChooser;
	private Button fBuildTexFileRControl;
	private SnippetEditor fConsoleCommandEditor;
	private Combo fOutputFormatControl;
	
	private WritableValue fOutputDirValue;
	private WritableValue fOutputFormatValue;
	private WritableValue fOpenTexEnabledValue;
	private WritableValue fOpenTexOnErrorsEnabledValue;
	private WritableValue fBuildTexTypeValue;
	private WritableValue fBuildTexBuilderIdValue;
	private WritableValue fBuildTexRCommandsValue;
	
	
	public TexTab() {
	}
	
	
	@Override
	public String getName() {
		return Messages.Processing_TexTab_label;
	}
	
	@Override
	public Image getImage() {
		return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_BUILDTEX_IMAGE_ID);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite= new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		final Group group= new Group(mainComposite, SWT.NONE);
		group.setLayout(LayoutUtil.createGroupGrid(1));
		group.setText(Messages.TexTab_label);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.fOpenTexFileControl= new Button(group, SWT.CHECK);
		this.fOpenTexFileControl.setText(Messages.TexTab_OpenTex_label);
		this.fOpenTexFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.fOpenTexFileOnErrorsControl= new Button(group, SWT.CHECK);
		this.fOpenTexFileOnErrorsControl.setText(Messages.TexTab_OpenTex_OnlyOnErrors_label);
		final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.horizontalIndent= LayoutUtil.defaultIndent();
		this.fOpenTexFileOnErrorsControl.setLayoutData(gd);
		
		LayoutUtil.addSmallFiller(group, false);
		
		createOutputOptions(group);
		
		LayoutUtil.addSmallFiller(group, false);
		
		createBuildOptions(group);
		
		initBindings();
	}
	
	private void createOutputOptions(final Group composite) {
		{	final Label label= new Label(composite, SWT.NONE);
			label.setText(Messages.TexTab_OutputDir_longlabel);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		this.fOutputDirControl= new ResourceInputComposite(composite, 
				ResourceInputComposite.STYLE_TEXT, 
				ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_SAVE, 
				Messages.TexTab_OutputDir_label) {
			
			@Override
			protected void fillMenu(final Menu menu) {
				super.fillMenu(menu);
				{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
					item.setText(Messages.Insert_SweaveDirVariable_label);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							insertText("${container_loc:${"+TexRweaveLaunchDelegate.VARNAME_SWEAVE_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
							getTextControl().setFocus();
						}
					});
				}
				{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
					item.setText(Messages.Insert_LatexDirVariable_label);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							insertText("${container_loc:${"+TexRweaveLaunchDelegate.VARNAME_LATEX_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
							getTextControl().setFocus();
						}
					});
				}
			}
		};
		this.fOutputDirControl.setShowInsertVariable(true,
				DialogUtil.DEFAULT_INTERACTIVE_FILTERS,
				ImCollections.newList(
						TexRweaveLaunchDelegate.VARIABLE_SWEAVE_FILE,
						TexRweaveLaunchDelegate.VARIABLE_LATEX_FILE ));
		this.fOutputDirControl.getValidator().setOnEmpty(IStatus.OK);
		this.fOutputDirControl.getValidator().setOnExisting(IStatus.OK);
		this.fOutputDirControl.getValidator().setOnFile(IStatus.ERROR);
		this.fOutputDirControl.getValidator().setOnLateResolve(IStatus.OK);
		this.fOutputDirControl.getValidator().setOnNotLocal(IStatus.ERROR);
		this.fOutputDirControl.getValidator().setIgnoreRelative(true);
		this.fOutputDirControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		{	final Composite lineComposite= new Composite(composite, SWT.NONE);
			lineComposite.setLayout(LayoutUtil.createCompositeGrid(2));
			lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			{	final Label label= new Label(lineComposite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.TexTab_OutputFormat_label);
			}
			{	this.fOutputFormatControl= new Combo(lineComposite, SWT.BORDER | SWT.DROP_DOWN);
				final GridData gd= new GridData(SWT.LEFT, SWT.CENTER, true, false);
				gd.widthHint= LayoutUtil.hintWidth(this.fOutputFormatControl, 3);
				this.fOutputFormatControl.setLayoutData(gd);
				this.fOutputFormatControl.setItems(new String[] { "dvi", "pdf" }); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	private void createBuildOptions(final Composite composite) {
		// Disabled
		this.fBuildTexFileDisabledControl= new Button(composite, SWT.RADIO);
		this.fBuildTexFileDisabledControl.setText(Messages.TexTab_BuildDisabled_label);
		this.fBuildTexFileDisabledControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.fBuildTexFileDisabledControl.setSelection(true);
		
		// Eclipse/TeXlipse
		this.fBuildTexFileEclipseControl= new Button(composite, SWT.RADIO);
		this.fBuildTexFileEclipseControl.setText(Messages.TexTab_BuildEclipse_label);
		this.fBuildTexFileEclipseControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		{	this.fBuildTexTypeChooser= new BuilderChooser(composite);
			final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gd.horizontalIndent= LayoutUtil.defaultIndent();
			this.fBuildTexTypeChooser.getControl().setLayoutData(gd);
		}
		
		// R Console
		{	this.fBuildTexFileRControl= new Button(composite, SWT.RADIO);
			this.fBuildTexFileRControl.setText(Messages.TexTab_BuildRConsole_label);
			this.fBuildTexFileRControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		{	final TemplateVariableProcessor templateVariableProcessor= new TemplateVariableProcessor();
			final RSourceViewerConfigurator configurator= new RTemplateSourceViewerConfigurator(
					null, templateVariableProcessor );
			this.fConsoleCommandEditor= new SnippetEditor(configurator, null, null, true) {
				@Override
				protected void fillToolMenu(final Menu menu) {
					{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
						item.setText(SharedMessages.InsertVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								final CustomizableVariableSelectionDialog dialog= new CustomizableVariableSelectionDialog(getTextControl().getShell());
								dialog.addVariableFilter(DialogUtil.EXCLUDE_JAVA_FILTER);
								dialog.addAdditional(TexRweaveLaunchDelegate.VARIABLE_SWEAVE_FILE);
								dialog.addAdditional(TexRweaveLaunchDelegate.VARIABLE_LATEX_FILE);
								dialog.addAdditional(TexRweaveLaunchDelegate.VARIABLE_OUTPUT_FILE);
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
						item.setText(Messages.Insert_LatexFileVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert("${resource_loc:${"+TexRweaveLaunchDelegate.VARNAME_LATEX_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
								getTextControl().setFocus();
							}
						});
					}
					{	final MenuItem item= new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.Insert_OutputDirVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert("${container_loc:${"+TexRweaveLaunchDelegate.VARNAME_OUTPUT_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
								getTextControl().setFocus();
							}
						});
					}
				}
			};
			this.fConsoleCommandEditor.create(composite, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
			final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint= LayoutUtil.hintHeight(this.fConsoleCommandEditor.getSourceViewer().getTextWidget(), 5);
			gd.horizontalIndent= LayoutUtil.defaultIndent();
			this.fConsoleCommandEditor.getControl().setLayoutData(gd);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		this.fOpenTexEnabledValue= new WritableValue(realm, false, Boolean.class);
		this.fOpenTexOnErrorsEnabledValue= new WritableValue(realm, false, Boolean.class);
		this.fOutputDirValue= new WritableValue(realm, null, String.class);
		this.fBuildTexTypeValue= new WritableValue(realm, 0, Integer.class);
		this.fBuildTexBuilderIdValue= new WritableValue(realm, 0, Integer.class);
		this.fBuildTexRCommandsValue= new WritableValue(realm, "", String.class); //$NON-NLS-1$
		this.fOutputFormatValue= new WritableValue(realm, "", String.class); //$NON-NLS-1$
		
		final ISWTObservableValue openObs= SWTObservables.observeSelection(this.fOpenTexFileControl);
		dbc.bindValue(openObs, this.fOpenTexEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(this.fOpenTexFileOnErrorsControl), this.fOpenTexOnErrorsEnabledValue, null, null);
		dbc.bindValue(new RadioGroupObservable(realm, new Button[] {
				this.fBuildTexFileDisabledControl, this.fBuildTexFileEclipseControl, this.fBuildTexFileRControl
		}), this.fBuildTexTypeValue, null, null);
		dbc.bindValue(new BuildChooserObservable(this.fBuildTexTypeChooser), this.fBuildTexBuilderIdValue, null, null);
		dbc.bindValue(SWTObservables.observeText(this.fConsoleCommandEditor.getTextControl(), SWT.Modify), this.fBuildTexRCommandsValue, null, null);
		dbc.bindValue(SWTObservables.observeText(this.fOutputFormatControl), this.fOutputFormatValue);
		
		this.fBuildTexBuilderIdValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				updateFormat();
			}
		});
		this.fBuildTexTypeValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final Object newValue= event.diff.getNewValue();
				final int typeId= (newValue instanceof Integer) ? ((Integer) newValue).intValue() : -1;
				switch (typeId) {
				case BUILDTEX_TYPE_ECLIPSE:
					updateFormat();
					break;
				case BUILDTEX_TYPE_RCONSOLE:
					if (TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS.equals(TexTab.this.fBuildTexRCommandsValue.getValue())) {
						TexTab.this.fOutputFormatValue.setValue(TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_FORMAT);
					}
					break;
				}
				
			}
		});
		
		// Enablement
		dbc.bindValue(SWTObservables.observeEnabled(this.fOpenTexFileOnErrorsControl), openObs, null, null);
		final Composite group= this.fBuildTexTypeChooser.getControl();
		dbc.bindValue(new SWTMultiEnabledObservable(realm, group.getChildren(), null), 
				new ComputedValue(realm, Boolean.class) {
					@Override
					protected Object calculate() {
						return (((Integer) TexTab.this.fBuildTexTypeValue.getValue()) == TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE);
					}
				}, null, null);
		dbc.bindValue(new SWTMultiEnabledObservable(realm, new Control[] { this.fConsoleCommandEditor.getControl() }, null),
				new ComputedValue(realm, Boolean.class) {
					@Override
					protected Object calculate() {
						return (((Integer) TexTab.this.fBuildTexTypeValue.getValue()) == TexRweaveLaunchDelegate.BUILDTEX_TYPE_RCONSOLE);
					}
				}, null, null);
		dbc.bindValue(new SWTMultiEnabledObservable(realm, new Control[] { this.fOutputFormatControl }, null),
				new ComputedValue(realm, Boolean.class) {
			@Override
			protected Object calculate() {
				return (((Integer) TexTab.this.fBuildTexTypeValue.getValue()) != TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE);
			}
		}, null, null);
		
		dbc.bindValue(this.fOutputDirControl.getObservable(), this.fOutputDirValue, 
				new UpdateValueStrategy().setAfterGetValidator(this.fOutputDirControl.getValidator()), null);
	}
	
	private void updateFormat() {
		final Object texBuilderId= this.fBuildTexBuilderIdValue.getValue();
		if (texBuilderId instanceof Integer) {
			final Builder builder= BuilderRegistry.get((Integer) texBuilderId);
			if (builder != null) {
				this.fOutputFormatValue.setValue(builder.getOutputFormat());
			}
		}
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_OPENTEX_ENABLED, OPEN_OFF);
		configuration.setAttribute(ATTR_BUILDTEX_TYPE, TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_TYPE);
		configuration.setAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, 0);
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, ""); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		int open= OPEN_OFF;
		try {
			open= configuration.getAttribute(ATTR_OPENTEX_ENABLED, open);
		} catch (final CoreException e) {
			logReadingError(e);
		}
		this.fOpenTexEnabledValue.setValue(open >= OPEN_ALWAYS);
		this.fOpenTexOnErrorsEnabledValue.setValue(open > OPEN_ALWAYS);
		
		int buildType= TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_TYPE;
		try {
			buildType= configuration.getAttribute(ATTR_BUILDTEX_TYPE, -2);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		if (buildType == -2) {
			try {
				buildType= configuration.getAttribute(ATTR_BUILDTEX_ENABLED, false) ?
						TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE :
						TexRweaveLaunchDelegate.BUILDTEX_TYPE_DISABLED;
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
		}
		this.fBuildTexTypeValue.setValue(buildType);
		
		int texBuilderId= 0;
		try {
			texBuilderId= configuration.getAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, texBuilderId);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		this.fBuildTexBuilderIdValue.setValue(texBuilderId);
		
		String rCommands= TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS;
		try {
			rCommands= configuration.getAttribute(ATTR_BUILDTEX_R_COMMANDS, rCommands);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		this.fBuildTexRCommandsValue.setValue(rCommands);
		
		if (buildType == BUILDTEX_TYPE_ECLIPSE) {
			updateFormat();
		}
		else {
			String format= TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_FORMAT;
			try {
				format= configuration.getAttribute(ATTR_BUILDTEX_FORMAT, format);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			this.fOutputFormatValue.setValue(format);
		}
		
		String outputDir= ""; //$NON-NLS-1$
		try {
			outputDir= configuration.getAttribute(ATTR_BUILDTEX_OUTPUTDIR, outputDir);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		this.fOutputDirValue.setValue(outputDir);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		int open= OPEN_OFF;
		if ((Boolean) this.fOpenTexEnabledValue.getValue()) {
			open= ((Boolean) this.fOpenTexOnErrorsEnabledValue.getValue()) ? IMarker.SEVERITY_ERROR : OPEN_ALWAYS;
		}
		configuration.setAttribute(ATTR_OPENTEX_ENABLED, open);
		
		final int buildType= (Integer) this.fBuildTexTypeValue.getValue();
		configuration.setAttribute(ATTR_BUILDTEX_TYPE, buildType);
		
		final Integer texBuilderId= (Integer) this.fBuildTexBuilderIdValue.getValue();
		if (texBuilderId != null
				&& (buildType == TexRweaveLaunchDelegate.BUILDTEX_TYPE_ECLIPSE || texBuilderId.intValue() != 0) ) {
			configuration.setAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, texBuilderId.intValue());
		}
		else {
			configuration.removeAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID);
		}
		
		final String rCommands= (String) this.fBuildTexRCommandsValue.getValue();
		if (buildType == TexRweaveLaunchDelegate.BUILDTEX_TYPE_RCONSOLE || !rCommands.equals(TexRweaveLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS)) {
			configuration.setAttribute(ATTR_BUILDTEX_R_COMMANDS, rCommands);
		}
		else {
			configuration.removeAttribute(ATTR_BUILDTEX_R_COMMANDS);
		}
		
		final String format= (String) this.fOutputFormatValue.getValue();
		configuration.setAttribute(ATTR_BUILDTEX_FORMAT, format);
		
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, (String) this.fOutputDirValue.getValue());
	}
	
	
	public boolean addOutputFormatListener(final IChangeListener listener) {
		if (this.fOutputFormatValue != null) {
			this.fOutputFormatValue.addChangeListener(listener);
			return true;
		}
		return false;
	}
	
	public String getOutputFormat() {
		return (String) this.fOutputFormatValue.getValue();
	}
	
}
