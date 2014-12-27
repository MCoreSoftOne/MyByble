package com.mcore.myvirtualbible.db;

import java.util.List;

import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.Highlighter;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;

public interface IMyBibleLocalServices {

	public boolean databaseContainsData();

	public String getBookChapterText(int bookId, int chapterNumber);

	public BibleTranslation getBibleVersionsById(int id);

	public List<BibleTranslation> getInstalledTranslations();

	public List<Book> getAllBooks(int bibleVersion);

	public boolean deleteTranslation(int trId);

	public boolean writeTranslationToDatabase(TranslationListDTO data);

	public TranslationListDTO readTranslationFromDatabase();

	public TranslationListDTO getDownloadbleTranslations();

	public int addTranslationToDataBase(String sourceFile) throws Exception;

	public BibleTranslation getSelectedBibleTranslation();

	public void setUseExternalStorage(boolean external);

	public boolean getUseExternalStorage();

	public List<HighlighterVerseMark> getHighlighterMarksByAll(
			Integer highlighterId, Integer bookorder, Integer chapter);
	
	public List<HighlighterVerseMark> getAllHighlighterMarks();
	
	public boolean existHighlighterVerses();

	public List<HighlighterVerse> getHighlighterMarksByBookChapter(
			int bookorder, int chapter);

	public List<Highlighter> getHighlighters();

	public boolean modifyHighlighter(Highlighter highlighter);

	public int insertHighlighter(Highlighter highlighter);

	public void deleteHighlighter(int highlighterId);

	public boolean existHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark);

	public void deleteHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark);
	
	public void deleteAllHighlighterVerse(int book,
			int chapter, String verseMark);

	public boolean modifyHighlighterVerse(HighlighterVerseMark highlighterMark);

	public boolean insertHighlighterVerse(HighlighterVerseMark highlighterMark);
	
	public boolean hasToMigrateDatabase();
	
	public int migrateDatabase();
	
	public void cleanTranslationDatabase();

}
