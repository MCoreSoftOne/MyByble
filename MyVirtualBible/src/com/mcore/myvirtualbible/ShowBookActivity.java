package com.mcore.myvirtualbible;

import static com.mcore.myvirtualbible.util.MyBibleConstants.ACTION_GOTO_VERSE;
import static com.mcore.myvirtualbible.util.MyBibleConstants.ACTION_SEARCH;
import static com.mcore.myvirtualbible.util.MyBibleConstants.ACTION_SET_VERSE_MARK;
import static com.mcore.myvirtualbible.util.MyBibleConstants.ACTION_UNSET_VERSE_MARK;
import static com.mcore.myvirtualbible.util.MyBibleConstants.ACTION_USE_TRANS;
import static com.mcore.myvirtualbible.util.MyBibleConstants.CATEGORY_USES;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.adapters.ViewPagerAdapter;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.dialog.DownloadDialog;
import com.mcore.myvirtualbible.listeners.IVerseSelectionListener;
import com.mcore.myvirtualbible.model.BiblePosition;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.Highlighter;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBiblePreferences;

public class ShowBookActivity extends BaseGeneralActivity implements
		IVerseSelectionListener {

	private static final int PREF_REQUEST_CODE = 10001;
	
	private static final int VERSELIST_REQUEST_CODE = 10002;

	private static final int action_refresh = 10001;
	
	private static final int action_test = 10002;
	
	private ViewPager mViewPager;
	
	private List<Book> books;
	
	private ViewPagerAdapter pagerAdapter;
	
	private BibleTranslation currentTranslation;

	private MyBiblePreferences preferences;

	private ActionMode mMode;
	
	private boolean lastModeWasClicked;
	
	private List<SelectedVerse> selectedVerses;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = MyBiblePreferences.getInstance(this
				.getApplicationContext());
		selectedVerses = new ArrayList<ShowBookActivity.SelectedVerse>();
		setContentView(R.layout.activity_show_book);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getSelectedBibleVersion();
		refreshBookList();
		getSupportActionBar().setHomeButtonEnabled(true);
		MyBibleLocalServices.getInstance(getApplicationContext());

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		pagerAdapter = new ViewPagerAdapter(this, currentTranslation, books,
				preferences, this);
		mViewPager.setAdapter(pagerAdapter);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				updateChapterPosition();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		int position = preferences.getLastPosition();
		mViewPager.setCurrentItem(position);
		updateChapterPosition();
	}

	@Override
	protected void onPause() {
		/* No se aplica en esta version
		if (preferences != null && pagerAdapter != null && mViewPager != null) {
			int position = mViewPager.getCurrentItem();
			preferences.setLastScrollPosition(
					pagerAdapter.getScrollPosByPosition(mViewPager, position),
					position);
		}*/
		super.onPause();
	}

	private void gotoText(Book book, int chapter, int verse) {
		if (book != null) {
			int index = pagerAdapter
					.getPositionFromBiblePosition(new BiblePosition(book,
							chapter, verse));
			mViewPager.setCurrentItem(index);
		}
	}

	private BibleTranslation getSelectedBibleVersion() {
		IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
				.getInstance(getApplicationContext());
		currentTranslation = myBibleLocalServices.getSelectedBibleTranslation();
		return currentTranslation;
	}

	private void refreshBookList() {
		ImageButton addTranslationBtn = (ImageButton) findViewById(R.id.btnDownloadTranslation);
		if (currentTranslation != null) {
			books = MyBibleLocalServices.getInstance(getApplicationContext())
					.getAllBooks(currentTranslation.getId());
			addTranslationBtn.setVisibility(View.GONE);
		} else {
			Book book = new Book();
			book.setName(getString(R.string.no_translations_availables));
			book.setChaptersSize(1);
			books = new ArrayList<Book>();
			books.add(book);
			addTranslationBtn.setVisibility(View.VISIBLE);
			addTranslationBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					DownloadDialog dialog = new DownloadDialog(
							ShowBookActivity.this) {
						protected void onOk(TranslationDTO data) {
							updateByChangeTranslation();
							DownloadDialog.sendDownloadEvent(ShowBookActivity.this, data);
						};

						protected void onCancel() {
							updateByChangeTranslation();
						};
					};
					dialog.show();
				}
			});
		}
	}

	private void cleanSelection() {
		cleanSelection(true);
	}

	private void cleanSelection(boolean finishing) {
		int position = mViewPager.getCurrentItem();
		pagerAdapter.cleanSelection(pagerAdapter.getViewByPosition(mViewPager,
				position - 1));
		pagerAdapter.cleanSelection(pagerAdapter.getViewByPosition(mViewPager,
				position));
		pagerAdapter.cleanSelection(pagerAdapter.getViewByPosition(mViewPager,
				position + 1));
		if (finishing && mMode != null) {
			mMode.finish();
		}
		mMode = null;
		lastModeWasClicked = false;
		selectedVerses.clear();
	}

	private void updateChapterPosition() {
		int position = mViewPager.getCurrentItem();
		cleanSelection();

		preferences.setLastPosition(position);
		BiblePosition biblePosition = pagerAdapter
				.getBiblePositionFromIndex(position);
		if (biblePosition != null && biblePosition.getBook() != null) {
			setTitleSubTitle(biblePosition.getBook().getName(),
					biblePosition.getChapter());
		} else {
			setTitleSubTitle("No Information", 0);
		}
	}

	protected void setTitleSubTitle(String bookname, int chapter) {
		getSupportActionBar().setTitle(bookname);
		getSupportActionBar().setSubtitle(
				getResources().getString(R.string.chapter) + " " + chapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PREF_REQUEST_CODE) {
			updateByChangeTranslation();
		}
		if (requestCode == VERSELIST_REQUEST_CODE) {
			if (data != null && resultCode == RESULT_OK) {
				Serializable extra = data.getSerializableExtra("result");
				if (extra instanceof HighlighterVerseMark) {
					gotoVerseMark((HighlighterVerseMark) extra);
					sendEvent(CATEGORY_USES, ACTION_GOTO_VERSE);
				}
			} else {
				updateByChangeTranslation();
			}
		}
	}
	
	private void gotoVerseMark(HighlighterVerseMark mark) {
		if (mark != null) {			
			gotoText(getBookById(mark.getBook()), mark.getChapter(), mark.getVerseRangeLow());
		}
	}

	private Book getBookById(int bookId) {
		if (books != null) {
			for (Iterator iterator = books.iterator(); iterator.hasNext();) {
				Book book = (Book) iterator.next();
				if (book != null && book.getBookNumber() == bookId) {
					return book;
				}
			}
		}
		return null;
	}
	
	private void updateByChangeTranslation() {
		getSelectedBibleVersion();
		refreshBookList();
		if (pagerAdapter != null) {
			pagerAdapter.changeTranslation(currentTranslation, books);
			pagerAdapter.notifyDataSetChanged();
		}
		updateChapterPosition();
		supportInvalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
				.getInstance(getApplicationContext());
		getSupportMenuInflater().inflate(R.menu.menu_show_book, menu);
		if (menu != null) {
			List<BibleTranslation> translations = myBibleLocalServices.getInstalledTranslations();
			for (Iterator iterator = translations.iterator(); iterator.hasNext();) {
				BibleTranslation bibleTranslation = (BibleTranslation) iterator
						.next();
				if (currentTranslation == null
						|| (bibleTranslation != null && bibleTranslation
								.getId() != currentTranslation.getId())) {
					MenuItem trMenu = menu.add(0, bibleTranslation.getId(), 0,
							getResources().getString(R.string.use_text) + " "
									+ bibleTranslation.getName());
					trMenu.setIcon(R.drawable.ic_data);
					trMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				} else if (bibleTranslation != null) {
					MenuItem trMenu = menu.add(0, bibleTranslation.getId(), 0,
							" >> "	+ bibleTranslation.getName());
					trMenu.setIcon(R.drawable.ic_data);
					trMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
					trMenu.setEnabled(false);
				}
			}
		}
		if (myBibleLocalServices != null && !myBibleLocalServices.existHighlighterVerses()) {			
			MenuItem item = (MenuItem) menu.findItem(R.id.action_saved_verses);
			if (item != null) {				
				item.setVisible(false);
			}
		}
		if (CommonConstants.MYBIBLE_DEVELOPER_MODE) {
			menu.add(0, action_refresh, 0, R.string.action_refresh)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			menu.add(0, action_test, 0, R.string.action_test)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_next:
			nextPage();
			return true;
		case R.id.action_previous:
			previousPage();
			return true;
		case android.R.id.home:
		case R.id.action_search:
			openBookChapterDialog();
			sendEvent(CATEGORY_USES, ACTION_SEARCH);
			return true;
		case R.id.action_setting:
			Intent intent = new Intent(this, PreferenceActivity.class);
			startActivityForResult(intent, PREF_REQUEST_CODE);
			return true;
		case R.id.action_saved_verses:
			Intent intent2 = new Intent(this, VerseListActivity.class);
			startActivityForResult(intent2, VERSELIST_REQUEST_CODE);
			return true;
		case action_refresh:
			pagerAdapter.notifyDataSetChanged();
			return true;
		case action_test: {
			//Solo para pruebas del desarrollador.
		}
		default:
			IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
					.getInstance(getApplicationContext());
			List<BibleTranslation> translations = myBibleLocalServices
					.getInstalledTranslations();
			for (Iterator iterator = translations.iterator(); iterator
					.hasNext();) {
				BibleTranslation bibleTranslation = (BibleTranslation) iterator
						.next();
				if (bibleTranslation.getId() == item.getItemId()) {
					preferences.setCurrentTranslation(bibleTranslation.getId());
					updateByChangeTranslation();
					sendEvent(CATEGORY_USES, ACTION_USE_TRANS);
				}
			}
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void selectVerse(String verse, String text) {
		if (verse == null) {
			finishContextMenu();
			return;
		}
		SelectedVerse selected = findVerseOnSelected(verse);
		if (selected == null) {			
			SelectedVerse verseData = new SelectedVerse();
			verseData.text = text;
			verseData.id = verse;
			selectedVerses.add(verseData);
		}
		if (mMode == null) {			
			mMode = startActionMode(new FavoriteMarksActionMode(isVersetMarked(verse),
					MyBibleLocalServices.getInstance(getApplicationContext())
					.getHighlighters()));
			lastModeWasClicked = false;
			int doneButtonId = Resources.getSystem().getIdentifier(
					"action_mode_close_button", "id", "android");
			View doneButton = findViewById(doneButtonId);
			if (doneButton != null) {
				doneButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cleanSelection();
					}
				});
			}
		}
	}
	
	@Override
	public void unSelectVerse(String verse) {
		SelectedVerse unselected = findVerseOnSelected(verse);
		if (unselected != null) {
			selectedVerses.remove(unselected);
			if (selectedVerses.size() == 0) {
				finishContextMenu();
			}
		}
	}
	
	private void finishContextMenu() {
		cleanSelection();
	}
	
	private SelectedVerse findVerseOnSelected(String verse) {
		for (Iterator iterator = selectedVerses.iterator(); iterator.hasNext();) {
			SelectedVerse item = (SelectedVerse) iterator.next();
			if (item != null && item.id != null && item.id.equals(verse)) {
				return item;
			}
		}
		return null;
	}

	private boolean isVersetMarked(String verse) {
		if (verse != null) {
			IMyBibleLocalServices instance = MyBibleLocalServices
					.getInstance(getApplicationContext());
			BiblePosition biblePosition = pagerAdapter
					.getBiblePositionFromIndex(mViewPager.getCurrentItem());
			List<HighlighterVerse> highlighterList = instance
					.getHighlighterMarksByBookChapter(biblePosition.getBook()
							.getBookNumber(), biblePosition.getChapter());
			if (highlighterList != null) {
				for (Iterator iterator = highlighterList.iterator(); iterator
						.hasNext();) {
					HighlighterVerse highlighterVerse = (HighlighterVerse) iterator
							.next();
					if (highlighterVerse != null
							&& highlighterVerse.getVerseMark() != null
							&& highlighterVerse.getVerseMark().equals(
									verse)) {
						return true;
					}

				}
			}
		}
		return false;
	}

	public void nextPage() {
		mViewPager.setCurrentItem(getItem(+1), true);
	}

	public void previousPage() {
		mViewPager.setCurrentItem(getItem(-1), true);
	}

	public int pageCount() {
		return pagerAdapter.getCount();
	}

	private int getItem(int i) {
		int a = mViewPager.getCurrentItem();
		i += a;
		return i;
	}

	private void openBookChapterDialog() {
		BiblePosition biblePosition = pagerAdapter
				.getBiblePositionFromIndex(mViewPager.getCurrentItem());
		final Dialog dialog = new Dialog(this.getSupportActionBar()
				.getThemedContext());
		dialog.setContentView(R.layout.book_chapter_dialog);
		dialog.setTitle(getResources().getString(R.string.book_selection_title));

		final IcsSpinner bookSpinner = (IcsSpinner) dialog
				.findViewById(R.id.book_spinner);
		final IcsSpinner chapterSpinner = (IcsSpinner) dialog
				.findViewById(R.id.chapter_spinner);
		final BookArrayAdapter bookAdapter = new BookArrayAdapter(this,
				android.R.layout.simple_list_item_1, books);
		bookSpinner.setAdapter(bookAdapter);
		bookSpinner
				.setOnItemSelectedListener(new IcsAdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(IcsAdapterView<?> parent,
							View view, int position, long id) {
						Book book = bookAdapter.getItem(position);
						fillDialogChapterSpinner(chapterSpinner, book);
					}

					@Override
					public void onNothingSelected(IcsAdapterView<?> parent) {

					}

				});
		setSpinnerBibleBook(bookSpinner, biblePosition.getBook());
		fillDialogChapterSpinner(chapterSpinner, biblePosition.getBook());
		chapterSpinner.setSelection(biblePosition.getChapter() - 1);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoText((Book) bookSpinner.getSelectedItem(), Integer
						.parseInt((String) chapterSpinner.getSelectedItem()), 1);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void fillDialogChapterSpinner(IcsSpinner chapterSpinner, Book book) {
		int position = chapterSpinner.getSelectedItemPosition();
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < book.getChaptersSize(); i++) {
			list.add("" + (i + 1));
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		chapterSpinner.setAdapter(dataAdapter);
		if (position >= 0 && position < chapterSpinner.getCount()) {
			chapterSpinner.setSelection(position);
		}
	}

	private void setSpinnerBibleBook(IcsSpinner bookSpinner, Book book) {
		if (book != null && bookSpinner != null
				&& bookSpinner.getAdapter() instanceof BookArrayAdapter) {
			BookArrayAdapter adapter = (BookArrayAdapter) bookSpinner
					.getAdapter();
			for (int i = 0; i < adapter.getCount(); i++) {
				Book book2 = adapter.getItem(i);
				if (book2 != null && book.getId() == book2.getId()) {
					bookSpinner.setSelection(i);
					break;
				}
			}
		}
	}

	private void removeCurrentVerseMark() {
		if (selectedVerses.size() > 0) {
			boolean modified = false;
			for (Iterator iterator = selectedVerses.iterator(); iterator.hasNext();) {
				SelectedVerse selectedVerse = (SelectedVerse) iterator.next();
				String currentVerse = selectedVerse.id;
				IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
						.getInstance(getApplicationContext());
				BiblePosition biblePosition = pagerAdapter
						.getBiblePositionFromIndex(mViewPager.getCurrentItem());
				if (biblePosition != null && biblePosition.getBook() != null) {
					myBibleLocalServices.deleteAllHighlighterVerse(biblePosition
							.getBook().getBookNumber(), biblePosition.getChapter(),
							currentVerse);
					int position = mViewPager.getCurrentItem();
					pagerAdapter.unMarkVerse(
							pagerAdapter.getViewByPosition(mViewPager, position),
							currentVerse);
					Integer[] verseInformation = BibleUtilities
							.getVerseInformation(currentVerse);
					modified = true;
				}
			}
			if (modified) {				
				String message = getResources().getString(
						R.string.message_verse_unmarked_favorite, "");
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setCurrentVerseMark(Highlighter highlighter) {
		if (selectedVerses.size() > 0) {
			boolean modified = false;
			IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
					.getInstance(getApplicationContext());
			if (highlighter != null) {
				BiblePosition biblePosition = pagerAdapter
						.getBiblePositionFromIndex(mViewPager.getCurrentItem());
				if (biblePosition != null && biblePosition.getBook() != null) {
					for (Iterator iterator = selectedVerses.iterator(); iterator.hasNext();) {
						SelectedVerse selectedVerse = (SelectedVerse) iterator.next();
						String currentVerse = selectedVerse.id;
						String currentText = selectedVerse.text;
						Integer[] verseInformation = BibleUtilities
								.getVerseInformation(currentVerse);
						if (verseInformation != null && verseInformation.length > 0) {
							myBibleLocalServices.deleteAllHighlighterVerse(
									biblePosition.getBook().getBookNumber(),
									biblePosition.getChapter(), currentVerse);
							HighlighterVerseMark mark = new HighlighterVerseMark();
							mark.setConfig(highlighter);
							mark.setBook(biblePosition.getBook().getBookNumber());
							mark.setChapter(biblePosition.getChapter());
							mark.setVerseMark(currentVerse);
							mark.setVerseRangeLow(verseInformation[0]);
							mark.setVerseRangeHigh(verseInformation.length > 1 ? verseInformation[1]
									: verseInformation[0]);
							if (currentText != null) {
								String tempCurrentText = currentText.replaceAll(
										"\\<sup>.*?</sup>", "");
								tempCurrentText = tempCurrentText.replaceAll(
										"\\<.*?>", "");
								if (tempCurrentText.length() > 151) {
									tempCurrentText = tempCurrentText.substring(0,
											150) + "...";
								}
								mark.setExtract(tempCurrentText);
							} else {
								mark.setExtract("");
							}
							mark.setNote("");
							boolean inserted = myBibleLocalServices
									.insertHighlighterVerse(mark);
							if (inserted) {
								int position = mViewPager.getCurrentItem();
								pagerAdapter.markVerse(pagerAdapter
										.getViewByPosition(mViewPager, position),
										highlighter, currentVerse);
								modified = true;
							}
						}
					}
				}
			}
			if (modified) {
				String message = getResources().getString(
						R.string.message_verse_marked_favorite,
						"", highlighter.getName());
				Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class BookArrayAdapter extends ArrayAdapter<Book> {
		public BookArrayAdapter(Context context, int textViewResourceId,
				List<Book> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	private final class FavoriteMarksActionMode implements ActionMode.Callback {

		private static final int action_mark = 50001;
		private static final int action_unmark = 150002;

		private boolean isMarked;

		private List<Highlighter> highlighters;

		public FavoriteMarksActionMode(boolean isMarked,
				List<Highlighter> highlighters) {
			this.isMarked = isMarked;
			this.highlighters = highlighters;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if (highlighters == null || highlighters.size() == 0) {
				return false;
			}
			if (isMarked) {
				menu.add(Menu.NONE, action_unmark, Menu.NONE,
						R.string.action_clear_mark)
						.setIcon(R.drawable.ic_clear_normal)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
			int i = 0;
			for (Iterator iterator = highlighters.iterator(); iterator
					.hasNext();) {
				Highlighter highlighter = (Highlighter) iterator.next();
				i++;
				int iconRes;
				switch (i) {
				case 1:
					iconRes = R.drawable.marker_1;
					break;
				case 2:
					iconRes = R.drawable.marker_2;
					break;
				default:
					iconRes = R.drawable.marker_3;
					break;
				}
				menu.add(Menu.NONE, action_mark + highlighter.getId(),
						Menu.NONE, highlighter.getName()).setIcon(iconRes)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int itemId = item.getItemId();
			if (itemId == action_unmark) {
				removeCurrentVerseMark();
				sendEvent(CATEGORY_USES, ACTION_UNSET_VERSE_MARK);
			} else if (highlighters != null) {
				for (Iterator iterator = highlighters.iterator(); iterator
						.hasNext();) {
					Highlighter highlighter = (Highlighter) iterator.next();
					if (itemId == (action_mark + highlighter.getId())) {
						setCurrentVerseMark(highlighter);
						sendEvent(CATEGORY_USES, ACTION_SET_VERSE_MARK);						
					}
				}
			}
			lastModeWasClicked = true;
			cleanSelection();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (lastModeWasClicked) {
				cleanSelection(false);
			}
		}

	}
	
	private class SelectedVerse {
		String id;
		String text;
	}

}
