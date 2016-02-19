/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.ui.processing;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.text.core.ILineInformation;

import de.walware.docmlet.base.ui.DocBaseUI;
import de.walware.docmlet.base.ui.processing.DocProcessingConfig;
import de.walware.docmlet.wikitext.core.source.extdoc.YamlBlockWeaveParticipant;
import de.walware.eutils.yaml.core.ast.Collection;
import de.walware.eutils.yaml.core.ast.SourceComponent;
import de.walware.eutils.yaml.core.ast.Tuple;
import de.walware.eutils.yaml.core.ast.YamlAst.NodeType;
import de.walware.eutils.yaml.core.ast.YamlAstNode;
import de.walware.eutils.yaml.core.ast.YamlAstVisitor;
import de.walware.eutils.yaml.core.ast.YamlParser;

import de.walware.statet.redocs.wikitext.r.core.WikitextRweaveCore;


public class YamlFormatDetector {
	
	
	private static final String FAIL= "<fail>"; //$NON-NLS-1$
	
	
	private final String modelTypeId;
	
	private Matcher extValidator;
	
	
	public YamlFormatDetector(final String modelTypeId) {
		this.modelTypeId= modelTypeId;
	}
	
	
	public String detect(final IFile file, final SubMonitor m) throws CoreException {
		m.beginTask(NLS.bind("Detecting output format of ''{0}''...", file.getName()),
				10 );
		
		final ISourceUnit unit= LTK.getSourceUnitManager().getSourceUnit(this.modelTypeId,
				LTK.PERSISTENCE_CONTEXT, file, true, m.newChild(1));
		try {
			final SourceContent content= unit.getContent(m.newChild(1));
			m.worked(0);
			final IRegion yamlRegion= getYamlBlockRegion(content);
			m.worked(1);
			
			final YamlParser yamlParser= new YamlParser();
			yamlParser.setScalarText(true);
			
			final String code= content.getText().substring(
					yamlRegion.getOffset(), yamlRegion.getOffset() + yamlRegion.getLength());
			final SourceComponent block= yamlParser.parse(code, null, yamlRegion.getOffset());
			
			final String format= searchOutputInfo(block, code);
			final String ext= toExtension(format);
			
			if (!isValidExt(ext)) {
				throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
						NLS.bind("Failed to detect file extension for format ''{0}''.",
								format )));
			}
			
			return ext;
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, WikitextRweaveCore.PLUGIN_ID, 0,
					NLS.bind("Failed to detect output format specified in doc (YAML) for ''{0}''.",
							file.getName() ),
					e ));
		}
		finally {
			unit.disconnect(m);
		}
	}
	
	
	private IRegion getYamlBlockRegion(final SourceContent content)
			throws BadLocationException, CoreException {
		final ILineInformation lines= content.getLines();
		final YamlBlockWeaveParticipant part= new YamlBlockWeaveParticipant();
		part.reset(content);
		
		int lineEndOffset= lines.getLineOffset(0);
		final int numLines= lines.getNumberOfLines();
		for (int line= 0; line < numLines; line++) {
			int lineOffset= lineEndOffset;
			lineEndOffset= lines.getLineEndOffset(line);
			if (part.checkStartLine(lineOffset, lineEndOffset)) {
				while (++line < numLines) {
					lineOffset= lineEndOffset;
					lineEndOffset= lines.getLineEndOffset(line);
					if (part.checkEndLine(lineOffset, lineEndOffset)) {
						return new Region(part.getStartOffset(), lineEndOffset);
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, WikitextRweaveCore.PLUGIN_ID,
				"No YAML metadata block found."));
	}
	
	
	private String searchOutputInfo(final SourceComponent block, final String code)
			throws CoreException, InvocationTargetException {
		
		class Searcher extends YamlAstVisitor {
			
			private String format;
			private String output;
			private String outputFormat;
			
			@Override
			public void visit(final Collection node) throws InvocationTargetException {
				if (node.getYamlParent().getNodeType() == NodeType.DOC_CONTENT
						&& node.getNodeType() == NodeType.MAP) {
					node.acceptInYamlChildren(this);
				}
			}
			
			@Override
			public void visit(final Tuple node) throws InvocationTargetException {
				if (node.getKeyNode().getNodeType() == NodeType.SCALAR) {
					final String key= node.getKeyNode().getText();
					if (key != null) {
						if (key.equals("format")) { //$NON-NLS-1$
							if (this.format == null) {
								if (node.getValueNode().getNodeType() == NodeType.SCALAR) {
									this.format= node.getValueNode().getText();
								}
								if (this.format == null) {
									this.format= FAIL;
								}
							}
						}
						else if (key.equals("output")) { //$NON-NLS-1$
							if (this.output == null) {
								switch (node.getValueNode().getNodeType()) {
								case SCALAR:
									this.output= node.getValueNode().getText();
									break;
								case MAP:
									if (node.getValueNode().hasChildren()) {
										final YamlAstNode firstOutput= node.getValueNode().getChild(0);
										if (firstOutput.getNodeType() == NodeType.MAP_ENTRY) {
											checkOutputEntry((Tuple) firstOutput);
										}
									}
									break;
								default:
									break;
								}
								if (this.output == null) {
									this.output= FAIL;
								}
							}
						}
					}
				}
			}
			
			private void checkOutputEntry(final Tuple outputEntry) {
				if (outputEntry.getKeyNode().getNodeType() == NodeType.SCALAR) {
					this.output= outputEntry.getKeyNode().getText();
				}
				if (outputEntry.getValueNode().getNodeType() == NodeType.MAP) {
					final Collection outputConfig= (Collection) outputEntry.getValueNode();
					for (int i= 0; i < outputConfig.getChildCount(); i++) {
						final YamlAstNode iChild= outputConfig.getChild(i);
						if (iChild.getNodeType() == NodeType.MAP_ENTRY) {
							final Tuple outputConfigEntry= (Tuple) iChild;
							if (outputConfigEntry.getKeyNode().getNodeType() == NodeType.SCALAR
									&& outputConfigEntry.getKeyNode().getText().equals("format") //$NON-NLS-1$
									&& outputConfigEntry.getValueNode().getNodeType() == NodeType.SCALAR) { 
								this.outputFormat= outputConfigEntry.getValueNode().getText();
							}
						}
					}
				}
			}
			
		}
		
		final Searcher searcher= new Searcher();
		block.acceptInYaml(searcher);
		
		if (searcher.format != null) {
			if (searcher.format == FAIL) {
				throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
						"Unexpected data for 'format'.\nYAML code:\n" + code ));
			}
			else {
				return searcher.format;
			}
		}
		else if (searcher.output != null) {
			if (searcher.outputFormat != null && isValidExt(toExtension(searcher.outputFormat))) {
				return searcher.outputFormat;
			}
			else if (searcher.output == FAIL) {
				throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
						"Unexpected data for 'output'.\nYAML code:\n" + code ));
			}
			else {
				return searcher.output;
			}
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, DocBaseUI.PLUGIN_ID,
					"No 'format' or 'output' entry found.\nYAML code:\n" + code ));
		}
	}
	
	
	private String toExtension(String format) {
		int idx= format.lastIndexOf(':');
		if (idx >= 0) {
			format= format.substring(idx + 1);
		}
		idx= format.indexOf('_');
		if (idx >= 0) {
			format= format.substring(0, idx);
		}
		
		switch (format) {
		case "native":
			return "hs";
		case "plain":
			return "txt";
		case "markdown":
			return "md";
		case "mediawiki":
			return "mediawiki";
		case "textile":
			return "textile";
		case "asciidoc":
			return "asciidoc";
		case "html":
		case "html5":
			return "html";
		case "pdf":
		case "latex":
		case "beamer":
			return "pdf";
		case "context":
			return "tex";
		case "texinfo":
			return "texi";
		case "docbook":
			return "dbk";
		case "opendocument":
		case "odt":
			return "odt";
		case "word":
		case "docx":
			return "docx";
		case "rtf":
			return "rtf";
		case "epub":
		case "epub3":
			return "epub";
		case "slidy":
		case "slideous":
		case "dzslides":
		case "revealjs":
		case "s5":
			return "html";
		default:
			return format;
		}
	}
	
	private boolean isValidExt(final String ext) {
		if (this.extValidator == null) {
			this.extValidator= DocProcessingConfig.VALID_EXT_PATTERN.matcher(ext);
		}
		else {
			this.extValidator.reset(ext);
		}
		return this.extValidator.matches();
	}
	
}
