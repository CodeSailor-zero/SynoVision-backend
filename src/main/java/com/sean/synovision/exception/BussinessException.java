package com.sean.synovision.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sean
 * @Date 2025/52/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BussinessException extends RuntimeException{
    private  int code;

    public BussinessException(int code,String message) {
        super(message);
        this.code = code;
    }

    public BussinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BussinessException(ErrorCode errorCode,String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
