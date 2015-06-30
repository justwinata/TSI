package com.idt.devctrl;

/**
 * @author Vitaliy Kulikov
 *
 */
interface I2CDeviceAccess extends Settings {
	String FTDICHIP = "FTDI";
	String I2CBUS = "I2C";
	String DRIVER = "DRV";
	
	String getMethod();
	String getInfo();
	String getStatus();
	String getErrorMessage();
	boolean open();
	boolean read(int devAddress, int regAddrSz, boolean msbf, int register, byte[] values, int valuesCount);
	boolean write(int devAddress, int regAddrSz, boolean msbf, int register, byte[] values, int valuesCount);
	boolean close();
}
