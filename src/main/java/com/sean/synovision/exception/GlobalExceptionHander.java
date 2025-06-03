package com.sean.synovision.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.sean.synovision.common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author sean
 * @Date 2025/06/20
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHander {

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> NotLoginExceptionHandler(NotLoginException e){
        log.error("BussinessException " + e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR,e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> NotPermissionExceptionHandler(NotPermissionException e){
        log.error("BussinessException " + e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR,e.getMessage());
    }

    @ExceptionHandler(BussinessException.class)
    public BaseResponse<?> BussinessExceptionHandler(BussinessException e){
        log.error("BusinessException: {}, Message: {}, StackTrace: {}",
                e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
//        log.error("BussinessException " + e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> RuntimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException: {}, Message: {}, StackTrace: {}",
                e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }

}
