package com.idt.devctrl;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Vitaliy Kulikov
 *
 */
class I2CDevice implements StateController, Settings {
	static final boolean DEBUG = true;
	static final long BYTE_MASK = 0xFF;
	static final int PAGE_SIZE = 256;
	static final int SUCCESS = 0;
	static final int FAILURE = -1;
	static final int DSP_CYRUS = 1;
	static final int DSP_WOLVERINE = 2;
	static final int DSP_STORM = 3;
	static final int DSP_TINY_DSP = 4;
	
	private I2CDeviceAccess hwAccess = null;
	private Register[][] registers = null;
	private Register[][] dspRegisters = null;
	//private int indirPage = -1;
	private int dspRWStatusReg = -1;
	private int dspRdStatusBit = 0;
	private int dspWrStatusBit = 0;
	private int dspAccessPage[] = null;
	private int dspAddrRegister = -1;
	private int dspWrDataReg = -1;
	private int dspRdDataReg = -1;
	private int extDspRWStatusReg = -1;
	private int extDspRdStatusBit = 0;
	private int extDspWrStatusBit = 0;
	private int extDspAccessPage = -1;
	private int extDspAddrRegister = -1;
	private int extDspWrDataReg = -1;
	private int extDspRdDataReg = -1;
	private int dspType = 0;
	private Register[] writeProtected = null;
	
	private String infoStr = null;
	private String name = null;
	private boolean validated;

	private int devAddr = 0;
	private int regAddrSz = 8;
	private boolean regAddrMsbF = false;
	private int regValSz = 8;
	private int pageCnt = 0;
	private int pageReg = -1;
	private int currPage = 0;
	private int currDspPage = 0;
	private String[] dspPageNames = null;
	private int resetReg = -1;
	private long resetMask = 0;
	private long compatDeviceId = -1;
	private long deviceId = -1;
	private int devIdRegLow = -1;
	private int devIdRegHigh = -1;
	private long devCompatIdMask = 0;
	private long revision = 0;
	private int revisionReg = -1;
	private long revRegMask = 0;
	private int subSysReg = -1;
	private int subSysRegMask = 0;
	private byte[] rwBuf = null;
	private byte[] pgBuf = new byte[1];
	private Vector<ChangeListener> changeListeners = null;
	private int status = SUCCESS;
	private String errMsg = null;
	/*** Settings ***/
	private int settingsCnt = 1;
	private int newAddress;
	private int newRegAddrSz;
	private int newRegValSz;
	
	public I2CDevice(String devName) {
		name = devName;
		rwBuf = new byte[65536];
		loadDefinition();
	}
	
	public void addChangeListener(ChangeListener changeListener) {
		if (changeListeners == null)
			changeListeners = new Vector<ChangeListener>();
		changeListeners.add(changeListener);
	}
	
	private void clear() {
		if (registers != null) {
			for (int i = 0; i < registers.length; i++) {
				for (int j = 0; j < registers[i].length; j++) {
					if (registers[i][j] != null)
						registers[i][j].clear();
					}
			}
		}
	}
	
	private void reinit() {
		loadDefinition();
	}
	
	public void close() {
		hwAccess.close();
	}
	
	public void fireChangeEvent(ChangeEvent changeEvent) {
		for (ChangeListener changeListener: changeListeners)
			changeListener.stateChanged(changeEvent);
	}
	
	public String getAccessStatus() {
		return hwAccess.getStatus(); 
	}
	
	public String getErrorMsg() {
		if (status != SUCCESS)
			return errMsg;
		if (hwAccess != null)
			return hwAccess.getErrorMessage();
		return null;
	}
	
	public Settings getHwAccessSettings() {
		return hwAccess;
	}
	
	public Register getDspRegister(int idx, int page) {
		if (dspRegisters != null && dspRegisters[page] != null)
			return dspRegisters[page][idx];
		return null;
	}

	public int getDspRegistersCount(int page) {
		if (dspRegisters != null && dspRegisters[page] != null)
			return dspRegisters[page].length;
		return 0;
	}

	public String getName() {
		return name;
	}
	
	public int getPageCount() {
		return pageCnt;
	}
	
	public int getCurrentPage() {
		return currPage;
	}
	
	public int getDspPageCount() {
		return dspPageNames == null ? 0 : dspPageNames.length;
	}

	public String getDspPageName(int idx) {
		return dspPageNames[idx];
	}
	
	public int getCurrentDspPage() {
		return currDspPage;
	}

	private int getReadRangeAt(Register[] regs, int startIdx, int limit) {
		int i;
		for (i=startIdx; i<regs.length && i<limit; i++) {
			if (regs[i] == null
				|| !(i == startIdx || regs[i].getAddress() == (regs[startIdx].getAddress() + i - startIdx) && regs[i].getPage() == regs[startIdx].getPage())) {
				break;
			}
		}
		return i - startIdx;
	}
	
	public Register getRegister(int page, int reg) {
		if (registers != null) {
			return registers[page][reg];
		}
		return null;
	}
	
	public String getStatus() {
		//if (infoStr == null)
			updateStatus();
		return infoStr != null ? infoStr : " "; 
	}
	
	private int getWriteRangeAt(Register[] regs, int startIdx, int limit, boolean modifiedOnly) {
		int i;
		for (i=startIdx; i<regs.length && i<limit; i++) {
			if (regs[i] == null
					|| isWriteProtected(regs[i])
					|| !(regs[i].isModified() || (!modifiedOnly && regs[i].isInitialized()))
					|| !(i == startIdx || regs[i].getAddress() == (regs[startIdx].getAddress() + i - startIdx) && regs[i].getPage() == regs[startIdx].getPage())) {
				break;
			}
		}
		return i - startIdx;
	}

	public boolean isModified(int page) {
		if (registers != null && pageCnt > 0) {
			if (page >= 0) {
				for (Register reg: registers[page]) {
					if (reg != null && reg.isModified()) {
						return true;
					}
				}
			} else {
				for (int i = 0; i < registers.length; i++) {
					for (int j = 0; j < registers[i].length; j++) {
						if (registers[i][j] != null && registers[i][j].isModified()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isResetSupported() {
		return resetReg >= 0 && resetReg < PAGE_SIZE;
	}
	
	boolean isValidPage(int page) {
		return page >= 0 && page < pageCnt;
	}

	boolean isValidRegister(int page, int reg) {
		return page >= 0 && page < registers.length && reg >= 0 && reg < registers[page].length;
	}
	
	private boolean isWriteProtected(Register reg) {
		if (writeProtected != null) {
			for (Register noWrReg: writeProtected) {
				if (noWrReg.equals(reg))
					return true;
			}
		}
		return false;
	}
	
	private boolean loadDefinition() {
		if (name != null) {
			if (name.equalsIgnoreCase("Cyrus")) {
				devAddr = 0x2A;
				pageCnt = 4;
				pageReg = 1;
				resetReg = 0;
				resetMask = 0x80;
				devIdRegLow = 0;
				devIdRegHigh = -1;
				revisionReg = 0x1F;
				compatDeviceId = 0x55;
				
				registers = new Register[pageCnt][PAGE_SIZE];
				for (int i = 0; i < 234; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 0xA0; i < 0xF0; i++) {
					registers[1][i] = new Register(this, 1, i, regAddrSz, regValSz);
				}
				for (int i = 0x10; i < 0x40; i++) {
					registers[2][i] = new Register(this, 2, i, regAddrSz, regValSz);
				}
				for (int i = 16; i < 24; i++) {
					registers[3][i] = new Register(this, 3, i, regAddrSz, regValSz);
				}
				
				dspRegisters = new Register[1][52];
				for (int i = 0; i < dspRegisters[0].length; i++) {
					dspRegisters[0][i] = new Register(this, 2, i, regAddrSz, 24);
				}
				dspRWStatusReg = 0x3E;
				dspRdStatusBit = 0x80;
				dspWrStatusBit = 0x40;
				dspAddrRegister = 0x3C;
				dspRdDataReg = 0x36;
				dspWrDataReg = 0x39;
				dspType = DSP_CYRUS;
				
				writeProtected = new Register[] {registers[0][0], registers[0][1], registers[0][0x31], registers[2][0x39], registers[2][0x3A], registers[2][0x3B]};
				
				infoStr = null;
				return true;
			} else if (name.equalsIgnoreCase("Wookie")) { // Keith
				devAddr = 0x71;
				pageCnt = 1;
				resetReg = 0x80;
				resetMask = 0x85;
				devIdRegLow = 0x7D;
				devIdRegHigh = 0x7E;
				compatDeviceId = 0x2200;
				devCompatIdMask = 0xff00;
				revisionReg = 0x7F;
				registers = new Register[pageCnt][PAGE_SIZE];
				for (int i = 0; i < 70; i++) { //Keith
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 78; i < 88; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 96; i < 98; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}

				for (int i = 103; i < 129; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 136; i < 150; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 160; i < 166; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 176; i < 177; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 192; i < 221; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				registers[0][221] = new Register(this, 0, 221, regAddrSz, regValSz);
				
				dspPageNames = new String[] { "0" };
				dspRegisters = new Register[1][0xCE];
				for (int i = 0; i < dspRegisters[0].length; i++) {
					dspRegisters[0][i] = new Register(this, 0, i, regAddrSz, 24);
				}
				dspRWStatusReg = 0x8A;
				dspRdStatusBit = 0x80;
				dspWrStatusBit = 0x80;
				dspAddrRegister = 0x40;
				dspRdDataReg = 0x3D;
				dspWrDataReg = 0x3A;
				dspType = DSP_WOLVERINE;
				
//				writeProtected = new Register[] {registers[0][0x7F], registers[0][0x3A], registers[0][0x3B], registers[0][0x3C]};
				writeProtected = new Register[] {registers[0][0x7F]};				
				infoStr = null;
				return true;
			} else if (name.equalsIgnoreCase("Storm")) {
				devAddr = 0x68;
				pageCnt = 11;
				pageReg = 0;
				resetReg = 1;
				resetMask = 0x85;
				devIdRegLow = 8;
				devCompatIdMask = 0xf8;
				compatDeviceId = 0x40;
				revisionReg = 0x9;
				registers = new Register[pageCnt][PAGE_SIZE];
				for (int i = 0; i <= 0x37; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x22; i++) {
					registers[1][i] = new Register(this, 1, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x1D; i++) {
					registers[2][i] = new Register(this, 2, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x35; i++) {
					registers[3][i] = new Register(this, 3, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x35; i++) {
					registers[4][i] = new Register(this, 4, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x35; i++) {
					registers[5][i] = new Register(this, 5, i, regAddrSz, regValSz);
				}
				for (int i = 0; i < 1; i++) {
					registers[6][i] = new Register(this, 6, i, regAddrSz, regValSz);
				}
				for (int i = 0; i < 1; i++) {
					registers[7][i] = new Register(this, 7, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0x1F; i++) {
					registers[8][i] = new Register(this, 8, i, regAddrSz, regValSz);
				}
				for (int i = 0; i < 1; i++) {
					registers[9][i] = new Register(this, 9, i, regAddrSz, regValSz);
				}
				for (int i = 0; i <= 0xAE; i++) {
					registers[10][i] = new Register(this, 10, i, regAddrSz, regValSz);
				}
				
				dspPageNames = new String[] { "S", "H", "X", "Y", "Z" };
				dspRegisters = new Register[dspPageNames.length][];
				dspRegisters[0] = new Register[0xCE];
				dspRegisters[1] = new Register[0xCE];
				for (int i = 0; i < dspRegisters[0].length; i++) {
					dspRegisters[0][i] = new Register(this, 0, i, regAddrSz, 24);
					dspRegisters[1][i] = new Register(this, 1, i, regAddrSz, 24);
				}
				dspAccessPage = new int[] {0x3, 0x4};
				dspRWStatusReg = 0x09;
				dspRdStatusBit = 0x80;
				dspWrStatusBit = 0x80;
				dspAddrRegister = 0x08;
				dspRdDataReg = 0x05;
				dspWrDataReg = 0x02;

				dspRegisters[2] = new Register[0x1000];
				dspRegisters[3] = new Register[0x1000];
				dspRegisters[4] = new Register[0x1000];
				for (int i = 0; i < dspRegisters[2].length; i++) {
					dspRegisters[2][i] = new Register(this, 2, 0x1000+i, regAddrSz, 24);
					dspRegisters[3][i] = new Register(this, 3, 0x2000+i, regAddrSz, 24);
					dspRegisters[4][i] = new Register(this, 4, 0x3000+i, regAddrSz, 24);
				}
				extDspAccessPage = 0x8;
				extDspRWStatusReg = 1;
				extDspRdStatusBit = 4;
				extDspWrStatusBit = 2;
				extDspAddrRegister = 0x2;
				extDspRdDataReg = 0xE;
				extDspWrDataReg = 0xE;
				dspType = DSP_STORM;

				infoStr = null;
				return true;
			} else if (name.equalsIgnoreCase("Tiny DSP")) {
				devAddr = 0x69;
				pageCnt = 1;
				pageReg = -1;
				resetReg = -1;
				resetMask = 0;
				devIdRegLow = 0;
				compatDeviceId = 0xB81A;
				devCompatIdMask = 0xffff;
				revisionReg = 0x4;
				regAddrSz = 16;
				regValSz = 16;
				registers = new Register[pageCnt][PAGE_SIZE];
				for (int i = 0; i < 0x2F; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				
				dspPageNames = new String[] { "X", "Y", "Z", "I", "A" };
				dspRegisters = new Register[dspPageNames.length][];
				dspRegisters[0] = new Register[0x1000];
				dspRegisters[1] = new Register[0x1000];
				dspRegisters[2] = new Register[0x1000];
				dspRegisters[3] = new Register[0x1000];
				dspRegisters[4] = new Register[0x2000];
				for (int i = 0; i < dspRegisters[0].length; i++) {
					dspRegisters[0][i] = new Register(this, 0, 0x1000+i, regAddrSz, 24);
					dspRegisters[1][i] = new Register(this, 1, 0x2000+i, regAddrSz, 24);
					dspRegisters[2][i] = new Register(this, 2, 0x3000+i, regAddrSz, 24);
				}
				for (int i = 0; i < dspRegisters[3].length; i++) {
					dspRegisters[3][i] = new Register(this, 3, 0x6000+i, regAddrSz, 10);
				}
				for (int i = 0; i < dspRegisters[4].length; i++) {
					dspRegisters[4][i] = new Register(this, 4, 0x8000+i, regAddrSz, 50);
				}
				dspAddrRegister = 0x1000; //that's actually base address of DSP registers pool
				dspType = DSP_TINY_DSP;
				infoStr = null;
				return true;
			} else if (name.equalsIgnoreCase("Unknown")) {
				pageCnt = 1;
				registers = new Register[pageCnt][PAGE_SIZE];
				for (int i = 0; i < registers[0].length; i++) {
					registers[0][i] = new Register(this, 0, i, regAddrSz, regValSz);
				}
				regAddrMsbF = true;
				settingsCnt = 3;
				infoStr = null;
				return true;
			}
		}
		return false;
	}
	
	public int loadFromFile(File file) {
		if (pageCnt > 0 && registers != null) {
			boolean valid = false;
			int reg;
			String val;
			int page = 0;
			BufferedReader fileReader = null;
			try {
				fileReader = new BufferedReader(new FileReader(file));
				String ln;
				
				while ((ln = fileReader.readLine()) != null) {
					if (!ln.startsWith("#") && ln.length() > 0) {
						if (!valid) {
							if (ln.equals(name))
								valid = true;
						} else {
							if (ln.equals("WRITE")) {
							//	writeAll();
							}
							else {
								reg = -1;
								val = null;
								String[] tokens = ln.split("[:;#=\\s]+");
								for (int i=0; i<tokens.length-1; i+=2) {
									if (tokens[i].equalsIgnoreCase(Register.REG_ADDR_TAG)) {
										reg = Integer.parseInt(tokens[i+1], 16);
									} else if (tokens[i].equalsIgnoreCase(Register.REG_NUM_TAG)) {
										reg = Integer.parseInt(tokens[i+1]);
									} else if (tokens[i].equalsIgnoreCase(Register.REG_VAL_TAG)) {
										val = tokens[i+1];
									} else if (tokens[i].equalsIgnoreCase(Register.PAGE_TAG)) {
										page = Integer.parseInt(tokens[i+1]);
									}
								}		
								if (isValidRegister(page, reg)) {
									Register register = getRegister(page, reg);
									if (register != null && val != null && register.isValidValue(val)) {
										register.modifyValue(val, 16);
									} else {
										status = FAILURE;
										errMsg = file.getName() + " - Invalid reg or data: " + String.format("p %x r %02X v %s", page, reg, val);
										return -1;
									}
								}
							}
						}
					}
				}
				if (!valid) {
		    		status = FAILURE;
		    		errMsg = file.getName() + " - Register values file is not for this device";
		    		return -1;
				}
			} catch(IOException ioEx) {
				System.out.println(ioEx.toString());
	    		status = FAILURE;
				errMsg = ioEx.toString();
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
			status = SUCCESS;
			return page;
		}
		throw new IllegalArgumentException("Invalid device state regs " + registers + " pages " + pageCnt);
	}
	
	public boolean loadPreset(File file) {
		if (pageCnt > 0 && registers != null) {
			boolean valid = false;
			char type = 0;
			int reg = -1;
			String val = null;
			int page = -1;
			BufferedReader fileReader = null;
			try {
				fileReader = new BufferedReader(new FileReader(file));
				String ln;
				
				while ((ln = fileReader.readLine()) != null) {
					ln = ln.trim();
					if (ln.length() > 0) {
						if (!valid) {
							if (ln.equals(name))
								valid = true;
						} else {
							if (ln.startsWith("//")) {
								if (ln.contains("DSP")) {
									type = 'd';
									reg = 0;
									int pgIdx = ln.indexOf("pg");
									if (pgIdx > 0) {
										String pgName = ln.substring(pgIdx+2, pgIdx+3);
										for (int i = 0; i < dspPageNames.length; i++)
											if (pgName.startsWith(dspPageNames[i])) {
													page = i;
													System.out.println("DSP for page "+pgName+"/"+String.valueOf(page));
													break;
											}
									}
								} else if (ln.contains("REGS")) {
									type = 'r';
									int pgIdx = ln.indexOf("pg");
									if (pgIdx > 0) {
										page = Integer.parseInt(ln.substring(pgIdx+2, pgIdx+3));
										System.out.println("reg values for page "+String.valueOf(page));
									}
								}
								continue;
							}
							if (type == 'd') {
								String[] tokens = ln.split("(\\s*,\\s*)|\\s");
							    for (int i = 0; i < tokens.length; i++) {
									val = null;
							    	if (tokens[i].length() > 0) {
							    		if (tokens[i].startsWith("0xff"))
							    			val = tokens[i].substring("0xff".length());
							    		else if (tokens[i].startsWith("0x"))
							    			val = tokens[i].substring("0x".length());
							    		else
							    			System.out.println("Invalid DSP value "+tokens[i]);
								    	dspRegisters[page][reg].modifyValue(val, 16);
								    	reg++;
							    	}
							    }
							}
							if (type == 'r') {
								String[] tokens = ln.split("(\\s*{\\s*)|(\\s*,\\s*)|(\\s*}\\s*)");
								if (tokens.length > 1) {
									reg = Integer.decode(tokens[0]);
									val = tokens[1];
									if (isValidRegister(page, reg)) {
										Register register = getRegister(page, reg);
										if (register != null && val != null && register.isValidValue(val)) {
											register.modifyValue(val, 16);
										} else {
											status = FAILURE;
											errMsg = file.getName() + " - Invalid registers data";
											return false;
										}
									}
							    }
						    }
						}
					}
				}
				if (!valid) {
		    		status = FAILURE;
		    		errMsg = file.getName() + " - Preset file is not for this device";
		    		return false;
				}
			} catch(IOException ioEx) {
				System.out.println(ioEx.toString());
	    		status = FAILURE;
				errMsg = ioEx.toString();
				return false;
			} finally {
				if (fileReader != null)
					try {
						fileReader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			status = SUCCESS;
			return true;
		}
		throw new IllegalArgumentException("Invalid device state regs " + registers + " pages " + pageCnt);
	}

	/*public int loadFromConfig (File file) {

	}*/
	
	public boolean read(int page) {
		if (!validHw()) {
			return false;
		}
		return readHw(registers[page]);
	}
	
	public long readHw(int page, int reg) {
		assert pageCnt > 0;
		if (page != currPage) {
			assert pageReg >= 0;
			assert regAddrSz == 8;
			pgBuf[0] = (byte)page;
			if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, pageReg, pgBuf, 1)) {
				currPage = page;
			} else {
				return -1;
			}
		}
		int regSz = (regValSz+7)/8;
		if (!hwAccess.read(devAddr, regAddrSz, regAddrMsbF, reg, rwBuf, regSz)) {
			hwAccess.close();
			return -1;
		}
		long val = 0;
		for (int i=0; i<regSz; i++)
			val |= (rwBuf[i] & BYTE_MASK) << i*8;
		return val;
	}
	
	public boolean readHw(Register reg) {
		long val = readHw(reg.getPage(), reg.getAddress());
		if (val < 0) {
			hwAccess.close();
			return false;
		}
		reg.setValue(val);
		return true;
	}
	
	public boolean readHw(Register[] regs) {
		return readHw(regs, regs.length);
	}
	
	/*
	 * This method tries to use batch reads as much as possible
	 */
	public boolean readHw(Register[] regs, int regsNum) {
		int count = 0;
		int page; 
		for (int i=0; i<regs.length && i<regsNum; i += count > 0 ? count : 1) {
			count = getReadRangeAt(regs, i, regsNum);
			if (count > 0) {
				page = regs[i].getPage();
				if (page != currPage) {
					assert page < pageCnt && pageReg >= 0;
					assert regAddrSz == 8;
					pgBuf[0] = (byte)page;
					if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, pageReg, pgBuf, 1)) {
						currPage = page;
					} else {
						hwAccess.close();
						return false;
					}
				}
				int regSz = (regValSz+7)/8;
				if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, regs[i].getAddress(), rwBuf, count*regSz)) {
					for (int j=0; j<count; j++) {
						long val = 0;
						for (int n=0; n<regSz; n++)
							val |= (rwBuf[j*regSz+n] & BYTE_MASK) << n*8;
						regs[i+j].setValue(val);
					}
				} else {
					hwAccess.close();
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean resetToPage0() {
		long pg = 0;
		if (pageReg >= 0 && currPage != 0)
			pg = readHw(0, pageReg);
		return pg == 0;
	}
	
	public boolean resetHW() {
		if (isResetSupported()) {
			return writeHw(0, resetReg, resetMask, resetMask);
		}
		return false;
	}
	
	/*
	 * Settings (non-Javadoc)
	 */

	public boolean saveToFile(int page, File file, boolean modified) {
		if (pageCnt > 0 && registers != null) {
			try {
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(name);
				fileWriter.write('\n');
				for (Register reg: registers[page]) {
					if (reg != null && (reg.isModified() || (!modified && reg.isInitialized()))) {
						fileWriter.write(reg.toString());
						fileWriter.write('\n');
					}
				}
				fileWriter.close();
			} catch(IOException ioEx) {
				System.out.println(ioEx.toString());
				return false;
			}
			return true;
		}
		throw new IllegalArgumentException("Invalid device state regs " + registers + " pages " + pageCnt);
	}
	
	public boolean setAccessMethod(String accessMethod) {
		if (hwAccess == null || !accessMethod.equals(hwAccess.getMethod())) {
			if (accessMethod.equals(I2CDeviceAccess.FTDICHIP)) {
				if (hwAccess != null) {
					hwAccess.close();
					clear();
				}
				hwAccess = new FTCI2CDeviceAccess();
				return true;
			} else {
				throw new IllegalArgumentException("Invalid access method " + accessMethod);
			}
		}
		return false;
	}

	private void updateStatus() {
		StringBuilder strBldr = new StringBuilder(100);
		strBldr.append(" DEVICE:");
		if (registers != null) {
			if (validated) {
				strBldr.append(String.format(" %04X/%02X", deviceId, revision));
			}
			strBldr.append(" @ ");
			strBldr.append(String.format("%02Xh", devAddr));
		} else {
			strBldr.append(" NOT SELECTED");
		}
		infoStr = strBldr.toString();
	}

	private boolean validHw() {
		long devId = 0;
		if (devIdRegLow >=0 && !validated) {
			deviceId = devId = readHw(0, devIdRegLow);
			if (devId < 0)
				return false;
			if (devIdRegHigh >=0 ) {
				devId = readHw(0, devIdRegHigh);
				if (devId < 0)
					return false;
				deviceId |= devId << 8;
			}
//			if ((deviceId & devCompatIdMask) != compatDeviceId) {
//				status = FAILURE;
//				errMsg = String.format("Invalid device %04X", deviceId);
//				return false;
//			}
			if (revisionReg >= 0)
				revision = readHw(0, revisionReg);
			validated = true;
		}
		return true;
	}

	public boolean write(int page, boolean modifiedOnly) {
		if (!validHw()) {
			return false;
		}
		return writeHw(registers[page], modifiedOnly);
	}

	public boolean writeHw(int page, int reg, long val) {
		assert pageCnt > 0;
		if (page != currPage) {
			assert pageReg >= 0;
			assert regAddrSz == 8;
			pgBuf[0] = (byte)page;
			if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, pageReg, pgBuf, 1)) {
				currPage = page;
			} else {
				hwAccess.close();
				return false;
			}
		}
		int regSz = (regValSz+7)/8;
		for (int i=0; i<regSz; i++) {
			rwBuf[i] = (byte)val;
			val >>= 8;
		}
		if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, reg, rwBuf, regSz)) {
			hwAccess.close();
			return false;
		}
		return true;
	}

	public boolean writeHw(int page, int reg, long val, long mask) {
		assert pageCnt > 0;
		if (page != currPage) {
			assert pageReg >= 0;
			assert regAddrSz == 8;
			pgBuf[0] = (byte)page;
			if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, pageReg, pgBuf, 1)) {
				currPage = page;
			} else {
				hwAccess.close();
				return false;
			}
		}
		int regSz = (regValSz+7)/8;
		if (!hwAccess.read(devAddr, regAddrSz, regAddrMsbF, reg, rwBuf, regSz)) {
			hwAccess.close();
			return false;
		}
		for (int i=0; i<regSz; i++) {
			rwBuf[i] = (byte)(rwBuf[i] & ~mask | val & mask);
			val >>= 8;
			mask >>= 8;
		}
		if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, reg, rwBuf, regSz)) {
			hwAccess.close();
			return false;
		}
		return true;
	}

	public boolean writeHw(Register reg) {
		if (!writeHw(reg.getPage(), reg.getAddress(), reg.getValue())) {
			hwAccess.close();
			return false;
		}
		reg.commitChanges();
		return true;
	}

	public boolean writeHw(Register reg, long mask) {
		if (mask == reg.getValueMask()) {
			return writeHw(reg);
		} else {
			if (!writeHw(reg.getPage(), reg.getAddress(), reg.getValue(), mask)) {
				hwAccess.close();
				return false;
			}
		}
		reg.commitChanges();
		return true;
	}

	public boolean writeHw(Register[] regs, boolean modifiedOnly) {
		return writeHw(regs, regs.length, modifiedOnly);
	}
	
	/*
	 * This method tries to use block writes as much as possible
	 */
	public boolean writeHw(Register[] regs, int regsNum, boolean modifiedOnly) {
		int count = 0;
		int page;
		for (int i=0; i<regs.length && i<regsNum; i += count > 0 ? count : 1) {
			count = getWriteRangeAt(regs, i, regsNum, modifiedOnly);
			if (count > 0) {
				page = regs[i].getPage();
				if (page != currPage) {
					assert page < pageCnt && pageReg >= 0;
					assert regAddrSz == 8;
					pgBuf[0] = (byte)page;
					if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, pageReg, pgBuf, 1)) {
						currPage = page;
					} else {
						hwAccess.close();
						return false;
					}
				}
				int regSz = regs[i].getByteSize();
				for (int j=0; j<count; j++) {
					for (int n=0; n<regSz; n++)
						rwBuf[j*regSz+n] = (byte)(regs[i+j].getValue() >> n*8);
				}
				if (hwAccess.write(devAddr, regAddrSz, regAddrMsbF, regs[i].getAddress(), rwBuf, count*regSz)) {
					for (int j=0; j<count; j++) {
						regs[i+j].commitChanges();
					}
				} else {
					hwAccess.close();
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean waitOnBits(int page, int reg, long mask, boolean inv) {
		int tocnt = 0;
		long status;
		do {
			if (tocnt++ == 3) {
				if (I2CDevice.DEBUG) {
					System.out.println(String.format("time out on status bits %x:%x(%x)\n", page, reg, mask));
				}
				return false;
			}
			status = readHw(page, reg);
			if (inv)
				status = ~status;
		} while ((status & mask) == 0);
		return true;
	}
	
	public boolean readDspRegister(Register reg) {
		switch (dspType) {
		
		case DSP_WOLVERINE: {
			if (!waitOnBits(reg.getPage(), dspRWStatusReg, dspRdStatusBit, true))
				return false;
			if (!writeHw(reg.getPage(), dspAddrRegister, reg.getAddress())) {
				return false;
			}
			long val = 0;
			int valSz = reg.getByteSize();
			if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, dspRdDataReg, rwBuf, valSz)) {
				for (int j = 0; j < valSz; j++)
					val |= (rwBuf[j] & BYTE_MASK) << (j*8);
			} else {
				return false;
			}
			reg.setValue(val);
			break;
		}
		case DSP_STORM: {
			int dspPg = reg.getPage();
			if (dspPg > 1) {
				writeHw(extDspAccessPage, extDspRWStatusReg, 8, 0x18);
				int dspAddr = reg.getAddress() - dspRegisters[dspPg][0].getAddress();
				dspAddr |= (dspPg - 2) << 14;
				rwBuf[0] = (byte) dspAddr;
				rwBuf[1] = (byte) (dspAddr >> 8);
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, extDspAddrRegister, rwBuf, 2)) {
					return false;
				}
				if (!waitOnBits(extDspAccessPage, extDspRWStatusReg, extDspRdStatusBit, false))
					return false;
				long val = 0;
				int valSz = reg.getByteSize();
				if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, extDspRdDataReg, rwBuf, valSz)) {
					for (int j = 0; j < valSz; j++)
						val |= (rwBuf[j] & BYTE_MASK) << (j*8);
				} else {
					return false;
				}
				reg.setValue(val);
				writeHw(extDspAccessPage, extDspRWStatusReg, 0, 0x8);
			} else {
				if (!waitOnBits(dspAccessPage[reg.getPage()], dspRWStatusReg, dspRdStatusBit, true))
					return false;
				if (!writeHw(dspAccessPage[reg.getPage()], dspAddrRegister, reg.getAddress())) {
					return false;
				}
				long val = 0;
				int valSz = reg.getByteSize();
				if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, dspRdDataReg, rwBuf, valSz)) {
					for (int j = 0; j < valSz; j++)
						val |= (rwBuf[j] & BYTE_MASK) << (j*8);
				} else {
					return false;
				}
				reg.setValue(val);
			}
			break;
		}
		case DSP_TINY_DSP: {
			int dspAddr = reg.getAddress();
			long val = 0;
			int valSz = reg.getByteSize();
			if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, dspAddr, rwBuf, valSz)) {
				for (int j = 0; j < valSz; j++)
					val |= (rwBuf[j] & BYTE_MASK) << (j*8);
			} else {
				return false;
			}
			reg.setValue(val);
			break;
		}
		case DSP_CYRUS: {
			if (!writeHw(reg.getPage(), dspAddrRegister, reg.getAddress())) {
				return false;
			}
			if (!(dspRWStatusReg < 0 || writeHw(reg.getPage(), dspRWStatusReg, dspRdStatusBit))) {
				return false;
			}
			if (!waitOnBits(reg.getPage(), dspRWStatusReg, dspRdStatusBit, true))
				return false;
			long val = 0;
			int valSz = reg.getByteSize();
			if (hwAccess.read(devAddr, regAddrSz, regAddrMsbF, dspRdDataReg, rwBuf, valSz)) {
				for (int j = 0; j < valSz; j++)
					val |= (rwBuf[j] & BYTE_MASK) << (j*8);
			} else {
				return false;
			}
			reg.setValue(val);
		}
		}
		return true;
	}

	public boolean readDspRegisters(int page) {
		assert dspAddrRegister >= 0;
		currDspPage = page;
		if (dspType == DSP_TINY_DSP) {
			int valSz = dspRegisters[page][0].getByteSize();
			if (!hwAccess.read(devAddr, regAddrSz, regAddrMsbF, dspRegisters[page][0].getAddress(), rwBuf, dspRegisters[page].length * valSz))
				return false;
			for (int i = 0; i < dspRegisters[page].length; i++) {
				long val = 0;
				for (int j = 0; j < valSz; j++) {
					val |= (rwBuf[i * valSz + j] & BYTE_MASK) << (j * 8);
				}
				dspRegisters[page][i].setValue(val);
			}
		} else if (dspType == DSP_STORM && page > 1) {
			writeHw(extDspAccessPage, extDspRWStatusReg, 9, 0x19);
			int n = dspRegisters[page].length / 6;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < 6; j++) {
					int dspAddr =  dspRegisters[page][i*6+j].getAddress() - dspRegisters[page][0].getAddress();
					dspAddr |= (page - 2) << 14;
					rwBuf[j*2] = (byte)dspAddr;
					rwBuf[j*2+1] = (byte)(dspAddr >> 8);
				}
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, extDspAddrRegister, rwBuf, 6 * 2))
					return false;
				if (!waitOnBits(extDspAccessPage, extDspRWStatusReg, extDspRdStatusBit, false))
					return false;
				if (!hwAccess.read(devAddr, regAddrSz, regAddrMsbF, extDspRdDataReg, rwBuf, 6 * 3))
					return false;
				for (int j = 0; j < 6; j++) {
					long val = 0;
					for (int k = 0; k < 3; k++)
						val |= (rwBuf[j*3+k] & BYTE_MASK) << (k*8);
					dspRegisters[page][i*6+j].setValue(val);
				}
			}
			writeHw(extDspAccessPage, extDspRWStatusReg, 0, 0x19);
			for (int i = n * 6; i < dspRegisters[page].length; i++)
				if (!readDspRegister(dspRegisters[page][i]))
					return false;
		} else
			for (Register reg : dspRegisters[page]) {
				if (!readDspRegister(reg))
					return false;
			}
		return true;
	}
	
	public boolean writeDspRegister(Register reg)
	{
		long status;
		int toCnt = 0;
		switch (dspType) {
		
		case DSP_WOLVERINE: {
			if (!waitOnBits(reg.getPage(), dspRWStatusReg, dspWrStatusBit, true))
				return false;
			if (!writeHw(reg.getPage(), dspAddrRegister, reg.getAddress())) {
				return false;
			}
			long val = reg.getValue();
			int valSz = reg.getByteSize();
			for (int j=0; j<valSz; j++) {
				rwBuf[j] = (byte) (val & BYTE_MASK);
				val >>= 8;
			}
			if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, dspWrDataReg, rwBuf, valSz)) {
				return false;
			}
			break;
		}
		case DSP_STORM: {
			int dspPg = reg.getPage();
			if (dspPg > 1) {
				writeHw(extDspAccessPage, extDspRWStatusReg, 0x18, 0x18);
				int dspAddr = reg.getAddress() - dspRegisters[dspPg][0].getAddress();
				dspAddr |= (dspPg - 2) << 14;
				rwBuf[0] = (byte) dspAddr;
				rwBuf[1] = (byte) (dspAddr >> 8);
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, extDspAddrRegister, rwBuf, 2)) {
					return false;
				}
				long val = reg.getValue();
				int valSz = reg.getByteSize();
				for (int j = 0; j < valSz; j++) {
					rwBuf[j] = (byte) (val & BYTE_MASK);
					val >>= 8;
				}
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, extDspWrDataReg, rwBuf, valSz)) {
					return false;
				}
				if (!waitOnBits(extDspAccessPage, extDspRWStatusReg, extDspWrStatusBit, false))
					return false;
				writeHw(extDspAccessPage, extDspRWStatusReg, 0, 0x18);
			} else {
				if (!waitOnBits(dspAccessPage[reg.getPage()], dspRWStatusReg, dspWrStatusBit, true))
					return false;
				if (!writeHw(dspAccessPage[reg.getPage()], dspAddrRegister, reg.getAddress())) {
					return false;
				}
				long val = reg.getValue();
				int valSz = reg.getByteSize();
				for (int j=0; j<valSz; j++) {
					rwBuf[j] = (byte) (val & BYTE_MASK);
					val >>= 8;
				}
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, dspWrDataReg, rwBuf, valSz)) {
					return false;
				}
			}
			break;
		} 
		case DSP_TINY_DSP: {
			int dspAddr = reg.getAddress();
			long val = reg.getValue();
			int valSz = reg.getByteSize();
			for (int j = 0; j < valSz; j++) {
				rwBuf[j] = (byte) (val & BYTE_MASK);
				val >>= 8;
			}
			if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, dspAddr, rwBuf, valSz)) {
				return false;
			}
			break;
		}
		case DSP_CYRUS: {
			do {
				if (toCnt++ == 3) {
					if (I2CDevice.DEBUG) {
						System.out.println("DSP reg write failed with timeout\n");
					}
					return false;
				}
				status = readHw(reg.getPage(), dspRWStatusReg);
			} while ((status & dspWrStatusBit) != 0);
			long val = reg.getValue();
			int valSz = reg.getByteSize();
			for (int j=0; j<valSz; j++) {
				rwBuf[j] = (byte) (val & BYTE_MASK);
				val >>= 8;
			}
			if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, dspWrDataReg, rwBuf, valSz)) {
				return false;
			}
			if (!writeHw(reg.getPage(), dspAddrRegister, reg.getAddress())) {
				return false;
			}
			if (!(dspRWStatusReg < 0 || writeHw(reg.getPage(), dspRWStatusReg, dspWrStatusBit))) {
				return false;
			}
		}
		}
		reg.commitChanges();
		return true;
	}

	public boolean writeDspRegisters(int page, boolean modifiedOnly) {
		assert dspAddrRegister >= 0;
		currDspPage = page;
		if (dspType == DSP_TINY_DSP && !modifiedOnly) {
			int valSz = dspRegisters[page][0].getByteSize();
			for (int i = 0; i < dspRegisters[page].length; i++) {
				long val = dspRegisters[page][i].getValue();
				for (int j = 0; j < valSz; j++) {
					rwBuf[i * valSz + j] = (byte)(val & BYTE_MASK);
					val >>= 8;
				}
			}
			if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, dspRegisters[page][0].getAddress(), rwBuf, dspRegisters[page].length * valSz))
				return false;
			for(Register reg: dspRegisters[page])
				reg.commitChanges();
		} else if (dspType == DSP_STORM && page > 1 && !modifiedOnly) {
			writeHw(extDspAccessPage, dspRWStatusReg, 0x19, 0x19);
			int n = dspRegisters[page].length / 6;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < 6; j++) {
					int dspAddr =  dspRegisters[page][i*6+j].getAddress() - dspRegisters[page][0].getAddress();
					long val = dspRegisters[page][i*6+j].getValue();
					dspRegisters[page][i*6+j].commitChanges();
					
					dspAddr |= (page - 2) << 14;
					rwBuf[j*2] = (byte)dspAddr;
					rwBuf[j*2+1] = (byte)(dspAddr >> 8);
					for (int k = 0; k < 3; k++) {
						rwBuf[6*2+j*3+k] = (byte)(val & BYTE_MASK);
						val >>= 8;
					}
				}
				if (!hwAccess.write(devAddr, regAddrSz, regAddrMsbF, extDspAddrRegister, rwBuf, 6*2+6*3))
					return false;
				if (!waitOnBits(extDspAccessPage, extDspRWStatusReg, extDspRdStatusBit, false))
					return false;
			}
			writeHw(extDspAccessPage, extDspRWStatusReg, 0, 0x19);
			for (int i = n*6; i < dspRegisters[page].length; i++)
				if (!writeDspRegister(dspRegisters[page][i]))
					return false;
		} else
			for (Register reg : dspRegisters[page]) {
				if (!modifiedOnly || reg.isModified()) {
					if (!writeDspRegister(reg))
						return false;
				} else {
				}
			}
		return true;
	}
	
	/*** Settings interface ***/
	
	@Override
	public int getSettingsCount() {
		return hwAccess.getSettingsCount() + settingsCnt;
	}
	
	@Override
	public String getSettingName(int prpIdx) {
		if (prpIdx < hwAccess.getSettingsCount())
			return hwAccess.getSettingName(prpIdx);
		switch (prpIdx - hwAccess.getSettingsCount()) {
		case 0:
			return "Device address";
		case 1:
			return "Reg. address size";
		case 2:
			return "Reg. bit width";
		}
		return null;
	}

	@Override
	public int getSettingType(int prpIdx) {
		if (prpIdx < hwAccess.getSettingsCount())
			return hwAccess.getSettingType(prpIdx);
		return Settings.STRING_TYPE;
	}
	
	@Override
	public String[] getSettingEnum(int prpIdx) {
		if (prpIdx < hwAccess.getSettingsCount())
			return hwAccess.getSettingEnum(prpIdx);
		return null;
	}

	@Override
	public String getSettingValue(int prpIdx) {
		if (prpIdx < hwAccess.getSettingsCount())
			return hwAccess.getSettingValue(prpIdx);
		switch (prpIdx - hwAccess.getSettingsCount()) {
		case 0:
			return String.format("0x%02X", devAddr);
		case 1:
			return String.format("%d", regAddrSz);
		case 2:
			return String.format("%d", regValSz);
		}
		return null;
	}
	
	@Override
	public boolean setSettingValue(int prpIdx, String value) {
		if (prpIdx < hwAccess.getSettingsCount())
			return hwAccess.setSettingValue(prpIdx, value);
		int val = Integer.decode(value);
		switch (prpIdx - hwAccess.getSettingsCount()) {
		case 0:
			if ((val & ~BYTE_MASK) == 0) {
				newAddress = val;
				return true;
			}
			break;
		case 1:
			newRegAddrSz = val;
			return true;
		case 2:
			newRegValSz = val;
			return true;
		}
		return false;
	}

	@Override
	public void applySettings() {
		hwAccess.applySettings();
		if (newAddress != devAddr) {
			clear();
			devAddr = newAddress;
		}
		if (settingsCnt > 1) {
			if (regAddrSz != newRegAddrSz || regValSz != newRegValSz) {
				regAddrSz = newRegAddrSz;
				regValSz = newRegValSz;
				reinit();
			}
		}
	}
	
	@Override
	public void resetSettings() {
		hwAccess.resetSettings();
		newAddress = devAddr;
		newRegAddrSz = regAddrSz;
		newRegValSz = regValSz;
	}
}
