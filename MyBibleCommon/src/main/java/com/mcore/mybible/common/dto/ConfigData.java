package com.mcore.mybible.common.dto;

public class ConfigData extends ResultInfoDTO {

	private static final long serialVersionUID = -7850545998059619802L;
	
	private ConfigItem[] items;

	public ConfigItem[] getItems() {
		return items;
	}

	public void setItems(ConfigItem[] items) {
		this.items = items;
	}

}
