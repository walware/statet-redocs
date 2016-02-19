/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.docmlet.wikitext.commonmark.ui.AbstractCommonmarkConfigDialog;
import de.walware.docmlet.wikitext.core.markup.IMarkupConfig;
import de.walware.docmlet.wikitext.ui.config.IMarkupConfigUIAdapter;

import de.walware.statet.redocs.internal.wikitext.r.commonmark.core.RCommonmarkConfig;


public class RCommonmarkConfigUI implements IMarkupConfigUIAdapter {
	
	
	static class RCommonmarkConfigDialog extends AbstractCommonmarkConfigDialog<RCommonmarkConfig> {
		
		
		public RCommonmarkConfigDialog(final Shell parent, final String contextLabel,
				final boolean isContextEnabeld, final RCommonmarkConfig config) {
			super(parent, contextLabel, isContextEnabeld, config);
		}
		
		
		@Override
		protected Composite createExtensionGroup(final Composite parent) {
			final Composite composite= super.createExtensionGroup(parent);
			
			addProperty(composite, RCommonmarkConfig.YAML_METADATA_ENABLED_PROP);
			addProperty(composite, RCommonmarkConfig.TEX_MATH_DOLLARS_ENABLED_PROP);
			addProperty(composite, RCommonmarkConfig.TEX_MATH_SBACKSLASH_ENABLED_PROP);
			LayoutUtil.addSmallFiller(composite, false);
			addProperty(composite, RCommonmarkConfig.HEADER_INTERRUPT_PARAGRAPH_DISABLED_PROP);
			addProperty(composite, RCommonmarkConfig.BLOCKQUOTE_INTERRUPT_PARAGRAPH_DISABLED_PROP);
			LayoutUtil.addSmallFiller(composite, false);
			addProperty(composite, RCommonmarkConfig.STRIKEOUT_DTILDE_ENABLED_PROP);
			addProperty(composite, RCommonmarkConfig.SUPERSCRIPT_SCIRCUMFLEX_ENABLED_PROP);
			addProperty(composite, RCommonmarkConfig.SUBSCRIPT_STILDE_ENABLED_PROP);
			
			return composite;
		}
		
	}
	
	
	@Override
	public boolean edit(final String contextLabel, final AtomicBoolean isContextEnabled, final IMarkupConfig config, final Shell parent) {
		final RCommonmarkConfigDialog dialog= new RCommonmarkConfigDialog(parent, contextLabel,
				(isContextEnabled != null && isContextEnabled.get()), (RCommonmarkConfig) config );
		if (dialog.open() == Dialog.OK) {
			if (isContextEnabled != null) {
				isContextEnabled.set(dialog.isCustomEnabled());
			}
			return true;
		}
		return false;
	}
	
}
