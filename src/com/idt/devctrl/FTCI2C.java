package com.idt.devctrl;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Vitaliy Kulikov
 *
 */
public interface FTCI2C extends Library {
	FTCI2C DLL = (FTCI2C) Native.loadLibrary(System.getProperty("os.arch").equals("amd64")?"ftci2c64":"ftci2c", FTCI2C.class);
	int NO_WRITE_TYPE = 0;
	int BYTE_WRITE_TYPE = 1;
	int PAGE_WRITE_TYPE = 2;
	
	int BYTE_READ_TYPE = 1;
	int BLOCK_READ_TYPE = 2;
	
	int STANDARD_MODE = 1;
	int FAST_MODE = 2;
	int STRETCH_DATA_MODE = 4;

	int FTC_SUCCESS = 0;
	int FTC_INVALID_HANDLE = 1;
	int FTC_DEVICE_NOT_FOUND = 2;
	int FTC_DEVICE_NOT_OPENED = 3;
	int FTC_IO_ERROR = 4;
	int FTC_INSUFFICIENT_RESOURCES = 5;
	
	public static class SPageWriteData extends Structure {
		  public int numPages;
		  public int numBytesPerPage;
	}
	
	int I2C_GetNumDevices(IntByReference pNumDevices);
	int I2C_GetNumHiSpeedDevices(IntByReference pNumDevices);
	int I2C_GetDeviceNameLocID(int deviceNameIndex, byte[] deviceNameBuffer, int bufferSize, IntByReference pLocationID);
	int I2C_GetHiSpeedDeviceNameLocIDChannel(int deviceNameIndex, byte[] deviceNameBuffer, int nameBufSize, IntByReference pLocationID, byte[] channelBuffer, int chanBufSize, IntByReference pDevType);
	int I2C_GetDllVersion(byte[] pDllVersioBuffer, int bufferSize);
	int I2C_GetErrorCodeString(String language, int statusCode, byte[] errorMessageBuffer, int bufferSize);
	
	int I2C_Open(IntByReference pftHandle);
	int I2C_OpenEx(String deviceName, int locationID, IntByReference pftHandle);
	int I2C_OpenHiSpeedDevice(String deviceName, int locationID, String channel, IntByReference pftHandle);
	int I2C_Close(int ftHandle);
	int I2C_InitDevice(int ftHandle, int clockDivisor);
	int I2C_TurnOnDivideByFiveClockingHiSpeedDevice(int ftHandle);
	int I2C_GetClock(int clockDivisor, IntByReference pClockFrequencyHz);
	int I2C_GetHiSpeedDeviceClock(int clockDivisor, IntByReference pClockFrequencyHz);
	int I2C_SetClock(int ftHandle, int clockDivisor, IntByReference pClockFrequencyHz);
	int I2C_SetDeviceLatencyTimer(int ftHandle, byte timerValue);
	int I2C_GetDeviceLatencyTimer(int ftHandle, ByteByReference pTimerValue);
	int I2C_SetMode(int ftHandle, int commsMode);
	int I2C_Write(int ftHandle, byte[] writeControlBuffer, int numControlBytesToWrite, int controlAcknowledge,
			int controlAckTimeoutmSecs, int stopCondition, int dataWriteTypes, byte[] writeDataBuffer,
			int numDataBytesToWrite, int dataAcknowledge, int dataAckTimeoutmSecs, SPageWriteData pageData);
	int I2C_Read(int ftHandle, byte[] writeControlBuffer, int numControlBytesToWrite, int controlAcknowledge,
			int controlAckTimeoutmSecs, int dataReadTypes, byte[] readDataBuffer, int numDataBytesToRead);
}
