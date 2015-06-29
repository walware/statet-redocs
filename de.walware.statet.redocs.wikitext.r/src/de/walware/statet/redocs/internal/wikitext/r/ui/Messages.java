/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String NewDocWizard_Task_label;
	public static String NewDocWizard_error_ApplyTemplate_message;
	
	
	static {
		initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
