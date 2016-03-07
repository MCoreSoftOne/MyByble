package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class ResultInfoDTO implements Serializable {

	private static final long serialVersionUID = -2413826178739697099L;

	protected int resultID;
	
	protected String resultDetails;
	
	public ResultInfoDTO() {
		
	}

	public ResultInfoDTO(int resultID) {
		super();
		this.resultID = resultID;
	}

	public ResultInfoDTO(int resultID, String resultDetails) {
		super();
		this.resultID = resultID;
		this.resultDetails = resultDetails;
	}

	public int getResultID() {
		return resultID;
	}

	public void setResultID(int resultID) {
		this.resultID = resultID;
	}

	public String getResultDetails() {
		return resultDetails;
	}

	public void setResultDetails(String resultDetails) {
		this.resultDetails = resultDetails;
	}

}
