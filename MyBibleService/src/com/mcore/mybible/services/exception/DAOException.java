package com.mcore.mybible.services.exception;

public class DAOException extends BaseException {

	private static final long serialVersionUID = 5290454285724934257L;

	public DAOException() {
		super();
	}

	public DAOException(int errorId, String message) {
		super(errorId, message);
	}

	public DAOException(int errorId, Throwable cause) {
		super(errorId, cause);
	}

	public DAOException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public DAOException(String message) {
		super(message);
	}

	public DAOException(Throwable cause) {
		super(cause);
	}


}
