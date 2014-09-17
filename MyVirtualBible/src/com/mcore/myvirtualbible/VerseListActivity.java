package com.mcore.myvirtualbible;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.mcore.myvirtualbible.adapters.VerseMarksListAdapter;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.HighlighterVerseMark;

public class VerseListActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verse_list);

		IMyBibleLocalServices localServices = MyBibleLocalServices
				.getInstance(this);
		BibleTranslation currentVersion = localServices
				.getSelectedBibleTranslation();
		List<Book> allBooks = localServices.getAllBooks(currentVersion.getId());
		List<HighlighterVerseMark> marks = localServices
				.getAllHighlighterMarks();
		final VerseMarksListAdapter adapter = new VerseMarksListAdapter(this,
				marks, allBooks);

		ListView verseList = (ListView) findViewById(R.id.verse_list);
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
	}
	
	/*
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_filter_order:
			VerseFilterOrderDialog dialog = new VerseFilterOrderDialog(this);
			dialog.show();
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_verse_list, menu);
		return super.onCreateOptionsMenu(menu);
	}*/

}
