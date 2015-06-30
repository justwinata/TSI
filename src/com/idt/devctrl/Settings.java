package com.idt.devctrl;

interface Settings {
	int STRING_TYPE = 1;
	int BOOL_TYPE = 2;
	int ENUM_TYPE = 3;
	
	int getSettingsCount();
	int getSettingType(int prpIdx);
	String getSettingName(int prpIdx);
	String getSettingValue(int prpIdx);
	boolean setSettingValue(int prpIdx, String value);
	String[] getSettingEnum(int prpIdx);
	void applySettings();
	void resetSettings();
}
