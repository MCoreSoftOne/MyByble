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
 * Interface necesaria de cliente para comunicacion con el servicio backend.
 * 
 * @author Mario
 * 
 */
public interface ICommonService {

	
	/**
	 * Realiza la autenticacion de un Usuario.
	 * 
	 * @param loginInfo
	 *            Informacion y credenciales del usuario que inicia sesion.
	 * @return Se esta autorizado retorna un token de identificacion diferente
	 *         de cero. Cero se ha ocurrido un error. El POJO de retorna incluye
	 *         informacion adicional del codigo error producido y detalles
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
	 *            Identificador unico de la traduccion.
	 * @param dataOut
	 *            Flujo de bytes donde quedara almacenado el media.
	 * @return Informacion adicional del media.
	 */
	public ResultInfoDTO getBibleData(String bibleId, OutputStream dataOut);
	
	public TranslationListDTO getTranslations();
	
	public StatisticsDTO getStatistics(StatisticsInDTO filterInfo);
	
	public StatusDTO getStatus();
	
	public ResultInfoDTO putConfiguration(String securityId, ServerConfigurartionData serverConfigdata);

}
