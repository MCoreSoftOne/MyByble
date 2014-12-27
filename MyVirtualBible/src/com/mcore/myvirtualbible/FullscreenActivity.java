package com.mcore.myvirtualbible;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.dialog.DownloadDialog;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBibleConstants;
import com.mcore.myvirtualbible.util.MyBiblePreferences;
import com.mcore.myvirtualbible.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends BaseGeneralActivity {

	private static final boolean AUTO_HIDE = true;

	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

	private static final boolean TOGGLE_ON_CLICK = true;

	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		final View contentView = findViewById(R.id.fullscreen_content);
		TextView mybibletext = (TextView) findViewById(R.id.my_bible_text);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Qwigley-Regular.ttf");
		mybibletext.setTypeface(tf);
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (visible && AUTO_HIDE) {
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
		if (checkExternalStorage()) {
			try {
				populateIfDont();
			} catch (Exception e) {
				Toast.makeText(this, R.string.sorry_initialization,
						Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	private boolean checkExternalStorage() {
		final MyBiblePreferences preferences = MyBiblePreferences
				.getInstance(getApplicationContext());
		if (preferences.getUseExternalStorage()) {
			boolean isSDPresent = BibleUtilities.isSDPresent();
			if (!isSDPresent) {

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							preferences.setUseExternalStorage(false);
							populateIfDont();
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							finish();
							break;
						}
					}
				};

				new AlertDialog.Builder(this)
						.setTitle(R.string.no_sd_available_title)
						.setMessage(R.string.no_sd_available_message_ask)
						.setPositiveButton(android.R.string.ok,
								dialogClickListener)
						.setNegativeButton(android.R.string.cancel,
								dialogClickListener).show();
				return false;
			}
		}
		return true;
	}

	private void setLoadingMode(boolean loading) {
		ProgressBar prgBar = (ProgressBar) findViewById(R.id.prgb_loading);
		TextView txtLoading = (TextView) findViewById(R.id.txt_loading_text);
		if (loading) {
			txtLoading.setText(R.string.loading);
			prgBar.setVisibility(View.VISIBLE);
			txtLoading.setVisibility(View.VISIBLE);
		} else {
			prgBar.setVisibility(View.INVISIBLE);
			txtLoading.setVisibility(View.INVISIBLE);
		}
	}

	private void populateIfDont() {
		IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
				.getInstance(getApplicationContext());
		if (myBibleLocalServices.hasToMigrateDatabase()) {
			AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
			        FullscreenActivity.this);
			alertDialog2.setTitle(R.string.wait_default_title);
			alertDialog2.setMessage("Migrate?");
			alertDialog2.setIcon(R.drawable.ic_data);
			alertDialog2.setPositiveButton(android.R.string.ok,
			        new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	new MigrateTask().execute();
			            }
			        });
			alertDialog2.setNegativeButton(android.R.string.no,
			        new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			                finish();
			            }
			        });
			
			alertDialog2.setCancelable(false).show();
			return;
		}
		boolean wasPopulated = myBibleLocalServices.databaseContainsData();
		if (!wasPopulated) {
			setLoadingMode(true);
			DownloadDialog dialog = new DownloadDialog(this) {
				protected void onOk(TranslationDTO data) {
					infoLoaded(0);
					DownloadDialog.sendDownloadEvent(FullscreenActivity.this,
							data);
				};

				protected void onCancel() {
					finish();
				};
			};
			dialog.allowExternalStorageOption();
			dialog.show();
		} else {
			infoLoaded(CommonConstants.MYBIBLE_DEVELOPER_MODE ? 0 : 1500);
		}
	}

	private void infoLoaded(final int time) {
		if (time == 0) {
			doFinishMe();
		} else {
			Thread t = new Thread() {
				public void run() {
					try {
						sleep(time);
						doFinishMe();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
	}

	private final void doFinishMe() {
		finish();
		Intent cv = new Intent(FullscreenActivity.this, ShowBookActivity.class);
		startActivity(cv);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		delayedHide(100);
	}

	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private void verifyAndContinue(int result) {
		if (result != MyBibleConstants.MIGRATION_NO_ERROR) {
			Toast.makeText(this, "Se ha producido un error", Toast.LENGTH_SHORT).show();
		}
		final IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
				.getInstance(getApplicationContext());
		if (myBibleLocalServices.hasToMigrateDatabase()) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						myBibleLocalServices.cleanTranslationDatabase();
						populateIfDont();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						new MigrateTask().execute();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						finish();
						break;
					}
				}
			};
			new AlertDialog.Builder(this)
					.setTitle(R.string.wait_default_title)
					.setMessage("No se ha podido migrar...")
					.setPositiveButton(android.R.string.ok,
							dialogClickListener)
					.setNeutralButton(R.string.button_retry_text, dialogClickListener)
					.setNegativeButton(android.R.string.cancel,
							dialogClickListener).
							setCancelable(false).show();
			return;
		} else {			
			populateIfDont();
		}
	}

	private class MigrateTask extends
			AsyncTask<Void, Void, Integer> {
		
		@Override
		protected void onPreExecute() {
			initWait(0, 0);
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
					.getInstance(getApplicationContext()); 
			return myBibleLocalServices.migrateDatabase();
		}

		@Override
		protected void onPostExecute(Integer result) {
			stopWait();
			verifyAndContinue(result);
		}

	}

}
