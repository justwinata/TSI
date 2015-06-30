package com.idt.devctrl;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author Vitaliy Kulikov
 *
 */
public class RegisterViewTableModel extends AbstractTableModel 
				implements DevRegTableModel, ChangeListener, ListSelectionListener , TableColumnModelListener {
	private static final long serialVersionUID = -9098807547601264029L;

	static final int REG_NUM_COLUMN = 0;
	static final int ADDR_COLUMN = REG_NUM_COLUMN + 1;
	static final int VALUE_COLUMN = ADDR_COLUMN + 1;
	static final int BITS_COLUMN = VALUE_COLUMN + 1;
	int descrColumn = BITS_COLUMN;
	
	private I2CDevice device = null;
	private Register register = null;
	private int regRowAddr = -1;
	private int regColAddr = -1;
	private int page = -1;
	
	public boolean isBitColumn(int col) {
		return col >= BITS_COLUMN && col < descrColumn;
	}
	
	private int columnToBit(int col) {
		int bit = descrColumn - col -1;
		return bit;
	}

	public void setDevice(I2CDevice device) {
		if (this.device != device) {
			if (device != null) {
				page = device.getCurrentPage();
				device.addChangeListener(this);
				descrColumn = BITS_COLUMN + device.getRegister(0, 0).getBitDepth();
			} else
				descrColumn = BITS_COLUMN;
			register = null;
			this.device = device;
			fireTableStructureChanged();
		}
	}
	
	public void setPage(int pg) {
		if (page != pg) {
			page = pg;
			register = null;
		}
		fireTableDataChanged();
	}
	
	public void setRegister(Register reg) {
		if (register != reg) {
			register = reg;
			assert reg == null || reg.getPage() == page;
		}
		fireTableDataChanged();
	}
	
	public void setRegister(int reg) {
		if (register == null || register.getAddress() != reg) {
			if (device != null && device.isValidRegister(page, reg)) {
				setRegister(device.getRegister(page , reg));
			}
		}
	}

	@Override
	public int getColumnCount() {
		return descrColumn + 1;
	}

	@Override
	public int getRowCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case REG_NUM_COLUMN:
			return "Reg(d)";
		case ADDR_COLUMN:
			return "Reg(h)";
		case VALUE_COLUMN:
			return "Val";
		}
		if (col == descrColumn)
			return "Description";
		return String.format("%d", columnToBit(col));
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if (device != null && row == 0) {
			switch (col) {
			case REG_NUM_COLUMN:
			case ADDR_COLUMN:
				return true;
			case VALUE_COLUMN:
				return register != null;
			}
			if (isBitColumn(col))
				return register != null;
		}
		return false;
	}

	@Override
	public boolean isChanged(int row, int col) {
		if (register != null) {
			switch (col) {
			case VALUE_COLUMN:
				return register.isChanged();
			}
			if (isBitColumn(col))
				return register.isChanged(columnToBit(col));
		}
		return false;
	}

	@Override
	public boolean isModified(int row, int col) {
		if (register != null) {
			switch (col) {
			case VALUE_COLUMN:
				return register.isModified();
			}
			if (isBitColumn(col))
				return register.isModified(columnToBit(col));
		}
		return false;
	}

	@Override
	public boolean isBitCell(int row, int col) {
		if (register != null && row == 0) {
			return isBitColumn(col);
		}
		return false;
	}

	@Override
	public boolean mergeWithRightCell(int row, int col) {
		return (row == 0 && isBitColumn(col) && col != descrColumn-1) 
				|| (row != 0 && col == REG_NUM_COLUMN);
				//|| col == descrColumn;
	}

	@Override
	public boolean isMaskCell(int row, int col) {
		return false;
	}

	@Override
	public String getToolTip(int row, int col) {
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (register != null) {
			switch (col) {
			case REG_NUM_COLUMN:
				return row == 0 ? register.getAddressStr() : "old";
			case ADDR_COLUMN:
				return row == 0 ? register.getAddressHexStr() : "value";
			case VALUE_COLUMN:
				return row == 0 ? register.getValueHexStr() : register.getOldValueHexStr();
			}
			if (col == descrColumn)
				return row == 0 ? register.getDescription() : "old value";
			if (isBitColumn(col)) {
				return row == 0 ? register.getBit(columnToBit(col)) : register.getOldBit(columnToBit(col));
			}
		}
		return null;
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		String val = value.toString();
		if (row == 0) {
			if (isBitColumn(col)) {
				if (register != null && (val.isEmpty() || Register.isValidBitValue(val))) {
					register.modifyBit(columnToBit(col), val);
					fireTableDataChanged();
				}
			} else {
				switch (col) {
				case VALUE_COLUMN:
					if (register != null && (val.isEmpty() || Register.isValidHexStr(val))) {
						register.modifyValue(val, 16);
						fireTableDataChanged();
					}
					break;
				case REG_NUM_COLUMN:
					if (Register.isValidDecStr(val))
						setRegister(Integer.parseInt(val));
					break;
				case ADDR_COLUMN:
					if (Register.isValidHexStr(val))
						setRegister(Integer.parseInt(val, 16));
					break;
				}
			}
		}
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (register != null) {
			if (((Register)e.getSource()).getAddress() == register.getAddress()) {
				fireTableDataChanged();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		regRowAddr = lsm.getMinSelectionIndex();
		int reg = CtrlRegTableModel.toRegisterAddress(regRowAddr, regColAddr);
		if (register == null || register.getAddress() != reg)
			setRegister(reg);
	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		regColAddr = lsm.getMinSelectionIndex();
		int reg = CtrlRegTableModel.toRegisterAddress(regRowAddr, regColAddr);
		if (register == null || register.getAddress() != reg)
			setRegister(reg);
	}

	@Override
	public void columnAdded(TableColumnModelEvent e) {}
	@Override
	public void columnMarginChanged(ChangeEvent e) {}
	@Override
	public void columnMoved(TableColumnModelEvent e) {}
	@Override
	public void columnRemoved(TableColumnModelEvent e) {}
}
