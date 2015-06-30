package com.idt.devctrl;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public final class CursorController {
	public final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public final static Cursor defaultCursor = Cursor.getDefaultCursor();

	private CursorController() {}

	public static ActionListener createListener(final Component component, final ActionListener clientActionListener) {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					component.setCursor(busyCursor);
					clientActionListener.actionPerformed(ae);
				} finally {
					component.setCursor(defaultCursor);
				}
			}
		};
		return actionListener;
	}
}
