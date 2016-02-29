package com.mcore.mybible.services.webservices.rest.utilities;

import static com.mcore.mybible.common.utilities.CommonErrorCodes.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.mcore.mybible.common.dto.UserDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonUtilities;

public class RestServicesUtil {

	private static final Logger logger = Logger
			.getLogger(RestServicesUtil.class);

	private static RestServicesUtil instance = new RestServicesUtil();

	public static RestServicesUtil getInstance() {
		return instance;
	}

	private RestServicesUtil() {
	}

	/**
	 * Valida que un usuario este correctamente logado en la sesión.
	 * 
	 * @return Datos del usuario sí el usuario es correcto, en caso contrario
	 *         genera un código de error.
	 */
	public UserDTO validateUser(HttpServletRequest req,
			HttpServletResponse res, String clientToken) {
		HttpSession session = req.getSession();
		Object userID = session
				.getAttribute(CommonConstants.SESSION_ATTRIBUTE_USERID);
		Object internalID = session
				.getAttribute(CommonConstants.SESSION_ATTRIBUTE_TOKEN);
		Object version = session
				.getAttribute(CommonConstants.SESSION_ATTRIBUTE_VERSION);
		int iId = (internalID instanceof Integer ? (Integer) internalID : 0);
		String cfmNumber = CommonUtilities.cfm(iId, (String)userID, (String)version);
		UserDTO result = new UserDTO(iId,
				(userID instanceof String ? (String) userID : null),
				(version instanceof String? (String) version : null));
		result.setResultID(ERROR_CODE_NO_ERROR);
		if (result.getToken() == 0 || result.getUserId() == null
				|| internalID == null || clientToken == null
				|| !clientToken.equals(cfmNumber)) {
			logger.debug("Usuario invalido - userID: " + userID + ", version: " + version);
			result.setResultID(ERROR_CODE_INVALID_USER_SESSION);
			session.invalidate();
		}
		return result;
	}

	/**
	 * Utilidad: Verifica sí una cadena esta vacía.
	 */
	public boolean isStringEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	
	public static void main(String[] args) {
		try {
			System.err.println(CommonUtilities.getMD5FromFile("d:\\Temp\\bblrepo\\B_NVI.zip"));
			System.err.println(CommonUtilities.getMD5FromFile("d:\\Temp\\bblrepo\\B_KJV.zip"));
			System.err.println(CommonUtilities.getMD5FromFile("d:\\Temp\\bblrepo\\B_RVA.zip"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
