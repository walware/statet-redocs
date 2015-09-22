/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     erka - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.ecommons.ltk.ui.templates.NewDocTemplateGenerateWizardPage;

import de.walware.statet.redocs.internal.wikitext.r.commonmark.CommonmarkRweavePlugin;
import de.walware.statet.redocs.internal.wikitext.r.commonmark.Messages;
import de.walware.statet.redocs.wikitext.r.ui.codegen.NewWikidocRweaveDocCreationWizard;
import de.walware.statet.redocs.wikitext.r.ui.codegen.NewWikidocRweaveDocCreationWizardPage;


public class NewDocCreationWizard extends NewWikidocRweaveDocCreationWizard {
	
	
	public NewDocCreationWizard() {
		super(CommonmarkRweavePlugin.DOC_CONTENT_TYPE);
		setWindowTitle(Messages.NewDocWizard_title);
	}
	
	
	@Override
	protected NewWikidocRweaveDocCreationWizardPage createFirstPage(final IStructuredSelection selection) {
		final NewWikidocRweaveDocCreationWizardPage page= new NewWikidocRweaveDocCreationWizardPage(
				selection, ".Rmd" ); //$NON-NLS-1$
		page.setTitle(Messages.NewDocWizardPage_title);
		page.setDescription(Messages.NewDocWizardPage_description);
		return page;
	}
	
	@Override
	protected NewDocTemplateGenerateWizardPage createTemplatePage() {
		final NewDocTemplateGenerateWizardPage page= new NewDocTemplateGenerateWizardPage(
				new NewDocTemplateCategoryConfiguration(),
				CommonmarkRweavePlugin.NEWDOC_TEMPLATE_CATEGORY_ID );
		return page;
	}
	
}
