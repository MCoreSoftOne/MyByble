package com.mcore.myvirtualbible.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.Chapter;

public class BibleDatabaseConnector {

	private static final String DB_NAME = "MyBibles";
	private SQLiteDatabase database;
	private BibleDatabaseHelper dbOpenHelper;
	
	private static final int CURRENT_BIBLE_DATABASE_VERSION = 2;

	public BibleDatabaseConnector(Context context, boolean external) {
		dbOpenHelper = new BibleDatabaseHelper(context, DB_NAME, null, CURRENT_BIBLE_DATABASE_VERSION, external);
	}

	public void open() throws SQLException {
		database = dbOpenHelper.getWritableDatabase();
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
	
	public boolean isExternal() {
		return dbOpenHelper.isExternal();
	}
 	
	public void setExternal(Context context, boolean external) {
		if (external != isExternal()) {
			if (isOpen()) {
				close();
			}
			dbOpenHelper = new BibleDatabaseHelper(context, DB_NAME, null, CURRENT_BIBLE_DATABASE_VERSION, external);
		}
	}

	public boolean wasPopulated() {
		Cursor tCursor = database.query("bible_versions", new String[] { "id",
				"name" }, "loaded = 1", null, null, null, "name");
		return tCursor.getCount() > 0;
	}
	
	public void activateBibleTranslation(BibleTranslation bibleTranslation) {
		ContentValues uptBibleTranslation = new ContentValues();
		uptBibleTranslation.put("loaded", 1);
		database.update("bible_versions", uptBibleTranslation, "id=?", new String[] {""+bibleTranslation.getId()});
	}

	public int insertBibleVersion(BibleTranslation bibleTranslation) {
		if (bibleTranslation == null) {
			return -1;
		}
		ContentValues newBibleVersion = new ContentValues();
		newBibleVersion.put("name", bibleTranslation.getName());
		newBibleVersion.put("abrev", bibleTranslation.getAbrev());
		newBibleVersion.put("revision", bibleTranslation.getRevision());
		newBibleVersion.put("language", bibleTranslation.getLanguage());
		newBibleVersion.put("copyright", bibleTranslation.getCopyright());
		newBibleVersion.put("encryption_method", bibleTranslation.getEncryptionMethod());
		newBibleVersion.put("loaded", 0);
		long result = database.insert("bible_versions", null, newBibleVersion);
		bibleTranslation.setId((int) result);
		return (int) result;
	}

	public int insertBook(Book book) {
		if (book == null) {
			return -1;
		}
		ContentValues newBook = new ContentValues();
		newBook.put("book_number", book.getBookNumber());
		newBook.put("name", book.getName());
		newBook.put("chapters_count", book.getChaptersSize());
		newBook.put("bible_version", book.getBibleVersion().getId());
		
		long result = database.insert("books", null, newBook);
		book.setId((int) result);
		return (int) result;
	}

	public int insertChapter(Chapter chapter) {
		if (chapter == null) {
			return -1;
		}
		ContentValues newChapter = new ContentValues();
		newChapter.put("number", chapter.getNumber());
		newChapter.put("chapter", chapter.getChapter());
		newChapter.put("book", chapter.getBook().getId());
		long result = database.insert("chapters", null, newChapter);
		chapter.setId((int) result);
		return (int) result;
	}
	
	public List<BibleTranslation> getInstalledTranslations() {
		Cursor tCursor = database.query("bible_versions", new String[] { "id", "name",
				"abrev", "revision", "language", "copyright", "encryption_method", "loaded" }, "loaded=1", null, null, null, "id");
		tCursor.moveToFirst();
		List<BibleTranslation> result = new ArrayList<BibleTranslation>();
		while (!tCursor.isAfterLast()) {
			BibleTranslation comment = cursorToBibleTranslation(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}
	
	public BibleTranslation getBibleTranslationById(int id) {
		Cursor tCursor = database.query("bible_versions", new String[] { "id", "name",
				"abrev", "revision", "language", "copyright", "encryption_method", "loaded" }, "id=" + id, null, null, null, "id");
		tCursor.moveToFirst();
		BibleTranslation result = null;
		if (!tCursor.isAfterLast()) {
			result = cursorToBibleTranslation(tCursor);
		}
		return result;
	}
	
	private BibleTranslation cursorToBibleTranslation(Cursor tCursor) {
		BibleTranslation result = new BibleTranslation();
		result.setId(tCursor.getInt(0));
		result.setName(tCursor.getString(1));
		result.setAbrev(tCursor.getString(2));
		result.setRevision(tCursor.getString(3));
		result.setLanguage(tCursor.getString(4));
		result.setCopyright(tCursor.getString(5));
		result.setEncryptionMethod(tCursor.getString(6));
		result.setLoaded(tCursor.getInt(7) == 1);
		return result;
	}

	public List<Book> getAllBooks(int bibleVersion) {
		Cursor tCursor = database.query("books", new String[] { "id", "name",
				"chapters_count", "book_number" }, " bible_version = " + bibleVersion, null, null, null, "book_number");
		tCursor.moveToFirst();
		List<Book> result = new ArrayList<Book>();
		while (!tCursor.isAfterLast()) {
			Book comment = cursorToBook(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}
	
	public List<Book> getBooksByFilter(int filter, int bibleVersion) {
		String filterStr = "";
		if (filter == 1) {
			filterStr = "book_number <= 39";
		} else {
			filterStr = "book_number > 39";
		}
		filterStr += " and bible_version="+bibleVersion;
		Cursor tCursor = database.query("books", new String[] { "id", "name",
				"chapters_count", "book_number" }, filterStr, null, null, null, "book_number");
		tCursor.moveToFirst();
		List<Book> result = new ArrayList<Book>();
		while (!tCursor.isAfterLast()) {
			Book comment = cursorToBook(tCursor);
			result.add(comment);
			tCursor.moveToNext();
		}
		return result;
	}

	private Book cursorToBook(Cursor tCursor) {
		Book result = new Book();
		result.setId(tCursor.getInt(0));
		result.setName(tCursor.getString(1));
		result.setChaptersSize(tCursor.getInt(2));
		result.setBookNumber(tCursor.getInt(3));
		return result;
	}

	public String getBookChapterText(int bookId, int chapterNumber) {
		Cursor tCursor = database.query("chapters", new String[] { "id", "number",
				"book", "chapter" }, "book="+bookId+" and number="+chapterNumber, null, null, null, "number");
		tCursor.moveToFirst();
		String result = "";
		if (!tCursor.isAfterLast()) {
			result = tCursor.getString(3);
		}		
		return result;
	}
	
	public TranslationListDTO readTranslationFromDatabase() {
		TranslationListDTO result = new TranslationListDTO();
		List<TranslationDTO> traslations = new ArrayList<TranslationDTO>();
		Cursor tCursor = database.query("downloable_versions", new String[] { "id", "name", "version", "language", "md5" }, null, null, null, null, "id");
		tCursor.moveToFirst();
		while (!tCursor.isAfterLast()) {
			traslations.add(new TranslationDTO(tCursor.getString(0), tCursor.getString(1), 
					tCursor.getString(2), tCursor.getString(3), tCursor.getString(4)));
			tCursor.moveToNext();
		}		
		result.setTranslations((TranslationDTO[]) traslations.toArray(new TranslationDTO[traslations.size()]));
		return result;
	}
	
	public boolean deleteTranslation(int trId) {
		boolean result = false;
		if (trId > 0) {
			database.beginTransaction();
			try {
				Cursor tCursor = database.query("books", new String[] {"id"}, "bible_version="+trId, null, null, null, null);
				tCursor.moveToFirst();
				int count = 0;
				while (!tCursor.isAfterLast()) {
					int bookId = tCursor.getInt(0);
					database.delete("chapters", "book=?", new String[] {String.valueOf(bookId)});
					count++;
					tCursor.moveToNext();
				}
				if (count > 0) {
					database.delete("books", "bible_version=?", new String[] {String.valueOf(trId)});
					database.delete("bible_versions", "id=?", new String[] {String.valueOf(trId)});
					database.setTransactionSuccessful();
					result = true;
				}
			} catch (Exception e) {
					
			} finally {
				database.endTransaction();
			}
		}
		return result;		
	}
	
	public boolean writeTranslationToDatabase(TranslationListDTO data) {
		boolean result = false;
		if (data != null && data.getTranslations() != null) {
			TranslationDTO[] list = data.getTranslations();
			database.beginTransaction();
			try {
				database.execSQL("DELETE FROM downloable_versions;");
				for (int i = 0; i < list.length; i++) {
					if (list[i] != null) {
						ContentValues newChapter = new ContentValues();
						newChapter.put("id", list[i].getId());
						newChapter.put("name", list[i].getName());
						newChapter.put("version", list[i].getVersion());
						newChapter.put("language", list[i].getLanguage());
						newChapter.put("md5", list[i].getMd5());
						database.insert("downloable_versions", null, newChapter);
					}
				}				
				database.setTransactionSuccessful();
				result = true;
			} catch (Exception e) {
					
			} finally {
				database.endTransaction();
			}
		}
		return result;		
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}
	
}
