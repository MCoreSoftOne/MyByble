package com.mcore.myvirtualbible.adapters;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mcore.myvirtualbible.R;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;


public class VerseMarksListAdapter extends BaseAdapter {

	private List<HighlighterVerseMark> model;

	protected Context ctx;
	
	protected List<Book> books;
	
	private ListView parentList;

	public VerseMarksListAdapter(Context ctx, ListView parentList, List<HighlighterVerseMark> model, List<Book> books) {
		this.model = model;
		this.ctx = ctx;
		this.books = books;
		this.parentList = parentList;
	}
	
	public List<HighlighterVerseMark> getModel() {
		return model;
	}

	@Override
	public int getCount() {
		return model != null? model.size(): 0;
	}

	@Override
	public Object getItem(int position) {
		return getVerseMark(position);
	}
	
	public HighlighterVerseMark getVerseMark(int position) {
		if (position >= 0 && position < model.size()) {
			return model.get(position);
		}
		return null;
	}
	
	public void removeVerseMarkFromModel(HighlighterVerseMark mark) {
		if (model != null && mark != null) {
			model.remove(mark);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		VerseHolder holder = null;
		HighlighterVerseMark item = (HighlighterVerseMark) getItem(position);
		if (row != null) {
			holder = (VerseHolder) row.getTag();			
		}
		if (row == null || holder.needInflate) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			row = inflater.inflate(R.layout.item_marker, parent, false);
			holder = new VerseHolder();
			holder.markerImage = (ImageView) row.findViewById(R.id.marker_image);
			holder.textTitleMarker = (TextView) row.findViewById(R.id.text_title_marker);
			holder.textVerseMarker = (TextView) row.findViewById(R.id.text_verse_marker);
			holder.deleteImage = (ImageView) row.findViewById(R.id.trash_image_btn);
			row.setTag(holder);
		}
		final View view = row;
		OnClickListener removeSubcriptorListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteCell(view, position);
			}
		};
		holder.deleteImage.setOnClickListener(removeSubcriptorListener);
		
		holder.textTitleMarker.setText(extractVerseTitle(item));
		holder.textVerseMarker.setText(item.getExtract());
		if (parentList.isItemChecked(position)) {
			row.setBackgroundColor(Color.parseColor("#0099CC"));
		} else {
			row.setBackgroundColor(Color.TRANSPARENT);
		}
		int iconRes;
		switch (item.getConfig().getId()) {
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
		holder.markerImage.setImageResource(iconRes);
		return row;
	}
	
	private String extractVerseTitle(HighlighterVerseMark item) {
		return getBookName(item.getBook()) + " " + item.getChapter() + ":"+ 
				item.getVerseRangeLow() + (item.getVerseRangeLow() != item.getVerseRangeHigh()? "-"+item.getVerseRangeHigh():"") ;
	}
	
	private String getBookName(int bookId) {
		if (books != null) {
			for (Iterator iterator = books.iterator(); iterator.hasNext();) {
				Book book = (Book) iterator.next();
				if (book != null && book.getBookNumber() == bookId) {
					return book.getName();
				}
			}
		}
		return "Book " + bookId;
	}

	public void setModel(List<HighlighterVerseMark> model) {
		this.model = model;
		notifyDataSetChanged();
	}
	
	private void deleteCell(final View v, final int index) {
		AnimationListener al = new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				model.remove(index);
				VerseHolder vh = (VerseHolder)v.getTag();
				vh.needInflate = true;
				notifyDataSetChanged();
			}
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override public void onAnimationStart(Animation animation) {}
		};

		collapse(v, al);
	}
	
	private void collapse(final View v, AnimationListener al) {
		final int initialHeight = v.getMeasuredHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				}
				else {
					v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		if (al!=null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(200);
		v.startAnimation(anim);
	}
	
	private static class VerseHolder {
		boolean needInflate;
		ImageView markerImage;
		TextView textTitleMarker;
		TextView textVerseMarker;
		ImageView deleteImage;
	}

}
