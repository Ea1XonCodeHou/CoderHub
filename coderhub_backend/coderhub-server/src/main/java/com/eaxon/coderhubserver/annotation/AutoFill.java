package com.eaxon.coderhubserver.annotation;

import com.eaxon.coderhubcommon.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
实现自定义注解，用于标识数据库更新方法
实现字段自动填充
 */
@Target(ElementType.METHOD) //表示用于方法的注解，而不是类
@Retention(RetentionPolicy.RUNTIME) //在运行时保留，通过反射读取
public @interface AutoFill {
    OperationType value();
    //之后注解内部需要的指定元素
    //类似于@Api(tags="")的tags
}
