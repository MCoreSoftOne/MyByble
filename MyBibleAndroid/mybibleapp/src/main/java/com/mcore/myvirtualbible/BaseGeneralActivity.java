package com.mcore.myvirtualbible;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseGeneralActivity extends AppCompatActivity {


	ProgressDialog waitDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected String getScreenName() {
		return this.getClass().getSimpleName();
	}

	public void sendEvent(String category, String action) {
	}

	public void sendEvent(String category, String action, String label) {
	}

	public void sendEvent(String category, String action, String param, String value) {
	}
	
	protected void initWait(int title, int message) {
		stopWait();
		waitDialog = ProgressDialog.show(this, getText(title==0? R.string.wait_default_title:title), getText(message==0? R.string.wait_default_message:message), true);
	}
	
	protected void stopWait() {
		if (waitDialog != null) {
			waitDialog.dismiss();
			waitDialog = null;
		}
	}

}
