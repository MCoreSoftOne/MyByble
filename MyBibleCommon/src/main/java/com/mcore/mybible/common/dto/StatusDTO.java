package com.mcore.mybible.common.dto;

public class StatusDTO extends ResultInfoDTO {

	private static final long serialVersionUID = -5007596835357229700L;

	private int status;

	private String bibleServerVersion;

	private String appVersion;

	private String appName;

	private String statusDescription;

	private String securitySeed;

	private String fileOpts;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getBibleServerVersion() {
		return bibleServerVersion;
	}

	public void setBibleServerVersion(String bibleServerVersion) {
		this.bibleServerVersion = bibleServerVersion;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	public String getSecuritySeed() {
		return securitySeed;
	}

	public void setSecuritySeed(String securitySeed) {
		this.securitySeed = securitySeed;
	}

	public String getFileOpts() {
		return fileOpts;
	}

	public void setFileOpts(String fileOpts) {
		this.fileOpts = fileOpts;
	}

	@Override
	public String toString() {
		return "StatusDTO [status=" + status + ", bibleServerVersion="
				+ bibleServerVersion + ", appVersion=" + appVersion
				+ ", appName=" + appName + ", statusDescription="
				+ statusDescription + ", securitySeed=" + securitySeed
				+ ", fileOpts=" + fileOpts + "]";
	}

}
