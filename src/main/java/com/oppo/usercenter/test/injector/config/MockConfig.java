package com.oppo.usercenter.test.injector.config;

import com.oppo.usercenter.test.injector.TestInjector;

/**
 * Mock 配置接口。
 * 用于定义可复用的 Mock 配置。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/27
 */
@FunctionalInterface
public interface MockConfig {

    /**
     * 应用配置到 Builder。
     *
     * @param builder TestInjector.Builder 实例
     */
    void apply(TestInjector.Builder builder);

    /**
     * 组合多个配置。
     *
     * @param other 另一个配置
     * @return 组合后的配置
     */
    default MockConfig andThen(MockConfig other) {
        return builder -> {
            this.apply(builder);
            other.apply(builder);
        };
    }
}
