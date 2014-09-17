package com.mcore.mybible.common.dto;

import java.io.Serializable;

import com.mcore.mybible.common.dto.ResultInfoDTO;

public class IdOutDTO extends ResultInfoDTO implements Serializable{

	private static final long serialVersionUID = -5785776336099662723L;
	
	private int id;
	
	public IdOutDTO() {
		
	}

	public IdOutDTO(Integer id) {
		super();
		this.id = id;
	}


	public IdOutDTO(int id, int resultID, String resultDetails) {
		super(resultID, resultDetails);
		this.id = id;
	}
	
	public IdOutDTO(int id, int resultID) {
		super(resultID);
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "id:"+id;
	}
	
}
