package com.mcore.mybible.common.utilities;


/**
 * Contiene constantes utilizadas en el servicio y en el cliente de la
 * aplicacion.
 * 
 * @author Mario
 * 
 */
public class CommonConstants {
	
	public static final boolean MYBIBLE_DEVELOPER_MODE = true;

	public static final String BUNDLE_RESOURCE_NAME = "mybible";

	public static final String CLIENT_BUNDLE_RESOURCE_NAME = "serviceClientConfig";

	public static final String RESOURCE_PATH_KEY = "BIBLE_RESOURCE_PATH";

	public static final String SERVICE_SERVER_KEY = "DEFAULT_SERVICE_SERVER";
	
	public static final String DATABASE_HOST_KEY = "DATABASE_HOST";
	
	public static final String DATABASE_PORT_KEY = "DATABASE_PORT";
	
	public static final String DATABASE_USERNAME_KEY = "DATABASE_USERNAME";
	
	public static final String DATABASE_PASSWORD_KEY = "DATABASE_PASSWORD";
	
	public static final String SERVER_CONFIG_URL_KEY = "SERVER_CONFIG_URL";
	
	public static final String SERVER_CONFIG_ALIAS_CONTEXT_KEY = "SERVER_CONFIG_ALIAS";
	
	public static final String MAINTENANCE_MODE = "MAINTENANCE_MODE";
	
	public static final String LANGUAGE_CODE_ALL = "all";
	
	public static final String DEFAULT_SERVER_CONFIG_ALIAS = "Default";
	
	public static final String AUDIT_FILENAME = "auditlog";
	
	public static final String FAIL_LOGS_FILENAME = "failslog";
	
	public static final String AUDIT_EXT = ".txt";

	public static final String URL_SEPARATOR = "/";

	public static final String PARAMETER_ID = "id";

	public static final String PATH_PARAMETER_BIBLEID = "bibleId";

	public static final String PATH_PARAMETER_SIZEID = "sizeId";	
	
	public static final String ERROR_PATH = "/errorinfo";
	
	public static final String BASE_LOGOUT_PATH = "/logout";

	public static final String BASE_LOGIN_PATH = "/login";

	public static final String BASE_STATUS_PATH = "/status";
	
	public static final String BASE_GET_STATISTICS_PATH = "/getStatistics";

	public static final String BASE_GET_BIBLE_DATA_PATH = "/getBibleData/";
	
	public static final String BASE_GET_TRANSLATIONS_PATH = "/getTranslationList/";
	
	public static final String BASE_PUT_CONFIGURATION_PATH = "/putConfiguration/";
	
	public static final String LOGIN_PATH = BASE_LOGIN_PATH;

	public static final String LOGOUT_PATH = BASE_LOGOUT_PATH;

	public static final String STATUS_PATH = BASE_STATUS_PATH;
	
	public static final String GET_STATISTICS_PATH = BASE_GET_STATISTICS_PATH;
	
	public static final String GET_BIBLE_DATA_PATH = BASE_GET_BIBLE_DATA_PATH
			+ "{" + PARAMETER_ID + "}/{" + PATH_PARAMETER_BIBLEID + "}";
	
	public static final String GET_TRANSLATIONS_PATH = BASE_GET_TRANSLATIONS_PATH
			+ "{" + PARAMETER_ID + "}";
	
	public static final String PUT_CONFIGURATION_PATH = BASE_PUT_CONFIGURATION_PATH
			+ "{" + PARAMETER_ID + "}";

	/********************************************************************************************/
	public static final String PARAMETER_ERROR_ID = "errorID";
	public static final String PARAMETER_ERROR_DETAILS = "errorDetails";
	
	/*************************** ARTEFACTOS EN BB.DD DE SISTEMA *************************/
	
	public static final int SYSTEM_USERID = 1;
	public static final int PROFILES_GROUP_ID = 1;
	
	/************************************************************************************/

	public static final String SESSION_ATTRIBUTE_USERID = "userId";

	public static final String SESSION_ATTRIBUTE_TOKEN = "userToken";
	
	public static final String SESSION_ATTRIBUTE_VERSION = "version";

	public static final String METHOD_POST = "POST";
	
	public static final String METHOD_PUT = "PUT";

	public static final String REQUEST_PROPERTY_CONTENT_TYPE = "Content-Type";

	public static final String PROPERTY_CONTENT_TYPE_JSON = "application/json";
	public static final String MEDIA_FILENAME_PREFIX = "file";

	public static final String MEDIA_FILENAME_SUFIX = ".dat";

	public static final String MEDIA_FILENAME_MEDIAID_SEPARATOR = "-";

	public static final String TRANSLATION_LIST_FILENAME = "translations.props";
	
	public static final String PROP_ALIAS_NAME = "Alias";
	
	public static final String PROP_COPYRIGHT = "Copyright";
	
	public static final String PROP_ENCRYPTION_ALGORITHMS = "encryptionalgorithms";
	
	public static final String PROP_LANGUAGE = "language";
	
}