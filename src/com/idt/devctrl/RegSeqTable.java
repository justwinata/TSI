package com.idt.devctrl;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.ListSelectionModel;

public class RegSeqTable extends DevRegTable {
	private static final long serialVersionUID = -8976447780235508823L;

	public String getToolTipText(MouseEvent e) {
		String toolTip = super.getToolTipText(e);
        Point p = e.getPoint();
        int row = rowAtPoint(p);
		ListSelectionModel lsm = getSelectionModel();
		int selRow = lsm.getMinSelectionIndex();
		if (selRow >= 0) {
			toolTip = String.format("%s / %d", toolTip, row - selRow);
		}
		return toolTip;
	}
}
