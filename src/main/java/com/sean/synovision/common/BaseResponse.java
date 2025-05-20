package com.sean.synovision.common;

import com.sean.synovision.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/01/20
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code,data,"");
    }
    public BaseResponse(ErrorCode errorCode) {
       this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
