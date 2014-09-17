package com.mcore.mybible.services.tests;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mcore.mybible.common.dto.DayStatisticDTO;
import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.common.dto.TranslationDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.services.client.IBusinessDelegate;
import com.mcore.mybible.services.client.factories.BusinessDelegateFactory;

public class MyBibleTest001 {

	private static final String userId = "897217368918276";
	private static final String version = "1.3";
	
	private static final String traslation = "RVA";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLogin() {
		IBusinessDelegate services = BusinessDelegateFactory.getNewServiceClient(getContext());
		LoginOutDTO login = services.login(new LoginInDTO(userId, version));
		assertNotNull("Error al invocar login: error de conexión ?", login);
		assertEquals("Error al invocar login: token invalido", CommonErrorCodes.ERROR_CODE_NO_ERROR, login.getResultID());
		assertTrue("Error al invocar login: token invalido", login.getId() !=0);
		int id01 = login.getId();
		login = services.login(new LoginInDTO(userId, version));
		assertNotNull("Error al invocar login: error de conexión ?", login);
		assertEquals("Error al invocar login: token invalido", CommonErrorCodes.ERROR_CODE_NO_ERROR, login.getResultID());
		assertTrue("Error al invocar login: token invalido", login.getId() !=0);
		assertEquals("Error al invocar login: no devuelve el mismo token", id01, login.getId());
	}
	
	@Test
	public void testGetTranslation() {
		IBusinessDelegate services = BusinessDelegateFactory.getNewServiceClient(getContext());
		LoginOutDTO login = services.login(new LoginInDTO(userId, version));
		assertNotNull("Error al invocar login: error de conexión ?", login);
		assertEquals("Error al invocar login: token invalido", CommonErrorCodes.ERROR_CODE_NO_ERROR, login.getResultID());
		assertTrue("Error al invocar login: token invalido", login.getId() !=0);
		TranslationListDTO translations = services.getTranslations();
		assertNotNull("No se pueden obtener las traducciones", translations);
		for (int i = 0; i < translations.getTranslations().length; i++) {
			TranslationDTO translationDTO = translations.getTranslations()[i];
			assertNotNull("La traducción " + i + " no puede ser vacia", translationDTO);
			assertNotNull("La traducción " + i + " no puede tener id nulo", translationDTO.getId());
			assertTrue("La traducción " + i + " no puede tener id vacio", translationDTO.getId().length() > 0);
		}
	}
	
	@Test
	public void testGetData() {
		IBusinessDelegate services = BusinessDelegateFactory.getNewServiceClient(getContext());
		LoginOutDTO login = services.login(new LoginInDTO(userId, version));
		assertNotNull("Error al invocar login: error de conexión ?", login);
		assertEquals("Error al invocar login: token invalido", CommonErrorCodes.ERROR_CODE_NO_ERROR, login.getResultID());
		assertTrue("Error al invocar login: token invalido", login.getId() !=0);
		try {
			FileOutputStream dataOut = new FileOutputStream("c:\\temp\\"+traslation+".zip");
			services.getBibleData(traslation, dataOut);
			dataOut.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetStats() {
		IBusinessDelegate services = BusinessDelegateFactory.getNewServiceClient(getContext());
		StatisticsDTO result = services.getStatistics(new StatisticsInDTO("servertokenvalue", 20));
		assertNotNull("Error al obtener estadisticas: error de conexión ?", result);
		assertNotNull("Error al obtener estadisticas", result.getDayStatistics());
		assertEquals("Error al obtener estadisticas: token invalido", CommonErrorCodes.ERROR_CODE_NO_ERROR, result.getResultID());
		for (Iterator<DayStatisticDTO> iterator = result.getDayStatistics().iterator(); iterator.hasNext();) {
			DayStatisticDTO daystat = iterator.next();
			System.err.println(daystat);
		}	
	}
	
	public Map getContext() {
		Map result = new HashMap();
		result.put(CommonConstants.SERVER_CONFIG_ALIAS_CONTEXT_KEY, CommonConstants.DEFAULT_SERVER_CONFIG_ALIAS);
		return null;
	}

}
