package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;

import java.util.List;

/**
 * A command to display a dialog to change some document properties
 *
 * @author Allan van Hulst
 */
public class SetDocumentPropertiesCommand implements UndoableCommand {
    private Transcription transcription = null;
    private final String name;
    private String oldAuthor;
    private String nextAuthor;
    private List<LicenseRecord> oldRecords;
    private List<LicenseRecord> nextRecords;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public SetDocumentPropertiesCommand(String name) {
        this.name = name;
    }

    /**
     * Sets the new author and licenses again.
     */
    @Override
    public void redo() {
        if (transcription != null) {
            if (nextAuthor != null) {
                transcription.setAuthor(nextAuthor);
            }
            if (nextRecords != null) {
                transcription.setLicenses(nextRecords);
            }
        }
    }

    /**
     * Resets author and licenses.
     */
    @Override
    public void undo() {
        if (transcription != null) {
            if (oldAuthor != null) {
                transcription.setAuthor(oldAuthor);
            }

            transcription.setLicenses(oldRecords);
        }
    }

    /**
     * @param receiver the transcription
     * @param arguments [0] = author (String), [1] = list of license objects
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof Transcription) {
            transcription = (Transcription) receiver;
        }

        if (transcription == null) {
            return;
        }

        if (arguments[0] instanceof String) {
            nextAuthor = (String) arguments[0];
        }

        if (arguments[1] instanceof List<?> l) {
            if (l.size() > 0) {
                if (l.get(0) instanceof LicenseRecord) {
                    nextRecords = (List<LicenseRecord>) l;
                }
            }
        }

        checkAndApplyChanges();
    }

    /**
     * Compares new and old settings and applies changes.
     */
    private void checkAndApplyChanges() {
        if (transcription != null) {
            oldAuthor = transcription.getAuthor();
            if (nextAuthor != null && !nextAuthor.equals(oldAuthor)) {
                transcription.setAuthor(nextAuthor);
                transcription.setChanged();
            }

            // the comparison of the licenses includes the order
            oldRecords = transcription.getLicenses();
            if (nextRecords != null) {
                if (oldRecords.size() != nextRecords.size()) {
                    transcription.setLicenses(nextRecords);
                    transcription.setChanged();
                } else {
                    boolean different = false;

                    for (int i = 0; i < nextRecords.size(); i++) {
                        LicenseRecord lr = nextRecords.get(i);
                        LicenseRecord olr = oldRecords.get(i);

                        if ((lr.getUrl() == null && olr.getUrl() != null) || (lr.getUrl() != null && !lr.getUrl()
                                                                                                        .equals(olr.getUrl()))) {
                            different = true;
                            break;
                        }
                        if (!lr.getText().equals(olr.getText())) {
                            different = true;
                            break;
                        }
                    }

                    if (different) {
                        transcription.setLicenses(nextRecords);
                        transcription.setChanged();
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
