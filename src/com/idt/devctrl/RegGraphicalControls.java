package com.idt.devctrl;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class RegGraphicalControls extends JPanel {
	private static final long serialVersionUID = -1633600894160995484L;
	
	private int register;
	private int value;
	
	public RegGraphicalControls(int reg) {
		register = reg;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	public int getRegister() {
		return register;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getMask() {
		return 0;
	}
	
	public void addToggleControls(int mask) {
		
	}
	
	public void addSlider(int mask) {
		
	}

	public void addComboBox(int mask) {
		
	}
	
	public void deleteControl(int mask) {
		
	}
}
