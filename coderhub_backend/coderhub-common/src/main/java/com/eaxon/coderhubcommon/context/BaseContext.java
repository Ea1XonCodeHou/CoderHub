package com.eaxon.coderhubcommon.context;

/**
 * ThreadLocal工具类
 * 用于在同一线程中传递用户ID等上下文信息
 */
public class BaseContext {

    /**
     * ThreadLocal存储当前登录用户的ID
     */
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     *
     * @param id 用户ID
     */
    public static void setCurrentId(String id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return 用户ID
     */
    public static String getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程的用户ID
     * 重要：请求结束后必须调用，防止内存泄漏
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}

