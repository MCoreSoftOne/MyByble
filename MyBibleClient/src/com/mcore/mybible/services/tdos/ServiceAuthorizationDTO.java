package com.mcore.mybible.services.tdos;

import java.io.Serializable;

public class ServiceAuthorizationDTO implements Serializable {

	private static final long serialVersionUID = 3143958567123599946L;

	private String username;
	
	private String credentials;
	
	private int token;
	
	public ServiceAuthorizationDTO() {
		
	}
	
	public ServiceAuthorizationDTO(String username, String credentials) {
		super();
		this.username = username;
		this.credentials = credentials;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "ServiceAuthorization [username=" + username + ", credentials=******"
				+ ", token=" + token + "]";
	}

}
