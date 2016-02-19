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

package de.walware.statet.redocs.wikitext.r.core.source;

import de.walware.ecommons.ltk.ui.templates.IWaTemplateContextTypeExtension1;

import de.walware.statet.ext.templates.StatextCodeTemplatesContextType;


public class WikitextRweaveTemplatesContextType extends StatextCodeTemplatesContextType
		implements IWaTemplateContextTypeExtension1 {
	
	
/*- Context Types -------------------------------------------------------------*/
	
	public static final String NEWDOC_CONTEXTTYPE_SUFFIX= "weave_NewDoc"; //$NON-NLS-1$
	
	public static final String WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX= "weave_Weave:DocDefault"; //$NON-NLS-1$
	
/*- Templates -----------------------------------------------------------------*/
	
	
	public WikitextRweaveTemplatesContextType(final String id) {
		super(id);
		
		init();
	}
	
	/** Instantiation extension point */
	public WikitextRweaveTemplatesContextType() {
		super();
	}
	
	
	@Override
	public void init() {
		final String id= getId();
		
		addCommonVariables();
		if (id.endsWith(NEWDOC_CONTEXTTYPE_SUFFIX)) {
			addSourceUnitGenerationVariables();
		}
		else if (id.endsWith(WEAVE_DOCDEFAULT_CONTEXTTYPE_SUFFIX)) {
			addEditorVariables();
		}
	}
	
}
