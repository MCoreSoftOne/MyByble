package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class Book implements Serializable {
	
	private static final long serialVersionUID = -9117264723690733837L;

	private int id;
	
	private int bookNumber;
	
	private String name;
	
	private int chaptersSize;
	
	private BibleTranslation bibleVersion;

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

	public int getChaptersSize() {
		return chaptersSize;
	}

	public void setChaptersSize(int chaptersSize) {
		this.chaptersSize = chaptersSize;
	}

	public BibleTranslation getBibleVersion() {
		return bibleVersion;
	}

	public void setBibleVersion(BibleTranslation bibleVersion) {
		this.bibleVersion = bibleVersion;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public int getBookNumber() {
		return bookNumber;
	}

	public void setBookNumber(int bookNumber) {
		this.bookNumber = bookNumber;
	}
	
}
