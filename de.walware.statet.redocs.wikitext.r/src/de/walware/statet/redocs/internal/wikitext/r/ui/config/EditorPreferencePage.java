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

package de.walware.statet.redocs.internal.wikitext.r.ui.config;

import static de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikitextRweaveEditingSettings.WIKIDOC_SPELLCHECK_ENABLED_PREF;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.docmlet.wikitext.ui.WikitextUI;

import de.walware.statet.redocs.wikitext.r.ui.sourceediting.WikitextRweaveEditingSettings;


public class EditorPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public EditorPreferencePage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() {
		return new EditorConfigurationBlock(createStatusChangedListener());
	}
	
}


class EditorConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private Button spellEnableControl;
	
	
	public EditorConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	public void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		prefs.put(WIKIDOC_SPELLCHECK_ENABLED_PREF, WikitextRweaveEditingSettings.WIKIDOC_EDITOR_NODE);
		
		setupPreferenceManager(prefs);
		
		// Controls
		{	final Link link= addLinkControl(pageComposite, NLS.bind(
					Messages.EditorOptions_WikitextAndRRef_note,
					WikitextUI.BASE_PREF_PAGE_ID ));
			final GridData gd= new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.widthHint= 300;
			link.setLayoutData(gd);
		}
		{	final Link link= addLinkControl(pageComposite, Messages.EditorOptions_SyntaxColoring_note);
			final GridData gd= new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.widthHint= 300;
			gd.horizontalIndent= LayoutUtil.defaultIndent();
			link.setLayoutData(gd);
		}
		
		// Annotation
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		{	this.spellEnableControl= new Button(pageComposite, SWT.CHECK);
			this.spellEnableControl.setText(Messages.EditorOptions_SpellChecking_Enable_label);
			this.spellEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final Link link= addLinkControl(pageComposite, Messages.EditorOptions_SpellChecking_note);
			final GridData gd= new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.widthHint= 300;
			gd.horizontalIndent= LayoutUtil.defaultIndent();
			link.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(pageComposite, true);
		
		{	final Link link= addLinkControl(pageComposite, Messages.EditorOptions_AnnotationAppearance_info);
			final GridData gd= new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.widthHint= 300;
			link.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		// Binding
		initBindings();
		updateControls();
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(
				WidgetProperties.selection().observe(this.spellEnableControl),
				createObservable(WIKIDOC_SPELLCHECK_ENABLED_PREF) );
	}
	
}
