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

package de.walware.statet.redocs.r.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;
import de.walware.jcommons.string.InternStringCache;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.IEmbeddingAstNode;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.impl.SourceUnitModelContainer;
import de.walware.ecommons.ltk.core.model.IEmbeddingReconcileItem;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.text.core.ILineInformation;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.model.IRCompositeSourceElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RProblemReporter;
import de.walware.statet.r.core.rsource.ast.FCall.Arg;
import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


public class RChunkReconciler<U extends IRSourceUnit, M extends ISourceUnitModelInfo,
		C extends SourceUnitModelContainer<U, M>> {
	
	
	private final String name;
	
	private final IRModelManager rManager;
	
	private final Matcher raChunkStartLineMatcher;
	private final Matcher raChunkRefLineMatcher;
	private final Matcher raChunkEndLineMatcher;
	
	private final RScanner raScanner= new RScanner(AstInfo.LEVEL_MODEL_DEFAULT, new InternStringCache(0x20));
	private final StringParserInput raInput= new StringParserInput(0x1000);
	
	protected final RProblemReporter rpReporter;
	
	
	public RChunkReconciler(final String name, final Pattern chunkStartLinePattern,
			final Pattern chunkRefLinePattern, final Pattern chunkEndLinePattern) {
		this.name= name;
		this.rManager= RModel.getRModelManager();
		
		this.raChunkStartLineMatcher= chunkStartLinePattern.matcher(""); //$NON-NLS-1$
		this.raChunkEndLineMatcher= chunkEndLinePattern.matcher(""); //$NON-NLS-1$
		this.raChunkRefLineMatcher= (chunkRefLinePattern != null) ? chunkRefLinePattern.matcher("") : null; //$NON-NLS-1$
		
		this.rpReporter= new RProblemReporter();
	}
	
	
	public String getName() {
		return this.name;
	}
	
	public IRModelManager getRModelManager() {
		return this.rManager;
	}
	
	public void reconcileAst(final C adapter,
			final SourceContent content, final List<? extends IEmbeddingAstNode> list,
			final IProgressMonitor monitor) {
		try {
			final String source= content.getText();
			final ILineInformation lines= content.getLines();
			
			this.raChunkStartLineMatcher.reset(source);
			if (this.raChunkRefLineMatcher != null) {
				this.raChunkRefLineMatcher.reset(source);
			}
			this.raChunkEndLineMatcher.reset(source);
			
			this.raInput.reset(source);
			
			for (final IEmbeddingAstNode embeddingNode : list) {
				if (embeddingNode.getForeignTypeId() != RModel.R_TYPE_ID) {
					continue;
				}
				
				final RChunkNode rChunk= new RChunkNode(embeddingNode);
				rChunk.startOffset= embeddingNode.getOffset();
				rChunk.stopOffset= embeddingNode.getEndOffset();
				embeddingNode.setForeignNode(rChunk);
				
				final IRegion startRegion;
				final List<IRegion> rCode= new ArrayList<>(4);
				
				switch ((embeddingNode.getEmbedDescr() & 0xf)) {
				
				case IEmbeddingAstNode.EMBED_INLINE:
					startRegion= null;
					rCode.add(new Region(embeddingNode.getOffset(), embeddingNode.getLength()));
					break;
					
				case IEmbeddingAstNode.EMBED_CHUNK: {
					int lineOffset= rChunk.startOffset;
					int line= lines.getLineOfOffset(rChunk.startOffset);
					int rCodeStartOffset;
					
					// start line
					int lineEndOffset= lines.getLineEndOffset(line);
					this.raChunkStartLineMatcher.region(lineOffset, lineEndOffset);
					if (!this.raChunkStartLineMatcher.matches()) {
						throw new IllegalStateException("R chunk does not start with start line.");
					}
					{	final int start= this.raChunkStartLineMatcher.start(1);
						final int end= this.raChunkStartLineMatcher.end(1);
						startRegion= new Region(start, end - start);
					}
					rCodeStartOffset= lineEndOffset;
					
					if (lineEndOffset < rChunk.stopOffset) {
						do {
							line++;
							lineOffset= lineEndOffset;
							lineEndOffset= lines.getLineEndOffset(line);
							
							if (this.raChunkRefLineMatcher != null) {
								this.raChunkRefLineMatcher.region(lineOffset, lineEndOffset);
								if (this.raChunkRefLineMatcher.matches()) {
									if (rCodeStartOffset < lineOffset) {
										rCode.add(new Region(rCodeStartOffset, lineOffset - rCodeStartOffset));
									}
									rCodeStartOffset= lineEndOffset;
								}
							}
						} while (lineEndOffset < rChunk.stopOffset);
						
						if (rChunk.stopOffset != lineEndOffset) {
							throw new IllegalStateException("R chunk does not end at line end.");
						}
						
						this.raChunkEndLineMatcher.region(lineOffset, lineEndOffset);
						if (this.raChunkEndLineMatcher.matches()) {
							if (rCodeStartOffset < lineOffset) {
								rCode.add(new Region(rCodeStartOffset, lineOffset - rCodeStartOffset));
							}
							rCodeStartOffset= lineEndOffset;
						}
						else if (rCodeStartOffset < lineOffset) {
							rCode.add(new Region(rCodeStartOffset, lineEndOffset - rCodeStartOffset));
						}
					}
					break; }
				
				default:
					throw new UnsupportedOperationException("embedType= " + embeddingNode.getEmbedDescr());
				}
				
				if (startRegion != null) {
					rChunk.weaveArgs= this.raScanner.scanFCallArgs(
							this.raInput.init(startRegion.getOffset(), startRegion.getOffset() + startRegion.getLength()),
							true );
				}
				final SourceComponent[] rCodeNodes= new SourceComponent[rCode.size()];
				for (int j= 0; j < rCodeNodes.length; j++) {
					final IRegion region= rCode.get(j);
					rCodeNodes[j]= this.raScanner.scanSourceRange(
							this.raInput.init(region.getOffset(), region.getOffset() + region.getLength()),
							rChunk, true );
				}
				rChunk.rSources= ImCollections.newList(rCodeNodes);
			}
		}
		catch (final BadLocationException | IllegalStateException e) {
			throw new RuntimeException(e);
		}
	}
	
	public IRModelInfo reconcileModel(final C adapter,
			final SourceContent content, final M mainModel,
			final List<? extends IEmbeddingReconcileItem<?, ?>> list,
			final int level, final IProgressMonitor monitor) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		int chunkCount= 0;
		final List<RedocsRChunkElement> chunkElements= new ArrayList<>();
		final List<SourceComponent> inlineNodes= new ArrayList<>();
		
		for (final IEmbeddingReconcileItem<?, ?> item : list) {
			if (item.getForeignTypeId() != RModel.R_TYPE_ID) {
				continue;
			}
			
			final IEmbeddingAstNode embeddingNode= item.getAstNode();
			final RChunkNode rChunk= (RChunkNode) embeddingNode.getForeignNode();
			if (rChunk == null) {
				continue;
			}
			
			final ImList<SourceComponent> rSources= rChunk.getRCodeChildren();
			switch ((embeddingNode.getEmbedDescr() & 0xf)) {
			
			case IEmbeddingAstNode.EMBED_INLINE:
				if (rSources.size() == 1) {
					inlineNodes.add(rSources.get(0));
				}
				break;
				
			case IEmbeddingAstNode.EMBED_CHUNK: {
				chunkCount++;
				
				RElementName name= null;
				IRegion nameRegion= null;
				if (rChunk.getWeaveArgsChild() != null) {
					final Arg arg= getLabelArg(rChunk.getWeaveArgsChild());
					if (arg != null && arg.hasValue()) {
						final RAstNode labelNode= arg.getValueChild();
						final String label;
						if (arg.getValueChild().getNodeType() == NodeType.SYMBOL) {
							label= labelNode.getText();
						}
						else {
							label= new String(content.getText().substring(
								labelNode.getOffset(), labelNode.getEndOffset() ));
						}
						name= RElementName.create(RElementName.MAIN_OTHER, label);
						nameRegion= labelNode;
					}
				}
				if (name == null) {
					name= RElementName.create(RElementName.MAIN_OTHER, "#"+Integer.toString(chunkCount)); //$NON-NLS-1$
					nameRegion= new Region(embeddingNode.getOffset() + 2, 0);
				}
				final RedocsRChunkElement element= new RedocsRChunkElement(item.getModelRefElement(),
						rChunk, name, nameRegion );
				item.setModelTypeElement(element);
				chunkElements.add(element);
				break; }
			}
		}
		
		if (chunkElements.isEmpty() && inlineNodes.isEmpty()) {
			return null;
		}
		
		final IRModelInfo modelInfo= getRModelManager().reconcile(adapter.getSourceUnit(),
				mainModel, chunkElements, inlineNodes, level, monitor );
		mainModel.addAttachment(modelInfo);
		return modelInfo;
	}
	
	private Arg getLabelArg(final Args weaveArgs) {
		if (!weaveArgs.hasChildren()) {
			return null;
		}
		for (int i= 0; i < weaveArgs.getChildCount(); i++) {
			final Arg arg= weaveArgs.getChild(i);
			if ((arg.hasName()) ?
					(arg.getNameChild().getNodeType() == NodeType.SYMBOL
							&& "label".equals(arg.getNameChild().getText()) ) : //$NON-NLS-1$
					(i == 0) ) {
				return arg;
			}
		}
		return null;
	}
	
	public void reportEmbeddedProblems(final C adapter,
			final SourceContent content, final M mainModel,
			final IProblemRequestor problemRequestor, final int level,
			final IProgressMonitor monitor) {
		final IRModelInfo rModel= RModel.getRModelInfo(mainModel);
		if (rModel == null) {
			return;
		}
		final IRSourceUnit su= adapter.getSourceUnit();
		final IRLangSourceElement element= rModel.getSourceElement();
		if (element instanceof IRCompositeSourceElement) {
			final List<? extends IRLangSourceElement> elements= ((IRCompositeSourceElement) element)
					.getCompositeElements();
			for (final IRLangSourceElement rChunk : elements) {
				final IAstNode rChunkNode= (IAstNode) rChunk.getAdapter(IAstNode.class);
				this.rpReporter.run(su, content, rChunkNode, problemRequestor);
			}
		}
	}
	
}
