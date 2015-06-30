package com.idt.devctrl;

import javax.swing.table.AbstractTableModel;

/**
 * @author Vitaliy Kulikov
 *
 */
public class SettingsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 562286528822863376L;

	static final int NAME_COLUMN = 0;
	static final int VALUE_COLUMN = 1;

	private Settings settings;
	private String[] valuesCache;
	
	public void setDevice(Settings settings) {
		if (this.settings == null || this.settings != settings) {
			this.settings = settings;
			if (settings != null)
				valuesCache = new String[settings.getSettingsCount()];
		}
	}
	
	public void reset() {
		if (settings != null) {
			settings.resetSettings();
			for (int i = 0; i < valuesCache.length; i++)
				valuesCache[i] = null;
			fireTableDataChanged();
		}
	}
	
	public void update() {
		if (settings != null) {
			settings.applySettings();
		}
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case NAME_COLUMN:
			return "Name";
		case VALUE_COLUMN:
			return "Value";
		}
		return "";
	}
	
	@Override
	public int getRowCount() {
		return settings == null ? 0 : settings.getSettingsCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (settings != null) {
			switch (col) {
			case NAME_COLUMN:
				return settings.getSettingName(row);
			case VALUE_COLUMN:
				if (valuesCache[row] != null)
					return valuesCache[row];
				else
					return settings.getSettingValue(row);
			default:
				throw new IllegalArgumentException("Invalid column "+col);
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (settings != null) {
			return col == VALUE_COLUMN;
		}
		return false;
	}

	@Override
	public void setValueAt(Object val, int row, int col) {
		if (settings != null && val != null) {
			if (col == VALUE_COLUMN) {
				if (settings.setSettingValue(row, val.toString()))
					valuesCache[row] = val.toString();
			} else {
				throw new IllegalArgumentException("Invalid column "+col);
			}
		}
	}
	
	public int getValueType(int row) {
		return settings.getSettingType(row);
	}
	
	public String[] getValueEnum(int row) {
		return settings.getSettingEnum(row);
	}
}
