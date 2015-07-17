package com.mcore.mybible.common.dto;

import java.io.Serializable;

public class DayStatisticDTO implements Serializable {

	private static final long serialVersionUID = 624849572173869929L;

	private String day;

	private int usercount;

	private int newuserscount;

	private int downloads;

	public DayStatisticDTO() {
	}

	public DayStatisticDTO(String day, int usercount, int newuserscount,
			int downloads) {
		super();
		this.day = day;
		this.usercount = usercount;
		this.newuserscount = newuserscount;
		this.downloads = downloads;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getUsercount() {
		return usercount;
	}

	public void setUsercount(int usercount) {
		this.usercount = usercount;
	}

	public int getNewuserscount() {
		return newuserscount;
	}

	public void setNewuserscount(int newuserscount) {
		this.newuserscount = newuserscount;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	@Override
	public String toString() {
		return "DayStatisticDTO [day=" + day + ", usercount=" + usercount
				+ ", newuserscount=" + newuserscount + ", downloads="
				+ downloads + "]";
	}

}
