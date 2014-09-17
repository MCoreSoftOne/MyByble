package com.mcore.mybible.services.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mcore.mybible.common.dto.ConfigData;
import com.mcore.mybible.common.dto.ConfigItem;
import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.ServerConfigurartionData;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.common.dto.StatusDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.service.ICommonService;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.common.utilities.CommonUtilities;
import com.mcore.mybible.services.client.managers.CookieManager;

/**
 * Implementa la comunicaci�n con el servicio de backend para la aplicaci�n.
 * Requiere un fichero de propiedades con nombre
 * Constante=CLIENT_BUNDLE_RESOURCE_NAME para extraer la informacion de
 * conexi�n.
 * 
 * NO es un singleton. S� se usa monousuario, se puede usar una �nica instancia
 * usando getInstance().
 * 
 * @author Mario
 * 
 */
public class ServiceClientBusinessDelegate implements ICommonService,
		Serializable {

	private static final long serialVersionUID = 5310166741726777974L;

	private static final String USER_ID_TOKEN_KEY = "USER_ID_TOKEN";

	private Map context;

	private String serverName;

	private CookieManager cookieManager;

	public ServiceClientBusinessDelegate() {
		init(context);
	}

	public ServiceClientBusinessDelegate(Map context) {
		init(context);
	}

	private void init(Map context) {
		if (context == null) {
			context = new HashMap();
		}
		this.context = context;
		cookieManager = new CookieManager(context);
	}
	
	public int initServerNameIfDont() {
		return initServerNameIfDont(false);
	}
	
	public int initServerNameIfDont(boolean force) {
		int result = CommonErrorCodes.ERROR_CODE_NO_ERROR;
		if (serverName == null || force) {
			serverName = null;
			String serverConfig = null;
			if (context != null) {
				serverConfig = (String)context.get(CommonConstants.SERVER_CONFIG_ALIAS_CONTEXT_KEY);
			}
			if (serverConfig == null || serverConfig.trim().length() == 0) {
				serverConfig = CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS;
			}
			try {
				new URL(serverConfig);
				serverName = serverConfig;
			} catch (MalformedURLException e1) {
				// URL No valida, continua buscando en el servidor
			}
			boolean wasDefault = serverConfig.equalsIgnoreCase(CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS);
			ConfigData configData = getConfigurationFromServer();
			ConfigItem configItem = getServerFromConfiguration(configData, serverConfig);
			if (configItem != null) {
				serverName = configItem.getServeraddress();
			} 
			if (wasDefault && (serverName == null || serverName.length() == 0)) {
				try {
					ResourceBundle rb = ResourceBundle
							.getBundle(CommonConstants.CLIENT_BUNDLE_RESOURCE_NAME);
					serverName = rb.getString(CommonConstants.SERVICE_SERVER_KEY);
				} catch (Exception e) {
					// Evita errores.
				}
			}
			if (serverName != null) {				
				serverName = serverName.trim();
			}
			if (serverName == null || serverName.length() == 0) {
				result = CommonErrorCodes.ERROR_CODE_CONFIGURATION_NOT_FOUND;
			}
			if (serverName != null && serverName.equalsIgnoreCase(CommonConstants.MAINTENANCE_MODE)) {
				result = CommonErrorCodes.ERROR_CODE_SERVER_IN_MAINTENANCE_MODE;
			}
		}
		return result;
	}
	
	private ConfigData getConfigurationFromServer() {
		try {
			ResourceBundle rb = ResourceBundle.getBundle(CommonConstants.CLIENT_BUNDLE_RESOURCE_NAME);
			String urlServerConfiguration = rb.getString(CommonConstants.SERVER_CONFIG_URL_KEY);
			
			URL url = new URL(urlServerConfiguration);
			ResultInfoDTO response = genericPOSTRequest(url, null, ConfigData.class);
			return (ConfigData) convertObjectTo(response, ConfigData.class);
		} catch (Exception e) {
			
		}
		return null;
	}
	
	private ConfigItem getServerFromConfiguration(ConfigData configData, String configuration) {
		if (configData != null && configData.getItems() != null) {
			ConfigItem[] items = configData.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null
						&& items[i].getServeralias() != null) {
					if (items[i].getServeralias().equalsIgnoreCase(configuration)
							&& items[i].getServeralias() != null
							&& items[i].getServeralias().trim().length() > 0) {
						return items[i];
					}
				}
			}
		}
		return null;
	}

	/**
	 * Realiza la autenticacion de un Usuario.
	 * 
	 * @param loginInfo
	 *            Informaci�n y credenciales del usuario que inicia sesi�n.
	 * @return S� esta autorizado retorna un token de identificacion diferente
	 *         de cero. Cero s� ha ocurrido un error. El POJO de retorna incluye
	 *         informaci�n adicional del codigo error producido y detalles
	 *         adicionales.
	 */
	@Override
	public LoginOutDTO login(LoginInDTO loginInfo) {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_LOGIN_PATH);
			ResultInfoDTO response = genericPOSTRequest(url, loginInfo,
					LoginOutDTO.class);
			LoginOutDTO result = (LoginOutDTO) convertObjectTo(response,
					LoginOutDTO.class);
			if (result != null) {
				setUserInternalId(result.getId(), loginInfo.getId(), loginInfo.getVersion());
			}
			return result;
		} catch (Exception e) {
			return (LoginOutDTO) setConnectionError(new LoginOutDTO(), e);
		}
	}

	/**
	 * Valida s� el usuario ya ha realizado login en la sesion actual. Este debe
	 * ser un servicio local, no consume ningun servicio remoto.
	 * 
	 * @return S� usuario ya ha realizado login, retorna true, en caso contrario
	 *         retorna false.
	 */
	public boolean isUserLoggedIn() {
		try {
			String internalId = getUserInternalId();
			return  internalId != null && !internalId.equals("0");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Termina la sesion del usuario actual.
	 */
	@Override
	public void logout() {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_LOGOUT_PATH);
			genericPOSTRequest(url, null, null);
			setUserInternalId(0, null, null);
		} catch (Exception e) {
			// Nada pasa
		}
	}
	
	/**
	 * Estableces los datos de un recurso en el Stream indicado. Tiene en cuenta
	 * el tama�o solicitado.
	 * 
	 * @param mediaId
	 *            Identificador �nico del recurso.
	 * @param dataOut
	 *            Flujo de bytes donde quedar� almacenado el recurso.
	 * @return Informaci�n adicional del recurso.
	 */
	@Override
	public ResultInfoDTO getBibleData(String mediaId, OutputStream dataOut) {
		URL url;
		HttpURLConnection conn;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_GET_BIBLE_DATA_PATH
					+ getUserInternalId() + CommonConstants.URL_SEPARATOR
					+ mediaId);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(CommonConstants.METHOD_POST);
			conn.addRequestProperty(
					CommonConstants.REQUEST_PROPERTY_CONTENT_TYPE,
					CommonConstants.PROPERTY_CONTENT_TYPE_JSON);
			preConnect(conn);
			CommonUtilities.copyStream(conn.getInputStream(), dataOut);
			postConnect(conn);
			//CommonUtilities.copyStream(new FileInputStream("/sdcard/B_RVA.zip"), dataOut);
			return new ResultInfoDTO(CommonErrorCodes.ERROR_CODE_NO_ERROR,
					CommonErrorCodes.ERROR_CODE_NO_ERROR_DETAIL);
		} catch (Exception e) {
			return setConnectionError(new ResultInfoDTO(), e);
		}
	}


	@Override
	public TranslationListDTO getTranslations() {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_GET_TRANSLATIONS_PATH + getUserInternalId());
			ResultInfoDTO response = genericPOSTRequest(url, null,
					TranslationListDTO.class);
			TranslationListDTO result = (TranslationListDTO) convertObjectTo(response,
					TranslationListDTO.class);
			return result;
		} catch (Exception e) {
			return (TranslationListDTO) setConnectionError(new TranslationListDTO(), e);
		}
	}

	@Override
	public StatisticsDTO getStatistics(StatisticsInDTO filterInfo) {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_GET_STATISTICS_PATH);
			ResultInfoDTO response = genericPOSTRequest(url, filterInfo,
					StatisticsDTO.class);
			StatisticsDTO result = (StatisticsDTO) convertObjectTo(response,
					StatisticsDTO.class);
			return result;
		} catch (Exception e) {
			return (StatisticsDTO) setConnectionError(new StatisticsDTO(), e);
		}
	}
	
	// ***************** UTILIDADES *******************
	/**
	 * Transforma el objeto indicado en una cadena JSON escrita en el flujo de
	 * bytes indicado.
	 */
	private void getPostJsonObject(Object param, OutputStream out)
			throws IOException {
		byte[] jsonobject = createGSon().toJson(param).getBytes();
		out.write(jsonobject);
	}

	/**
	 * Transforma el retorno HTTP en un objeto JSON con el tipo indicado.
	 * 
	 */
	private ResultInfoDTO getPostResult(InputStream is, Class clase)
			throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		ResultInfoDTO result = (ResultInfoDTO) createGSon().fromJson(reader,
				clase);
		reader.close();
		return result;
	}

	public Gson createGSon() {
		GsonBuilder builder = new GsonBuilder();
		// Register an adapter to manage the date types as long values
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			public Date deserialize(JsonElement json, Type typeOfT,
					JsonDeserializationContext context)
					throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		});
		builder.disableHtmlEscaping();
		return builder.create();
	}

	/**
	 * Establece los codigos correspondientes de error cuando se produce un
	 * error de conexi�n.
	 */
	protected ResultInfoDTO setConnectionError(ResultInfoDTO obj, Exception e) {
		obj.setResultID(CommonErrorCodes.ERROR_CODE_CONNECTION_ERROR);
		if (e != null) {
			obj.setResultDetails("Error: " + e.getMessage());
		} else {
			obj.setResultDetails("Error de conexi�n");
		}
		return obj;
	}

	/**
	 * Establece cookies antes de iniciar la comunicaci�n HTTP.
	 * 
	 */
	protected void preConnect(HttpURLConnection conn) throws IOException {
		cookieManager.setCookies(conn);
	}

	/**
	 * Almacena la cookies de la respuesta (s� aplica) despues de la llamada
	 * HTTP.
	 * 
	 */
	protected void postConnect(HttpURLConnection conn) throws IOException {
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(conn.getResponseMessage());
		}
		cookieManager.storeCookies(conn);
	}

	private ResultInfoDTO convertObjectTo(ResultInfoDTO object, Class classTo) {
		if (object != null) {
			if (object.getClass().isAssignableFrom(classTo)) {
				return object;
			} else {
				try {
					ResultInfoDTO result = (ResultInfoDTO) classTo
							.newInstance();
					result.setResultID(object.getResultID());
					result.setResultDetails(object.getResultDetails());
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
	
	protected ResultInfoDTO genericPOSTRequest(URL url, Object param,
			Class responseClass) {
		return genericRequest(CommonConstants.METHOD_POST, url, param, responseClass);
	}

	protected ResultInfoDTO genericRequest(String method, URL url, Object param,
			Class responseClass) {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.addRequestProperty(
					CommonConstants.REQUEST_PROPERTY_CONTENT_TYPE,
					CommonConstants.PROPERTY_CONTENT_TYPE_JSON);
			preConnect(conn);
			if (param != null) {
				conn.setDoOutput(true);
				OutputStream outStream = conn.getOutputStream();
				try {
					getPostJsonObject(param, outStream);
				} finally {
					outStream.close();
				}
			}
			ResultInfoDTO result = null;
			if (responseClass != null) {
				result = (ResultInfoDTO) getPostResult(conn.getInputStream(),
						responseClass);
			} else {
				conn.getInputStream();
			}
			postConnect(conn);
			conn.disconnect();
			return result;
		} catch (Exception e) {
			try {
				return setConnectionError(
						(ResultInfoDTO) responseClass.newInstance(), e);
			} catch (Exception e1) {
				return setConnectionError(new ResultInfoDTO(), e);
			}
		}
	}

	private String getUserInternalId() {
		String result = null;
		if (context != null) {
			result = (String) context.get(USER_ID_TOKEN_KEY);
		}
		if (result == null) {
			result = "0";
		}
		return result;
	}

	private void setUserInternalId(int userInternalId, String deviceId, String version) {
		if (context != null) {
			if (userInternalId == 0 || deviceId == null) {
				context.put(USER_ID_TOKEN_KEY, "" + userInternalId);
			} else {
				context.put(USER_ID_TOKEN_KEY, CommonUtilities.cfm(userInternalId, deviceId, version));
			}
		}
	}

	public Map getContext() {
		return context;
	}

	public void setContext(Map context) {
		this.context = context;
		cookieManager.setContext(context);
	}

	@Override
	public StatusDTO getStatus() {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_STATUS_PATH);
			ResultInfoDTO response = genericPOSTRequest(url, null,
					StatusDTO.class);
			StatusDTO result = (StatusDTO) convertObjectTo(response,
					StatusDTO.class);
			
			return result;
		} catch (Exception e) {
			return (StatusDTO) setConnectionError(new StatusDTO(), e);
		}
	}

	@Override
	public ResultInfoDTO putConfiguration(String securityId,
			ServerConfigurartionData serverConfigdata) {
		URL url;
		try {
			url = new URL(serverName
					+ CommonConstants.BASE_PUT_CONFIGURATION_PATH + securityId);
			ResultInfoDTO response = genericRequest(CommonConstants.METHOD_PUT,  url, serverConfigdata,
					ResultInfoDTO.class);
			ResultInfoDTO result = (ResultInfoDTO) convertObjectTo(response,
					ResultInfoDTO.class);
			
			return result;
		} catch (Exception e) {
			return (StatusDTO) setConnectionError(new StatusDTO(), e);
		}
	}

	// ****** FIN UTILIDADES **********
	
}
