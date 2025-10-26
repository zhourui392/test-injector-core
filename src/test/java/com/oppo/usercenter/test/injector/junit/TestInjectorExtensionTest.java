package com.oppo.usercenter.test.injector.junit;

import com.oppo.usercenter.test.injector.testdata.ConfigService;
import com.oppo.usercenter.test.injector.testdata.SimpleManager;
import com.oppo.usercenter.test.injector.testdata.SimpleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * TestInjectorExtension 集成测试。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
@ExtendWith(TestInjectorExtension.class)
class TestInjectorExtensionTest {

    @Mock
    private SimpleService mockSimpleService;

    @Mock
    private ConfigService mockConfigService;

    @InjectMocks
    private SimpleManager manager;

    @Test
    void should_injectMocks_when_usingExtension() {
        assertThat(manager).isNotNull();
        assertThat(manager.getSimpleService()).isNotNull();
        assertThat(manager.getSimpleService()).isSameAs(mockSimpleService);
    }

    @Test
    void should_allowMockConfiguration_when_usingExtension() {
        when(mockSimpleService.getData()).thenReturn("Test Data");

        String result = manager.process();

        assertThat(result).isEqualTo("Processed: Test Data");
    }
}
