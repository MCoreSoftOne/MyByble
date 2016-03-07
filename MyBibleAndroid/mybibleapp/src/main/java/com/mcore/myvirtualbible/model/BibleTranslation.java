package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class BibleTranslation implements Serializable {

	private static final long serialVersionUID = 9029194200039044570L;

	private int id;
	
	private String name;
	
	private String abrev;
	
	private String revision;
	
	private String language;
	
	private String copyright;
	
	private String encryptionMethod; 

	private boolean loaded;
	
	private String othersprops;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getAbrev() {
		return abrev;
	}

	public void setAbrev(String abrev) {
		this.abrev = abrev;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getEncryptionMethod() {
		return encryptionMethod;
	}

	public void setEncryptionMethod(String encryptionMethod) {
		this.encryptionMethod = encryptionMethod;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	public void addToOthersprops(String key, String value) {
		if (key != null && value != null && key.length() > 0 && value.length() > 0) {
			if (othersprops == null) {
				othersprops = "";
			}
			othersprops += key + "=" + value + ";";
		}
	}

	public String getOthersprops() {
		return othersprops;
	}

	public void setOthersprops(String othersprops) {
		this.othersprops = othersprops;
	}
	
}
