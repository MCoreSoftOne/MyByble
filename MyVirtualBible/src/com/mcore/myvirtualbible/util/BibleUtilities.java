package com.mcore.myvirtualbible.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.myvirtualbible.R;

public class BibleUtilities {
	
	public static Map connContext;
	
	public static Map getConnContext(Context ctx) {
		String serverConfig = MyBiblePreferences.getInstance(ctx).getServerAlias();
		if (serverConfig != null && serverConfig.trim().length() > 0) {
			if (connContext == null) {
				connContext = new HashMap();
			}			
			connContext.put(CommonConstants.SERVER_CONFIG_ALIAS_CONTEXT_KEY, serverConfig);
		}
		return connContext;
	}

	public static TranslationListDTO readTranslationFromDefaultFile(Context ctx) {
		TranslationListDTO result = new TranslationListDTO();
		List<TranslationDTO> traslations = new ArrayList<TranslationDTO>();
		try {
			InputStream stream = ctx.getAssets().open(CommonConstants.TRANSLATION_LIST_FILENAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					if (line != null && !line.startsWith("#")) {
						String[] data = line.split(",");
						if (data != null && data.length >= 5) {
							traslations.add(new TranslationDTO(data[0], data[1], data[2], data[3], data[4]));
						}
					}
				}
			} finally {
				br.close();
			}
			result.setTranslations((TranslationDTO[]) traslations.toArray(new TranslationDTO[traslations.size()]));
		} catch (Exception e) {
			//No se pudo leer
		}
		return result;
	}
	
	public static boolean resultDataForceMessage(ResultInfoDTO resultData, String serverConfigAlias) {
		if (resultData != null
				&& resultData.getResultID() == CommonErrorCodes.ERROR_CODE_USER_MESSAGE
				&& resultData.getResultDetails() != null
				&& resultData.getResultDetails().length() > 0) {
			return true;
		}
		if (resultData != null && serverConfigAlias != null && !serverConfigAlias.equalsIgnoreCase(CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS)
				&& resultData.getResultID() == CommonErrorCodes.ERROR_CODE_CONFIGURATION_NOT_FOUND) {
			return true;
		}
		return false;
	}
	
	public static boolean showUserMessage(Context context,
			ResultInfoDTO resultData) {
		if (resultData != null
				&& resultData.getResultID() == CommonErrorCodes.ERROR_CODE_USER_MESSAGE
				&& resultData.getResultDetails() != null
				&& resultData.getResultDetails().length() > 0) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			alertDialogBuilder.setTitle(R.string.msg_user_message_title);
			alertDialogBuilder.setMessage(resultData.getResultDetails())
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			try {
				AlertDialog alertDialog = alertDialogBuilder.create();
				
				alertDialog.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	public static boolean isSDPresent() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static String normalizeVerseInformation(Integer[] verseInfo) {
		if (verseInfo == null) {
			return null;
		}
		String result = null;
		if (verseInfo.length > 0) {
			result = String.valueOf(verseInfo[0]);
		}
		if (verseInfo.length > 1 && verseInfo[1] != verseInfo[0]) {
			result += "_"+String.valueOf(verseInfo[1]);
		}
		return result;
	}
	
	public static Integer[] getVerseInformation(String verse) {
		if (verse == null || verse.contains("(")) {
			return null;
		}
		int verseini = 0;
		int versefin = 0;
		if (verse.contains("[")) {
			verse = verse.replaceAll("\\[", "").replaceAll("\\]", "");
		}
		if (verse.contains(",")) {
			verse = verse.substring(0, verse.indexOf(",")).trim();
		}
		String separator = null;
		verse = verse.replaceAll("[a-z,A-Z]", "");		
		if (verse.contains("-")) {
			separator = "-";
		} else if (verse.contains("–")) {
			separator = "–";
		} else if (verse.contains("_")) {
			separator = "_";
		} else if (verse.contains("/")) {
			separator = "\\/";
		}
		if (separator != null) {
			String[] split = verse.split(separator);
			verseini = split.length > 0? Integer.parseInt(split[0].trim()): 0;
			versefin = split.length > 1? Integer.parseInt(split[1].trim()): 0;
		} else {
			verseini = Integer.parseInt(verse.trim());
			versefin = verseini;
		}
		if (verseini == 0) {
			return null;
		}
		return new Integer[] {verseini, versefin};
	}
	
}
