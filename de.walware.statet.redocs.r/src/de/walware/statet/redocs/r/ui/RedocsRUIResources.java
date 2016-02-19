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

package de.walware.statet.redocs.r.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.redocs.internal.r.Plugin;


public final class RedocsRUIResources {
	
	
	private static final String NS= "de.walware.statet.redocs.r"; //$NON-NLS-1$
	
	
	public static final String OBJ_RCHUNK_IMAGE_ID= NS + "/image/obj/rchunk"; //$NON-NLS-1$
	
	public static final String TOOL_RWEAVE_IMAGE_ID= NS + "/image/tool/rweave"; //$NON-NLS-1$
	public static final String TOOL_BUILDTEX_IMAGE_ID= NS + "/image/tool/build-tex"; //$NON-NLS-1$
	
	public static final String LOCTOOL_FILTERCHUNKS_IMAGE_ID= NS + "/image/loctool/filter-r_chunks"; //$NON-NLS-1$
	
	
	public static final RedocsRUIResources INSTANCE= new RedocsRUIResources();
	
	
	private final ImageRegistry registry;
	
	
	private RedocsRUIResources() {
		this.registry= Plugin.getDefault().getImageRegistry();
	}
	
	public ImageDescriptor getImageDescriptor(final String id) {
		return this.registry.getDescriptor(id);
	}
	
	public Image getImage(final String id) {
		return this.registry.get(id);
	}
	
}
