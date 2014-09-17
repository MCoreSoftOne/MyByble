package com.mcore.mybible.common.dto;


public class LoginOutDTO extends ResultInfoDTO {

	private static final long serialVersionUID = 7073884403689422633L;

	private int id;
	
	public LoginOutDTO() {
	}
	
	public LoginOutDTO(int id) {
		super();
		this.id = id;
		
	}
	
	public LoginOutDTO(int id, int resultID) {
		super();
		this.id = id;
		this.resultID = resultID;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "LoginOutDTO [id=" + id + "]";
	}
	
	

}
