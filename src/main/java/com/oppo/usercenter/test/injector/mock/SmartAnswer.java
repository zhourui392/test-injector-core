package com.oppo.usercenter.test.injector.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

/**
 * 智能 Answer。
 * 为 Mock 对象自动返回合理的默认值，减少手动 when() 配置。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class SmartAnswer implements Answer<Object> {

    /**
     * 单例实例。
     */
    public static final SmartAnswer INSTANCE = new SmartAnswer();

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Class<?> returnType = invocation.getMethod().getReturnType();

        // 基本类型
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == byte.class || returnType == Byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class || returnType == Short.class) {
            return (short) 0;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (returnType == float.class || returnType == Float.class) {
            return 0.0f;
        }
        if (returnType == double.class || returnType == Double.class) {
            return 0.0;
        }
        if (returnType == char.class || returnType == Character.class) {
            return '\u0000';
        }

        // String
        if (returnType == String.class) {
            return "";
        }

        // 集合类型
        if (returnType == List.class || returnType == ArrayList.class) {
            return Collections.emptyList();
        }
        if (returnType == Set.class || returnType == HashSet.class) {
            return Collections.emptySet();
        }
        if (returnType == Map.class || returnType == HashMap.class) {
            return Collections.emptyMap();
        }

        // Optional
        if (returnType == Optional.class) {
            return Optional.empty();
        }

        // void
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }

        // 对象类型：递归创建 Mock
        if (returnType.isInterface() || java.lang.reflect.Modifier.isAbstract(returnType.getModifiers())) {
            return mock(returnType, this);
        }

        // 其他情况：使用 Mockito 默认行为
        return RETURNS_DEFAULTS.answer(invocation);
    }
}
