/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.config;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String EditorOptions_WikitextAndRRef_note;
	public static String EditorOptions_SyntaxColoring_note;
	
	public static String EditorOptions_SpellChecking_Enable_label;
	public static String EditorOptions_SpellChecking_note;
	
	public static String EditorOptions_AnnotationAppearance_info;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
