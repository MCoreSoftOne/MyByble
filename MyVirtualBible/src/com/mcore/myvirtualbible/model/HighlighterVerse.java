package com.mcore.myvirtualbible.model;

public class HighlighterVerse {

	private Highlighter config;

	private String verseMark;

	public HighlighterVerse() {
	}

	public HighlighterVerse(Highlighter config, String verseMark) {
		super();
		this.config = config;
		this.verseMark = verseMark;
	}

	public Highlighter getConfig() {
		return config;
	}

	public void setConfig(Highlighter config) {
		this.config = config;
	}

	public String getVerseMark() {
		return verseMark;
	}

	public void setVerseMark(String verseMark) {
		this.verseMark = verseMark;
	}

	@Override
	public String toString() {
		return "HighlighterVerse [config=" + config + ", verseMark="
				+ verseMark + "]";
	}

}
