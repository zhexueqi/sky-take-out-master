package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author zhexueqi
 * @ClassName AutoFill
 * @since 2024/2/25    11:32
 */

//自定义注解，用于标识需要进行公共字段自动填充的方法
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoFill {

    //数据库操作类型 Update Insert
    OperationType value();
}
