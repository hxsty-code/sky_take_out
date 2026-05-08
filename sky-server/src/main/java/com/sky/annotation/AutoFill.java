package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要自动填充的字段
 */
@Target(ElementType.METHOD)             // 指定注解的作用目标为方法
@Retention(RetentionPolicy.RUNTIME)     // 指定注解的保留策略为运行时
public @interface AutoFill {
    // 用于指定数据库操作类型: INSERT, UPDATE
    OperationType value();
}
