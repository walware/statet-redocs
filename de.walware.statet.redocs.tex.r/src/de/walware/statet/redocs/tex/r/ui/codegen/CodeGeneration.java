/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.redocs.tex.r.ui.codegen;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.templates.CodeGenerationTemplateContext;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil;
import de.walware.ecommons.ltk.ui.templates.TemplatesUtil.EvaluatedTemplate;
import de.walware.ecommons.templates.TemplateMessages;

import de.walware.statet.redocs.internal.tex.r.RedocsTexRPlugin;
import de.walware.statet.redocs.tex.r.ui.TexRweaveUI;


/**
 * Class that offers access to the code templates contained.
 */
public class CodeGeneration {
	
	
	public static ContextTypeRegistry getDocContextTypeRegistry() {
		return RedocsTexRPlugin.getInstance().getDocTemplateContextTypeRegistry();
	}
	
	public static TemplateStore getDocTemplateStore() {
		return RedocsTexRPlugin.getInstance().getDocTemplateStore();
	}
	
	
	/**
	 * Generates initial content for a new document file.
	 * 
	 * @param sourceUnit the source unit to create the source for. The unit does not need to exist.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException thrown when the evaluation of the code template fails.
	 */
	public static EvaluatedTemplate getNewDocContent(final ISourceUnit sourceUnit,
			final String lineDelimiter, final Template template) throws CoreException {
		if (template == null) {
			return null;
		}
		
		final CodeGenerationTemplateContext context= new CodeGenerationTemplateContext(
				getDocContextTypeRegistry().getContextType(template.getContextTypeId()),
				lineDelimiter );
		
		try {
			final TemplateBuffer buffer= context.evaluate(template);
			if (buffer == null) {
				return null;
			}
			return new TemplatesUtil.EvaluatedTemplate(buffer, lineDelimiter);
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, TexRweaveUI.PLUGIN_ID,
					NLS.bind(TemplateMessages.TemplateEvaluation_error_description,
							template.getDescription() ),
					e ));
		}
	}
	
}
