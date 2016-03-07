package com.mcore.myvirtualbible.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mcore.myvirtualbible.model.Highlighter;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;

public class BiblePersonalDatabaseConnector {

	private static final String PERSONAL_DB_NAME = "PersonalData.db";

	private static final int CURRENT_PERSONAL_DATABASE_VERSION = 2;

	private BiblePersonalDatabaseHelper dbPersonalOpenHelper;

	private SQLiteDatabase database;

	private List<Highlighter> highlighterList;

	public BiblePersonalDatabaseConnector(Context context) {
		dbPersonalOpenHelper = new BiblePersonalDatabaseHelper(context,
				PERSONAL_DB_NAME, null, CURRENT_PERSONAL_DATABASE_VERSION);
	}

	public void open() throws SQLException {
		database = dbPersonalOpenHelper.getWritableDatabase();
	}

	public void close() {
		if (database != null)
			database.close();
	}

	public boolean isOpen() {
		if (database != null)
			return database.isOpen();
		return false;
	}
	
	public List<HighlighterVerseMark> getAllHighlighterMarks() {
		Cursor tCursor = database.query("highlighter_verse", new String[] {
				"highlighter_id", "book", "chapter", "verse_mark",
				"verse_range_low", "verse_range_high", "extract", "note" },
				null, null, null, null, "highlighter_id");
		tCursor.moveToFirst();
		List<HighlighterVerseMark> result = new ArrayList<HighlighterVerseMark>();
		while (!tCursor.isAfterLast()) {
			HighlighterVerseMark comment = cursorToHighlighterVerseMark(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}

	public List<HighlighterVerseMark> getHighlighterMarksByAll(
			Integer highlighterId, Integer bookorder, Integer chapter) {
		String selection = "";
		if (bookorder != null) {
			selection += "highlighter_id = " + highlighterId + " and ";
		}
		if (selection.length() > 0) {
			selection += " and ";
		}
		if (bookorder != null) {
			selection += "book = " + bookorder + " and ";
		}
		selection += chapter != null ? "chapter = " + chapter : "";
		Cursor tCursor = database.query("highlighter_verse", new String[] {
				"highlighter_id", "book", "chapter", "verse_mark",
				"verse_range_low", "verse_range_high", "extract", "note" },
				selection, null, null, null, "highlighter_id");
		tCursor.moveToFirst();
		List<HighlighterVerseMark> result = new ArrayList<HighlighterVerseMark>();
		while (!tCursor.isAfterLast()) {
			HighlighterVerseMark comment = cursorToHighlighterVerseMark(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}

	public List<HighlighterVerse> getHighlighterMarksByBookChapter(
			int bookorder, int chapter) {
		Cursor tCursor = database.query("highlighter_verse", new String[] {
				"highlighter_id", "verse_mark" }, "book = " + bookorder
				+ " and chapter = " + chapter, null, null, null,
				"highlighter_id");
		tCursor.moveToFirst();
		List<HighlighterVerse> result = new ArrayList<HighlighterVerse>();
		while (!tCursor.isAfterLast()) {
			HighlighterVerse comment = cursorToHighlighterVerse(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}

	public List<Highlighter> getHighlighters() {
		if (highlighterList == null || highlighterList.size() == 0) {
			Cursor tCursor = database.query("highlighter_config", new String[] {
					"id", "name", "color", "data", "note" }, null, null, null,
					null, "id");
			tCursor.moveToFirst();
			highlighterList = new ArrayList<Highlighter>();
			while (!tCursor.isAfterLast()) {
				Highlighter comment = cursorToHighlighter(tCursor);
				highlighterList.add(comment);
				tCursor.moveToNext();
			}
		}
		return highlighterList;
	}

	public boolean modifyHighlighter(Highlighter highlighter) {
		if (highlighter == null || highlighter.getId() <= 0) {
			return false;
		}
		ContentValues newHighlighter = new ContentValues();
		newHighlighter.put("name", highlighter.getName());
		newHighlighter.put("color", highlighter.getColor());
		newHighlighter.put("data", highlighter.getData());
		newHighlighter.put("note", highlighter.getNote());
		try {
			long result = database.update("highlighter_config", newHighlighter,
					"id = " + highlighter.getId(), null);
			return result > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public int insertHighlighter(Highlighter highlighter) {
		if (highlighter == null) {
			return -1;
		}
		ContentValues newHighlighter = new ContentValues();
		newHighlighter.put("name", highlighter.getName());
		newHighlighter.put("color", highlighter.getColor());
		newHighlighter.put("data", highlighter.getData());
		newHighlighter.put("note", highlighter.getNote());
		long result = database.insert("highlighter_config", null,
				newHighlighter);
		highlighter.setId((int) result);
		if (highlighterList != null && highlighter.getId() > 0) {
			highlighterList.add(highlighter);
		}
		return (int) result;
	}

	public void deleteHighlighter(int highlighterId) {
		if (highlighterId > 1) {
			database.delete("highlighter_verse", "highlighter_id = "
					+ highlighterId, null);
			database.delete("highlighter_config", "id = " + highlighterId, null);
		}
	}

	public boolean existHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark) {
		if (highlighterId <= 0 || book <= 0 || chapter <= 0
				|| verseMark == null || verseMark.length() == 0) {
			return false;
		}
		Cursor tCursor = database.query("highlighter_verse",
				new String[] { "highlighter_id" }, "highlighter_id="
						+ highlighterId + " and book=" + book + " and chapter="
						+ chapter + " and verse_mark = ?",
				new String[] { verseMark }, null, null, null, null);
		return !tCursor.isAfterLast();
	}
	
	public boolean existHighlighterVerses() {
		Cursor tCursor = database.query("highlighter_verse",
				new String[] { "highlighter_id" }, null,
				null, null, null, null, null);
		return !tCursor.isAfterLast();
	}

	public void deleteHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark) {
		if (highlighterId <= 0 || book <= 0 || chapter <= 0
				|| verseMark == null || verseMark.length() == 0) {
			return;
		}
		database.delete("highlighter_verse", "highlighter_id=" + highlighterId
				+ " and book=" + book + " and chapter=" + chapter
				+ " and verse_mark = ?", new String[] { verseMark });
	}

	public void deleteAllHighlighterVerse(int book, int chapter,
			String verseMark) {
		if (book <= 0 || chapter <= 0 || verseMark == null
				|| verseMark.length() == 0) {
			return;
		}
		database.delete("highlighter_verse", "book=" + book + " and chapter="
				+ chapter + " and verse_mark = ?", new String[] { verseMark });
	}

	public boolean modifyHighlighterVerse(HighlighterVerseMark highlighterMark) {
		if (highlighterMark == null || highlighterMark.getConfig() == null
				|| highlighterMark.getConfig().getId() <= 0
				|| highlighterMark.getBook() <= 0
				|| highlighterMark.getChapter() <= 0
				|| highlighterMark.getVerseMark() == null
				|| highlighterMark.getVerseMark().length() == 0) {
			return false;
		}
		ContentValues newHighlighter = new ContentValues();
		newHighlighter.put("extract", highlighterMark.getExtract());
		newHighlighter.put("note", highlighterMark.getNote());
		try {
			database.update("highlighter_verse", newHighlighter,
					"highlighter_id=" + highlighterMark.getConfig().getId()
							+ " and book=" + highlighterMark.getBook()
							+ " and chapter=" + highlighterMark.getChapter()
							+ " and verse_mark = ?",
					new String[] { highlighterMark.getVerseMark() });
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean insertHighlighterVerse(HighlighterVerseMark highlighterMark) {
		if (highlighterMark == null || highlighterMark.getConfig() == null) {
			return false;
		}
		if (existHighlighterVerse(highlighterMark.getConfig().getId(),
				highlighterMark.getBook(), highlighterMark.getChapter(),
				highlighterMark.getVerseMark())) {
			return modifyHighlighterVerse(highlighterMark);
		}
		ContentValues newHighlighter = new ContentValues();
		newHighlighter.put("highlighter_id", highlighterMark.getConfig()
				.getId());
		newHighlighter.put("book", highlighterMark.getBook());
		newHighlighter.put("chapter", highlighterMark.getChapter());
		newHighlighter.put("verse_mark", highlighterMark.getVerseMark());
		newHighlighter.put("verse_range_low",
				highlighterMark.getVerseRangeLow());
		newHighlighter.put("verse_range_high",
				highlighterMark.getVerseRangeHigh());
		newHighlighter.put("extract", highlighterMark.getExtract());
		newHighlighter.put("note", highlighterMark.getNote());
		try {
			database.insertOrThrow("highlighter_verse", null, newHighlighter);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private HighlighterVerseMark cursorToHighlighterVerseMark(Cursor tCursor) {
		HighlighterVerseMark result = new HighlighterVerseMark();
		result.setConfig(findHighlighter(tCursor.getInt(0)));
		result.setBook(tCursor.getInt(1));
		result.setChapter(tCursor.getInt(2));
		result.setVerseMark(tCursor.getString(3));
		result.setVerseRangeLow(tCursor.getInt(4));
		result.setVerseRangeHigh(tCursor.getInt(5));
		result.setExtract(tCursor.getString(6));
		result.setNote(tCursor.getString(7));
		return result;
	}

	private HighlighterVerse cursorToHighlighterVerse(Cursor tCursor) {
		HighlighterVerse result = new HighlighterVerse();
		result.setConfig(findHighlighter(tCursor.getInt(0)));
		result.setVerseMark(tCursor.getString(1));
		return result;
	}

	private Highlighter findHighlighter(int id) {
		List<Highlighter> highlighters = getHighlighters();
		if (highlighters != null) {
			for (Iterator iterator = highlighters.iterator(); iterator
					.hasNext();) {
				Highlighter highlighterConfig = (Highlighter) iterator.next();
				if (highlighterConfig != null
						&& highlighterConfig.getId() == id) {
					return highlighterConfig;
				}
			}
		}
		return null;
	}

	private Highlighter cursorToHighlighter(Cursor tCursor) {
		Highlighter result = new Highlighter();
		result.setId(tCursor.getInt(0));
		result.setName(tCursor.getString(1));
		result.setColor(tCursor.getInt(2));
		result.setData(tCursor.getString(3));
		result.setNote(tCursor.getString(4));
		return result;
	}

}