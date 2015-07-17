package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class IdInDTO implements Serializable {

	private static final long serialVersionUID = -4975881129579854288L;
	
	private int id;
	
	public IdInDTO() {
		
	}

	public IdInDTO(Integer id) {
		super();
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
