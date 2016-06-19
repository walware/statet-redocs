/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
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
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.config.LaunchConfigTabWithDbc;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.docmlet.base.ui.DocBaseUIResources;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.viewer.TexLaunchConfigurationDelegate;
import net.sourceforge.texlipse.viewer.TexLaunchConfigurationTab;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


public class PreviewTab extends LaunchConfigTabWithDbc {
	
	
	public static final String NS= "de.walware.statet.r.debug/DocPreview/"; //$NON-NLS-1$
	public static final String ATTR_VIEWER_CODE= NS + "Viewer.code"; //$NON-NLS-1$
	
	
	private class SelectionObservable extends AbstractObservableValue implements SelectionListener, ISelectionChangedListener, IDocumentListener, IValidator {
		
		private String encodedValue;
		private IStatus currentStatus;
		
		
		public SelectionObservable() {
			this.currentStatus= ValidationStatus.ok();
		}
		
		
		@Override
		public Object getValueType() {
			return String.class;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof String) {
				final String s= (String) value;
				this.encodedValue= s;
				if (s.startsWith(TexRweaveLaunchDelegate.PREVIEW_IDE)) {
					updateEnablement(TexRweaveLaunchDelegate.PREVIEW_IDE);
					this.currentStatus= ValidationStatus.ok();
					return;
				}
				else if (s.startsWith(TexRweaveLaunchDelegate.PREVIEW_SPECIAL)) {
					updateEnablement(TexRweaveLaunchDelegate.PREVIEW_SPECIAL);
					
					final String[] split= s.split(":", 2); //$NON-NLS-1$
					if (split.length == 2 && split[1].length() > 0) {
						final List<ViewerConfiguration> configs= PreviewTab.this.availablePreviewConfigs;
						for (final ViewerConfiguration config : configs) {
							if (config.getName().equals(split[1])) {
								PreviewTab.this.launchConfigTable.setSelection(new StructuredSelection(config));
								this.currentStatus= ValidationStatus.ok();
								return;
							}
						}
					}
					PreviewTab.this.launchConfigTable.setSelection(new StructuredSelection());
					this.currentStatus= ValidationStatus.warning(Messages.PreviewTab_LaunchConfig_error_NoConfigSelected_message);
					return;
				}
			}
			
			this.currentStatus= ValidationStatus.ok();
			updateEnablement(null);
		}
		
		@Override
		protected Object doGetValue() {
			return this.encodedValue;
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
			if (PreviewTab.this.systemSelectControl.getSelection()) {
				value= TexRweaveLaunchDelegate.PREVIEW_IDE;
				this.currentStatus= ValidationStatus.ok();
				updateEnablement(TexRweaveLaunchDelegate.PREVIEW_IDE);
			}
			else if (PreviewTab.this.launchConfigSelectControl.getSelection()) {
				final Object selectedLaunch= ((StructuredSelection) PreviewTab.this.launchConfigTable.getSelection()).getFirstElement();
				value= TexRweaveLaunchDelegate.PREVIEW_SPECIAL;
				if (selectedLaunch instanceof ViewerConfiguration) {
					value += ':'+((ViewerConfiguration) selectedLaunch).getName();
					this.currentStatus= ValidationStatus.ok();
				}
				else {
					this.currentStatus= ValidationStatus.warning(Messages.PreviewTab_LaunchConfig_error_NoConfigSelected_message);
				}
				updateEnablement(TexRweaveLaunchDelegate.PREVIEW_SPECIAL);
			}
			else {
				value= ""; //$NON-NLS-1$
				this.currentStatus= ValidationStatus.ok();
				updateEnablement(null);
			}
			if (!value.equals(this.encodedValue)) {
				final String oldValue= this.encodedValue;
				this.encodedValue= value;
				fireValueChange(Diffs.createValueDiff(oldValue, value));
			}
		}
		
		@Override
		public IStatus validate(final Object value) {
			return this.currentStatus;
		}
		
		public void updateEnablement(final String selection) {
			PreviewTab.this.disableSelectControl.setSelection(selection == null);
			PreviewTab.this.systemSelectControl.setSelection(selection == TexRweaveLaunchDelegate.PREVIEW_IDE);
			PreviewTab.this.launchConfigSelectControl.setSelection(selection == TexRweaveLaunchDelegate.PREVIEW_SPECIAL);
			
			PreviewTab.this.launchConfigTable.getControl().setEnabled(selection == TexRweaveLaunchDelegate.PREVIEW_SPECIAL);
			PreviewTab.this.launchConfigNewButton.setEnabled(selection == TexRweaveLaunchDelegate.PREVIEW_SPECIAL);
		}
		
	}
	
	
	private List<ViewerConfiguration> availablePreviewConfigs;
	private final WritableValue selectionValue;
	
	private ILaunchConfigurationListener launchConfigurationListener;
	private Button disableSelectControl;
	private Button systemSelectControl;
	private Button launchConfigSelectControl;
	private TableViewer launchConfigTable;
	private Button launchConfigNewButton;
	
	private String outputFormat;
	private TexTab texTab;
	private SelectionObservable selectionObservable;
	
	
	public PreviewTab() {
		final Realm realm= getRealm();
		this.selectionValue= new WritableValue(realm, null, String.class);
	}
	
	@Override
	public String getName() {
		return Messages.Processing_PreviewTab_label;
	}
	
	@Override
	public Image getImage() {
		return DocBaseUIResources.INSTANCE.getImage(DocBaseUIResources.TOOL_PREVIEW_IMAGE_ID);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite= new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		final Label label= new Label(mainComposite, SWT.NONE);
		label.setText(Messages.PreviewTab_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		Composite composite;
		composite= new Composite(mainComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOptions(composite);
		
		this.launchConfigurationListener= new ILaunchConfigurationListener() {
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
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this.launchConfigurationListener);
		
		initBindings();
	}
	
	private void createOptions(final Composite group) {
		GridData gd;
		group.setLayout(LayoutUtil.createCompositeGrid(2));
		
		this.disableSelectControl= new Button(group, SWT.RADIO);
		this.disableSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.disableSelectControl.setText(Messages.PreviewTab_Disable_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		this.systemSelectControl= new Button(group, SWT.RADIO);
		this.systemSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.systemSelectControl.setText(Messages.PreviewTab_SystemEditor_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		this.launchConfigSelectControl= new Button(group, SWT.RADIO);
		this.launchConfigSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		this.launchConfigSelectControl.setText(Messages.PreviewTab_LaunchConfig_label);
		
		this.launchConfigTable= new TableViewer(group, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		gd= new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd.horizontalIndent= LayoutUtil.defaultIndent();
		gd.heightHint= LayoutUtil.hintHeight(this.launchConfigTable.getTable(), 5);
		this.launchConfigTable.getControl().setLayoutData(gd);
		this.launchConfigTable.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ViewerConfiguration) {
					return ((ViewerConfiguration) element).getName();
				}
				return super.getText(element);
			}
		});
		this.launchConfigTable.setContentProvider(new ArrayContentProvider());
		this.launchConfigTable.setInput(new Object());
		
		this.launchConfigNewButton= new Button(group, SWT.PUSH);
		this.launchConfigNewButton.setText(Messages.PreviewTab_LaunchConfig_NewConfig_label);
		gd= new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.widthHint= LayoutUtil.hintWidth(this.launchConfigNewButton);
		this.launchConfigNewButton.setLayoutData(gd);
		this.launchConfigNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				createNewPreviewLaunchConfig();
			}
			
		});
	}
	
	
	private final Runnable fUpdateConfigsRunnable= new Runnable() {
		@Override
		public void run() {
			PreviewTab.this.fUpdateConfigsScheduled= false;
			if (PreviewTab.this.texTab == null) {
				final ILaunchConfigurationTab[] tabs= getLaunchConfigurationDialog().getTabs();
				for (int i= 0; i < tabs.length; i++) {
					if (tabs[i] instanceof TexTab) {
						PreviewTab.this.texTab= (TexTab) tabs[i];
					}
				}
				if (PreviewTab.this.texTab == null) {
					return;
				}
				if (!PreviewTab.this.texTab.addOutputFormatListener(new IChangeListener() {
					@Override
					public void handleChange(final ChangeEvent event) {
						updateAvailableConfigs();
					}
				})) {
					PreviewTab.this.texTab= null;
					return;
				}
			}
			PreviewTab.this.outputFormat= PreviewTab.this.texTab.getOutputFormat();
			PreviewTab.this.availablePreviewConfigs= Texlipse.getViewerManager().getAvailableConfigurations(PreviewTab.this.outputFormat);
			if (UIAccess.isOkToUse(PreviewTab.this.launchConfigTable)) {
				PreviewTab.this.launchConfigTable.setInput(PreviewTab.this.availablePreviewConfigs);
				if (PreviewTab.this.selectionObservable != null) {
					PreviewTab.this.selectionObservable.updateValue();
				}
			}
		}
	};
	private volatile boolean fUpdateConfigsScheduled;
	
	private void updateAvailableConfigs() {
		final Display display= UIAccess.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			this.fUpdateConfigsRunnable.run();
		}
		else if (!this.fUpdateConfigsScheduled) {
			this.fUpdateConfigsScheduled= true;
			display.asyncExec(this.fUpdateConfigsRunnable);
		}
	}
	
	private void createNewPreviewLaunchConfig() {
		try {
			final ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfigurationType type= launchManager.getLaunchConfigurationType(TexLaunchConfigurationDelegate.CONFIGURATION_ID);
			final String name= getLaunchConfigurationDialog().generateName(NLS.bind(Messages.PreviewTab_LaunchConfig_NewConfig_seed, this.outputFormat.toUpperCase()));
			final ILaunchConfigurationWorkingCopy config= type.newInstance(null, name);
			new EnvironmentTab().setDefaults(config);
			new TexLaunchConfigurationTab().setDefaults(config);
			
			this.selectionValue.setValue(TexRweaveLaunchDelegate.PREVIEW_SPECIAL+':'+name);
			setDirty(true);
			
			config.doSave();
		} catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.PreviewTab_LaunchConfig_NewConfig_error_Creating_message, e), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		this.selectionObservable= new SelectionObservable();
		this.disableSelectControl.addSelectionListener(this.selectionObservable);
		this.systemSelectControl.addSelectionListener(this.selectionObservable);
		this.launchConfigSelectControl.addSelectionListener(this.selectionObservable);
		this.launchConfigTable.addSelectionChangedListener(this.selectionObservable);
		this.selectionValue.setValue("init"); //$NON-NLS-1$
		
		dbc.bindValue(this.selectionObservable, this.selectionValue, new UpdateValueStrategy().setAfterGetValidator(this.selectionObservable), null);
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_VIEWER_CODE, TexRweaveLaunchDelegate.PREVIEW_IDE);
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		updateAvailableConfigs();
		
		String value= null;
		try {
			value= configuration.getAttribute(ATTR_VIEWER_CODE, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			logReadingError(e);
		}
		final Object firstConfig= this.launchConfigTable.getElementAt(0);
		this.launchConfigTable.setSelection((firstConfig != null) ? new StructuredSelection(firstConfig) : new StructuredSelection());
		this.selectionValue.setValue(value);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_VIEWER_CODE, (String) this.selectionValue.getValue());
	}
	
	@Override
	public void dispose() {
		if (this.launchConfigurationListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this.launchConfigurationListener);
			this.launchConfigurationListener= null;
		}
		super.dispose();
	}
	
}
