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

package de.walware.statet.redocs.internal.tex.r.core;

import de.walware.ecommons.text.core.input.TextParserInput;

import de.walware.docmlet.tex.core.parser.ICustomScanner;
import de.walware.docmlet.tex.core.parser.LtxLexer;


/**
 * Scanner supporting expand variables {{ ... }}.
 */
public class CurlyExpandEmbeddedScanner implements ICustomScanner {
	
	
	protected static final ICustomScanner INSTANCE= new CurlyExpandEmbeddedScanner();
	
	
	protected CurlyExpandEmbeddedScanner() {
	}
	
	
	@Override
	public byte consume(final LtxLexer lexer) {
		final TextParserInput input= lexer.getInput();
		lexer.consume(true);
		
		int offset= 0;
		int expandVar= 0;
		while (true) {
			final int c= input.get(offset++);
			if (c < 0x20) {
				input.consume(offset - 1);
				lexer.consume(true);
				return 0;
			}
			switch (c) {
			case '{':
				if (input.get(offset) == '{') {
					offset++;
					expandVar++;
					continue;
				}
				continue;
			case '}':
				if (expandVar > 0 && input.get(offset) == '}') {
					offset++;
					expandVar--;
					continue;
				}
				input.consume(offset);
				lexer.consume(true);
				return LtxLexer.CURLY_BRACKET_CLOSE;
			default:
				continue;
			}
		}
	}
	
}
