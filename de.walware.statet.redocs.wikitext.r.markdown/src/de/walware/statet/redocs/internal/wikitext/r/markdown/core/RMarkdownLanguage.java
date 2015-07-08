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

package de.walware.statet.redocs.internal.wikitext.r.markdown.core;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.MultiplexingDocumentBuilder;
import org.eclipse.mylyn.wikitext.markdown.core.MarkdownLanguage;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.core.SourceContent;

import de.walware.docmlet.wikitext.core.WikitextProblemReporter;
import de.walware.docmlet.wikitext.core.markup.IMarkupConfig;
import de.walware.docmlet.wikitext.core.markup.IMarkupLanguageExtension2;
import de.walware.docmlet.wikitext.core.markup.MarkupParser2;
import de.walware.docmlet.wikitext.core.source.MarkupEventPrinter;
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
import de.walware.statet.redocs.wikitext.r.core.source.IRweaveMarkupLanguage;
import de.walware.statet.redocs.wikitext.r.markdown.core.IRMarkdownConfig;


public class RMarkdownLanguage extends MarkdownLanguage implements IMarkupLanguageExtension2,
		IExtdocMarkupLanguage, IRweaveMarkupLanguage {
	
	
	public static final String BASE_MARKUP_LANGUAGE_NAME= "Markdown"; //$NON-NLS-1$
	
	public static final String WEAVE_MARKUP_LANGUAGE_NAME= "Markdown+R"; //$NON-NLS-1$
	
	private static final boolean DEBUG_LOG_BASE_EVENTS= "true".equalsIgnoreCase( //$NON-NLS-1$
			Platform.getDebugOption("de.walware.statet.redocs.wikitext.r.markdown/debug/Parser/logBaseEvents")); //$NON-NLS-1$
	
	
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
	
	
	private /*final*/ String scope;
	
	private /*final*/ int mode;
	
	private IRMarkdownConfig config;
	
	private IRMarkdownConfig configuredConfig;
	
	private WeaveLanguageProcessor weaveProcessor;
	
	
	public RMarkdownLanguage() {
		this(null, 0, null);
	}
	
	public RMarkdownLanguage(final String scope, final int mode, final IRMarkdownConfig config) {
		super();
		
		assert (BASE_MARKUP_LANGUAGE_NAME.equals(getName()));
		setName(WEAVE_MARKUP_LANGUAGE_NAME);
		setExtendsLanguage(BASE_MARKUP_LANGUAGE_NAME);
		
		this.scope= scope;
		this.mode= mode;
		setMarkupConfig(config);
	}
	
	@Override
	public RMarkdownLanguage clone() {
		final RMarkdownLanguage clone= (RMarkdownLanguage) super.clone();
		clone.mode= this.mode;
		clone.config= this.config;
		return clone;
	}
	
	@Override
	public RMarkdownLanguage clone(final String scope, final int mode) {
		final RMarkdownLanguage clone= (RMarkdownLanguage) super.clone();
		clone.scope= scope;
		clone.mode= mode;
		clone.config= this.config;
		return clone;
	}
	
	
	@Override
	public String getScope() {
		return this.scope;
	}
	
	
	@Override
	public int getMode() {
		return this.mode;
	}
	
	@Override
	public boolean isModeEnabled(int modeMask) {
		return ((this.mode & modeMask) != 0);
	}
	
	
	@Override
	public void setMarkupConfig(final IMarkupConfig config) {
		if (config != null) {
			config.seal();
		}
		this.config= (IRMarkdownConfig) config;
	}
	
	@Override
	public IRMarkdownConfig getMarkupConfig() {
		return this.config;
	}
	
	
	protected void configure(final WeaveLanguageProcessor weaveProcessor,
			final IRMarkdownConfig config) {
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
		
		final IRMarkdownConfig config= getMarkupConfig();
		if (config != this.configuredConfig) {
			this.weaveProcessor.clearConfig();
			configure(this.weaveProcessor, config);
			this.configuredConfig= config;
		}
		
		setFilterGenerativeContents(parser.isDisabled(MarkupParser2.GENERATIVE_CONTENT));
		setBlocksOnly(parser.isDisabled(MarkupParser2.INLINE_MARKUP));
		
		final String markupContent= this.weaveProcessor.preprocess(content, parser.getBuilder(),
				parser.getFlags() );
		
		if (DEBUG_LOG_BASE_EVENTS) {
			final StringWriter out= new StringWriter();
			try {
				final MarkupEventPrinter printer= new MarkupEventPrinter(markupContent, this, out);
				final MarkupParser baseParser= new MarkupParser(this,
						new MultiplexingDocumentBuilder(printer, this.weaveProcessor) );
				super.processContent(baseParser, markupContent, asDocument);
				this.weaveProcessor.finish();
				System.out.println(out.toString());
			}
			catch (final Exception e) {
				System.out.println(out.toString());
				e.printStackTrace();
			}
		}
		else {
			final MarkupParser baseParser= new MarkupParser(this, this.weaveProcessor);
			super.processContent(baseParser, markupContent, asDocument);
			this.weaveProcessor.finish();
		}
	}
	
	@Override
	public void processContent(final MarkupParser parser, final String markupContent, final boolean asDocument) {
		processContent(new MarkupParser2(parser), new SourceContent(0, markupContent), asDocument);
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
	
	
	@Override
	public int hashCode() {
		return getName().hashCode() + this.mode;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		final RMarkdownLanguage other= (RMarkdownLanguage) obj;
		return (getName().equals(other.getName())
				&& this.mode == other.mode
				&& Objects.equals(this.config, other.getMarkupConfig()) );
	}
	
	
	private WikitextProblemReporter validator;
	
	@Override
	public WikitextProblemReporter getProblemReporter() {
		if (this.validator == null) {
			this.validator= new WikitextProblemReporter();
		}
		return this.validator;
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
