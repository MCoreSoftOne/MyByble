package com.mcore.mybible.services.statistics.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class UserData {
	
	private String id;
	
	private int totalLogin;
	
	private int totalDownload;
	
	private String version;
	
	private List<String> downloadedVersions = new ArrayList<String>();
	
	private Date lastDownload;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTotalLogin() {
		return totalLogin;
	}

	public void setTotalLogin(int totalLogin) {
		this.totalLogin = totalLogin;
	}

	public int getTotalDownload() {
		return totalDownload;
	}

	public void setTotalDownload(int totalDownload) {
		this.totalDownload = totalDownload;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getLastDownload() {
		return lastDownload;
	}

	public void setLastDownload(Date lastDownload) {
		this.lastDownload = lastDownload;
	}
	
	public String getDownloadedVersionsStr() {
		String result = "";
		if (downloadedVersions != null) {
			int i = 0;
			for (Iterator iterator = downloadedVersions.iterator(); iterator.hasNext();) {
				String data = (String) iterator.next();
				result += (i==0?"":",") + data;
				i++;
			}
		}
		return result;
	}

	public String[] getDownloadedVersions() {
		return (String[]) downloadedVersions.toArray(new String[downloadedVersions.size()]);
	}

	public void addDownloadedVersions(String version) {
		if (version != null && downloadedVersions != null) {
			boolean enc = false;
			for (Iterator iterator = downloadedVersions.iterator(); iterator.hasNext();) {
				String data = (String) iterator.next();
				if (data.equals(version)) {
					enc = true;
				}
			}
			if (!enc) {
				downloadedVersions.add(version);
			}
		}
	}

	@Override
	public String toString() {
		return "UserData [id=" + id + ", totalLogin=" + totalLogin
				+ ", totalDownload=" + totalDownload + ", version=" + version
				+ ", downloadedVersions=" + downloadedVersions
				+ ", lastDownload=" + lastDownload + "]";
	}

}
