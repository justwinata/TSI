package com.idt.devctrl;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Vitaliy Kulikov
 *
 */
class FTCI2CDeviceAccess implements I2CDeviceAccess {
	static long timestamp;
	static final String STANDARD_MODE = "STANDARD_MODE";
	static final String FAST_MODE = "FAST_MODE";
	static final String STRETCH_DATA_MODE = "STRETCH_DATA_MODE";
	static final String INVALID_MODE = "INVALID_MODE";
	static final int MODE_PROPERTY = 0;
	static final int FREQUENCY_PROPERTY = 1;
	static final int LATENCY_PROPERTY = 2;
	static final int DEVICE_NUM_PROPERTY = 3;
	static final String[] MODES = { STANDARD_MODE, FAST_MODE, STRETCH_DATA_MODE };
	static final int DEFAULT_FREQUENCY = 400000;
	static final int DEFAULT_LATENCY = 16;
	static final int DEFAULT_MODE = FTCI2C.FAST_MODE;
	
	private int handle;
	private int mode;
	private int frequency;
	private int latency;
	private int devNum;
	private int devCount;
	private String ftdiName;
	private String channel;
	private int location;
	private boolean hiSpeed;
	private String dllVersion;
	private boolean connected;
	private String infoStr;
	private String statusStr;
	private int status;
	private byte[] wCtrlBuf;
	private byte[] errMsgBuf;
	FTCI2C.SPageWriteData pageData;
	private int newMode;
	private int newFreq;
	private int newLat;
	private int newDevNum;


	static public boolean isValidMode(int mode) {
		switch (mode) {
		case FTCI2C.STANDARD_MODE:
		case FTCI2C.FAST_MODE:
		case FTCI2C.STRETCH_DATA_MODE:
			return true;
		}
		return false;
	}
	
	static public boolean isValidFrequency(int frequency) {
		return frequency >= 10000 && frequency <= 2000000;
	}
	
	static public boolean isValidLatency(int latency) {
		return latency >= 2 && latency <= 255;
	}
	
	public boolean isValidDevNum(int devNum) {
		return devNum < devCount;
	}
	
	private static int getClockDivisor(int freq) {
		return (6000000 / freq) - 1;
	}
	
	public FTCI2CDeviceAccess() {
		status = FTCI2C.FTC_SUCCESS;
		mode = newMode = DEFAULT_MODE;
		frequency = newFreq = DEFAULT_FREQUENCY;
		latency = newLat = DEFAULT_LATENCY;
		wCtrlBuf = new byte[4];
		errMsgBuf = new byte[100];
		pageData = new FTCI2C.SPageWriteData();

		byte[] bbuf = new byte[100];
		FTCI2C.DLL.I2C_GetDllVersion(bbuf, 10);
		dllVersion = Native.toString(bbuf);
	}
	
	@Override
	public String getStatus() {
		if (statusStr == null)
			updateStatus();
		return statusStr != null ? statusStr : " ";
	}

	private void updateStatus() {
		StringBuilder strBldr = new StringBuilder(100);
		strBldr.append(" ACCESS:  ");
		strBldr.append(getMethod());
		strBldr.append(" ");
		if (connected) {
			if (ftdiName != null) {
				strBldr.append(ftdiName);
				strBldr.append(" @ ");
				strBldr.append(location);
				strBldr.append("  ");
			}
			strBldr.append(getModeString());
			strBldr.append(" @ ");
			strBldr.append(frequency);
			strBldr.append("Hz ");
			if (status != FTCI2C.FTC_SUCCESS) {
				strBldr.append(" Access failed!");
			}
		} else {
			strBldr.append(" NOT CONNECTED");
		}
		statusStr = strBldr.toString();
	}
	
	@Override
	public String getInfo() {
		if (infoStr == null)
			updateInfo();
		return infoStr != null ? infoStr : " ";
	}

	private void updateInfo() {
		StringBuilder strBldr = new StringBuilder(100);
		strBldr.append("FTCI2C.DLL v.");
		strBldr.append(dllVersion);
		infoStr = strBldr.toString();
	}
	
	private String getModeString() {
		switch (mode) {
		case FTCI2C.STANDARD_MODE:
			return STANDARD_MODE;
		case FTCI2C.FAST_MODE:
			return FAST_MODE;
		case FTCI2C.STRETCH_DATA_MODE:
			return STRETCH_DATA_MODE;
		}
		return INVALID_MODE;
	}
	
	@Override
	public String getMethod() {
		return FTDICHIP;
	}

	@Override
	public String getErrorMessage() {
		FTCI2C.DLL.I2C_GetErrorCodeString("EN", status, errMsgBuf, errMsgBuf.length);
		return Native.toString(errMsgBuf);
	}

	@Override
	public boolean open() {
		if (!connected) {
			if (I2CDevice.DEBUG) {
				System.out.print(String.format("OPEN  FTDI#%d", devNum+1));
				timestamp = System.nanoTime();
			}
			
			hiSpeed = false;
			byte[] bbuf = new byte[100];
			byte[] bbuf2 = new byte[6];
			IntByReference intByRef = new IntByReference();
			IntByReference intByRef2 = new IntByReference();
			status = FTCI2C.DLL.I2C_GetNumDevices(intByRef);
			if (status != FTCI2C.FTC_SUCCESS || intByRef.getValue() == 0) {
				status = FTCI2C.DLL.I2C_GetNumHiSpeedDevices(intByRef);
				hiSpeed = true;
			}
			if (status == FTCI2C.FTC_SUCCESS) {
				devCount = intByRef.getValue();
				if (devNum < devCount) {
					if (hiSpeed)
						status = FTCI2C.DLL.I2C_GetHiSpeedDeviceNameLocIDChannel(devNum, bbuf, bbuf.length, intByRef, bbuf2, bbuf2.length, intByRef2);
					else
						status = FTCI2C.DLL.I2C_GetDeviceNameLocID(devNum, bbuf, bbuf.length, intByRef);
					if (status == FTCI2C.FTC_SUCCESS) {
						ftdiName = Native.toString(bbuf);
						location = intByRef.getValue();
						if (hiSpeed) {
							channel = Native.toString(bbuf2);
							status = FTCI2C.DLL.I2C_OpenHiSpeedDevice(ftdiName, location, channel, intByRef);
						} else
							status = FTCI2C.DLL.I2C_OpenEx(ftdiName, location, intByRef);
						if (status == FTCI2C.FTC_SUCCESS) {
							handle = intByRef.getValue();
							status = FTCI2C.DLL.I2C_InitDevice(handle, getClockDivisor(frequency));
							if (status == FTCI2C.FTC_SUCCESS && hiSpeed)
								status = FTCI2C.DLL.I2C_TurnOnDivideByFiveClockingHiSpeedDevice(handle);
							if (status == FTCI2C.FTC_SUCCESS) {
								if (status == FTCI2C.DLL.I2C_SetMode(handle, mode)) {
									FTCI2C.DLL.I2C_SetDeviceLatencyTimer(handle, (byte)latency);
									FTCI2C.DLL.I2C_GetClock(getClockDivisor(frequency), intByRef);
									frequency = intByRef.getValue();
									connected = true;
								} else {
									FTCI2C.DLL.I2C_Close(handle);
									handle = 0;
								}
							}
						}
					}
				} else {
					status = FTCI2C.FTC_DEVICE_NOT_FOUND;
				}
			}

			if (I2CDevice.DEBUG) {
				long currTime = System.nanoTime(); 
				System.out.println(String.format(" at slot %d with status %d %dms", location, status, (currTime - timestamp + 500000) / 1000000));
			}
			
			updateStatus();
		}
		return connected;
	}
	
	@Override
	public boolean read(int address, int addrSz, boolean msbf, int register, byte[] values, int valuesCount) {
		if (open()) {
			if (I2CDevice.DEBUG) {
				System.out.print(String.format("READ  FTDI#%d d:%02X r:%02X/%d c:%d", devNum+1, address, register, register, valuesCount));
				timestamp = System.nanoTime();
			}
			wCtrlBuf[ 0 ] = (byte)((address << 1) | 1);
			wCtrlBuf[ 1 ] = (byte)register;
			int ctrlCnt = 2;
			if (addrSz > 8) {
				if (msbf) {
					wCtrlBuf[ 1 ] = (byte)(register >> 8);
					wCtrlBuf[ 2 ] = (byte)register;
				} else {
					wCtrlBuf[ 2 ] = (byte)(register >> 8);
				}
				ctrlCnt++;
			}
			int readType = valuesCount > 1 ? FTCI2C.BLOCK_READ_TYPE : FTCI2C.BYTE_READ_TYPE;
			status = FTCI2C.DLL.I2C_Read(handle, wCtrlBuf, ctrlCnt, 1, 30, readType, values, valuesCount);

			if (I2CDevice.DEBUG) {
				long currTime = System.nanoTime();
				if (valuesCount > 1)
					System.out.println(String.format(" %dms v:%02x:%02x%s", (currTime - timestamp + 500000) / 1000000, values[0], values[1], valuesCount > 2 ? "..":""));
				else
					System.out.println(String.format(" %dms v:%02x", (currTime - timestamp + 500000) / 1000000, values[0]));
			}
		}
		return status == FTCI2C.FTC_SUCCESS;
	}

	@Override
	public boolean write(int address, int addrSz, boolean msbf, int register, byte[] values, int valuesCount) {
		if (open()) {
			if (I2CDevice.DEBUG) {
				if (valuesCount > 1)
					System.out.print(String.format("WRITE FTDI#%d d:%02X r:%02X/%d c:%d v:%02x:%02x%s", devNum+1, address, register, register, valuesCount, values[0], values[1], valuesCount > 2 ? "..":""));
				else
					System.out.print(String.format("WRITE FTDI#%d d:%02X r:%02X/%d c:%d v:%02x", devNum+1, address, register, register, valuesCount, values[0]));
				timestamp = System.nanoTime();
			}
			wCtrlBuf[ 0 ] = (byte)(address << 1);
			wCtrlBuf[ 1 ] = (byte)register;
			int ctrlCnt = 2;
			if (addrSz > 8) {
				if (msbf) {
					wCtrlBuf[ 1 ] = (byte)(register >> 8);
					wCtrlBuf[ 2 ] = (byte)register;
				} else {
					wCtrlBuf[ 2 ] = (byte)(register >> 8);
				}
				ctrlCnt++;
			}
			int writeType;
			if (valuesCount > 1) {
				writeType = FTCI2C.PAGE_WRITE_TYPE;
				pageData.numPages = 1;
				pageData.numBytesPerPage = valuesCount;
			} else {
				writeType = FTCI2C.BYTE_WRITE_TYPE;
				pageData.numPages = 0;
				pageData.numBytesPerPage = 0;
			}
			status = FTCI2C.DLL.I2C_Write(handle, wCtrlBuf, ctrlCnt, 1, 30, 1, writeType, values, valuesCount, 1, 30, pageData);

			if (I2CDevice.DEBUG) {
				long currTime = System.nanoTime(); 
				System.out.println(String.format(" %dms", (currTime - timestamp + 500000) / 1000000));
			}
		}
		return status == FTCI2C.FTC_SUCCESS;
	}
	
	public boolean close() {
		if (connected && handle != 0) {
			if (I2CDevice.DEBUG) {
				System.out.println(String.format("CLOSE FTDI#%d close", devNum+1));
			}
			
			FTCI2C.DLL.I2C_Close(handle);
			handle = 0;
			connected = false;
		}
		return true;
	}
	
	protected void finalize() {
		close();
	}

	/*
	 * Settings interface (non-Javadoc)
	 */
	
	@Override
	public int getSettingsCount() {
		return DEVICE_NUM_PROPERTY + 1;
	}

	@Override
	public String[] getSettingEnum(int prpIdx) {
		if (MODE_PROPERTY == prpIdx) {
			return MODES;
		} else if (DEVICE_NUM_PROPERTY == prpIdx) {
			if (!connected) {
				IntByReference intByRef = new IntByReference();
				status = FTCI2C.DLL.I2C_GetNumDevices(intByRef);
				if (status == FTCI2C.FTC_SUCCESS) {
					devCount = intByRef.getValue();
				}
			}
			if (devCount > 0) {
				String[] enm = new String[devCount];
				for (int i=0; i<devCount; i++) {
					enm[i] = String.format("%d", i+1);
				}
				return enm;
			}
		}
		return null;
	}

	@Override
	public String getSettingName(int prpIdx) {
		switch (prpIdx) {
		case MODE_PROPERTY:
			return "Bus mode";
		case FREQUENCY_PROPERTY:
			return "Frequency (Hz)";
		case LATENCY_PROPERTY:
			return "Latency (ms)";
		case DEVICE_NUM_PROPERTY:
			return "Device#";
		}
		throw new IllegalArgumentException("Invalid property " + prpIdx);
	}

	@Override
	public int getSettingType(int prpIdx) {
		switch (prpIdx) {
		case MODE_PROPERTY:
			return Settings.ENUM_TYPE;
		case FREQUENCY_PROPERTY:
			return Settings.STRING_TYPE;
		case LATENCY_PROPERTY:
			return Settings.STRING_TYPE;
		case DEVICE_NUM_PROPERTY:
			return Settings.ENUM_TYPE;
		}
		throw new IllegalArgumentException("Invalid property " + prpIdx);
	}

	@Override
	public String getSettingValue(int prpIdx) {
		switch (prpIdx) {
		case MODE_PROPERTY:
			return getModeString();
		case FREQUENCY_PROPERTY:
			return String.valueOf(frequency);
		case LATENCY_PROPERTY:
			return String.valueOf(latency);
		case DEVICE_NUM_PROPERTY:
			return String.format("%d", devNum + 1);
		}
		throw new IllegalArgumentException("Invalid property " + prpIdx);
	}

	@Override
	public boolean setSettingValue(int prpIdx, String value) {
		switch (prpIdx) {
		case MODE_PROPERTY:
			if (value.equals(STANDARD_MODE)) {
				newMode = FTCI2C.STANDARD_MODE;
			} else if (value.equals(FAST_MODE)) {
				newMode = FTCI2C.FAST_MODE;
			} else if (value.equals(STRETCH_DATA_MODE)) {
				newMode = FTCI2C.STRETCH_DATA_MODE;
			} else {
				return false;
				//throw new IllegalArgumentException("Invalid mode value " + value);
			}
			break;
		case FREQUENCY_PROPERTY:
			int f = Integer.parseInt(value);
			if (isValidFrequency(f)) {
				newFreq = f;
			} else {
				return false;
				//throw new IllegalArgumentException("Invalid frequency value " + value);
			}
			break;
		case LATENCY_PROPERTY:
			int l = Integer.parseInt(value);
			if (isValidLatency(l)) {
				newLat = l;
			} else {
				return false;
				//throw new IllegalArgumentException("Invalid frequency value " + value);
			}
			break;
		case DEVICE_NUM_PROPERTY:
			newDevNum = Integer.parseInt(value) - 1;
			break;
		default:
			return false;
			//throw new IllegalArgumentException("Invalid property " + prpIdx);
		}
		return true;
	}

	@Override
	public void applySettings() {
		if (devNum != newDevNum) {
			if (connected) {
				close();
			}
			frequency = DEFAULT_FREQUENCY;
			latency = DEFAULT_LATENCY;
			mode = DEFAULT_MODE;
			devNum = newDevNum;
		}
		if (newFreq != frequency) {
			if (open()) {
				IntByReference intByRef = new IntByReference();
				FTCI2C.DLL.I2C_SetClock(handle, getClockDivisor(newFreq), intByRef);
				frequency = intByRef.getValue();
			}
		}
		if (newMode != mode) {
			if (open()) {
				mode = newMode;
				FTCI2C.DLL.I2C_SetMode(handle, mode);
			}
		}
		if (newLat != latency) {
			if (open()) {
				latency = newLat;
				FTCI2C.DLL.I2C_SetDeviceLatencyTimer(handle, (byte)latency);
			}
		}
		updateStatus();
	}

	@Override
	public void resetSettings() {
		newMode = mode;
		newFreq = frequency;
		newLat = latency;
		newDevNum = devNum;
	}

}
