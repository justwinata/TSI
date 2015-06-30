package com.idt.devctrl;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class SettingsTable extends JTable {
	private static final long serialVersionUID = -851385318983640100L;

	SettingsTableModel settingsModel = null;
	JComboBox<String> cbEditor = null;
	
	public SettingsTable(SettingsTableModel settingsModel) {
		super(settingsModel);
		this.settingsModel = settingsModel;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		if (col == SettingsTableModel.VALUE_COLUMN) {
			switch (settingsModel.getValueType(row)) {
			case Settings.BOOL_TYPE:
				return new DefaultCellEditor(new JCheckBox());
			case Settings.ENUM_TYPE:
				if (cbEditor == null)
					cbEditor = new JComboBox<String>();
				cbEditor.removeAllItems();
				String[] values = settingsModel.getValueEnum(row);
				if (values != null) {
					for (String value: values) {
						cbEditor.addItem(value);
					}
				}
				return new DefaultCellEditor(cbEditor);
			case Settings.STRING_TYPE:
				break;
			}
		}
		return super.getCellEditor(row, col);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		if (col == SettingsTableModel.VALUE_COLUMN) {
			switch (settingsModel.getValueType(row)) {
			case Settings.BOOL_TYPE:
				return getDefaultRenderer(Boolean.class);
			case Settings.ENUM_TYPE:
			case Settings.STRING_TYPE:
				break;
			}
		}
		return super.getCellRenderer(row, col);
	}
	
}
