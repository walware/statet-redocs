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

package de.walware.statet.redocs.r.core.model;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * R Chunk
 */
public class RChunkNode implements IAstNode {
	
	
	private static final ImList<Object> NO_ATTACHMENT= ImCollections.emptyList();
	
	
	private final IAstNode parent;
	// start/stop control chunk
	Args weaveArgs;
	ImList<SourceComponent> rSources;
	
	int startOffset;
	int stopOffset;
	
	private volatile ImList<Object> attachments= NO_ATTACHMENT;
	
	
	RChunkNode(final IAstNode parent) {
		this.parent= parent;
	}
	
	
	@Override
	public int getStatusCode() {
		return 0;
	}
	
	
	@Override
	public IAstNode getParent() {
		return this.parent;
	}
	
	@Override
	public boolean hasChildren() {
		return true;
	}
	
	public Args getWeaveArgsChild() {
		return this.weaveArgs;
	}
	
	public ImList<SourceComponent> getRCodeChildren() {
		return this.rSources;
	}
	
	@Override
	public int getChildCount() {
		if (this.weaveArgs != null) {
			return this.rSources.size() + 1;
		}
		else {
			return this.rSources.size();
		}
	}
	
	@Override
	public IAstNode getChild(final int index) {
		if (this.weaveArgs != null) {
			if (index == 0) {
				return this.weaveArgs;
			}
			return this.rSources.get(index - 1);
		}
		else {
			return this.rSources.get(index);
		}
	}
	
	@Override
	public int getChildIndex(final IAstNode element) {
		if (this.weaveArgs != null) {
			if (this.weaveArgs == element) {
				return 0;
			}
			for (int i= 0; i < this.rSources.size(); i++) {
				if (this.rSources.get(i) == element) {
					return i + 1;
				}
			}
			return -1;
		}
		else {
			for (int i= 0; i < this.rSources.size(); i++) {
				if (this.rSources.get(i) == element) {
					return i;
				}
			}
			return -1;
		}
	}
	
	
	@Override
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		if (this.weaveArgs != null) {
			visitor.visit(this.weaveArgs);
		}
		for (final SourceComponent node : this.rSources) {
			visitor.visit(node);
		}
	}
	
	
	@Override
	public int getOffset() {
		return this.startOffset;
	}
	
	@Override
	public int getEndOffset() {
		return this.stopOffset;
	}
	
	@Override
	public int getLength() {
		return this.stopOffset - this.startOffset;
	}
	
	
	@Override
	public synchronized void addAttachment(final Object data) {
		this.attachments= ImCollections.addElement(this.attachments, data);
	}
	
	@Override
	public synchronized void removeAttachment(final Object data) {
		this.attachments= ImCollections.removeElement(this.attachments, data);
	}
	
	@Override
	public ImList<Object> getAttachments() {
		return this.attachments;
	}
	
}
