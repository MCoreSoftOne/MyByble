package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class Chapter implements Serializable {
	
	private static final long serialVersionUID = -6197334792282894148L;

	private int id;
	
	private int number;
	
	private String chapter;
	
	private Book book;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	
	
}
