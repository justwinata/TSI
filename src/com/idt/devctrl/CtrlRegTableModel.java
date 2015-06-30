package com.idt.devctrl;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author Vitaliy Kulikov
 *
 */
public class CtrlRegTableModel extends AbstractTableModel implements DevRegTableModel, ChangeListener {
	private static final long serialVersionUID = 1124236546248341432L;

	static String addrHFormat = "%02X"; 
	static String columnHFormat = "%X";
	static int columnCount = 0x10;
	static int rowCount = 0x10;
	
	private I2CDevice device = null;
	private int page = 0;
	private String[] toolTipsCache = null;

	public static int toRegisterAddress(int row, int col) {
		return row * columnCount + col;
	}
	
	private static int getColumn(int address) {
		return address % rowCount;
	}
	
	private static int getRow(int address) {
		return address / rowCount;
	}
	
	public CtrlRegTableModel() {
		toolTipsCache = new String[rowCount * columnCount];
	}
	
	public void setDevice(I2CDevice device) {
		if (this.device != device) {
			if (device != null) {
				this.page = device.getCurrentPage();
				device.addChangeListener(this);
			}
			this.device = device;
			fireTableDataChanged();
		}
	}
	
	public void setDecimalTableFormat(boolean decimal) {
		if (decimal) {
			addrHFormat = "%02d";
			columnHFormat = "%d";
			columnCount = 10;
			rowCount = 25;
		} else {
			addrHFormat = "%02X";
			columnHFormat = "%X";
			columnCount = 0x10;
			rowCount = 0x10;
		}
		fireTableStructureChanged();
	}
	
	public void setPage(int page) {
		this.page = page;
		for (int i=0; i<toolTipsCache.length; i++)
			toolTipsCache[i] = null;
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	@Override
	public String getColumnName(int col) {
		return String.format(columnHFormat, col);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (device != null)
			return device.getRegister(page, toRegisterAddress( row, col)) != null;
		return false;
	}

	@Override
	public boolean isChanged(int row, int col) {
		if (device != null)
			return device.getRegister(page, toRegisterAddress( row, col)).isChanged();
		return false;
	}

	@Override
	public boolean isModified(int row, int col) {
		if (device != null)
			return device.getRegister(page, toRegisterAddress( row, col)).isModified();
		return false;
	}

	@Override
	public boolean isBitCell(int row, int col) {
		return false;
	}

	@Override
	public boolean mergeWithRightCell(int row, int col) {
		return false;
	}

	@Override
	public boolean isMaskCell(int row, int col) {
		return false;
	}

	@Override
	public String getToolTip(int row, int col) {
		if (isCellEditable(row, col)) {
			int radr = toRegisterAddress(row, col);
			if (toolTipsCache[radr] == null)
				toolTipsCache[radr] = String.format("%d / %02Xh", radr, radr);
			return toolTipsCache[radr];
			}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (device != null) {
			Register r = device.getRegister(page, toRegisterAddress( row, col));
			if (r != null)
				return r.getValueHexStr();
		}
		return null;
	}

	@Override
	public void setValueAt(Object val, int row, int col) {
		String valStr = val.toString();
		Register reg = device.getRegister(page, toRegisterAddress( row, col)); 
		if (valStr.isEmpty() || Register.isValidHexStr(valStr)) {
			/* empty value will get loaded value restored */
			reg.modifyValue(val.toString(), 16);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int reg = ((Register)e.getSource()).getAddress(); 
		fireTableCellUpdated(getRow(reg), getColumn(reg));
	}
}
