/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.util.Transcription2Tiger;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A dialog for selecting tiers whose annotations will be exported into the Tiger Syntax Format (as leaf nodes). In
 * "Tiger-terminology": annotations will become feature values of terminal nodes.
 *
 * @author klasal
 */
@SuppressWarnings("serial")
public class ExportTigerDialog extends AbstractTierExportDialog implements ListSelectionListener {
    private JPanel textFieldPanel;
    private JTextField[] featureTextFields = new JTextField[0];

    /**
     * Creates a new ExportTigerDialog object.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the transcription
     * @param selection the selected time interval
     */
    public ExportTigerDialog(Frame parent, boolean modal, TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);
        makeLayout();
        extractTiers();
        updateFeatureTextFields();
        postInit();
    }

    /**
     * for debugging
     *
     * @param args no args
     */
    public static void main(String[] args) {
        String filename = "resources/testdata/elan/elan-example2.eaf";
        TranscriptionImpl transcription = new TranscriptionImpl(filename);

        JFrame frame = new JFrame();
        javax.swing.JDialog dialog = new ExportTigerDialog(frame, false, transcription, null);
        dialog.setVisible(true);
    }

    /**
     * Updates the checked state of the export checkboxes.
     *
     * @param lse the list selection event
     */
    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (!lse.getValueIsAdjusting()) {
            updateFeatureTextFields();
        }
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {
                List<TierImpl> v = getSentenceTiers(transcription);
                for (Tier t : v) {
                    // add all
                    model.addRow(Boolean.TRUE, t.getName());
                }
            }

            //read Preferences
            List<String> useTyp = Preferences.getListOfString("ExportTigerDialog.selectedTiers", transcription);
            if (useTyp != null) {
                loadTierPreferences(useTyp);
            }
        }
    }

    /**
     * Layout components.
     */
    @Override
    protected void makeLayout() {
        super.makeLayout();

        upButton.setEnabled(false);
        downButton.setEnabled(false);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(restrictCheckBox, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        optionsPanel.add(new JSeparator(), gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        optionsPanel.add(new JLabel("Features:"), gridBagConstraints);

        textFieldPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        optionsPanel.add(textFieldPanel, gridBagConstraints);

        tierTable.getSelectionModel().addListSelectionListener(this);
        setPreferredSetting();
        updateLocale();
    }

    /**
     * Starts the actual export.
     *
     * @return {@code true} if the export completed successfully
     *
     * @throws IOException any IO exception
     */
    @Override
    protected boolean startExport() throws
                                    IOException {

        List<String> selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                                          ElanLocale.getString("Message.Warning"),
                                          JOptionPane.WARNING_MESSAGE);

            return false;
        }

        long selectionBT = 0L;
        long selectionET = Long.MAX_VALUE;

        if (restrictCheckBox.isSelected()) {
            selectionBT = selection.getBeginTime();
            selectionET = selection.getEndTime();
        }

        String[] features = new String[featureTextFields.length];

        for (int i = 0; i < featureTextFields.length; i++) {
            features[i] = featureTextFields[i].getText();
        }

        // tiers should be presented in ordered form -> LinkedHashMap
        Map<TierImpl, Map<TierImpl, String>> sentenceTierHash = new LinkedHashMap<TierImpl, Map<TierImpl, String>>();

        // add selected tiers in the right order
        for (int i = 0; i < selectedTiers.size(); i++) {
            TierImpl sentenceTier = transcription.getTierWithId(selectedTiers.get(i));
            List<TierImpl> featureTiers = getFeatureTiers(sentenceTier);
            Map<TierImpl, String> featureHash = new LinkedHashMap<TierImpl, String>();

            for (int j = 0; j < featureTiers.size(); j++) {
                featureHash.put(featureTiers.get(j), features[j]);
            }

            sentenceTierHash.put(sentenceTier, featureHash);
        }

        //show only if there is a choice
        if (features.length > 1) {
            if (ExportTigerFeatureCheckPane.showFeatureCheckPane(this, sentenceTierHash, features)
                == ExportTigerFeatureCheckPane.CANCEL_OPTION) {
                return false;
            }
        }

        //prompt for file name and location
        File exportFile =
            promptForFile(ElanLocale.getString("Export.TigerDialog.title"), null, FileExtension.TIGER_EXT, true);

        if (exportFile == null) {
            return false;
        }

        Transcription2Tiger.exportTiers(transcription, sentenceTierHash, exportFile, encoding, selectionBT, selectionET);

        return true;
    }

    /**
     * Localization support.
     */
    @Override
    protected void updateLocale() {
        super.updateLocale();
        setTitle(ElanLocale.getString("ExportTigerDialog.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTigerDialog.TitleLabel"));
    }

    /**
     * Collects tiers that can become feature tiers.
     *
     * @param sentenceTier the root tier
     *
     * @return ordered HashMap of dependent Tiers with Constraint TIME_SUBDIVISION resp. SYMBOLIC_ASSOCIATION, containing
     *     (empty) features as values;
     */
    private static List<TierImpl> getFeatureTiers(TierImpl sentenceTier) {
        List<TierImpl> featureTiers = new ArrayList<TierImpl>();
        List<TierImpl> childTiers = sentenceTier.getChildTiers();

        for (int j = 0; j < childTiers.size(); j++) {
            TierImpl childTier = childTiers.get(j);

            if (childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) {
                featureTiers.add(childTier);

                addDescendantFeatureTiers(featureTiers, childTier);
            }
        }

        return featureTiers;
    }

    /**
     * Collects the tiers that can become sentence tiers.
     *
     * @param transcription the source transcription
     *
     * @return list of all tiers that have no constraints and a child with constraint TIME_SUBDIVISION
     */
    private static List<TierImpl> getSentenceTiers(TranscriptionImpl transcription) {
        //tiers without a constraint
        List<TierImpl> noConstraintTiers = new ArrayList<TierImpl>();

        //tiers without a constraint that have a child with constraint TIME_SUBDIVISION
        List<TierImpl> sentenceTiers = new ArrayList<TierImpl>();

        List<LinguisticType> lingTypes = transcription.getLinguisticTypes();

        for (int i = 0; i < lingTypes.size(); i++) {
            LinguisticType lingType = lingTypes.get(i);

            if (lingType.getConstraints() == null) {
                noConstraintTiers.addAll(transcription.getTiersWithLinguisticType(lingTypes.get(i).getLinguisticTypeName()));
            }
        }

        for (int i = 0; i < noConstraintTiers.size(); i++) {
            List<TierImpl> childTiers = noConstraintTiers.get(i).getChildTiers();

            boolean containsWordTier = false;

            for (int j = 0; j < childTiers.size(); j++) {
                TierImpl childTier = childTiers.get(j);
                Constraint constraint = childTier.getLinguisticType().getConstraints();

                if ((constraint != null) && (constraint.getStereoType() == Constraint.TIME_SUBDIVISION)) {
                    containsWordTier = true;

                    break;
                }
            }

            if (containsWordTier) {
                sentenceTiers.add(noConstraintTiers.get(i));
            }
        }

        return sentenceTiers;
    }

    /**
     * Adds descendant tiers with constraint SYMBOLIC ASSOCIATION to hashMap (with empty features as values)
     *
     * @param featureTiers the list to add feature tiers to
     * @param tier the source tier
     */
    private static void addDescendantFeatureTiers(List<TierImpl> featureTiers, TierImpl tier) {
        List<TierImpl> childTiers = tier.getChildTiers();

        for (int k = 0; k < childTiers.size(); k++) {
            TierImpl childTier = childTiers.get(k);

            if (childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) {
                featureTiers.add(childTier);

                addDescendantFeatureTiers(featureTiers, childTier);
            }
        }
    }

    /**
     * Depending on which sentence tiers are selected, the number possible features may vary.
     */
    private void updateFeatureTextFields() {
        int includeCol = model.findColumn(EXPORT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        int maxFeatures = 0;

        final int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Boolean include = (Boolean) model.getValueAt(i, includeCol);

            if (include.booleanValue()) {
                try {
                    TierImpl sentenceTier = transcription.getTierWithId((String) model.getValueAt(i, nameCol));
                    maxFeatures = Math.max(maxFeatures, getFeatureTiers(sentenceTier).size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (maxFeatures != featureTextFields.length) {
            textFieldPanel.removeAll();
            featureTextFields = new JTextField[maxFeatures];

            for (int i = 0; i < maxFeatures; i++) {
                featureTextFields[i] = new JTextField((i < Transcription2Tiger.defaultFeatureNames.length)
                                                      ? Transcription2Tiger.defaultFeatureNames[i]
                                                      : "", 10);
                textFieldPanel.add(featureTextFields[i]);
            }

            featureTextFields[0].setEnabled(false);
            textFieldPanel.revalidate();
        }
    }

    /**
     * Initializes the dialogBox with the last preferred/used settings
     */

    private void setPreferredSetting() {
        Boolean useTyp = Preferences.getBool("ExportTigerDialog.restrictCheckBox", null);
        if (useTyp != null) {
            restrictCheckBox.setSelected(useTyp);
        }

        List<String> stringsPref = Preferences.getListOfString("ExportTigerDialog.featureTextFields", transcription);
        if (stringsPref != null) {
            //if (((List) useTyp).size() == featureTextFields.length) {
            textFieldPanel.removeAll();
            final int size = stringsPref.size();
            featureTextFields = new JTextField[size];
            for (int i = 0; i < size; i++) {
                featureTextFields[i] = new JTextField(stringsPref.get(i));
                textFieldPanel.add(featureTextFields[i]);
            }
            //}
        }
    }

    /**
     * Saves the preferred settings Used.
     */
    private void savePreferences() {
        Preferences.set("ExportTigerDialog.restrictCheckBox", restrictCheckBox.isSelected(), null);

        List<String> features = new ArrayList<String>();
        for (JTextField featureTextField : featureTextFields) {
            features.add(featureTextField.getText());
        }
        Preferences.set("ExportTigerDialog.featureTextFields", features, transcription);
        Preferences.set("ExportTigerDialog.selectedTiers", getSelectedTiers(), transcription);
    }
}
