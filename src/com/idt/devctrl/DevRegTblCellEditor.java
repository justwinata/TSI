package com.idt.devctrl;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class DevRegTblCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = -1903516341447688235L;

	public DevRegTblCellEditor() {
		super(new JTextField());
	}

	@Override
	public Component getTableCellEditorComponent(JTable arg0, Object arg1,
			boolean arg2, int arg3, int arg4) {
		JTextField tf = (JTextField)super.getTableCellEditorComponent(arg0, arg1, arg2, arg3, arg4);
		tf.selectAll();
		return tf;
	}
	
}
