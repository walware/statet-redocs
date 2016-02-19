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

package de.walware.statet.redocs.internal.wikitext.r.textile.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.impl.Problem;
import de.walware.ecommons.text.core.ILineInformation;

import de.walware.docmlet.wikitext.core.WikitextProblemReporter;
import de.walware.docmlet.wikitext.core.model.IWikidocModelInfo;
import de.walware.docmlet.wikitext.core.model.IWikitextSourceUnit;
import de.walware.docmlet.wikitext.core.model.WikitextModel;

import de.walware.statet.redocs.internal.wikitext.r.textile.Messages;


public class TextileValidator extends WikitextProblemReporter {
	
	
	private static final Pattern BLOCK_START_PATTERN= Pattern.compile("\\A((?:bc|bq|pre|table|p)\\.\\.?+)(.)?"); //$NON-NLS-1$
	
	
	public TextileValidator() {
	}
	
	
	@Override
	public void run(final IWikitextSourceUnit su, final SourceContent content,
			final IWikidocModelInfo model,
			final IProblemRequestor requestor, final int level, final IProgressMonitor monitor) {
		
		final Matcher matcher= BLOCK_START_PATTERN.matcher(content.getText());
		try {
			final ILineInformation lines= content.getLines();
			final int numLines= lines.getNumberOfLines();
			int start= 0;
			for (int line= 0; line < numLines; line++) {
				final int end= lines.getLineEndOffset(line);
				matcher.region(start, end);
				if (matcher.find()) {
					final String followingCharacter= matcher.group(2);
					if (followingCharacter == null || !followingCharacter.equals(" ")) { //$NON-NLS-1$
						final String matched= matcher.group(1);
						requestor.acceptProblems(new Problem(WikitextModel.WIKIDOC_TYPE_ID,
								IProblem.SEVERITY_WARNING, 0,
								NLS.bind(Messages.Validation_BlockWhitespace_message, matched),
								su, line, start, matcher.end(1) ));
					}
				}
				start= end;
			}
		}
		catch (final BadLocationException e) {}
	}
	
}
