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
	
	private static final int MODE_TEMPLATE=                 0b0_00000000_00000000_00000000_00000001;
	
	
	private /*final*/ String scope;
	
	private IRMarkdownConfig config;
	
	private int mode;
	
	private IRMarkdownConfig configuredConfig;
	
	private WeaveLanguageProcessor weaveProcessor;
	
	private boolean enableInlineWeaves;
	
	
	public RMarkdownLanguage() {
		this(null, null);
	}
	
	public RMarkdownLanguage(final String scope, final IRMarkdownConfig config) {
		super();
		
		assert (BASE_MARKUP_LANGUAGE_NAME.equals(getName()));
		setName(WEAVE_MARKUP_LANGUAGE_NAME);
		setExtendsLanguage(BASE_MARKUP_LANGUAGE_NAME);
		
		this.scope= scope;
		setMarkupConfig(config);
	}
	
	@Override
	public RMarkdownLanguage clone() {
		final RMarkdownLanguage clone= (RMarkdownLanguage) super.clone();
		clone.config= this.config;
		clone.mode= this.mode;
		return clone;
	}
	
	@Override
	public RMarkdownLanguage clone(final String scope) {
		final RMarkdownLanguage clone= clone();
		clone.scope= scope;
		return clone;
	}
	
	
	@Override
	public String getScope() {
		return this.scope;
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
	
	
	private void setMode(final int newMode) {
		if (this.mode != newMode) {
			this.mode= newMode;
			if (this.weaveProcessor != null) {
				this.configuredConfig= null;
			}
		}
	}
	
	@Override
	public void setTemplateMode(final boolean enable) {
		setMode((enable) ? (this.mode | MODE_TEMPLATE) : (this.mode & ~MODE_TEMPLATE));
	}
	
	@Override
	public boolean getTemplateMode() {
		return ((this.mode & MODE_TEMPLATE) != 0);
	}
	
	
	@Override
	public void setBlocksOnly(final boolean blocksOnly, final boolean enableInlineWeaves) {
		setBlocksOnly(blocksOnly);
		this.enableInlineWeaves= enableInlineWeaves;
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
						getTemplateMode() ));
				weaveProcessor.addInlineParticipants(new TexMathDollarsInlineWeaveParticipant(
						getTemplateMode() ));
			}
			if (config.isTexMathSBackslashEnabled()) {
				weaveProcessor.addInlineParticipants(new TexMathSBackslashDisplayWeaveParticipant());
				weaveProcessor.addInlineParticipants(new TexMathSBackslashInlineWeaveParticipant());
			}
		}
	}
	
	@Override
	public void processContent(final MarkupParser parser, final SourceContent content, final boolean asDocument) {
		if (this.weaveProcessor == null) {
			this.weaveProcessor= new WeaveLanguageProcessor();
		}
		
		final IRMarkdownConfig config= getMarkupConfig();
		if (config != this.configuredConfig) {
			this.weaveProcessor.clearConfig();
			configure(this.weaveProcessor, config);
			this.configuredConfig= config;
		}
		
		final byte mode;
		if (isBlocksOnly()) {
			if (this.enableInlineWeaves) {
				mode= WeaveLanguageProcessor.MODE_BLOCKS_AND_INLINE;
			}
			else {
				mode= WeaveLanguageProcessor.MODE_BLOCKS_ONLY;
			}
		}
		else {
			mode= WeaveLanguageProcessor.MODE_ORG;
		}
		
		final String markupContent= this.weaveProcessor.preprocess(content, parser.getBuilder(),
				mode );
		
		if (DEBUG_LOG_BASE_EVENTS) {
			final StringWriter out= new StringWriter();
			try {
				final MarkupEventPrinter printer= new MarkupEventPrinter(markupContent, this, out);
				final MarkupParser markupParser= new MarkupParser(this,
						new MultiplexingDocumentBuilder(printer, this.weaveProcessor) );
				super.processContent(markupParser, markupContent, asDocument);
				this.weaveProcessor.finish();
				System.out.println(out.toString());
			}
			catch (final Exception e) {
				System.out.println(out.toString());
				e.printStackTrace();
			}
		}
		else {
			final MarkupParser markupParser= new MarkupParser(this, this.weaveProcessor);
			super.processContent(markupParser, markupContent, asDocument);
			this.weaveProcessor.finish();
		}
	}
	
	@Override
	public void processContent(final MarkupParser parser, final String markupContent, final boolean asDocument) {
		processContent(parser, new SourceContent(0, markupContent), asDocument);
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
				&& ((this.config != null) ? this.config.equals(other.getMarkupConfig()) : null == other.getMarkupConfig()) );
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
