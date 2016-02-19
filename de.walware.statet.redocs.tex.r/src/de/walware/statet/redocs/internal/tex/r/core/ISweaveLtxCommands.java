/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.tex.r.core;

import de.walware.jcommons.collections.ImCollections;

import de.walware.docmlet.tex.core.commands.Argument;
import de.walware.docmlet.tex.core.commands.TexCommand;
import de.walware.docmlet.tex.core.commands.TexEmbedCommand;
import de.walware.docmlet.tex.core.parser.ICustomScanner;

import de.walware.statet.r.core.model.RModel;

import de.walware.statet.redocs.internal.tex.r.Messages;


public interface ISweaveLtxCommands {
	
	
	TexCommand SWEAVE_SweaveOpts_COMMANDS= new TexCommand(0,
			"SweaveOpts", false, ImCollections.newList( //$NON-NLS-1$
					new Argument("options", Argument.REQUIRED, Argument.NONE) //$NON-NLS-1$
			), Messages.LtxCommand_SweaveOpts_description);
	
	TexEmbedCommand SWEAVE_Sexpr_COMMANDS= new TexEmbedCommand(0, RModel.R_TYPE_ID,
			"Sexpr", false, ImCollections.newList( //$NON-NLS-1$
					new Argument("expression", Argument.REQUIRED, Argument.EMBEDDED) //$NON-NLS-1$
			), Messages.LtxCommand_Sexpr_description) {
		
		@Override
		public ICustomScanner getArgumentScanner(final int argIdx) {
			return CurlyExpandEmbeddedScanner.INSTANCE;
		}
		
	};
	
}
