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

package de.walware.statet.redocs.internal.wikitext.r.ui.editors;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.source.IAnnotationModel;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.SourceAnnotationModel;
import de.walware.ecommons.ltk.ui.sourceediting.SourceDocumentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.SourceProblemAnnotation;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.preferences.core.IPreferenceAccess;

import de.walware.docmlet.wikitext.core.model.WikitextModel;
import de.walware.docmlet.wikitext.ui.editors.WikitextEditorBuild;
import de.walware.eutils.yaml.core.model.YamlModel;
import de.walware.eutils.yaml.ui.editors.YamlEditorBuild;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.editors.REditorBuild;

import de.walware.statet.redocs.wikitext.r.core.model.IWikidocRweaveSourceUnit;
import de.walware.statet.redocs.wikitext.r.core.model.WikitextRweaveModel;


public class WikidocRweaveDocumentProvider extends SourceDocumentProvider<IWikidocRweaveSourceUnit>
		implements IDisposable {
	
	
	private class ThisAnnotationModel extends SourceAnnotationModel {
		
		
		private class ThisProblemRequestor extends ProblemRequestor {
			
			
			private final boolean handleTemporaryRProblems;
			
			
			public ThisProblemRequestor() {
				this.handleTemporaryRProblems= isHandlingTemporaryRProblems();
			}
			
			
			@Override
			public void acceptProblems(final IProblem problem) {
				if (problem.getCategoryId() == RModel.R_TYPE_ID) {
					if (this.handleTemporaryRProblems) {
						this.fReportedProblems.add(problem);
					}
				}
				else {
					if (this.fHandleTemporaryProblems) {
						this.fReportedProblems.add(problem);
					}
				}
			}
			
			@Override
			public void acceptProblems(final String modelTypeId, final List<IProblem> problems) {
				if (modelTypeId == RModel.R_TYPE_ID) {
					if (this.handleTemporaryRProblems) {
						this.fReportedProblems.addAll(problems);
					}
				}
				else {
					if (this.fHandleTemporaryProblems) {
						this.fReportedProblems.addAll(problems);
					}
				}
			}
			
		}
		
		
		public ThisAnnotationModel(final IResource resource) {
			super(resource);
		}
		
		@Override
		protected boolean isHandlingTemporaryProblems() {
			return WikidocRweaveDocumentProvider.this.handleTemporaryDocProblems;
		}
		
		protected boolean isHandlingTemporaryRProblems() {
			return WikidocRweaveDocumentProvider.this.handleTemporaryRProblems;
		}
		
		@Override
		protected IProblemRequestor doCreateProblemRequestor() {
			return new ThisProblemRequestor();
		}
		
		@Override
		protected SourceProblemAnnotation createAnnotation(final IProblem problem) {
			if (problem.getCategoryId() == RModel.R_TYPE_ID) {
				switch (problem.getSeverity()) {
				case IProblem.SEVERITY_ERROR:
					return new SourceProblemAnnotation(REditorBuild.ERROR_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.ERROR_CONFIG );
				case IProblem.SEVERITY_WARNING:
					return new SourceProblemAnnotation(REditorBuild.WARNING_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.WARNING_CONFIG );
				default:
					return new SourceProblemAnnotation(REditorBuild.INFO_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.INFO_CONFIG );
				}
			}
			else if (problem.getCategoryId() == WikitextModel.WIKIDOC_TYPE_ID) {
				switch (problem.getSeverity()) {
				case IProblem.SEVERITY_ERROR:
					return new SourceProblemAnnotation(WikitextEditorBuild.ERROR_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.ERROR_CONFIG );
				case IProblem.SEVERITY_WARNING:
					return new SourceProblemAnnotation(WikitextEditorBuild.WARNING_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.WARNING_CONFIG );
				default:
					return new SourceProblemAnnotation(WikitextEditorBuild.INFO_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.INFO_CONFIG );
				}
			}
			else if (problem.getCategoryId() == YamlModel.YAML_TYPE_ID) {
				switch (problem.getSeverity()) {
				case IProblem.SEVERITY_ERROR:
					return new SourceProblemAnnotation(YamlEditorBuild.ERROR_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.ERROR_CONFIG );
				case IProblem.SEVERITY_WARNING:
					return new SourceProblemAnnotation(YamlEditorBuild.WARNING_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.WARNING_CONFIG );
				default:
					return new SourceProblemAnnotation(YamlEditorBuild.INFO_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.INFO_CONFIG );
				}
			}
			return null;
		}
		
	}
	
	
	private SettingsChangeNotifier.ChangeListener editorPrefListener;
	
	private boolean handleTemporaryDocProblems;
	private boolean handleTemporaryRProblems;
	
	
	public WikidocRweaveDocumentProvider() {
		super(WikitextRweaveModel.WIKIDOC_R_MODEL_TYPE_ID, null);
		
		this.editorPrefListener= new SettingsChangeNotifier.ChangeListener() {
			@Override
			public void settingsChanged(final Set<String> groupIds) {
				if (groupIds.contains(REditorBuild.GROUP_ID)
						|| groupIds.contains(WikitextEditorBuild.GROUP_ID)) {
					updateEditorPrefs();
				}
			}
		};
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this.editorPrefListener);
		final IPreferenceAccess access= PreferencesUtil.getInstancePrefs();
		this.handleTemporaryDocProblems= access.getPreferenceValue(WikitextEditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		this.handleTemporaryRProblems= access.getPreferenceValue(REditorBuild.PROBLEMCHECKING_ENABLED_PREF);
	}
	
	@Override
	public void dispose() {
		if (this.editorPrefListener != null) {
			PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this.editorPrefListener);
			this.editorPrefListener= null;
		}
	}
	
	private void updateEditorPrefs() {
		final IPreferenceAccess access= PreferencesUtil.getInstancePrefs();
		final boolean newHandleTemporaryDocProblems= access.getPreferenceValue(WikitextEditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		final boolean newHandleTemporaryRProblems= access.getPreferenceValue(REditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		if (this.handleTemporaryDocProblems != newHandleTemporaryDocProblems
				|| this.handleTemporaryRProblems != newHandleTemporaryRProblems ) {
			final boolean enabled= ((this.handleTemporaryDocProblems != newHandleTemporaryDocProblems) ? newHandleTemporaryDocProblems : false)
					|| ((this.handleTemporaryRProblems != newHandleTemporaryRProblems) ? newHandleTemporaryRProblems : false);
			this.handleTemporaryDocProblems= newHandleTemporaryRProblems;
			this.handleTemporaryRProblems= newHandleTemporaryRProblems;
			if (enabled) {
				WikitextModel.getWikidocModelManager().refresh(LTK.EDITOR_CONTEXT);
			}
			else {
				final String mode;
				if (!this.handleTemporaryDocProblems) {
					if (!this.handleTemporaryRProblems) {
						mode= null;
					}
					else {
						mode= WikitextModel.WIKIDOC_TYPE_ID;
					}
				}
				else {
					mode= RModel.R_TYPE_ID;
				}
				final List<? extends ISourceUnit> sus= LTK.getSourceUnitManager().getOpenSourceUnits(
						RModel.R_TYPE_ID, LTK.EDITOR_CONTEXT );
				for (final ISourceUnit su : sus) {
					final IAnnotationModel model= getAnnotationModel(su);
					if (model instanceof ThisAnnotationModel) {
						((ThisAnnotationModel) model).clearProblems(mode);
					}
				}
			}
		}
	}
	
	@Override
	protected IAnnotationModel createAnnotationModel(final IFile file) {
		return new ThisAnnotationModel(file);
	}
	
}
