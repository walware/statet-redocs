/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.redocs.r.ui.processing.RWeaveDocProcessingConfig;


public class WikitextRweaveConfig extends RWeaveDocProcessingConfig {
	
	
	public static final String TYPE_ID= "de.walware.statet.redocs.launchConfigurations.WikitextRweaveDocProcessing"; //$NON-NLS-1$
	
	
/*[ Attributes ]===============================================================*/
	
	// see RWeaveDocProcessingConfig
	
	
/*[ Formats ]==================================================================*/
	
	
	public static final String AUTO_WIKITEXT_FORMAT_KEY= Format.AUTO_TYPE + ":wikitext"; //$NON-NLS-1$
	
	
	public static final Format SOURCE_FORMAT= createSourceFormat(
			Messages.Format_WikitextRweave_label );
	
	public static final Format AUTO_WIKITEXT_FORMAT= new Format(AUTO_WIKITEXT_FORMAT_KEY,
			Messages.Format_AutoWikitext_label, "md / textile / \u2026") { //$NON-NLS-1$
		
		@Override
		public String getExt(final String inputExt) {
			if (inputExt != null && inputExt.length() > 1
					&& (inputExt.charAt(0) == 'R' || inputExt.charAt(0) == 'r') ) {
				return inputExt.substring(1);
			}
			return null;
		}
		
	};
	
	public static Format createWeaveOutputFormat(final Format format) {
		return new Format(format.getKey(),
				NLS.bind(Messages.Format_RweaveResult_label, format.getInfoLabel()),
				format.getExt() );
	}
	
	public static final ImList<Format> WEAVE_OUTPUT_FORMATS= ImCollections.newList(
			WikitextRweaveConfig.AUTO_WIKITEXT_FORMAT,
			WikitextRweaveConfig.EXT_LTX_FORMAT,
			WikitextRweaveConfig.EXT_OTHER_FORMAT );
	
	public static final ImList<Format> PRODUCE_OUTPUT_FORMATS= ImCollections.newList(
			WikitextRweaveConfig.AUTO_YAML_FORMAT,
			WikitextRweaveConfig.EXT_PDF_FORMAT,
			WikitextRweaveConfig.EXT_HTML_FORMAT,
			WikitextRweaveConfig.EXT_OTHER_FORMAT );
	
	
}
