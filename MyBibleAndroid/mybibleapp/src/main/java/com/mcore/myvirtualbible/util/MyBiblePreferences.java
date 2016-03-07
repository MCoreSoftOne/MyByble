package com.mcore.myvirtualbible.util;

import com.mcore.mybible.common.utilities.CommonConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class MyBiblePreferences {
	
	private static MyBiblePreferences instance;
	
	private Context ctx;
	
	private Integer lastPosition;
	
	private Integer currentTranslation;
	
	private Integer textColor;
	
	private Integer backgroundColor;
	
	private String textSize;
	
	private String serverAlias;
	
	private String downloadSelectedLanguage;
	
	private Boolean useExternalStorage;
	
	public static MyBiblePreferences getInstance(Context ctx) {
		if (instance == null) {
			instance = new MyBiblePreferences();
		}
		instance.ctx = ctx;
		return instance;
	}
	
	protected MyBiblePreferences() {
		
	}

	public int getLastPosition() {
		if (lastPosition != null) {
			return lastPosition;
		}
		int result = 0;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getInt(MyBibleConstants.PREF_LAST_BIBLE_POSITION, result);
		}
		lastPosition = result;
		return result;
	}
	
	public void setLastPosition(int position) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putInt(MyBibleConstants.PREF_LAST_BIBLE_POSITION, position);
			editor.commit();
			lastPosition = position;
		}
	}
	
	public int getLastScrollPosition(int position) {
		int result = 0;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			int tPos = preferences.getInt(MyBibleConstants.PREF_LAST_SCROLL_BIBLE_POSITION+"_POS", result);
			if (tPos == position) {
				result = preferences.getInt(MyBibleConstants.PREF_LAST_SCROLL_BIBLE_POSITION, result);
			}
		}
		return result;
	}
	
	public void setLastScrollPosition(int scrollposition, int bibleposition) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putInt(MyBibleConstants.PREF_LAST_SCROLL_BIBLE_POSITION+"_POS", bibleposition);
			editor.putInt(MyBibleConstants.PREF_LAST_SCROLL_BIBLE_POSITION, scrollposition);
			editor.commit();
		}
	}
	
	public int getCurrentTranslation() {
		if (currentTranslation != null) {
			return currentTranslation;
		}
		int result = 0;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getInt(MyBibleConstants.PREF_SELECTED_BIBLE_ID, result);
		}
		currentTranslation = result;
		return result;
	}

	public void setCurrentTranslation(int id) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putInt(MyBibleConstants.PREF_SELECTED_BIBLE_ID, id);
			editor.commit();
			currentTranslation = id;
		}
	}

	public String getTextSize() {
		if (textSize != null) {
			return textSize;
		}
		String result = "medium";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getString(MyBibleConstants.PREF_BIBLE_TEXT_SIZE, result);
		}
		textSize = result;
		return result;
	}

	public void setTextSize(String textSize) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putString(MyBibleConstants.PREF_BIBLE_TEXT_SIZE, textSize);
			editor.commit();
			this.textSize = textSize;
		}
	}

	public Integer getTextColor() {
		if (textColor != null) {
			return textColor;
		}
		int result = Color.BLACK;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getInt(MyBibleConstants.PREF_BIBLE_TEXT_COLOR, result);
		}
		textColor = result;
		return result;
	}

	public void setTextColor(Integer textColor) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putInt(MyBibleConstants.PREF_BIBLE_TEXT_COLOR, textColor);
			editor.commit();
			this.textColor = textColor;
		}
	}

	public Integer getBackgroundColor() {
		if (backgroundColor != null) {
			return backgroundColor;
		}
		int result = Color.WHITE;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getInt(MyBibleConstants.PREF_BIBLE_BACKGROUND_COLOR, result);
		}
		backgroundColor = result;
		return result;
	}

	public void setBackgroundColor(Integer backgroundColor) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putInt(MyBibleConstants.PREF_BIBLE_BACKGROUND_COLOR, backgroundColor);
			editor.commit();
			this.backgroundColor = backgroundColor;
		}
	}

	public String getServerAlias() {
		if (serverAlias != null) {
			return serverAlias;
		}
		String result = CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getString(MyBibleConstants.PREF_BIBLE_SERVER_ALIAS, result);
		}
		serverAlias = result;
		return result;
	}

	public void setServerAlias(String serverAlias) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putString(MyBibleConstants.PREF_BIBLE_SERVER_ALIAS, serverAlias);
			editor.commit();
			this.serverAlias = serverAlias;
		}
	}

	public String getDownloadSelectedLanguage() {
		if (downloadSelectedLanguage != null) {
			return downloadSelectedLanguage;
		}
		String result = CommonConstants.LANGUAGE_CODE_ALL;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getString(MyBibleConstants.PREF_BIBLE_DOWNLOAD_SELECTED_LANG, result);
		}
		downloadSelectedLanguage = result;
		return result;
	}

	public void setDownloadSelectedLanguage(String downloadSelectedLanguage) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putString(MyBibleConstants.PREF_BIBLE_DOWNLOAD_SELECTED_LANG, downloadSelectedLanguage);
			editor.commit();
			this.downloadSelectedLanguage = downloadSelectedLanguage;
		}
	}

	public Boolean getUseExternalStorage() {
		if (useExternalStorage != null) {
			return useExternalStorage;
		}
		Boolean result = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			result = preferences.getBoolean(MyBibleConstants.PREF_BIBLE_USE_EXTERNAL_STORAGE, result);
		}
		useExternalStorage = result;
		return result;
	}

	public void setUseExternalStorage(Boolean useExternalStorage) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putBoolean(MyBibleConstants.PREF_BIBLE_USE_EXTERNAL_STORAGE, useExternalStorage);
			editor.commit();
			this.useExternalStorage = useExternalStorage;
		}
	}

}
