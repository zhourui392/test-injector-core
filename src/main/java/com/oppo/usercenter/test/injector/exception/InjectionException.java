package com.oppo.usercenter.test.injector.exception;

/**
 * 依赖注入异常。
 * 当依赖注入过程中发生错误时抛出此异常。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class InjectionException extends RuntimeException {

    /**
     * 构造依赖注入异常。
     *
     * @param message 异常消息
     */
    public InjectionException(String message) {
        super(message);
    }

    /**
     * 构造依赖注入异常。
     *
     * @param message 异常消息
     * @param cause 原始异常
     */
    public InjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
