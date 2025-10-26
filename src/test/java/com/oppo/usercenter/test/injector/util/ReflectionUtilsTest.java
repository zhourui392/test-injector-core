package com.oppo.usercenter.test.injector.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReflectionUtils 单元测试。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
class ReflectionUtilsTest {

    static class NoArgClass {
        public NoArgClass() {}
    }

    static class MultiConstructorClass {
        private String name;
        private int age;

        public MultiConstructorClass() {}

        public MultiConstructorClass(String name) {
            this.name = name;
        }

        public MultiConstructorClass(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class PrivateConstructorClass {
        private String value;

        private PrivateConstructorClass(String value) {
            this.value = value;
        }
    }

    static class Parent {
        private String parentField;
    }

    static class Child extends Parent {
        private String childField;
    }

    interface TestInterface {}
    abstract static class TestAbstract {}

    @Test
    void should_selectNoArgConstructor_when_onlyNoArg() {
        Constructor<NoArgClass> constructor = ReflectionUtils.selectBestConstructor(NoArgClass.class);

        assertThat(constructor).isNotNull();
        assertThat(constructor.getParameterCount()).isEqualTo(0);
    }

    @Test
    void should_selectMaxParamConstructor_when_multipleConstructors() {
        Constructor<MultiConstructorClass> constructor = ReflectionUtils.selectBestConstructor(MultiConstructorClass.class);

        assertThat(constructor).isNotNull();
        assertThat(constructor.getParameterCount()).isEqualTo(2);
    }

    @Test
    void should_selectPrivateConstructor_when_onlyPrivate() {
        Constructor<PrivateConstructorClass> constructor = ReflectionUtils.selectBestConstructor(PrivateConstructorClass.class);

        assertThat(constructor).isNotNull();
        assertThat(constructor.getParameterCount()).isEqualTo(1);
        assertThat(constructor.isAccessible()).isTrue();
    }

    @Test
    void should_getAllFields_when_hasParent() {
        List<Field> fields = ReflectionUtils.getAllFields(Child.class);

        assertThat(fields).hasSizeGreaterThanOrEqualTo(2);
        assertThat(fields.stream().anyMatch(f -> f.getName().equals("childField"))).isTrue();
        assertThat(fields.stream().anyMatch(f -> f.getName().equals("parentField"))).isTrue();
    }

    @Test
    void should_setAndGetField_when_validField() throws NoSuchFieldException {
        // 使用 MultiConstructorClass 的字段进行测试
        MultiConstructorClass instance = new MultiConstructorClass();
        Field field = MultiConstructorClass.class.getDeclaredField("name");

        // 设置字段值
        ReflectionUtils.setField(field, instance, "TestName");

        // 获取字段值
        Object value = ReflectionUtils.getField(field, instance);

        assertThat(value).isEqualTo("TestName");
    }

    @Test
    void should_returnFalse_when_interface() {
        assertThat(ReflectionUtils.isInstantiable(TestInterface.class)).isFalse();
    }

    @Test
    void should_returnFalse_when_abstractClass() {
        assertThat(ReflectionUtils.isInstantiable(TestAbstract.class)).isFalse();
    }

    @Test
    void should_returnTrue_when_concreteClass() {
        assertThat(ReflectionUtils.isInstantiable(NoArgClass.class)).isTrue();
    }
}
