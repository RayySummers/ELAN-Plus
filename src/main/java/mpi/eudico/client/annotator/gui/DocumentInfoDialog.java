package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * This dialog presents an overview of several document properties. Properties such as:
 * <ul>
 * <li>Author and license information</li>
 * <li>General information about tiers</li>
 * <li>Controlled vocabularies (e.g. number of entries)</li>
 * <li>Linked media descriptors</li>
 * </ul>
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class DocumentInfoDialog extends ClosableDialog {
    private final Transcription transcription;
    private final Insets insets = new Insets(2, 6, 2, 6);

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param transcription the transcription
     *
     * @throws HeadlessException if created in a headless environment
     */
    public DocumentInfoDialog(Frame owner, Transcription transcription) throws
                                                                        HeadlessException {
        super(owner, true);
        this.transcription = transcription;

        JPanel allPanel = new JPanel();
        allPanel.setLayout(new GridBagLayout());
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(ElanLocale.getString("DocumentInfoDialog.Document.Info"));

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        int y = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = y++;
        allPanel.add(titlePanel, gbc);

        gbc.gridy = y++;
        allPanel.add(createInformationComponent(), gbc);
        /* licenses etc */
        gbc.gridy = y++;
        addPanel(allPanel, createLicensesComponent(), gbc);

        /* tiers */
        gbc.gridy = y++;
        addPanel(allPanel, createTiersComponent(), gbc);

        /* types */
        gbc.gridy = y++;
        addPanel(allPanel, createTypeComponent(), gbc);

        /* CV's */
        gbc.gridy = y++;
        addPanel(allPanel, createControlledVocabulariesComponent(), gbc);

        /* linked files */
        gbc.gridy = y++;
        addPanel(allPanel, createLinkedFilesComponent(), gbc);

        getContentPane().add(new JScrollPane(allPanel));

        pack();

        setLocationRelativeTo(getParent());
        setTitle(ElanLocale.getString("DocumentInfoDialog.Document.Info"));
        setVisible(true);
    }

    /**
     * Adds a panel to a container
     *
     * @param receiver the host panel
     * @param nextPanel the panel to add
     * @param gbc a configured constraints object, the weighty and fill properties will be set conditionally
     */
    private void addPanel(JPanel receiver, JPanel nextPanel, GridBagConstraints gbc) {
        if (nextPanel.getComponent(0) instanceof JLabel) {
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else {
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
        }
        receiver.add(nextPanel, gbc);
    }

    /**
     * Create components in tabs that displays general EAF-file information such as:
     * <ul>
     * <li>Author name</li>
     * <li>Version number (at the moment, this is a TODO item)</li>
     * </ul>
     *
     * @return the new JPanel
     */
    private JPanel createInformationComponent() {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.Information")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.Author")), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String auth = transcription.getAuthor();
        if (auth == null || auth.isEmpty()) {
            auth = "-";
        }
        panel.add(new JLabel(auth), gbc);
        // cannot be set yet
        //panel.add(new JLabel("  " + ElanLocale.getString("DocumentInfoDialog.Version")), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create a component that displays licensing information such as:
     *
     * <p>- License number [1 .. N] if there are N licenses
     * - License URL - License content
     */
    private JPanel createLicensesComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.Licensing")));

        if (transcription.getLicenses().size() > 0) {
            /* Three columns: license-number, URL, content (will be abbreviated by JTable) */
            String[] cols = {"#", "URL", ElanLocale.getString("DocumentInfoDialog.Content")};
            String[][] data = new String[transcription.getLicenses().size()][3];

            /* Add license-information to data-model */
            for (int i = 0; i < transcription.getLicenses().size(); i = i + 1) {
                data[i][0] = Integer.toString(i + 1);
                data[i][1] = transcription.getLicenses().get(i).getUrl();
                data[i][2] = transcription.getLicenses().get(i).getText();
            }

            JTable table = new JTable(data, cols);
            table.setEnabled(false);
            table.getTableHeader().getColumnModel().getColumn(0).sizeWidthToFit();
            JScrollPane sp = new JScrollPane(table);
            int h = table.getRowHeight() * (table.getRowCount() + 2);
            sp.setPreferredSize(new Dimension(400, Math.min(80, h)));

            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(sp, gbc);
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.NoLicenses")));
        }
        return panel;
    }

    /**
     * Create component in tabs that displays information about the tiers such as:
     *
     * <p>- Tier name
     * - Number of annotations - Tier type (also referred to as LinguisticType)
     *
     * @return the new JPanel
     */
    private JPanel createTiersComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.Tiers")));

        if (transcription.getTiers().size() > 0) {
            List<? extends Tier> tiers = transcription.getTiers();

            String[] cols = {ElanLocale.getString("DocumentInfoDialog.Name"),
                             "# " + ElanLocale.getString("DocumentInfoDialog.Annotations"),
                             ElanLocale.getString("DocumentInfoDialog.Type")};
            String[][] data = new String[tiers.size()][3];

            for (int i = 0; i < tiers.size(); i = i + 1) {
                data[i][0] = tiers.get(i).getName();
                data[i][1] = Integer.toString(tiers.get(i).getNumberOfAnnotations());
                data[i][2] = tiers.get(i).getLinguisticType().getLinguisticTypeName();
            }

            JTable table = new JTable(data, cols);
            table.setEnabled(false);
            JScrollPane sp = new JScrollPane(table);
            int h = table.getRowHeight() * (table.getRowCount() + 2);
            sp.setPreferredSize(new Dimension(400, Math.min(100, h)));

            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(sp, gbc);
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.NoTiers")));
        }
        return panel;
    }

    /**
     * Creates a panel with a simplified type table.
     *
     * @return a panel with type information
     */
    private JPanel createTypeComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.Type")));

        if (transcription.getLinguisticTypes().size() > 0) {
            List<LinguisticType> types = transcription.getLinguisticTypes();
            String[] columns = new String[] {LinguisticTypeTableModel.NAME,
                                             LinguisticTypeTableModel.STEREOTYPE,
                                             LinguisticTypeTableModel.CV_NAME};
            LinguisticTypeTableModel model = new LinguisticTypeTableModel(types, columns);

            JTable table = new JTable(model);
            table.setEnabled(false);
            JScrollPane sp = new JScrollPane(table);
            int h = table.getRowHeight() * (table.getRowCount() + 2);
            sp.setPreferredSize(new Dimension(400, Math.min(100, h)));

            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(sp, gbc);
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.NoTypes")));
        }

        return panel;
    }

    /**
     * Create component in tabs that displays information about controlled vocabularies such as:
     *
     * <p>- CV name
     * - Number of entries - Number of languages
     *
     * @return the new JPanel
     */
    private JPanel createControlledVocabulariesComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.ControlledVocabularies")));

        if (transcription.getControlledVocabularies().size() > 0) {
            List<ControlledVocabulary> cvs = transcription.getControlledVocabularies();

            String[] cols = {ElanLocale.getString("DocumentInfoDialog.Name"),
                             "# " + ElanLocale.getString("DocumentInfoDialog.Languages"),
                             "# " + ElanLocale.getString("DocumentInfoDialog.Entries")};
            String[][] data = new String[cvs.size()][3];

            for (int i = 0; i < cvs.size(); i = i + 1) {
                data[i][0] = cvs.get(i).getName();
                data[i][1] = Integer.toString(cvs.get(i).getNumberOfLanguages());
                data[i][2] = Integer.toString(cvs.get(i).size());
            }

            JTable table = new JTable(data, cols);
            table.setEnabled(false);
            JScrollPane sp = new JScrollPane(table);
            int h = table.getRowHeight() * (table.getRowCount() + 2);
            sp.setPreferredSize(new Dimension(400, Math.min(100, h)));

            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(sp, gbc);
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.NoCVs")));
        }

        return panel;
    }

    /**
     * Create component in tabs that displays information about linked files such as:
     *
     * <p>- Location of the media
     * - Relative location - MIME type - Time origin
     *
     * @return the new JPanel
     */
    private JPanel createLinkedFilesComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(ElanLocale.getString("DocumentInfoDialog.Linked.Media")));

        if (transcription.getMediaDescriptors().size() > 0) {
            List<MediaDescriptor> files = transcription.getMediaDescriptors();

            String[] cols = {ElanLocale.getString("DocumentInfoDialog.Location"),
                             ElanLocale.getString("DocumentInfoDialog.Relative.Location"),
                             ElanLocale.getString("DocumentInfoDialog.MIME.Type"),
                             ElanLocale.getString("DocumentInfoDialog.Time.Origin")};
            String[][] data = new String[files.size()][4];

            for (int i = 0; i < files.size(); i = i + 1) {
                data[i][0] = files.get(i).mediaURL;
                data[i][1] = files.get(i).relativeMediaURL;
                data[i][2] = files.get(i).mimeType;
                data[i][3] = Long.toString(files.get(i).timeOrigin);
            }

            JTable table = new JTable(data, cols);
            table.setEnabled(false);
            JScrollPane sp = new JScrollPane(table);
            int h = table.getRowHeight() * (table.getRowCount() + 2);
            sp.setPreferredSize(new Dimension(400, Math.min(100, h)));

            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(sp, gbc);
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(ElanLocale.getString("DocumentInfoDialog.NoLinkedMedia")));
        }

        return panel;
    }
}
