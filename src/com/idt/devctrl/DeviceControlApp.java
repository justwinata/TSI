package com.idt.devctrl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.BoxLayout;

/**
 * @author Vitaliy Kulikov
 *
 */
public class DeviceControlApp {
	static final Color READ_ONLY_COLOR = new Color(238,238,238);
	protected static final int ALL_REGS_VIEW = 0;
	protected static final int DSP_REGS_VIEW = 1;
	protected static final int SEQUENCE_VIEW = 2;

	private JFrame appFrame = null;  //  @jve:decl-index=0:visual-constraint="-140,13"
	//Menu
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem readMenuItem = null;
	private JMenuItem writeMenuItem = null;
	private JMenuItem writeAllMenuItem = null;
	private JMenuItem saveMenuItem = null;
	
	//Dialogs
	private JDialog aboutDialog = null;  //  @jve:decl-index=0:visual-constraint="315,563"
	private JPanel aboutContentPane = null;
	private JLabel aboutVersionLabel = null;
	private FileNameExtensionFilter devDefFileFilter = new FileNameExtensionFilter("Device definition (*.devd)", "devd");
	private FileNameExtensionFilter regsFileFilter = new FileNameExtensionFilter("Device register values (*.regv,*.prst)", "regv", "prst"); //  @jve:decl-index=0:
	private FileNameExtensionFilter seqFileFilter = new FileNameExtensionFilter("Device registers sequence (*.rseq)", "rseq"); //SequenceFileFilter();  //  @jve:decl-index=0:
	private FileNameExtensionFilter indirRegFileFilter = new FileNameExtensionFilter("Internal registers (*.ireg) or DSP (*.dsps) values", "ireg", "dsps", "prst"); //  @jve:decl-index=0:
	private FileNameExtensionFilter indirRegSaveFileFilter = new FileNameExtensionFilter("Internal registers (*.ireg) values", "ireg"); //  @jve:decl-index=0:
	private FileNameExtensionFilter configFileFilter = new FileNameExtensionFilter("Internal and device register configuration (*.config)", "config"); //  @jve:decl-index=0:

	
	private JDialog settingsDialog = null;  //  @jve:decl-index=0:visual-constraint="17,558"
	private JPanel settingsContentPane = null;
	private JScrollPane settingsScrollPane = null;
	private JPanel dlgButtonPanel = null;

	//Views & controls containers
	private JPanel appContentPane = null;
	private JToolBar devToolBar = null;
	private JTabbedPane viewTabs = null;
	private JPanel ctrlRegsPanel = null;
	private JScrollPane regTableScrollPane = null;
	private JPanel dspRegsPanel = null;
	private JScrollPane dspRegsScrollPane = null;
	private JPanel regSeqPanel = null;
	private JScrollPane seqScrollPane = null;
	private JPanel statusPanel = null;
	private JToolBar regSequenceToolBar = null;
	private JToolBar dspRegsToolBar = null;
	
	//no plans to implement - remove?
	private JToolBar ctrlsToolBar = null;
	private JPanel controlsPanel = null;
	private JScrollPane ctrlsScrollPane = null;
	private JPanel ctrlsPanel = null;
	private JPanel ctrlsWidgPanel = null;
	private JToggleButton contWrTglBtn = null;
	private JTable regCtrlsTable = null;
	private RegCtrlsTableModel regCtrlsTableModel = null;
	private JToggleButton pageTglCtrlBtn = null;
	private JToggleButton adrTglCtrlBtn = null;
	private JToggleButton numTglCtrlBtn = null;
	private JToggleButton bitsTglCtrlBtn = null;
	private JButton addCtrlBtn = null;
	private JButton delCtrlBtn = null;
	private JButton moveUpCtrlBtn = null;
	private JButton moveDwnCtrlBtn = null;
	private JButton readSelCtrlBtn = null;
	private JButton writeSelCtrlBtn = null;
	private JButton autoFillCtrlBtn = null;
	
	//Views
	private JTable settingsTable = null;
	private SettingsTableModel settingsTableModel = null;
	private JTable ctrlRegsTable = null;
	private CtrlRegTableModel ctrlRegsTableModel = null;
	private I2CDeviceRowHeaderDataModel ctrlRegsRowHeaderModel = null;
	private JTable regViewTable = null;
	private RegisterViewTableModel regViewTableModel = null;
	private JTable regSeqTable = null;
	private RegSequenceTableModel regSeqTableModel;
	private JTable dspRegsTable = null;
	private DspRegTableModel dspRegsTableModel;
	//Controls
	private JComboBox<String> accessSelector = null;
	private JComboBox<String> devSelector = null;
	private JComboBox<String> pageSelector = null;
	private JButton settingsBtn = null;
	private JButton updSettingsButton = null;
	private JButton readRegsBtn = null;
	private JButton writeRegsBtn = null;
	private JButton loadBtn = null;
	private JButton saveBtn = null;
	private JButton saveConfigBtn = null;
	private JButton resetBtn = null;
	//Sequence tab controls
	private JButton addSeqItemBtn = null;
	private JButton delSeqItemBtn = null;
	private JButton moveUpSeqBtn = null;
	private JButton moveDwnSeqBtn = null;
	private JButton readSelSeqBtn = null;
	private JButton writeSelSeqBtn = null;
	private JButton autoFillSeqBtn = null;
	private JToggleButton pageTglSeqBtn = null;
	private JToggleButton adrTglSeqBtn = null;
	private JToggleButton numTglSeqBtn = null;
	private JToggleButton bitsTglSeqBtn = null;
	private JToggleButton maskTglSeqBtn = null;
	private JToggleButton delayTglSeqBtn = null;
	private JToggleButton editMskTglSeqBtn = null;
	//DSP tab controls
	private JComboBox<String> dspPgSelector = null;
	private JToggleButton adrTglDspRegBtn = null;
	private JToggleButton numTglDspRegBtn = null;
	private JToggleButton dvalTglDspRegBtn = null;
	private JToggleButton hvalTglDspRegBtn = null;
	private JToggleButton bitsTglDspRegBtn = null;
	
	private JCheckBox modifiedChBx = null;
	private JCheckBox decimalTblFormatChkBox = null;
	private JLabel deviceStatus = null;
	private JLabel accessStatus = null;
	//Control labels
	private JLabel accessLabel = null;
	private JLabel devLabel = null;

	//HW controls
	private I2CDevice currDevice = null;
	private HashMap<String,I2CDevice> devices = new HashMap<String,I2CDevice>();
	/**
	 * This method initializes appFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getAppFrame() {
		if (appFrame == null) {
			appFrame = new JFrame();
			appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			appFrame.setJMenuBar(getAppMenuBar());
			appFrame.setSize(800, 535);
			appFrame.setMinimumSize(new Dimension(800, 296));
			appFrame.setPreferredSize(new Dimension(800, 535));
			appFrame.setContentPane(getAppContentPane());
			appFrame.setTitle("Device Registers");
			appFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent we) {
					closeApp();
				}
			});
			
			ctrlRegsTable.getSelectionModel().addListSelectionListener(regViewTableModel);
			ctrlRegsTable.getColumnModel().addColumnModelListener(regViewTableModel);
			
			devSelector.setSelectedIndex(0);
		}
		return appFrame;
	}
	
	private void updateDeviceViews() {
		ctrlRegsTableModel.fireTableDataChanged();
		dspRegsTableModel.fireTableDataChanged();
		regViewTableModel.fireTableDataChanged();
		regSeqTableModel.fireTableDataChanged();

		deviceStatus.setText(currDevice.getStatus());
		accessStatus.setText(currDevice.getAccessStatus());
	}
	
	private void toggleDspTableColumns() {
		int charWidth = dspRegsTable.getFontMetrics(dspRegsTable.getFont()).charWidth('F');
		int valSz = ((currDevice != null && currDevice.getDspRegister(0, currDevice.getCurrentDspPage()) != null) ? Math.max(8,(currDevice.getDspRegister(0, currDevice.getCurrentDspPage()).getBitDepth()+3)/4) : 8);
		if (getNumTglDspRegBtn().isSelected()) {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setMaxWidth(charWidth * 7);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setMinWidth(charWidth * 7);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setPreferredWidth(charWidth * 7);
		} else {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setMinWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setMaxWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.REG_NUM_COLUMN).setPreferredWidth(0);
		}
		if (getAdrTglDspRegBtn().isSelected()) {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setMaxWidth(charWidth * 7);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setMinWidth(charWidth * 7);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setPreferredWidth(charWidth * 7);
		} else {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setMinWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setMaxWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.ADDR_COLUMN).setPreferredWidth(0);
		}
		if (getDValTglDspRegBtn().isSelected()) {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setMaxWidth(charWidth * (valSz+4));
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setMinWidth(charWidth * (valSz+4));
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setPreferredWidth(charWidth * (valSz+4));
		} else {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setMinWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setMaxWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.DVALUE_COLUMN).setPreferredWidth(0);
		}
		if (getHValTglDspRegBtn().isSelected()) {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setMaxWidth(charWidth * (valSz+2));
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setMinWidth(charWidth * (valSz+2));
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setPreferredWidth(charWidth * (valSz+2));
		} else {
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setMinWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setMaxWidth(0);
			dspRegsTable.getColumnModel().getColumn(DspRegTableModel.HVALUE_COLUMN).setPreferredWidth(0);
		}
		for (int i = 0; i <= dspRegsTable.getColumnCount(); i++) {
			if (dspRegsTableModel.isBitColumn(i)) {
				if (getBitsTglDspRegBtn().isSelected()) {
					dspRegsTable.getColumnModel().getColumn( i ).setMaxWidth(charWidth * 3);
					dspRegsTable.getColumnModel().getColumn( i ).setMinWidth(charWidth * 3);
					dspRegsTable.getColumnModel().getColumn( i ).setPreferredWidth(charWidth * 3);
				} else {
					dspRegsTable.getColumnModel().getColumn( i ).setMinWidth(0);
					dspRegsTable.getColumnModel().getColumn( i ).setMaxWidth(0);
					dspRegsTable.getColumnModel().getColumn( i ).setPreferredWidth(0);
				}
			}
		}
	}
	
	private void setRegViewTableColumns() {
		int charWidth = regViewTable.getFontMetrics(regViewTable.getFont()).charWidth('0');
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.REG_NUM_COLUMN).setMaxWidth(charWidth * 7);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.REG_NUM_COLUMN).setMinWidth(charWidth * 7);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.REG_NUM_COLUMN).setPreferredWidth(charWidth * 7);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.ADDR_COLUMN).setMaxWidth(charWidth * 7);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.ADDR_COLUMN).setMinWidth(charWidth * 7);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.ADDR_COLUMN).setPreferredWidth(charWidth * 7);
		int valSz = ((currDevice != null && currDevice.getRegister(0, 0) != null) ? Math.max(4,(currDevice.getRegister(0, 0).getBitDepth()+3)/4) : 4);
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.VALUE_COLUMN).setMaxWidth(charWidth * (valSz+2));
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.VALUE_COLUMN).setMinWidth(charWidth * (valSz+2));
		regViewTable.getColumnModel().getColumn(RegisterViewTableModel.VALUE_COLUMN).setPreferredWidth(charWidth * (valSz+2));
		for (int i = 0; i <= regViewTable.getColumnCount(); i++) {
			if (regViewTableModel.isBitColumn(i)) {
				regViewTable.getColumnModel().getColumn( i ).setMaxWidth(charWidth * 3);
				regViewTable.getColumnModel().getColumn( i ).setMinWidth(charWidth * 3);
				regViewTable.getColumnModel().getColumn( i ).setPreferredWidth(charWidth * 3);
			}
		}
	}
	
	private void toggleSeqTableColumns() {
		int charWidth = regSeqTable.getFontMetrics(regSeqTable.getFont()).charWidth('0');
		if (getSeqPageTglSeqBtn().isSelected()) {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setMaxWidth(charWidth * 7);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setMinWidth(charWidth * 7);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setPreferredWidth(charWidth * 7);
		} else {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setMinWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setMaxWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN).setPreferredWidth(0);
		}
		if (getSeqNumTglBtn().isSelected()) {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setMaxWidth(charWidth * 7);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setMinWidth(charWidth * 7);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setPreferredWidth(charWidth * 7);
		} else {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setMinWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setMaxWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN).setPreferredWidth(0);
		}
		if (getSeqAdrTglBtn().isSelected()) {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setMaxWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setMinWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setPreferredWidth(charWidth * 8);
		} else {
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setMinWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setMaxWidth(0);
			regSeqTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN).setPreferredWidth(0);
		}
		if (getSeqMaskTglBtn().isSelected()) {
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setMaxWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setMinWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setPreferredWidth(charWidth * 8);
		} else {
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setMinWidth(0);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setMaxWidth(0);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getMaskColumn()).setPreferredWidth(0);
		}
		if (getSeqDelayTglBtn().isSelected()) {
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setMaxWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setMinWidth(charWidth * 8);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setPreferredWidth(charWidth * 8);
		} else {
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setMinWidth(0);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setMaxWidth(0);
			regSeqTable.getColumnModel().getColumn(regSeqTableModel.getDelayColumn()).setPreferredWidth(0);
		}
		for (int i = 0; i <= regSeqTable.getColumnCount(); i++) {
			if (regSeqTableModel.isBitColumn(i)) {
				if (getSeqBitsTglBtn().isSelected()) {
					regSeqTable.getColumnModel().getColumn( i ).setMaxWidth(charWidth * 3);
					regSeqTable.getColumnModel().getColumn( i ).setMinWidth(charWidth * 3);
					regSeqTable.getColumnModel().getColumn( i ).setPreferredWidth(charWidth * 3);
				} else {
					regSeqTable.getColumnModel().getColumn( i ).setMinWidth(0);
					regSeqTable.getColumnModel().getColumn( i ).setMaxWidth(0);
					regSeqTable.getColumnModel().getColumn( i ).setPreferredWidth(0);
				}
			}
		}
	}
	
	private void setPageSelector() {
		I2CDevice dev = getDevice();
		int pgCnt = dev.getPageCount();
		pageSelector.setEnabled(false);
		pageSelector.removeAllItems();
		for (int i = 0; i < pgCnt; i++)
			pageSelector.addItem(String.valueOf(i));
		if (pgCnt > 1) {
			pageSelector.setSelectedIndex(dev.getCurrentPage());
			pageSelector.setEnabled(true);
		}
	}
	
	private void setDspPageSelector() {
		I2CDevice dev = getDevice();
		int pgCnt = dev.getDspPageCount();
		dspPgSelector.setEnabled(false);
		dspPgSelector.removeAllItems();
		for (int i = 0; i < pgCnt; i++)
			dspPgSelector.addItem(dev.getDspPageName(i));
		if (pgCnt > 1) {
			dspPgSelector.setSelectedIndex(dev.getCurrentDspPage());
			dspPgSelector.setEnabled(true);
		}
	}
	
	private I2CDevice getDevice() {
		if (currDevice == null) {
			String devName = devSelector.getSelectedItem().toString();
			currDevice = devices.get(devName);
			if (currDevice == null) {
				currDevice = new I2CDevice(devName);
				currDevice.setAccessMethod(accessSelector.getSelectedItem().toString());
				devices.put(currDevice.getName(), currDevice);
			}
		}
		return currDevice;
	}
	
	private void setDevice() {
		String devName = devSelector.getSelectedItem().toString();
		if (currDevice != null && currDevice.getName().equalsIgnoreCase(devName))
			return;
		if (currDevice != null) {
			currDevice.close();
			currDevice = null;
		}
		I2CDevice dev = getDevice(); 

		setPageSelector();
		setDspPageSelector();

		ctrlRegsTableModel.setDevice(dev);
		dspRegsTableModel.setDevice(dev);
		regViewTableModel.setDevice(dev);
		regSeqTableModel.setDevice(dev);
		//regCtrlsTableModel.setDevice(dev);
		settingsTableModel.setDevice(dev);
		
		deviceStatus.setText(dev.getStatus());
		accessStatus.setText(dev.getAccessStatus());
		if (dev != null && dev.isResetSupported()) {
			resetBtn.setEnabled(true);
		} else {
			resetBtn.setEnabled(false);
		}
		
		setRegViewTableColumns();

		if (dev.getPageCount() > 1) {
			getSeqPageTglSeqBtn().setSelected(true);
		} else {
			getSeqPageTglSeqBtn().setSelected(false);
		}
		getSeqBitsTglBtn().setSelected(false);
		toggleSeqTableColumns();

		getNumTglDspRegBtn().setSelected(false);
		getDValTglDspRegBtn().setSelected(false);
		getBitsTglDspRegBtn().setSelected(false);
		toggleDspTableColumns();
	}
	
	private void resetDevice() {
		if (currDevice != null) {
			currDevice.close();
			currDevice = null;
		}
		setDevice();
	}
	
	private void setDeviceAccess() {
		if (getDevice().setAccessMethod(accessSelector.getSelectedItem().toString())) {
			updateDeviceViews();
		}
	}
	
	private void showDeviceErrorMsg(String msg) {
		JOptionPane.showMessageDialog(appFrame, msg, "Device error", JOptionPane.ERROR_MESSAGE);
	}

	private boolean showDeviceWarningMsg(String msg) {
		return JOptionPane.showConfirmDialog(appFrame, msg, "Device warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
			appMenuBar.add(getEditMenu());
			appMenuBar.add(getHelpMenu());
		}
		return appMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu();
			editMenu.setText("Edit");
			editMenu.add(getReadMenuItem());
			editMenu.add(getWriteMenuItem());
			editMenu.add(getWriteAllMenuItem());
		}
		return editMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeApp();
				}
			});
		}
		return exitMenuItem;
	}

	protected void closeApp() {
		for(I2CDevice dev: devices.values()) {
			if (dev.isModified(-1) || regSeqTableModel.isModified() /*|| regCtrlsTableModel.isModified()*/) {
				if (!showDeviceWarningMsg(dev.getName()+"- all unsaved changes will be lost"))
					return;
			}
			if (!dev.resetToPage0())
				System.out.println("Failed return to page 0\n");
			dev.close();
		}
		System.exit(0);
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog aboutDialog = getAboutDialog();
					aboutDialog.pack();
					Point loc = getAppFrame().getLocation();
					loc.translate(20, 20);
					aboutDialog.setLocation(loc);
					aboutDialog.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(getAppFrame(), true);
			aboutDialog.setTitle("About");
			aboutDialog.setContentPane(getAboutContentPane());
		}
		return aboutDialog;
	}

	/**
	 * This method initializes aboutContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAboutContentPane() {
		if (aboutContentPane == null) {
			aboutContentPane = new JPanel();
			aboutContentPane.setLayout(new BorderLayout());
			aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
		}
		return aboutContentPane;
	}

	/**
	 * This method initializes aboutVersionLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getAboutVersionLabel() {
		if (aboutVersionLabel == null) {
			aboutVersionLabel = new JLabel();
			aboutVersionLabel.setText("Version 1.0");
			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return aboutVersionLabel;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getReadMenuItem() {
		if (readMenuItem == null) {
			readMenuItem = new JMenuItem();
			readMenuItem.setText("Read");
			readMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
					Event.CTRL_MASK, true));
		}
		return readMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getWriteMenuItem() {
		if (writeMenuItem == null) {
			writeMenuItem = new JMenuItem();
			writeMenuItem.setText("Write");
			writeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					Event.CTRL_MASK, true));
		}
		return writeMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getWriteAllMenuItem() {
		if (writeAllMenuItem == null) {
			writeAllMenuItem = new JMenuItem();
			writeAllMenuItem.setText("Write all");
			writeAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
					Event.CTRL_MASK, true));
		}
		return writeAllMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
		}
		return saveMenuItem;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAppContentPane() {
		if (appContentPane == null) {
			appContentPane = new JPanel();
			appContentPane.setLayout(new BorderLayout());
			appContentPane.add(getDevToolBar(), BorderLayout.NORTH);
			appContentPane.add(getViewTabs(), BorderLayout.CENTER);
			appContentPane.add(getStatusPanel(), BorderLayout.SOUTH);
		}
		return appContentPane;
	}

	/**
	 * This method initializes devToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getDevToolBar() {
		if (devToolBar == null) {
			devLabel = new JLabel();
			devLabel.setText("Device: ");
			accessLabel = new JLabel();
			accessLabel.setText("Access: ");
			devToolBar = new JToolBar();
			devToolBar.setFloatable(false);
			devToolBar.setMargin(new Insets(2, 2, 2, 2));
			devToolBar.setPreferredSize(new Dimension(698, 32));
			devToolBar.add(devLabel);
			devToolBar.add(getDevSelector());
			devToolBar.add(Box.createHorizontalStrut(4));
			devToolBar.add(accessLabel);
			devToolBar.add(getAccessSelector());
			devToolBar.add(Box.createHorizontalStrut(4));
			devToolBar.add(getSettingsBtn());
			devToolBar.add(Box.createHorizontalStrut(12));
			devToolBar.add(getReadRegsBtn());
			devToolBar.add(getWriteRegsBtn());
			devToolBar.add(Box.createHorizontalStrut(12));
			devToolBar.add(getLoadBtn());
			devToolBar.add(getSaveBtn());
			devToolBar.add(getModifiedChBx());
			devToolBar.add(Box.createHorizontalStrut(12));
			devToolBar.add(getDecimalTblFormat());
			devToolBar.add(Box.createGlue());
			devToolBar.add(getResetBtn());
		}
		return devToolBar;
	}

	/**
	 * This method initializes accessSelector	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox<String> getAccessSelector() {
		if (accessSelector == null) {
			accessSelector = new JComboBox<String>();
			accessSelector.addItem(I2CDeviceAccess.FTDICHIP);
			accessSelector.setToolTipText("Device access method");
			accessSelector.setPreferredSize(new Dimension(48, 24));
			accessSelector.setSize(new Dimension(48, 24));
			int charWidth = accessSelector.getFontMetrics(accessSelector.getFont()).charWidth('D');
			Dimension dim = accessSelector.getMaximumSize();
			dim.width = charWidth * 7;
			accessSelector.setMaximumSize(dim);
			accessSelector.setMinimumSize(dim);
			accessSelector.setPreferredSize(dim);
			accessSelector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setDeviceAccess();
				}
			});
		}
		return accessSelector;
	}

	/**
	 * This method initializes devSelector	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox<String> getDevSelector() {
		if (devSelector == null) {
			devSelector = new JComboBox<String>();
			//devSelector.addItem("Storm");
			//devSelector.addItem("Tiny DSP");
			devSelector.addItem("Wookie");
			//devSelector.addItem("Cyrus");
			devSelector.addItem("Unknown");
			devSelector.setToolTipText("Name of the device to access");
			devSelector.setPreferredSize(new Dimension(96, 24));
			devSelector.setSize(new Dimension(96, 24));
			int charWidth = devSelector.getFontMetrics(devSelector.getFont()).charWidth('D');
			Dimension dim = devSelector.getMaximumSize();
			dim.width = charWidth * 12;
			devSelector.setMaximumSize(dim);
			devSelector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setDevice();
				}
			});
		}
		return devSelector;
	}

	/**
	 * This method initializes readRegsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getReadRegsBtn() {
		if (readRegsBtn == null) {
			readRegsBtn = new JButton();
			readRegsBtn.setText("Read");
			readRegsBtn.setToolTipText("Read device registers");
			readRegsBtn.addActionListener(CursorController.createListener(getAppFrame(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						if (viewTabs.getSelectedIndex() == ALL_REGS_VIEW) { 
							if (currDevice.isModified(pageSelector.getSelectedIndex())) {
								if (!showDeviceWarningMsg("All current changes will be lost")) {
									return;
								}
							}
							if (!currDevice.read(pageSelector.getSelectedIndex())) {
								showDeviceErrorMsg(currDevice.getErrorMsg());
							}
							updateDeviceViews();
						} else if (viewTabs.getSelectedIndex() == DSP_REGS_VIEW) {
							/*if (device.isModified()) {
								if (!showDeviceWarningMsg("All current changes will be lost")) {
									return;
								}
							}*/
							if (!currDevice.readDspRegisters(dspPgSelector.getSelectedIndex())) {
								showDeviceErrorMsg(dspRegsTableModel.getErrorMsg());
							}
							updateDeviceViews();
						} else if (viewTabs.getSelectedIndex() == SEQUENCE_VIEW) {
							if (regSeqTableModel.isModified()) {
								if (!showDeviceWarningMsg("All current changes will be lost")) {
									return;
								}
							}
							if (!regSeqTableModel.readAll()) {
								showDeviceErrorMsg(regSeqTableModel.getErrorMsg());
							}
						}
					}
				}
			}));
		}
		return readRegsBtn;
	}

	/**
	 * This method initializes writeRegsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getWriteRegsBtn() {
		if (writeRegsBtn == null) {
			writeRegsBtn = new JButton();
			writeRegsBtn.setText("Write");
			writeRegsBtn.setToolTipText("Write registers");
			writeRegsBtn.addActionListener(CursorController.createListener(getAppFrame(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						if (viewTabs.getSelectedIndex() == ALL_REGS_VIEW) { 
							if (!currDevice.write(pageSelector.getSelectedIndex(), modifiedChBx.isSelected())) {
								showDeviceErrorMsg(currDevice.getErrorMsg());
							}
							updateDeviceViews();
						} else if (viewTabs.getSelectedIndex() == DSP_REGS_VIEW) {
							if (!currDevice.writeDspRegisters(dspPgSelector.getSelectedIndex(), modifiedChBx.isSelected())) {
								showDeviceErrorMsg(dspRegsTableModel.getErrorMsg());
							}
							updateDeviceViews();
						} else if (viewTabs.getSelectedIndex() == SEQUENCE_VIEW) {
							if (!regSeqTableModel.writeAll()) {
								showDeviceErrorMsg(regSeqTableModel.getErrorMsg());
							}
						}
					}
				}
			}));
		}
		return writeRegsBtn;
	}

	/**
	 * This method initializes loadBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getLoadBtn() {
		if (loadBtn == null) {
			loadBtn = new JButton();
			loadBtn.setText("Load...");
			loadBtn.setToolTipText("Load registers' values from file");
			loadBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
						if (viewTabs.getSelectedIndex() == ALL_REGS_VIEW) { 
							if (currDevice.isModified(pageSelector.getSelectedIndex())) {
								if (!showDeviceWarningMsg("All current changes will be lost")) {
									return;
								}
							}
							fc.setFileFilter(regsFileFilter);
							int status = fc.showOpenDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
					            File file = fc.getSelectedFile();
					            if (file.getName().endsWith(".prst")) {
					            	if (!currDevice.loadPreset(file))
										showDeviceErrorMsg(currDevice.getErrorMsg());
					            } else {
									int page = currDevice.loadFromFile(file);
						            if (page >= 0) {
						            	pageSelector.setSelectedIndex(page);
						            } else {
										showDeviceErrorMsg(currDevice.getErrorMsg());
						            }
					            }
				            	updateDeviceViews();
							}
						} else if (viewTabs.getSelectedIndex() == SEQUENCE_VIEW) {
							fc.setFileFilter(seqFileFilter);
							int status = fc.showOpenDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
								ListSelectionModel lsm = regSeqTable.getSelectionModel();
								int selRow = lsm.getMinSelectionIndex();
								if (selRow < 0)
									selRow = 0;
					            File file = fc.getSelectedFile();
					            selRow = regSeqTableModel.loadFromFile(file, selRow);
					            if (selRow < 0) {
									showDeviceErrorMsg(regSeqTableModel.getErrorMsg());
					            } else {
					            	lsm.setSelectionInterval(selRow, selRow);
					            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(selRow, 0, true));
					            }
							}
						} else if (viewTabs.getSelectedIndex() == DSP_REGS_VIEW) {
							fc.setFileFilter(indirRegFileFilter);
							int status = fc.showOpenDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
					            File file = fc.getSelectedFile();
					            if (file.getName().endsWith(".prst")) {
					            	if (!currDevice.loadPreset(file))
										showDeviceErrorMsg(currDevice.getErrorMsg());
					            	updateDeviceViews();
					            } else {
					            	int res = dspRegsTableModel.loadFromFile(file);
						            if (res < 0) {
										showDeviceErrorMsg(file.getName()+" file is either corrupted or not for this chip");
						            }
					            }
							}
						}
					} 
				}
			});
		}
		return loadBtn;
	}

	/**
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton();
			saveBtn.setText("Save...");
			saveBtn.setToolTipText("Save registers' values to file");
			saveBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
						if (viewTabs.getSelectedIndex() == ALL_REGS_VIEW) { 
							fc.setFileFilter(regsFileFilter);
							int status = fc.showSaveDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
					            File file = fc.getSelectedFile();
					            String[] ext = regsFileFilter.getExtensions();
					            if (ext.length > 0) {
					            	String fext = "."+ext[0];
					            	if (!file.getAbsolutePath().endsWith(fext)) {
					            		file = new File(file.getAbsolutePath() + fext);
					            	}
					            }
					            if (file.exists()) {
					            	if (!showDeviceWarningMsg("This will overwrite existing file")) {
					            		return;
					            	}
					            }
					            if (!currDevice.saveToFile(pageSelector.getSelectedIndex(), file, modifiedChBx.isSelected())) {
					            	showDeviceErrorMsg("Saving registers to file failed!");
					            }
							}
						} else if (viewTabs.getSelectedIndex() == SEQUENCE_VIEW) {
							fc.setFileFilter(seqFileFilter);
							int status = fc.showSaveDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
					            File file = fc.getSelectedFile();
					            String[] ext = seqFileFilter.getExtensions();
					            if (ext.length > 0) {
					            	String fext = "."+ext[0];
					            	if (!file.getAbsolutePath().endsWith(fext)) {
					            		file = new File(file.getAbsolutePath() + fext);
					            	}
					            }
					            if (file.exists()) {
					            	if (!showDeviceWarningMsg("This will overwrite existing file")) {
					            		return;
					            	}
					            }
					            if (!regSeqTableModel.saveToFile(file)) {
					            	showDeviceErrorMsg("Saving sequence to file failed!");
					            }
							}
						} else if (viewTabs.getSelectedIndex() == DSP_REGS_VIEW) {
							fc.setFileFilter(indirRegSaveFileFilter);
							int status = fc.showSaveDialog(appFrame);
							if (status == JFileChooser.APPROVE_OPTION) {
					            File file = fc.getSelectedFile();
					            String[] ext = indirRegSaveFileFilter.getExtensions();
					            if (ext.length > 0) {
					            	String fext = "."+ext[0];
					            	if (!file.getAbsolutePath().endsWith(fext)) {
					            		file = new File(file.getAbsolutePath() + fext);
					            	}
					            }
					            if (file.exists()) {
					            	if (!showDeviceWarningMsg("This will overwrite existing file")) {
					            		return;
					            	}
					            }
					            if (!dspRegsTableModel.saveToFile(file)) {
					            	showDeviceErrorMsg("Saving internal registers to file failed!");
					            }
							}
						}
					}
				}
			});
		}
		return saveBtn;
	}

	/**
	 * This method initializes saveConfigBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveConfigBtn() {
		if (saveConfigBtn == null) {
			saveConfigBtn = new JButton();
			saveConfigBtn.setText("Save to config...");
			saveConfigBtn.setToolTipText("Save registers' values to file");
			saveConfigBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
						fc.setFileFilter(configFileFilter);
						int status = fc.showSaveDialog(appFrame);
						if (status == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String[] ext = configFileFilter.getExtensions();
							if (ext.length > 0) {
								String fext = "." + ext[0];
								if (!file.getAbsolutePath().endsWith(fext)) {
									file = new File(file.getAbsolutePath() + fext);
								}
							}
							if (file.exists()) {
								if(!showDeviceWarningMsg("This will overwrite existing file")) {
									return;
								}
							}
							if (!dspRegsTableModel.saveToFile(file)) {
								showDeviceErrorMsg("Saving internal registers to file failed!");
							}
	    				    if (!currDevice.saveToFile(pageSelector.getSelectedIndex(), file, modifiedChBx.isSelected())) {
	 				       		showDeviceErrorMsg("Saving registers to file failed!");
	        				}					
	        			}
					}
				}
			});
		}
		return saveConfigBtn;
	}
	/**
	 * This method initializes viewTabs	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getViewTabs() {
		if (viewTabs == null) {
			viewTabs = new JTabbedPane();
			viewTabs.setBorder(BorderFactory.createLineBorder(Color.cyan, 2));
			viewTabs.addTab("Control", null, getCtrlRegsPanel(), null);
			viewTabs.addTab("DSP", null, getDspRegsPanel(), null);
			viewTabs.addTab("Sequence", null, getRegSeqPanel(), null);
			//viewTabs.addTab("Controls", null, getControlsPanel(), null);
		}
		return viewTabs;
	}

	/**
	 * This method initializes allRegsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCtrlRegsPanel() {
		if (ctrlRegsPanel == null) {
			ctrlRegsPanel = new JPanel();
			ctrlRegsPanel.setLayout(new BorderLayout());
			ctrlRegsPanel.add(getRegTableScrollPane(), BorderLayout.CENTER);
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			panel.add(getRegViewTable().getTableHeader(), BorderLayout.NORTH);
			panel.add(getRegViewTable(), BorderLayout.SOUTH);
			ctrlRegsPanel.add(panel, BorderLayout.SOUTH);
		}
		return ctrlRegsPanel;
	}

	/**
	 * This method initializes regTableScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getRegTableScrollPane() {
		if (regTableScrollPane == null) {
			regTableScrollPane = new JScrollPane();
			regTableScrollPane.setViewportView(getAllRegsTable());
			JTable table = new JTable() {
				private static final long serialVersionUID = 5218840116884006539L;

				public boolean isFocusable() {
					return false;
				}
			};
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			ctrlRegsRowHeaderModel = new I2CDeviceRowHeaderDataModel(); 
			table.setModel(ctrlRegsRowHeaderModel);
			table.setIntercellSpacing(new Dimension(1, 1));
			table.setRowHeight(20);
			table.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			table.setRowSelectionAllowed(false);
			table.getColumnModel().getColumn(0).setPreferredWidth(table.getFontMetrics(table.getFont()).charWidth('0') * 8);
			table.setBackground(new Color(238,238,238));
			table.setDefaultRenderer(String.class, new CenterAlignedStringCellRenderer());
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(table, BorderLayout.CENTER);
			regTableScrollPane.setRowHeaderView(panel);
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			JLabel label = new JLabel("Pg:");
			label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			panel.add(label, BorderLayout.CENTER);
			pageSelector = new JComboBox<String>();
			pageSelector.setToolTipText("Current page");
			pageSelector.setEditable(false);
			panel.add(pageSelector, BorderLayout.EAST);
			regTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);
			pageSelector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					if (pageSelector.isEnabled()) {
						int page = pageSelector.getSelectedIndex();
						if (page >= 0) {
							ctrlRegsTableModel.setPage(page);
							regViewTableModel.setPage(page);
						}
					}
				}
			});
		};
		return regTableScrollPane;
	}

	/**
	 * This method initializes allRegsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getAllRegsTable() {
		if (ctrlRegsTable == null) {
			ctrlRegsTable = new DevRegTable();
			ctrlRegsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			ctrlRegsTable.setShowGrid(true);
			ctrlRegsTableModel = new CtrlRegTableModel();
			ctrlRegsTable.setModel(ctrlRegsTableModel);
			ctrlRegsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ctrlRegsTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			ctrlRegsTable.setCellSelectionEnabled(true);
			ctrlRegsTable.setRowHeight(20);
			ctrlRegsTable.setIntercellSpacing(new Dimension(1, 1));
			ctrlRegsTable.getTableHeader().setReorderingAllowed(false);
			ctrlRegsTable.getTableHeader().setResizingAllowed(false);
			ctrlRegsTable.getTableHeader().setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			ctrlRegsTable.setDefaultRenderer(Object.class, new DevRegTblCellRenderer());
			ctrlRegsTable.setDefaultEditor(String.class, new DevRegTblCellEditor());
			int charWidth = ctrlRegsTable.getFontMetrics(ctrlRegsTable.getFont()).charWidth('0');
			for (int i = 0; i < ctrlRegsTable.getColumnCount(); i++) {
				ctrlRegsTable.getColumnModel().getColumn(i).setPreferredWidth(charWidth * 6);
				ctrlRegsTable.getColumnModel().getColumn(i).setMinWidth(charWidth * 4);
			}
		}
		return ctrlRegsTable;
	}

	/**
	 * This method initializes settingsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSettingsBtn() {
		if (settingsBtn == null) {
			settingsBtn = new JButton();
			settingsBtn.setText("Settings...");
			settingsBtn.setToolTipText("Communication settings");
			settingsTableModel = new SettingsTableModel();
			settingsBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog d = getSettingsDialog(); 
					settingsTableModel.reset();
					d.setVisible(true);
				}
			});
		}
		return settingsBtn;
	}

	/**
	 * This method initializes regSeqPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRegSeqPanel() {
		if (regSeqPanel == null) {
			regSeqPanel = new JPanel();
			regSeqPanel.setLayout(new BorderLayout());
			regSeqPanel.add(getSeqScrollPane(), BorderLayout.CENTER);
			regSeqPanel.add(getRegSequenceToolBar(), BorderLayout.WEST);
		}
		return regSeqPanel;
	}

	/**
	 * This method initializes seqScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getSeqScrollPane() {
		if (seqScrollPane == null) {
			seqScrollPane = new JScrollPane();
			seqScrollPane.setViewportView(getRegSeqTable());
		}
		return seqScrollPane;
	}

	/**
	 * This method initializes regSeqTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getRegSeqTable() {
		if (regSeqTable == null) {
			regSeqTable = new RegSeqTable();
			regSeqTableModel = new RegSequenceTableModel();
			regSeqTable.setModel(regSeqTableModel);
			regSeqTable.setRowHeight(20);
			regSeqTable.setShowGrid(false);
			regSeqTable.setIntercellSpacing(new Dimension(0, 0));
			regSeqTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			regSeqTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			regSeqTable.getTableHeader().setReorderingAllowed(false);
			//regSeqTable.getTableHeader().setResizingAllowed(false);
			regSeqTable.getTableHeader().setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			regSeqTable.setDefaultRenderer(String.class, new RegViewTblCellRenderer());
			regSeqTable.setDefaultEditor(String.class, new DevRegTblCellEditor());
		}
		return regSeqTable;
	}

	/**
	 * This method initializes regViewTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getRegViewTable() {
		if (regViewTable == null) {
			regViewTable = new JTable();
			regViewTableModel = new RegisterViewTableModel();
			regViewTable.setModel(regViewTableModel);
			regViewTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
			regViewTable.setIntercellSpacing(new Dimension(0, 0));
			regViewTable.setRowHeight(20);
			regViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			regViewTable.setCellSelectionEnabled(true);
			regViewTable.getTableHeader().setReorderingAllowed(false);
			regViewTable.getTableHeader().setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			regViewTable.setDefaultRenderer(String.class, new RegViewTblCellRenderer());
			regViewTable.setDefaultEditor(String.class, new DevRegTblCellEditor());
		}
		return regViewTable;
	}

	/**
	 * This method initializes modifiedChBx	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getModifiedChBx() {
		if (modifiedChBx == null) {
			modifiedChBx = new JCheckBox();
			modifiedChBx.setText("Modified");
			modifiedChBx.setToolTipText("Save only modified registers");
			modifiedChBx.setSelected(true);
		}
		return modifiedChBx;
	}
	
	/**
	 * This method initializes showBitNamesChBx	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDecimalTblFormat() {
		if (decimalTblFormatChkBox == null) {
			decimalTblFormatChkBox = new JCheckBox();
			decimalTblFormatChkBox.setText("Decimal reg #s");
			decimalTblFormatChkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ctrlRegsTableModel.setDecimalTableFormat(decimalTblFormatChkBox.isSelected());
					ctrlRegsRowHeaderModel.fireTableDataChanged();
				}
			});
		}
		return decimalTblFormatChkBox;
	}

	/**
	 * This method initializes stngsDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getSettingsDialog() {
		if (settingsDialog == null) {
			settingsDialog = new JDialog(getAppFrame());
			settingsDialog.setSize(new Dimension(237, 166));
			settingsDialog.setTitle("Setttings");
			settingsDialog.setModal(true);
			settingsDialog.setLocation(new Point(60, 70));
			settingsDialog.setContentPane(getSettingsContentPane());
		}
		return settingsDialog;
	}

	/**
	 * This method initializes settingsDialog	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSettingsContentPane() {
		if (settingsContentPane == null) {
			settingsContentPane = new JPanel();
			settingsContentPane.setLayout(new BorderLayout());
			settingsContentPane.add(getSettingsScrollPane(), BorderLayout.CENTER);
			settingsContentPane.add(getDlgButtonPanel(), BorderLayout.SOUTH);
		}
		return settingsContentPane;
	}

	/**
	 * This method initializes settingsScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getSettingsScrollPane() {
		if (settingsScrollPane == null) {
			settingsScrollPane = new JScrollPane();
			settingsScrollPane.setViewportView(getSettingsTable());
		}
		return settingsScrollPane;
	}

	/**
	 * This method initializes settingsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getSettingsTable() {
		if (settingsTable == null) {
			settingsTable = new SettingsTable(settingsTableModel);
			settingsTable.setRowHeight(18);
			settingsTable.getTableHeader().setReorderingAllowed(false);
		}
		return settingsTable;
	}

	/**
	 * This method initializes dlgButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDlgButtonPanel() {
		if (dlgButtonPanel == null) {
			dlgButtonPanel = new JPanel();
			dlgButtonPanel.setLayout(new GridBagLayout());
			dlgButtonPanel.add(getCloseSettingsDlgButton(), new GridBagConstraints());
		}
		return dlgButtonPanel;
	}

	/**
	 * This method initializes closeSettingsDlgButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCloseSettingsDlgButton() {
		if (updSettingsButton == null) {
			updSettingsButton = new JButton();
			updSettingsButton.setText("Update");
			updSettingsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					TableCellEditor tce = settingsTable.getCellEditor();
					if (tce != null)
						tce.stopCellEditing();
					settingsTableModel.update();
					settingsDialog.setVisible(false);
					resetDevice();
				}
			});
		}
		return updSettingsButton;
	}

	/**
	 * This method initializes resetBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getResetBtn() {
		if (resetBtn == null) {
			resetBtn = new JButton();
			resetBtn.setText("Reset");
			resetBtn.setToolTipText("Reset HW");
			resetBtn.setBackground(new Color(255, 192, 192));
			resetBtn.addActionListener(CursorController.createListener(getAppFrame(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currDevice != null) {
						if (showDeviceWarningMsg("Device will be reset and may stop responding!")) {
							if (!currDevice.resetHW()) {
								showDeviceErrorMsg(currDevice.getErrorMsg());
							}
							pageSelector.setSelectedIndex(0);
							updateDeviceViews();
						}
					}
				}
			}));
		}
		return resetBtn;
	}

	/**
	 * This method initializes regSequenceToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getRegSequenceToolBar() {
		if (regSequenceToolBar == null) {
			regSequenceToolBar = new JToolBar();
			regSequenceToolBar.setOrientation(JToolBar.VERTICAL);
			regSequenceToolBar.setFloatable(false);
			regSequenceToolBar.setPreferredSize(new Dimension(34, 116));
			regSequenceToolBar.add(Box.createVerticalGlue());
			regSequenceToolBar.add(getUpBtn());
			regSequenceToolBar.add(getDwnBtn());
			regSequenceToolBar.add(Box.createVerticalStrut(8));
			regSequenceToolBar.add(getAddBtn());
			regSequenceToolBar.add(getDelBtn());
			regSequenceToolBar.add(Box.createVerticalStrut(8));
			regSequenceToolBar.add(getReadSelBtn());
			regSequenceToolBar.add(getWriteSelBtn());
			regSequenceToolBar.add(getAutoFillBtn());
			regSequenceToolBar.add(Box.createVerticalStrut(8));
			regSequenceToolBar.add(getSeqPageTglSeqBtn());
			regSequenceToolBar.add(getSeqNumTglBtn());
			regSequenceToolBar.add(getSeqAdrTglBtn());
			regSequenceToolBar.add(getSeqBitsTglBtn());
			regSequenceToolBar.add(getSeqMaskTglBtn());
			regSequenceToolBar.add(getSeqDelayTglBtn());
			//regSequenceToolBar.add(getEditMskTglBtn());
			regSequenceToolBar.add(Box.createVerticalGlue());
		}
		return regSequenceToolBar;
	}

	/**
	 * This method initializes statusPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			accessStatus = new JLabel();
			accessStatus.setText("ACCESS");
			deviceStatus = new JLabel();
			deviceStatus.setText("DEVICE");
			deviceStatus.setOpaque(false);
			deviceStatus.setBackground(new Color(238, 238, 255));
			statusPanel = new JPanel();
			statusPanel.setLayout(new BorderLayout());
			statusPanel.add(deviceStatus, BorderLayout.WEST);
			statusPanel.add(accessStatus, BorderLayout.EAST);
		}
		return statusPanel;
	}
	
	/**
	 * This method initializes upBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpBtn() {
		if (moveUpSeqBtn == null) {
			moveUpSeqBtn = new JButton();
			moveUpSeqBtn.setText("^");
			moveUpSeqBtn.setPreferredSize(new Dimension(30, 24));
			moveUpSeqBtn.setMaximumSize(new Dimension(30, 24));
			moveUpSeqBtn.setMinimumSize(new Dimension(30, 24));
			moveUpSeqBtn.setFont(new Font("Dialog", Font.PLAIN, 18));
			moveUpSeqBtn.setToolTipText("Move selected row up");
			moveUpSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow > 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						cnt = regSeqTableModel.moveItemUp(selRow, cnt);
						if (cnt > 0) {
							lsm.setSelectionInterval(selRow-1, selRow - 1 + cnt - 1);
		            		regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(selRow-1, 0, true));
						}
					}
				}
			});
		}
		return moveUpSeqBtn;
	}

	/**
	 * This method initializes dwnBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDwnBtn() {
		if (moveDwnSeqBtn == null) {
			moveDwnSeqBtn = new JButton();
			moveDwnSeqBtn.setText("v");
			moveDwnSeqBtn.setPreferredSize(new Dimension(30, 24));
			moveDwnSeqBtn.setMaximumSize(new Dimension(30, 24));
			moveDwnSeqBtn.setMinimumSize(new Dimension(30, 24));
			moveDwnSeqBtn.setToolTipText("Move selected row down");
			moveDwnSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						cnt = regSeqTableModel.moveItemDwn(selRow, cnt);
						if (cnt > 0) {
							lsm.setSelectionInterval(selRow + 1, selRow + 1 + cnt - 1);
			            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(selRow+1, 0, true));
						}
					}
				}
			});
		}
		return moveDwnSeqBtn;
	}

	/**
	 * This method initializes addBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddBtn() {
		if (addSeqItemBtn == null) {
			addSeqItemBtn = new JButton();
			addSeqItemBtn.setText("+");
			addSeqItemBtn.setPreferredSize(new Dimension(30, 24));
			addSeqItemBtn.setMaximumSize(new Dimension(30, 24));
			addSeqItemBtn.setMinimumSize(new Dimension(30, 24));
			addSeqItemBtn.setFont(new Font("Dialog", Font.PLAIN, 14));
			addSeqItemBtn.setToolTipText("Add row after selected one");
			addSeqItemBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int nextRow = regSeqTableModel.addItem(selRow);
						if (nextRow >= 0) {
							lsm.setSelectionInterval(nextRow, nextRow);
			            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return addSeqItemBtn;
	}

	/**
	 * This method initializes delBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDelBtn() {
		if (delSeqItemBtn == null) {
			delSeqItemBtn = new JButton();
			delSeqItemBtn.setText("x");
			delSeqItemBtn.setPreferredSize(new Dimension(30, 24));
			delSeqItemBtn.setMaximumSize(new Dimension(30, 24));
			delSeqItemBtn.setMinimumSize(new Dimension(30, 24));
			delSeqItemBtn.setToolTipText("Delete selected row");
			delSeqItemBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						if (cnt > 1) {
							if (!showDeviceWarningMsg(cnt + " rows will be deleted")) {
								return;
							}
						}
						int nextRow = regSeqTableModel.delItem(selRow, cnt);
						if (nextRow >= 0) {
							lsm.setSelectionInterval(nextRow, nextRow);
			            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return delSeqItemBtn;
	}

	/**
	 * This method initializes readSelBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getReadSelBtn() {
		if (readSelSeqBtn == null) {
			readSelSeqBtn = new JButton();
			readSelSeqBtn.setText("rs");
			readSelSeqBtn.setToolTipText("Read selection");
			readSelSeqBtn.setMinimumSize(new Dimension(30, 24));
			readSelSeqBtn.setPreferredSize(new Dimension(30, 24));
			readSelSeqBtn.setMaximumSize(new Dimension(30, 24));
			readSelSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						int nextRow = regSeqTableModel.readRange(selRow, cnt);
						if (nextRow < 0) {
							showDeviceErrorMsg(regSeqTableModel.getErrorMsg());
						} else {
							lsm.setSelectionInterval(nextRow, nextRow);
			            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return readSelSeqBtn;
	}

	/**
	 * This method initializes writeSelectionBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getWriteSelBtn() {
		if (writeSelSeqBtn == null) {
			writeSelSeqBtn = new JButton();
			writeSelSeqBtn.setText("ws");
			writeSelSeqBtn.setMinimumSize(new Dimension(30, 24));
			writeSelSeqBtn.setPreferredSize(new Dimension(30, 24));
			writeSelSeqBtn.setToolTipText("Write selection");
			writeSelSeqBtn.setMaximumSize(new Dimension(30, 24));
			writeSelSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						int nextRow = regSeqTableModel.writeRange(selRow, cnt); 
						if (nextRow < 0) {
							showDeviceErrorMsg(regSeqTableModel.getErrorMsg());
						} else {
							lsm.setSelectionInterval(nextRow, nextRow);
			            	regSeqTable.scrollRectToVisible(regSeqTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return writeSelSeqBtn;
	}

	/**
	 * This method initializes autoFillBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAutoFillBtn() {
		if (autoFillSeqBtn == null) {
			autoFillSeqBtn = new JButton();
			autoFillSeqBtn.setText("af");
			autoFillSeqBtn.setMinimumSize(new Dimension(30, 24));
			autoFillSeqBtn.setPreferredSize(new Dimension(30, 24));
			autoFillSeqBtn.setToolTipText("Auto-fil selected rows");
			autoFillSeqBtn.setMaximumSize(new Dimension(30, 24));
			autoFillSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regSeqTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						regSeqTableModel.autoFillItems(selRow, cnt);
					}
				}
			});
		}
		return autoFillSeqBtn;
	}

	/**
	 * This method initializes pageTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqPageTglSeqBtn() {
		if (pageTglSeqBtn == null) {
			pageTglSeqBtn = new JToggleButton();
			pageTglSeqBtn.setText("pg");
			pageTglSeqBtn.setToolTipText("Show/hide page column");
			pageTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			pageTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			pageTglSeqBtn.setSelected(true);
			pageTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			pageTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return pageTglSeqBtn;
	}

	/**
	 * This method initializes numTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqNumTglBtn() {
		if (numTglSeqBtn == null) {
			numTglSeqBtn = new JToggleButton();
			numTglSeqBtn.setText("rd");
			numTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			numTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			numTglSeqBtn.setToolTipText("Show/hide register (d) column");
			numTglSeqBtn.setSelected(true);
			numTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			numTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return numTglSeqBtn;
	}

	/**
	 * This method initializes regAdrBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqAdrTglBtn() {
		if (adrTglSeqBtn == null) {
			adrTglSeqBtn = new JToggleButton();
			adrTglSeqBtn.setText("rh");
			adrTglSeqBtn.setToolTipText("Show/hide register (h) column");
			adrTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			adrTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			adrTglSeqBtn.setSelected(true);
			adrTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			adrTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return adrTglSeqBtn;
	}

	/**
	 * This method initializes showBitsTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqBitsTglBtn() {
		if (bitsTglSeqBtn == null) {
			bitsTglSeqBtn = new JToggleButton();
			bitsTglSeqBtn.setText("vb");
			bitsTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			bitsTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			bitsTglSeqBtn.setToolTipText("Show/hide value bits columns");
			bitsTglSeqBtn.setSelected(true);
			bitsTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			bitsTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return bitsTglSeqBtn;
	}

	/**
	 * This method initializes showMskTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqMaskTglBtn() {
		if (maskTglSeqBtn == null) {
			maskTglSeqBtn = new JToggleButton();
			maskTglSeqBtn.setText("m");
			maskTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			maskTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			maskTglSeqBtn.setToolTipText("Show/hide mask column");
			maskTglSeqBtn.setSelected(true);
			maskTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			maskTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return maskTglSeqBtn;
	}

	/**
	 * This method initializes showMskTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSeqDelayTglBtn() {
		if (delayTglSeqBtn == null) {
			delayTglSeqBtn = new JToggleButton();
			delayTglSeqBtn.setText("d");
			delayTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			delayTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			delayTglSeqBtn.setToolTipText("Show/hide delay column");
			delayTglSeqBtn.setSelected(true);
			delayTglSeqBtn.setMaximumSize(new Dimension(30, 24));
			delayTglSeqBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleSeqTableColumns();
				}
			});
		}
		return delayTglSeqBtn;
	}

	/**
	 * This method initializes editMskTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getEditMskTglBtn() {
		if (editMskTglSeqBtn == null) {
			editMskTglSeqBtn = new JToggleButton();
			editMskTglSeqBtn.setText("bm");
			editMskTglSeqBtn.setMinimumSize(new Dimension(30, 24));
			editMskTglSeqBtn.setPreferredSize(new Dimension(30, 24));
			editMskTglSeqBtn.setToolTipText("Show/hide bit mask");
			editMskTglSeqBtn.setMaximumSize(new Dimension(30, 24));
		}
		return editMskTglSeqBtn;
	}

	/**
	 * This method initializes dspRegsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDspRegsPanel() {
		if (dspRegsPanel == null) {
			dspRegsPanel = new JPanel();
			dspRegsPanel.setLayout(new BorderLayout());
			dspRegsPanel.add(getDspRegsScrollPane(), BorderLayout.CENTER);
			dspRegsPanel.add(getDspRegsToolBar(), BorderLayout.WEST);
		}
		return dspRegsPanel;
	}

	/**
	 * This method initializes dspRegsToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getDspRegsToolBar() {
		if (dspRegsToolBar == null) {
			dspRegsToolBar = new JToolBar();
			dspRegsToolBar.setOrientation(JToolBar.VERTICAL);
			dspRegsToolBar.setFloatable(false);
			dspRegsToolBar.setPreferredSize(new Dimension(46, 116));
			dspPgSelector = new JComboBox<String>();
			dspPgSelector.setToolTipText("Current DSP page");
			dspPgSelector.setEditable(false);
			dspPgSelector.setMaximumSize(new Dimension(38,24));
			dspPgSelector.setPreferredSize(new Dimension(38,24));
			dspPgSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
			dspRegsToolBar.add(dspPgSelector);
			dspRegsToolBar.add(Box.createVerticalGlue());
			//indirRegsToolBar.add(getReadIndRegBtn());
			//indirRegsToolBar.add(getWriteIndRegBtn());
			dspRegsToolBar.add(Box.createVerticalStrut(8));
			dspRegsToolBar.add(getNumTglDspRegBtn());
			dspRegsToolBar.add(getAdrTglDspRegBtn());
			dspRegsToolBar.add(getDValTglDspRegBtn());
			dspRegsToolBar.add(getHValTglDspRegBtn());
			dspRegsToolBar.add(getBitsTglDspRegBtn());
			dspRegsToolBar.add(Box.createVerticalGlue());
			dspPgSelector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					if (dspPgSelector.isEnabled()) {
						int page = dspPgSelector.getSelectedIndex();
						if (page >= 0) {
							dspRegsTableModel.setPage(page);
							toggleDspTableColumns();
						}
					}
				}
			});
		}
		return dspRegsToolBar;
	}

	/**
	 * This method initializes dspRegsScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDspRegsScrollPane() {
		if (dspRegsScrollPane == null) {
			dspRegsScrollPane = new JScrollPane();
			dspRegsScrollPane.setViewportView(getDspRegsTable());
		}
		return dspRegsScrollPane;
	}

	/**
	 * This method initializes dspRegsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspRegsTable() {
		if (dspRegsTable == null) {
			dspRegsTable = new JTable();
			dspRegsTableModel = new DspRegTableModel();
			dspRegsTable.setModel(dspRegsTableModel);
			dspRegsTable.setRowHeight(20);
			dspRegsTable.setShowGrid(false);
			dspRegsTable.setIntercellSpacing(new Dimension(0, 0));
			dspRegsTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			dspRegsTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			dspRegsTable.getTableHeader().setReorderingAllowed(false);
			dspRegsTable.getTableHeader().setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			dspRegsTable.setDefaultRenderer(String.class, new RegViewTblCellRenderer());
			dspRegsTable.setDefaultEditor(String.class, new DevRegTblCellEditor());
		}
		return dspRegsTable;
	}

	/**
	 * This method initializes numTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getNumTglDspRegBtn() {
		if (numTglDspRegBtn == null) {
			numTglDspRegBtn = new JToggleButton();
			numTglDspRegBtn.setText("rd");
			numTglDspRegBtn.setMinimumSize(new Dimension(38, 24));
			numTglDspRegBtn.setMaximumSize(new Dimension(38, 24));
			numTglDspRegBtn.setToolTipText("Show/hide register (d) column");
			numTglDspRegBtn.setSelected(true);
			numTglDspRegBtn.setPreferredSize(new Dimension(38, 24));
			numTglDspRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			numTglDspRegBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleDspTableColumns();
				}
			});
		}
		return numTglDspRegBtn;
	}

	/**
	 * This method initializes regAdrBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getAdrTglDspRegBtn() {
		if (adrTglDspRegBtn == null) {
			adrTglDspRegBtn = new JToggleButton();
			adrTglDspRegBtn.setText("rh");
			adrTglDspRegBtn.setToolTipText("Show/hide register (h) column");
			adrTglDspRegBtn.setMinimumSize(new Dimension(38, 24));
			adrTglDspRegBtn.setPreferredSize(new Dimension(38, 24));
			adrTglDspRegBtn.setMaximumSize(new Dimension(38, 24));
			adrTglDspRegBtn.setSelected(true);
			adrTglDspRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			adrTglDspRegBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleDspTableColumns();
				}
			});
		}
		return adrTglDspRegBtn;
	}

	/**
	 * This method initializes showMskTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getDValTglDspRegBtn() {
		if (dvalTglDspRegBtn == null) {
			dvalTglDspRegBtn = new JToggleButton();
			dvalTglDspRegBtn.setText("vd");
			dvalTglDspRegBtn.setMinimumSize(new Dimension(38, 24));
			dvalTglDspRegBtn.setPreferredSize(new Dimension(38, 24));
			dvalTglDspRegBtn.setToolTipText("Show/hide decimal value column");
			dvalTglDspRegBtn.setSelected(true);
			dvalTglDspRegBtn.setMaximumSize(new Dimension(38, 24));
			dvalTglDspRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			dvalTglDspRegBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleDspTableColumns();
				}
			});
		}
		return dvalTglDspRegBtn;
	}

	/**
	 * This method initializes editMskTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getHValTglDspRegBtn() {
		if (hvalTglDspRegBtn == null) {
			hvalTglDspRegBtn = new JToggleButton();
			hvalTglDspRegBtn.setText("vh");
			hvalTglDspRegBtn.setMinimumSize(new Dimension(38, 24));
			hvalTglDspRegBtn.setPreferredSize(new Dimension(38, 24));
			hvalTglDspRegBtn.setToolTipText("Show/hide hex value column");
			hvalTglDspRegBtn.setSelected(true);
			hvalTglDspRegBtn.setMaximumSize(new Dimension(38, 24));
			hvalTglDspRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			hvalTglDspRegBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleDspTableColumns();
				}
			});
		}
		return hvalTglDspRegBtn;
	}

	/**
	 * This method initializes showBitsTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getBitsTglDspRegBtn() {
		if (bitsTglDspRegBtn == null) {
			bitsTglDspRegBtn = new JToggleButton();
			bitsTglDspRegBtn.setText("vb");
			bitsTglDspRegBtn.setMinimumSize(new Dimension(38, 24));
			bitsTglDspRegBtn.setPreferredSize(new Dimension(38, 24));
			bitsTglDspRegBtn.setToolTipText("Show/hide bit columns");
			bitsTglDspRegBtn.setSelected(true);
			bitsTglDspRegBtn.setMaximumSize(new Dimension(38, 24));
			bitsTglDspRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
			bitsTglDspRegBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleDspTableColumns();
				}
			});
		}
		return bitsTglDspRegBtn;
	}

	/**
	 * This method initializes controlsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getControlsPanel() {
		if (controlsPanel == null) {
			controlsPanel = new JPanel();
			controlsPanel.setLayout(new BorderLayout());
			controlsPanel.add(getCtrlsScrollPane(), BorderLayout.CENTER);
			controlsPanel.add(getCtrlsToolBar(), BorderLayout.WEST);
		}
		return controlsPanel;
	}

	/**
	 * This method initializes ctrlsScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCtrlsScrollPane() {
		if (ctrlsScrollPane == null) {
			ctrlsScrollPane = new JScrollPane();
			ctrlsScrollPane.setViewportView(getCtrlsPanel());
		}
		return ctrlsScrollPane;
	}

	/**
	 * This method initializes ctrlsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCtrlsPanel() {
		if (ctrlsPanel == null) {
			ctrlsPanel = new JPanel();
			ctrlsPanel.setLayout(new BorderLayout());
			ctrlsPanel.add(getCtrlsRegTable(), BorderLayout.WEST);
			ctrlsPanel.add(getCtrlsWidgPanel(), BorderLayout.EAST);
		}
		return ctrlsPanel;
	}

	/**
	 * This method initializes ctrlsToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getCtrlsToolBar() {
		if (ctrlsToolBar == null) {
			ctrlsToolBar = new JToolBar();
			ctrlsToolBar.setOrientation(JToolBar.VERTICAL);
			ctrlsToolBar.setFloatable(false);
			ctrlsToolBar.setPreferredSize(new Dimension(34, 116));
			ctrlsToolBar.add(Box.createVerticalGlue());
			ctrlsToolBar.add(getCtrlUpBtn());
			ctrlsToolBar.add(getCtrlDwnBtn());
			ctrlsToolBar.add(Box.createVerticalStrut(8));
			ctrlsToolBar.add(getCtrlAddBtn());
			ctrlsToolBar.add(getCtrlDelBtn());
			ctrlsToolBar.add(Box.createVerticalStrut(8));
			ctrlsToolBar.add(getCtrlReadSelBtn());
			ctrlsToolBar.add(getCtrlWriteSelBtn());
			ctrlsToolBar.add(getCtrlAutoFillBtn());
			ctrlsToolBar.add(Box.createVerticalStrut(8));
			ctrlsToolBar.add(getCtrlPageTglBtn());
			ctrlsToolBar.add(getCtrlNumTglBtn());
			ctrlsToolBar.add(getCtrlAdrTglBtn());
			ctrlsToolBar.add(getCtrlBitsTglBtn());
			ctrlsToolBar.add(Box.createVerticalStrut(8));
			ctrlsToolBar.add(getCtrlContWrTglBtn());
			ctrlsToolBar.add(Box.createVerticalGlue());
		}
		return ctrlsToolBar;
	}

	/**
	 * This method initializes upBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlUpBtn() {
		if (moveUpCtrlBtn == null) {
			moveUpCtrlBtn = new JButton();
			moveUpCtrlBtn.setText("^");
			moveUpCtrlBtn.setPreferredSize(new Dimension(30, 24));
			moveUpCtrlBtn.setMaximumSize(new Dimension(30, 24));
			moveUpCtrlBtn.setMinimumSize(new Dimension(30, 24));
			moveUpCtrlBtn.setFont(new Font("Dialog", Font.PLAIN, 18));
			moveUpCtrlBtn.setToolTipText("Move selected row up");
			moveUpCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow > 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						cnt = regCtrlsTableModel.moveItemUp(selRow, cnt);
						if (cnt > 0) {
							lsm.setSelectionInterval(selRow-1, selRow - 1 + cnt - 1);
		            		regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(selRow-1, 0, true));
						}
					}
				}
			});
		}
		return moveUpCtrlBtn;
	}

	/**
	 * This method initializes dwnBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlDwnBtn() {
		if (moveDwnCtrlBtn == null) {
			moveDwnCtrlBtn = new JButton();
			moveDwnCtrlBtn.setText("v");
			moveDwnCtrlBtn.setPreferredSize(new Dimension(30, 24));
			moveDwnCtrlBtn.setMaximumSize(new Dimension(30, 24));
			moveDwnCtrlBtn.setMinimumSize(new Dimension(30, 24));
			moveDwnCtrlBtn.setToolTipText("Move selected row down");
			moveDwnCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						cnt = regCtrlsTableModel.moveItemDwn(selRow, cnt);
						if (cnt > 0) {
							lsm.setSelectionInterval(selRow + 1, selRow + 1 + cnt - 1);
							regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(selRow+1, 0, true));
						}
					}
				}
			});
		}
		return moveDwnCtrlBtn;
	}

	/**
	 * This method initializes addBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlAddBtn() {
		if (addCtrlBtn == null) {
			addCtrlBtn = new JButton();
			addCtrlBtn.setText("+");
			addCtrlBtn.setPreferredSize(new Dimension(30, 24));
			addCtrlBtn.setMaximumSize(new Dimension(30, 24));
			addCtrlBtn.setMinimumSize(new Dimension(30, 24));
			addCtrlBtn.setFont(new Font("Dialog", Font.PLAIN, 14));
			addCtrlBtn.setToolTipText("Add row after selected one");
			addCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int nextRow = regCtrlsTableModel.addItem(selRow);
						if (nextRow >= 0) {
							lsm.setSelectionInterval(nextRow, nextRow);
							regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return addCtrlBtn;
	}

	/**
	 * This method initializes delBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlDelBtn() {
		if (delCtrlBtn == null) {
			delCtrlBtn = new JButton();
			delCtrlBtn.setText("x");
			delCtrlBtn.setPreferredSize(new Dimension(30, 24));
			delCtrlBtn.setMaximumSize(new Dimension(30, 24));
			delCtrlBtn.setMinimumSize(new Dimension(30, 24));
			delCtrlBtn.setToolTipText("Delete selected row");
			delCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						if (cnt > 1) {
							if (!showDeviceWarningMsg(cnt + " rows will be deleted")) {
								return;
							}
						}
						int nextRow = regCtrlsTableModel.delItem(selRow, cnt);
						if (nextRow >= 0) {
							lsm.setSelectionInterval(nextRow, nextRow);
							regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return delCtrlBtn;
	}

	/**
	 * This method initializes readSelBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlReadSelBtn() {
		if (readSelCtrlBtn == null) {
			readSelCtrlBtn = new JButton();
			readSelCtrlBtn.setText("rs");
			readSelCtrlBtn.setMinimumSize(new Dimension(30, 24));
			readSelCtrlBtn.setPreferredSize(new Dimension(30, 24));
			readSelCtrlBtn.setMaximumSize(new Dimension(30, 24));
			readSelCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						int nextRow = regCtrlsTableModel.readRange(selRow, cnt);
						if (nextRow < 0) {
							showDeviceErrorMsg(regCtrlsTableModel.getErrorMsg());
						} else {
							lsm.setSelectionInterval(nextRow, nextRow);
							regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return readSelCtrlBtn;
	}

	/**
	 * This method initializes writeSelectionBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlWriteSelBtn() {
		if (writeSelCtrlBtn == null) {
			writeSelCtrlBtn = new JButton();
			writeSelCtrlBtn.setText("ws");
			writeSelCtrlBtn.setMinimumSize(new Dimension(30, 24));
			writeSelCtrlBtn.setPreferredSize(new Dimension(30, 24));
			writeSelCtrlBtn.setToolTipText("Write selection");
			writeSelCtrlBtn.setMaximumSize(new Dimension(30, 24));
			writeSelCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						int nextRow = regCtrlsTableModel.writeRange(selRow, cnt); 
						if (nextRow < 0) {
							showDeviceErrorMsg(regCtrlsTableModel.getErrorMsg());
						} else {
							lsm.setSelectionInterval(nextRow, nextRow);
							regCtrlsTable.scrollRectToVisible(regCtrlsTable.getCellRect(nextRow, 0, true));
						}
					}
				}
			});
		}
		return writeSelCtrlBtn;
	}

	/**
	 * This method initializes autoFillBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCtrlAutoFillBtn() {
		if (autoFillCtrlBtn == null) {
			autoFillCtrlBtn = new JButton();
			autoFillCtrlBtn.setText("af");
			autoFillCtrlBtn.setMinimumSize(new Dimension(30, 24));
			autoFillCtrlBtn.setPreferredSize(new Dimension(30, 24));
			autoFillCtrlBtn.setToolTipText("Auto-fil selected rows");
			autoFillCtrlBtn.setMaximumSize(new Dimension(30, 24));
			autoFillCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ListSelectionModel lsm = regCtrlsTable.getSelectionModel();
					int selRow = lsm.getMinSelectionIndex();
					if (selRow >= 0) {
						int cnt = lsm.getMaxSelectionIndex() - selRow + 1;
						regCtrlsTableModel.autoFillItems(selRow, cnt);
					}
				}
			});
		}
		return autoFillCtrlBtn;
	}

	/**
	 * This method initializes pageTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getCtrlPageTglBtn() {
		if (pageTglCtrlBtn == null) {
			pageTglCtrlBtn = new JToggleButton();
			pageTglCtrlBtn.setText("p");
			pageTglCtrlBtn.setMinimumSize(new Dimension(30, 24));
			pageTglCtrlBtn.setPreferredSize(new Dimension(30, 24));
			pageTglCtrlBtn.setSelected(true);
			pageTglCtrlBtn.setMaximumSize(new Dimension(30, 24));
			pageTglCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TableColumn tc = regCtrlsTable.getColumnModel().getColumn(RegSequenceTableModel.PAGE_COLUMN);
					if (pageTglCtrlBtn.isSelected()) {
						tc.setPreferredWidth(tc.getMaxWidth());
					} else {
						tc.setPreferredWidth(0);
					}
				}
			});
		}
		return pageTglCtrlBtn;
	}

	/**
	 * This method initializes numTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getCtrlNumTglBtn() {
		if (numTglCtrlBtn == null) {
			numTglCtrlBtn = new JToggleButton();
			numTglCtrlBtn.setText("#");
			numTglCtrlBtn.setMinimumSize(new Dimension(30, 24));
			numTglCtrlBtn.setMaximumSize(new Dimension(30, 24));
			numTglCtrlBtn.setToolTipText("Show/hide register number column");
			numTglCtrlBtn.setSelected(true);
			numTglCtrlBtn.setPreferredSize(new Dimension(30, 24));
			numTglCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TableColumn tc = regCtrlsTable.getColumnModel().getColumn(RegSequenceTableModel.REG_NUM_COLUMN);
					if (numTglCtrlBtn.isSelected()) {
						tc.setPreferredWidth(tc.getMaxWidth());
					} else {
						tc.setPreferredWidth(0);
//						if (!adrTglBtn.isSelected())
//							adrTglBtn.doClick();
					}
				}
			});
		}
		return numTglCtrlBtn;
	}

	/**
	 * This method initializes regAdrBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getCtrlAdrTglBtn() {
		if (adrTglCtrlBtn == null) {
			adrTglCtrlBtn = new JToggleButton();
			adrTglCtrlBtn.setText("&");
			adrTglCtrlBtn.setMinimumSize(new Dimension(30, 24));
			adrTglCtrlBtn.setPreferredSize(new Dimension(30, 24));
			adrTglCtrlBtn.setSelected(true);
			adrTglCtrlBtn.setMaximumSize(new Dimension(30, 24));
			adrTglCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TableColumn tc = regCtrlsTable.getColumnModel().getColumn(RegSequenceTableModel.ADDR_COLUMN);
					if (adrTglCtrlBtn.isSelected()) {
						tc.setPreferredWidth(tc.getMaxWidth());
					} else {
						tc.setPreferredWidth(0);
//						if (!numTglBtn.isSelected())
//							numTglBtn.doClick();
					}
				}
			});
		}
		return adrTglCtrlBtn;
	}

	/**
	 * This method initializes showBitsTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getCtrlBitsTglBtn() {
		if (bitsTglCtrlBtn == null) {
			bitsTglCtrlBtn = new JToggleButton();
			bitsTglCtrlBtn.setText("b");
			bitsTglCtrlBtn.setMinimumSize(new Dimension(30, 24));
			bitsTglCtrlBtn.setPreferredSize(new Dimension(30, 24));
			bitsTglCtrlBtn.setToolTipText("Show/hide bit columns");
			bitsTglCtrlBtn.setSelected(true);
			bitsTglCtrlBtn.setMaximumSize(new Dimension(30, 24));
			bitsTglCtrlBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					/*for (int i = RegSequenceTableModel.highBitColumn; i <= RegSequenceTableModel.lowBitColumn; i++) {
						TableColumn tc = regCtrlsTable.getColumnModel().getColumn(i);
						if (bitsTglCtrlBtn.isSelected()) {
							tc.setPreferredWidth(tc.getMaxWidth());
						} else {
							tc.setPreferredWidth(0);
						}
					}*/
				}
			});
		}
		return bitsTglCtrlBtn;
	}

	/**
	 * This method initializes contWrTglBtn	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getCtrlContWrTglBtn() {
		if (contWrTglBtn == null) {
			contWrTglBtn = new JToggleButton();
			contWrTglBtn.setText("ca");
			contWrTglBtn.setMinimumSize(new Dimension(30, 24));
			contWrTglBtn.setPreferredSize(new Dimension(30, 24));
			contWrTglBtn.setToolTipText("Enable contiguous HW access");
			contWrTglBtn.setMaximumSize(new Dimension(30, 24));
		}
		return contWrTglBtn;
	}

	/**
	 * This method initializes ctrlsRegTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getCtrlsRegTable() {
		if (regCtrlsTable == null) {
			regCtrlsTable = new JTable();
			regCtrlsTableModel = new RegCtrlsTableModel();
			regCtrlsTable.setModel(regCtrlsTableModel);
			regCtrlsTable.setRowHeight(20);
			regCtrlsTable.setCellSelectionEnabled(true);
			regCtrlsTable.setShowGrid(false);
			regCtrlsTable.setIntercellSpacing(new Dimension(0, 0));
			regCtrlsTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			regCtrlsTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			regCtrlsTable.getTableHeader().setReorderingAllowed(false);
			//ctrlsRegTable.getTableHeader().setResizingAllowed(false);
			regCtrlsTable.getTableHeader().setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
			regCtrlsTable.setDefaultRenderer(String.class, new RegViewTblCellRenderer());
			regCtrlsTable.setDefaultEditor(String.class, new DevRegTblCellEditor());
			int charWidth = regCtrlsTable.getFontMetrics(regCtrlsTable.getFont()).charWidth('0');
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.NAME_COLUMN).setPreferredWidth(charWidth * 8);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.NAME_COLUMN).setMinWidth(charWidth * 4);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.NAME_COLUMN).setMaxWidth(charWidth * 16);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.PAGE_COLUMN).setPreferredWidth(charWidth * 4);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.PAGE_COLUMN).setMinWidth(0);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.PAGE_COLUMN).setMaxWidth(charWidth * 4);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.REG_NUM_COLUMN).setPreferredWidth(charWidth * 6);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.REG_NUM_COLUMN).setMinWidth(0);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.REG_NUM_COLUMN).setMaxWidth(charWidth * 6);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.ADDR_COLUMN).setPreferredWidth(charWidth * 6);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.ADDR_COLUMN).setMinWidth(0);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.ADDR_COLUMN).setMaxWidth(charWidth * 6);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.VALUE_COLUMN).setPreferredWidth(charWidth * 6);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.VALUE_COLUMN).setMinWidth(charWidth * 4);
			regCtrlsTable.getColumnModel().getColumn(RegCtrlsTableModel.VALUE_COLUMN).setMaxWidth(charWidth * 6);
			for (int i = RegCtrlsTableModel.BIT_7_COLUMN; i <= RegCtrlsTableModel.BIT_0_COLUMN; i++) {
				regCtrlsTable.getColumnModel().getColumn( i ).setPreferredWidth(charWidth * 3);
				regCtrlsTable.getColumnModel().getColumn( i ).setMinWidth(0);
				regCtrlsTable.getColumnModel().getColumn( i ).setMaxWidth(charWidth * 3);
			}
		}
		return regCtrlsTable;
	}

	/**
	 * This method initializes ctrlsWidgPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCtrlsWidgPanel() {
		if (ctrlsWidgPanel == null) {
			ctrlsWidgPanel = new JPanel();
			ctrlsWidgPanel.setLayout(new BoxLayout(getCtrlsWidgPanel(), BoxLayout.Y_AXIS));
		}
		return ctrlsWidgPanel;
	}

	/**
	 * Launches this application
	 */
	public static void main(String[] args) {
		System.setProperty("jna.library.path", System.getProperty("java.class.path")+System.getProperty("path.separator")+System.getProperty("user.dir"));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DeviceControlApp application = new DeviceControlApp();
				application.getAppFrame().setVisible(true);
			}
		});
	}
}
