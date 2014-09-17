package com.mcore.mybible.services.client;

import com.mcore.mybible.common.service.ICommonService;

public interface IBusinessDelegate extends ICommonService {

	public void setSessionListener(ISessionListener listener);
	
}
