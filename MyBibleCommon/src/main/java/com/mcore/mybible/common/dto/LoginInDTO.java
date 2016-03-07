package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class LoginInDTO implements Serializable {

	private static final long serialVersionUID = -7145942147404872075L;

	private String id;
	
	private String version;
	
	public LoginInDTO() {
		
	}

	public LoginInDTO(String id, String version) {
		super();
		this.id = id;
		this.version = version;
	}



	public String getId() {
		return id;
	}

	public void setId(String email) {
		this.id = email;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String password) {
		this.version = password;
	}

	@Override
	public String toString() {
		return "LoginInfoDTO [id=" + id + ", version=" + version + "]";
	}
	
	
	
}
