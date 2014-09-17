package com.mcore.mybible.common.utilities;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Utilidades comunes de la aplicación.
 * @author Mario
 *
 */
public class CommonUtilities {

	private static CommonUtilities instance;

	public static CommonUtilities getInstance() {
		if (instance == null) {
			instance = new CommonUtilities();
		}
		return instance;
	}

	/**
	 * Realiza una copia del flujo de bytes encontrados en el input al output
	 * @param input Flujo de bytes fuente.
	 * @param output Flujo de bytes objetivo.
	 * @throws IOException Error en el recurso de lectura o escritura.
	 */
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[16384]; // Adjust if you want
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
	
	public static String getMD5FromStream(InputStream fis) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] dataBytes = new byte[16384];
			int nread = 0; 
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			};
        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
	}
	
	public static String getMD5FromFile(String fileName) throws Exception {
		String result = null;
		FileInputStream fis = new FileInputStream(fileName);
		try {
			result = getMD5FromStream(fis);
		} finally {
			fis.close();
		}
        return result;
	}
	
	public static String cfm(int m, String d, String w) {
		String e = ((char)65) + "" + m + d + w + "mjcylah".hashCode() + "r";
		e += e;
		try {
			ByteArrayInputStream u = new ByteArrayInputStream(e.getBytes("UTF-8"));
			String result;
			try {
				result= getMD5FromStream(u);
			}	finally {
				u.close();
			}
			return result;
		} catch (Exception e2) {
			e2.printStackTrace();
			return "error";
		}		
	}

}
