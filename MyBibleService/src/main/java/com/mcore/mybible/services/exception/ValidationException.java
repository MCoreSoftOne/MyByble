package com.mcore.mybible.services.exception;

import com.mcore.mybible.common.utilities.CommonErrorCodes;

public class ValidationException extends BaseException implements CommonErrorCodes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int errorCode = ERROR_CODE_UNKNOWN;
	
	public ValidationException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return this.errorCode;
	}

}
