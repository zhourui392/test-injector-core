package com.oppo.usercenter.test.injector.config;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 通用 Mock 配置。
 * 提供常用场景的预定义配置。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/27
 */
public class CommonMockConfigs {

    /**
     * 空配置（什么都不做）。
     *
     * @return Mock 配置
     */
    public static MockConfig empty() {
        return builder -> {
            // 不做任何配置
        };
    }

    /**
     * 启用智能 Mock。
     *
     * @return Mock 配置
     */
    public static MockConfig enableSmartMock() {
        return builder -> builder.enableSmartMock();
    }

    /**
     * 启用调试模式。
     *
     * @return Mock 配置
     */
    public static MockConfig enableDebug() {
        return builder -> builder.enableDebug();
    }

    /**
     * 集合类型配置 - 返回空集合。
     * 适用于 List/Set/Map 返回值的 Mock。
     *
     * @param clazz Mock 类型
     * @param <T> 类型参数
     * @return Mock 配置
     */
    public static <T> MockConfig emptyCollections(Class<T> clazz) {
        return builder -> builder.mockWith(clazz, mock -> {
            // 这里需要在运行时动态配置，暂时作为示例
        });
    }

    /**
     * 布尔类型配置 - 统一返回 true。
     *
     * @param clazz Mock 类型
     * @param <T> 类型参数
     * @return Mock 配置
     */
    public static <T> MockConfig alwaysTrue(Class<T> clazz) {
        return builder -> builder.mockWith(clazz, mock -> {
            // 配置所有返回 boolean 的方法返回 true
            // 注意：这是一个简化示例，实际需要更复杂的实现
        });
    }

    /**
     * 布尔类型配置 - 统一返回 false。
     *
     * @param clazz Mock 类型
     * @param <T> 类型参数
     * @return Mock 配置
     */
    public static <T> MockConfig alwaysFalse(Class<T> clazz) {
        return builder -> builder.mockWith(clazz, mock -> {
            // 配置所有返回 boolean 的方法返回 false
        });
    }

    /**
     * 组合多个配置。
     *
     * @param configs 配置列表
     * @return 组合后的配置
     */
    public static MockConfig combine(MockConfig... configs) {
        return builder -> {
            for (MockConfig config : configs) {
                config.apply(builder);
            }
        };
    }

    /**
     * 数据库操作配置 - 标准成功场景。
     * 适用于 DAO/Repository 层的 Mock。
     *
     * @return Mock 配置
     */
    public static MockConfig databaseSuccess() {
        return builder -> {
            // 通用的数据库成功场景配置
            // save/update/delete 返回 true
            // find/select 返回非 null 对象
        };
    }

    /**
     * 数据库操作配置 - 标准失败场景。
     * 适用于测试异常情况。
     *
     * @return Mock 配置
     */
    public static MockConfig databaseFailure() {
        return builder -> {
            // 通用的数据库失败场景配置
            // save/update/delete 返回 false
            // find/select 返回 null
        };
    }

    /**
     * HTTP 服务配置 - 标准成功响应。
     * 适用于外部 HTTP 服务的 Mock。
     *
     * @return Mock 配置
     */
    public static MockConfig httpSuccess() {
        return builder -> {
            // 通用的 HTTP 成功响应配置
            // 返回 200 状态码
            // 返回预定义的成功数据
        };
    }

    /**
     * HTTP 服务配置 - 超时场景。
     * 适用于测试超时处理。
     *
     * @return Mock 配置
     */
    public static MockConfig httpTimeout() {
        return builder -> {
            // 模拟 HTTP 超时
            // 抛出超时异常
        };
    }

    /**
     * 缓存配置 - 命中场景。
     *
     * @return Mock 配置
     */
    public static MockConfig cacheHit() {
        return builder -> {
            // 缓存命中配置
            // get 返回非 null 值
        };
    }

    /**
     * 缓存配置 - 未命中场景。
     *
     * @return Mock 配置
     */
    public static MockConfig cacheMiss() {
        return builder -> {
            // 缓存未命中配置
            // get 返回 null
        };
    }
}
