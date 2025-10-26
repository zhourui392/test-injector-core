package com.oppo.usercenter.test.injector.config.example;

import com.oppo.usercenter.test.injector.config.MockConfig;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 用户增长业务 Mock 配置示例。
 * 展示如何为特定业务场景定义配置。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/27
 */
public class UserGrowingMockConfigs {

    /**
     * 标准场景配置。
     * 适用于大多数正常流程测试。
     *
     * @return Mock 配置
     */
    public static MockConfig standard() {
        return builder -> {
            // 启用智能 Mock
            builder.enableSmartMock();

            // 配置示例：用户群组服务
            // builder.mockWith(UserGroupGatewayService.class, mock -> {
            //     when(mock.batchMatch(any(), any(), any(), any(), anyBoolean()))
            //         .thenReturn(Collections.emptyList());
            // });

            // 配置示例：计划配置管理器
            // builder.mockWith(PlanConfigManager.class, mock -> {
            //     when(mock.getPlanAudienceListFromCache(anyInt()))
            //         .thenReturn(Collections.emptyList());
            // });
        };
    }

    /**
     * 人群匹配成功场景。
     *
     * @return Mock 配置
     */
    public static MockConfig audienceMatchSuccess() {
        return builder -> {
            // 配置用户群组匹配成功
            // builder.mockWith(UserGroupGatewayService.class, mock -> {
            //     UsergroupMatchResult result = new UsergroupMatchResult();
            //     result.setMatched(true);
            //     when(mock.batchMatch(any(), any(), any(), any(), anyBoolean()))
            //         .thenReturn(Collections.singletonList(result));
            // });
        };
    }

    /**
     * 人群匹配失败场景。
     *
     * @return Mock 配置
     */
    public static MockConfig audienceMatchFailure() {
        return builder -> {
            // 配置用户群组匹配失败
            // builder.mockWith(UserGroupGatewayService.class, mock -> {
            //     UsergroupMatchResult result = new UsergroupMatchResult();
            //     result.setMatched(false);
            //     when(mock.batchMatch(any(), any(), any(), any(), anyBoolean()))
            //         .thenReturn(Collections.singletonList(result));
            // });
        };
    }

    /**
     * 黑名单场景 - 用户在黑名单中。
     *
     * @return Mock 配置
     */
    public static MockConfig inBlacklist() {
        return builder -> {
            // 配置黑名单检查返回 true
            // builder.mockWith(BlacklistManager.class, mock -> {
            //     when(mock.isInBlacklist(anyLong())).thenReturn(true);
            // });
        };
    }

    /**
     * 黑名单场景 - 用户不在黑名单中。
     *
     * @return Mock 配置
     */
    public static MockConfig notInBlacklist() {
        return builder -> {
            // 配置黑名单检查返回 false
            // builder.mockWith(BlacklistManager.class, mock -> {
            //     when(mock.isInBlacklist(anyLong())).thenReturn(false);
            // });
        };
    }

    /**
     * 组合配置：标准场景 + 匹配成功 + 不在黑名单。
     *
     * @return Mock 配置
     */
    public static MockConfig happyPath() {
        return standard()
                .andThen(audienceMatchSuccess())
                .andThen(notInBlacklist());
    }

    /**
     * 组合配置：标准场景 + 匹配失败。
     *
     * @return Mock 配置
     */
    public static MockConfig noMatch() {
        return standard()
                .andThen(audienceMatchFailure());
    }

    /**
     * 组合配置：标准场景 + 在黑名单。
     *
     * @return Mock 配置
     */
    public static MockConfig blockedByBlacklist() {
        return standard()
                .andThen(inBlacklist());
    }
}
