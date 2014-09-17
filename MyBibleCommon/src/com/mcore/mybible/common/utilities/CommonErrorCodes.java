package com.mcore.mybible.common.utilities;

/**
 * Contienes los c�digos de error generados por la aplicaci�n.
 * 
 * @author Mario
 * 
 */
public interface CommonErrorCodes {
	
	//NOTA: No pueden ser negativos.

	public static final int ERROR_CODE_NO_ERROR = 0;
	public static final String ERROR_CODE_NO_ERROR_DETAIL = "OK";
	public static final int ERROR_CODE_USER_MESSAGE = 5;
	public static final int ERROR_CODE_UNKNOWN = 10;
	public static final int ERROR_CODE_DATABASE_ERROR = 20;

	public static final int ERROR_CODE_INVALID_USER = 100;
	public static final int ERROR_CODE_INVALID_USER_SESSION = 101;
	public static final int ERROR_CODE_ERROR_READING_FILE = 102;
	public static final int ERROR_CODE_CONFIGURATION_NOT_FOUND = 103;
	public static final int ERROR_CODE_ACCESS_DENIED = 104;
	public static final int ERROR_CODE_SERVER_IN_MAINTENANCE_MODE = 105;

	public static final int ERROR_CODE_CONNECTION_ERROR = 1000;

}
