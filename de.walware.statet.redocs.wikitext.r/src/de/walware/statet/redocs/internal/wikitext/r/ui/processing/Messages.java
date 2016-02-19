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

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Format_WikitextRweave_label;
	public static String Format_AutoWikitext_label;
	public static String Format_RweaveResult_label;
	
	public static String Weave_label;
	public static String WeaveTab_name;
	public static String WeaveTab_OpenResult_label;
	
	public static String Produce_label;
	public static String ProduceTab_name;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
