package com.mcore.mybible.common.utilities;

/**
 * Implementa (Temporalmente?) la utilidad de Trazas en el sistema.
 * 
 * @author Mario
 * 
 */
public class CommonLog {

	private static final String DEBUG_STR = "DEBUG";

	private static final String INFO_STR = "INFO";

	private static final String WARNING_STR = "WARNING";

	private static final String ERROR_STR = "ERROR";

	public static void d(String tag, String msg) {
		writeLog(DEBUG_STR, tag, msg, null);
	}

	public static void i(String tag, String msg) {
		writeLog(INFO_STR, tag, msg, null);
	}

	public static void w(String tag, String msg) {
		writeLog(WARNING_STR, tag, msg, null);
	}

	public static void e(String tag, String msg) {
		writeLog(ERROR_STR, tag, msg, null);
	}

	public static void d(String tag, String msg, Throwable e) {
		writeLog(DEBUG_STR, tag, msg, e);
	}

	public static void i(String tag, String msg, Throwable e) {
		writeLog(INFO_STR, tag, msg, e);
	}

	public static void w(String tag, String msg, Throwable e) {
		writeLog(WARNING_STR, tag, msg, e);
	}

	public static void e(String tag, String msg, Throwable e) {
		writeLog(ERROR_STR, tag, msg, e);
	}

	private static void writeLog(String type, String tag, String msg,
			Throwable e) {
		System.err.println("[" + type + "] " + tag + " --> " + msg);
		if (e != null) {
			e.printStackTrace();
		}
	}

}
