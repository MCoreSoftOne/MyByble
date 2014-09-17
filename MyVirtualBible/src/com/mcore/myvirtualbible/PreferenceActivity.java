package com.mcore.myvirtualbible;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.actionbarsherlock.view.Menu;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.dialog.DownloadDialog;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBiblePreferences;
import com.mcore.myvirtualbible.views.ColorOptionsView;

public class PreferenceActivity extends SherlockActivity {

	private int currentTranslation;

	private MyBiblePreferences preferences;

	private TextView txtServer;

	private ListView listInstalled;

	private OnCheckedChangeListener chkUseExternalStorageListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		preferences = MyBiblePreferences.getInstance(this
				.getApplicationContext());
		currentTranslation = preferences.getCurrentTranslation();
		reloadTranslationList();
		listInstalled = (ListView) findViewById(R.id.list_installed);
		registerForContextMenu(listInstalled);

		final IcsSpinner spinnerFont = (IcsSpinner) findViewById(R.id.spinner_font_size);
		ArrayAdapter<String> adapterFont = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.font_size_array));
		spinnerFont.setAdapter(adapterFont);
		spinnerFont.setOnItemSelectedListener(new IcsAdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(IcsAdapterView<?> parent, View view,
					int position, long id) {
				String[] values = getResources().getStringArray(
						R.array.font_size_array_value);
				if (values != null && position >= 0 && position < values.length) {
					preferences.setTextSize(values[position]);
				}				
			}

			@Override
			public void onNothingSelected(IcsAdapterView<?> parent) {
				
			}
		});

		final ColorOptionsView fontColor = (ColorOptionsView) findViewById(R.id.color_options_fontcolor);
		fontColor.setOnClickImageListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(
						PreferenceActivity.this, preferences.getTextColor(),
						new AmbilWarnaDialog.OnAmbilWarnaListener() {
							@Override
							public void onOk(AmbilWarnaDialog dialog, int color) {
								fontColor.setValueColor(color);
								preferences.setTextColor(color);
							}

							@Override
							public void onCancel(AmbilWarnaDialog dialog) {

							}

						});
				dialog.show();
			}
		});

		final ColorOptionsView backgroundColor = (ColorOptionsView) findViewById(R.id.color_options_background);
		backgroundColor.setOnClickImageListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(
						PreferenceActivity.this, preferences
								.getBackgroundColor(),
						new AmbilWarnaDialog.OnAmbilWarnaListener() {
							@Override
							public void onOk(AmbilWarnaDialog dialog, int color) {
								backgroundColor.setValueColor(color);
								preferences.setBackgroundColor(color);
							}

							@Override
							public void onCancel(AmbilWarnaDialog dialog) {

							}

						});
				dialog.show();
			}
		});
		setPreferencesValues();
		OnClickListener listener = getServerClickListener();
		txtServer = (TextView) findViewById(R.id.editServerAlias);
		updateServerTextFromPreferences();
		txtServer.setOnClickListener(listener);
		TextView txtServerName = (TextView) findViewById(R.id.textServerName);
		txtServerName.setOnClickListener(listener);
		chkUseExternalStorageListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				changeStorageOption(isChecked);
			}

		};
		CheckBox chkUseExternalStorage = (CheckBox) findViewById(R.id.chkUseExternalStorage);
		chkUseExternalStorage.setChecked(preferences.getUseExternalStorage());
		chkUseExternalStorage
				.setOnCheckedChangeListener(chkUseExternalStorageListener);
	}

	private void changeStorageOption(final boolean external) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					preferences.setUseExternalStorage(external);
					MyBibleLocalServices.getInstance(getApplicationContext())
							.setUseExternalStorage(external);
					setCurrentBibleTranslation();
					reloadTranslationList();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					CheckBox chkUseExternalStorage = (CheckBox) findViewById(R.id.chkUseExternalStorage);
					chkUseExternalStorage.setOnCheckedChangeListener(null);
					chkUseExternalStorage.setChecked(!external);
					chkUseExternalStorage.setOnCheckedChangeListener(chkUseExternalStorageListener);
					break;
				}
			}
		};
		if (external) {
			boolean isSDPresent = BibleUtilities.isSDPresent();
			if (!isSDPresent) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.no_sd_available_title)
						.setMessage(R.string.no_sd_available_message)
						.setPositiveButton(android.R.string.ok, null).show();
				CheckBox chkUseExternalStorage = (CheckBox) findViewById(R.id.chkUseExternalStorage);
				chkUseExternalStorage.setChecked(false);
				return;
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.use_external_storage_warning)
				.setPositiveButton(android.R.string.ok, dialogClickListener)
				.setNegativeButton(android.R.string.cancel, dialogClickListener)
				.show();
	}

	protected void setPreferencesValues() {
		final IcsSpinner spinnerFont = (IcsSpinner) findViewById(R.id.spinner_font_size);
		final ColorOptionsView fontColor = (ColorOptionsView) findViewById(R.id.color_options_fontcolor);
		final ColorOptionsView backgroundColor = (ColorOptionsView) findViewById(R.id.color_options_background);
		spinnerFont.setSelection(getTextSizePosition());
		fontColor.setValueColor(preferences.getTextColor());
		backgroundColor.setValueColor(preferences.getBackgroundColor());
	}

	protected void updateServerTextFromPreferences() {
		if (txtServer != null) {
			txtServer.setText(preferences.getServerAlias());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_preferences, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_translate:
			DownloadDialog dialog = new DownloadDialog(PreferenceActivity.this) {
				protected void onOk() {
					currentTranslation = preferences.getCurrentTranslation();
					reloadTranslationList();
					updateServerTextFromPreferences();
				};

				protected void onCancel() {
					reloadTranslationList();
					updateServerTextFromPreferences();
				};
			};
			dialog.show();
			return true;
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_use_default:
			preferences.setTextSize("medium");
			preferences.setTextColor(Color.BLACK);
			preferences.setBackgroundColor(Color.WHITE);
			setPreferencesValues();
			return true;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.list_installed) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.translation_installed_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.set_as_default:
			if (info.id > 0) {
				preferences.setCurrentTranslation((int) info.id);
				currentTranslation = preferences.getCurrentTranslation();
				((TranslationListAdapter) listInstalled.getAdapter())
						.notifyDataSetChanged();
			}
			return true;
		case R.id.delete_traduction:
			List<BibleTranslation> installedVersions = MyBibleLocalServices
					.getInstance(getApplicationContext())
					.getInstalledTranslations();
			if (installedVersions != null && installedVersions.size() > 1) {
				new DeleteTraslationTask().execute((int) info.id);
			} else {
				Toast.makeText(this, R.string.no_delete_last_translation,
						Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private OnClickListener getServerClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = PreferenceActivity.this;
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.server_name_title);
				final FrameLayout frameView = new FrameLayout(context);
				builder.setView(frameView);
				builder.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText input = (EditText) frameView
										.findViewById(R.id.editServerAlias);
								String serverAlias = input.getText().toString()
										.trim();
								if (serverAlias.length() == 0) {
									serverAlias = CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS;
								}
								txtServer.setText(serverAlias);
								preferences.setServerAlias(serverAlias);
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});

				final AlertDialog alertDialog = builder.create();

				LayoutInflater inflater = alertDialog.getLayoutInflater();
				View inflateView = inflater.inflate(
						R.layout.dialog_server_alias, frameView);
				final EditText input = (EditText) inflateView
						.findViewById(R.id.editServerAlias);
				input.setText(txtServer.getText());
				alertDialog.show();
			}
		};
	}

	private int getTextSizePosition() {
		String current = preferences.getTextSize();
		String[] values = getResources().getStringArray(
				R.array.font_size_array_value);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null && values[i].equalsIgnoreCase(current)) {
					return i;
				}
			}
		}
		return 2;
	}

	private void reloadTranslationList() {
		ListView listInstalled = (ListView) findViewById(R.id.list_installed);
		List<BibleTranslation> installedVersions = null;
		installedVersions = MyBibleLocalServices.getInstance(
				getApplicationContext()).getInstalledTranslations();
		TranslationListAdapter adapter = new TranslationListAdapter(this,
				installedVersions);
		listInstalled.setAdapter(adapter);
		TextView lbl_installed = (TextView) findViewById(R.id.lbl_installed);
		if (installedVersions != null && installedVersions.size() > 0) {
			lbl_installed.setText(R.string.lbl_installed);
		} else {
			lbl_installed.setText(R.string.no_translations_availables);
		}
	}

	private ProgressDialog tempProgressDialog;

	protected void initWait(int resID) {
		if (tempProgressDialog != null) {
			tempProgressDialog.dismiss();
		}
		tempProgressDialog = ProgressDialog.show(this, null, getResources()
				.getString(resID));
		tempProgressDialog.setCancelable(false);
		tempProgressDialog.show();
	}

	protected void stopWait() {
		if (tempProgressDialog != null) {
			tempProgressDialog.dismiss();
			tempProgressDialog = null;
		}
	}

	private class DeleteTraslationTask extends
			AsyncTask<Integer, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			initWait(R.string.deleting);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			try {
				MyBibleLocalServices.getInstance(getApplicationContext())
						.deleteTranslation(params[0]);
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			stopWait();
			if (result != null && result) {
				Toast.makeText(PreferenceActivity.this,
						R.string.msg_translation_deleted, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(PreferenceActivity.this,
						R.string.msg_delete_translation_error,
						Toast.LENGTH_LONG).show();
			}
			setCurrentBibleTranslation();
			reloadTranslationList();
			super.onPostExecute(result);
		}
	}

	private void setCurrentBibleTranslation() {
		BibleTranslation translation = MyBibleLocalServices.getInstance(
				getApplicationContext()).getSelectedBibleTranslation();
		if (translation != null) {
			currentTranslation = translation.getId();
		}
	}

	private class TranslationListAdapter extends ArrayAdapter<BibleTranslation> {

		private final Context context;
		private final List<BibleTranslation> installedVersions;

		public TranslationListAdapter(Context context,
				List<BibleTranslation> installedVersions) {
			super(context, R.layout.item_translation_on_list, installedVersions);
			this.context = context;
			this.installedVersions = installedVersions;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				rowView = inflater.inflate(R.layout.item_translation_on_list,
						null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.translationText = (TextView) rowView
						.findViewById(R.id.text_translation);
				viewHolder.image_selected = (ImageView) rowView
						.findViewById(R.id.imageCheck);
				rowView.setTag(viewHolder);
			}
			BibleTranslation bibleVersion = installedVersions.get(position);
			ViewHolder holder = (ViewHolder) rowView.getTag();
			holder.translationText.setText(bibleVersion.getAbrev() + " "
					+ bibleVersion.getName());
			holder.image_selected
					.setVisibility(currentTranslation == bibleVersion.getId() ? View.VISIBLE
							: View.INVISIBLE);

			return rowView;
		}

		@Override
		public long getItemId(int position) {
			return installedVersions.get(position).getId();
		}
	}

	private class ViewHolder {
		TextView translationText;
		ImageView image_selected;
	}

}
