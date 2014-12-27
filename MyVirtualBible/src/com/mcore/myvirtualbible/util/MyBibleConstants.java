package com.mcore.myvirtualbible.util;

public class MyBibleConstants {
	
	public static final boolean MYBIBLE_DEVELOPER_MODE_FULL_DOWNLOAD = false;
	
	public static final String EXTERNAL_DB_DIR = "/mybible/";
	
	public static final String PREF_LAST_BIBLE_POSITION = "lastPosition";
	public static final String PREF_LAST_SCROLL_BIBLE_POSITION = "lastScrollPosition";
	public static final String PREF_BIBLE_TEXT_SIZE = "textSize";
	public static final String PREF_BIBLE_TEXT_COLOR = "textColor";
	public static final String PREF_BIBLE_BACKGROUND_COLOR = "backgroundColor";
	public static final String PREF_SELECTED_BIBLE_ID = "selectedVersionId";
	public static final String PREF_BIBLE_SERVER_ALIAS = "serverAlias";
	public static final String PREF_BIBLE_DOWNLOAD_SELECTED_LANG = "downloadSelectedLanguage";
	public static final String PREF_BIBLE_USE_EXTERNAL_STORAGE = "useExternalStorage";
	
	public static final String CATEGORY_DOWNLOAD = "MYBIBLE.DOWNLOADS";
	public static final String CATEGORY_PREFERENCES = "MYBIBLE.PREFERENCES";
	public static final String CATEGORY_USES = "MYBIBLE.USES";
	public static final String ACTION_DOWNLOAD = "MYBIBLE.DOWNLOAD";
	public static final String ACTION_FONT_SIZE = "MYBIBLE.PREF.FONT_SIZE";
	public static final String ACTION_COLOR = "MYBIBLE.PREF.COLOR";
	public static final String ACTION_SDCARD = "MYBIBLE.PREF.SDCARD";
	public static final String ACTION_RESET = "MYBIBLE.PREF.RESET";
	public static final String ACTION_USE_TRANS = "MYBIBLE.USE.USE_TRANS";
	public static final String ACTION_DEL_TRANS = "MYBIBLE.USE.DEL_TRANS";
	public static final String ACTION_SEARCH = "MYBIBLE.USE.SEARCH";
	public static final String ACTION_GOTO_VERSE = "MYBIBLE.USE.GOTO_VERSE";
	public static final String ACTION_SET_VERSE_MARK = "MYBIBLE.USE.SET_VERSE_MARK";
	public static final String ACTION_UNSET_VERSE_MARK = "MYBIBLE.USE.UNSET_VERSE_MARK";
	public static final String DOWNLOAD_NAME = "NAME";
	public static final String DOWNLOAD_LANG = "LANG";
	
	public static final int MIGRATION_NO_ERROR = 0;
	public static final int MIGRATION_UNKNOWN_ERROR = 1;
	public static final int MIGRATION_SQL_ERROR = 100;
	public static final int MIGRATION_INSUFFICIENT_SPACE_ERROR = 200;
	

}
