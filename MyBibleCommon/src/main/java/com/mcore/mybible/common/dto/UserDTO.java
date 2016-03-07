package com.mcore.mybible.common.dto;

import com.mcore.mybible.common.dto.ResultInfoDTO;

public class UserDTO extends ResultInfoDTO {

	private static final long serialVersionUID = 581014755657252750L;

	private int token;

	private String userId;

	private String version;

	public UserDTO() {

	}

	public UserDTO(int token, String userId, String version) {
		super();
		this.token = token;
		this.userId = userId;
		this.version = version;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int id) {
		this.token = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String user) {
		this.userId = user;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "UserDTO [token=" + token + ", userId=" + userId + ", version="
				+ version + "]";
	}

}
