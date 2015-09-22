
package de.walware.statet.redocs.internal.wikitext.r.commonmark.ui;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.docmlet.wikitext.commonmark.ui.CommonmarkCompletionExtension;
import de.walware.docmlet.wikitext.ui.sourceediting.IMarkupCompletionExtension;


public class LanguageUIAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class[] {
		IMarkupCompletionExtension.class
	};
	
	
	private IMarkupCompletionExtension markupCompletion;
	
	
	public LanguageUIAdapterFactory() {
	}
	
	@Override
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == IMarkupCompletionExtension.class) {
			synchronized (this) {
				if (this.markupCompletion == null) {
					this.markupCompletion= new CommonmarkCompletionExtension();
				}
				return this.markupCompletion;
			}
		}
		return null;
	}
	
}
