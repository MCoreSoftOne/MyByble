package com.mcore.mybible.services.utils;

import java.util.UUID;

public class GeneralUtils {

	public static String getUniqueToken() {
		String result = UUID.randomUUID().toString();
		return result.replaceAll("-", "").toUpperCase();
	}
	
}
