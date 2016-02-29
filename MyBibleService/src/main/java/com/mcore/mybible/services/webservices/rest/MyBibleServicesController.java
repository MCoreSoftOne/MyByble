package com.mcore.mybible.services.webservices.rest;

import static com.mcore.mybible.common.utilities.CommonConstants.*;
import static com.mcore.mybible.common.utilities.CommonErrorCodes.*;
import static com.mcore.mybible.services.constants.ServicesConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.ServerConfigurartionData;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.common.dto.StatusDTO;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.dto.UserDTO;
import com.mcore.mybible.common.utilities.CommonUtilities;
import com.mcore.mybible.services.exception.ServiceException;
import com.mcore.mybible.services.webservices.rest.utilities.AuditUtils;
import com.mcore.mybible.services.webservices.rest.utilities.RestServicesUtil;

@Controller
public class MyBibleServicesController {

	private static final Logger logger = Logger
			.getLogger(MyBibleServicesController.class);
	private Random rndGenerator = new Random();

	private RestServicesUtil utils = RestServicesUtil.getInstance();

	private String resPath;

	private static String securitySeed = UUID.randomUUID().toString();

	private static long lastSSUpdate = System.currentTimeMillis();

	/* volatile configuration */
	private static String translationFileName;

	public MyBibleServicesController() {
		ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_RESOURCE_NAME);
		resPath = rb.getString(RESOURCE_PATH_KEY);
	}

	@RequestMapping(value = { LOGIN_PATH }, method = { RequestMethod.POST })
	public @ResponseBody
	LoginOutDTO login(@RequestBody LoginInDTO loginInfo,
			HttpServletRequest request) throws ServiceException {
		updateSecuritySeed();
		if (loginInfo == null || loginInfo.getId() == null
				|| loginInfo.getVersion() == null) {
			return new LoginOutDTO(0, ERROR_CODE_INVALID_USER);
		}
		int result = rndGenerator.nextInt();
		int errCode = ERROR_CODE_NO_ERROR;
		HttpSession session = request.getSession();
		Integer value = (Integer) session.getAttribute(SESSION_ATTRIBUTE_TOKEN);
		String userID = (String) session.getAttribute(SESSION_ATTRIBUTE_USERID);
		String version = (String) session
				.getAttribute(SESSION_ATTRIBUTE_VERSION);
		if (value != null && userID != null && version != null
				&& userID.equals(loginInfo.getId())
				&& version.equals(loginInfo.getVersion())) {
			result = value;
		} else {
			AuditUtils.getInstance().appendContents(
					AuditUtils.EVENT_TYPE_LOGIN, new Date(), loginInfo.getId(),
					loginInfo.getVersion(), null);
			session.setAttribute(SESSION_ATTRIBUTE_TOKEN, result);
			session.setAttribute(SESSION_ATTRIBUTE_USERID, loginInfo.getId());
			session.setAttribute(SESSION_ATTRIBUTE_VERSION,
					loginInfo.getVersion());
		}
		return new LoginOutDTO(result, errCode);
	}

	private String getTranslationFileName() {
		if (translationFileName != null) {
			File pFile = new File(resPath + translationFileName);
			if (pFile != null && pFile.isFile()) {
				return translationFileName;
			}
		}
		return TRANSLATION_LIST_FILENAME;
	}

	@RequestMapping(value = { GET_TRANSLATIONS_PATH }, method = { RequestMethod.POST })
	public @ResponseBody
	TranslationListDTO getTranslations(HttpServletRequest request)
			throws ServiceException {
		updateSecuritySeed();
		TranslationListDTO result = new TranslationListDTO();
		List<TranslationDTO> trs = new ArrayList<TranslationDTO>();
		String fileName = resPath + getTranslationFileName();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					if (line != null && !line.startsWith("#")) {
						String[] data = line.split(",");
						if (data != null && data.length >= 5) {
							trs.add(new TranslationDTO(data[0], data[1],
									data[2], data[3], data[4]));
						}
					}
				}
			} finally {
				br.close();
			}
		} catch (Exception e) {
			return new TranslationListDTO(ERROR_CODE_ERROR_READING_FILE, null);
		}
		if (trs != null && trs.size() > 0) {
			result.setTranslations((TranslationDTO[]) trs
					.toArray(new TranslationDTO[trs.size()]));
			return result;
		} else {
			return new TranslationListDTO(ERROR_CODE_ERROR_READING_FILE, null);
		}
	}

	/**
	 * Termina la sesion del usuario actual.
	 */
	@RequestMapping(value = { LOGOUT_PATH }, method = { RequestMethod.POST })
	public @ResponseBody
	String logout(HttpServletRequest req) {
		logger.debug("Entrando a URL " + LOGOUT_PATH);
		HttpSession session = req.getSession();
		if (session != null) {
			logger.info("Ejecutado logout.");
			session.invalidate();
		}
		return "";
	}

	/**
	 * Retorna una imagen en el Stream del body. Tiene en cuenta el tama�o
	 * solicitado.
	 * 
	 * @param mediaId
	 *            Identificador �nico del media.
	 * @param sizeId
	 *            Tipo del tamano del media solicitado.
	 * @return Informaci�n adicional del media.
	 */
	@RequestMapping(value = { GET_BIBLE_DATA_PATH }, method = { RequestMethod.POST })
	public void getBibleData(@PathVariable(PARAMETER_ID) String id,
			@PathVariable(PATH_PARAMETER_BIBLEID) String bibleId,
			HttpServletRequest request, HttpServletResponse response)
			throws ServiceException, IOException {
		logger.debug("Entrando a URL " + GET_BIBLE_DATA_PATH + " - bibleId:"
				+ bibleId);
		updateSecuritySeed();
		UserDTO user = utils.validateUser(request, response, id);
		boolean sendOK = false;
		if (user != null && user.getResultID() == ERROR_CODE_NO_ERROR) {
			logger.debug("obtener bible '" + bibleId);
			AuditUtils.getInstance().appendContents(
					AuditUtils.EVENT_TYPE_DOWNLOAD, new Date(), user.getUserId(),
					user.getVersion(), bibleId);
			if (bibleId != null && bibleId.length() > 0) {
				bibleId = bibleId.trim();
				String fileName = resPath + FILE_BIBLE_PREFIX + bibleId
						+ FILE_BIBLE_SUFIX;
				if (fileName != null) {
					try {
						OutputStream os = response.getOutputStream();
						FileInputStream resFile = new FileInputStream(fileName);
						try {
							CommonUtilities.copyStream(resFile, os);
						} finally {
							resFile.close();
							os.close();
						}
						sendOK = true;
						logger.debug("BIBLE retornada correctamente " + bibleId);
					} catch (Exception e) {
						logger.debug("GetBible " + bibleId + " ERROR: "
								+ e.getMessage());
						sendOK = false;
					}
				}
			}
			if (!sendOK) {
				logger.debug("No se ha podido enviar la biblia [" + bibleId
						+ "]");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
	}

	@RequestMapping(value = { PUT_CONFIGURATION_PATH }, method = { RequestMethod.PUT })
	public @ResponseBody
	ResultInfoDTO putConfiguration(@PathVariable(PARAMETER_ID) String id,
			@RequestBody ServerConfigurartionData serverConfigdata,
			HttpServletRequest request) {
		ResultInfoDTO result = new ResultInfoDTO();
		if (validateID(id) && serverConfigdata != null) {
			if (serverConfigdata.getTransationlistid() != null
					&& !serverConfigdata.getTransationlistid().isEmpty()) {
				translationFileName = serverConfigdata.getTransationlistid();
			} else {
				translationFileName = null;
			}
		} else {
			result.setResultID(ERROR_CODE_ACCESS_DENIED);
			result.setResultDetails("Access denied.");
		}
		updateSecuritySeed();
		return result;
	}

	private boolean validateID(String id) {
		return id != null && id.equals(String.valueOf(("ss:"+securitySeed).hashCode()));
	}

	/**
	 * Retorna el estado del servidor y conexion a la base de datos.
	 * 
	 * @return Estado del servidor como cadena.
	 */
	@RequestMapping(value = { STATUS_PATH }, method = { RequestMethod.GET,
			RequestMethod.POST })
	public @ResponseBody
	StatusDTO getStatus() {
		// Probar usando "http://<host>:<port>/MyBibleService/services/status"
		updateSecuritySeed();
		StatusDTO result = new StatusDTO();
		result.setAppName(MYBIBLE_APP_NAME);
		result.setAppVersion(MYBIBLE_APP_VERSION);
		result.setBibleServerVersion(BIBLE_SERVER_VERSION);
		result.setSecuritySeed(securitySeed);
		try {
			result.setFileOpts(
					getTranslationFileName().trim().replaceAll("translations", "").replaceAll("props", ""));			
		} catch (Exception e) {
			//NADA
		}
		try {
			File resDir = new File(resPath);
			if (!resDir.exists() || !resDir.isDirectory()) {
				throw new RuntimeException("Directorio de recursos inv�lido.");
			}
			result.setStatus(ERROR_CODE_NO_ERROR);
			result.setStatusDescription("OK");
			try {
				AuditUtils.getInstance().testDB();
				result.setStatus(ERROR_CODE_NO_ERROR);
				result.setStatusDescription("OK");
			} catch (Exception e) {
				result.setStatus(ERROR_CODE_DATABASE_ERROR);
				result.setStatusDescription("ERROR DB:" + e.getMessage());
			}
		} catch (Exception e) {
			result.setStatus(ERROR_CODE_UNKNOWN);
			result.setStatusDescription("ERROR " + e.getMessage());
		}
		return result;
	}
	
	@RequestMapping(value = { GET_STATISTICS_PATH }, method = { RequestMethod.GET,
			RequestMethod.POST })
	public @ResponseBody
	StatisticsDTO getStatistics(@RequestBody StatisticsInDTO filterInfo, HttpServletRequest request, HttpServletResponse response) {
		if (filterInfo == null || filterInfo.getToken() == null || !filterInfo.getToken().equals("servertokenvalue")) {
			return new StatisticsDTO(ERROR_CODE_ACCESS_DENIED, "ACCESS_DENIED");
		}
		return AuditUtils.getInstance().getStatistics(filterInfo);
	}
	
	
	@RequestMapping(value = { "/loadhistory" }, method = { RequestMethod.GET,
			RequestMethod.POST })
	public @ResponseBody
	String loadHistory() {
		// Probar usando "http://<host>:<port>/MyBibleService/services/status"
		updateSecuritySeed();
		return "NO ACCESS"; //Ya no lo hacemos mas -- AuditUtils.getInstance().appendHistoryContents();
	}

	private static void updateSecuritySeed() {
		if ((System.currentTimeMillis() - lastSSUpdate) > 3600000 * 2 /* 2 horas */) {
			securitySeed = UUID.randomUUID().toString();
			lastSSUpdate = System.currentTimeMillis();
		}
	}

}
