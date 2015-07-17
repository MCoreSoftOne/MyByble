package com.mcore.mybible.services.statistics.model;

import java.util.Iterator;
import java.util.List;

public class StatisticsGroupData {
	
	private int users;
	
	private int downloads;

	private String fileName;

	private List<StatisticsItemData> data;

	public StatisticsGroupData() {
	}

	public StatisticsGroupData(String fileName, List<StatisticsItemData> data) {
		super();
		this.fileName = fileName;
		this.data = data;
	}
	
	public void sort() {
		if (data != null) {
			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				StatisticsItemData item = (StatisticsItemData) iterator.next();
				if (item != null) {
					item.sort();
				}
			}
		}
	}
	
	public void calculate() {
		users = 0;
		downloads = 0;
		if (data != null) {
			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				StatisticsItemData item = (StatisticsItemData) iterator.next();
				if (item != null) {
					item.calculate();
				}
				users += item.getUsers();
				downloads += item.getDownloads();
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<StatisticsItemData> getData() {
		return data;
	}

	public void setData(List<StatisticsItemData> data) {
		this.data = data;
		calculate();
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
}
