package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class BiblePosition implements Serializable {

	private static final long serialVersionUID = -6013263857948361969L;
	private Book book;
	private int chapter;
	private int verse;
	
	public BiblePosition() {

	}

	public BiblePosition(Book book, int chapter, int verse) {
		super();
		this.book = book;
		this.chapter = chapter;
		this.verse = verse;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public int getVerse() {
		return verse;
	}

	public void setVerse(int verse) {
		this.verse = verse;
	}
}
