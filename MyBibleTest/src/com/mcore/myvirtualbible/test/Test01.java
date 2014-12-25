package com.mcore.myvirtualbible.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.myvirtualbible.util.BibleHtmlTransform;

public class Test01 extends AndroidTestCase {

	@Test
	public void testHTMLTransform() {
		Map params = new HashMap();
		params.put(
				"bodystyle",
				"text-align:justify;font-size:medium;color:#000000;background-color:#FFFFFF");
		params.put("highlighterMap", new HashMap());
		params.put("highlighters", new ArrayList());
		String converted = BibleHtmlTransform.getInstance().convert(
				readFile("mybbl_Isaías_25.xml"), params);
		saveToDeveloperFile("converted.txt", converted);
		System.err.println("*********************************************");
	}

	private String readFile(String fileName) {
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					Environment.getExternalStorageDirectory().getPath()
							+ "/mybible/" + fileName)));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			br.close();
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		return text.toString();
	}

	private void saveToDeveloperFile(String fileName, String fileContent) {
		if (CommonConstants.MYBIBLE_DEVELOPER_MODE) {
			try {
				File myFile = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/mybible/mybbl_" + fileName);
				myFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myFile);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						fOut);
				outputStreamWriter.write(fileContent);
				outputStreamWriter.flush();
				outputStreamWriter.close();
				MediaScannerConnection.scanFile(getContext(),
						new String[] { myFile.getAbsolutePath() }, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
