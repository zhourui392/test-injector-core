package com.oppo.usercenter.test.injector.testdata;

/**
 * 简单管理器类（用于测试）。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class SimpleManager {

    private final SimpleService simpleService;

    public SimpleManager(SimpleService simpleService) {
        this.simpleService = simpleService;
    }

    public String process() {
        return "Processed: " + simpleService.getData();
    }

    public SimpleService getSimpleService() {
        return simpleService;
    }
}
