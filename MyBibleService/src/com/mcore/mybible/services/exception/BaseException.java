package com.mcore.mybible.services.exception;

public class BaseException extends Exception {

	private static final long serialVersionUID = 918207958432440118L;
	
	private int errorId = 0;

	public BaseException() {
		super();
	}

	public BaseException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(String message) {
		super(message);
	}
	
	public BaseException(int errorId, String message) {
		super(message);
		this.errorId = errorId;
	}

	public BaseException(Throwable cause) {
		super(cause);
	}
	
	public BaseException(int errorId, Throwable cause) {
		super(cause);
		this.errorId = errorId;
	}
	
	public int getErrorId() {
		if (errorId == 0 && getCause() instanceof BaseException) {
			return ((BaseException)getCause()).errorId;
		}
		return errorId;
	}

}
