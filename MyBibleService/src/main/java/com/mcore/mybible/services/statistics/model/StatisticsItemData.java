package com.mcore.mybible.services.statistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class StatisticsItemData {

	private Date date;

	private int users;

	private int downloads;

	private int userfromotherdays;

	private List<UserData> data;

	public void sort() {
		if (data != null) {
			Collections.reverse(data);
		}
	}

	public void calculate() {
		users = 0;
		downloads = 0;
		if (data != null) {
			users = data.size();
			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				UserData usr = (UserData) iterator.next();
				if (usr != null) {
					downloads += usr.getTotalDownload();
				}
			}
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getUsers() {
		return users;
	}

	public void setUsers(int users) {
		this.users = users;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	public int getUserfromotherdays() {
		return userfromotherdays;
	}

	public void setUserfromotherdays(int userfromotherdays) {
		this.userfromotherdays = userfromotherdays;
	}

	public boolean addUser(UserData userData) {
		boolean result = false;
		if (userData != null) {
			if (data == null) {
				data = new ArrayList<UserData>();
			}
			if (!data.contains(userData)) {
				data.add(userData);
			}
			result = true;
		}
		return result;
	}

	public List<UserData> getData() {
		return data;
	}

	public void setData(List<UserData> data) {
		this.data = data;
	}

}
