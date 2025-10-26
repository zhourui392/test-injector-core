package com.oppo.usercenter.test.injector;

/**
 * Mock 配置器函数式接口。
 * 用于配置 Mock 对象的行为。
 *
 * @param <T> Mock 对象类型
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
@FunctionalInterface
public interface MockConfigurator<T> {

    /**
     * 配置 Mock 对象。
     *
     * @param mock Mock 对象
     */
    void configure(T mock);
}
