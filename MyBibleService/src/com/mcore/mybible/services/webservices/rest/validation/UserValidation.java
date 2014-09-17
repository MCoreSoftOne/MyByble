package com.mcore.mybible.services.webservices.rest.validation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.mcore.mybible.common.dto.UserDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.services.exception.ValidationException;

public class UserValidation implements CommonErrorCodes {
	
	private static final Logger logger = Logger.getLogger(UserValidation.class);

	private static UserValidation instance = new UserValidation();

	public static UserValidation getInstance() {
		return instance;
	}

	private UserValidation() {
		
	}
	
	/**
	 * Comprueba si el usuario esta logueado o no.
	 * @return el usuario
	 * @throws ValidationException
	 */
	public UserDTO isLoggedIn(HttpServletRequest req, HttpServletResponse res, String id) throws ValidationException {
		logger.debug("Validando login de usuario...");
		UserDTO result = new UserDTO();
		result.setResultID(ERROR_CODE_NO_ERROR);
		HttpSession session = req.getSession();
		Object version 	= session.getAttribute(CommonConstants.SESSION_ATTRIBUTE_VERSION);
		Object userID 		= session.getAttribute(CommonConstants.SESSION_ATTRIBUTE_USERID);
		Object internalID 	= session.getAttribute(CommonConstants.SESSION_ATTRIBUTE_TOKEN);
		try {
			if (id == null || !id.equals(internalID.toString()) || internalID 	== null)
				throw new ValidationException(ERROR_CODE_INVALID_USER_SESSION);
			result.setUserId((version instanceof String && version.toString().length() > 0) 
					? version.toString() 
					: null);
			result.setToken((userID instanceof Integer) 
					? (Integer) userID 
					: 0);
			if (result.getToken() <= 0 || result.getUserId() == null)
				throw new ValidationException(ERROR_CODE_INVALID_USER_SESSION);
		} catch (ValidationException e) {
			logger.debug("Usuario invalido - version: " + version + ", userID: " + userID);
			result.setResultID(e.getErrorCode());
			session.invalidate();
			throw e; //TODO debe haber una mejor forma de hacer esto
		}
		return result;
	}
}
