package com.mcore.myvirtualbible;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mcore.myvirtualbible.adapters.VerseMarksListAdapter;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class VerseListActivity extends BaseGeneralActivity {
	
	private TextView noItemsText;
	
	private VerseMarksListAdapter adapter;
	
	private ListView verseList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verse_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		noItemsText = (TextView) findViewById(R.id.textNoItems);
		IMyBibleLocalServices localServices = MyBibleLocalServices
				.getInstance(this);
		BibleTranslation currentVersion = localServices
				.getSelectedBibleTranslation();
		List<Book> allBooks = localServices.getAllBooks(currentVersion.getId());
		List<HighlighterVerseMark> marks = localServices
				.getAllHighlighterMarks();
		verseList = (ListView) findViewById(R.id.verse_list);
		adapter = new VerseMarksListAdapter(this, verseList, marks, allBooks);
		 
		verseList.setAdapter(adapter);
		
		verseList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				return false;
			}			
		});

		verseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object item = adapter.getItem(position);
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", (Serializable)item);
				setResult(RESULT_OK, returnIntent);
				finish();
			}

		});
		addContextMenuOnList();
		doChangeList();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void addContextMenuOnList() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {			
			final IMyBibleLocalServices localServices = MyBibleLocalServices.getInstance(this);
			verseList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			verseList.setMultiChoiceModeListener(new  AbsListView.MultiChoiceModeListener() {
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					adapter.notifyDataSetChanged();
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
					case R.id.action_delete_verse:
						List marks = new ArrayList();
						for (int i = 0; i < adapter.getCount(); i++) {
							if (verseList.isItemChecked(i)) {
								HighlighterVerseMark mark = adapter.getVerseMark(i);
								if (mark != null) {
									localServices.deleteHighlighterVerse(
											mark.getConfig().getId(), mark.getBook(), mark.getChapter(), mark.getVerseMark());
									marks.add(mark);
								}
							}
						}
						for (Iterator iterator2 = marks.iterator(); iterator2
								.hasNext();) {
							HighlighterVerseMark mark = (HighlighterVerseMark) iterator2.next();
							adapter.removeVerseMarkFromModel((HighlighterVerseMark) mark);							
						}
						adapter.notifyDataSetChanged();
						mode.finish();
						return true;
					default:
						return false;
					}
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.menu_context_verse_list, menu);
					return true;
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
			});
		}
	}
	
	private void doChangeList() {
		boolean hasData = adapter != null && adapter.getModel() != null && adapter.getModel().size() > 0;
		noItemsText.setVisibility(hasData? View.INVISIBLE: View.VISIBLE);		
	}
	
	public boolean onOptionsItemSelected(
			MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_filter_order:
			//VerseFilterOrderDialog dialog = new VerseFilterOrderDialog(this);
			//dialog.show();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_verse_list, menu);
		return super.onCreateOptionsMenu(menu);
	}*/

}
