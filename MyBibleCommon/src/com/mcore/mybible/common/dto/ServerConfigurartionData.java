package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class ServerConfigurartionData implements Serializable {

	private static final long serialVersionUID = -66804232602114875L;

	private String transationlistid;

	public String getTransationlistid() {
		return transationlistid;
	}

	public void setTransationlistid(String transationlistid) {
		this.transationlistid = transationlistid;
	}
}
