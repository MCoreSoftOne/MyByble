package com.mcore.mybible.services.exception;

public class ServiceException extends BaseException {

	private static final long serialVersionUID = 8535802426188866051L;

	public ServiceException() {
		super();
	}

	public ServiceException(int errorId, String message) {
		super(errorId, message);
	}

	public ServiceException(int errorId, Throwable cause) {
		super(errorId, cause);
	}

	public ServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	

}
