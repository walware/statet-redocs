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

package de.walware.statet.redocs.r.ui.processing;

import de.walware.docmlet.base.ui.processing.DocProcessingConfig;


public abstract class RWeaveDocProcessingConfig extends DocProcessingConfig {
	
	
/*[ Attributes ]===============================================================*/
	
//	public static final String WORKING_DIRECTORY_ATTR_NAME= DocProcessingConfig.WORKING_DIRECTORY_ATTR_NAME;
	
	public static final String WEAVE_ATTR_QUALIFIER= "de.walware.statet.redocs/weave"; //$NON-NLS-1$
	public static final String WEAVE_ENABLED_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_ENABLED_ATTR_KEY;
	public static final String WEAVE_OUTPUT_FORMAT_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OUTPUT_FORMAT_ATTR_KEY;
	public static final String WEAVE_OUTPUT_FILE_PATH_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OUTPUT_FILE_PATH_ATTR_KEY;
	public static final String WEAVE_OPERATION_ID_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_ID_ATTR_KEY;
	public static final String WEAVE_OPERATION_SETTINGS_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_SETTINGS_ATTR_KEY;
	public static final String WEAVE_OPEN_RESULT_ATTR_NAME= WEAVE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_POST_OPEN_OUTPUT_ENABLED_ATTR_KEY;
	
	public static final String PRODUCE_ATTR_QUALIFIER= "de.walware.statet.redocs/produce"; //$NON-NLS-1$
	public static final String PRODUCE_ENABLED_ATTR_NAME= PRODUCE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_ENABLED_ATTR_KEY;
	public static final String PRODUCE_OUTPUT_FORMAT_ATTR_NAME= PRODUCE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OUTPUT_FORMAT_ATTR_KEY;
	public static final String PRODUCE_OUTPUT_FILE_PATH_ATTR_NAME= PRODUCE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OUTPUT_FILE_PATH_ATTR_KEY;
	public static final String PRODUCE_OPERATION_ID_ATTR_NAME= PRODUCE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_ID_ATTR_KEY;
	public static final String PRODUCE_OPERATION_SETTINGS_ATTR_NAME= PRODUCE_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_SETTINGS_ATTR_KEY;
	
	public static final String PREVIEW_ATTR_QUALIFIER= "de.walware.docmlet.base/preview"; //$NON-NLS-1$
	public static final String PREVIEW_ENABLED_ATTR_NAME= PREVIEW_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_ENABLED_ATTR_KEY;
	public static final String PREVIEW_OPERATION_ID_ATTR_NAME= PREVIEW_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_ID_ATTR_KEY;
	public static final String PREVIEW_OPERATION_SETTINGS_ATTR_NAME= PREVIEW_ATTR_QUALIFIER + '/' +
			DocProcessingConfig.STEP_OPERATION_SETTINGS_ATTR_KEY;
	
}
