package com.idt.devctrl;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class CenterAlignedStringCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -2398279337403615564L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		renderedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		return renderedLabel;
	}
}
