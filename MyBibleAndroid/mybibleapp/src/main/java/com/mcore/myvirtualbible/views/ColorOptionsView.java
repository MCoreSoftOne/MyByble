package com.mcore.myvirtualbible.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mcore.myvirtualbible.R;

public class ColorOptionsView extends LinearLayout {

	private ImageButton mImage;

	private TextView title;
	
	public ColorOptionsView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ColorOptionsView, 0, 0);
		String titleText = a.getString(R.styleable.ColorOptionsView_titleText);
		int valueColor = a.getColor(R.styleable.ColorOptionsView_valueColor,
				android.R.color.white);
		a.recycle();

		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_color_options, this, true);

		title = (TextView) getChildAt(0);
		title.setText(titleText);

		mImage = (ImageButton) getChildAt(1);
		mImage.setBackgroundColor(valueColor);
	}
	
	public void setOnClickImageListener(OnClickListener l) {
		mImage.setOnClickListener(l);
		title.setOnClickListener(l);
		this.setOnClickListener(l);
		this.setClickable(true);
	}

	public ColorOptionsView(Context context) {
		this(context, null);
	}

	public void setValueColor(int color) {
		mImage.setBackgroundColor(color);
	}

	public void setImageVisible(boolean visible) {
		mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

}