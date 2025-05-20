package com.sean.synovision.exception;

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
    @ExceptionHandler(BussinessException.class)
    public BaseResponse<?> bussinessExceptionHandler(BussinessException e){
        log.error("BussinessException " + e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> RuntimeExceptionHandler(RuntimeException e){
        log.error("BussinessException " + e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }

}
