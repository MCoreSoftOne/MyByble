package com.mcore.myvirtualbible.dialog;

import com.mcore.myvirtualbible.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class VerseFilterOrderDialog extends Dialog {

	public VerseFilterOrderDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.dialog_verse_filter_order);
		super.onCreate(savedInstanceState);
	}
	
	
}
