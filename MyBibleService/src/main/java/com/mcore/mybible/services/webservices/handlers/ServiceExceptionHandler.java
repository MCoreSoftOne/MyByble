package com.mcore.mybible.services.webservices.handlers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.mcore.mybible.common.dto.ResultInfoDTO;
import com.mcore.mybible.common.utilities.CommonConstants;
import com.mcore.mybible.common.utilities.CommonErrorCodes;
import com.mcore.mybible.services.exception.ServiceException;

@Controller
@ControllerAdvice
public class ServiceExceptionHandler {

   @ExceptionHandler(ServiceException.class)
   public ModelAndView handleException (ServiceException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/services" + CommonConstants.ERROR_PATH);
        int errCode = ex.getErrorId();
        if (errCode <= 0) {
        	errCode = CommonErrorCodes.ERROR_CODE_UNKNOWN;
        }
        modelAndView.addObject(CommonConstants.PARAMETER_ERROR_ID, errCode);
        modelAndView.addObject(CommonConstants.PARAMETER_ERROR_DETAILS, ex.getMessage());
        return modelAndView;
   }
   
   /**
	 * Maneja los errores de toda la aplicacion
	 * @return ResultInfoDTO con los detalles del error.
	 */
	@RequestMapping(value = { CommonConstants.ERROR_PATH }, method = { RequestMethod.POST })
	public @ResponseBody
	ResultInfoDTO errorInfo(HttpServletRequest request) {
		Integer id = (Integer)request.getAttribute(CommonConstants.PARAMETER_ERROR_ID);
		if (id == null) {
			id = CommonErrorCodes.ERROR_CODE_UNKNOWN;
		}
		String str = (String)request.getAttribute(CommonConstants.PARAMETER_ERROR_DETAILS);
		if (str == null) {
			str = "UNKNOWN";
		}
		return new ResultInfoDTO(id, str);
	}

}
