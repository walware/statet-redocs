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

package de.walware.statet.redocs.internal.r;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.redocs.r.ui.RedocsRUIResources;


/**
 * The activator class controls the plug-in life cycle
 */
public class Plugin extends AbstractUIPlugin {
	
	
	// The shared instance
	private static Plugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static Plugin getDefault() {
		return gPlugin;
	}
	
	
	private boolean started;
	
	
	/**
	 * The constructor
	 */
	public Plugin() {
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin= this;
		this.started= true;
	}
	
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			synchronized (this) {
				this.started= false;
			}
		}
		finally {
			gPlugin= null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util= new ImageRegistryUtil(this);
		
		util.register(RedocsRUIResources.OBJ_RCHUNK_IMAGE_ID, ImageRegistryUtil.T_OBJ, "r_chunk.png"); //$NON-NLS-1$
		
		util.register(RedocsRUIResources.TOOL_RWEAVE_IMAGE_ID, ImageRegistryUtil.T_TOOL, "rweave.png"); //$NON-NLS-1$
		util.register(RedocsRUIResources.TOOL_BUILDTEX_IMAGE_ID, ImageRegistryUtil.T_TOOL, "build-tex.png"); //$NON-NLS-1$
		
		util.register(RedocsRUIResources.LOCTOOL_FILTERCHUNKS_IMAGE_ID, ImageRegistryUtil.T_LOCTOOL, "filter-r_chunks.png"); //$NON-NLS-1$
	}
	
}
