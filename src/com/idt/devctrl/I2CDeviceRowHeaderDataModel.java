package com.idt.devctrl;

import javax.swing.table.AbstractTableModel;


public class I2CDeviceRowHeaderDataModel extends AbstractTableModel {
	private static final long serialVersionUID = -7246394298284614264L;

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return CtrlRegTableModel.rowCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return String.format(CtrlRegTableModel.addrHFormat, row * CtrlRegTableModel.columnCount);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
	
}
