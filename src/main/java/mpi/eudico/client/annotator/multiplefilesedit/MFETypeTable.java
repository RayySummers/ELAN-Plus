package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;

/**
 * The multiple file editor table for tier types (linguistic types). 
 */
public class MFETypeTable extends MFETable {
	private static final long serialVersionUID = -6660589343733391482L;
	private MFEModel model;
	private CheckBoxTableCellRenderer cbRenderer;
	/** background color for even rows */
	//public final Color EVEN_LIGHT_BLUE = new Color(240, 255, 255);
	/** background color for selected row */
	//public final Color LESS_LIGHT_BLUE = new Color(200, 255, 255);
	/** background color for types with inconsistencies */
	public Color INCONS_LIGHT_GREY = new Color(240, 240, 240);
	/** darker background color for types with inconsistencies */
	public Color INCONS_DARK_GREY = new Color(150, 150, 150);
	
	/**
	 * Constructor.
	 * 
	 * @param model the tier type table model
	 * @param parent the parent frame
	 */
	public MFETypeTable(MFEModel model, final MFEFrame parent) {
		super(model);
		this.model = model;
		
		setModel(new TableByTypeModel(model));

        ImageIcon tickIcon = new ImageIcon(this.getClass().getResource(
            		Constants.ICON_LOCATION + "Tick16.gif"));
        ImageIcon untickIcon = new ImageIcon(this.getClass().getResource(
            		Constants.ICON_LOCATION + "Untick16.gif"));
        
        cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setIcon(untickIcon);
        cbRenderer.setSelectedIcon(tickIcon);
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				parent.initCombobox();
			}
		});
		if (Constants.DARK_MODE) {
			INCONS_LIGHT_GREY = new Color(
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getRed() + 20),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getGreen() + 20),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getBlue() + 20));
			INCONS_DARK_GREY = new Color(
					Math.max(0, Constants.DEFAULTBACKGROUNDCOLOR.getRed() - 10),
					Math.max(0, Constants.DEFAULTBACKGROUNDCOLOR.getGreen() - 10),
					Math.max(0, Constants.DEFAULTBACKGROUNDCOLOR.getBlue() - 10));
		}
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c;
		
		if(vColIndex==MFEModel.TYPE_TIMEALIGNABLECOLUMN)
			c = super.prepareRenderer(cbRenderer, rowIndex, vColIndex);
		else
			c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		int modelRow = this.convertRowIndexToModel(rowIndex);
		
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			if(!model.isConsistentType(modelRow)) {
				c.setBackground(INCONS_LIGHT_GREY);
			} else {
				c.setBackground(Constants.EVEN_ROW_BG);
			}
		} else {
			if(!model.isConsistentType(modelRow)) {
				c.setBackground(INCONS_DARK_GREY);
			} else {
				// If not shaded, match the table's background
				c.setBackground(getBackground());
			}
		}
		c.setForeground(getForeground());
		if(rowIndex == getSelectedRow())
			c.setBackground(Constants.SELECTED_ROW_BG);
		/*
		if(!model.isConsistentType(rowIndex)) {
			c.setBackground(INCONS_LIGHT_GREY);
			c.setBackground(INCONS_DARK_GREY); //?? check even/odd row?
		}
		*/
		return c;
	}

}
