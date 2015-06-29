/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.r.ui.processing;

import org.eclipse.swt.graphics.Image;

import de.walware.docmlet.base.ui.processing.DocProcessingManager;

import de.walware.statet.redocs.internal.r.Messages;
import de.walware.statet.redocs.r.ui.RedocsRUIResources;


public class RweaveDocProcessingManager extends DocProcessingManager {
	
	
	public RweaveDocProcessingManager() {
	}
	
	
	@Override
	protected Image getActionImage(final byte bits) {
		switch (bits) {
		case WEAVE_BIT:
			return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_RWEAVE_IMAGE_ID);
		case PRODUCE_OUTPUT_BIT:
			return RedocsRUIResources.INSTANCE.getImage(RedocsRUIResources.TOOL_BUILDTEX_IMAGE_ID);
		default:
			return super.getActionImage(bits);
		}
	}
	
	@Override
	protected String getActionLabel(final byte bits) {
		switch (bits) {
		case WEAVE_BIT:
			return Messages.ProcessingAction_Weave_label;
		default:
			return super.getActionLabel(bits);
		}
	}
	
}
