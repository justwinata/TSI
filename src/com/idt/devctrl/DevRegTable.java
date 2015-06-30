package com.idt.devctrl;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class DevRegTable extends JTable {
	private static final long serialVersionUID = 5892435781282105124L;

	public String getToolTipText(MouseEvent e) {
		TableModel tblModel = getModel();
		if (tblModel instanceof DevRegTableModel) {
	        Point p = e.getPoint();
	        int row = rowAtPoint(p);
	        int col = columnAtPoint(p);
	        col = convertColumnIndexToModel(col);
	        return ((DevRegTableModel)tblModel).getToolTip(row, col);
		}
		return super.getToolTipText();
	}
}
