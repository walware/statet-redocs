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

package de.walware.statet.redocs.tex.r.ui.sourceediting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCodeStyleSettings;
import de.walware.docmlet.tex.core.commands.TexCommandSet;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;

import de.walware.statet.redocs.tex.r.core.ITexRweaveCoreAccess;
import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.tex.r.core.util.TexRweaveCoreAccess;


/**
 * Configurator for Sweave (LaTeX+R) code source viewers.
 */
public class LtxRweaveSourceViewerConfigurator extends SourceEditorViewerConfigurator
		implements ITexRweaveCoreAccess {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(Arrays.asList(new String[] {
			TexCodeStyleSettings.INDENT_GROUP_ID,
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private ITexRweaveCoreAccess sourceCoreAccess;
	
	private final TexCodeStyleSettings docCodeStyleCopy;
	private final RCodeStyleSettings rCodeStyleCopy;
	
	
	public LtxRweaveSourceViewerConfigurator(final ITexCoreAccess texCore, final IRCoreAccess rCore,
			final LtxRweaveSourceViewerConfiguration config) {
		super(config);
		this.docCodeStyleCopy= new TexCodeStyleSettings(1);
		this.rCodeStyleCopy= new RCodeStyleSettings(1);
		config.setCoreAccess(this);
		setSource(texCore, rCore);
		
		this.docCodeStyleCopy.load(this.sourceCoreAccess.getTexCodeStyle());
		this.docCodeStyleCopy.resetDirty();
		this.docCodeStyleCopy.addPropertyChangeListener(this);
		
		this.rCodeStyleCopy.load(this.sourceCoreAccess.getRCodeStyle());
		this.rCodeStyleCopy.resetDirty();
		this.rCodeStyleCopy.addPropertyChangeListener(this);
	}
	
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new LtxRweaveDocumentSetupParticipant();
	}
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(final ITexCoreAccess texCore, final IRCoreAccess rCore) {
		final ITexRweaveCoreAccess newAccess= TexRweaveCoreAccess.combine(texCore, rCore);
		if (this.sourceCoreAccess != newAccess) {
			this.sourceCoreAccess= newAccess;
			handleSettingsChanged(null, null);
		}
	}
	
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		
		this.docCodeStyleCopy.resetDirty();
		this.rCodeStyleCopy.resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(TexCodeStyleSettings.INDENT_GROUP_ID)) {
			this.docCodeStyleCopy.load(this.sourceCoreAccess.getTexCodeStyle());
		}
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.rCodeStyleCopy.load(this.sourceCoreAccess.getRCodeStyle());
		}
		if (groupIds.contains(TexRweaveEditingOptions.LTX_EDITOR_NODE)) {
			this.fUpdateCompleteConfig= true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			this.fUpdateInfoHovers= true;
		}
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.sourceCoreAccess.getPrefs();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rCodeStyleCopy;
	}
	
	@Override
	public TexCommandSet getTexCommandSet() {
		return this.sourceCoreAccess.getTexCommandSet();
	}
	
	@Override
	public TexCodeStyleSettings getTexCodeStyle() {
		return this.docCodeStyleCopy;
	}
	
}
