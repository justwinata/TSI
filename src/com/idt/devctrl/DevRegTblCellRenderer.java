package com.idt.devctrl;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class DevRegTblCellRenderer extends CenterAlignedStringCellRenderer {
	private static final long serialVersionUID = -3634542769956939935L;
	
	static final Color READ_ONLY_COLOR = new Color(238, 238, 238);
	static final Color MODIFIED_COLOR = Color.CYAN;// new Color(238,238,255);
	static final Color CHANGED_COLOR = new Color(255, 192, 192);

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		
		JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		TableModel tblModel = table.getModel();
		if (tblModel instanceof DevRegTableModel) {
			DevRegTableModel regTblModel = (DevRegTableModel) tblModel;
			if (!isSelected) {
				if (tblModel.isCellEditable(row, col)) {
					renderedLabel.setBackground(Color.WHITE);
					if (regTblModel.isChanged(row, col)) {
						renderedLabel.setBackground(CHANGED_COLOR);
					} else if (regTblModel.isModified(row, col)) {
						renderedLabel.setBackground(MODIFIED_COLOR);
					}
				} else {
					renderedLabel.setBackground(READ_ONLY_COLOR);
				}
			}
		}
		return renderedLabel;
	}
}
