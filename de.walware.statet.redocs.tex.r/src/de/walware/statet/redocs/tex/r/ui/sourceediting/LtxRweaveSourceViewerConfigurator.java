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

package de.walware.statet.redocs.tex.r.ui.sourceediting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCodeStyleSettings;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.util.RCoreAccessWrapper;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;

import de.walware.statet.redocs.tex.r.core.source.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.tex.r.core.util.TexRweaveCoreAccessWrapper;


/**
 * Configurator for Sweave (LaTeX+R) code source viewers.
 */
public class LtxRweaveSourceViewerConfigurator extends SourceEditorViewerConfigurator {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(Arrays.asList(new String[] {
			TexCodeStyleSettings.INDENT_GROUP_ID,
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private final TexRweaveCoreAccessWrapper docCoreAccess;
	private final RCoreAccessWrapper rCoreAccess;
	
	
	public LtxRweaveSourceViewerConfigurator(
			final ITexCoreAccess texCoreAccess, final IRCoreAccess rCoreAccess,
			final LtxRweaveSourceViewerConfiguration config) {
		super(config);
		
		this.docCoreAccess= new TexRweaveCoreAccessWrapper(texCoreAccess) {
			private final TexCodeStyleSettings codeStyle= new TexCodeStyleSettings(1);
			@Override
			public TexCodeStyleSettings getTexCodeStyle() {
				return this.codeStyle;
			}
		};
		this.rCoreAccess= new RCoreAccessWrapper(rCoreAccess) {
			private final RCodeStyleSettings codeStyle= new RCodeStyleSettings(1);
			@Override
			public RCodeStyleSettings getRCodeStyle() {
				return this.codeStyle;
			}
		};
		config.setCoreAccess(this.docCoreAccess, this.rCoreAccess);
		
		this.docCoreAccess.getTexCodeStyle().load(
				this.docCoreAccess.getParent().getTexCodeStyle() );
		this.docCoreAccess.getTexCodeStyle().resetDirty();
		this.docCoreAccess.getTexCodeStyle().addPropertyChangeListener(this);
		
		this.rCoreAccess.getRCodeStyle().load(
				this.rCoreAccess.getParent().getRCodeStyle() );
		this.rCoreAccess.getRCodeStyle().resetDirty();
		this.rCoreAccess.getRCodeStyle().addPropertyChangeListener(this);
	}
	
	
	public final ITexCoreAccess getTexCoreAccess() {
		return this.docCoreAccess;
	}
	
	public final IRCoreAccess getRCoreAccess() {
		return this.rCoreAccess;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new LtxRweaveDocumentSetupParticipant();
	}
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(final ITexCoreAccess texCoreAccess, final IRCoreAccess rCoreAccess) {
		boolean changed= false;
		if (texCoreAccess != null) {
			changed|= this.docCoreAccess.setParent(texCoreAccess);
		}
		if (rCoreAccess != null) {
			changed|= this.rCoreAccess.setParent(rCoreAccess);
		}
		if (changed) {
			handleSettingsChanged(null, null);
		}
	}
	
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		
		this.docCoreAccess.getTexCodeStyle().resetDirty();
		this.rCoreAccess.getRCodeStyle().resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(TexCodeStyleSettings.INDENT_GROUP_ID)) {
			this.docCoreAccess.getTexCodeStyle().load(
					this.docCoreAccess.getParent().getTexCodeStyle() );
		}
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.rCoreAccess.getRCodeStyle().load(
					this.rCoreAccess.getParent().getRCodeStyle() );
		}
		if (groupIds.contains(TexRweaveEditingOptions.LTX_EDITOR_NODE)) {
			this.fUpdateCompleteConfig= true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			this.fUpdateInfoHovers= true;
		}
	}
	
}
