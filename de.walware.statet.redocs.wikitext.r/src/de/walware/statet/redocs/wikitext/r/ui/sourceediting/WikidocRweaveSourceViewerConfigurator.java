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

package de.walware.statet.redocs.wikitext.r.ui.sourceediting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;

import de.walware.docmlet.wikitext.core.IWikitextCoreAccess;
import de.walware.docmlet.wikitext.core.WikitextCodeStyleSettings;
import de.walware.docmlet.wikitext.core.util.WikitextCoreAccessWrapper;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.util.RCoreAccessWrapper;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;

import de.walware.statet.redocs.wikitext.r.core.source.WikidocRweaveDocumentSetupParticipant;


/**
 * Configurator for source viewers of Wikitext-R documents.
 */
public class WikidocRweaveSourceViewerConfigurator extends SourceEditorViewerConfigurator {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(Arrays.asList(new String[] {
			WikitextCodeStyleSettings.INDENT_GROUP_ID,
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private final WikidocRweaveDocumentSetupParticipant documentSetup;
	
	private final WikitextCoreAccessWrapper docCoreAccess;
	private final RCoreAccessWrapper rCoreAccess;
	
	
	public WikidocRweaveSourceViewerConfigurator(final WikidocRweaveDocumentSetupParticipant documentSetup,
			final IWikitextCoreAccess wikitextCoreAccess, final IRCoreAccess rCoreAccess,
			final WikidocRweaveSourceViewerConfiguration config) {
		super(config);
		this.documentSetup= documentSetup;
		
		this.docCoreAccess= new WikitextCoreAccessWrapper(wikitextCoreAccess) {
			private final WikitextCodeStyleSettings codeStyle= new WikitextCodeStyleSettings(1);
			@Override
			public WikitextCodeStyleSettings getWikitextCodeStyle() {
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
		
		this.docCoreAccess.getWikitextCodeStyle().load(
				this.docCoreAccess.getParent().getWikitextCodeStyle() );
		this.docCoreAccess.getWikitextCodeStyle().resetDirty();
		this.docCoreAccess.getWikitextCodeStyle().addPropertyChangeListener(this);
		
		this.rCoreAccess.getRCodeStyle().load(
				this.rCoreAccess.getParent().getRCodeStyle() );
		this.rCoreAccess.getRCodeStyle().resetDirty();
		this.rCoreAccess.getRCodeStyle().addPropertyChangeListener(this);
	}
	
	
	@Override
	public final IDocumentSetupParticipant getDocumentSetupParticipant() {
		return this.documentSetup;
	}
	
	public final WikitextCoreAccessWrapper getWikitextCoreAccess() {
		return this.docCoreAccess;
	}
	
	public final IRCoreAccess getRCoreAccess() {
		return this.rCoreAccess;
	}
	
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(final IWikitextCoreAccess wikitextCoreAccess, final IRCoreAccess rCoreAccess) {
		boolean changed= false;
		if (wikitextCoreAccess != null) {
			changed|= this.docCoreAccess.setParent(wikitextCoreAccess);
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
		
		this.docCoreAccess.getWikitextCodeStyle().resetDirty();
		this.rCoreAccess.getRCodeStyle().resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(WikitextCodeStyleSettings.INDENT_GROUP_ID)) {
			this.docCoreAccess.getWikitextCodeStyle().load(
					this.docCoreAccess.getParent().getWikitextCodeStyle() );
		}
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.rCoreAccess.getRCodeStyle().load(
					this.rCoreAccess.getParent().getRCodeStyle() );
		}
		if (groupIds.contains(WikitextRweaveEditingSettings.WIKIDOC_EDITOR_NODE)) {
			this.fUpdateCompleteConfig= true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			this.fUpdateInfoHovers= true;
		}
	}
	
}
