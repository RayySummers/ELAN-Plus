package mpi.eudico.client.annotator.tier;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ModifyAllAnnotationsBoundariesCommand;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A class to receive the user input, differentiates the single file and
 * multi-file dialog and calls a command to apply the modification logic.
 *
 */
public class ModifyAllAnnotationBoundaries implements ProcessReporter {

	/** single file mode constant */
	public static final int SINGLE_FILE_MODE = 0;
	/** multi file mode constant */
	public static final int MULTI_FILE_MODE = 1;

	/** command name */
	protected String commandName;
	/** transcription */
	protected TranscriptionImpl transcription;

	/** filenames array */
	protected String[] fileNames;

	/** begin time shift variable */
	protected Long beginTimeShift;

	/** end time shift variable */
	protected Long endTimeShift;

	/** process report */
	protected ProcessReport report;

	/** process mode */
	protected int processMode;

	private List<ProgressListener> listeners;

	/** source tiers array */
	protected String[] selectedTiers;

	private Boolean overrideLeftAnnotation = false;
	private Boolean overrideRightAnnotation = false;

	/**
	 * Constructor.
	 *
	 * @param name the name of the command
	 */
	public ModifyAllAnnotationBoundaries(String name) {
		commandName = name;
		report = new SimpleReport(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Report.Title"));
	}

	/**
	 * Executes the command.
	 * 
	 * @param receiver  the TranscriptionImpl
	 * @param arguments the arguments:
	 *                  <ul>
	 *                  <li>arg[0] = the selected tiers (String[])</li>
	 *                  <li>arg[1] = the file names (String[])</li>
	 *                  <li>arg[2] = the begin time shift (Long)</li>
	 *                  <li>arg[3] = the end time shift (Long)</li>
	 *                  <li>arg[4] = the override left annotation (Boolean)</li>
	 *                  <li>arg[5] = the override right annotation (Boolean)</li>
	 *                  <li>arg[6] = the process mode (int)</li>
	 *                  </ul>
	 * 
	 */
	public void execute(Object receiver, Object[] arguments) {
		transcription = (TranscriptionImpl) receiver;

		Object[] objectArray = (Object[]) arguments[0];
		selectedTiers = new String[objectArray.length];

		for (int i = 0; i < objectArray.length; i++) {
			selectedTiers[i] = objectArray[i].toString();
		}

		objectArray = (Object[]) arguments[1];
		if (objectArray != null) {
			fileNames = new String[objectArray.length];
			for (int i = 0; i < objectArray.length; i++)
				fileNames[i] = objectArray[i].toString();
		}

		beginTimeShift = (Long) arguments[2];

		endTimeShift = (Long) arguments[3];

		overrideLeftAnnotation = (Boolean) arguments[4];

		overrideRightAnnotation = (Boolean) arguments[5];

		processMode = (Integer) arguments[6];

		execute();
	}

	/**
	 * Starts the process in a separate thread.
	 */
	protected void execute() {
		Thread calcThread = new ModifyAnnotationsThread(ModifyAllAnnotationBoundaries.class.getName());

		try {
			calcThread.start();
		} catch (Exception exc) {
			LOG.severe("Exception in calculation: " + exc.getMessage());
			progressInterrupt("An exception occurred: " + exc.getMessage());
			report("An exception occurred: " + exc.getMessage());
		}
	}

	private void modifyAllAnnotations(TranscriptionImpl transcription) {

		List<String> selectedTierList = Arrays.asList(selectedTiers);
		List<TierImpl> parentTiers = new ArrayList<TierImpl>();
		List<TierImpl> childTiers = new ArrayList<TierImpl>();
		List<TierImpl> allTiersSorted = new ArrayList<TierImpl>();

		for (String name : selectedTierList) {
			TierImpl tier = transcription.getTierWithId(name);
			if (tier != null) {
				TierImpl parentTier = tier.getParentTier();
				if (parentTier == null) {
					parentTiers.add(tier);
				} else {
					childTiers.add(tier);
				}
			}
		}

		allTiersSorted.addAll(parentTiers);
		allTiersSorted.addAll(childTiers);

		Object arguments[] = null;
		arguments = new Object[] { beginTimeShift, endTimeShift, overrideLeftAnnotation, overrideRightAnnotation,
				allTiersSorted };
		Command com = ELANCommandFactory.createCommand(transcription,
				ELANCommandFactory.MODIFY_ALL_ANNOTATION_BOUNDARIES_CMD);
		com.execute(transcription, arguments);

	}

	private void modifyAllAnnotationsPerTranscription(TranscriptionImpl transcription) {
		List<String> selectedTierList = Arrays.asList(selectedTiers);
		List<TierImpl> parentTiers = new ArrayList<TierImpl>();
		List<TierImpl> childTiers = new ArrayList<TierImpl>();
		List<TierImpl> allTiersSorted = new ArrayList<TierImpl>();

		for (String name : selectedTierList) {
			TierImpl tier = transcription.getTierWithId(name);
			if (tier != null) {
				TierImpl parentTier = tier.getParentTier();
				if (parentTier == null) {
					parentTiers.add(tier);
				} else {
					childTiers.add(tier);
				}
			}

		}

		allTiersSorted.addAll(parentTiers);
		allTiersSorted.addAll(childTiers);

		Object arguments[] = null;
		arguments = new Object[] { beginTimeShift, endTimeShift, overrideLeftAnnotation, overrideRightAnnotation,
				allTiersSorted };
		ModifyAllAnnotationsBoundariesCommand command = new ModifyAllAnnotationsBoundariesCommand(
				ELANCommandFactory.MODIFY_ALL_ANNOTATION_BOUNDARIES_CMD);
		command.execute(transcription, arguments);

	}

	/**
	 * Adds a ProgressListener to the list of ProgressListeners.
	 *
	 * @param pl the new ProgressListener
	 */
	public synchronized void addProgressListener(ProgressListener pl) {
		if (listeners == null) {
			listeners = new ArrayList<ProgressListener>(2);
		}

		listeners.add(pl);
	}

	/**
	 * Removes the specified ProgressListener from the list of listeners.
	 *
	 * @param pl the ProgressListener to remove
	 */
	public synchronized void removeProgressListener(ProgressListener pl) {
		if ((pl != null) && (listeners != null)) {
			listeners.remove(pl);
		}
	}

	/**
	 * Notifies any listeners of a progress update.
	 *
	 * @param percent the new progress percentage, [0 - 100]
	 * @param message a descriptive message
	 */
	protected void progressUpdate(int percent, String message) {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).progressUpdated(this, percent, message);
			}
		}
	}

	/**
	 * Notifies any listeners that the process has completed.
	 *
	 * @param message a descriptive message
	 */
	protected void progressComplete(String message) {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).progressCompleted(this, message);
			}
		}
	}

	/**
	 * Notifies any listeners that the process has been interrupted.
	 *
	 * @param message a descriptive message
	 */
	protected void progressInterrupt(String message) {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).progressInterrupted(this, message);
			}
		}
	}

	@Override
	public void setProcessReport(ProcessReport report) {

	}

	@Override
	public ProcessReport getProcessReport() {

		return null;
	}

	@Override
	public void report(String message) {
		if (report != null) {
			report.append(message);
		}

	}

	/**
	 * A thread class that sorts the selected tier and based on the single file or
	 * multi file calls the command class by sending that state
	 */
	private class ModifyAnnotationsThread extends Thread {

		public ModifyAnnotationsThread(String name) {
			super(name);

		}

		/**
		 * Interrupts the current calculation process.
		 */
		@Override
		public void interrupt() {
			super.interrupt();
			progressInterrupt("Operation interrupted...");
			report("Operation interrupted...");
		}

		/**
		 * The actual action of this thread.
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (processMode == SINGLE_FILE_MODE) {

				modifyAllAnnotations(transcription);
				progressComplete("Operation complete...");

			} else if (processMode == MULTI_FILE_MODE) {

				float perFile = 25f;
				if (fileNames.length > 0) {
					perFile = 25 / (float) fileNames.length;
				}

				String total = "Total no of files : " + fileNames.length;
				String processed = "Processed files : ";
				String processing = "processing file : ";
				TranscriptionImpl transcription;
				String buffer = null;

				for (int i = 0; i < fileNames.length; i++) {

					transcription = new TranscriptionImpl(fileNames[i]);
					report("\nFile " + i + " : " + transcription.getName());
					buffer = total + "\n" + processed + i + "\n" + processing + transcription.getName();
					progressUpdate((int) (i * perFile), buffer);
					modifyAllAnnotationsPerTranscription(transcription);

					String directoryToSave = transcription.getPathName();
					int saveAsType = SaveAs27Preferences.saveAsTypeWithCheck(transcription);
					try {
						ACMTranscriptionStore.getCurrentTranscriptionStore().storeTranscriptionIn(transcription, null,
								new ArrayList<TierImpl>(), directoryToSave, saveAsType);
					} catch (IOException e) {
						LOG.warning("Can not write transcription to file with directory/filename: " + directoryToSave);

						report("Can not write transcription ' " + transcription.getName()
								+ "' to file with directory/filename: " + directoryToSave);
					}
				}

				progressComplete(buffer + '\n' + "Operation complete...");

				report("\nNumber of files in domain:  " + fileNames.length);
				report("Modified Tiers : " + Arrays.toString(selectedTiers));
				
				new ReportDialog(report).setVisible(true);
			}

		}

	}

}
