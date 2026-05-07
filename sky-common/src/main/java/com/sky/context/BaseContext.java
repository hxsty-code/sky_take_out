package com.sky.context;

/**
 * 基于ThreadLocal封装工具类，用于保存和获取当前登录用户id
 */
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //设置当前登录用户的ID
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    //获取当前登录用户的ID
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    //清理当前登录用户ID
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
