package com.idt.devctrl;

public interface DevRegTableModel {
	boolean isBitCell(int row, int col);
	boolean mergeWithRightCell(int row, int col);
	boolean isMaskCell(int row, int col);
	boolean isModified(int row, int col);
	boolean isChanged(int row, int col);
	String getToolTip(int row, int col);
}
