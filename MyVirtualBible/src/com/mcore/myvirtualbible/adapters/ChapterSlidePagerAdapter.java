package com.mcore.myvirtualbible.adapters;

import java.util.Iterator;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.mcore.myvirtualbible.fragment.ChapterSlidePageFragment;
import com.mcore.myvirtualbible.model.BiblePosition;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.util.MyBibleConstants;

public class ChapterSlidePagerAdapter extends FragmentStatePagerAdapter {
	
	private List<Book> books;

	public ChapterSlidePagerAdapter(FragmentManager fm,
			List<Book> books) {
		super(fm);
		Log.d(MyBibleConstants.APP_TAG, "Inicializando ChapterSlidePagerAdapter");
		this.books = books;
	}

	@Override
	public Fragment getItem(int position) {
		return ChapterSlidePageFragment.create(getBiblePositionFromIndex(position));
	}

	@Override
	public int getCount() {
		int result = 0;
		if (books != null) {
			for (Iterator iterator = books.iterator(); iterator.hasNext();) {
				Book book = (Book) iterator.next();
				if (book != null) {
					result += book.getChaptersSize();
				}

			}
		}
		return result;
	}
	
	public int getPositionFromBiblePosition(BiblePosition position) {
		int result = 0;
		if (position != null && position.getBook() != null) {
			result = getPositionFromBiblePosition(position.getBook().getId(), position.getChapter());
		}
		return result;
	}
	
	public int getPositionFromBiblePosition(int pbook, int pchapter) {
		int result = 0;
		for (Iterator iterator = books.iterator(); iterator.hasNext();) {
			Book book = (Book) iterator.next();
			if (book != null && book.getId() == pbook) {
				result += pchapter - 1;
				break;
			}
			result += book.getChaptersSize();
		}
		return result;
	}
	
	public BiblePosition getBiblePositionFromIndex(int index) {
		BiblePosition result = new BiblePosition();
		if (books != null) {
			int currentPos = 0;
			for (Iterator iterator = books.iterator(); iterator.hasNext();) {
				Book book = (Book) iterator.next();
				if (index < currentPos + book.getChaptersSize()) {
					result.setBook(book);
					result.setChapter((index - currentPos) + 1);
					break;
				}
				if (book != null) {
					currentPos += book.getChaptersSize();
				}
			}
			if (result.getBook() == null) {
				result.setBook(books.get(0));
				result.setChapter(1);
			}
		}
		return result;
	}
	
	@Override
	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
	public void changeTranslation(List<Book> books) {
		this.books = books;
	}

}
