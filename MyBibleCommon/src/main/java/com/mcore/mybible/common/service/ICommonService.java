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
 * Interface necesaria de cliente para comunicaci�n con el servicio backend.
 * 
 * @author Mario
 * 
 */
public interface ICommonService {

	
	/**
	 * Realiza la autenticacion de un Usuario.
	 * 
	 * @param loginInfo
	 *            Informaci�n y credenciales del usuario que inicia sesi�n.
	 * @return S� esta autorizado retorna un token de identificacion diferente
	 *         de cero. Cero s� ha ocurrido un error. El POJO de retorna incluye
	 *         informaci�n adicional del codigo error producido y detalles
	 *         adicionales.
	 */
	public LoginOutDTO login(LoginInDTO loginInfo);

	/**
	 * Termina la sesion del usuario actual.
	 */
	public void logout();

	
	/**
	 * Estableces los datos de un media en el Stream indicado. Tiene en cuenta
	 * el tama�o solicitado.
	 * 
	 * @param bibleId
	 *            Identificador �nico de la traducci�n.
	 * @param dataOut
	 *            Flujo de bytes donde quedar� almacenado el media.
	 * @return Informaci�n adicional del media.
	 */
	public ResultInfoDTO getBibleData(String bibleId, OutputStream dataOut);
	
	public TranslationListDTO getTranslations();
	
	public StatisticsDTO getStatistics(StatisticsInDTO filterInfo);
	
	public StatusDTO getStatus();
	
	public ResultInfoDTO putConfiguration(String securityId, ServerConfigurartionData serverConfigdata);

}
