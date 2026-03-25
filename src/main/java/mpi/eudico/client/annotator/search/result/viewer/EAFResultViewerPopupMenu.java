package mpi.eudico.client.annotator.search.result.viewer;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportResultTableAsEAF;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.client.annotator.grid.GridViewerPopupMenu;

/**
 * The context menu for a result viewer.
 */
@SuppressWarnings("serial")
public class EAFResultViewerPopupMenu extends GridViewerPopupMenu {
    final private JMenuItem exportAsEAFMenuItem;

    /**
     * Creates a EAFResultViewerPopupMenu instance.
     * 
     * @param table the table, the source component
     */
    public EAFResultViewerPopupMenu(AnnotationTable table) {
        super(table);
        
        exportAsEAFMenuItem = new JMenuItem(ElanLocale.getString("Frame.GridFrame.ExportTableAsEAF"));
        exportAsEAFMenuItem.addActionListener(this);
    }

    
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exportAsEAFMenuItem) {
            // export results as eaf
            ExportResultTableAsEAF exporter = new ExportResultTableAsEAF();
            exporter.exportTableAsEAF(table);
        } else {
            super.actionPerformed(e);    
        }
    }
    
    /**
     * Calls {@link GridViewerPopupMenu#makeLayout()} and adds an additional item, the 
     * "export with context as eaf" item.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();
        add(exportAsEAFMenuItem);
    }
    
}
