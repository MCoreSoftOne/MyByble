package com.mcore.myvirtualbible.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.mcore.myvirtualbible.R;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.listeners.IVerseSelectionListener;
import com.mcore.myvirtualbible.model.BiblePosition;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.util.BibleHtmlTransform;
import com.mcore.myvirtualbible.util.MyBibleConstants;
import com.mcore.myvirtualbible.util.MyBiblePreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChapterSlidePageFragment extends Fragment implements ObservableScrollViewCallbacks {
	/**
	 * The argument key for the page number this fragment represents.
	 */
	public static final String BIBLE_POSITION_KEY = "biblePos";

	private BiblePosition biblePosition;

	private ObservableWebView webview;

	private MyReciever asynReceiver;

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the
	 * given page number.
	 */
	public static ChapterSlidePageFragment create(BiblePosition position) {
		ChapterSlidePageFragment fragment = new ChapterSlidePageFragment();

		Bundle args = new Bundle();
		args.putSerializable(BIBLE_POSITION_KEY, position);
		fragment.setArguments(args);
		return fragment;
	}

	public ChapterSlidePageFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		biblePosition = (BiblePosition) getArguments().getSerializable(
				BIBLE_POSITION_KEY);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout containing a title and body text.
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.items_chapter, container, false);

		// Set the title view to show the page number.
		webview = ((ObservableWebView) rootView.findViewById(R.id.text_chapter));
		enableJavaScript(webview);
		webview.setScrollViewCallbacks(this);
		if (biblePosition != null && biblePosition.getBook() != null) {
			webview.addJavascriptInterface(new JavaScriptInterface(),
					"mybibleinternal");
			fillWebView(webview, biblePosition);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(MyBibleConstants.MESSAGE_ACTION_CLEAR_ALL);
		filter.addAction(MyBibleConstants.MESSAGE_ACTION_MARK_VERSE);
		filter.addAction(MyBibleConstants.MESSAGE_ACTION_UNMARK_VERSE);
		filter.addAction(MyBibleConstants.MESSAGE_ACTION_JUMP_TO_VERSE);
		asynReceiver = new MyReciever();
		getActivity().registerReceiver(asynReceiver, filter);
		return rootView;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void enableJavaScript(WebView webview) {
		webview.getSettings().setJavaScriptEnabled(true);
	}

	private void fillWebView(WebView view, BiblePosition biblePosition) {
		Context ctx = getActivity();
		MyBiblePreferences preferences = MyBiblePreferences.getInstance(ctx
				.getApplicationContext());
		IMyBibleLocalServices instance = MyBibleLocalServices.getInstance(ctx);
		Map params = new HashMap();
		putVersionData(params);
		List<HighlighterVerse> highlighterList = instance
				.getHighlighterMarksByBookChapter(biblePosition.getBook()
						.getBookNumber(), biblePosition.getChapter());
		Map highlighterMap = new HashMap();
		for (Iterator iterator = highlighterList.iterator(); iterator.hasNext();) {
			HighlighterVerse highlighterVerse = (HighlighterVerse) iterator
					.next();
			if (highlighterVerse != null
					&& highlighterVerse.getConfig() != null
					&& highlighterVerse.getVerseMark() != null) {
				highlighterMap.put(highlighterVerse.getVerseMark(),
						highlighterVerse.getConfig());
			}
		}
		params.put(
				"bodystyle",
				"text-align:justify;font-size:" + preferences.getTextSize()
						+ ";color:#"
						+ intToHexColor(preferences.getTextColor())
						+ ";background-color:#"
						+ intToHexColor(preferences.getBackgroundColor()));
		params.put("highlighterMap", highlighterMap);
		params.put("highlighters", instance.getHighlighters());
		String converData = instance.getBookChapterText(biblePosition.getBook()
				.getId(), biblePosition.getChapter());
		saveToDeveloperFile(biblePosition.getBook().getName() + "_"
				+ biblePosition.getChapter() + ".xml", converData);
		String toHTML = BibleHtmlTransform.getInstance().convert(converData,
				params);
		view.loadDataWithBaseURL("file:///android_asset/.", toHTML,
				"text/html", "ISO-8859-1", null);

		view.setWebChromeClient(new WebChromeClient() {
			public void onConsoleMessage(String message, int lineNumber,
					String sourceID) {
				Log.d("CONSOLE", message + " -- From line " + lineNumber
						+ " of " + sourceID);
			}
		});
		saveToDeveloperFile(biblePosition.getBook().getName() + "_"
				+ biblePosition.getChapter() + ".html", toHTML);
	}

	private String intToHexColor(int intColor) {
		return String.format("%06X", (0xFFFFFF & intColor));
	}

	private BibleTranslation getSelectedBibleVersion() {
		IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
				.getInstance(getActivity().getApplicationContext());
		BibleTranslation currentTranslation = myBibleLocalServices
				.getSelectedBibleTranslation();
		return currentTranslation;
	}

	private void putVersionData(Map params) {
		BibleTranslation version = getSelectedBibleVersion();
		if (params != null && version != null && version.getCopyright() != null) {
			String copyright = version.getCopyright();
			if (copyright != null) {
				String[] split = copyright.split("\\\\n");
				params.put("copyright", split[0]);
				if (split.length > 1) {
					params.put("copyright2", split[1]);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void saveToDeveloperFile(String fileName, String fileContent) {
		if (MyBibleConstants.MYBIBLE_DEVELOPER_MODE_SAVE_DEBUG_DATA
				&& MyBibleConstants.MYBIBLE_DEVELOPER_MODE) {
			try {
				File myFile = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/mybible/mybbl_" + fileName);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						fOut);
				outputStreamWriter.write(fileContent);
				outputStreamWriter.close();
				MediaScannerConnection.scanFile(getActivity(),
						new String[] { myFile.getAbsolutePath() }, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroyView() {
		getActivity().unregisterReceiver(asynReceiver);
		super.onDestroyView();
	}

	private void jumpToVerse(String verse) {
		if (webview != null && verse != null) {
			runJS(webview, "jumpToVerse('" + verse + "')");
		}
	}

	private void unMarkVerse(String verse) {
		if (webview != null && verse != null) {
			runJS(webview, "markVerse('" + verse + "','')");
		}
	}

	private void markVerse(String highlighterclass, String verse) {
		if (webview != null && verse != null) {
			runJS(webview, "markVerse('" + verse + "','" + highlighterclass
					+ "')");
		}
	}

	private void cleanSelection() {
		if (webview != null) {
			runJS(webview, "cleanSelection()");
		}
	}

	private void runJS(final WebView webView, final String scriptSrc) {
		webView.post(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl("javascript:" + scriptSrc);
			}
		});
	}

	// ----

	private class MyReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction() != null) {
				Serializable position = intent
						.getSerializableExtra(MyBibleConstants.MESSAGE_PARAM_PAGE_POSITION);
				if (!(position instanceof Integer[]) && (position instanceof Object[])) {
					Object[] objList = (Object[]) position;
					List<Integer> list = new ArrayList<>();
					if (objList.length > 0) {
						for (int i = 0; i < objList.length; i++) {
							Object o = objList[i];
							if (o instanceof Integer) {
								list.add((Integer) o);
							} else {
								list.add(null);
							}
						}
					}
					position = list.toArray(new Integer[list.size()]);
				}
				if (position instanceof Integer[]) {
					Integer[] posData = (Integer[]) position;
					if (biblePosition != null && biblePosition.getBook() != null
							&& posData.length >= 2 && posData[0] != null
							&& posData[1] != null && posData[0].intValue() == biblePosition.getBook().getId()
							&& posData[1].intValue() == biblePosition.getChapter()) {
						if (intent.getAction().equals(
								MyBibleConstants.MESSAGE_ACTION_CLEAR_ALL)) {
							cleanSelection();
						} else if (intent.getAction().equals(
								MyBibleConstants.MESSAGE_ACTION_MARK_VERSE)) {
							markVerse(
									intent.getStringExtra(MyBibleConstants.MESSAGE_PARAM_HTMLCLASSNAME),
									intent.getStringExtra(MyBibleConstants.MESSAGE_PARAM_VERSE));
						} else if (intent.getAction().equals(
								MyBibleConstants.MESSAGE_ACTION_UNMARK_VERSE)) {
							unMarkVerse(intent
									.getStringExtra(MyBibleConstants.MESSAGE_PARAM_VERSE));
						} else if (intent.getAction().equals(
								MyBibleConstants.MESSAGE_ACTION_JUMP_TO_VERSE)) {
							jumpToVerse(intent
									.getStringExtra(MyBibleConstants.MESSAGE_PARAM_VERSE));
						}
					}
				}
			}
		}
	}

	final Handler myHandler = new Handler();

	private class JavaScriptInterface {

		private IVerseSelectionListener listener;

		protected JavaScriptInterface() {
			FragmentActivity activity = getActivity();
			if (activity instanceof IVerseSelectionListener) {
				listener = (IVerseSelectionListener) activity;
			}
		}

		@JavascriptInterface
		public void selectVerse(final String verse, final String text) {
			if (listener != null) {
				myHandler.post(new Runnable() {
					@Override
					public void run() {
						listener.selectVerse(verse, text);
					}
				});
			}

		}

		@JavascriptInterface
		public void unSelectVerse(final String verse) {
			if (listener != null) {
				myHandler.post(new Runnable() {
					@Override
					public void run() {
						listener.unSelectVerse(verse);
					}
				});
			}

		}
	}


	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll,
								boolean dragging) {
	}

	@Override
	public void onDownMotionEvent() {
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		Activity activity = getActivity();
		if (activity instanceof AppCompatActivity) {
			ActionBar ab = ((AppCompatActivity)activity).getSupportActionBar();
			if (scrollState == ScrollState.UP) {
				if (ab.isShowing()) {
					ab.hide();
				}
			} else if (scrollState == ScrollState.DOWN) {
				if (!ab.isShowing()) {
					ab.show();
				}
			}
		}
	}
}
