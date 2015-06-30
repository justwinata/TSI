package com.idt.devctrl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author Vitaliy Kulikov
 *
 */
public class RegSequenceTableModel extends AbstractTableModel implements DevRegTableModel {
	private static final long serialVersionUID = -3279430326378143661L;

	private static final String MASK_TAG = "m";
	private static final String DELAY_TAG = "d";
	private static final int DELAY_MASK = 0xFFFF;
	static final int SUCCESS = 0;
	static final int FAILURE = -1;
	static final int PAGE_COLUMN = 0;
	static final int REG_NUM_COLUMN = PAGE_COLUMN + 1;
	static final int ADDR_COLUMN = REG_NUM_COLUMN + 1;
	static final int VALUE_COLUMN = ADDR_COLUMN + 1;
	private int highBitColumn = VALUE_COLUMN + 1;
	private int lowBitColumn = highBitColumn + 7;
	private int maskColumn = lowBitColumn + 1;
	private int delayColumn = maskColumn + 1;
	private int descrColumn = delayColumn + 1;

	private Vector<RegSequenceItem> seqItems = null;
	private Register[] regsBuf = null;
	private I2CDevice device = null;
	private boolean modified = false;
	private int status = SUCCESS;
	private String errMsg = null;
	
	public RegSequenceTableModel() {
		seqItems = new Vector<RegSequenceItem>(256, 16);
		regsBuf = new Register[256];
	}

	public boolean isBitColumn(int col) {
		return col >= highBitColumn && col <= lowBitColumn;
	}
	
	private int columnToBit(int col) {
		int bit = lowBitColumn - col;
		return bit;
	}
	
	public int getMaskColumn() {
		return maskColumn;
	}

	public int getDelayColumn() {
		return delayColumn;
	}
	
	public void setDevice(I2CDevice device) {
		if (this.device != device) {
			seqItems.setSize(0);
			this.device = device;
			if (device != null) {
				lowBitColumn = highBitColumn + device.getRegister(0, 0).getBitDepth() - 1;
				maskColumn = lowBitColumn + 1;
				delayColumn = maskColumn + 1;
				descrColumn = delayColumn + 1;
			} else {
				lowBitColumn = highBitColumn + 7;
				maskColumn = lowBitColumn + 1;
				delayColumn = maskColumn + 1;
				descrColumn = delayColumn + 1;
			}
			fireTableStructureChanged();
		}
	}
	
	@Override
	public int getColumnCount() {
		return descrColumn + 1;
	}

	@Override
	public int getRowCount() {
		if (seqItems != null)
			return seqItems.capacity();
		return 0;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case PAGE_COLUMN:
			return "Pg";
		case REG_NUM_COLUMN:
			return "Reg(d)";
		case ADDR_COLUMN:
			return "Reg(h)";
		case VALUE_COLUMN:
			return "Val";
		}
		if (col == maskColumn) {
			return "Mask";
		} else if (col == delayColumn) {
			return "Del(ms)";
		} else if (col == descrColumn) {
			return "Description";
		} else if (isBitColumn(col))
			return String.format("%d", columnToBit(col));
		return null;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				if (col == PAGE_COLUMN) {
					return seqItm.getPageStr();
				} else {
					Register reg = seqItm.getRegister();
					if (reg != null) {
						switch (col) {
						case REG_NUM_COLUMN:
							return reg.getAddressStr();
						case ADDR_COLUMN:
							return reg.getAddressHexStr();
						case VALUE_COLUMN:
							return reg.getValueHexStr();
						}
						if (col == maskColumn) {
							return seqItm.getMaskStr();
						} else if (col == delayColumn) {
							return seqItm.getDelayStr();
						} else if (col == descrColumn) {
							return reg.getDescription();
						}
						if (isBitColumn(col)) {
							return reg.getBit(columnToBit(col));
						}
					} else {
						if (col == descrColumn) {
							return seqItm.getComment();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		String val = value.toString();
		RegSequenceItem seqItm = null;
		Register reg = null;
		if (row < seqItems.size() && (seqItm = seqItems.elementAt(row)) != null) {
			reg = seqItm.getRegister();
		}
		if (isBitColumn(col)) {
			if (reg != null && (val.isEmpty() || Register.isValidBitValue(val))) {
				reg.modifyBit(columnToBit(col), val);
				fireTableDataChanged();
			}
			return;
		} else {
			switch (col) {
			case PAGE_COLUMN:
				if (val.isEmpty()) {
					if (seqItm != null)
						seqItm.clearPage();
				} if (Register.isValidDecStr(val)) {
					setPage(row, Integer.parseInt(val));
				}
				return;
			case REG_NUM_COLUMN:
				if (val.isEmpty()) {
					if (seqItm != null)
						seqItm.setRegister(null);
				} else if (Register.isValidDecStr(val)) {
					setRegister(row, Integer.parseInt(val));
				}
				return;
			case ADDR_COLUMN:
				if (val.isEmpty()) {
					if (seqItm != null)
						seqItm.setRegister(null);
				} else if (Register.isValidHexStr(val)) {
					setRegister(row, Integer.parseInt(val, 16));
				}
				return;
			case VALUE_COLUMN:
				if (reg != null && (val.isEmpty() || Register.isValidHexStr(val))) {
					reg.modifyValue(val, 16);
					fireTableDataChanged();
				}
				return;
			}
			if (col == maskColumn) {
				if (reg != null && Register.isValidHexStr(val)) {
					seqItm.setMask(Long.parseLong(val, 16) & reg.getValueMask());
					fireTableDataChanged();
				}
				return;
			} else if (col == delayColumn) {
				if (reg != null && Register.isValidDecStr(val)) {
					seqItm.setDelay(Integer.parseInt(val) & DELAY_MASK);
					fireTableDataChanged();
				}
				return;
			} else if (col == descrColumn) {
				if (reg == null) {
					if (seqItm == null) {
						seqItm = new RegSequenceItem();
						seqItems.setElementAt(seqItm, row);
					}
					seqItm.setComment(val);
					fireTableDataChanged();
				}
				return;
			}
		}
	}
	
	private RegSequenceItem getItemAt(int idx) {
		if (idx >= seqItems.size())
			seqItems.setSize(idx+1);
		RegSequenceItem item = seqItems.elementAt(idx);
		if (item == null) {
			item = new RegSequenceItem();
			seqItems.setElementAt(item, idx);
		}
		return item;
	}
	
	private RegSequenceItem insertItemAt(int idx) {
		RegSequenceItem item = new RegSequenceItem();
		if (idx >= seqItems.size()) {
			seqItems.setSize(idx+1);
			seqItems.setElementAt(item, idx);
		} else {
			seqItems.insertElementAt(item, idx);
		}
		return item;
	}
	
	private void trimSequence() {
		int i = 0;
		for (i=seqItems.size()-1; i>=0; i--) {
			if (seqItems.elementAt(i) != null && seqItems.elementAt(i).getRegister() != null)
				break;
		}
		seqItems.setSize(i + 1);
	}
	
	private void setRegister(int idx, int register) {
		if (device != null) { 
			RegSequenceItem seqItm = getItemAt(idx);
			if (device.isValidRegister(seqItm.getPage(), register)) {
				Register reg = seqItm.getRegister();
				if (reg == null || reg.getAddress() != register) {
					reg = device.getRegister(seqItm.getPage(), register);
					if (reg != null) {
						reg = (Register)reg.clone();
						seqItm.setRegister(reg);
						modified  = true;
					}
					fireTableDataChanged();
				}
			}
		}
	}
	
	private void setRegister(int idx, int page, int register, boolean updateView) {
		if (device != null && device.isValidPage(page) && device.isValidRegister(page, register)) {
			Register reg = device.getRegister(page, register);
			if (reg != null) {
				RegSequenceItem seqItm = getItemAt(idx);
				Register r = seqItm.getRegister();
				if (r == null || r.getPage() != page || r.getAddress() != register) {
					reg = (Register)reg.clone();
					seqItm.setPage(page);
					seqItm.setRegister(reg);
					modified  = true;
				} else {
					r.setValue(reg.getValue());
				}
				if (updateView)
					fireTableDataChanged();
			}
		}
	}
	
	private void setPage(int idx, int page) {
		if (device != null && device.isValidPage(page)) {
			RegSequenceItem seqItm = getItemAt(idx);
			if (seqItm.getPage() != page) {
				Register oldReg = seqItm.getRegister();
				seqItm.setPage(page);
				if (oldReg != null) {
					Register reg = device.getRegister(page, oldReg.getAddress());
					if (reg != null) {
						seqItm.setRegister((Register)reg.clone());
						if (oldReg.isModified())
							seqItm.getRegister().modifyValue(oldReg.getValueHexStr(), 16);
					}
				}
				fireTableDataChanged();
			}
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (row < seqItems.size()) {
			if (seqItems.elementAt(row) != null && seqItems.elementAt(row).getRegister() != null) {
				return col != descrColumn;
			}
			return col == PAGE_COLUMN || col == REG_NUM_COLUMN || col == ADDR_COLUMN || col == descrColumn;
		}
		return col == PAGE_COLUMN || col == REG_NUM_COLUMN || col == ADDR_COLUMN;
	}

	@Override
	public boolean isChanged(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				Register reg = seqItm.getRegister();
				if (reg != null) {
					switch (col) {
					case VALUE_COLUMN:
						return reg.isChanged();
					}
					if (isBitColumn(col))
						return reg.isChanged(columnToBit(col));
				}
			}
		}
		return false;
	}

	@Override
	public boolean isModified(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				Register reg = seqItm.getRegister();
				if (reg != null) {
					if (col == VALUE_COLUMN) {
						return reg.isModified();
					} else if (col == maskColumn) {
						return seqItm.getMask() != reg.getValueMask();
					} else if (col == delayColumn) {
						return seqItm.getDelay() > 0;
					}
					if (isBitColumn(col))
						return reg.isModified(columnToBit(col));
				}
			}
		}
		return false;
	}

	@Override
	public boolean isBitCell(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				Register reg = seqItm.getRegister();
				if (reg != null) {
					return isBitColumn(col);
				}
			}
		}
		return false;
	}

	@Override
	public boolean mergeWithRightCell(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				Register reg = seqItm.getRegister();
				if (reg != null) {
					return (isBitColumn(col) && col != lowBitColumn);
				}
			}
		}
		return col == descrColumn;
	}

	@Override
	public boolean isMaskCell(int row, int col) {
		if (row < seqItems.size()) {
			RegSequenceItem seqItm = seqItems.elementAt(row);
			if (seqItm != null) {
				Register reg = seqItm.getRegister();
				if (reg != null) {
					return col == maskColumn;
				}
			}
		}
		return false;
	}

	@Override
	public String getToolTip(int row, int col) {
		return String.format("row# %d", row);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}

	public int loadFromFile(File file, int startIdx) {
		if (device != null) {
			boolean valid = false;
			int reg = -1;
			String val = null;
			int page = 0;
			int mask = 0xFF;
			int delay = 0;
			FileReader fileReader = null;
			try {
				StringBuilder sb = new StringBuilder(100);
				fileReader = new FileReader(file);
				String ln = null;
				RegSequenceItem itm = null;
				int ich;
				
				while ((ich = fileReader.read()) > 0) {
					char ch = (char)ich;
					if (ch != '\n') {
						if (ch != '\r')
							sb.append(ch);
					} else {
						ln = sb.toString();
						if (!valid) {
							if (ln.equals(device.getName())) {
								valid = true;
							}
						} else {
							if (ln.startsWith("#")) {
					    		itm = insertItemAt(startIdx);
					    		startIdx++;
								if (ln.length() > 1)
									itm.setComment(ln.substring(1));
							} else if (ln.length() > 6) {
								reg = -1;
								val = null;
								page = 0;
								mask = 0xFF;
								delay = 0;
								String[] tokens = ln.split("[:;#=\\s]+");
								for (int i=0; i<tokens.length-1; i+=2) {
									if (tokens[i].equalsIgnoreCase(Register.REG_ADDR_TAG)) {
							    		 reg = Integer.parseInt(tokens[i+1], 16);
							    	} else if (tokens[i].equalsIgnoreCase(Register.REG_NUM_TAG)) {
							    		 reg = Integer.parseInt(tokens[i+1]);
							    	} else if (tokens[i].equalsIgnoreCase(Register.REG_NUM_ALT_TAG)) {
										reg = Integer.parseInt(tokens[i+1]);
									} else if (tokens[i].equalsIgnoreCase(Register.REG_VAL_TAG)) {
										val = tokens[i+1];
									} else if (tokens[i].equalsIgnoreCase(Register.PAGE_TAG)) {
								    	page = Integer.parseInt(tokens[i+1]);
									} else if (tokens[i].equalsIgnoreCase(MASK_TAG)) {
										mask = Integer.parseInt(tokens[i+1], 16);
									} else if (tokens[i].equalsIgnoreCase(DELAY_TAG)) {
										delay = Integer.parseInt(tokens[i+1]);
									}
								}
							    if (device.isValidRegister(page, reg)) {
							    	Register register = device.getRegister(page, reg);
							    	if (register != null && val != null && register.isValidValue(val)) {
							    		register = (Register)register.clone();
							    		register.modifyValue(val, 16);
							    		itm = insertItemAt(startIdx);
							    		itm.setPage(page);
							    		itm.setRegister(register);
							    		itm.setMask(mask);
							    		itm.setDelay(delay);
										System.out.println(String.format("p %d r %X v %s m %x", page, reg, val, mask));
							    		startIdx++;
							    	} else {
							    		status = FAILURE;
							    		errMsg = file.getName() + " - Invalid sequence file format";
							    		return -1;
							    	}
							    }
							}
						}
					    sb.setLength(0);
					}
				}
				if (!valid) {
		    		status = FAILURE;
		    		errMsg = file.getName() + " - Sequence file is not for this device";
		    		return -1;
				} else {
					trimSequence();
				}
			} catch(IOException ioEx) {
				System.out.println(ioEx.getMessage());
				errMsg = ioEx.getMessage();
				status = FAILURE;
				return -1;
			} finally {
				if (fileReader != null)
					try {
						fileReader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			fireTableDataChanged();
			status = SUCCESS;
			return startIdx;
		}
		throw new IllegalArgumentException("Invalid device " + device);
	}

	public boolean saveToFile(File file) {
		trimSequence();
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("# instead of hexadecimal r&:XX decimal register numbers r:N.. or r#:N.. can be used as well\n");
			fileWriter.write("# only register and value are required\n");
			fileWriter.write(device.getName());
			fileWriter.write('\n');
			for (RegSequenceItem itm: seqItems) {
				if (itm != null) {
					Register reg = itm.getRegister();
					if (reg != null) {
						fileWriter.write(String.format("%s:%X %s:%x %s:%d %s:%s %s:%s #%s\n", 
								Register.REG_ADDR_TAG, reg.getAddress(), 
								Register.REG_VAL_TAG, reg.getValue(), 
								Register.PAGE_TAG, reg.getPage(), 
								MASK_TAG, itm.getMaskStr(), 
								DELAY_TAG, itm.getDelayStr(), 
								reg.getName()));
					} else {
						fileWriter.write(String.format("#%s\n", itm.getComment()));
					}
				} else {
					fileWriter.write("#\n");
				}
			}
			modified = false;
			fileWriter.close();
		} catch(IOException ioEx) {
			System.out.println(ioEx.toString());
			return false;
		}
		return true;
	}
	
	public boolean isModified() {
		if (seqItems.size() > 0) {
			if (modified)
				return true;
			for (RegSequenceItem itm: seqItems) {
				if (itm != null) {
					Register reg = itm.getRegister();
					if (reg != null) {
						if (reg.isModified())
							return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * This method reads all registers in sequence	
	 * 	
	 * @return true on success, false on failure
	 */
	public boolean readAll() {
		trimSequence();
		int status = readRange(0, seqItems.size());
		if (status >= 0)
			modified = false;
		return status >= 0;
	}
	
	/**
	 * This method writes all registers in sequence	
	 * 	
	 * @return true on success, false on failure
	 */
	public boolean writeAll() {
		trimSequence();
		int cnt = 0;
		try {
			for (RegSequenceItem itm: seqItems) {
				if (itm != null) {
					Register reg = itm.getRegister();
					if (reg != null) {
						long mask = itm.getMask();
						if (mask != reg.getValueMask()) {
							if (cnt > 0) {
								if (!device.writeHw(regsBuf, cnt, true/*all*/))
									return false;
								cnt = 0;
							}
							if (!device.writeHw(reg, mask))
								return false;
						} else {
							if (cnt >= regsBuf.length )
								regsBuf = Arrays.copyOf(regsBuf, regsBuf.length + 256);
							regsBuf[cnt++] = reg;
						}
						if (itm.getDelay() > 0) {
							if (cnt > 0) {
								if (!device.writeHw(regsBuf, cnt, true/*all*/))
									return false;
								cnt = 0;
							}
							if (I2CDevice.DEBUG)
								System.out.println("sleep(ms) "+itm.getDelay());
							try {
								Thread.sleep(itm.getDelay());
							} catch (InterruptedException e) {
								System.out.println(e.getMessage());
							}
						}
					}
				}
			}
			if (cnt > 0) {
				if (!device.writeHw(regsBuf, cnt, true/*all*/))
					return false;
			}
		} finally {
			fireTableDataChanged();
		}
		return true;
	}
	
	private int getPrevRegIndex(int idx) {
		if (idx > seqItems.size())
			idx = seqItems.size();
		idx--;
		while (idx >= 0) {
			RegSequenceItem itm = seqItems.elementAt(idx);
			if (itm != null && itm.getRegister() != null) {
				return idx;
			}
			idx--;
		}
		return idx;
	}
	
	private int getNextRegIndex(int idx) {
		while (idx < seqItems.size()) {
			RegSequenceItem itm = seqItems.elementAt(idx);
			if (itm != null && itm.getRegister() != null) {
				return idx;
			}
			idx++;
		}
		return 0;
	}
	
	/**
	 * This method reads registers in a sequence range	
	 * 	
	 * @return next available register index on success, -1 on failure
	 */
	public int readRange(int idx, int cnt) {
		int c = 0;
		for (int i=idx; i<idx+cnt && i<seqItems.size(); i++) {
			RegSequenceItem itm = seqItems.elementAt(i);
			if (itm != null) {
				Register reg = itm.getRegister();
				if (reg != null) {
					if (c >= regsBuf.length )
						regsBuf = Arrays.copyOf(regsBuf, regsBuf.length + 256);
					regsBuf[c++] = reg;
				}
			}
		}
		if (device.readHw(regsBuf, c)) {
			fireTableDataChanged();
			return getNextRegIndex(idx + cnt);
		} else {
			return -1;
		}
	}

	/**
	 * This method writes registers in a sequence range	
	 * 	
	 * @return next available register index on success, -1 on failure
	 */
	public int writeRange(int idx, int cnt) {
		try {
			for (int i=idx; i<idx+cnt && i<seqItems.size(); i++) {
				RegSequenceItem itm = seqItems.elementAt(i);
				if (itm != null) {
					Register reg = itm.getRegister();
					if (reg != null) {
						if (!device.writeHw(reg, itm.getMask()))
							return -1;
						if (itm.getDelay() > 0) {
							if (I2CDevice.DEBUG)
								System.out.println("sleep(ms) "+itm.getDelay());
							try {
								Thread.sleep(itm.getDelay());
							} catch (InterruptedException e) {
								System.out.println(e.getMessage());
							}
						}
					}
				}
			}
		} finally {
			fireTableDataChanged();
		}
		return getNextRegIndex(idx + cnt);
	}
	
	public int moveItemUp(int itemIdx, int cnt) {
		if (cnt > 0 && itemIdx > 0 && itemIdx  < seqItems.size()) {
			if (itemIdx + cnt > seqItems.size())
				cnt = seqItems.size() - itemIdx;
			for (int i=0; i<cnt; i++) {
				RegSequenceItem seqItm = seqItems.remove(itemIdx+i);
				seqItems.insertElementAt(seqItm, itemIdx+i-1);
			}
			fireTableDataChanged();
			return cnt;
		}
		return 0;
	}
	
	public int moveItemDwn(int itemIdx, int cnt) {
		if (cnt > 0 && itemIdx + cnt <= seqItems.size()) {
			if (itemIdx + cnt == seqItems.size())
				seqItems.setSize(itemIdx + cnt + 1);
			for (int i=cnt-1; i>=0; i--) {
				RegSequenceItem seqItm = seqItems.remove(itemIdx+i);
				seqItems.insertElementAt(seqItm, itemIdx+i+1);
			}
			fireTableDataChanged();
			return cnt;
		}
		return 0;
	}
	
	/**
	 * This method adds space for register at requested location	
	 * 	
	 * @return next item index on success, -1 on failure
	 */
	public int addItem(int itemIdx) {
		if (itemIdx < seqItems.size()) {
			seqItems.insertElementAt(null, itemIdx+1);
			fireTableDataChanged();
			return itemIdx + 1;
		}
		return -1;
	}
	
	/**
	 * This method deletes item (can be empty) at provided location	
	 * 	
	 * @return next item index on success, -1 on failure
	 */
	public int delItem(int itemIdx, int cnt) {
		if (cnt > 0 && itemIdx < seqItems.size()) {
			if (itemIdx + cnt > seqItems.size())
				cnt = seqItems.size() - itemIdx;
			for (int i=0; i<cnt; i++) {
				seqItems.remove(itemIdx);
			}
			fireTableDataChanged();
			return itemIdx < seqItems.size() ? itemIdx : seqItems.size() - 1;
		}
		return -1;
	}
	
	public int autoFillItems(int idx, int cnt) {
		int page = 0;
		int r = -1;
		int prevRegIdx = getPrevRegIndex(idx);
		if (prevRegIdx >= 0) {
			Register reg = seqItems.elementAt(prevRegIdx).getRegister();
			page = reg.getPage();
			r = reg.getAddress();
		}
		for (int i=idx; i<idx+cnt; i++)
			setRegister(i, page, ++r, false);
		trimSequence();
		fireTableDataChanged();
		return cnt;
	}
	
	public String getErrorMsg() {
		if (status != SUCCESS)
			return errMsg;
		if (device != null)
			return device.getErrorMsg();
		return null;
	}
}
