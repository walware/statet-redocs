
package de.walware.statet.redocs.internal.wikitext.r.textile.ui;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.docmlet.wikitext.ui.sourceediting.IMarkupConfigUIAdapter;


public class UIAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS= new Class[] {
		IMarkupConfigUIAdapter.class
	};
	
	
	private IMarkupConfigUIAdapter markupConfigUI;
	
	
	public UIAdapterFactory() {
	}
	
	@Override
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == IMarkupConfigUIAdapter.class) {
			synchronized (this) {
//				if (this.markupConfigUI == null) {
//					this.markupConfigUI= new RTextileConfigUI();
//				}
				return this.markupConfigUI;
			}
		}
		return null;
	}
	
}
