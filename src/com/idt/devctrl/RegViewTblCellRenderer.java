package com.idt.devctrl;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableModel;

public class RegViewTblCellRenderer extends DevRegTblCellRenderer {
	private static final long serialVersionUID = 6149465746300771587L;

	static final Color BIT_COLOR = new Color(255, 255, 192);
	static final Color MASK_COLOR = new Color(200, 255, 192);
	static final Color FOCUS_COLOR = Color.GRAY;
	static final Border NORMAL_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY);
	static final Border RIGHT_MERGE_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
	static final Border FOCUS_BORDER = BorderFactory.createMatteBorder(1, 1, 2, 2, FOCUS_COLOR);
	static final Border FOCUS_RIGHT_MERGE_BORDER = BorderFactory.createMatteBorder(1, 2, 2, 2, FOCUS_COLOR);

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		
		JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		TableModel tblModel = table.getModel();
		if (tblModel instanceof DevRegTableModel) {
			DevRegTableModel regTblModel = (DevRegTableModel) tblModel;
			if (!isSelected) {
				if (tblModel.isCellEditable(row, col)) {
					if (!(regTblModel.isChanged(row, col) || regTblModel.isModified(row, col))) {
						if (regTblModel.isBitCell(row, col)) {
							renderedLabel.setBackground(BIT_COLOR);
						} else if (regTblModel.isMaskCell(row, col)) {
							renderedLabel.setBackground(MASK_COLOR);
						}
					}
				}
			}
			if (hasFocus) {
				if (regTblModel.mergeWithRightCell(row, col)) {
					renderedLabel.setBorder(FOCUS_RIGHT_MERGE_BORDER);
				} else {
					renderedLabel.setBorder(FOCUS_BORDER);
				}
			} else {
				if (regTblModel.mergeWithRightCell(row, col)) {
					renderedLabel.setBorder(RIGHT_MERGE_BORDER);
				} else {
					renderedLabel.setBorder(NORMAL_BORDER);
				}
			}
		}
		return renderedLabel;
	}
}
