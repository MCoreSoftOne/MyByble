package com.mcore.mybible.common.service;

import java.io.OutputStream;

import com.mcore.mybible.common.dto.LoginInDTO;
import com.mcore.mybible.common.dto.LoginOutDTO;
import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.dto.ServerConfigurartionData;
import com.mcore.mybible.common.dto.StatisticsDTO;
import com.mcore.mybible.common.dto.StatisticsInDTO;
import com.mcore.mybible.common.dto.StatusDTO;
import com.mcore.mybible.common.dto.TranslationListDTO;

/**
 * Interface necesaria de cliente para comunicación con el servicio backend.
 * 
 * @author Mario
 * 
 */
public interface ICommonService {

	
	/**
	 * Realiza la autenticacion de un Usuario.
	 * 
	 * @param loginInfo
	 *            Información y credenciales del usuario que inicia sesión.
	 * @return Sí esta autorizado retorna un token de identificacion diferente
	 *         de cero. Cero sí ha ocurrido un error. El POJO de retorna incluye
	 *         información adicional del codigo error producido y detalles
	 *         adicionales.
	 */
	public LoginOutDTO login(LoginInDTO loginInfo);

	/**
	 * Termina la sesion del usuario actual.
	 */
	public void logout();

	
	/**
	 * Estableces los datos de un media en el Stream indicado. Tiene en cuenta
	 * el tamaño solicitado.
	 * 
	 * @param bibleId
	 *            Identificador único de la traducción.
	 * @param dataOut
	 *            Flujo de bytes donde quedará almacenado el media.
	 * @return Información adicional del media.
	 */
	public ResultInfoDTO getBibleData(String bibleId, OutputStream dataOut);
	
	public TranslationListDTO getTranslations();
	
	public StatisticsDTO getStatistics(StatisticsInDTO filterInfo);
	
	public StatusDTO getStatus();
	
	public ResultInfoDTO putConfiguration(String securityId, ServerConfigurartionData serverConfigdata);

}
