package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.constant.MethodConstant;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author zhexueqi
 * @ClassName AutoFillAspect
 * @since 2024/2/25    11:35
 */
@Aspect
@Component
@Slf4j
/*
* 自定义切面类，用于统一拦截加入了AutoFill注解的方法，通过反射为公共字段赋值
* */
public class AutoFillAspect {


    /*
    * 切入点
    * */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointCut() {}


    /*
    * 前置通知
    *
    * */
    @Before("AutoFillPointCut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("正在为公共字段赋值...");

        //获取到当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //获取方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); //获取方法上的注解对象
        OperationType operationType = autoFill.value(); //获取数据库操作类型

        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        //我们约定，insert和update方法的第一个参数为实体对象,所以我们这里直接获取0索引的参数
        Object entity = args[0];

        //准备好要复制的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //如果为增加操作
        if (operationType.equals(OperationType.INSERT)){
            //需要为4个公共字段赋值
            Method setCreateTime =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);

        } else if (operationType.equals(OperationType.UPDATE)) {
            Method setUpdateTime =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser =entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        }


    }

}
