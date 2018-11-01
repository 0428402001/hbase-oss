package com.salmon.oss.controller.exception;

import com.salmon.oss.common.api.ApiResponse;
import com.salmon.oss.core.ConstantInfo;
import com.salmon.oss.core.OssException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class OssExceptionHandler {

    /**
     * 异常处理.
     */
    @ExceptionHandler
    @ResponseBody
    public ApiResponse exceptionHandle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        if (OssException.class.isAssignableFrom(ex.getClass())) {
            OssException ossException = (OssException) ex;
            if (ossException.errorCode() == ConstantInfo.ERROR_PERMISSION_DENIED) {
                response.setStatus(status.value());
            } else {
                response.setStatus(status.value());
            }
            return ApiResponse.ofMessage(ossException.errorCode(), ossException.errorMessage());
        } else {
            response.setStatus(status.value());
            return ApiResponse.ofMessage(status.value(), status.getReasonPhrase() + " detail info :" + ex.getMessage());
        }
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
