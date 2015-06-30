package com.idt.devctrl;

import javax.swing.event.ChangeEvent;

public class Register implements Comparable<Register> {
	static final int NOT_INITIALIZED = 0;
	static final int LOADED = 1;
	static final int MODIFIED = 2;
	static final String DEC_VALUE = "\\d+";
	static final String HEX_VALUE = "\\p{XDigit}+";
	static final String BIT_VALUE = "[01]";
	static final String PAGE_TAG = "p";
	static final String REG_ADDR_TAG = "r&";
	static final String REG_NUM_TAG = "r#";
	static final String REG_NUM_ALT_TAG = "r";
	static final String REG_VAL_TAG = "v";
	static final String REG_NAME_TAG = "n";
	
	private final int page;
	private final int address;
	private final int addressSize;
	private final int valBitDepth;
	private final long valueMask;
	private final long signMask;
	private final String addrHexFormat;
	private final String valHexFormat;
	private final String addrDecStr;
	private final String addrHexStr;
	private final String pageStr;
	private final ChangeEvent changeEvent;
	
	private long value;
	private String valHexStr;
	private String valDecStr;
	private long oldValue;
	private String oldValHexStr;
	private int state;
	private String name;
	private String[] bitNames;
	private String description;
	private StateController changeCtrl;
	
	public static boolean isValidDecStr(String val) {
		return val.matches(DEC_VALUE);
	}
	
	public static boolean isValidHexStr(String val) {
		return val.matches(HEX_VALUE);
	}
	
	public static boolean isValidBitValue(String val) {
		return val.matches(BIT_VALUE);
	}
	
	public Register(StateController changeCtrl, int page, int address, int addressSize, int valBitDepth) {
		this.changeCtrl = changeCtrl;
		this.page = page;
		this.address = address;
		this.addressSize = addressSize;
		this.valBitDepth = valBitDepth;
		addrHexFormat = "%0"+(addressSize+3)/4+"X";
		valHexFormat = "%0"+(valBitDepth+3)/4+"x";
		addrDecStr = String.valueOf(address);
		addrHexStr = String.format(addrHexFormat, address);
		pageStr = String.valueOf(page);

		state = NOT_INITIALIZED;
		int mask = 0;
		for (int i=0; i<valBitDepth; i++)
			mask = (mask << 1) | 1;
		valueMask = mask;
		
		signMask = 1 << (valBitDepth - 1);
		
		bitNames = new String[valBitDepth];
		for (int i = 0; i < bitNames.length; i++) {
			bitNames[ i ] = "";
		}
		valHexStr = oldValHexStr = "";
		valDecStr = "";
		changeEvent = new ChangeEvent(this);
	}
	
	public Register(int page, int address, int addressSize, int valBitDepth) {
		this.page = page;
		this.address = address;
		this.addressSize = addressSize;
		this.valBitDepth = valBitDepth;
		addrHexFormat = "%0"+(addressSize+3)/4+"X";
		valHexFormat = "%0"+(valBitDepth+3)/4+"x";
		addrDecStr = String.valueOf(address);
		addrHexStr = String.format(addrHexFormat, address);
		pageStr = String.valueOf(page);

		state = NOT_INITIALIZED;
		int mask = 0;
		for (int i=0; i<valBitDepth; i++)
			mask = (mask << 1) | 1;
		valueMask = mask;
		
		signMask = 1 << (valBitDepth - 1);
		
		bitNames = new String[valBitDepth];
		for (int i = 0; i < bitNames.length; i++) {
			bitNames[ i ] = "";
		}
		valHexStr = oldValHexStr = "";
		valDecStr = "";
		changeEvent = new ChangeEvent(this);
	}

	public void setChangeController(StateController changeCtrl) {
		this.changeCtrl = changeCtrl;
	}
	
	public String getAddressFormat() {
		return addrHexFormat;
	}
	
	public String getValueFormat() {
		return valHexFormat;
	}
	
	public int getPage() {
		return page;
	}
	
	public String getPageStr() {
		return pageStr;
	}
	
	public int getAddress() {
		return address;
	}
	
	public String getAddressStr() {
		return addrDecStr;
	}
	
	public String getAddressHexStr() {
		return addrHexStr;
	}
	
	public int getBitDepth() {
		return valBitDepth;
	}
	
	public int getByteSize() {
		return (valBitDepth + 7) / 8;
	}
	
	public long getValueMask() {
		return valueMask;
	}
	
	public String getName() {
		return name != null ? name : "";
	}
	
	public String getDescription() {
		return description != null ? description : "";
	}
	
	public boolean isValidValue(String val) {
		return isValidHexStr(val) && val.length() <= 2;
	}
	
	public boolean isValidValue(long val) {
		return (val & ~valueMask) == 0;
	}
	
	public long getValue() {
		return value;
	}
	
	public String getValueDecStr() {
		if (valDecStr == null) {
			valDecStr = String.format("%d", (value & signMask) == 0 ? value : value | ~valueMask);
		}
		return valDecStr;
	}
	
	public String getValueHexStr() {
		return valHexStr;
	}
	
	public String getOldValueHexStr() {
		return oldValHexStr;
	}
	
	public void clear() {
		state = NOT_INITIALIZED;
		value = oldValue = 0;
		valHexStr = oldValHexStr = "";
	}
	
	public void setValue(long newValue) {
		if (!isValidValue(newValue))
			throw new IllegalArgumentException("Invalid value " + newValue);
		String sv = String.format(valHexFormat, newValue);
		if (state == NOT_INITIALIZED || oldValHexStr.isEmpty()) {
			value = oldValue = newValue;
			valHexStr = oldValHexStr = sv;
		} else if (state == MODIFIED) {
			value = newValue;
			valHexStr = sv;
		} else {
			oldValue = value;
			value = newValue;
			oldValHexStr = valHexStr;
			valHexStr = sv;
		}
		valDecStr = null;
		state = LOADED;
	}
	
	public void modifyValue(String newValue, int radix) {
		if (newValue.isEmpty()) {
			if (state == MODIFIED) {
				value = oldValue;
				valHexStr = oldValHexStr;
				state = valHexStr.isEmpty()? NOT_INITIALIZED : LOADED;
				fireChangeEvent();
			}
		} else {
			long v = Long.parseLong(newValue, radix);
			if (isValidValue(v)) {
				if (v != value || state == NOT_INITIALIZED) {
					if (state == LOADED) {
						oldValue = value;
						oldValHexStr = valHexStr;
					}
					value = v;
					if (isMalformed(newValue) || radix != 16)
						valHexStr = String.format(valHexFormat, v);
					else
						valHexStr = newValue;
					state = MODIFIED;
					fireChangeEvent();
				}
			}
		}
		valDecStr = null;
	}
	
	private boolean isMalformed(String valueHexStr) {
		return valueHexStr.length() != (valBitDepth+3)/4;
	}

	public void modifyBit(int bit, String val) {
		if (bit >= valBitDepth)
			return;
			//throw new IllegalArgumentException("Invalid bit " + bit + ":" + val);
		if (val.isEmpty()) {
			if (state == MODIFIED) {
				long m = 1 << bit;
				value = (value & ~m) | (oldValue & m);
				if (value == oldValue && oldValHexStr.isEmpty()) {
					state = NOT_INITIALIZED;
					valHexStr = oldValHexStr;
				} else {
					valHexStr = String.format(valHexFormat, value);
				}
				fireChangeEvent();
			}
		} else {
			if (val.length() != 1)
				throw new IllegalArgumentException("Invalid bit " + bit + ":" + val);
			long v = Integer.parseInt(val, 2) & 1;
			long m = 1 << bit;
			v = v << bit;
			v = (value & ~m) | v;
			if (v != value || state == NOT_INITIALIZED) {
				String newValue = String.format(valHexFormat, v);
				if (state == LOADED) {
					oldValue = value;
					oldValHexStr = valHexStr;
				}
				value = v;
				valHexStr = newValue;
				state = MODIFIED;
				fireChangeEvent();
			}
		}
		valDecStr = null;
	}
	
	public void commitChanges() {
		oldValue = value;
		oldValHexStr = valHexStr;
	}
	
	public String getBit(int bit) {
		if (state == NOT_INITIALIZED)
			return "";
		if (bit < valBitDepth) {
			if (((value >> bit) & 1) == 0)
				return "0";
			else
				return "1";
		}
		return "";
		//throw new IllegalArgumentException("Invalid bit " + bit);
	}
	
	public String getOldBit(int bit) {
		if (state == NOT_INITIALIZED || oldValHexStr.isEmpty())
			return "";
		if (bit < valBitDepth) {
			if (((oldValue >> bit) & 1) == 0)
				return "0";
			else
				return "1";
		}
		return "";
		//throw new IllegalArgumentException("Invalid bit " + bit);
	}
	
	public boolean isInitialized() {
		return state != NOT_INITIALIZED;
	}
	
	public boolean isModified() {
		return state == MODIFIED && (oldValue != value || oldValHexStr.isEmpty());
	}
	
	public boolean isModified(int bit) {
		long mask = 1 << bit;
		return state == MODIFIED && ((oldValue & mask) != (value & mask) || oldValHexStr.isEmpty());
	}
	
	public boolean isChanged() {
		return state == LOADED && oldValue != value;
	}
	
	public boolean isChanged(int bit) {
		long mask = 1 << bit;
		return state == LOADED && (oldValue & mask) != (value & mask);
	}
	
	public void fireChangeEvent() {
		if (changeCtrl != null)
			changeCtrl.fireChangeEvent(changeEvent);
	}
	
	public static String noTagsStringTitle() {
		return "#reg&:value[:page #name]";
	}

	public String toNoTagsString() {
		return String.format("%X:%x:%d # %s", address, value, page, name != null ? name : "");
	}
	
	public String toString() {
		return String.format("%s:%X %s:%x %s:%d %s:%s", REG_ADDR_TAG, address, REG_VAL_TAG, value, PAGE_TAG, page, REG_NAME_TAG, name != null ? name : "N/A");
	}
	
	@Override
	public int compareTo(Register devreg) {
		return address - devreg.address;
	}
	
	public boolean equals(Register devreg) {
		return devreg.getAddress() == address && devreg.getPage() == page;
	}
	
	public Object clone() {
		Register newReg = new Register(page, address, addressSize, valBitDepth);
		newReg.value = value;
		newReg.valHexStr = valHexStr;
		newReg.oldValue = value;
		newReg.oldValHexStr = newReg.valHexStr;
		newReg.state = state;
		newReg.name = name;
		newReg.description = description;
		newReg.bitNames = bitNames;
		return newReg;
	}

}
