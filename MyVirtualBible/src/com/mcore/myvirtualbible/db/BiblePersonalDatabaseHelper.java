package com.mcore.myvirtualbible.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

public class BiblePersonalDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String BIBLE_DB_TAG = "BIBLE_PERSONAL_DD.BB";


	public BiblePersonalDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	public void createTables(SQLiteDatabase db) {
		Log.d(BIBLE_DB_TAG, "CREATING DATABASE --------");
        db.execSQL("CREATE TABLE highlighter_config (id INTEGER PRIMARY KEY autoincrement, name TEXT, color NUMERIC, data TEXT, note TEXT);");
        db.execSQL("CREATE TABLE highlighter_verse (highlighter_id NUMERIC, book NUMERIC, chapter NUMERIC, verse_mark TEXT, verse_range_low NUMERIC, verse_range_high NUMERIC, extract TEXT, note TEXT)");
        populateHighlighter(db);
	}
	
	private void populateHighlighter(SQLiteDatabase db) {
		createHighlighter(db, "Fuchsia", Color.parseColor("#C072ED"),"", "Highlighter 1: Bible Verses");
		createHighlighter(db, "Green", Color.parseColor("#2BF042"),"", "Highlighter 2: Bible Verses");
		createHighlighter(db, "Yellow", Color.parseColor("#EBEB1E"),"", "Highlighter 3: Bible Verses");
	}
	
	private void createHighlighter(SQLiteDatabase db, String name, int color, String data, String note) {
		ContentValues newHighlighter = new ContentValues();
		newHighlighter.put("name", name);
		newHighlighter.put("color", color);
		newHighlighter.put("data", data);
		newHighlighter.put("note", note);
		db.insert("highlighter_config", null, newHighlighter);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
}