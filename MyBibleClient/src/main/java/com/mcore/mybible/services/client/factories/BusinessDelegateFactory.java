package com.mcore.mybible.services.client.factories;

import java.util.Map;

import com.mcore.mybible.services.client.IBusinessDelegate;
import com.mcore.mybible.services.client.ServiceClientBusinessDelegate;
import com.mcore.mybible.services.client.interceptors.BusinessDelegateInterceptor;

public class BusinessDelegateFactory {
	
	public static IBusinessDelegate getNewServiceClient() {
		return getNewServiceClient(null);
	}
	
	public static IBusinessDelegate getNewServiceClient(Map context) {
		IBusinessDelegate service = (IBusinessDelegate)
				java.lang.reflect.Proxy.newProxyInstance(BusinessDelegateFactory.class.getClassLoader(),
				                                         new Class[] { IBusinessDelegate.class },
				                                         new BusinessDelegateInterceptor(
				                                        		 new ServiceClientBusinessDelegate(context)));
		
		return service;
	}

}
