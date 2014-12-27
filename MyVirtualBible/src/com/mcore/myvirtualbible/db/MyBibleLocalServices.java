package com.mcore.myvirtualbible.db;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.Chapter;
import com.mcore.myvirtualbible.model.Highlighter;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBibleConstants;
import com.mcore.myvirtualbible.util.MyBiblePreferences;

public class MyBibleLocalServices implements IMyBibleLocalServices {

	private static IMyBibleLocalServices instance;

	private Context context;

	private BibleDatabaseConnector dbConnector;

	private BiblePersonalDatabaseConnector dbPersonalConnector;

	public static IMyBibleLocalServices getInstance(Context context) {
		if (instance == null) {
			instance = (IMyBibleLocalServices) java.lang.reflect.Proxy
					.newProxyInstance(MyBibleLocalServices.class
							.getClassLoader(),
							new Class[] { IMyBibleLocalServices.class },
							new LocalServiceInvocationHandler(
									new MyBibleLocalServices(context)));
		}
		return instance;
	}

	private MyBibleLocalServices(Context context) {
		this.context = context;
		dbConnector = new BibleDatabaseConnector(context, MyBiblePreferences
				.getInstance(context).getUseExternalStorage());
		dbPersonalConnector = new BiblePersonalDatabaseConnector(context);
		getHighlighters();
	}

	public boolean databaseContainsData() {
		dbConnector.open();
		try {
			return dbConnector.wasPopulated();
		} finally {
			dbConnector.close();
		}
	}

	public String getBookChapterText(int bookId, int chapterNumber) {
		dbConnector.open();
		try {
			return dbConnector.getBookChapterText(bookId, chapterNumber);
		} finally {
			dbConnector.close();
		}
	}

	public BibleTranslation getBibleVersionsById(int id) {
		dbConnector.open();
		try {
			return dbConnector.getBibleTranslationById(id);
		} finally {
			dbConnector.close();
		}
	}

	public List<BibleTranslation> getInstalledTranslations() {
		dbConnector.open();
		try {
			return dbConnector.getInstalledTranslations();
		} finally {
			dbConnector.close();
		}
	}

	public List<Book> getAllBooks(int bibleVersion) {
		dbConnector.open();
		try {
			return dbConnector.getAllBooks(bibleVersion);
		} finally {
			dbConnector.close();
		}
	}

	public boolean deleteTranslation(int trId) {
		dbConnector.open();
		try {
			return dbConnector.deleteTranslation(trId);
		} finally {
			dbConnector.close();
		}
	}

	public boolean writeTranslationToDatabase(TranslationListDTO data) {
		dbConnector.open();
		try {
			return dbConnector.writeTranslationToDatabase(data);
		} finally {
			dbConnector.close();
		}
	}

	public TranslationListDTO readTranslationFromDatabase() {
		dbConnector.open();
		try {
			return dbConnector.readTranslationFromDatabase();
		} finally {
			dbConnector.close();
		}
	}

	public TranslationListDTO getDownloadbleTranslations() {
		dbConnector.open();
		try {
			TranslationListDTO result = dbConnector
					.readTranslationFromDatabase();
			if (result == null || result.getTranslations() == null
					|| result.getTranslations().length == 0) {
				result = BibleUtilities.readTranslationFromDefaultFile(context);
				dbConnector.writeTranslationToDatabase(result);
				result = dbConnector.readTranslationFromDatabase();
			}
			return result;
		} finally {
			dbConnector.close();
		}
	}

	public int addTranslationToDataBase(String sourceFile) throws Exception {
		int result = 0;
		dbConnector.open();
		try {
			boolean isSDCard = dbConnector.isExternal();
			InputStream ims;
			if (isSDCard) {
				ims = new FileInputStream(sourceFile);
			} else {
				ims = context.openFileInput(sourceFile);
			}
			String fileName = unzip(ims, isSDCard);
			if (fileName != null) {
				try {
					result = readFromDisk(fileName, isSDCard);
				} finally {
					if (isSDCard) {
						new File(fileName).delete();
					} else {
						context.deleteFile(fileName);
					}
				}
			}
		} finally {
			dbConnector.close();
		}
		return result;
	}

	private boolean verifyBibleExist(String translationabrev, String revision) {
		List<BibleTranslation> installedVersions = dbConnector
				.getInstalledTranslations();
		if (installedVersions != null) {
			for (Iterator iterator = installedVersions.iterator(); iterator
					.hasNext();) {
				BibleTranslation bibleTranslation = (BibleTranslation) iterator
						.next();
				if (bibleTranslation != null
						&& bibleTranslation.getAbrev() != null
						&& bibleTranslation.getRevision() != null
						&& bibleTranslation.getAbrev().equalsIgnoreCase(
								translationabrev)
						&& bibleTranslation.getRevision().equalsIgnoreCase(
								revision)) {
					return true;
				}
			}
		}
		return false;
	}

	private int readFromDisk(String fileData, boolean isSDCard)
			throws Exception {
		int result = 0;
		dbConnector.open();
		try {
			DataInputStream dos;
			if (isSDCard) {
				dos = new DataInputStream(new FileInputStream(fileData));
			} else {
				dos = new DataInputStream(context.openFileInput(fileData));
			}
			try {
				String app = dos.readUTF();
				String appversion = dos.readUTF();
				String translationname = dos.readUTF();
				String translationabrev = dos.readUTF();
				String revision = dos.readUTF();
				if (verifyBibleExist(translationabrev, revision)) {
					throw new Exception("Traducción existente");
				}
				dbConnector.getDatabase().beginTransaction();
				BibleTranslation bibleVersion = new BibleTranslation();
				bibleVersion.setName(translationname);
				bibleVersion.setAbrev(translationabrev);
				bibleVersion.setRevision(revision);
				Log.d("PopulateDB", app + " " + appversion + " "
						+ translationname + " " + translationabrev + " "
						+ revision);
				int p1 = dos.readInt();
				for (int i = 0; i < p1; i++) {
					String propName = dos.readUTF();
					String propValue = dos.readUTF();
					if (propName != null && propValue != null
							&& propValue.length() > 0 && propName.length() > 0) {
						if (propName.equals(CommonConstants.PROP_LANGUAGE)) {
							bibleVersion.setLanguage(propValue);
						} else if (propName
								.equals(CommonConstants.PROP_COPYRIGHT)) {
							bibleVersion.setCopyright(propValue);
						} else if (propName
								.equals(CommonConstants.PROP_ENCRYPTION_ALGORITHMS)) {
							bibleVersion.setEncryptionMethod(propValue);
						} else {
							bibleVersion.addToOthersprops(propName, propValue);
						}
					}
				}
				result = dbConnector.insertBibleVersion(bibleVersion);
				int s1 = dos.readInt();
				for (int i = 0; i < s1; i++) {
					int booknumber = dos.readInt();
					String name = dos.readUTF();
					int p2 = dos.readInt();
					for (int j = 0; j < p2; j++) {
						/* String propName = */dos.readUTF();
						/* String propValue = */dos.readUTF();
					}
					Log.d("PopulateDB", "Reading " + name);
					Book book = new Book();
					book.setBookNumber(booknumber);
					book.setBibleVersion(bibleVersion);
					book.setName(name);
					int s2 = dos.readInt();
					book.setChaptersSize(s2);
					dbConnector.insertBook(book);
					for (int j = 0; j < s2; j++) {
						int capNumber = dos.readInt();
						/* String chapterTitle = */dos.readUTF();
						int p3 = dos.readInt();
						for (int k = 0; k < p3; k++) {
							/* String propName = */dos.readUTF();
							/* String propValue = */dos.readUTF();
						}
						String chapterData = dos.readUTF();
						Chapter chapter = new Chapter();
						chapter.setBook(book);
						chapter.setNumber(capNumber);
						chapter.setChapter(chapterData);
						dbConnector.insertChapter(chapter);
					}
				}
				dbConnector.activateBibleTranslation(bibleVersion);
				dbConnector.getDatabase().setTransactionSuccessful();
			} finally {
				dos.close();
				dbConnector.getDatabase().endTransaction();
			}
		} finally {
			dbConnector.close();
		}
		return result;
	}

	private String unzip(InputStream fin, boolean isSDCard) throws Exception {
		ZipInputStream zin = new ZipInputStream(fin);
		ZipEntry ze = null;
		String result = "";
		while ((ze = zin.getNextEntry()) != null) {
			Log.d("Decompress", "Unzipping " + ze.getName());

			result = ze.getName();
			OutputStream fout;
			if (isSDCard) {
				result = Environment.getExternalStorageDirectory()
						+ MyBibleConstants.EXTERNAL_DB_DIR + result;
				fout = new FileOutputStream(result);
			} else {
				fout = context.openFileOutput(result, Context.MODE_PRIVATE);
			}

			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = zin.read(buffer)) != -1) {
				fout.write(buffer, 0, bytesRead);
			}

			zin.closeEntry();
			fout.close();

		}
		zin.close();
		return result;
	}

	public BibleTranslation getSelectedBibleTranslation() {
		// TODO Optimizar, tiene doble db.open()
		MyBiblePreferences preferences = MyBiblePreferences
				.getInstance(context);
		int id = preferences.getCurrentTranslation();
		BibleTranslation currentVersion = null;
		if (id > 0) {
			currentVersion = getBibleVersionsById(id);
		}
		if (currentVersion == null) {
			List<BibleTranslation> installedVersions = getInstalledTranslations();
			if (installedVersions != null && installedVersions.size() > 0
					&& installedVersions.get(0) != null) {
				currentVersion = installedVersions.get(0);
				if (currentVersion.getId() > 0) {
					preferences.setCurrentTranslation(currentVersion.getId());
				}
			}
		}
		return currentVersion;
	}

	public void setUseExternalStorage(boolean external) {
		dbConnector.setExternal(context, external);
	}

	public boolean getUseExternalStorage() {
		return dbConnector.isExternal();
	}

	@Override
	public List<HighlighterVerseMark> getHighlighterMarksByAll(
			Integer highlighterId, Integer bookorder, Integer chapter) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.getHighlighterMarksByAll(highlighterId, bookorder, chapter);
		} finally {
			dbPersonalConnector.close();
		}
	}
	
	@Override
	public List<HighlighterVerseMark> getAllHighlighterMarks() {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.getAllHighlighterMarks();
		} finally {
			dbPersonalConnector.close();
		}
	}
	
	@Override
	public boolean existHighlighterVerses() {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.existHighlighterVerses();
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public List<HighlighterVerse> getHighlighterMarksByBookChapter(
			int bookorder, int chapter) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.getHighlighterMarksByBookChapter(bookorder, chapter);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public List<Highlighter> getHighlighters() {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.getHighlighters();
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public boolean modifyHighlighter(Highlighter highlighter) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.modifyHighlighter(highlighter);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public int insertHighlighter(Highlighter highlighter) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.insertHighlighter(highlighter);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public void deleteHighlighter(int highlighterId) {
		dbPersonalConnector.open();
		try {
			dbPersonalConnector.deleteHighlighter(highlighterId);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public boolean existHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.existHighlighterVerse(highlighterId, book, chapter, verseMark);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public void deleteHighlighterVerse(int highlighterId, int book,
			int chapter, String verseMark) {
		dbPersonalConnector.open();
		try {
			dbPersonalConnector.deleteHighlighterVerse(highlighterId, book, chapter, verseMark);;
		} finally {
			dbPersonalConnector.close();
		}
		
	}
	
	@Override
	public void deleteAllHighlighterVerse(int book, int chapter,
			String verseMark) {
		dbPersonalConnector.open();
		try {
			dbPersonalConnector.deleteAllHighlighterVerse(book, chapter, verseMark);;
		} finally {
			dbPersonalConnector.close();
		}		
	}

	@Override
	public boolean modifyHighlighterVerse(HighlighterVerseMark highlighterMark) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.modifyHighlighterVerse(highlighterMark);
		} finally {
			dbPersonalConnector.close();
		}
	}

	@Override
	public boolean insertHighlighterVerse(HighlighterVerseMark highlighterMark) {
		dbPersonalConnector.open();
		try {
			return dbPersonalConnector.insertHighlighterVerse(highlighterMark);
		} finally {
			dbPersonalConnector.close();
		}
	}
	
	
	@Override
	public boolean hasToMigrateDatabase() {
		dbConnector.open();
		try {
			return dbConnector.hasToMigrateDatabase();
		} finally {
			dbConnector.close();
		}
	}
	
	@Override
	public int migrateDatabase() {
		dbConnector.open();
		try {
			return dbConnector.migrateDatabase();
		} finally {
			dbConnector.close();
		}
	}
	
	@Override
	public void cleanTranslationDatabase() {
		dbConnector.open();
		try {
			dbConnector.cleanTranslationDatabase();
		} finally {
			dbConnector.close();
		}		
	}

	private static class LocalServiceInvocationHandler implements
			InvocationHandler {

		private MyBibleLocalServices localservice;

		public LocalServiceInvocationHandler(MyBibleLocalServices localservice) {
			this.localservice = localservice;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			try {
				return method.invoke(localservice, args);
			} catch (Exception e) {
				// TODO Pendiente un mensaje
				System.exit(0);
				return null;
			}
		}
	}


}
