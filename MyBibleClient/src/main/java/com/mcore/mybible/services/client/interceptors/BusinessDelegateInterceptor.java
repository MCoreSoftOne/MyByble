package com.mcore.mybible.services.client.interceptors;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.mcore.mybible.common.dto.IdOutDTO;
import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.service.ICommonService;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.services.client.ISessionListener;
import com.mcore.mybible.services.client.ServiceClientBusinessDelegate;
import com.mcore.mybible.services.tdos.ServiceAuthorizationDTO;

public class BusinessDelegateInterceptor implements InvocationHandler, Serializable {
	
	private static final long serialVersionUID = 3849329702627762321L;

	private ServiceClientBusinessDelegate service;
	
	private static final String AUTHORIZATION_KEY = "interceptorToken";
	
	private static final String LOGIN_METHOD_NAME = "login";
	
	private static final String LOGOUT_METHOD_NAME = "logout";
	
	private static final String SET_SESSION_LISTENER_METHOD_NAME = "setSessionListener";
	
	private static final String[] METHOD_NOREQUIRE_AUTHORIZATION = new String[] { "login","isUserLoggedIn","isSessionAlive", "getStatistics", "getStatus", "putConfiguration" };
	
	private ISessionListener listener;
	
	private ServiceAuthorizationDTO authorization;
	
	private Map context;
	
	public BusinessDelegateInterceptor(ServiceClientBusinessDelegate service) {
		this.service = service;
		context = service.getContext();
		authorization = getAuthorization();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		boolean isSetListener = method.getName().equals(SET_SESSION_LISTENER_METHOD_NAME);
		if (isSetListener) {
			setSessionListener((ISessionListener)args[0]);
			return null;
		}
		if (!validateAuthorization(method)) {
			ResultInfoDTO returnInstance = returnTypeInstance(method);
			if (returnInstance != null) {
				returnInstance.setResultID(CommonErrorCodes.ERROR_CODE_INVALID_USER_SESSION);
				return returnInstance;
			}
		}
		boolean isLoginMethod = method.getName().equals(LOGIN_METHOD_NAME);
		if (isLoginMethod) {
			if (args[0] instanceof LoginInDTO) {				
				LoginInDTO userInfo = (LoginInDTO)args[0];
				setAuthorization(new ServiceAuthorizationDTO(userInfo.getId(), userInfo.getVersion()));
				int errId = service.initServerNameIfDont(true);
				if (errId != CommonErrorCodes.ERROR_CODE_NO_ERROR) {
					ResultInfoDTO returnInstance = returnTypeInstance(method);
					if (returnInstance != null) {
						returnInstance.setResultID(errId);
						return returnInstance;
					}
				}
			}
		} else if (method.getName().equals(LOGOUT_METHOD_NAME)) {
			setAuthorization(null);
		}
		int errId = service.initServerNameIfDont();
		if (errId != CommonErrorCodes.ERROR_CODE_NO_ERROR) {
			ResultInfoDTO returnInstance = returnTypeInstance(method);
			if (returnInstance != null) {
				returnInstance.setResultID(errId);
				return returnInstance;
			}
		}
		Object result = method.invoke(service, args);
		if (isLoginMethod && result instanceof IdOutDTO) {
			int loginId = ((IdOutDTO)result).getId();
			if (loginId > 0) {
				authorization.setToken(loginId);
				sessionEstablished(authorization);
			} else {
				setAuthorization(null);
			}
		}
		if (!validateResult(result, method)) {
			if (tryToRecoverSession((ICommonService)proxy)) {
				result = method.invoke(service, args);
			} else {
				sessionLost();
				setAuthorization(null);
			}
		}
		return result;
	}
	
	private ResultInfoDTO returnTypeInstance(Method method) {
		if (method.getReturnType() != null) {
			try {
				Object obj = method.getReturnType().newInstance();
				if (obj instanceof ResultInfoDTO) {
					return (ResultInfoDTO)obj;
				}
			} catch (Exception e) {
				// Se ignora el error.
			}
		}
		return null;
	}
	
	private boolean tryToRecoverSession(ICommonService proxy) {
		if (authorization != null) {
			LoginOutDTO idOut = proxy.login(new LoginInDTO(authorization.getUsername(), authorization.getCredentials()));
			return (idOut != null && idOut.getId() > 0);
		}
		return false;
	}
	
	private boolean validateResult(Object result, Method method) {
		boolean isException = isExceptionMethod(method);
		if (!isException && result instanceof ResultInfoDTO) {
			if (((ResultInfoDTO)result).getResultID() == CommonErrorCodes.ERROR_CODE_INVALID_USER_SESSION) {
				return false;
			}
		}
		return true;
	}
	
	private boolean validateAuthorization(Method method) {
		boolean isException = isExceptionMethod(method);
		return isException || authorization != null;
	}
	
	private boolean isExceptionMethod(Method method) {
		String methodName = method.getName();
		for (int i = 0; i < METHOD_NOREQUIRE_AUTHORIZATION.length; i++) {
			if (METHOD_NOREQUIRE_AUTHORIZATION[i].equals(methodName)) {
				return true;
			}
		}
		return false;
	}
	
	public void setSessionListener(ISessionListener listener) {
		this.listener = listener;
	}
	
	public void sessionEstablished(ServiceAuthorizationDTO authorization) {
		if (listener != null) {
			listener.sessionEstablished(authorization);
		}
	}
	
	public void sessionLost() {
		if (listener != null) {
			listener.sessionLost();
		}
	}

	public ServiceAuthorizationDTO getAuthorization() {
		return (ServiceAuthorizationDTO)context.get(AUTHORIZATION_KEY);
	}

	public void setAuthorization(ServiceAuthorizationDTO authorization) {
		this.authorization = authorization;
		context.put(AUTHORIZATION_KEY, authorization);
	}

}
