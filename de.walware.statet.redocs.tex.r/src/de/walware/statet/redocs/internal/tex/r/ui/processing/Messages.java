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

package de.walware.statet.redocs.internal.tex.r.ui.processing;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	public static String Weave_label;
	public static String Processing_SweaveTab_label;
	public static String RweaveTab_label;
	public static String RweaveTab_Skip_label;
	public static String RweaveTab_InConsole_label;
	public static String RweaveTab_InConsole_InserVar_label;
	public static String RweaveTab_RCmd_label;
	public static String RweaveTab_RCmd_NewConfig_label;
	public static String RweaveTab_RCmd_NewConfig_error_Creating_message;
	public static String RweaveTab_RCmd_NewConfig_seed;
	public static String RweaveTab_RCmd_error_NoConfigSelected_message;
	
	public static String Insert_SweaveDirVariable_label;
	public static String Insert_LatexDirVariable_label;
	public static String Insert_LatexFileVariable_label;
	public static String Insert_OutputDirVariable_label;
	
	public static String Produce_label;
	public static String Processing_TexTab_label;
	public static String TexTab_label;
	public static String TexTab_OpenTex_label;
	public static String TexTab_OpenTex_OnlyOnErrors_label;
	public static String TexTab_OutputDir_label;
	public static String TexTab_OutputDir_longlabel;
	public static String TexTab_OutputFormat_label;
	public static String TexTab_BuildDisabled_label;
	public static String TexTab_BuildEclipse_label;
	public static String TexTab_BuildRConsole_label;
	
	public static String Processing_PreviewTab_label;
	public static String PreviewTab_label;
	public static String PreviewTab_Disable_label;
	public static String PreviewTab_SystemEditor_label;
	public static String PreviewTab_LaunchConfig_label;
	public static String PreviewTab_LaunchConfig_NewConfig_label;
	public static String PreviewTab_LaunchConfig_NewConfig_seed;
	public static String PreviewTab_LaunchConfig_NewConfig_error_Creating_message;
	public static String PreviewTab_LaunchConfig_error_NoConfigSelected_message;
	
	public static String ProcessingConfig_error_MissingRCmdConfig_message;
	public static String ProcessingConfig_error_MissingViewerConfig_message;
	
	public static String RweaveTexProcessing_info_Canceled_message;
	public static String RweaveTexProcessing_Sweave_InConsole_label;
	public static String RweaveTexProcessing_Sweave_error_ResourceVariable_message;
	public static String RweaveTexProcessing_Sweave_Task_label;
	public static String RweaveTexProcessing_Sweave_Task_info_Canceled_message;
	public static String RweaveTexProcessing_Sweave_RCmd_label;
	public static String RweaveTexProcessing_Sweave_RCmd_error_Found_message;
	public static String RweaveTexProcessing_Tex_label;
	public static String RweaveTexProcessing_Tex_error_BuilderNotConfigured_message;
	public static String RweaveTexProcessing_Tex_error_OutputDir_message;
	public static String RweaveTexProcessing_Tex_error_MustBeInWorkspace_message;
	public static String RweaveTexProcessing_Tex_error_NotFound_message;
	public static String RweaveTexProcessing_Tex_error_ResourceVariable_message;
	public static String RweaveTexProcessing_Output_error_NotFound_message;
	public static String RweaveTexProcessing_Output_info_SkipBecauseTex_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
