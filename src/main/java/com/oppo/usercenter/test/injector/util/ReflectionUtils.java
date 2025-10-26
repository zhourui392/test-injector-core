package com.oppo.usercenter.test.injector.util;

import com.oppo.usercenter.test.injector.exception.InjectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 反射工具类。
 * 提供构造器选择、字段注入等反射操作。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class ReflectionUtils {

    /**
     * 选择最佳构造器。
     * 策略：优先选择 public 构造器中参数最多的，如果没有 public 构造器，则选择所有构造器中参数最多的。
     *
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 最佳构造器
     */
    public static <T> Constructor<T> selectBestConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length == 0) {
            throw new InjectionException("No constructor found for class: " + clazz.getName());
        }

        // 优先选择 public 构造器
        List<Constructor<?>> publicConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                publicConstructors.add(constructor);
            }
        }

        Constructor<?> selected;
        if (!publicConstructors.isEmpty()) {
            // 选择参数最多的 public 构造器
            selected = publicConstructors.stream()
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElse(null);
        } else {
            // 没有 public 构造器，选择所有构造器中参数最多的
            selected = Arrays.stream(constructors)
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElse(null);
        }

        if (selected == null) {
            throw new InjectionException("Failed to select constructor for class: " + clazz.getName());
        }

        selected.setAccessible(true);
        return (Constructor<T>) selected;
    }

    /**
     * 获取类的所有字段（包括父类）。
     *
     * @param clazz 目标类
     * @return 所有字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * 设置字段值。
     *
     * @param field 目标字段
     * @param target 目标对象
     * @param value 要设置的值
     */
    public static void setField(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new InjectionException(
                    "Failed to set field: " + field.getName() + " on " + target.getClass().getName(), e);
        }
    }

    /**
     * 获取字段值。
     *
     * @param field 目标字段
     * @param target 目标对象
     * @return 字段值
     */
    public static Object getField(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new InjectionException(
                    "Failed to get field: " + field.getName() + " from " + target.getClass().getName(), e);
        }
    }

    /**
     * 检查类是否可实例化。
     *
     * @param clazz 目标类
     * @return true 如果可实例化
     */
    public static boolean isInstantiable(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers) && !clazz.isInterface();
    }
}
