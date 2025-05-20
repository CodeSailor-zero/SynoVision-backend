package com.sean.synovision.utill;

import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;

/**
 * @author sean
 * @Date 2025/57/20
 * 异常处理类
 */
public class ThrowUtill {
    /**
     * 条件成立抛出异常
     *
     * @param condition        条件
     * @param bussinessException 异常
     */
    public static void throwIf(boolean condition, BussinessException bussinessException) {
        if (condition) {
            throw bussinessException;
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BussinessException(errorCode));
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode,String message) {
        throwIf(condition, new BussinessException(errorCode,message));
    }
}
