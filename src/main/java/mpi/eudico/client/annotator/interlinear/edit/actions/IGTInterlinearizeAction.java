package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTAnnotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import nl.mpi.lexan.analyzers.helpers.Position;

/**
 * An action class for starting the interlinearization ofa  single annotation.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTInterlinearizeAction extends IGTEditAction {
	/** The analyzer context  to use for starting the interlinearization action */
	protected TextAnalyzerHostContext hostContext;

	/**
	 * Constructor.
	 *
	 * @param igtAnnotation the IGT annotation to analyze
	 * @param analyzer the analyzer context class providing access to the interlinearization mechanism
	 * @param label the label for the action
	 *
	 * @see IGTEditAction#IGTEditAction(IGTAnnotation, String)
	 */
	public IGTInterlinearizeAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext analyzer, String label) {
		super(igtAnnotation, label);
		this.hostContext = analyzer;
	}

	/**
	 * Constructor.
	 *
	 * @param igtAnnotation the IGT annotation to analyze
	 * @param analyzer the the analyzer context class providing access to the interlinearization mechanism
	 * @param label the label for the action
	 * @param icon an icon for the action
	 *
	 * @see IGTEditAction#IGTEditAction(IGTAnnotation, String, Icon)
	 */
	public IGTInterlinearizeAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext analyzer, String label,
			Icon icon) {
		super(igtAnnotation, label, icon);
		this.hostContext = analyzer;
	}

	/**
	 * Constructor.
	 *
	 * @param igtAnnotation the IGT annotation to analyze
	 * @param analyzer the analyzer context class providing access to the interlinearization mechanism
	 *
	 * @see IGTEditAction#IGTEditAction(IGTAnnotation)
	 */
	public IGTInterlinearizeAction(IGTAnnotation igtAnnotation, TextAnalyzerHostContext analyzer) {
		super(igtAnnotation);
		this.hostContext = analyzer;
	}

	/**
	 * Creates a Position object that initializes the interlinearization process.
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (hostContext != null) {
			AbstractAnnotation annotation = null;

			if (igtAnnotation != null) {
				annotation = igtAnnotation.getAnnotation();
			} else {
				Transcription tr = hostContext.getTranscription();
				ViewerManager2 vm = ELANCommandFactory.getViewerManager(tr);
				annotation = (AbstractAnnotation) vm.getActiveAnnotation().getAnnotation();
			}

			if (annotation != null) {
				Position pos = new Position(annotation.getTier().getName(),
											annotation.getBeginTimeBoundary(),
											annotation.getEndTimeBoundary());

				hostContext.analyze(pos);
			}
		}
	}
}
