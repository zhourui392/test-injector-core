package com.oppo.usercenter.test.injector.annotation;

import com.oppo.usercenter.test.injector.TestInjector;
import com.oppo.usercenter.test.injector.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * 注解处理器。
 * 扫描并处理 @Mock、@Spy、@InjectMocks 注解。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class AnnotationProcessor {

    private final TestInjector injector;

    /**
     * 构造注解处理器。
     *
     * @param injector TestInjector 实例
     */
    public AnnotationProcessor(TestInjector injector) {
        this.injector = injector;
    }

    /**
     * 处理测试实例的所有注解。
     *
     * @param testInstance 测试实例
     */
    public void processAnnotations(Object testInstance) {
        List<Field> fields = ReflectionUtils.getAllFields(testInstance.getClass());

        // 第一步：处理 @Mock 和 @Spy 注解
        for (Field field : fields) {
            if (field.isAnnotationPresent(Mock.class)) {
                processMockAnnotation(field, testInstance);
            } else if (field.isAnnotationPresent(Spy.class)) {
                processSpyAnnotation(field, testInstance);
            }
        }

        // 第二步：处理 @InjectMocks 注解
        for (Field field : fields) {
            if (field.isAnnotationPresent(InjectMocks.class)) {
                processInjectMocksAnnotation(field, testInstance);
            }
        }
    }

    /**
     * 处理 @Mock 注解。
     *
     * @param field 字段
     * @param testInstance 测试实例
     */
    private void processMockAnnotation(Field field, Object testInstance) {
        Class<?> fieldType = field.getType();
        Object mockInstance = mock(fieldType);

        // 注入到字段
        ReflectionUtils.setField(field, testInstance, mockInstance);

        // 注册到 injector（用于后续依赖注入）
        injector.getInstance(fieldType); // 确保缓存
        registerInstance(fieldType, mockInstance);
    }

    /**
     * 处理 @Spy 注解。
     *
     * @param field 字段
     * @param testInstance 测试实例
     */
    private void processSpyAnnotation(Field field, Object testInstance) {
        Class<?> fieldType = field.getType();

        // 检查字段是否已有值
        Object existingValue = ReflectionUtils.getField(field, testInstance);
        Object spyInstance;

        if (existingValue != null) {
            // 如果字段已有值，spy 该值
            spyInstance = spy(existingValue);
        } else {
            // 如果字段为 null，先创建实例再 spy
            Object realInstance = injector.getInstance(fieldType);
            spyInstance = spy(realInstance);
        }

        // 注入到字段
        ReflectionUtils.setField(field, testInstance, spyInstance);

        // 注册到 injector
        registerInstance(fieldType, spyInstance);
    }

    /**
     * 处理 @InjectMocks 注解。
     *
     * @param field 字段
     * @param testInstance 测试实例
     */
    private void processInjectMocksAnnotation(Field field, Object testInstance) {
        Class<?> fieldType = field.getType();

        // 使用 TestInjector 创建实例（自动注入依赖）
        Object instance = injector.getInstance(fieldType);

        // 注入到字段
        ReflectionUtils.setField(field, testInstance, instance);
    }

    /**
     * 注册实例到 injector（通过反射访问私有字段）。
     *
     * @param clazz 类型
     * @param instance 实例
     */
    private void registerInstance(Class<?> clazz, Object instance) {
        try {
            Field cacheField = TestInjector.class.getDeclaredField("instanceCache");
            cacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Class<?>, Object> cache = (java.util.Map<Class<?>, Object>) cacheField.get(injector);
            cache.put(clazz, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register instance", e);
        }
    }
}
