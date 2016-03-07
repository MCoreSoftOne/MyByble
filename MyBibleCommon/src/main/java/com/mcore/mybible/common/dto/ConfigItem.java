package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class ConfigItem implements Serializable {

	public ConfigItem(String serveralias, String serveraddress, String order) {
		super();
		this.serveralias = serveralias;
		this.serveraddress = serveraddress;
		this.order = order;
	}

	private static final long serialVersionUID = 5692020940331716945L;

	private String serveralias;
	private String serveraddress;
	private String order;

	public ConfigItem() {
	}

	public String getServeralias() {
		return serveralias;
	}

	public void setServeralias(String serveralias) {
		this.serveralias = serveralias;
	}

	public String getServeraddress() {
		return serveraddress;
	}

	public void setServeraddress(String serveraddress) {
		this.serveraddress = serveraddress;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

}
