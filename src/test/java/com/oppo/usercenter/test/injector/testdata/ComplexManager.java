package com.oppo.usercenter.test.injector.testdata;

/**
 * 复杂管理器类（用于测试复杂依赖链）。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class ComplexManager {

    private final SimpleService simpleService;
    private final SimpleManager simpleManager;
    private final ConfigService configService;

    public ComplexManager(SimpleService simpleService, SimpleManager simpleManager, ConfigService configService) {
        this.simpleService = simpleService;
        this.simpleManager = simpleManager;
        this.configService = configService;
    }

    public String execute() {
        String config = configService.getConfig();
        String data = simpleService.getData();
        String processed = simpleManager.process();
        return String.format("Config: %s, Data: %s, Processed: %s", config, data, processed);
    }

    public SimpleService getSimpleService() {
        return simpleService;
    }

    public SimpleManager getSimpleManager() {
        return simpleManager;
    }

    public ConfigService getConfigService() {
        return configService;
    }
}
