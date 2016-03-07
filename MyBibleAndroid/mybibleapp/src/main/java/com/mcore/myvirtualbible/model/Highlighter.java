package com.mcore.myvirtualbible.model;

import java.io.Serializable;

public class Highlighter implements Serializable {

	private static final long serialVersionUID = 3623068301670298240L;

	private int id;

	private String name;

	private int color;

	private String data;

	private String note;

	public Highlighter() {
		
	}

	public Highlighter(int id, String name, int color, String data,
			String note) {
		super();
		this.id = id;
		this.name = name;
		this.color = color;
		this.data = data;
		this.note = note;
	}

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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public String getHighlightClassName() {
		return "markbbl" + id;
	}

	@Override
	public String toString() {
		return "HighlighterConfig [id=" + id + ", name=" + name + ", color="
				+ color + ", data=" + data + ", note=" + note + "]";
	}

}
