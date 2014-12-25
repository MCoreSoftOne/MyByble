package com.mcore.myvirtualbible.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.common.utilities.CommonUtilities;
import com.mcore.mybible.services.client.IBusinessDelegate;
import com.mcore.mybible.services.client.factories.BusinessDelegateFactory;
import com.mcore.myvirtualbible.BaseGeneralActivity;
import com.mcore.myvirtualbible.R;
import com.mcore.myvirtualbible.db.IMyBibleLocalServices;
import com.mcore.myvirtualbible.db.MyBibleLocalServices;
import com.mcore.myvirtualbible.model.BibleTranslation;
import com.mcore.myvirtualbible.util.BibleUtilities;
import com.mcore.myvirtualbible.util.MyBibleConstants;
import com.mcore.myvirtualbible.util.MyBiblePreferences;

public class DownloadDialog extends Dialog {

	// private static final String MYBIBLE_TAG = "MYBIBLE_TAG";
	private TranslationListDTO translations;
	private TranslationListDTO updatePending;
	private boolean working;

	private List<BibleTranslation> installedTr;

	private ResultInfoDTO messagePending;

	private MyBiblePreferences preferences;

	private TextView txtServer;
	
	private IMyBibleLocalServices myBibleLocalServices;

	private boolean allowExternalStorage;

	public DownloadDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		messagePending = null;
		Context context = getContext();
		preferences = MyBiblePreferences.getInstance(context
				.getApplicationContext());
		setCancelable(true);
		setContentView(R.layout.dialog_download_bible);
		working = false;
		setTitle(context.getResources().getString(
				R.string.download_dialog_title));
		myBibleLocalServices = MyBibleLocalServices
				.getInstance(getContext().getApplicationContext());
		translations = myBibleLocalServices.getDownloadbleTranslations();
		installedTr = myBibleLocalServices.getInstalledTranslations();
		adjustInstallablesVersions(translations);
		final IcsSpinner versionsSpinner = (IcsSpinner) findViewById(R.id.spn_bbl_available);
		executeUpdateVersionTask();
		final Button btnDownload = (Button) findViewById(R.id.btn_download);
		final Button btnCancel = (Button) findViewById(R.id.btn_cancelar);
		btnDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TranslationDTO selectedItem = (TranslationDTO) versionsSpinner
						.getSelectedItem();
				if (selectedItem != null) {
					setLoadingMode(true);
					if (CommonConstants.MYBIBLE_DEVELOPER_MODE && MyBibleConstants.MYBIBLE_DEVELOPER_MODE_FULL_DOWNLOAD) {
						new DownloadAndLoadTranslationTask()
						.execute(translations.getTranslations());
					} else {						
						new DownloadAndLoadTranslationTask()
						.execute(new TranslationDTO[] { selectedItem });
					}
				}
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		View.OnClickListener listener = getServerClickListener();
		txtServer = (TextView) findViewById(R.id.editServerAlias);
		txtServer.setText(preferences.getServerAlias());
		txtServer.setOnClickListener(listener);
		TextView txtServerName = (TextView) findViewById(R.id.textServerName);
		txtServerName.setOnClickListener(listener);
		final CheckBox chkUseExternalStorage = (CheckBox) findViewById(R.id.chkUseExternalStorage);
		chkUseExternalStorage.setChecked(preferences.getUseExternalStorage());
		if (allowExternalStorage) {
			chkUseExternalStorage.setVisibility(View.VISIBLE);
			chkUseExternalStorage
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								boolean isSDPresent = BibleUtilities
										.isSDPresent();
								if (!isSDPresent) {
									new AlertDialog.Builder(getContext())
											.setTitle(
													R.string.no_sd_available_title)
											.setMessage(
													R.string.no_sd_available_message)
											.setPositiveButton(
													android.R.string.ok, null)
											.show();
									chkUseExternalStorage.setChecked(false);
									isChecked = false;
									return;
								}
							}
							preferences.setUseExternalStorage(isChecked);
							myBibleLocalServices
									.setUseExternalStorage(isChecked);
							if (isChecked) {
								List<BibleTranslation> installedTranslations = myBibleLocalServices.getInstalledTranslations();
								if (installedTranslations != null && installedTranslations.size() > 0) {
									doOk(null);
								}
 							}
						}
					});
		} else {
			chkUseExternalStorage.setVisibility(View.GONE);
		}
		onTranlationChange();
	}

	protected void onTranlationChange() {
		updateLangSpinner();
		updateTranslationSpinner();
		TextView text = (TextView) findViewById(R.id.text_dialog_download);
		final IcsSpinner translationsSpinner = (IcsSpinner) findViewById(R.id.spn_bbl_available);
		final IcsSpinner languagesSpinner = (IcsSpinner) findViewById(R.id.spn_language);
		if (translations == null || translations.getTranslations() == null
				|| translations.getTranslations().length == 0) {
			enableComponents(false);
			TextView txtServer = (TextView) findViewById(R.id.editServerAlias);
			TextView txtServerName = (TextView) findViewById(R.id.textServerName);
			final Button btnCancel = (Button) findViewById(R.id.btn_cancelar);
			btnCancel.setEnabled(true);
			txtServer.setEnabled(true);
			txtServerName.setEnabled(true);
			text.setText(R.string.no_downloads_avaliables);
			translationsSpinner.setVisibility(View.INVISIBLE);
			languagesSpinner.setVisibility(View.INVISIBLE);
		} else {
			enableComponents(true);
			text.setText(R.string.download_dialog_text);
			translationsSpinner.setVisibility(View.VISIBLE);
			languagesSpinner.setVisibility(View.VISIBLE);
		}
	}

	protected void executeUpdateVersionTask() {
		new UpdateTranslationListTask().execute(new String[0]);
	}

	private View.OnClickListener getServerClickListener() {
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = getContext();
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
								executeUpdateVersionTask();
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

	@Override
	public void cancel() {
		super.cancel();
		onCancel();
	}

	private void adjustInstallablesVersions(TranslationListDTO translations) {
		String lang = Locale.getDefault().getLanguage();
		if (translations != null && translations.getTranslations() != null) {
			List<TranslationDTO> nList = new ArrayList<TranslationDTO>();
			List<TranslationDTO> nOtrosList = new ArrayList<TranslationDTO>();
			TranslationDTO[] cList = translations.getTranslations();
			for (int i = 0; i < cList.length; i++) {
				if (cList[i] != null && !isVersionInstalled(cList[i])) {
					if (lang != null && cList[i].getLanguage() != null
							&& lang.equalsIgnoreCase(cList[i].getLanguage())) {
						nList.add(cList[i]);
					} else {
						nOtrosList.add(cList[i]);
					}
				}
			}
			nList.addAll(nOtrosList);
			translations.setTranslations((TranslationDTO[]) nList
					.toArray(new TranslationDTO[nList.size()]));
		}
	}

	private boolean isVersionInstalled(TranslationDTO data) {
		for (Iterator iterator = installedTr.iterator(); iterator.hasNext();) {
			BibleTranslation tversion = (BibleTranslation) iterator.next();
			if (tversion != null) {
				boolean result = isEqual(tversion.getAbrev(), data.getId())
						&& isEqual(tversion.getLanguage(), data.getLanguage())
						&& isEqual(tversion.getRevision(), data.getVersion());
				if (result) {
					return result;
				}
			}
		}
		return false;
	}

	protected boolean isEqual(String a, String b) {
		return (a == b || (a != null && b != null && b.equals(a)));
	}

	protected void onCancel() {

	}

	protected void onOk(TranslationDTO data) {

	}
	
	public static void sendDownloadEvent(BaseGeneralActivity activity, TranslationDTO data) {
		if (activity != null && data != null && data.getName() != null) {
			activity.sendEvent(MyBibleConstants.CATEGORY_DOWNLOAD, MyBibleConstants.ACTION_DOWNLOAD, MyBibleConstants.DOWNLOAD_NAME, data.getName());
			activity.sendEvent(MyBibleConstants.CATEGORY_DOWNLOAD, MyBibleConstants.ACTION_DOWNLOAD, MyBibleConstants.DOWNLOAD_LANG, data.getLanguage());
		}
	}

	protected void doOk(TranslationDTO dataT) {
		try {
			dismiss();
		} catch (Exception e) {
		}
		onOk(dataT);
	}

	private void setLoadingMode(boolean loading) {
		if (loading) {
			initWait(R.string.downloading);
		} else {
			stopWait();
		}
		enableComponents(!loading);
		setCancelable(!loading);
	}

	private void enableComponents(boolean enabled) {
		final Button btnDownload = (Button) findViewById(R.id.btn_download);
		final Button btnCancel = (Button) findViewById(R.id.btn_cancelar);
		final IcsSpinner languagesSpinner = (IcsSpinner) findViewById(R.id.spn_language);
		final IcsSpinner translationsSpinner = (IcsSpinner) findViewById(R.id.spn_bbl_available);
		TextView txtServer = (TextView) findViewById(R.id.editServerAlias);
		TextView txtServerName = (TextView) findViewById(R.id.textServerName);
		translationsSpinner.setEnabled(enabled);
		languagesSpinner.setEnabled(enabled);
		btnDownload.setEnabled(enabled);
		btnCancel.setEnabled(enabled);
		txtServer.setEnabled(enabled);
		txtServerName.setEnabled(enabled);
	}

	public void allowExternalStorageOption() {
		allowExternalStorage = true;
	}

	// NO invocar desde hilo principal.
	private LoginOutDTO doLogin(IBusinessDelegate services) throws Exception {
		String deviceId;
		try {
			deviceId = Secure.getString(getContext().getContentResolver(),
					Secure.ANDROID_ID);

		} catch (Exception e) {
			deviceId = "unknown";
		}
		PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(
				getContext().getPackageName(), 0);
		LoginOutDTO login = services.login(new LoginInDTO(deviceId,
				pInfo.versionName));
		String serverAlias = preferences.getServerAlias();
		if (!BibleUtilities.resultDataForceMessage(login, serverAlias)) {
			return login;
		} else {
			messagePending = login;
		}
		return null;
	}

	private void processPendingMessageIfExist() {
		if (messagePending != null) {
			reloadInvocationConfigNotFound();
			BibleUtilities.showUserMessage(getContext(), messagePending);
			messagePending = null;
		}
	}

	private void tryToSaveVersions(TranslationListDTO data) {
		if (data != null && working) {
			updatePending = data;
		} else {
			updateTranslationList(data);
		}
	}

	private void updateLangSpinner() {
		String lastSelectedLanguage = null;
		getSelectedLanguage();
		if (lastSelectedLanguage == null || lastSelectedLanguage.length() == 0) {
			lastSelectedLanguage = getSystemLanguage();
		}
		final IcsSpinner langSpinner = (IcsSpinner) findViewById(R.id.spn_language);
		langSpinner.setOnItemSelectedListener(null);
		List<LanguageDTO> languages = new ArrayList<DownloadDialog.LanguageDTO>();
		languages.add(new LanguageDTO(CommonConstants.LANGUAGE_CODE_ALL,
				getContext().getResources().getString(R.string.all_languages)));
		for (int i = 0; i < translations.getTranslations().length; i++) {
			TranslationDTO translationDTO = translations.getTranslations()[i];
			if (translationDTO != null) {
				String language = translationDTO.getLanguage();
				if (language != null && !langCodeExiste(language, languages)) {
					languages.add(new LanguageDTO(language,
							getLanguageDescription(language)));
				}
			}
		}
		final LanguagesArrayAdapter versionsAdapter = new LanguagesArrayAdapter(
				getContext(), android.R.layout.simple_list_item_1, languages);
		langSpinner.setAdapter(versionsAdapter);
		int position = -1;
		int i = 0;
		for (Iterator iterator = languages.iterator(); iterator.hasNext();) {
			LanguageDTO itemTemp = (LanguageDTO) iterator.next();
			if (itemTemp != null && itemTemp.code != null
					&& itemTemp.code.equalsIgnoreCase(lastSelectedLanguage)) {
				position = i;
				break;
			}
			i++;
		}
		if (position >= 0) {
			langSpinner.setSelection(position);
		}
		langSpinner
				.setOnItemSelectedListener(new IcsAdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(IcsAdapterView<?> parent,
							View view, int position, long id) {
						preferences
								.setDownloadSelectedLanguage(getSelectedLanguage());
						updateTranslationSpinner();
					}

					@Override
					public void onNothingSelected(IcsAdapterView<?> parent) {

					}

				});
	}

	private boolean langCodeExiste(String code, List<LanguageDTO> languages) {
		for (Iterator iterator = languages.iterator(); iterator.hasNext();) {
			LanguageDTO languageDTO = (LanguageDTO) iterator.next();
			if (languageDTO.code.equalsIgnoreCase(code)) {
				return true;
			}
		}
		return false;
	}

	private String getLanguageDescription(String lang) {
		Locale[] availableLocales = Locale.getAvailableLocales();
		if (availableLocales != null) {
			for (int i = 0; i < availableLocales.length; i++) {
				if (availableLocales[i] != null
						&& availableLocales[i].getISO3Language() != null
						&& availableLocales[i].getLanguage().equalsIgnoreCase(
								lang)) {
					return availableLocales[i].getDisplayName();
				}
			}
		}
		return String.valueOf(lang);
	}

	private String getSystemLanguage() {
		return Locale.getDefault().getLanguage();
	}

	private String getSelectedLanguage() {
		String result = null;
		final IcsSpinner langSpinner = (IcsSpinner) findViewById(R.id.spn_language);
		if (langSpinner.getAdapter() != null) {
			Object item = langSpinner.getSelectedItem();
			if (item instanceof LanguageDTO) {
				LanguageDTO selectedItem = (LanguageDTO) item;
				if (selectedItem != null) {
					result = selectedItem.code;
				}
			}
		}
		if (result == null || result.length() == 0) {
			result = preferences.getDownloadSelectedLanguage();
		}
		return result;
	}

	private void updateTranslationSpinner() {
		final IcsSpinner translationSpinner = (IcsSpinner) findViewById(R.id.spn_bbl_available);
		TranslationDTO selectedItem = null;
		if (translationSpinner.getAdapter() != null) {
			Object item = translationSpinner.getSelectedItem();
			if (item instanceof TranslationDTO) {
				selectedItem = (TranslationDTO) item;
			}
		}
		if (translations != null) {
			String selectedLanguage = getSelectedLanguage();
			List<TranslationDTO2> listTemp = new ArrayList<TranslationDTO2>();
			for (int i = 0; i < translations.getTranslations().length; i++) {
				TranslationDTO dto = translations.getTranslations()[i];
				if (selectedLanguage.equals(CommonConstants.LANGUAGE_CODE_ALL)
						|| selectedLanguage.equalsIgnoreCase(dto.getLanguage())) {
					listTemp.add(new TranslationDTO2(dto));
				}
			}
			final TranslationsArrayAdapter versionsAdapter = new TranslationsArrayAdapter(
					getContext(), android.R.layout.simple_list_item_1, listTemp);
			translationSpinner.setAdapter(versionsAdapter);
			if (selectedItem != null && selectedItem.getId() != null) {
				int position = -1;
				int i = 0;
				for (Iterator iterator = listTemp.iterator(); iterator
						.hasNext();) {
					TranslationDTO2 itemTemp = (TranslationDTO2) iterator
							.next();
					if (itemTemp != null && itemTemp.getId() != null
							&& itemTemp.getId().equals(selectedItem.getId())) {
						position = i;
						break;
					}
					i++;
				}
				if (position >= 0) {
					translationSpinner.setSelection(position);
				}
			}
		}
	}

	private void updateTranslationList(TranslationListDTO data) {
		updatePending = null;
		if (data != null && data.getTranslations() != null
				&& data.getTranslations().length > 0) {
			adjustInstallablesVersions(data);
			if (!data.equals(translations)) {
				try {
					IMyBibleLocalServices myBibleLocalServices = MyBibleLocalServices
							.getInstance(getContext().getApplicationContext());
					myBibleLocalServices.writeTranslationToDatabase(data);
					translations = myBibleLocalServices
							.readTranslationFromDatabase();
					onTranlationChange();
				} catch (Exception e) {
					// No pasa nada
				}
			}
		}
	}

	private ProgressDialog tempProgressDialog;

	protected void initWait(int resID) {
		if (tempProgressDialog != null) {
			tempProgressDialog.dismiss();
		}
		tempProgressDialog = ProgressDialog.show(getContext(), null,
				getContext().getResources().getString(resID));
		tempProgressDialog.setCancelable(false);
		tempProgressDialog.show();
	}

	protected void changeTextWait(int resID) {
		if (tempProgressDialog != null) {
			tempProgressDialog.setMessage(getContext().getResources()
					.getString(resID));
		}
	}

	protected void stopWait() {
		if (tempProgressDialog != null) {
			tempProgressDialog.dismiss();
			tempProgressDialog = null;
		}
	}
	
	protected void showMaintenanceMessage() {
		Dialog dl = new Dialog(getContext()) {
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.dialog_maintenance);
				setTitle(R.string.maintenance_mode_title);
				Button btnClose = (Button) findViewById(R.id.button_close);
				btnClose.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();				
					}
				});
			}
		};
		dl.show();
	}

	private void reloadInvocationConfigNotFound() {
		if (messagePending != null
				&& messagePending.getResultID() == CommonErrorCodes.ERROR_CODE_CONFIGURATION_NOT_FOUND) {
			if (!CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS
					.equalsIgnoreCase(preferences.getServerAlias())) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						getContext());
				alertDialogBuilder.setTitle(R.string.server_name_title);
				alertDialogBuilder
						.setMessage(
								getContext().getResources().getString(
										R.string.server_alias_error,
										preferences.getServerAlias()))
						.setCancelable(true)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										txtServer
												.setText(CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS);
										preferences
												.setServerAlias(CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS);
										executeUpdateVersionTask();
									}
								})
						.setNegativeButton(android.R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
			messagePending = null;
		}
	}

	private class UpdateTranslationListTask extends
			AsyncTask<String, Void, TranslationListDTO> {

		@Override
		protected TranslationListDTO doInBackground(String... params) {
			try {
				IBusinessDelegate services = BusinessDelegateFactory
						.getNewServiceClient(BibleUtilities
								.getConnContext(getContext()
										.getApplicationContext()));
				LoginOutDTO doLogin = doLogin(services);
				if (doLogin == null) {
					return null;
				}
				return services.getTranslations();
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(TranslationListDTO result) {
			processPendingMessageIfExist();
			tryToSaveVersions(result);
			super.onPostExecute(result);
		}
	}

	private class DownloadAndLoadTranslationTask extends
			AsyncTask<TranslationDTO, Integer, Integer> {
		
		protected TranslationDTO dataT;

		@Override
		protected void onPreExecute() {
			working = true;
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(TranslationDTO... params) {
			int result = 0;
			try {
				for (int i = 0; i < params.length; i++) {
					dataT = params[i];
					String translation = dataT.getId();
					String translationFilename = translation + ".zip";
					IBusinessDelegate services = BusinessDelegateFactory
							.getNewServiceClient(BibleUtilities
									.getConnContext(getContext()
											.getApplicationContext()));
					LoginOutDTO doLogin = doLogin(services);
					if (doLogin == null) {
						return 0;
					}
					if (doLogin.getResultID() != CommonErrorCodes.ERROR_CODE_NO_ERROR) {
						return -doLogin.getResultID();
					}
					FileOutputStream dataOut;
					boolean isSDCard = myBibleLocalServices.getUseExternalStorage();
					if (isSDCard) {
						translationFilename = Environment
								.getExternalStorageDirectory()
								+ MyBibleConstants.EXTERNAL_DB_DIR
								+ translationFilename;
						dataOut = new FileOutputStream(translationFilename);
					} else {
						dataOut = getContext().openFileOutput(
								translationFilename, Context.MODE_PRIVATE);
					}
					try {
						ResultInfoDTO dataResult = null;
						try {
							dataResult = services
									.getBibleData(translation, dataOut);
						} finally {
							dataOut.close();
						}
						if (dataResult == null
								|| dataResult.getResultID() != CommonErrorCodes.ERROR_CODE_NO_ERROR) {
							throw new RuntimeException("Server error !");
						}
						publishProgress(1);
						String md5FromStream = null;
						FileInputStream dataTest;
						if (isSDCard) {
							dataTest = new FileInputStream(translationFilename);
						} else {
							dataTest = getContext().openFileInput(
									translationFilename);
						}
						try {
							md5FromStream = CommonUtilities
									.getMD5FromStream(dataTest);
						} finally {
							dataTest.close();
						}
						if (md5FromStream != null
								&& md5FromStream.equalsIgnoreCase(dataT.getMd5())) {
							if (dataResult != null
									&& dataResult.getResultID() == CommonErrorCodes.ERROR_CODE_NO_ERROR) {
								result = MyBibleLocalServices.getInstance(
										getContext().getApplicationContext())
										.addTranslationToDataBase(
												translationFilename);
							}
						} else {
							return 0;
						}
						
					} finally {
						if (isSDCard) {
							new File(translationFilename).delete();
						} else {
							getContext().deleteFile(translationFilename);
						}
					}
				}
			} catch (Exception e) {
				return 0;
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int value = values != null && values.length > 0
					&& values[0] != null ? values[0] : 1;
			changeTextWait(value == 0 ? R.string.downloading : R.string.loading);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) {
			processPendingMessageIfExist();
			working = false;
			if (result != null && result > 0) {
				preferences.setCurrentTranslation(result);
				doOk(dataT);
			} else {
				if (result == -CommonErrorCodes.ERROR_CODE_SERVER_IN_MAINTENANCE_MODE) {					
					showMaintenanceMessage();
				} else {					
					Toast.makeText(
							getContext().getApplicationContext(),
							getContext().getResources().getString(
									R.string.msg_error_downloading),
									Toast.LENGTH_LONG).show();
				}
			}
			setLoadingMode(false);
			updateTranslationList(updatePending);
		}

	}

	private class TranslationsArrayAdapter extends
			ArrayAdapter<TranslationDTO2> {
		public TranslationsArrayAdapter(Context context,
				int textViewResourceId, List<TranslationDTO2> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	private class LanguagesArrayAdapter extends ArrayAdapter<LanguageDTO> {
		public LanguagesArrayAdapter(Context context, int textViewResourceId,
				List<LanguageDTO> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	private class LanguageDTO {

		String code;
		String name;

		public LanguageDTO(String code, String name) {
			super();
			this.code = code;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private class TranslationDTO2 extends TranslationDTO {
		private static final long serialVersionUID = 1L;

		public TranslationDTO2(TranslationDTO data) {
			setId(data.getId());
			setName(data.getName());
			setVersion(data.getVersion());
			setLanguage(data.getLanguage());
			setMd5(data.getMd5());
		}

		@Override
		public String toString() {
			return getName();
		}
	}

}
