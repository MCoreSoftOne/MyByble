package com.mcore.mybible.common.dto;

public class StatisticsInDTO {

	private String token;

	private int days;

	public StatisticsInDTO() {
	}

	public StatisticsInDTO(String token, int days) {
		super();
		this.token = token;
		this.days = days;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

}
