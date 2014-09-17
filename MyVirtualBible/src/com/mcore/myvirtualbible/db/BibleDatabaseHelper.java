package com.mcore.myvirtualbible.db;

import com.mcore.myvirtualbible.util.MyBibleConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.util.Log;

public class BibleDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String BIBLE_DB_TAG = "BIBLE_DD.BB";
	
	private boolean external;

	public BibleDatabaseHelper(Context context, String name,
			CursorFactory factory, int version, boolean external) {
		super(context, external? Environment.getExternalStorageDirectory()+MyBibleConstants.EXTERNAL_DB_DIR+name :name, factory, version);
		this.external = external;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	public void createTables(SQLiteDatabase db) {
		Log.d(BIBLE_DB_TAG, "CREATING DATABASE --------");
        db.execSQL("CREATE TABLE downloable_versions (id TEXT, name TEXT, version TEXT, language TEXT, md5 TEXT);");		
        db.execSQL("CREATE TABLE bible_versions (id INTEGER PRIMARY KEY autoincrement, name TEXT, abrev TEXT, revision TEXT, language TEXT, copyright TEXT, encryption_method TEXT, loaded INTEGER DEFAULT 0, othersprops TEXT);");		
        db.execSQL("CREATE TABLE books (id INTEGER PRIMARY KEY autoincrement, book_number NUMERIC, name TEXT, aliases TEXT, chapters_count NUMERIC, bible_version NUMERIC);");		
        db.execSQL("CREATE TABLE chapters (id INTEGER PRIMARY KEY autoincrement, number NUMERIC, chapter TEXT, book NUMERIC);");
        db.execSQL("CREATE TABLE repo_versions (id TEXT, name TEXT, version TEXT, language TEXT, md5 TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(BIBLE_DB_TAG, "UPDATING " + oldVersion + " to " + newVersion);
		if (oldVersion < 2) {
			try {
				db.execSQL("ALTER TABLE bible_versions ADD COLUMN language TEXT;");
				db.execSQL("ALTER TABLE bible_versions ADD COLUMN copyright TEXT;");
				db.execSQL("ALTER TABLE bible_versions ADD COLUMN encryption_method TEXT;");
				db.execSQL("ALTER TABLE bible_versions ADD COLUMN loaded INTEGER DEFAULT 0;");
				db.execSQL("ALTER TABLE bible_versions ADD COLUMN othersprops TEXT;");
				db.execSQL("CREATE TABLE repo_versions (id TEXT, name TEXT, version TEXT, language TEXT, md5 TEXT);");
		        db.execSQL("CREATE TABLE downloable_versions (id TEXT, name TEXT, version TEXT, language TEXT, md5 TEXT);");
			} catch (Exception e) {
				recreateAllDataBase(db);
			}
			try {
				adjustTableVersion1(db);
			} catch (Exception e) {
				// No pasa nada
			}
		}
	}
	
	private void adjustTableVersion1(SQLiteDatabase db) {
		db.execSQL("UPDATE bible_versions SET language='es', copyright='Public Domain.\\nTraducci�n realizada por las Sociedades B�blicas Unidas.', encryption_method='NONE', loaded=1, othersprops='';");
	}
	
	private void recreateAllDataBase(SQLiteDatabase db) {
		Log.d(BIBLE_DB_TAG, "RECREATING DATABASE ");
		db.execSQL("DROP TABLE IF EXISTS bible_versions");
		db.execSQL("DROP TABLE IF EXISTS books");
		db.execSQL("DROP TABLE IF EXISTS chapters");
		db.execSQL("DROP TABLE IF EXISTS repo_versions");
		createTables(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(BIBLE_DB_TAG, "DOWNDATING " + oldVersion + " to " + newVersion);
		recreateAllDataBase(db);
	}

	public boolean isExternal() {
		return external;
	}
}