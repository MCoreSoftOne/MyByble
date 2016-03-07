package com.mcore.myvirtualbible;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.dialog.DownloadDialog;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBibleConstants;
import com.mcore.myvirtualbible.util.MyBiblePreferences;
import com.mcore.myvirtualbible.util.SystemUiHider;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends BaseGeneralActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		TextView mybibletext = (TextView) findViewById(R.id.my_bible_text);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Qwigley-Regular.ttf");
		mybibletext.setTypeface(tf);

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
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

	private boolean continueCheckStorage(final MyBiblePreferences preferences) {
		boolean isSDPresent = BibleUtilities.isSDPresent() && BibleUtilities.isReadWriteStoragePermissionGranted(this);
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
		return true;
	}

	private boolean checkExternalStorage() {
		final MyBiblePreferences preferences = MyBiblePreferences
				.getInstance(getApplicationContext());
		if (preferences.getUseExternalStorage()) {
			if (BibleUtilities.verifyReadWriteStoragePermission(this, MyBibleConstants.PERMISSIONS_REQUEST_STORAGE__INIT_CHECK_EXTERNAL)) {
				return continueCheckStorage(preferences);
			} else {
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
		MyBiblePreferences preferences = MyBiblePreferences.getInstance(getApplicationContext());
		if (preferences.getUseExternalStorage() && !BibleUtilities.isReadWriteStoragePermissionGranted(this)) {
			return;
		}
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
			infoLoaded(MyBibleConstants.MYBIBLE_DEVELOPER_MODE ? 0 : 1500);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean isGranted = grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
		switch (requestCode) {
			case MyBibleConstants.PERMISSIONS_REQUEST_STORAGE__CHANGE_STORAGE_TYPE: {
				if (isGranted) {
					boolean isChecked = true;
					MyBiblePreferences preferences = MyBiblePreferences.getInstance(getApplicationContext());
					IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
							.getInstance(getApplicationContext());
					preferences.setUseExternalStorage(isChecked);
					myBibleLocalServices
							.setUseExternalStorage(isChecked);
					break;
				}
			}
			case MyBibleConstants.PERMISSIONS_REQUEST_STORAGE__INIT_CHECK_EXTERNAL: {
				MyBiblePreferences preferences = MyBiblePreferences.getInstance(getApplicationContext());
				continueCheckStorage(preferences);
				if (isGranted) {
					try {
						populateIfDont();
					} catch (Exception e) {
						Toast.makeText(this, R.string.sorry_initialization,
								Toast.LENGTH_LONG).show();
						finish();
					}
				}
			}
			default:
				break;
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
