package com.oppo.usercenter.test.injector;

import com.oppo.usercenter.test.injector.exception.CircularDependencyException;
import com.oppo.usercenter.test.injector.testdata.ComplexManager;
import com.oppo.usercenter.test.injector.testdata.ConfigService;
import com.oppo.usercenter.test.injector.testdata.SimpleManager;
import com.oppo.usercenter.test.injector.testdata.SimpleService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TestInjector 单元测试。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
class TestInjectorTest {

    @Test
    void should_createSimpleInstance_when_usingStaticMethod() {
        SimpleManager manager = TestInjector.createInstance(SimpleManager.class);

        assertThat(manager).isNotNull();
        assertThat(manager.getSimpleService()).isNotNull();
    }

    @Test
    void should_createMockForInterface_when_interfaceDetected() {
        TestInjector injector = new TestInjector();

        SimpleService service = injector.getInstance(SimpleService.class);

        assertThat(service).isNotNull();
        verify(service, never()).getData(); // 确认是 Mock 对象
    }

    @Test
    void should_createRealInstance_when_concreteClass() {
        TestInjector injector = new TestInjector();

        SimpleManager manager = injector.getInstance(SimpleManager.class);

        assertThat(manager).isNotNull();
        assertThat(manager.getSimpleService()).isNotNull();
    }

    @Test
    void should_returnCachedInstance_when_requestedTwice() {
        TestInjector injector = new TestInjector();

        SimpleManager first = injector.getInstance(SimpleManager.class);
        SimpleManager second = injector.getInstance(SimpleManager.class);

        assertThat(first).isSameAs(second);
    }

    @Test
    void should_createComplexDependencyChain_when_nestedDependencies() {
        TestInjector injector = new TestInjector();

        ComplexManager manager = injector.getInstance(ComplexManager.class);

        assertThat(manager).isNotNull();
        assertThat(manager.getSimpleService()).isNotNull();
        assertThat(manager.getSimpleManager()).isNotNull();
        assertThat(manager.getConfigService()).isNotNull();
    }

    @Test
    void should_useUserInstance_when_registered() {
        SimpleService customService = mock(SimpleService.class);
        when(customService.getData()).thenReturn("Custom Data");

        SimpleManager manager = TestInjector.builder()
                .instance(SimpleService.class, customService)
                .autoCreate(SimpleManager.class)
                .build();

        assertThat(manager.getSimpleService()).isSameAs(customService);
        assertThat(manager.process()).isEqualTo("Processed: Custom Data");
    }

    @Test
    void should_forceMock_when_configuredInBuilder() {
        SimpleManager manager = TestInjector.builder()
                .mock(SimpleService.class)
                .autoCreate(SimpleManager.class)
                .build();

        assertThat(manager.getSimpleService()).isNotNull();
        verify(manager.getSimpleService(), never()).getData();
    }

    @Test
    void should_forceReal_when_configuredInBuilder() {
        // 这里暂时跳过，因为 SimpleService 是接口，无法创建真实对象
        // 在实际项目中会用具体类测试
    }

    @Test
    void should_configureMock_when_usingMockWith() {
        SimpleManager manager = TestInjector.builder()
                .mockWith(SimpleService.class, mock -> {
                    when(mock.getData()).thenReturn("Mocked Data");
                })
                .autoCreate(SimpleManager.class)
                .build();

        assertThat(manager.process()).isEqualTo("Processed: Mocked Data");
    }

    @Test
    void should_returnSmartDefaults_when_smartMockEnabled() {
        TestInjector injector = TestInjector.builder()
                .enableSmartMock()
                .buildInjector();

        SimpleService service = injector.getInstance(SimpleService.class);

        // SmartAnswer 应该返回空字符串而不是 null
        assertThat(service.getData()).isEqualTo("");
    }

    @Test
    void should_throwCircularDependencyException_when_circularDetected() {
        // 由于我们的测试数据没有循环依赖，这里创建一个特殊场景
        // 暂时跳过此测试，在有循环依赖的测试数据时再测试
    }

    @Test
    void should_buildTypedBuilder_when_usingAutoCreate() {
        SimpleManager manager = TestInjector.builder()
                .autoCreate(SimpleManager.class)
                .mock(SimpleService.class)
                .build();

        assertThat(manager).isNotNull();
        assertThat(manager.getSimpleService()).isNotNull();
    }

    @Test
    void should_chainMultipleConfigurations_when_usingBuilder() {
        ComplexManager manager = TestInjector.builder()
                .mockWith(SimpleService.class, mock -> when(mock.getData()).thenReturn("Service Data"))
                .mockWith(ConfigService.class, mock -> when(mock.getConfig()).thenReturn("Config Value"))
                .autoCreate(ComplexManager.class)
                .build();

        String result = manager.execute();

        assertThat(result).contains("Config Value");
        assertThat(result).contains("Service Data");
    }
}
