package com.idt.devctrl;


/**
 * @author Vitaliy Kulikov
 *
 */
public class RegSequenceItem {
	private Register reg;
	private int page = 0;
	private String pageStr = "";
	private long mask = 0;
	private String maskStr;
	private int delay;
	private String delayStr;
	private String comment;
	
	public void RegSeqItem(Register register) {
		reg = (Register)register.clone();
		page = reg.getPage();
		mask = reg.getValueMask();
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		if (this.page != page) {
			this.page = page;
			reg = null;
			pageStr = null;
		}
	}
	
	public void clearPage() {
		if (reg == null) {
			page = 0;
			pageStr = "";
		}
	}
	
	public String getPageStr() {
		if (pageStr == null) {
			pageStr = String.valueOf(page);
		}
		return pageStr;
	}
	
	public Register getRegister() {
		return reg;
	}
	
	public void setRegister(Register register) {
		if (register != null && register.getPage() != page)
			throw new IllegalArgumentException("Invalid register " + register);
		reg = register;
		mask = reg.getValueMask();
		pageStr = null;
	}
	
	public long getMask() {
		return mask;
	}
	
	public void setMask(long mask) {
		if (this.mask != mask) {
			this.mask = mask;
			maskStr = null;
		}
	}
	
	public String getMaskStr() {
		if (maskStr == null && reg != null) {
			maskStr = String.format(reg.getValueFormat(), mask);
		}
		return maskStr;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int delay) {
		if (this.delay != delay) {
			this.delay = delay;
			delayStr = null;
		}
	}
	
	public String getDelayStr() {
		if (delayStr == null && reg != null) {
			delayStr = String.format("%d", delay);
		}
		return delayStr;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
	
}
