package com.idt.devctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedWriter;

import javax.swing.table.AbstractTableModel;

/**
 * @author Vitaliy Kulikov
 *
 */
public class DspRegTableModel extends AbstractTableModel implements DevRegTableModel {
	private static final long serialVersionUID = -8120272910342096983L;

	static final String DEC_VALUE = "[+-]?\\d+";
	static final int REG_NUM_COLUMN = 0;
	static final int ADDR_COLUMN = REG_NUM_COLUMN + 1;
	static final int DVALUE_COLUMN = ADDR_COLUMN + 1;
	static final int HVALUE_COLUMN = DVALUE_COLUMN + 1;
	static final int SUCCESS = 0;
	static final int FAILURE = -1;
	int hBitColumn = -1;
	int lBitColumn = -1;
	int descrColumn = HVALUE_COLUMN + 1;

	private I2CDevice device = null;
	private int page = 0;
	private String errMsg;

	public static boolean isValidDecStr(String val) {
		return val.matches(DEC_VALUE);
	}
	
	private int columnToBit(int col) {
		int bit = lBitColumn - col;
		return bit;
	}

	public boolean isBitColumn(int col) {
		return col >= hBitColumn && col <= lBitColumn;
	}
	
	public void setDevice(I2CDevice device) {
		if (this.device != device) {
			this.device = device;
			page = device.getCurrentDspPage();
			Register reg = device.getDspRegister(0, page);
			if (reg != null) {
				hBitColumn = HVALUE_COLUMN + 1;
				lBitColumn = hBitColumn + reg.getBitDepth() - 1;
				descrColumn = lBitColumn + 1;
			}
			fireTableStructureChanged();
		}
	}
	
	public void setPage(int page) {
		if (device != null) {
			this.page = page;
			Register reg = device.getDspRegister(0, page);
			if (reg != null) {
				hBitColumn = HVALUE_COLUMN + 1;
				lBitColumn = hBitColumn + reg.getBitDepth() - 1;
				descrColumn = lBitColumn + 1;
			}
		}
		fireTableStructureChanged();
	}

	@Override
	public String getToolTip(int row, int col) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChanged(int row, int col) {
		if (device != null) {
			Register reg = device.getDspRegister(row, page);
			if (reg != null)
				return reg.isChanged();
		}
		return false;
	}

	@Override
	public boolean isModified(int row, int col) {
		if (device != null) {
			Register reg = device.getDspRegister(row, page);
			if (reg != null)
				return reg.isModified();
		}
		return false;
	}

	@Override
	public boolean isMaskCell(int row, int col) {
		return false;
	}

	@Override
	public boolean isBitCell(int row, int col) {
		return isBitColumn(col);
	}

	@Override
	public boolean mergeWithRightCell(int row, int col) {
		return isBitColumn(col) && col != lBitColumn;
	}

	@Override
	public int getColumnCount() {
		return descrColumn + 1;
	}

	@Override
	public int getRowCount() {
		if (device != null)
			return device.getDspRegistersCount(page);
		return 0;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case REG_NUM_COLUMN:
			return "Reg(d)";
		case ADDR_COLUMN:
			return "Reg(h)";
		case DVALUE_COLUMN:
			return "Val(d)";
		case HVALUE_COLUMN:
			return "Val(h)";
		}
		if (col == descrColumn)
			return "Description";
		return String.format("%d", columnToBit(col));
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		if (device != null) {
			Register reg = device.getDspRegister(row, page);
			if (reg != null) {
				switch (col) {
				case REG_NUM_COLUMN:
					return reg.getAddressStr();
				case ADDR_COLUMN:
					return reg.getAddressHexStr();
				case DVALUE_COLUMN:
					return reg.getValueDecStr();
				case HVALUE_COLUMN:
					return reg.getValueHexStr();
				}
				if (col == descrColumn)
					return reg.getDescription();
				if (isBitColumn(col))
					return reg.getBit(columnToBit(col));
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (device != null) {
			Register reg = device.getDspRegister(row, page);
			return reg != null && (isBitColumn(col) || col == DVALUE_COLUMN || col == HVALUE_COLUMN);
		}
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (device != null) {
			Register reg = device.getDspRegister(row, page);
			if (reg != null) {
				String val = value.toString();
				if (!val.isEmpty()) {
					if (isBitColumn(col)) {
						if (Register.isValidBitValue(val)) {
							reg.modifyBit(columnToBit(col), val);
							fireTableDataChanged();
						}
					} else if (col == DVALUE_COLUMN ) {
						if (isValidDecStr(val)) {
							reg.modifyValue(val, 10);
							fireTableDataChanged();
						}
					} else if (col == HVALUE_COLUMN ) {
						if (Register.isValidHexStr(val)) {
							reg.modifyValue(val, 16);
							fireTableDataChanged();
						}
					}
				} else {
					/* undo modification */
					if (col == DVALUE_COLUMN) {
						reg.modifyValue(val, 0);
						fireTableDataChanged();
					} else if (col == HVALUE_COLUMN) {
						reg.modifyValue(val, 0);
						fireTableDataChanged();
					} else if (isBitColumn(col)) {
						reg.modifyBit(columnToBit(col), val);
						fireTableDataChanged();
					}
				}
			}
		}
	}

	public String getErrorMsg() {
		if (device != null)
			return device.getErrorMsg();
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
	
	public int loadFromFile(File file) {
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			int reg = 0;
			while (sc.hasNextLine() && reg <= 205) {
				String ln = sc.nextLine();
				device.getDspRegister(reg, page).modifyValue(ln, 16);
				reg++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sc.close();
		fireTableDataChanged();
		return SUCCESS;
	}
		/*
		if (device != null) {
			boolean valid = true;
			int[] eqOffsets = {0, 0x40 };
			int[] chOffsets = { 0, 0x20 };
			int[] bOffsets = { 0, 1, 2 };
			int[] aOffsets = {3, 4 };
			int NonLinearBase = 0x80;
			int eqCnt = 2;
			int chCnt = 2;
			int biQuadCnt = 6;
			int biQuadSz = 5;
			int eq = -1;			//if EQ, rest of ln;
			int chn = -1;			//if EQ, reset; if CH, first char after CH, 
			int bnd = -1;			//if EQ, reset; if CH, ln[8]+;
			int an = -1;			//if A, first char after A;
			int bn = -1;			//if B, first char after B;
			int addr = 0;			//if RAM, first word after RAM in base 16
			String val;				//if RAM, second word after RAM
			String[] eqSet = new String[device.getDspRegistersCount(page)];
			try {
				StringBuilder sb = new StringBuilder(100);
				FileReader fileReader = new FileReader(file);
				String ln = null;
				int ich;
				
				while ((ich = fileReader.read()) > 0) { //reads one char at a time
					char ch = (char)ich;
					if (ch != '\n') {                   //if not a line return, append to StringBuilder
						if (ch != '\r')		
							sb.append(ch);
					} else {							//if \n or \r, StringBuilder -> ln
						ln = sb.toString();
						if (!valid) {					//if invalid, check if ln is device name and set true if so
							if (ln.equals(device.getName())) {
								valid = true;					
							}							
						} else {
							if (ln.startsWith("EQ") && ln.length() > 2) {	//if ln starts with EQ, eq = rest of ln as int
					    		eq = Integer.parseInt(ln.substring(2));
					    		chn = -1;
					    		bnd = -1;
							} else if (ln.startsWith("CH") && ln.length() > 8) {	//if ln starts with CH and longer than 8, 
								chn = Integer.parseInt(ln.substring(2, 3));			//chn = first char after CH as int
								bnd = Integer.parseInt(ln.substring(8));			//bnd = everything after 8th char of ln
							} else if (ln.startsWith("A") && ln.length() > 1) {
								an = Integer.parseInt(ln.substring(1, 2));			//an = 2nd char of ln as int
								String[] tokens = ln.split("[\\s]+");				//StringArray of "words" in ln
								if (tokens.length > 1)								
									eqSet[eqOffsets[eq] + chOffsets[chn] + bnd * biQuadSz + aOffsets[an-1]] = tokens[1];	//?????
							} else if (ln.startsWith("B") && ln.length() > 1) {		//if ln starts with B, bn = first char ater B as int
								bn = Integer.parseInt(ln.substring(1, 2));			//only difference with A is
								String[] tokens = ln.split("[\\s]+");				//bOffsets[bn] vs aOffsets[an-1]
								if (tokens.length > 1)
									eqSet[eqOffsets[eq] + chOffsets[chn] + bnd * biQuadSz + bOffsets[bn]] = tokens[1]; //?????
							} else if (ln.startsWith("RAM") && ln.length() > 8) { /* Added by HMB for non */ /*
								String[] tokens = ln.split("[\\s]+");				//if ln starts with RAM, StringArray of words in ln
								if(tokens.length > 2)
									addr = Integer.parseInt(tokens[1],16);			//addr = first word in base 16 as int
								val = tokens[2];									//val = second word
								eqSet[addr] = val;									//eqSet[addr] = val
								System.out.println(String.format("found %X  %s", addr, eqSet[addr]));		//prints "found addr val"						
						    }
						}
					    sb.setLength(0);	//reset StringBuilder
					}
				}
				fileReader.close();
			} catch(IOException ioEx) {
				System.out.println(ioEx.getMessage());
				errMsg = ioEx.getMessage();
				return FAILURE;
			} catch(Throwable thr) {
				System.out.println(thr.getMessage());
			} finally {
				int reg;			//address in eqSet
				for (int i = 0; i < eqCnt; i++) {							//eqCnt = 2
					for (int j = 0; j < chCnt; j++) {						//chCnt = 2
						for (int n = 0; n < (biQuadCnt * biQuadSz); n++) {	//bQC = 6, bQS = 5
							reg = eqOffsets[i] + chOffsets[j] + n;			//0 or 0x40; 0 or 0x20; 0-29
							if(eqSet[reg] != null)							//skip if null
							{
								System.out.println(String.format("r %X v %s", reg, eqSet[reg]));	//print reg, val@reg
								device.getDspRegister(reg, page).modifyValue(eqSet[reg], 10);		//
							}
						}
					}
				}
				/* added by HMB *//*
				for (int i = NonLinearBase; i < device.getDspRegistersCount(page); ++i )
				{
					if(eqSet[i] != null)
					{
						device.getDspRegister(i, page).modifyValue(eqSet[i], 10);
						System.out.println(String.format("r %X v %s", i, eqSet[i]));
					}
				}
			}
			fireTableDataChanged();
			return SUCCESS;
		}
		throw new IllegalArgumentException("Invalid device " + device);
		
		
	}*/
	
	public boolean saveToFile(File file) {
		try {
			FileWriter fileWriter;
			if (file.getName().endsWith(".config")) {
				fileWriter = new FileWriter(file, true);
			}
			else {
				fileWriter = new FileWriter(file);}
			//fileWriter.write(device.getName());
			//fileWriter.write('\n');
			for (int i = 0; i < device.getDspRegistersCount(page); i++)
			{
				//fileWriter.write(Integer.toHexString(i).toUpperCase()+ ": 0x");
				fileWriter.write(device.getDspRegister(i, page).getValueHexStr());
				fileWriter.write('\n');
			}
			fileWriter.close();
		} catch(IOException ioEx) {
			System.out.println(ioEx.toString());
			return false;
		}
		iregtoregv(file);
		return true;
	}
	public void iregtoregv(File file) {
		Scanner sc = null;
		FileWriter fileWriter = null;
		try {
			sc = new Scanner(file);
			try {
				fileWriter = new FileWriter(file + ".regv");
				fileWriter.write(device.getName());
				fileWriter.write('\n');
				fileWriter.write(String.format("%s:%02X %s:%02x %s:%d #%s\n", 
						Register.REG_ADDR_TAG, 64, 
						Register.REG_VAL_TAG, 0, 
						Register.PAGE_TAG, 0, 
						"N/A"));	
				while (sc.hasNextLine()) {
					String val = sc.nextLine();
					String hi = val.substring(0,2);
					String mid = val.substring(2,4);
					String lo = val.substring(4,6);
					//fileWriter.write("string: " + val +"\nhi: " +hi+"\nmid: "+mid+"\nlo: "+lo+"\n");
					fileWriter.write(String.format("%s:%02X %s:"+lo+" %s:%d #%s\n", 
						Register.REG_ADDR_TAG, 58, 
						Register.REG_VAL_TAG, 
						Register.PAGE_TAG, 0, 
						"N/A"));
					fileWriter.write(String.format("%s:%02X %s:"+mid+" %s:%d #%s\n", 
						Register.REG_ADDR_TAG, 59, 
						Register.REG_VAL_TAG, 
						Register.PAGE_TAG, 0, 
						"N/A"));
					fileWriter.write(String.format("%s:%02X %s:"+hi+" %s:%d #%s\n", 
						Register.REG_ADDR_TAG, 60, 
						Register.REG_VAL_TAG, 
						Register.PAGE_TAG, 0, 
						"N/A"));
				}	
				fileWriter.close();
			} catch(IOException ioEx) {
				System.out.println(ioEx.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sc.close();
	}
}
