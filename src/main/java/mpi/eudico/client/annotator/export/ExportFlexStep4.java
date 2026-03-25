package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoder;
import mpi.eudico.server.corpora.clomimpl.flex.FlexEncoderInfo;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Step pane for the actual export and progress monitoring.
 *
 * @author aarsom
 */
@SuppressWarnings("serial")
public class ExportFlexStep4 extends StepPane {

    private FlexEncoderInfo encoderInfo;

    private final TranscriptionImpl transcription;

    private JTextField fileTextField;
    private JButton browseButton;


    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param trans the transcription
     */
    public ExportFlexStep4(MultiStepPane multiPane, TranscriptionImpl trans) {
        super(multiPane);
        transcription = trans;
        initComponents();
    }


    /**
     * @see StepPane#getStepTitle()
     */
    @Override
    public String getStepTitle() {
        return ElanLocale.getString("ExportFlexStep4.Title");
    }

    /**
     * Calls doFinish.
     *
     * @see StepPane#enterStepForward()
     */
    @Override
    public void enterStepForward() {
        encoderInfo = (FlexEncoderInfo) multiPane.getStepProperty("EncoderInfo");
        updateButtonStates();
    }

    @Override
    public void enterStepBackward() {
        updateButtonStates();
    }

    /**
     * Set the button states appropriately, according to constraints
     */
    public void updateButtonStates() {
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);

        if (fileTextField.getText().trim().length() <= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }

    /**
     * Initializes the components for ui.
     */
    @Override
    protected void initComponents() {
        fileTextField = new JTextField();
        fileTextField.setEditable(false);

        browseButton = new JButton(ElanLocale.getString("Button.Browse"));
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFlexFile();
                updateButtonStates();
            }

        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 4, 2, 4);
        add(new JLabel(ElanLocale.getString("ExportFlexStep4.SelectDestination")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(fileTextField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        add(browseButton, gbc);

    }

    /**
     * Select a flex file for export
     */
    private void selectFlexFile() {
        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
        chooser.createAndShowFileDialog(ElanLocale.getString("ExportFlexDialog.Title"),
                                        FileChooser.SAVE_DIALOG,
                                        FileExtension.FLEX_EXT,
                                        "LastUsedFlexDir");
        File f = chooser.getSelectedFile();
        if (f != null) {
            fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
    public boolean doFinish() {
        encoderInfo.setFile(fileTextField.getText());

        FlexEncoder encoder = new FlexEncoder();
        encoder.setEncoderInfo(encoderInfo);
        encoder.encode(transcription);

        return true;
    }
}
