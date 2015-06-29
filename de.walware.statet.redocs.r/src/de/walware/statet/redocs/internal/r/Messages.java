/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.r;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String RChunkLaunch_error_message;
	
	public static String ProcessingAction_Weave_label;
	
	public static String ProcessingOperationContext_RConsole_label;
	public static String ProcessingOperationContext_RConsole_RTask_label;
	public static String ProcessingOperationContext_RConsole_RTask_Canceled_label;
	
	public static String ProcessingOperation_RunRConsoleSnippet_label;
	public static String ProcessingOperation_RunRConsoleSnippetSettings_RCode_label;
	public static String ProcessingOperation_Insert_InFileLocVariable_label;
	public static String ProcessingOperation_Insert_OutFileLocVariable_label;
	public static String ProcessingOperation_RunRConsoleSnippet_task;
	public static String ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecMissing_message;
	public static String ProcessingOperation_RunRConsoleSnippet_RCode_error_SpecInvalid_message;
	public static String ProcessingOperation_RunRConsoleSnippet_error_SetWdFailed_message;
	
	public static String ProcessingOperation_RunRCmdTool_label;
	public static String ProcessingOperation_RunRCmdTool_Wd_error_SpecInvalid_message;
	public static String ProcessingOperation_RunRCmdTool_RCmdResource_error_SpecInvalid_message;
	public static String ProcessingOperation_RunRCmdTool_RCmdOptions_error_SpecInvalid_message;

	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
