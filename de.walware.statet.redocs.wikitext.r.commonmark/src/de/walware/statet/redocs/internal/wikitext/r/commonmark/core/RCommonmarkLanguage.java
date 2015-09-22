/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.commonmark.core;

import java.util.List;
import java.util.regex.Pattern;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.core.SourceContent;

import de.walware.docmlet.wikitext.commonmark.core.CommonmarkLanguage;
import de.walware.docmlet.wikitext.core.markup.IMarkupConfig;
import de.walware.docmlet.wikitext.core.markup.MarkupParser2;
import de.walware.docmlet.wikitext.core.source.RegexBlockWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.RegexInlineWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.WeaveLanguageProcessor;
import de.walware.docmlet.wikitext.core.source.extdoc.IExtdocMarkupLanguage;
import de.walware.docmlet.wikitext.core.source.extdoc.TexMathDollarsDisplayWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.extdoc.TexMathDollarsInlineWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.extdoc.TexMathSBackslashDisplayWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.extdoc.TexMathSBackslashInlineWeaveParticipant;
import de.walware.docmlet.wikitext.core.source.extdoc.YamlBlockWeaveParticipant;

import de.walware.statet.redocs.r.core.source.AbstractRChunkPartitionNodeScanner;
import de.walware.statet.redocs.wikitext.r.commonmark.core.IRCommonmarkConfig;
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;


public class RCommonmarkLanguage extends CommonmarkLanguage
		implements IExtdocMarkupLanguage, IRweaveMarkupLanguage {
	
	
	public static final String COMMONMARK_RWEAVE_LANGUAGE_NAME= "CommonMark+R"; //$NON-NLS-1$
	
	
	private static final ImList<String> INDENT_PREFIXES= ImCollections.newList(" ", "\t"); //$NON-NLS-1$
	/**
	 * <code>```{r args}</code> or <code>```{.r args}</code>, identation allowed
	 * <p>
	 * Regex: <code>\A[ \t]*+```[ \t]*+(?:\{\.?r)(.*?)}?\s*\z</code>
	 */
	private static final Pattern CHUNK_START_LINE_PATTERN= Pattern.compile("\\A[ \\t]*+```[ \\t]*+(?:\\{\\.?r)(.*?)}?\\s*\\z"); //$NON-NLS-1$
	private static final Pattern CHUNK_REF_LINE_PATTERN= Pattern.compile("\\A[ \\t]*+<<(.*?)(?:>>)?+\\p{all}*\\z"); //$NON-NLS-1$
	/**
	 * <code>```</code>, but not start pattern, identation allowed
	 * <p>
	 * Regex: <code>\A[ \t]*+```[ \t]*+(?!\{\.?r)\p{all}*\z</code>
	 */
	private static final Pattern CHUNK_END_LINE_PATTERN= Pattern.compile("\\A[ \\t]*+```[ \\t]*+(?!\\{\\.?r)\\p{all}*\\z"); //$NON-NLS-1$
	
	private static final Pattern INLINE_PATTERN= Pattern.compile("`r ([^`]+)`"); //$NON-NLS-1$
	
	
	private IRCommonmarkConfig configuredConfig;
	
	private WeaveLanguageProcessor weaveProcessor;
	
	
	public RCommonmarkLanguage() {
		this(null, 0, null);
	}
	
	public RCommonmarkLanguage(final String scope, final int mode, final IRCommonmarkConfig config) {
		super(scope, mode, config);
		
		assert (COMMONMARK_LANGUAGE_NAME.equals(getName()));
		setName(COMMONMARK_RWEAVE_LANGUAGE_NAME);
		setExtendsLanguage(COMMONMARK_LANGUAGE_NAME);
		
		setMarkupConfig(config);
	}
	
	
	@Override
	public RCommonmarkLanguage clone() {
		return (RCommonmarkLanguage) super.clone();
	}
	
	@Override
	public RCommonmarkLanguage clone(final String scope, final int mode) {
		return (RCommonmarkLanguage) super.clone(scope, mode);
	}
	
	
	@Override
	public void setMarkupConfig(final IMarkupConfig config) {
		super.setMarkupConfig(config);
	}
	
	@Override
	public IRCommonmarkConfig getMarkupConfig() {
		return (IRCommonmarkConfig) super.getMarkupConfig();
	}
	
	
//	@Override
//	protected void modeChanged(int oldMode, int newMode) {
//		if (this.weaveProcessor != null) {
//			this.configuredConfig= null;
//		}
//	}
//	
	protected void configure(final WeaveLanguageProcessor weaveProcessor,
			final IRCommonmarkConfig config) {
		weaveProcessor.addChunkParticipant(new RegexBlockWeaveParticipant(
				EMBEDDED_R, EMBEDDED_R_CHUNK_DESCR,
				CHUNK_START_LINE_PATTERN, CHUNK_END_LINE_PATTERN ) {
			@Override
			protected void appendReplacement(final StringBuilder sb,
					final String source, final int beginOffset, final int endOffset) {
				{	// Add indent
					int offset= beginOffset;
					for (; offset < endOffset; offset++) {
						final char c= source.charAt(offset);
						if (c != ' ' && c != '\t') {
							break;
						}
					}
					if (beginOffset < offset) {
						sb.append(source.substring(beginOffset, offset));
					}
				}
				super.appendReplacement(sb, source, beginOffset, endOffset);
			}
		});
		weaveProcessor.addInlineParticipants(new RegexInlineWeaveParticipant(
				EMBEDDED_R, EMBEDDED_R_INLINE_DESCR,
				INLINE_PATTERN ));
		
		if (config != null) {
			if (config.isYamlMetadataEnabled()) {
				weaveProcessor.addChunkParticipant(new YamlBlockWeaveParticipant());
			}
			if (config.isTexMathDollarsEnabled()) {
				weaveProcessor.addInlineParticipants(new TexMathDollarsDisplayWeaveParticipant(
						isModeEnabled(TEMPLATE_MODE) ));
				weaveProcessor.addInlineParticipants(new TexMathDollarsInlineWeaveParticipant(
						isModeEnabled(TEMPLATE_MODE) ));
			}
			if (config.isTexMathSBackslashEnabled()) {
				weaveProcessor.addInlineParticipants(new TexMathSBackslashDisplayWeaveParticipant());
				weaveProcessor.addInlineParticipants(new TexMathSBackslashInlineWeaveParticipant());
			}
		}
	}
	
	@Override
	public void processContent(final MarkupParser2 parser, final SourceContent content, final boolean asDocument) {
		if (parser == null) {
			throw new NullPointerException("parser"); //$NON-NLS-1$
		}
		if (content == null) {
			throw new NullPointerException("content"); //$NON-NLS-1$
		}
		if (parser.getBuilder() == null) {
			throw new NullPointerException("parser.builder"); //$NON-NLS-1$
		}
		
		if (this.weaveProcessor == null) {
			this.weaveProcessor= new WeaveLanguageProcessor();
		}
		
		final IRCommonmarkConfig config= getMarkupConfig();
		if (config != this.configuredConfig) {
			this.weaveProcessor.clearConfig();
			configure(this.weaveProcessor, config);
			this.configuredConfig= config;
		}
		
		final String markupContent= this.weaveProcessor.preprocess(content, parser.getBuilder(),
				parser.getFlags() );
		
		final MarkupParser2 baseParser= new MarkupParser2(this, this.weaveProcessor, parser.getFlags());
		doProcessContent(baseParser, new SourceContent(content.getStamp(), markupContent), asDocument);
		this.weaveProcessor.finish();
	}
	
	
	@Override
	public List<String> getIndentPrefixes() {
		return INDENT_PREFIXES;
	}
	
	@Override
	public Pattern getRChunkStartLinePattern() {
		return CHUNK_START_LINE_PATTERN;
	}
	
	@Override
	public Pattern getRChunkRefLinePattern() {
		return CHUNK_REF_LINE_PATTERN;
	}
	
	@Override
	public Pattern getRChunkEndLinePattern() {
		return CHUNK_END_LINE_PATTERN;
	}
	
	
	private RChunkPartitionNodeScanner rPartitionScanner;
	
	@Override
	public AbstractRChunkPartitionNodeScanner getRChunkPartitionScanner() {
		if (this.rPartitionScanner == null) {
			this.rPartitionScanner= new RChunkPartitionNodeScanner();
		}
		return this.rPartitionScanner;
	}
	
}
