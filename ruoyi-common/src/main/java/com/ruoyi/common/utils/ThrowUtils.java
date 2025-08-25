package com.ruoyi.common.utils;

import com.ruoyi.common.exception.ServiceException;

/**
 * 异常处理工具类
 *
 * @Author YY
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, String message) {
        throwIf(condition, new ServiceException(message));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, Integer errorCode, String message) {
        throwIf(condition, new ServiceException(message, errorCode));
    }
}
