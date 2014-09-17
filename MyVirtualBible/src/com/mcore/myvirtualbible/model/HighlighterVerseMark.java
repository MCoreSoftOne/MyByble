package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class HighlighterVerseMark implements Serializable {

	private static final long serialVersionUID = 3811702186757845776L;

	private Highlighter config;
	private int book;
	private int chapter;
	private String verseMark;
	private int verseRangeLow;
	private int verseRangeHigh;
	private String extract;
	private String note;

	public HighlighterVerseMark() {

	}

	public Highlighter getConfig() {
		return config;
	}

	public void setConfig(Highlighter config) {
		this.config = config;
	}

	public int getBook() {
		return book;
	}

	public void setBook(int book) {
		this.book = book;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public String getVerseMark() {
		return verseMark;
	}

	public void setVerseMark(String verseMark) {
		this.verseMark = verseMark;
	}

	public String getExtract() {
		return extract;
	}

	public void setExtract(String extract) {
		this.extract = extract;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getVerseRangeLow() {
		return verseRangeLow;
	}

	public void setVerseRangeLow(int verseRangeLow) {
		this.verseRangeLow = verseRangeLow;
	}

	public int getVerseRangeHigh() {
		return verseRangeHigh;
	}

	public void setVerseRangeHigh(int verseRangeHigh) {
		this.verseRangeHigh = verseRangeHigh;
	}

	@Override
	public String toString() {
		return "HighlighterVerseMark [config=" + config + ", book=" + book
				+ ", chapter=" + chapter + ", verseMark=" + verseMark
				+ ", verseRangeLow=" + verseRangeLow + ", verseRangeHigh="
				+ verseRangeHigh + ", extract=" + extract + ", note=" + note
				+ "]";
	}

}
