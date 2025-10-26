package com.oppo.usercenter.test.injector.exception;

import java.util.List;

/**
 * 循环依赖异常。
 * 当检测到依赖链中存在循环引用时抛出此异常。
 *
 * @author zhourui(V33215020)
 * @since 2025/10/26
 */
public class CircularDependencyException extends RuntimeException {

    private final List<Class<?>> dependencyPath;

    /**
     * 构造循环依赖异常。
     *
     * @param message 异常消息
     * @param dependencyPath 依赖路径
     */
    public CircularDependencyException(String message, List<Class<?>> dependencyPath) {
        super(message);
        this.dependencyPath = dependencyPath;
    }

    /**
     * 获取依赖路径。
     *
     * @return 依赖路径列表
     */
    public List<Class<?>> getDependencyPath() {
        return dependencyPath;
    }

    /**
     * 格式化依赖路径为易读的字符串。
     *
     * @return 格式化后的依赖路径
     */
    public String formatDependencyPath() {
        if (dependencyPath == null || dependencyPath.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dependencyPath.size(); i++) {
            if (i > 0) {
                sb.append("\n  → ");
            }
            sb.append(dependencyPath.get(i).getName());
        }
        return sb.toString();
    }
}
