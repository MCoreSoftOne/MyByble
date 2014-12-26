package com.mcore.myvirtualbible.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.R;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.listeners.IVerseSelectionListener;
import com.mcore.myvirtualbible.model.BiblePosition;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.model.Book;
import com.mcore.myvirtualbible.model.Highlighter;
import com.mcore.myvirtualbible.model.HighlighterVerse;
import com.mcore.myvirtualbible.util.BibleHtmlTransform;
import com.mcore.myvirtualbible.util.MyBiblePreferences;

/**
 * Adaptador para el visor de imagenes de tama�o completo, carga las imágenes
 * de forma asíncrona.
 * 
 * @author Mario
 * 
 */
public class ViewPagerAdapter extends PagerAdapter {

	private Context ctx;

	private static String APP_TAG = "MY_BIBLE";

	private List<Book> books;

	private BibleTranslation version;

	private MyBiblePreferences preferences;
	
	private IVerseSelectionListener listener;

	/**
	 * Constructor del adaptador, inicializa el modelo.
	 * 
	 * @param ctx
	 *            Contexto de la aplicaci�n.
	 * @param group
	 *            Grupo actual que contiene la lista de imagenes a mostrar.
	 * @param loader
	 *            Objeto cargador de las im�genes del grupo. Debe implementar
	 *            la interface IImageLoader.
	 */
	public ViewPagerAdapter(Context ctx, BibleTranslation version,
			List<Book> books, MyBiblePreferences preferences, IVerseSelectionListener listener) {
		Log.d(APP_TAG, "Inicializando ViewPagerAdapter");
		this.books = books;
		this.ctx = ctx;
		this.version = version;
		this.preferences = preferences;
		this.listener = listener;
	}

	public void changeTranslation(BibleTranslation version, List<Book> books) {
		this.books = books;
		this.version = version;
	}

	/**
	 * Cantidad de elementos en el modelo.
	 */
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
			for (Iterator iterator = books.iterator(); iterator.hasNext();) {
				Book book = (Book) iterator.next();
				if (book != null && book.getId() == position.getBook().getId()) {
					result += position.getChapter() - 1;
					break;
				}
				result += book.getChaptersSize();
			}
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

	/**
	 * Instancia una nueva vista con la imagen que se debe mostrar en la
	 * posición indicada. La vista creada es de tipo ImageViewTouch.
	 */
	public Object instantiateItem(View collection, int position) {
		Log.d(APP_TAG, "Creando nueva vista en posici�n " + position);
		LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
		View row = inflater.inflate(R.layout.items_chapter, null);
		row.setTag("view" + position);
		WebView view = (WebView) row.findViewById(R.id.text_chapter);
		view.getSettings().setJavaScriptEnabled(true);
		BiblePosition biblePosition = getBiblePositionFromIndex(position);
		if (biblePosition != null && biblePosition.getBook() != null) {
			view.addJavascriptInterface(new JavaScriptInterface(ctx),
					"mybibleinternal");
			fillWebView(row, view, biblePosition, position);
		}
		
		((ViewPager) collection).addView(row, 0);
		return row;
	}
	
	public View getViewByPosition(ViewPager pager, int position) {
		if (pager != null) {
			return pager.findViewWithTag("view" + position);
		}
		return null;
	}
	
	public int getScrollPosByPosition(ViewPager pager, int position) {
		View view = getViewByPosition(pager, position);
		View scroll = view.findViewById(R.id.scrollWebContent);
		if (scroll != null) {
			return scroll.getScrollY();
		}
		return 0;
	}
	
	
	
	//JSMETHODS
	
	public void unMarkVerse(View row, String verse) {
		if (row != null) {			
			WebView webView = (WebView) row.findViewById(R.id.text_chapter);
			if (webView != null) {
				runJS(webView, "markVerse('"+verse+"','')");
			}
		}
	}
	
	public void markVerse(View row, Highlighter highlighter, String verse) {
		if (row != null) {			
			WebView webView = (WebView) row.findViewById(R.id.text_chapter);
			if (webView != null) {
				runJS(webView, "markVerse('"+verse+"','"+highlighter.getHighlightClassName()+"')");
			}
		}
	}
	
	public void cleanSelection(View row) {
		if (row != null) {			
			WebView webView = (WebView) row.findViewById(R.id.text_chapter);
			if (webView != null) {
				runJS(webView, "cleanSelection()");
			}
		}
	}
	
	public void runJS(final WebView webView, final String scriptSrc) { 
        webView.post(new Runnable() {
            @Override
            public void run() { 
                webView.loadUrl("javascript:" + scriptSrc); 
            }
        }); 
    }
	
	//----

	private void fillWebView(final View row, WebView view, BiblePosition biblePosition, final int position) {
		IMyBibleLocalServices instance = MyBibleLocalServices.getInstance(ctx);
		Map params = new HashMap();
		putVersionData(params);
		List<HighlighterVerse> highlighterList = instance.getHighlighterMarksByBookChapter(biblePosition.getBook().getBookNumber(), biblePosition.getChapter());
		Map highlighterMap = new HashMap();
		for (Iterator iterator = highlighterList.iterator(); iterator.hasNext();) {
			HighlighterVerse highlighterVerse = (HighlighterVerse) iterator
					.next();
			if (highlighterVerse != null && highlighterVerse.getConfig() != null && highlighterVerse.getVerseMark() != null) {
				highlighterMap.put(highlighterVerse.getVerseMark(), highlighterVerse.getConfig());
			}
		}
		params.put(
				"bodystyle",
				"text-align:justify;font-size:"
						+ preferences.getTextSize()
						+ ";color:#"
						+ intToHexColor(preferences.getTextColor())
						+ ";background-color:#"
						+ intToHexColor(preferences.getBackgroundColor()));
		params.put("highlighterMap", highlighterMap);
		params.put("highlighters", instance.getHighlighters());
		String converData = instance.getBookChapterText(
				biblePosition.getBook().getId(),
				biblePosition.getChapter());
		saveToDeveloperFile(biblePosition.getBook().getName() + "_" + biblePosition.getChapter() + ".xml", converData);
		String toHTML = BibleHtmlTransform.getInstance().convert(
				converData, params);
		// String toHTML =
		// XmlTransform.transformChapterToHTML(ctx,MyBibleLocalServices.getInstance(ctx).getBookChapterText(biblePosition.getBook().getId(),
		// biblePosition.getChapter()),params);
		view.loadDataWithBaseURL("file:///android_asset/.", toHTML,
				"text/html", "ISO-8859-1", null);
		
		view.setWebChromeClient(new WebChromeClient() {
			  public void onConsoleMessage(String message, int lineNumber, String sourceID) {
				    Log.d("CONSOLE", message + " -- From line "
				                         + lineNumber + " of "
				                         + sourceID);
				  }
				});
		saveToDeveloperFile(biblePosition.getBook().getName() + "_" + biblePosition.getChapter() + ".html", toHTML);
	}
	
	private void saveToDeveloperFile(String fileName, String fileContent) {
		if (CommonConstants.MYBIBLE_DEVELOPER_MODE) {			
			try {
				File myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/mybible/mybbl_" + fileName);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
				outputStreamWriter.write(fileContent);
				outputStreamWriter.close();
				MediaScannerConnection.scanFile(ctx, new String[] { myFile.getAbsolutePath() }, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
	}
	
	private String intToHexColor(int intColor) {
		return String.format("%06X", (0xFFFFFF & intColor));
	}
	
	private String putVersionData(Map params) {
		if (params != null && version != null && version.getCopyright() != null) {
			String copyright = version.getCopyright();
			if (copyright != null) {
				String[] split = copyright.split("\\\\n");
				params.put("copyright", split[0]);
				if (split.length > 1) {
					params.put("copyright2", split[1]);
				}
			}
			return copyright;
		}
		return "Error !!!";
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	final Handler myHandler = new Handler();

	public class JavaScriptInterface {
		Context mContext;

		JavaScriptInterface(Context c) {
			mContext = c;
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

}
