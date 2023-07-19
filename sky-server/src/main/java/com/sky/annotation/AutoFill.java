package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个公共字段的自动填充
 */
@Target(ElementType.METHOD) // 说明注解只能加载方法上面
@Retention(RetentionPolicy.RUNTIME) // 说明注解在运行时有效
public @interface AutoFill
{
    // 注定数据库的操作类型，分别是update和insert，因为只有在插入或者更新的时候才需要进行自动填充
    OperationType value();
}
