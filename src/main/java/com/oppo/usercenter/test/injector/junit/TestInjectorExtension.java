package com.oppo.usercenter.test.injector.junit;

import com.oppo.usercenter.test.injector.TestInjector;
import com.oppo.usercenter.test.injector.annotation.AnnotationProcessor;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockitoAnnotations;

/**
 * JUnit 5 扩展。
 * 自动初始化 Mockito 注解和 TestInjector。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class TestInjectorExtension implements BeforeEachCallback, AfterEachCallback {

    private static final String CLOSEABLE_KEY = "mockito.closeable";
    private static final String INJECTOR_KEY = "test.injector";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();

        // 初始化 Mockito 注解
        AutoCloseable closeable = MockitoAnnotations.openMocks(testInstance);
        getStore(context).put(CLOSEABLE_KEY, closeable);

        // 创建 TestInjector 实例
        TestInjector injector = new TestInjector();
        getStore(context).put(INJECTOR_KEY, injector);

        // 处理注解
        AnnotationProcessor processor = new AnnotationProcessor(injector);
        processor.processAnnotations(testInstance);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        // 清理 Mockito 资源
        AutoCloseable closeable = getStore(context).get(CLOSEABLE_KEY, AutoCloseable.class);
        if (closeable != null) {
            closeable.close();
        }

        // 清理存储
        getStore(context).remove(CLOSEABLE_KEY);
        getStore(context).remove(INJECTOR_KEY);
    }

    /**
     * 获取 ExtensionContext 的 Store。
     *
     * @param context ExtensionContext
     * @return Store
     */
    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
}
