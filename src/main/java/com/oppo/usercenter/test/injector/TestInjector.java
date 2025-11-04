package com.oppo.usercenter.test.injector;

import com.oppo.usercenter.test.injector.exception.CircularDependencyException;
import com.oppo.usercenter.test.injector.exception.InjectionException;
import com.oppo.usercenter.test.injector.mock.SmartAnswer;
import com.oppo.usercenter.test.injector.util.ReflectionUtils;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试依赖注入器。
 * 提供轻量级的依赖注入功能，用于简化单元测试中的对象创建和 Mock 配置。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class TestInjector {

    private static final Logger logger = LoggerFactory.getLogger(TestInjector.class);

    /**
     * 类型到实例的缓存。
     */
    private final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 强制 Mock 的类型集合（线程安全）。
     */
    private final Set<Class<?>> forceMockTypes = ConcurrentHashMap.newKeySet();

    /**
     * 强制真实对象的类型集合（线程安全）。
     */
    private final Set<Class<?>> forceRealTypes = ConcurrentHashMap.newKeySet();

    /**
     * 用户注册的实例。
     */
    private final Map<Class<?>, Object> userInstances = new ConcurrentHashMap<>();

    /**
     * 是否启用智能 Mock。
     */
    private boolean smartMockEnabled = false;

    /**
     * 是否启用调试日志。
     */
    private boolean debugEnabled = false;

    /**
     * 循环依赖检测。
     */
    private final ThreadLocal<Set<Class<?>>> creatingTypes = ThreadLocal.withInitial(HashSet::new);

    /**
     * 循环依赖路径记录。
     */
    private final ThreadLocal<List<Class<?>>> dependencyPath = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Mock 决策缓存（性能优化）。
     */
    private final Map<Class<?>, Boolean> mockDecisionCache = new ConcurrentHashMap<>();

    /**
     * 静态方法：快速创建实例。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 创建的实例
     */
    public static <T> T createInstance(Class<T> clazz) {
        return new TestInjector().getInstance(clazz);
    }

    /**
     * 静态方法：获取 Builder。
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 静态方法：初始化 Mockito 注解（用于 JUnit 5 扩展）。
     *
     * @param testInstance 测试实例
     * @return AutoCloseable 用于资源清理
     */
    public static AutoCloseable initMocks(Object testInstance) {
        return MockitoAnnotations.openMocks(testInstance);
    }

    /**
     * 获取实例（如果不存在则创建）。
     * 线程安全的获取或创建实例方法。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 实例
     */
    public <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        // 先检查缓存（避免 computeIfAbsent 嵌套调用问题）
        Object cached = instanceCache.get(clazz);
        if (cached != null) {
            debugLog("Return cached instance for: {}", clazz.getName());
            return (T) cached;
        }

        // 检查用户注册的实例
        Object userInstance = userInstances.get(clazz);
        if (userInstance != null) {
            instanceCache.put(clazz, userInstance);
            debugLog("Return user registered instance for: {}", clazz.getName());
            return (T) userInstance;
        }

        // 使用 computeIfAbsent 原子地创建实例
        return (T) instanceCache.computeIfAbsent(clazz, k -> {
            debugLog("Creating new instance for: {}", k.getName());
            return createNewInstance((Class<?>) k);
        });
    }

    /**
     * 创建新实例。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 新实例
     */
    private <T> T createNewInstance(Class<T> clazz) {
        // 循环依赖检测
        if (creatingTypes.get().contains(clazz)) {
            dependencyPath.get().add(clazz);
            throw new CircularDependencyException(
                    "Circular dependency detected",
                    new ArrayList<>(dependencyPath.get())
            );
        }

        creatingTypes.get().add(clazz);
        dependencyPath.get().add(clazz);

        try {
            // 决策：Mock 还是真实对象
            if (shouldMock(clazz)) {
                debugLog("Creating mock for: {}", clazz.getName());
                return createMock(clazz);
            } else {
                debugLog("Creating real instance for: {}", clazz.getName());
                return createRealInstance(clazz);
            }
        } finally {
            creatingTypes.get().remove(clazz);
            dependencyPath.get().remove(dependencyPath.get().size() - 1);
        }
    }

    /**
     * 判断是否应该 Mock。
     * 使用缓存提高性能。
     *
     * @param clazz 目标类
     * @return true 如果应该 Mock
     */
    private boolean shouldMock(Class<?> clazz) {
        return mockDecisionCache.computeIfAbsent(clazz, k -> {
            // 强制 Mock
            if (forceMockTypes.contains(k)) {
                return true;
            }

            // 强制真实对象
            if (forceRealTypes.contains(k)) {
                return false;
            }

            // 默认策略：接口或抽象类 → Mock，具体类 → 真实对象
            return k.isInterface() || Modifier.isAbstract(k.getModifiers());
        });
    }

    /**
     * 创建 Mock 对象。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return Mock 对象
     */
    private <T> T createMock(Class<T> clazz) {
        if (smartMockEnabled) {
            MockSettings settings = Mockito.withSettings().defaultAnswer(SmartAnswer.INSTANCE);
            return Mockito.mock(clazz, settings);
        } else {
            return Mockito.mock(clazz);
        }
    }

    /**
     * 创建真实对象实例。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 真实对象实例
     */
    private <T> T createRealInstance(Class<T> clazz) {
        if (!ReflectionUtils.isInstantiable(clazz)) {
            throw new InjectionException("Cannot instantiate abstract class or interface: " + clazz.getName());
        }

        Constructor<T> constructor = ReflectionUtils.selectBestConstructor(clazz);
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length == 0) {
            // 无参构造器
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new InjectionException("Failed to create instance of " + clazz.getName(), e);
            }
        } else {
            // 有参构造器：递归创建依赖
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = getInstance(parameterTypes[i]);
            }

            try {
                return constructor.newInstance(args);
            } catch (Exception e) {
                throw new InjectionException("Failed to create instance of " + clazz.getName(), e);
            }
        }
    }

    /**
     * 输出调试日志。
     *
     * @param format 日志格式
     * @param args 参数
     */
    private void debugLog(String format, Object... args) {
        if (debugEnabled) {
            logger.debug(format, args);
        }
    }

    /**
     * 清理 ThreadLocal 数据，防止内存泄漏。
     * 应在测试结束后调用。
     */
    public void clearThreadLocalData() {
        creatingTypes.remove();
        dependencyPath.remove();
    }

    /**
     * 重置所有缓存和配置。
     * 用于测试隔离或重新初始化。
     */
    public void reset() {
        instanceCache.clear();
        userInstances.clear();
        forceMockTypes.clear();
        forceRealTypes.clear();
        mockDecisionCache.clear();
        clearThreadLocalData();
    }

    /**
     * 公开方法：注册实例到缓存。
     * 用于 AnnotationProcessor 避免反射访问。
     *
     * @param clazz 类型
     * @param instance 实例
     */
    public void registerInstanceInternal(Class<?> clazz, Object instance) {
        instanceCache.put(clazz, instance);
    }

    /**
     * Builder 类。
     * 提供流畅的 API 用于配置 TestInjector。
     */
    public static class Builder {

        private final TestInjector injector = new TestInjector();
        private Class<?> targetClass;

        /**
         * 指定要自动创建的目标类。
         *
         * @param clazz 目标类
         * @param <T> 类型参数
         * @return TypedBuilder
         */
        public <T> TypedBuilder<T> autoCreate(Class<T> clazz) {
            this.targetClass = clazz;
            return new TypedBuilder<>(this, clazz);
        }

        /**
         * 强制 Mock 指定类型。
         *
         * @param classes 类型数组
         * @return Builder
         */
        public Builder mock(Class<?>... classes) {
            injector.forceMockTypes.addAll(Arrays.asList(classes));
            return this;
        }

        /**
         * 强制创建真实对象。
         *
         * @param classes 类型数组
         * @return Builder
         */
        public Builder real(Class<?>... classes) {
            injector.forceRealTypes.addAll(Arrays.asList(classes));
            return this;
        }

        /**
         * 注册用户实例。
         *
         * @param clazz 类型
         * @param instance 实例
         * @param <T> 类型参数
         * @return Builder
         */
        public <T> Builder instance(Class<T> clazz, T instance) {
            injector.userInstances.put(clazz, instance);
            return this;
        }

        /**
         * 创建 Mock 对象并配置。
         *
         * @param clazz 类型
         * @param configurator 配置器
         * @param <T> 类型参数
         * @return Builder
         */
        public <T> Builder mockWith(Class<T> clazz, MockConfigurator<T> configurator) {
            injector.forceMockTypes.add(clazz);
            T mockInstance = injector.createMock(clazz);
            configurator.configure(mockInstance);
            injector.userInstances.put(clazz, mockInstance);
            return this;
        }

        /**
         * 启用智能 Mock。
         *
         * @return Builder
         */
        public Builder enableSmartMock() {
            injector.smartMockEnabled = true;
            return this;
        }

        /**
         * 启用调试日志。
         *
         * @return Builder
         */
        public Builder enableDebug() {
            injector.debugEnabled = true;
            return this;
        }

        /**
         * 构建 TestInjector 实例。
         *
         * @return TestInjector
         */
        public TestInjector buildInjector() {
            return injector;
        }

        /**
         * 构建目标对象。
         *
         * @param <T> 类型参数
         * @return 构建的对象
         */
        public <T> T build() {
            if (targetClass == null) {
                throw new IllegalStateException("Target class not specified. Use autoCreate() first.");
            }
            return (T) injector.getInstance(targetClass);
        }
    }

    /**
     * 类型化 Builder。
     *
     * @param <T> 类型参数
     */
    public static class TypedBuilder<T> {

        private final Builder builder;
        private final Class<T> targetClass;

        public TypedBuilder(Builder builder, Class<T> targetClass) {
            this.builder = builder;
            this.targetClass = targetClass;
        }

        /**
         * 强制 Mock 指定类型。
         *
         * @param classes 类型数组
         * @return TypedBuilder
         */
        public TypedBuilder<T> mock(Class<?>... classes) {
            builder.mock(classes);
            return this;
        }

        /**
         * 强制创建真实对象。
         *
         * @param classes 类型数组
         * @return TypedBuilder
         */
        public TypedBuilder<T> real(Class<?>... classes) {
            builder.real(classes);
            return this;
        }

        /**
         * 注册用户实例。
         *
         * @param clazz 类型
         * @param instance 实例
         * @param <U> 类型参数
         * @return TypedBuilder
         */
        public <U> TypedBuilder<T> instance(Class<U> clazz, U instance) {
            builder.instance(clazz, instance);
            return this;
        }

        /**
         * 创建 Mock 对象并配置。
         *
         * @param clazz 类型
         * @param configurator 配置器
         * @param <U> 类型参数
         * @return TypedBuilder
         */
        public <U> TypedBuilder<T> mockWith(Class<U> clazz, MockConfigurator<U> configurator) {
            builder.mockWith(clazz, configurator);
            return this;
        }

        /**
         * 启用智能 Mock。
         *
         * @return TypedBuilder
         */
        public TypedBuilder<T> enableSmartMock() {
            builder.enableSmartMock();
            return this;
        }

        /**
         * 启用调试日志。
         *
         * @return TypedBuilder
         */
        public TypedBuilder<T> enableDebug() {
            builder.enableDebug();
            return this;
        }

        /**
         * 构建目标对象。
         *
         * @return 构建的对象
         */
        public T build() {
            return builder.injector.getInstance(targetClass);
        }
    }
}
