package com.mcore.mybible.services.client;

import com.mcore.mybible.services.tdos.ServiceAuthorizationDTO;


public interface ISessionListener {

	public void sessionEstablished(ServiceAuthorizationDTO authorization);
	
	public void sessionLost();
	
}
