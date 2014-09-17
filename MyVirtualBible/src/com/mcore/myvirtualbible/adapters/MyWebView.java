package com.mcore.myvirtualbible.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MyWebView extends WebView {

	public MyWebView(Context context) {
		super(context);

	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			int temp_ScrollY = getScrollY();
			scrollTo(getScrollX(), getScrollY() + 1);
			scrollTo(getScrollX(), temp_ScrollY);

		}

		return super.onTouchEvent(event);
	}

}
