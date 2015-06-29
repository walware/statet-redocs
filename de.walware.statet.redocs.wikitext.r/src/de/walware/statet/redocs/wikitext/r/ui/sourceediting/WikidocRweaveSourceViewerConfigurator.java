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

package de.walware.statet.redocs.wikitext.r.ui.sourceediting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;
import de.walware.docmlet.wikitext.core.WikitextCodeStyleSettings;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;

import de.walware.statet.redocs.wikitext.r.core.IWikitextRweaveCoreAccess;
import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentSetupParticipant;
import de.walware.statet.redocs.wikitext.r.core.util.WikitextRweaveCoreAccess;


/**
 * Configurator for source viewers of Wikitext-R documents.
 */
public class WikidocRweaveSourceViewerConfigurator extends SourceEditorViewerConfigurator
		implements IWikitextRweaveCoreAccess {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(Arrays.asList(new String[] {
			WikitextCodeStyleSettings.INDENT_GROUP_ID,
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private final WikidocRweaveDocumentSetupParticipant documentSetup;
	
	private IWikitextRweaveCoreAccess fSourceCoreAccess;
	
	private final WikitextCodeStyleSettings docCodeStyleCopy;
	private final RCodeStyleSettings rCodeStyleCopy;
	
	
	public WikidocRweaveSourceViewerConfigurator(final WikidocRweaveDocumentSetupParticipant documentSetup,
			final IWikitextCoreAccess wikitextCore, final IRCoreAccess rCore,
			final WikidocRweaveSourceViewerConfiguration config) {
		super(config);
		this.documentSetup= documentSetup;
		
		this.docCodeStyleCopy= new WikitextCodeStyleSettings(1);
		this.rCodeStyleCopy= new RCodeStyleSettings(1);
		config.setCoreAccess(this);
		setSource(wikitextCore, rCore);
		
		this.docCodeStyleCopy.load(this.fSourceCoreAccess.getWikitextCodeStyle());
		this.docCodeStyleCopy.resetDirty();
		this.docCodeStyleCopy.addPropertyChangeListener(this);
		
		this.rCodeStyleCopy.load(this.fSourceCoreAccess.getRCodeStyle());
		this.rCodeStyleCopy.resetDirty();
		this.rCodeStyleCopy.addPropertyChangeListener(this);
	}
	
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return this.documentSetup;
	}
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(final IWikitextCoreAccess wikitextCore, final IRCoreAccess rCore) {
		final IWikitextRweaveCoreAccess newAccess= WikitextRweaveCoreAccess.combine(wikitextCore, rCore);
		if (this.fSourceCoreAccess != newAccess) {
			this.fSourceCoreAccess= newAccess;
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
		
		if (groupIds.contains(WikitextCodeStyleSettings.INDENT_GROUP_ID)) {
			this.docCodeStyleCopy.load(this.fSourceCoreAccess.getWikitextCodeStyle());
		}
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.rCodeStyleCopy.load(this.fSourceCoreAccess.getRCodeStyle());
		}
		if (groupIds.contains(WikitextRweaveEditingSettings.WIKIDOC_EDITOR_NODE)) {
			this.fUpdateCompleteConfig= true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			this.fUpdateInfoHovers= true;
		}
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.fSourceCoreAccess.getPrefs();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rCodeStyleCopy;
	}
	
	@Override
	public WikitextCodeStyleSettings getWikitextCodeStyle() {
		return this.docCodeStyleCopy;
	}
	
}
